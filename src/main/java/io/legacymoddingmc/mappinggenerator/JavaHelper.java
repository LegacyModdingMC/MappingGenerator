package io.legacymoddingmc.mappinggenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class JavaHelper {
    public static String replaceLastSlashWithSpace(String s) {
        int lastSlash = s.lastIndexOf('/');
        if(lastSlash != -1) {
            s = s.substring(0, lastSlash) + " " + s.substring(lastSlash + 1);
        }
        return s;
    }

    public static <T> T getLast(T[] arr) {
        return arr[arr.length - 1];
    }

    public static List<String> getAllMatches(Matcher matcher) {
        List<String> matches = new ArrayList<>();
        while(matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }
}
