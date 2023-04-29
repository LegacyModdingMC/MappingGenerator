/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.IMinecraftyExtension;
import com.gtnewhorizons.retrofuturagradle.mcp.RemapSourceJarTask;
import io.legacymoddingmc.mappinggenerator.source.CSVSource;
import io.legacymoddingmc.mappinggenerator.source.MCPSource;
import io.legacymoddingmc.mappinggenerator.source.MappingSourceFactory;
import io.legacymoddingmc.mappinggenerator.source.YarnSource;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.Arrays;

/**
 * A simple 'hello world' plugin.
 */
public class MappingGeneratorPlugin implements Plugin<Project> {
    public void apply(Project project) {
        File outFile = new File(project.getBuildDir(), "extra-mappings/parameters.csv");

        val ext = project.getExtensions().create("mappingGenerator", MappingGeneratorExtension.class);
        ext.getSources().convention(Arrays.asList(DefaultSources.DEFAULT_SOURCES));

        registerDefaultSources();

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
                for(String[] mappingSourceSpec : ext.getSources().get()) {
                    generator.addSource(MappingSourceFactory.fromSpec(mappingSourceSpec));
                }
                generator.generateExtraParameters(outFile);
            });
            task.dependsOn("downloadVanillaJars", "patchDecompiledJar");
        });
        project.getTasks().getByName("remapDecompiledJar").dependsOn(taskGenerateExtraMappings);

        IMinecraftyExtension minecraft = (IMinecraftyExtension)project.getExtensions().getByName("minecraft");
        minecraft.getExtraParamsCsvs().from(outFile);
    }

    private void registerDefaultSources() {
        MappingSourceFactory.register("mcp", MCPSource::fromSpec);
        MappingSourceFactory.register("yarn", YarnSource::fromSpec);
        MappingSourceFactory.register("csv", CSVSource::fromSpec);
    }
}
