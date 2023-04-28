package io.legacymoddingmc.mappinggenerator.connection;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.ObjectUtils;
import io.legacymoddingmc.mappinggenerator.*;
import io.legacymoddingmc.mappinggenerator.name.Field;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import io.legacymoddingmc.mappinggenerator.util.GradleUtils;
import io.legacymoddingmc.mappinggenerator.util.IOHelper;
import io.legacymoddingmc.mappinggenerator.util.JavaHelper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.WorkResult;

import java.io.File;

public class MCPConnection implements MappingConnection {

    @Getter
    private final String gameVersion; // 1.7.10
    @Getter
    private final String mappingVersion; // stable_12
    private final File dir;

    private final Project project;
    private final String url;

    public MCPConnection(Project project, String gameVersion, String mappingVersion) {
        this.gameVersion = gameVersion;
        this.mappingVersion = mappingVersion;
        String[] mappingVersionParts = mappingVersion.split("_");
        Preconditions.checkState(mappingVersionParts.length == 2);
        String chan = mappingVersionParts[0];
        String ver = mappingVersionParts[1];
        dir = FileUtils.getFile(GradleUtils.getCacheDir(project), "mappings", "mcp", mappingVersion + "-" + gameVersion);
        url = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_"+chan+"/"+ver+"-"+gameVersion+"/mcp_"+chan+"-"+ver+"-"+gameVersion+".zip";
        this.project = project;
    }

    private boolean isUpToDate() {
        return new File(dir, "methods.csv").exists();
    }

    @SneakyThrows
    public File getDir() {
        if(!isUpToDate()) {
            File outFile = new File(dir, JavaHelper.getLast(url.split("/")));
            GradleUtils.downloadFile(url, outFile, project);
            WorkResult work = project.copy(a -> {
                a.from(project.zipTree(outFile));
                a.into(dir);
            });
            outFile.delete();
        }
        return dir;
    }

    @Override
    public void addTo(MappingCollection mappings) {
        getDir();
        val methods = IOHelper.readCSV(new File(dir, "methods.csv"));
        val fields = IOHelper.readCSV(new File(dir, "fields.csv"));
        val params = IOHelper.readCSV(new File(dir, "params.csv"));
        
        val mapping = new Mapping("srg", "mcp");
        
        for(val line : fields.subList(1, fields.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val desc = line[3];
            val id = searge.split("_")[1];
            for(val fullNotch : mappings.multiTranslate(new Field(null, id, null), gameVersion, "srgId", "notch")) {
                val fullSrg = mappings.translate(fullNotch, gameVersion, "notch", "srg");

                if (fullSrg != null) {
                    mapping.put(fullSrg, new Field(fullSrg.getKlass(), name, fullSrg.getDesc()));
                }
            }
        }
        
        for(val line : methods.subList(1, methods.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val desc = line[3];
            val id = searge.split("_")[1];
            for(val fullNotch : mappings.multiTranslate(new Method(null, id, null), gameVersion, "srgId", "notch")) {
                val fullSrg = mappings.translate(fullNotch, gameVersion, "notch", "srg");

                if (fullSrg != null) {
                    mapping.put(fullSrg, new Method(fullSrg.getKlass(), name, fullSrg.getDesc()));
                }
            }
        }
        
        for(val line : params.subList(1, params.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val id = searge.split("_")[1];
            for(val methodFullNotch : mappings.multiTranslate(new Method(null, id, null), gameVersion, "srgId", "notch")) {
                val methodFullSrg = mappings.translate(methodFullNotch, gameVersion, "notch", "srg");
                if (methodFullSrg == null) {
                    System.err.println("Failed to find method for parameter " + searge + " in " + dir.getName());
                    continue;
                }

                int paramIndex = Integer.parseInt(searge.split("_")[2]) - 1;

                mapping.put(new Parameter(methodFullSrg, paramIndex, searge), new Parameter(ObjectUtils.defaultIfNull(mapping.get(methodFullSrg), methodFullSrg), paramIndex, name));
            }
        }

        mappings.put(gameVersion, mapping, mapping.inverse());
    }
}
