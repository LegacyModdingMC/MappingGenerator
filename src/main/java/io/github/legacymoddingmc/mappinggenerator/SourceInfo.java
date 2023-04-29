package io.github.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Lists;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Sets;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import javax.tools.*;
import java.io.File;
import java.util.*;

@RequiredArgsConstructor
public class SourceInfo {

    private final Map<String, Collection<String>> data = new HashMap<>();

    @SneakyThrows
    public void load(File jar) {
        long t0 = System.nanoTime();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList("-sourcepath", jar.getPath()), null, null);
        Iterable<JavaFileObject> sources = fileManager.list(StandardLocation.SOURCE_PATH, "", Sets.newHashSet(JavaFileObject.Kind.SOURCE), true);

        JavaCompiler.CompilationTask task2 = compiler.getTask(null, fileManager, null, null, null, sources);
        val parsed = Lists.newArrayList(((JavacTask) task2).parse());

        for(CompilationUnitTree cu : parsed) {
            MyVisitor visitor = new MyVisitor();
            visitor.scan(cu, this);
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + data.keySet().size() + " sources in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
    }

    public void addVariable(String methodSrgId, String name) {
        data.computeIfAbsent(methodSrgId, x -> new HashSet<String>()).add(name);
    }

    public Collection<String> getVariablesInMethodsWithSrgId(String srgId) {
        return data.getOrDefault(srgId, Collections.emptySet());
    }

    public static class MyVisitor extends TreePathScanner<Void, SourceInfo> {

        @Override
        public Void visitVariable(VariableTree variableTree, SourceInfo si) {
            TreePath path = getCurrentPath();
            String msg = "";

            MethodTree method = null;
            int methodIndex = -1;
            BlockTree block = null;
            int blockIndex = -1;
            String className = "";
            CompilationUnitTree cu = null;

            int i = 0;
            while(path != null) {
                Tree leaf = path.getLeaf();

                if(method == null && leaf instanceof MethodTree) {
                    method = (MethodTree) leaf;
                    methodIndex = i;
                } else if(block == null && leaf instanceof BlockTree) {
                    block = (BlockTree) leaf;
                    blockIndex = i;
                }
                if(leaf instanceof ClassTree) {
                    ClassTree klass = (ClassTree) leaf;
                    String name = klass.getSimpleName().toString();
                    if(className.isEmpty()) {
                        className = name;
                    } else {
                        className = name + "$" + className;
                    }
                }
                if(leaf instanceof CompilationUnitTree) {
                    cu = (CompilationUnitTree) leaf;
                }

                i++;
                path = path.getParentPath();
            }

            if(method != null && block != null && blockIndex < methodIndex) {
                if(!method.getParameters().isEmpty()) {
                    String name = method.getParameters().get(0).getName().toString();
                    if(name.startsWith("p_")) {
                        String srgId = name.split("_")[1];
                        si.addVariable(srgId, variableTree.getName().toString());
                    }
                }
            }
            return super.visitVariable(variableTree, si);
        }
    }
}
