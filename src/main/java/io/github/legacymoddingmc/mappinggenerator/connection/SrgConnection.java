package io.github.legacymoddingmc.mappinggenerator.connection;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import io.github.legacymoddingmc.mappinggenerator.Mapping;
import io.github.legacymoddingmc.mappinggenerator.MappingCollection;
import io.github.legacymoddingmc.mappinggenerator.name.Field;
import io.github.legacymoddingmc.mappinggenerator.name.Klass;
import io.github.legacymoddingmc.mappinggenerator.name.Method;
import io.github.legacymoddingmc.mappinggenerator.util.BytecodeUtils;
import io.github.legacymoddingmc.mappinggenerator.util.GradleUtils;
import io.github.legacymoddingmc.mappinggenerator.util.JavaHelper;
import io.github.legacymoddingmc.mappinggenerator.util.MappingHelper;
import io.github.legacymoddingmc.mappinggenerator.name.Parameter;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.tasks.WorkResult;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SrgConnection implements MappingConnection {

    private final String gameVersion; // 1.7.10

    private final File dir;
    private String url;

    private final Project project;

    public SrgConnection(Project project, String gameVersion) {
        this.gameVersion = gameVersion;
        dir = FileUtils.getFile(GradleUtils.getCacheDir(project), "mappings", "srg", gameVersion);
        url = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + gameVersion + "/mcp-" + gameVersion + "-srg.zip";
        this.project = project;
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

    private boolean isUpToDate() {
        return new File(dir, "joined.srg").exists();
    }

    @SneakyThrows
    @Override
    public void addTo(MappingCollection mappings) {
        getDir();

        List<String[]> joined = Files
                .lines(new File(dir, "joined.srg").toPath())
                .map(l -> l.trim().split(" "))
                .collect(Collectors.toList());
        Map<String, String> constructorInfos = Files
                .lines(new File(dir, "joined.exc").toPath())
                .filter(x -> !x.startsWith("#") && x.contains(".<init>"))
                .map(l -> l.trim().split("="))
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));

        Mapping notch2srg = new Mapping("notch", "srg");
        Mapping srgId2notch = new Mapping("srgId", "notch");

        for(String[] line : joined) {
            if(line[0].equals("CL:")) {
                notch2srg.put(new Klass(line[1]), new Klass(line[2]));
            } else if(line[0].equals("FD:")) {
                String[] notchParts = JavaHelper.replaceLastSlashWithSpace(line[1]).split(" ");
                Field notchFull = new Field(notchParts[0], notchParts[1], null);

                String[] srgParts = JavaHelper.replaceLastSlashWithSpace(line[2]).split(" ");
                Field srgFull = new Field(srgParts[0], srgParts[1], null);

                notch2srg.put(notchFull, srgFull);

                String lastWord = JavaHelper.getLast(line[2].split("/"));
                if(lastWord.startsWith("field_")) {
                    String id = lastWord.split("_")[1];
                    srgId2notch.put(new Field(null, id, null), notchFull);
                }
            } else if(line[0].equals("MD:")) {
                String[] notchParts = JavaHelper.replaceLastSlashWithSpace(line[1]).split(" ");
                String notchClass = notchParts[0];
                String notchMethod = notchParts[1];
                String notchDesc = line[2];
                Method notchFull = new Method(notchClass, notchMethod, notchDesc);
                String[] srgParts = JavaHelper.replaceLastSlashWithSpace(line[3]).split(" ");
                Method srgFull = new Method(srgParts[0], srgParts[1], line[4]);
                notch2srg.put(notchFull, srgFull);

                String lastWord = JavaHelper.getLast(line[3].split("/"));
                if(lastWord.startsWith("func_")) {
                    String id = lastWord.split("_")[1];
                    srgId2notch.put(new Method(null, id, null), notchFull);

                    boolean isStatic = mappings.getJarInfo(gameVersion).getMethodInfo(notchFull).isStatic();

                    for(int parameterIdx : BytecodeUtils.getParameterIndexes(notchDesc, isStatic)) {
                        String paramName = "p_" + id + "_" + parameterIdx + "_";
                        notch2srg.put(new Parameter(notchFull, parameterIdx, null), new Parameter(srgFull, parameterIdx, paramName));
                    }

                    srgId2notch.put(new Method(null, id, null), notchFull);
                }
            }
        }
        
        Mapping srg2notch = notch2srg.inverse();
        
        for(String[] line : joined) {
            if(line[0].equals("CL:")) {
                for(Map.Entry<String, String> e : constructorInfos.entrySet()) {
                    String k = e.getKey();
                    String v = e.getValue();
                    if(k.startsWith(line[2] + ".<init>")) {
                        Preconditions.checkState(v.indexOf('|') == v.lastIndexOf('|'));
                        
                        Klass srgClazz = new Klass(k.substring(0, k.indexOf('.')));
                        Klass notchClazz = srg2notch.get(srgClazz);
                        String srgDesc = k.substring(k.indexOf('>') + 1);
                        String notchDesc = MappingHelper.remapDesc(srgDesc, srg2notch);
                        Method notchFull = new Method(notchClazz.getKlass(), "<init>", notchDesc);
                        Method srgFull = new Method(srgClazz.getKlass(), "<init>", srgDesc);
                    
                        notch2srg.put(notchFull, srgFull);
                        
                        String[] paramNames = v.substring(v.indexOf('|') + 1).split(",");
                        
                        srgId2notch.put(new Method(null, paramNames[0].split("_")[1], null), notchFull);
                    
                        for(String paramName : paramNames) {
                            int parameterIdx = Integer.parseInt(paramName.split("_")[2]);
                            notch2srg.put(new Parameter(notchFull, parameterIdx, null), new Parameter(srgFull, parameterIdx, paramName));
                        }
                    }
                }
            }
        }
        
        mappings.put(gameVersion, notch2srg, notch2srg.inverse(), srgId2notch);
    }

    public static String toSrgId(String srgMethodOrParameterName) {
        return srgMethodOrParameterName.split("_")[1];
    }
}
