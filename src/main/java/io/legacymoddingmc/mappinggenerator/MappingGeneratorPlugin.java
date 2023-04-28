/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.IMinecraftyExtension;
import com.gtnewhorizons.retrofuturagradle.mcp.RemapSourceJarTask;
import io.legacymoddingmc.mappinggenerator.source.MCPSource;
import io.legacymoddingmc.mappinggenerator.source.YarnSource;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

/**
 * A simple 'hello world' plugin.
 */
public class MappingGeneratorPlugin implements Plugin<Project> {
    public void apply(Project project) {
        File outFile = new File(project.getBuildDir(), "extra-mappings/parameters.csv");

        TaskProvider<?> taskPreGenerateExtraMappings = project.getTasks().register("preGenerateExtraMappings", task -> {
            task.doLast(s -> {
                boolean doIWantToRun = true;
                if(doIWantToRun) {
                    RemapSourceJarTask taskRemapDecompiledJar = (RemapSourceJarTask)project.getTasks().getByName("remapDecompiledJar");
                    File remappedJar = taskRemapDecompiledJar.getOutputJar().get().getAsFile();
                    if(remappedJar.isFile()) {
                        System.out.println("Deleting " + remappedJar.getName() + " to force the decompilation chain to re-run!");
                        remappedJar.delete();
                    }
                } else {
                    project.getTasks().getByName("generateExtraMappings").setEnabled(false);
                }
            });
        });
        project.getTasks().getByName("mergeVanillaSidedJars").dependsOn(taskPreGenerateExtraMappings);
        // We want to analyze the Forge-patched Minecraft jar, but it only exists while RFG's deobfuscation task chain
        // is running! So, we must force RFG to run decompilation in order to access it.

        TaskProvider<?> taskGenerateExtraMappings = project.getTasks().register("generateExtraMappings", task -> {
            task.doLast(s -> {
                System.out.println("Running generateExtraMappings!");
                MappingGenerator generator = new MappingGenerator(project);
                generator.addSource(new YarnSource("1.7.10+latest"));
                generator.addSource(new MCPSource("1.7.10", "stable_12", MCPSource.Type.METHOD_COMMENTS));
                generator.addSource(new MCPSource("1.8.9", "stable_22", MCPSource.Type.PARAMETERS));
                generator.addSource(new MCPSource("1.12", "stable_39", MCPSource.Type.PARAMETERS));
                //generator.addSource(new CSVSource("http://localhost:8000/test.csv"));
                generator.generateExtraParameters(outFile);
            });
            task.dependsOn("downloadVanillaJars", "patchDecompiledJar");
        });
        project.getTasks().getByName("remapDecompiledJar").dependsOn(taskGenerateExtraMappings);

        IMinecraftyExtension minecraft = (IMinecraftyExtension)project.getExtensions().getByName("minecraft");
        minecraft.getExtraParamsCsvs().from(outFile);
    }
}
