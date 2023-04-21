/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.IMinecraftyExtension;
import io.legacymoddingmc.mappinggenerator.source.MCPSource;
import io.legacymoddingmc.mappinggenerator.source.YarnSource;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.Arrays;

/**
 * A simple 'hello world' plugin.
 */
public class MappingGeneratorPlugin implements Plugin<Project> {
    public void apply(Project project) {
        File outFile = new File(project.getBuildDir(), "extra-mappings/parameters.csv");

        // Register a task
        TaskProvider<?> taskGenerateExtraMappings = project.getTasks().register("generateExtraMappings", task -> {
            task.doLast(s -> {
                System.out.println("Running generateExtraMappings!");
                MappingGenerator generator = new MappingGenerator(project);
                generator.addSource(new MCPSource("1.7.10", "stable_12", MCPSource.Type.METHOD_COMMENTS));
                generator.addSource(new MCPSource("1.12.2", "stable_39", MCPSource.Type.PARAMETERS));
                generator.addSource(new YarnSource("1.7.10", "latest"));
                generator.generateExtraParameters(outFile);
            });
            task.dependsOn("downloadVanillaJars", "generateForgeSrgMappings");
        });
        project.getTasks().getByName("remapDecompiledJar").dependsOn(taskGenerateExtraMappings);

        IMinecraftyExtension minecraft = (IMinecraftyExtension)project.getExtensions().getByName("minecraft");
        minecraft.getExtraParamsCsvs().from(outFile);
    }
}
