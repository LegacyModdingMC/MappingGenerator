package io.legacymoddingmc.mappinggenerator;

import org.gradle.api.provider.ListProperty;

public interface MappingGeneratorExtension {
    ListProperty<String[]> getSources();
}
