package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.download.MappingConnection;
import io.legacymoddingmc.mappinggenerator.download.YarnConnection;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class YarnSource implements IMappingSource {

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;

    @Override
    public void generateExtraParameters(MappingCollection mappings, Map<String, String> out) {
        for(Parameter notch : mappings.getParameters("1.7.10", "notch")) {
            Parameter srg = mappings.translate(notch, "1.7.10", "notch", "srg");
            Parameter mcp = mappings.translate(srg, "1.7.10", "srg", "mcp");

            Parameter intermediary = mappings.translate(notch, "1.7.10", "notch", "intermediary");
            Parameter yarn = mappings.translate(srg, "1.7.10", "intermediary", "yarn");

            if(mcp == null && yarn != null) {
                out.put(srg.getParameter(), yarn.getParameter());
            }
        }
    }

    @Override
    public Collection<MappingConnection> getNecessaryMappingConnections(Project project) {
        return Arrays.asList(new YarnConnection(project, gameVersion, mappingVersion));
    }
}
