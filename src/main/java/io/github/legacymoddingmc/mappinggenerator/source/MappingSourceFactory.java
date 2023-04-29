package io.github.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MappingSourceFactory {
    private static Map<String, Function<List<String>, MappingSource>> map = new HashMap<>();

    public static void register(String namespace, Function<List<String>, MappingSource> klass) {
        map.put(namespace, klass);
    }

    public static MappingSource fromSpec(List<String> mappingSourceSpec) {
        Preconditions.checkArgument(map.containsKey(mappingSourceSpec.get(0)), "Unknown mapping source type: " + mappingSourceSpec.get(0));
        return map.get(mappingSourceSpec.get(0)).apply(mappingSourceSpec);
    }
}
