package io.legacymoddingmc.mappinggenerator;

import java.io.File;

public class Test {

    public static void main(String[] args) {
        SourceInfo si = new SourceInfo();
        si.load(new File(args[0]));
    }

}
