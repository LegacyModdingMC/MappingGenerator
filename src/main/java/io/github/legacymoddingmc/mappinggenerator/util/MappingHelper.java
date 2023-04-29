package io.github.legacymoddingmc.mappinggenerator.util;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Multimap;
import io.github.legacymoddingmc.mappinggenerator.Mapping;
import io.github.legacymoddingmc.mappinggenerator.name.Klass;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class MappingHelper {

    private static final Map<Mapping, Map<String, String>> descRemapCache = new WeakHashMap<>();

    public static String remapDesc(String desc, Mapping mapping) {
        String originalDesc = desc;

        String cached = descRemapCache.computeIfAbsent(mapping, x -> new HashMap<>()).get(desc);
        if(cached != null) {
            return cached;
        }
        
        for(Map.Entry<Klass, Klass> e : (((Multimap<Klass, Klass>)mapping.getMapForClass(Klass.class))).entries()) {
            String k = e.getKey().getKlass();
            String v = e.getValue().getKlass();
            String sourceRef = "L" + k + ";";
            String destRef = "L" + v + ";";
            desc = desc.replace(sourceRef, destRef);
        }
        
        descRemapCache.get(mapping).put(originalDesc, desc);
        
        return desc;
    }
}
