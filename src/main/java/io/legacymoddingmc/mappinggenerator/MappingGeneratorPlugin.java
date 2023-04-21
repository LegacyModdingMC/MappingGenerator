/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.IMinecraftyExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;

import java.io.File;

/**
 * A simple 'hello world' plugin.
 */
public class MappingGeneratorPlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Register a task
        project.getTasks().register("greeting", task -> {
            task.doLast(s -> System.out.println("Hello from plugin 'io.legacymoddingmc.mappinggenerator.greeting'"));
        });

        IMinecraftyExtension minecraft = (IMinecraftyExtension)project.getExtensions().getByName("minecraft");
        System.out.println("Setting the thing !");
        minecraft.getExtraParamsCsvs().from(new File(project.getBuildDir(), "extra-mappings/test.csv"));
    }
}
