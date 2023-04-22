package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.tuple.Pair;
import io.legacymoddingmc.mappinggenerator.download.MappingConnection;
import io.legacymoddingmc.mappinggenerator.download.SrgConnection;
import io.legacymoddingmc.mappinggenerator.name.Field;
import io.legacymoddingmc.mappinggenerator.name.Klass;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import io.legacymoddingmc.mappinggenerator.source.IMappingSource;
import io.legacymoddingmc.mappinggenerator.source.MCPSource;
import io.legacymoddingmc.mappinggenerator.source.YarnSource;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MappingCollection {

    private final Map<String, JarInfo> jarInfos = new HashMap<>();
    private final Map<Pair<String, String>, Mapping> mappings = new HashMap<>();

    public void addVanillaJar(String gameVersion, File jar) {
        jarInfos.computeIfAbsent(gameVersion, x -> new JarInfo(gameVersion)).load(jar);
    }

    public JarInfo getJarInfo(String version) {
        return jarInfos.get(version);
    }

    public File getDir(String gameVersion, String mappingVersion) {
        throw new RuntimeException("TODO");
    }

    public <T> T translate(T name, String version, String src, String dest) {
        throw new RuntimeException("TODO");
    }

    public Collection<Parameter> getParameters(String version, String language) {
        throw new RuntimeException("TODO");
    }

    public void put(Mapping... mappings) {
        for(Mapping m : mappings) {
            this.mappings.put(Pair.of(m.getSrc(), m.getDest()), m);
        }
    }

    public void load(MappingConnection connection) {
        connection.addTo(this);
    }

    public void load(Collection<MappingConnection> connections) {
        for(MappingConnection connection : connections) {
            load(connection);
        }
    }

    public <T> Collection<T> multiTranslate(T name, String version, String src, String srg) {
        throw new RuntimeException("TODO");
    }
}
