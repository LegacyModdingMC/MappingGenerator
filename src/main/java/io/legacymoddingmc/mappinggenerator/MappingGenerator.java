package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.mcp.PatchSourcesTask;
import com.gtnewhorizons.retrofuturagradle.mcp.RemapSourceJarTask;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.connection.MCPConnection;
import io.legacymoddingmc.mappinggenerator.connection.SrgConnection;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import io.legacymoddingmc.mappinggenerator.source.MappingSource;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MappingGenerator {

    private final Project project;
    private final List<MappingSource> sources = new ArrayList<>();

    public MappingGenerator(Project project) {
        this.project = project;
    }

    public void addSource(MappingSource source) {
        sources.add(source);
    }

    public void generateExtraParameters(File out) {
        MappingCollection mappings = new MappingCollection();

        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "client.jar"));
        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "server.jar"));

        PatchSourcesTask taskPatchDecompiledJar = (PatchSourcesTask)project.getTasks().getByName("patchDecompiledJar");
        File patchedJar = taskPatchDecompiledJar.getOutputJar().get().getAsFile();

        mappings.addDecompiledSource(project, "1.7.10", patchedJar);

        MCPConnection mcpConn = new MCPConnection(project, "1.7.10", "stable_12");

        mappings.load(new SrgConnection(project, "1.7.10"));
        mappings.load(mcpConn);

        Map<String, String> extraParameters = new HashMap<>();

        for(MappingSource source : sources) {
            source.generateExtraParameters(project, mappings, extraParameters);
        }

        Set<String> defaultParameterNames = getDefaultParameterNames(mcpConn);
        Set<String> allSrgParameterNames = getAllSrgParameterNames(mappings);

        // Remove useless entries
        extraParameters.entrySet().removeIf(e -> defaultParameterNames.contains(e.getKey()) || !allSrgParameterNames.contains(e.getKey()));

        int totalParameters = getTotalParameters(mappings);
        int named = (defaultParameterNames.size() + extraParameters.size());
        System.out.println("Parameter coverage: " + defaultParameterNames.size() + " -> " + named + " / " + totalParameters + " (" + ((named / (double)totalParameters) * 100.0) + "%)");

        writeMappings(extraParameters, out);
    }

    private Set<String> getAllSrgParameterNames(MappingCollection mappings) {
        return mappings
                .getNames("1.7.10", "srg", Parameter.class)
                .stream()
                .map(Parameter::getParameter)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private int getTotalParameters(MappingCollection mappings) {
        int total = 0;
        for(Method m : mappings.getNames("1.7.10", "srgId", Method.class)) {
            total += BytecodeUtils.countDescriptorParams(mappings.multiTranslate(m, "1.7.10", "srgId", "notch").iterator().next().getDesc());
        }
        return total;
    }

    private Set<String> getDefaultParameterNames(MCPConnection mcpConn) {
        return MCPConnection
                .readCSV(new File(mcpConn.getDir(), "params.csv"))
                .stream()
                .map(a -> a[0])
                .collect(Collectors.toSet());
    }

    private void writeMappings(Map<String, String> parameters, File out) {
        out.getParentFile().mkdirs();
        try(FileWriter fw = new FileWriter(out)) {
            fw.write("param,name,side\n");
            for(Map.Entry<String, String> mapping : parameters.entrySet()) {
                fw.write(mapping.getKey() + "," + mapping.getValue() + ",-1\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
