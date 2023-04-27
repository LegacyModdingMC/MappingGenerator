package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Lists;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public class Test {



    @SneakyThrows
    public static void main(String[] args) {
        File jar = new File(args[0]);

        List<File> files = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(jar.toPath())) {
            stream.filter(Files::isRegularFile).forEach(f -> {
                if(f.toFile().getName().endsWith(".java")) {
                    files.add(f.toFile());
                }
            });
        }

        long t0 = System.nanoTime();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> cus = fileManager.getJavaFileObjects(files.toArray(new File[]{}));
        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, cus);
        val parsed = Lists.newArrayList(((JavacTask) task).parse());

        for(CompilationUnitTree cu : parsed) {
            System.out.println("CU: " + cu.getSourceFile().getName());
            MyVisitor visitor = new MyVisitor();
            visitor.scan(cu, null);
            System.out.println();
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + 0 + " classes in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
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
