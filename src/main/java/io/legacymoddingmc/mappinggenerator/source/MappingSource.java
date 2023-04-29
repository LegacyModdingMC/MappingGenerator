package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import org.gradle.api.Project;

import java.util.Map;

public interface MappingSource {
    void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out);

    /** It's SHA-256. */
    String getInputHash(Project project);
}
