package io.legacymoddingmc.mappinggenerator.download;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.MappingCollection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.io.File;
import java.util.function.Supplier;

public class MCPConnection implements MappingConnection {

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;
    @Getter
    private final File dir;

    public MCPConnection(Project project, String gameVersion, String mappingVersion) {
        this.gameVersion = gameVersion;
        this.mappingVersion = mappingVersion;
        String[] mappingVersionParts = mappingVersion.split("-");
        Preconditions.checkState(mappingVersionParts.length == 2);
        this.dir = Utilities.getRawCacheDir(project, "minecraft", "de", "oceanlabs", "mcp", "mcp_" + mappingVersionParts[0], mappingVersionParts[1]);
    }

    private boolean isUpToDate() {
        return new File(dir, "methods.csv").exists();
    }

    @Override
    public void addTo(MappingCollection mappings) {
        throw new RuntimeException("TODO");
    }
}
