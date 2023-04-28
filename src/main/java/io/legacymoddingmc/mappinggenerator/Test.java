package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Lists;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import lombok.SneakyThrows;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        long t0 = System.nanoTime();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList("-sourcepath", args[0]), null, null);
        Iterable<JavaFileObject> sources = fileManager.list(StandardLocation.SOURCE_PATH, "", newHashSet(JavaFileObject.Kind.SOURCE), true);

        CompilationTask task2 = compiler.getTask(null, fileManager, null, null, null, sources);
        val parsed = Lists.newArrayList(((JavacTask) task2).parse());

        for(CompilationUnitTree cu : parsed) {
            System.out.println("CU: " + cu.getSourceFile().getName());
            MyVisitor visitor = new MyVisitor();
            visitor.scan(cu, null);
            System.out.println();
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + 0 + " classes in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
    }

    private static <T> Set<T> newHashSet(T... elems) {
        val set = new HashSet<T>();
        for(val e : elems) {
            set.add(e);
        }
        return set;
    }

    public static class MyVisitor extends TreePathScanner<Void, Void> {

        @Override
        public Void visitVariable(VariableTree variableTree, Void unused) {
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
                System.out.println(cu.getPackageName().toString() + "." + className + " " + method.getName() + "(" + String.join(", ", method.getParameters().stream().map(v -> v.getType().toString() + " " + v.getName().toString()).collect(Collectors.toList())) + ") : " + variableTree.getType() + " " + variableTree.getName().toString());
            }
            return super.visitVariable(variableTree, unused);
        }
    }

}
