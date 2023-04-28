package io.legacymoddingmc.mappinggenerator.connection;

import io.legacymoddingmc.mappinggenerator.util.GradleUtils;
import lombok.SneakyThrows;
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
        GradleUtils.downloadFile(url, outFile, true, project);
        return outFile;
    }
}
