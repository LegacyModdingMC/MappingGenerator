package io.legacymoddingmc.mappinggenerator.connection;

import io.legacymoddingmc.mappinggenerator.MappingCollection;

/** A class responsible for loading a mapping, possibly accessing the network and the file system in the process. */
public interface MappingConnection {

    void addTo(MappingCollection mappings);

}
