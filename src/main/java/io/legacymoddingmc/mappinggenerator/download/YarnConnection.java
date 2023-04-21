package io.legacymoddingmc.mappinggenerator.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class YarnConnection implements Supplier<File> {

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;

    @Override
    public File get() {
        return null;
    }
}
