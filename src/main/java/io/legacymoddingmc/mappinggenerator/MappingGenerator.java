package io.legacymoddingmc.mappinggenerator;

import static io.legacymoddingmc.mappinggenerator.connection.SrgConnection.toSrgId;

import com.gtnewhorizons.retrofuturagradle.mcp.DeobfuscateTask;
import com.gtnewhorizons.retrofuturagradle.mcp.PatchSourcesTask;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.connection.MCPConnection;
import io.legacymoddingmc.mappinggenerator.connection.SrgConnection;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.name.Parameter;
import io.legacymoddingmc.mappinggenerator.source.MappingSource;
import lombok.val;
import lombok.var;
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

        mappings.addSrgForgeSource(project, "1.7.10", patchedJar);

        MCPConnection mcpConn = new MCPConnection(project, "1.7.10", "stable_12");

        mappings.load(new SrgConnection(project, "1.7.10"));
        mappings.load(mcpConn);

        final Map<String, String> extraParameters = new HashMap<>();

        for(MappingSource source : sources) {
            source.generateExtraParameters(project, mappings, extraParameters);
        }

        Map<String, String> defaultParameterNameMap = getDefaultParameterNameMap(mcpConn);
        Set<String> allSrgParameterNames = getAllSrgParameterNames(mappings);

        // Remove useless entries
        extraParameters.entrySet().removeIf(e -> defaultParameterNameMap.keySet().contains(e.getKey()) || !allSrgParameterNames.contains(e.getKey()));

        Map<String, Set<String>> srgIdToParameterNames = new HashMap<>();
        for(val name : allSrgParameterNames) {
            srgIdToParameterNames.computeIfAbsent(toSrgId(name), x -> new HashSet<>()).add(name);
        }

        Map<String, Set<String>> extraParamsBySrgId = new HashMap<>();
        for(val k : extraParameters.keySet()) {
            extraParamsBySrgId.computeIfAbsent(toSrgId(k), x -> new HashSet<>()).add(k);
        }

        // Rename parameters to avoid name collisions
        for(val e : extraParamsBySrgId.entrySet()) {
            String srgId = e.getKey();
            val params = JavaHelper.sorted(e.getValue());
            val localVars = mappings.getForgeLocalVariables("1.7.10", srgId);
            val allParams = srgIdToParameterNames.get(srgId);
            for(val param : params) {
                var name = extraParameters.get(param);
                val otherParams = JavaHelper.getCollectionWithoutElement(allParams, param);
                val otherNames = otherParams.stream().map(x -> defaultParameterNameMap.getOrDefault(x, extraParameters.get(x))).collect(Collectors.toList());
                if(localVars.contains(name) || otherNames.contains(name)) {
                    name += "_";
                    extraParameters.put(param, name);
                }
                if(localVars.contains(name) || otherNames.contains(name)) {
                    name += param.split("_")[2];
                    extraParameters.put(param, name);
                }
                if(localVars.contains(name) || otherNames.contains(name)) {
                    throw new IllegalStateException("Failed to avoid collision");
                }
            }
        }

        int totalParameters = getTotalParameters(mappings);
        int named = (defaultParameterNameMap.size() + extraParameters.size());
        System.out.println("Parameter coverage: " + defaultParameterNameMap.size() + " -> " + named + " / " + totalParameters + " (" + ((named / (double)totalParameters) * 100.0) + "%)");

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

    private Map<String, String> getDefaultParameterNameMap(MCPConnection mcpConn) {
        return IOHelper
                .readCSV(new File(mcpConn.getDir(), "params.csv"))
                .stream()
                .collect(Collectors.toMap(l -> l[0], l -> l[1]));
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
