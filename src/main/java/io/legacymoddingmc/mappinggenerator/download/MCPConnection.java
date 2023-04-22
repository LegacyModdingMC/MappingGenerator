package io.legacymoddingmc.mappinggenerator.download;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.shadow.com.opencsv.CSVReader;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.ObjectUtils;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.Mapping;
import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.name.Field;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

public class MCPConnection implements MappingConnection {

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;
    @Getter
    private final File dir;

    public MCPConnection(Project project, String gameVersion, String mappingVersion) {
        this.gameVersion = gameVersion;
        this.mappingVersion = mappingVersion;
        String[] mappingVersionParts = mappingVersion.split("_");
        Preconditions.checkState(mappingVersionParts.length == 2);
        this.dir = Utilities.getRawCacheDir(project, "minecraft", "de", "oceanlabs", "mcp", "mcp_" + mappingVersionParts[0], mappingVersionParts[1]);
    }

    private boolean isUpToDate() {
        return new File(dir, "methods.csv").exists();
    }

    @Override
    public void addTo(MappingCollection mappings) {
        val methods = readCSV(new File(dir, "methods.csv"));
        val fields = readCSV(new File(dir, "fields.csv"));
        val params = readCSV(new File(dir, "params.csv"));
        
        val mapping = new Mapping("srg", "mcp");
        
        for(val line : fields.subList(1, fields.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val desc = line[3];
            val id = searge.split("_")[1];
            val fullNotch = mappings.translate(new Field(null, id, null), gameVersion, "srgId", "notch");
            val fullSrg = mappings.translate(fullNotch, gameVersion, "notch", "srg");

            if (fullSrg != null) {
                mapping.put(fullSrg, new Field(fullSrg.getKlass(), name, fullSrg.getDesc()));
            }
        }
        
        for(val line : methods.subList(1, methods.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val desc = line[3];
            val id = searge.split("_")[1];
            val fullNotch = mappings.translate(new Method(null, id, null), gameVersion, "srgId", "notch");
            val fullSrg = mappings.translate(fullNotch, gameVersion, "notch", "srg");

            if(fullSrg != null) {
                mapping.put(fullSrg, new Method(fullSrg.getKlass(), name, fullSrg.getDesc()));
            }
        }
        
        for(val line : params.subList(1, params.size())) {
            val searge = line[0];
            val name = line[1];
            val side = line[2];
            val id = searge.split("_")[1];
            val methodFullNotch = mappings.translate(new Method(null, id, null), gameVersion, "srgId", "notch");

            for(val methodFullSrg : mappings.multiTranslate(methodFullNotch, gameVersion, "notch", "srg")) {
                if(methodFullSrg == null) {
                    System.err.println("Failed to find method for parameter " + searge + " in " + dir.getName());
                    continue;
                }

                int paramIndex = Integer.parseInt(searge.split("_")[2]) - 1;

                mapping.put(new Parameter(methodFullSrg, paramIndex, searge), new Parameter(ObjectUtils.defaultIfNull(mapping.get(methodFullSrg), methodFullSrg), paramIndex, name));
            }
        }

        mappings.put(mapping, mapping.inverse());
    }

    @SneakyThrows
    private static List<String[]> readCSV(File file) {
        try(CSVReader reader = Utilities.createCsvReader(file)) {
            return reader.readAll();
        }
    }
}
