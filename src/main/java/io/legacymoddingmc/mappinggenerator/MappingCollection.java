package io.legacymoddingmc.mappinggenerator;

import io.legacymoddingmc.mappinggenerator.source.IMappingSource;
import io.legacymoddingmc.mappinggenerator.source.MCPSource;
import io.legacymoddingmc.mappinggenerator.source.YarnSource;

import java.io.File;

public class MappingCollection {
    public void addVanillaJar(String gameVersion, File jar) {
    }

    public void loadMcp(String gameVersion, String mappingVersion) {
        //mappings.loadSrg("1.7.10", ((Copy)project.getTasks().getByName("generateForgeSrgMappings")).getDestinationDir());
        //mappings.loadMcp("1.7.10", Utilities.getRawCacheDir(project, "minecraft", "de", "oceanlabs", "mcp", "mcp_stable", "12"));
    }

    public void loadYarn(String gameVersion, String mappingVersion) {

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
}
