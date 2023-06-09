package io.github.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import io.github.legacymoddingmc.mappinggenerator.MappingCollection;
import io.github.legacymoddingmc.mappinggenerator.connection.CSVConnection;
import io.github.legacymoddingmc.mappinggenerator.util.IOHelper;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CSVSource implements MappingSource {

    private final String url;

    public static CSVSource fromSpec(List<String> spec) {
        Preconditions.checkArgument(spec.size() == 2);
        return new CSVSource(spec.get(1));
    }

    @Override
    public void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out) {
        CSVConnection conn = new CSVConnection(project, url);
        for(String[] line : IOHelper.readCSV(conn.getFile())) {
            out.put(line[0], line[1]);
        }
    }

    @Override
    public String getInputHash(Project project) {
        CSVConnection conn = new CSVConnection(project, url);
        return IOHelper.sha256(conn.getFile());
    }
}
