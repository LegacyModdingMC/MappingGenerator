package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.ArrayListMultimap;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Multimap;
import io.legacymoddingmc.mappinggenerator.name.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class Mapping {

    private static final Class<?>[] NAME_CLASSES = {Klass.class, Field.class, Method.class, Parameter.class};

    @Getter
    private final String src;
    @Getter
    private final String dest;

    private final Multimap<Klass, Klass> klass = ArrayListMultimap.create();
    private final Multimap<Field, List<Field>> field = ArrayListMultimap.create();
    private final Multimap<Method, List<Method>> method = ArrayListMultimap.create();
    private final Multimap<Parameter, Parameter> parameter = ArrayListMultimap.create();

    public <T> void put(T key, T value) {
        getMapForClass(key.getClass()).put(key, value);
    }

    public <T> Collection<T> getAll(T key) {
        return getMapForClass(key.getClass()).get(key);
    }

    public <T> T get(T key) {
        Collection<T> vals = getAll(key);
        if(vals.size() == 1) {
            return vals.iterator().next();
        } else {
            throw new IllegalStateException("Multiple values for key " + key + ", expected one.");
        }
    }

    private Multimap getMapForClass(Class<?> clazz) {
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
