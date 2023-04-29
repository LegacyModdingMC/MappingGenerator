package io.legacymoddingmc.mappinggenerator.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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

    public static <T> Collection<T> getCollectionWithoutElement(Collection<T> collection, String excluded) {
        return collection.stream().filter(x -> !x.equals(excluded)).collect(Collectors.toList());
    }

    public static <T> List<T> sorted(Collection<T> collection) {
        return collection.stream().sorted().collect(Collectors.toList());
    }

    public static <T> List<T> flatten(List<List<T>> lists) {
        // normal syntax for flattening a list
        return lists.stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
