package io.github.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.HashMultimap;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Multimap;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.SetMultimap;
import io.github.legacymoddingmc.mappinggenerator.name.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class Mapping {

    private static final Class<?>[] NAME_CLASSES = {Klass.class, Field.class, Method.class, Parameter.class};

    @Getter
    private final String src;
    @Getter
    private final String dest;

    private final SetMultimap<Klass, Klass> klass = HashMultimap.create();
    private final SetMultimap<Field, Field> field = HashMultimap.create();
    private final SetMultimap<Method, Method> method = HashMultimap.create();
    private final SetMultimap<Parameter, Parameter> parameter = HashMultimap.create();

    public <T> void put(T key, T value) {
        getMapForClass(key.getClass()).put(key, value);
    }

    public <T> Collection<T> getAll(T key) {
        if(key == null) {
            return Collections.emptyList();
        } else {
            return getMapForClass(key.getClass()).get(key);
        }
    }

    public <T> T get(T key) {
        Collection<T> vals = getAll(key);
        if(vals.size() == 1) {
            return vals.iterator().next();
        } else if(vals.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Multiple values for key " + key + ", expected one.");
        }
    }

    public Multimap getMapForClass(Class<?> clazz) {
        if(clazz == Klass.class) return klass;
        else if(clazz == Field.class) return field;
        else if(clazz == Method.class) return method;
        else if(clazz == Parameter.class) return parameter;
        else throw new IllegalStateException();
    }

    public Mapping inverse() {
        Mapping inverted = new Mapping(dest, src);
        for(Class<?> clazz : NAME_CLASSES) {
            Multimap<Name, Name> map = getMapForClass(clazz);
            for(Map.Entry<Name, Name> e : map.entries()) {
                inverted.put(e.getValue(), e.getKey());
            }
        }
        return inverted;
    }

}
