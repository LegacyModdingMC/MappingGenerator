package io.legacymoddingmc.mappinggenerator;

import org.gradle.api.provider.ListProperty;

import java.util.List;

public interface MappingGeneratorExtension {
    ListProperty<List<String>> getSources();
}
