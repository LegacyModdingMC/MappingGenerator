package io.legacymoddingmc.mappinggenerator.util;

import com.gtnewhorizons.retrofuturagradle.shadow.de.undercouch.gradle.tasks.download.DownloadExtension;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import lombok.val;
import org.gradle.api.Project;

import java.io.File;

public class GradleUtils {
    public static File getCacheDir(Project project) {
        return FileUtils.getFile(project.getGradle().getGradleUserHomeDir(), "caches", "minecraft-mapping-generator");
    }

    public static void downloadFile(String url, File file, boolean onlyIfModified, boolean quiet, Project project) {
        val e = new DownloadExtension(project);
        e.run(action -> {
            try {
                action.src(url);
                action.dest(file);
                if(onlyIfModified) {
                    action.onlyIfModified(true);
                }
                if(quiet) {
                    action.quiet(true);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void downloadFile(String url, File file, boolean onlyIfModified, Project project) {
        downloadFile(url, file, onlyIfModified, false, project);
    }

    public static void downloadFile(String url, File file, Project project) {
        downloadFile(url, file, false, false, project);
    }
}
