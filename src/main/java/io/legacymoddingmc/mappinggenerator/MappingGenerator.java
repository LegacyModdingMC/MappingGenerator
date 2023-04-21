package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.source.IMappingSource;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingGenerator {

    private final Project project;
    private final List<IMappingSource> sources = new ArrayList<>();

    public MappingGenerator(Project project) {
        this.project = project;
    }

    public void addSource(IMappingSource source) {
        sources.add(source);
    }

    public void generateExtraParameters(File out) {
        MappingCollection mappings = new MappingCollection();

        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "client.jar"));
        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "server.jar"));

        mappings.loadMcp("1.7.10", "stable_12");

        Map<String, String> extraParameters = new HashMap<>();

        for(IMappingSource source : sources) {
            mappings.loadNecessaryMappingsFor(source);
            source.generateExtraParameters(extraParameters);
        }

        writeMappings(extraParameters, out);
    }

    private void writeMappings(Map<String, String> parameters, File out) {
        out.getParentFile().mkdirs();
        try(FileWriter fw = new FileWriter(out)) {
            fw.write("param,name,side\n");
            for(Map.Entry<String, String> mapping : parameters.entrySet()) {
                fw.write(mapping.getKey() + "," + mapping.getValue() + ",-1\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
