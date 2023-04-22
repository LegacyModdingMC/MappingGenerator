package io.legacymoddingmc.mappinggenerator;

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

    public void addVanillaJar(String gameVersion, File jar) {
        jarInfos.computeIfAbsent(gameVersion, x -> new JarInfo(gameVersion)).load(jar);
    }

    public void loadMcp(String gameVersion, String mappingVersion) {
        throw new RuntimeException("TODO");
        //mappings.loadSrg("1.7.10", ((Copy)project.getTasks().getByName("generateForgeSrgMappings")).getDestinationDir());
        //mappings.loadMcp("1.7.10", Utilities.getRawCacheDir(project, "minecraft", "de", "oceanlabs", "mcp", "mcp_stable", "12"));
    }

    public void loadYarn(String gameVersion, String mappingVersion) {
        throw new RuntimeException("TODO");
    }

    public void loadNecessaryMappingsFor(IMappingSource source) {
        if(source instanceof YarnSource) {
            YarnSource yarn = (YarnSource) source;
            loadYarn(yarn.getGameVersion(), yarn.getMappingVersion());
        } else if(source instanceof MCPSource) {
            MCPSource mcp = (MCPSource) source;
            loadMcp(mcp.getGameVersion(), mcp.getMappingVersion());
        }
    }

    public JarInfo getJarInfo(String version) {
        return jarInfos.get(version);
    }

    public File getDir(String gameVersion, String mappingVersion) {
        throw new RuntimeException("TODO");
    }

    public Klass translate(Klass name, String version, String src, String dest) {
        throw new RuntimeException("TODO");
    }

    public Field translate(Field name, String version, String src, String dest) {
        throw new RuntimeException("TODO");
    }

    public Method translate(Method name, String version, String src, String dest) {
        throw new RuntimeException("TODO");
    }

    public Parameter translate(Parameter name, String version, String src, String dest) {
        throw new RuntimeException("TODO");
    }

    public Collection<Parameter> getParameters(String version, String language) {
        throw new RuntimeException("TODO");
    }
}
