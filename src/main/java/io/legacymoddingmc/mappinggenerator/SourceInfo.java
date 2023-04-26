package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.StaticJavaParser;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.ast.CompilationUnit;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.ast.Node;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.ast.body.*;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.gtnewhorizons.retrofuturagradle.shadow.com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.gson.Gson;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.FileUtils;
import com.gtnewhorizons.retrofuturagradle.util.Utilities;
import io.legacymoddingmc.mappinggenerator.name.Method;
import lombok.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;

import org.gradle.api.Project;
import org.gradle.api.tasks.WorkResult;

@RequiredArgsConstructor
public class SourceInfo {

    private final String gameVersion;

    private final Map<String, ClassInfo> data = new HashMap<>();

    @SneakyThrows
    public void load(File jar, Project project) {
        long t0 = System.nanoTime();

        File extractedDir = extractJar(jar, project);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(extractedDir)
        );
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        Map<String, byte[]> resourceMap = new HashMap<>();
        Map<String, String> sourceMap = new HashMap<>();

        Utilities.loadMemoryJar(jar, resourceMap, sourceMap);

        for(val e : sourceMap.entrySet()) {
            String fileName = e.getKey();
            String source = e.getValue();

            val cu = StaticJavaParser.parse(source);
            new Printer().visit(cu, this);
        }

        File outFile = FileUtils.getFile(GradleUtils.getCacheDir(project), "data", jar.getName() + ".json");
        outFile.getParentFile().mkdirs();
        try(FileWriter fw = new FileWriter(outFile)) {
            new Gson().toJson(data, fw);
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + data.keySet().size() + " classes in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
    }

    private File extractJar(File jar, Project project) {
        File outDir = new File(project.getBuildDir(), "mapping_generator/extracted/" + jar.getName().split("\\.")[0]);
        outDir.mkdirs();
        WorkResult work = project.copy(a -> {
            a.from(project.zipTree(jar));
            a.into(outDir);
        });
        return outDir;
    }

    public void addVariable(Method m, String name) {
        data.computeIfAbsent(m.getKlass(), x -> new ClassInfo()).methods.computeIfAbsent(m.getMethod() + " " + m.getDesc(), x -> new ClassInfo.MethodInfo()).getVariables().add(name);
    }

    public static class ClassInfo {
        @Getter
        private final Map<String, ClassInfo.MethodInfo> methods = new HashMap<>();

        @NoArgsConstructor
        public static class MethodInfo {
            @Getter
            private final List<String> variables = new ArrayList<>();
        }
    }

    private static class Printer extends VoidVisitorAdapter<SourceInfo> {
        @Override
        public void visit(VariableDeclarator n, SourceInfo si) {
            si.addVariable(getEnclosingMethod(n), n.getNameAsString());
            super.visit(n, si);
        }

        private static Method getEnclosingMethod(Node n) {
            String packageName = ((CompilationUnit)(n.findAncestor(CompilationUnit.class).get())).getPackageDeclaration().get().getNameAsString();
            TypeDeclaration td = (TypeDeclaration)n.findAncestor(TypeDeclaration.class).get();
            Optional<String> optionalKlassName = td.getFullyQualifiedName();
            String klassName = optionalKlassName.get();
            klassName = klassName.substring(0, packageName.length() + 1) + klassName.substring(packageName.length() + 1).replaceAll("\\.", Matcher.quoteReplacement("$"));

            CallableDeclaration method = n.findAncestor(CallableDeclaration.class).orElse(null);
            String methodName = null;
            String desc = null;
            try {
                if(method != null) {
                    if(method instanceof ConstructorDeclaration) {
                        methodName = "<init>";
                        desc = ((ConstructorDeclaration)method).toDescriptor();
                    } else if(method instanceof MethodDeclaration) {
                        methodName = method.getName().asString();
                        desc = ((MethodDeclaration)method).toDescriptor();
                    }
                }
            } catch(Exception e){

            }
            return new Method(klassName, methodName, desc);
        }
    }

}
