package io.legacymoddingmc.mappinggenerator.connection;

import com.gtnewhorizons.retrofuturagradle.shadow.de.undercouch.gradle.tasks.download.DownloadExtension;
import io.legacymoddingmc.mappinggenerator.GradleUtils;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;

import java.io.File;

public class CSVConnection {

    private final Project project;
    private final String url;

    private final File outFile;

    public CSVConnection(Project project, String url) {
        this.project = project;
        this.url = url;
        this.outFile = new File(GradleUtils.getCacheDir(project), "mappings/csv/" + urlToFileName(url));
    }

    private String urlToFileName(String url) {
        String newUrl = "";
        for(int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            char newC = c;
            if(!(Character.isLetterOrDigit(c)|| c == '.')) {
                newC = '_';
            }
            newUrl += newC;
        }
        return newUrl;
    }

    @SneakyThrows
    public File getFile() {
        val e = new DownloadExtension(project);
        e.run(action -> {
            try {
                action.src(url);
                action.dest(outFile);
                action.onlyIfModified(true);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });
        return outFile;
    }
}
