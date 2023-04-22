package io.legacymoddingmc.mappinggenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BytecodeUtils {

    private static final Pattern DESCRIPTOR = Pattern.compile("\\[*L[^;]+;|\\[*[ZBCSIFDJ]|[ZBCSIFDJ]");

    public static int countDescriptorParams(String descriptor) {
        return splitDescriptorParams(descriptor).size();
    }

    public static List<String> splitDescriptorParams(String desc) {
        desc = desc.substring(desc.indexOf('(') + 1, desc.lastIndexOf(')'));
        return JavaHelper.getAllMatches(DESCRIPTOR.matcher(desc));
    }

    public static int getTypeSize(String varType) {
        return "DJ".contains(varType.substring(0, 1)) ? 2 : 1;
    }

    public static List<Integer> getParameterIndexes(String desc, boolean isStatic) {
        int idx = isStatic ? 0 : 1;
        List<Integer> indexes = new ArrayList<>();
        for(String paramType : splitDescriptorParams(desc)) {
            indexes.add(idx);
            idx += getTypeSize(paramType);
        }
        return indexes;
    }
}
