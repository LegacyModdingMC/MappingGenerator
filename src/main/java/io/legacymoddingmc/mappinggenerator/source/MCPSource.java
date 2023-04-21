package io.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.opencsv.CSVReader;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.MappingCollection;
import io.legacymoddingmc.mappinggenerator.name.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MCPSource implements IMappingSource {

    public enum Type {
        PARAMETERS,
        METHOD_COMMENTS
    }

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;
    @Getter
    private final Type type;

    @Override
    public void generateExtraParameters(MappingCollection mappings, Map<String, String> out) {
        if(type == Type.PARAMETERS) {
            generateExtraParametersFromParameters(mappings, out);
        } else if(type == Type.METHOD_COMMENTS) {
            generateExtraParametersFromMethodComments(mappings, out);
        }
    }

    private void generateExtraParametersFromParameters(MappingCollection mappings, Map<String, String> out) {
        try(CSVReader reader = Utilities.createCsvReader(new File(mappings.getDir(gameVersion, mappingVersion), "parameters.csv"))) {
            for(String[] line : reader) {
                out.put(line[0], line[1]);
            }
        }
    }

    private void generateExtraParametersFromMethodComments(MappingCollection mappings, Map<String, String> out) {
        try(CSVReader reader = Utilities.createCsvReader(new File(mappings.getDir(gameVersion, mappingVersion), "methods.csv"))) {
            Method methodId = new Method(null, null, null);
            for(String[] line : reader) {
                String desc = line[3];
                if(desc.contains("Args:")) {
                    String argDesc = desc.substring(desc.indexOf("Args:"));
                    methodId.setMethod(line[0].split("_")[1]);
                    Method notch = mappings.translate("1.7.10", methodId, "methodId", "notch");
                    String descriptor = notch.getDesc();
                    int numParams = BytecodeUtils.countDescriptorParams(descriptor);
                    List<String> splitDesc = splitDescription(argDesc.substring(5));
                    List<Integer> parameterIndexes = mappings.getJarInfo("1.7.10").getMethodInfo(notch).getParameterIndexes();
                    for(int i = 0; i < splitDesc.size(); i++) {
                        String srgParam = "p_" + methodId.getMethod() + "_" + parameterIndexes.get(i) + "_";
                        out.put(srgParam, splitDesc.get(i));
                    }
                }
            }
        }
    }

    private static final Pattern notText = Pattern.compile("[^a-zA-Z1-9 ,]");
    private static final Pattern newSentence = Pattern.compile("([a-z] [A-Z])");

    static List<String> splitDescription(String desc) {
        // Remove stuff between parantheses
        desc = desc.replaceAll("\\(.*?\\)", "");

        // Treat dashes like spaces
        desc = desc.replaceAll("-", " ");

        Matcher notTextMatcher = notText.matcher(desc);
        int notTextBoundary = notTextMatcher.find() ? notTextMatcher.start() : desc.length();

        desc = desc.substring(0, notTextBoundary)
/*
    // End at one of the following:
    // - a character that is not a space or a letter
    // - a space followed by a capitalized letter
    int end = -1;
    for (int i = 0; i < desc.size() - 1; i++) {
        char left = desc.charAt(i)
        char right = desc.charAt(i + 1)

        if ((!Character.isLetter(right) && right != ' ') || (left == ' ' && !Character.isLowerCase(right))) {
            end = i + 1
            break
        }
    }
    if (end != -1) {
        desc = desc.substring(0, end)
    }*/

        //int i = 0;

        String[] descParts = desc.split(",");

        return IntStream.range(0, descParts.length).mapToObj(i -> {
            String arg = descParts[i].trim();

            if (arg.isEmpty()) {
                return "";
            }

            // Uncapitalize first letter
            if (Character.isUpperCase(arg.charAt(0))) {
                arg = "" + Character.toLowerCase(arg.charAt(0)) + arg.substring(1);
            }

            if(i == descParts.length - 1) {
                Matcher newSentenceMatcher = newSentence.matcher(arg);
                int newSentenceBoundary = newSentenceMatcher.find() ? newSentenceMatcher.start() + 1 : arg.length();

                arg = arg.substring(0, newSentenceBoundary);
            }

            // End at one of the following:
            // - a character that is not a space or a letter
            // - a space followed by a capitalized letter
        /*int end = -1;
        for (int i = 0; i < desc.size() - 1; i++) {
            char left = desc.charAt(i)
            char right = desc.charAt(i + 1)

            if ((!Character.isLetter(right) && right != ' ') || (left == ' ' && !Character.isLowerCase(right))) {
                end = i + 1
                break
            }
        }
        if (end != -1) {
            desc = desc.substring(0, end)
        }*/

            // Remove first word if it's a type or "and"
            String[] words = arg.split(" ");
            if (words.length > 1 && (words[0] == "string" || words[0] == "int" || words[0] == "byte" || words[0] == "char" || words[0] == "boolean" || words[0] == "long" || words[0] == "and")) {
                words = Arrays.copyOfRange(words, 1, words.length);
            }
            String a = "pepe";
            // Camelize
            String camel = String.join("", Arrays.stream(words).map(StringUtils::capitalize).toArray(String[]::new));
            if(!camel.startsWith("NBT")) { // exception :>
                camel = "" + Character.toLowerCase(camel.charAt(0)) + camel.substring(1)
            }
            return camel;
        }).filter(x -> !x.isEmpty()).collect(Collectors.toList());
    }
}
