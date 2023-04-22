package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.download.MCPConnection;
import io.legacymoddingmc.mappinggenerator.download.SrgConnection;
import io.legacymoddingmc.mappinggenerator.name.Method;
import io.legacymoddingmc.mappinggenerator.source.IMappingSource;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MappingGenerator {

    private final Project project;
    private final List<IMappingSource> sources = new ArrayList<>();

    public MappingGenerator(Project project) {
        this.project = project;
    }

    public void addSource(IMappingSource source) {
        sources.add(source);
    }

    public void generateExtraParameters(File out) {
        MappingCollection mappings = new MappingCollection();

        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "client.jar"));
        mappings.addVanillaJar("1.7.10", Utilities.getCacheDir(project, "mc-vanilla", "1.7.10", "server.jar"));

        MCPConnection mcpConn = new MCPConnection(project, "1.7.10", "stable_12");

        mappings.load(new SrgConnection(project, "1.7.10"));
        mappings.load(mcpConn);

        Map<String, String> extraParameters = new HashMap<>();

        for(IMappingSource source : sources) {
            source.generateExtraParameters(project, mappings, extraParameters);
        }

        Set<String> defaultParameterNames = getDefaultParameterNames(mcpConn);
        Set<String> methodIds = mappings
                .getNames("1.7.10", "srgId", Method.class)
                .stream()
                .map(Method::getMethod)
                .collect(Collectors.toSet());

        // Remove useless entries
        extraParameters.entrySet().removeIf(e -> defaultParameterNames.contains(e.getKey()) || !methodIds.contains(e.getKey().split("_")[1]));

        int totalParameters = getTotalParameters(mappings);
        int named = (defaultParameterNames.size() + extraParameters.size());
        System.out.println("Parameter coverage: " + defaultParameterNames.size() + " -> " + named + " / " + totalParameters + " (" + ((named / (double)totalParameters) * 100.0) + "%)");

        writeMappings(extraParameters, out);
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
