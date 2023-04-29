package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import org.gradle.api.Project;

import java.util.Map;

/** Declares a source from which mappings are obtained. */
public interface MappingSource {
    void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out);

    /** Gets an SHA-256 hash computed from the inputs of the source. */
    String getInputHash(Project project);
}
