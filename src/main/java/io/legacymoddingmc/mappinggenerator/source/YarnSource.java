package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.connection.YarnConnection;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.util.Map;

@RequiredArgsConstructor
public class YarnSource implements IMappingSource {

    @Getter
    private final String mappingVersion;

    @Override
    public void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out) {
        YarnConnection yarnConn = new YarnConnection(project, mappingVersion);
        mappings.load(yarnConn);

        for(Parameter notch : mappings.getNames("1.7.10", "notch", Parameter.class)) {
            Parameter srg = mappings.translate(notch, "1.7.10", "notch", "srg");
            Parameter mcp = mappings.translate(srg, "1.7.10", "srg", "mcp", true);

            Parameter intermediary = mappings.translate(notch, "1.7.10", "notch", "intermediary");
            Parameter yarn = mappings.translate(intermediary, "1.7.10", "intermediary", "yarn", true);

            if(mcp == null && yarn != null) {
                out.put(srg.getParameter(), yarn.getParameter());
            }
        }
    }
}
