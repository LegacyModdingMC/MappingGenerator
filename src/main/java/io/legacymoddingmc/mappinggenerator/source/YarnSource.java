package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}
