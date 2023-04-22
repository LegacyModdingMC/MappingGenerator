package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;

public class GradleUtils {
    public static File getCacheDir(Project project) {
        return FileUtils.getFile(project.getGradle().getGradleUserHomeDir(), "caches", "minecraft_mapping_generator");
    }
}
