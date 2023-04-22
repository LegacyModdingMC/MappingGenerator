package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.tuple.Pair;
import io.legacymoddingmc.mappinggenerator.download.MappingConnection;
import io.legacymoddingmc.mappinggenerator.name.*;
import lombok.val;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MappingCollection {

    private final Map<String, JarInfo> jarInfos = new HashMap<>();
    private final Map<String, Map<Pair<String, String>, Mapping>> mappings = new HashMap<>();

    public void addVanillaJar(String gameVersion, File jar) {
        jarInfos.computeIfAbsent(gameVersion, x -> new JarInfo(gameVersion)).load(jar);
    }

    public JarInfo getJarInfo(String version) {
        return jarInfos.get(version);
    }

    private Mapping getMapping(String version, String src, String dest) {
        val dir = Pair.of(src, dest);
        return mappings
                .computeIfAbsent(version, x -> new HashMap<>())
                .computeIfAbsent(dir, x -> new Mapping(src, dest));
    }

    public <T> T translate(T name, String version, String src, String dest) {
        return translate(name, version, src, dest, false);
    }

    public <T> T translate(T name, String version, String src, String dest, boolean forceDifferent) {
        Mapping mapping = getMapping(version, src, dest);
        T newName = mapping.get(name);
        if(forceDifferent && name.equals(newName)) {
            return null;
        } else {
            return newName;
        }
    }

    public <T> Collection<T> multiTranslate(T name, String version, String src, String dest) {
        return multiTranslate(name, version, src, dest, false);
    }

    public <T> Collection<T> multiTranslate(T name, String version, String src, String dest, boolean forceDifferent) {
        Mapping mapping = getMapping(version, src, dest);
        Collection<T> newNames = mapping.getAll(name);
        if(forceDifferent && newNames.size() == 1 && name.equals(newNames.iterator().next())) {
            return null;
        } else {
            return newNames;
        }
    }

    public Collection<Parameter> getParameters(String version, String language) {
        throw new RuntimeException("TODO");
    }

    public void put(String version, Mapping... mappings) {
        for(Mapping m : mappings) {
            this.mappings
                    .computeIfAbsent(version, x -> new HashMap<>())
                    .put(Pair.of(m.getSrc(), m.getDest()), m);
        }
    }

    public void load(MappingConnection connection) {
        if(!isLoaded(connection)) {
            connection.addTo(this);
        }
    }

    private boolean isLoaded(MappingConnection connection) {
        return false;
        // TODO
    }

    public void load(Collection<MappingConnection> connections) {
        for(MappingConnection connection : connections) {
            load(connection);
        }
    }
}
