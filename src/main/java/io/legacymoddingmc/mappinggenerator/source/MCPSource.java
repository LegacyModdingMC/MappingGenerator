package io.legacymoddingmc.mappinggenerator.source;

public class MCPSource implements IMappingSource {

    public static enum Type {
        PARAMETERS,
        METHOD_COMMENTS
    }

    private final String version;
    private final Type type;

    public MCPSource(String version, Type type) {
        this.version = version;
        this.type = type;
    }
}
