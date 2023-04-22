package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.download.MappingConnection;
import org.gradle.api.Project;

import java.util.Collection;
import java.util.Map;

public interface IMappingSource {
    void generateExtraParameters(MappingCollection mappings, Map<String, String> out);
    Collection<MappingConnection> getNecessaryMappingConnections(Project project);
}
