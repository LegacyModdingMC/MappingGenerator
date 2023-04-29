package io.legacymoddingmc.mappinggenerator;

import org.gradle.api.provider.ListProperty;

import java.util.List;

public interface MappingGeneratorExtension {
    /** A list of mapping source specifications. */
    ListProperty<List<String>> getSources();
}
