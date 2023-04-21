package io.legacymoddingmc.mappinggenerator;

import io.legacymoddingmc.mappinggenerator.source.IMappingSource;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        out.getParentFile().mkdirs();
        try(FileWriter fw = new FileWriter(out)) {
            fw.write("param,name,side\n" +
                    "p_71247_1_,helloGradle,2\n");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
