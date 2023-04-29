package io.github.legacymoddingmc.mappinggenerator.util;

import com.gtnewhorizons.retrofuturagradle.shadow.com.opencsv.CSVReader;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.codec.digest.DigestUtils;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IOHelper {

    @SneakyThrows
    public static List<String[]> readCSV(File file) {
        try(CSVReader reader = Utilities.createCsvReader(file)) {
            return reader.readAll();
        }
    }

    @SneakyThrows
    public static String sha256(File file) {
        try(FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(fis);
        }
    }

    @SneakyThrows
    public static String sha256(Collection<File> file) {
        return DigestUtils.sha256Hex(file.stream().map(f -> sha256(f)).collect(Collectors.joining()));
    }

    @SneakyThrows
    public static List<File> listRecursively(File dir) {
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            return stream.filter(Files::isRegularFile).map(f -> f.toFile()).collect(Collectors.toList());
        }
    }
}
