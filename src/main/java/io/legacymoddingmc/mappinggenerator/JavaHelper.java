package io.legacymoddingmc.mappinggenerator;

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
}
