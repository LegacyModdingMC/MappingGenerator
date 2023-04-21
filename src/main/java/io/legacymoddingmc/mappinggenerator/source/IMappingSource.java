package io.legacymoddingmc.mappinggenerator.source;

import io.legacymoddingmc.mappinggenerator.MappingCollection;

import java.util.Map;

public interface IMappingSource {
    void generateExtraParameters(MappingCollection mappings, Map<String, String> out);
}
