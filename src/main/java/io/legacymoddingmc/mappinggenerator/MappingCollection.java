package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.tuple.Pair;
import io.legacymoddingmc.mappinggenerator.connection.MappingConnection;
import lombok.val;

import java.io.File;
import java.util.*;

public class MappingCollection {

    private final Map<String, JarInfo> jarInfos = new HashMap<>();
    private final Map<String, Map<Pair<String, String>, Mapping>> mappings = new HashMap<>();

    public void addVanillaJar(String gameVersion, File jar) {
        jarInfos.computeIfAbsent(gameVersion, x -> new JarInfo(gameVersion)).load(jar);
    }

    public JarInfo getJarInfo(String version) {
        return jarInfos.get(version);
    }

    private Map<Pair<String, String>, Mapping> getVersionMap(String version) {
        return mappings.computeIfAbsent(version, x -> new HashMap<>());
    }

    private Mapping getMapping(String version, String src, String dest) {
        val dir = Pair.of(src, dest);
        return getVersionMap(version).computeIfAbsent(dir, x -> new Mapping(src, dest));
    }

    public <T> T translate(T name, String version, String src, String dest) {
        return translate(name, version, src, dest, false);
    }

    public <T> T translate(T name, String version, String src, String dest, boolean forceDifferent) {
        Mapping mapping = getMapping(version, src, dest);
        T newName = mapping.get(name);
        if(forceDifferent && Objects.equals(name, newName)) {
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

    public <T> Collection<T> getNames(String version, String language, Class<T> classOfT) {
        Collection<T> best = Collections.emptyList();
        for(val e : getVersionMap(version).entrySet()) {
            String src = e.getKey().getLeft();
            String dest = e.getKey().getRight();
            Mapping mapping = e.getValue();
            if(src.equals(language)) {
                val candidate = mapping.getMapForClass(classOfT).keySet();
                if(best.size() < candidate.size()) {
                    best = candidate;
                }
            }
        }
        return best;
    }

    public void put(String version, Mapping... mappings) {
        for(Mapping m : mappings) {
            getVersionMap(version).put(Pair.of(m.getSrc(), m.getDest()), m);
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
