package io.legacymoddingmc.mappinggenerator.download;

import io.legacymoddingmc.mappinggenerator.MappingCollection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;

import java.io.File;
import java.util.function.Supplier;

public class YarnConnection implements MappingConnection {

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;

    public YarnConnection(Project project, String gameVersion, String mappingVersion) {
        this.gameVersion = gameVersion;
        this.mappingVersion = mappingVersion;
        throw new RuntimeException("TODO");
    }

    @Override
    public void addTo(MappingCollection mappings) {
        throw new RuntimeException("TODO");
    }
}
