package io.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import io.legacymoddingmc.mappinggenerator.util.IOHelper;
import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.connection.CSVConnection;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.util.Map;

@RequiredArgsConstructor
public class CSVSource implements MappingSource {

    private final String url;

    public static CSVSource fromSpec(String[] spec) {
        Preconditions.checkArgument(spec.length == 2);
        return new CSVSource(spec[1]);
    }

    @Override
    public void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out) {
        CSVConnection conn = new CSVConnection(project, url);
        for(String[] line : IOHelper.readCSV(conn.getFile())) {
            out.put(line[0], line[1]);
        }
    }
}
