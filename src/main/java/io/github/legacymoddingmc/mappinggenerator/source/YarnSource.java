package io.github.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import io.github.legacymoddingmc.mappinggenerator.MappingCollection;
import io.github.legacymoddingmc.mappinggenerator.connection.YarnConnection;
import io.github.legacymoddingmc.mappinggenerator.name.Parameter;
import io.github.legacymoddingmc.mappinggenerator.util.IOHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class YarnSource implements MappingSource {

    @Getter
    private final String mappingVersion;

    public static YarnSource fromSpec(List<String> spec) {
        Preconditions.checkArgument(spec.size() == 2);
        return new YarnSource(spec.get(1));
    }

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

    @Override
    public String getInputHash(Project project) {
        YarnConnection yarnConn = new YarnConnection(project, mappingVersion);
        return IOHelper.sha256(IOHelper.listRecursively(yarnConn.getDir()));
    }
}
