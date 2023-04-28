package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.opencsv.CSVReader;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class IOHelper {

    @SneakyThrows
    public static List<String[]> readCSV(File file) {
        try(CSVReader reader = Utilities.createCsvReader(file)) {
            return reader.readAll();
        }
    }

}
