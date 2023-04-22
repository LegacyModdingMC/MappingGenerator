package io.legacymoddingmc.mappinggenerator.download;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.gson.Gson;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import io.legacymoddingmc.mappinggenerator.GradleUtils;
import io.legacymoddingmc.mappinggenerator.JavaHelper;
import io.legacymoddingmc.mappinggenerator.Mapping;
import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.name.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.gradle.api.Project;
import org.gradle.api.tasks.WorkResult;
import org.gradle.cache.internal.Loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.legacymoddingmc.mappinggenerator.MappingHelper.remapDesc;

public class YarnConnection implements MappingConnection {

    @Getter
    private final String gameVersion; // 1.7.10
    @Getter
    private final String mappingVersion; // 1.7.10+build.458

    private final Project project;
    private final File dir;
    private final String url;

    public YarnConnection(Project project, String mappingVersion) {
        String[] versionParts = mappingVersion.split("\\+");
        Preconditions.checkState(versionParts.length == 2);
        this.gameVersion = versionParts[0];
        mappingVersion = resolveMappingVersion(mappingVersion);
        this.mappingVersion = mappingVersion;

        url = "https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/yarn/"+mappingVersion+"/yarn-"+mappingVersion+"-mergedv2.jar";
        dir = FileUtils.getFile(GradleUtils.getCacheDir(project), "mappings", "yarn", mappingVersion+"-mergedv2");
        this.project = project;
    }

    @SneakyThrows
    private String resolveMappingVersion(String version) {
        if(version.endsWith("+latest")) {
            try(InputStream stream = new URL("https://meta.legacyfabric.net/v1/versions/loader/" + gameVersion).openStream()) {
                LoaderVersionMeta[] meta = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(stream)), LoaderVersionMeta[].class);
                return meta[0].mappings.version;
            }
        } else {
            return version;
        }
    }

    private boolean isUpToDate() {
        return new File(dir, "mappings/mappings.tiny").exists();
    }

    @SneakyThrows
    public File getDir() {
        if(!isUpToDate()) {
            File outFile = new File(dir, JavaHelper.getLast(url.split("/")));
            FileUtils.copyURLToFile(new URL(url), outFile);
            WorkResult work = project.copy(a -> {
                a.from(project.zipTree(outFile));
                a.into(dir);
            });
            outFile.delete();
        }
        return dir;
    }

    private static final String[] TINY_FIRST_LINE = new String[]{"tiny", "2", "0", "official", "intermediary", "named"};

    @Override
    @SneakyThrows
    public void addTo(MappingCollection mappings) {
        getDir();

        val tiny = Files
                .lines(new File(dir, "mappings/mappings.tiny").toPath())
                .map(line -> Arrays.stream(line.split("\\t")).map(x -> x.trim()).toArray(String[]::new))
                .collect(Collectors.toList());
        
        val notch2intermediary = new Mapping("notch", "intermediary");
        val intermediary2yarn = new Mapping("intermediary", "yarn");
        
        Preconditions.checkState(Arrays.equals(tiny.get(0), TINY_FIRST_LINE));
        
        for(val entry : tiny) {
            if(entry[0].equals("c")) {
                notch2intermediary.put(new Klass(entry[1]), new Klass(entry[2]));
                intermediary2yarn.put(new Klass(entry[2]), new Klass(entry[3]));
            }
        }

        String cls = null;
        Name notchFull = null, intermediaryFull = null, yarnFull = null;
        for(val entry : tiny) {
            if(entry[0].equals("c")) {
                cls = entry[1];
            } else if(entry[0].equals("")) {
                if(entry[1].equals("f") || entry[1].equals("m")) {
                    boolean isField = entry[1].equals("f");
                    val desc = isField ? null : entry[2];
                    val intermediaryDesc = isField ? null : remapDesc(entry[2], notch2intermediary);
                    notchFull = NameHelper.create(isField ? Field.class : Method.class, cls, entry[3], desc);
                    val clsIntermediary = notch2intermediary.get(new Klass(cls));
                    intermediaryFull = NameHelper.create(notchFull.getClass(), clsIntermediary.getKlass(), entry[4], intermediaryDesc);
                    val clsYarn = intermediary2yarn.get(clsIntermediary);
                    yarnFull = NameHelper.create(notchFull.getClass(), clsYarn.getKlass(), entry[5], intermediaryDesc);
                    notch2intermediary.put(notchFull, intermediaryFull);
                    intermediary2yarn.put(intermediaryFull, yarnFull);
                } else if(entry[1].equals("") && entry[2].equals("p")) {
                    val parameterIdx = Integer.parseInt(entry[3]);
                    val notchParamFull = new Parameter((Method)notchFull, parameterIdx, null);
                    val intermediaryParamFull = new Parameter((Method)intermediaryFull, parameterIdx, null);
                    val yarnParamFull = new Parameter((Method)yarnFull, parameterIdx, entry[6]);

                    notch2intermediary.put(notchParamFull, intermediaryParamFull);
                    intermediary2yarn.put(intermediaryParamFull, yarnParamFull);
                }
            }
        }
        
        mappings.put(gameVersion, notch2intermediary, notch2intermediary.inverse(), intermediary2yarn, intermediary2yarn.inverse());
    }

    public static class LoaderVersionMeta {
        Mappings mappings;
        public static class Mappings {
            public String version;
        }
    }
}
