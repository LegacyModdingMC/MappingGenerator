package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Lists;
import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Sets;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import io.legacymoddingmc.mappinggenerator.name.Name;
import lombok.*;

import javax.tools.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SourceInfo {

    private final Map<String, ClassInfo> data = new HashMap<>();

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
            System.out.println();
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + data.keySet().size() + " classes in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
    }

    public void addVariable(PoorlyDescribedMethod method, String name) {
        data.computeIfAbsent(method.getKlass(), x -> new ClassInfo()).methods.computeIfAbsent(method, x -> new ClassInfo.MethodInfo()).getVariables().add(name);
    }

    public static class ClassInfo {
        @Getter
        private final Map<PoorlyDescribedMethod, ClassInfo.MethodInfo> methods = new HashMap<>();

        @NoArgsConstructor
        public static class MethodInfo {
            @Getter
            private final List<String> variables = new ArrayList<>();
        }
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
                si.addVariable(new PoorlyDescribedMethod(cu.getPackageName().toString() + "." + className, method.getName().toString(), method.getParameters().stream().map(v -> new PoorlyDescribedMethod.Parameter(v.getType().toString(), v.getName().toString())).collect(Collectors.toList()), String.valueOf(method.getReturnType())), variableTree.getName().toString());
            }
            return super.visitVariable(variableTree, si);
        }
    }

    @Data
    @AllArgsConstructor
    private static class PoorlyDescribedMethod implements Name {
        private String klass;
        private String method;
        private List<Parameter> parameters = new ArrayList<>();
        private String returnType;

        @Override
        public String toString() {
            return StringUtils.defaultString(klass, "?") + " "
                    + StringUtils.defaultString(method, "?")
                    + "(" + String.join(", ", parameters.stream().map(Object::toString).toArray(String[]::new)) + ")"
                    + returnType;
        }

        @Data
        @AllArgsConstructor
        private static class Parameter {
            private final String type;
            private final String name;

            @Override
            public String toString() {
                return type + " " + name;
            }
        }
    }

}
