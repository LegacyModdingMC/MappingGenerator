package io.github.legacymoddingmc.mappinggenerator.source;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;
import com.gtnewhorizons.retrofuturagradle.shadow.com.opencsv.CSVReader;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.github.legacymoddingmc.mappinggenerator.name.Method;
import io.github.legacymoddingmc.mappinggenerator.util.BytecodeUtils;
import io.github.legacymoddingmc.mappinggenerator.util.JavaHelper;
import io.github.legacymoddingmc.mappinggenerator.MappingCollection;
import io.github.legacymoddingmc.mappinggenerator.connection.MCPConnection;
import io.github.legacymoddingmc.mappinggenerator.connection.SrgConnection;
import io.github.legacymoddingmc.mappinggenerator.util.IOHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MCPSource implements MappingSource {

    public enum Type {
        parameters,
        methodComments
    }

    @Getter
    private final String gameVersion;
    @Getter
    private final String mappingVersion;
    @Getter
    private final Type type;

    public static MCPSource fromSpec(List<String> spec) {
        Preconditions.checkArgument(spec.size() == 4);
        return new MCPSource(spec.get(1), spec.get(2), Type.valueOf(spec.get(3)));
    }

    @Override
    public void generateExtraParameters(Project project, MappingCollection mappings, Map<String, String> out) {
        SrgConnection srgConn = new SrgConnection(project, gameVersion);
        MCPConnection mcpConn = new MCPConnection(project, gameVersion, mappingVersion);

        if(type == Type.parameters) {
            generateExtraParametersFromParameters(mappings, mcpConn, out);
        } else if(type == Type.methodComments) {
            mappings.load(srgConn);
            generateExtraParametersFromMethodComments(mappings, mcpConn, out);
        }
    }

    @Override
    public String getInputHash(Project project) {
        boolean doINeedSrg = false;
        boolean doINeedMCP = false;
        switch(type) {
            case parameters:
                doINeedMCP = true;
                break;
            case methodComments:
                doINeedSrg = doINeedMCP = true;
                break;
        }
        List<File> dirsINeed = new ArrayList<>();
        if(doINeedSrg) {
            dirsINeed.add(new SrgConnection(project, gameVersion).getDir());
        }
        if(doINeedMCP) {
            dirsINeed.add(new MCPConnection(project, gameVersion, mappingVersion).getDir());
        }

        return IOHelper.sha256(JavaHelper.flatten(dirsINeed.stream().map(x -> IOHelper.listRecursively(x)).collect(Collectors.toList())));
    }

    @SneakyThrows
    private void generateExtraParametersFromParameters(MappingCollection mappings, MCPConnection mcpConn, Map<String, String> out) {
        try(CSVReader reader = Utilities.createCsvReader(new File(mcpConn.getDir(), "params.csv"))) {
            for(String[] line : reader) {
                out.put(line[0], line[1]);
            }
        }
    }

    @SneakyThrows
    private void generateExtraParametersFromMethodComments(MappingCollection mappings, MCPConnection mcpConn, Map<String, String> out) {
        try(CSVReader reader = Utilities.createCsvReader(new File(mcpConn.getDir(), "methods.csv"))) {
            Method methodId = new Method(null, null, null);
            for(String[] line : reader) {
                String desc = line[3];
                if(desc.contains("Args:")) {
                    String argDesc = desc.substring(desc.indexOf("Args:"));
                    methodId.setMethod(line[0].split("_")[1]);
                    Method notch = mappings.multiTranslate(methodId, "1.7.10", "srgId", "notch").iterator().next();
                    String descriptor = notch.getDesc();
                    int numParams = BytecodeUtils.countDescriptorParams(descriptor);
                    List<String> splitDesc = splitDescription(argDesc.substring(5));
                    List<Integer> parameterIndexes = BytecodeUtils.getParameterIndexes(descriptor, mappings.getJarInfo("1.7.10").getMethodInfo(notch).isStatic());
                    if(numParams == splitDesc.size()) {
                        for (int i = 0; i < splitDesc.size(); i++) {
                            String srgParam = "p_" + methodId.getMethod() + "_" + parameterIndexes.get(i) + "_";
                            out.put(srgParam, splitDesc.get(i));
                        }
                    } else {
                        // ignore mismatching comments (none of them are interesting anyway, I checked)
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

        desc = desc.substring(0, notTextBoundary);

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

            // Remove first word if it's a type or "and"
            String[] words = arg.split(" ");
            if (words.length > 1 && (words[0] == "string" || words[0] == "int" || words[0] == "byte" || words[0] == "char" || words[0] == "boolean" || words[0] == "long" || words[0] == "and")) {
                words = Arrays.copyOfRange(words, 1, words.length);
            }

            // Camelize
            String camel = String.join("", Arrays.stream(words).map(StringUtils::capitalize).toArray(String[]::new));
            if(!camel.startsWith("NBT")) { // exception :>
                camel = "" + Character.toLowerCase(camel.charAt(0)) + camel.substring(1);
            }
            return camel;
        }).filter(x -> !x.isEmpty()).collect(Collectors.toList());
    }
}
