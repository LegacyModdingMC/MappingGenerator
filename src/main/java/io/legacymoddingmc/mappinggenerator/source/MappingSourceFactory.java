package io.legacymoddingmc.mappinggenerator.source;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MappingSourceFactory {
    private static Map<String, Function<String[], MappingSource>> map = new HashMap<>();

    public static void register(String namespace, Function<String[], MappingSource> klass) {
        map.put(namespace, klass);
    }

    public static MappingSource fromSpec(String[] mappingSourceSpec) {
        return map.get(mappingSourceSpec[0]).apply(mappingSourceSpec);
    }
}
