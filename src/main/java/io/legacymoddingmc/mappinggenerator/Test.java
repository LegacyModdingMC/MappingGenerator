package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.collect.Lists;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.IOUtils;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
            for(Tree tree : cu.getTypeDecls()) {
                if(tree instanceof ClassTree) {
                    ClassTree classTree = (ClassTree)tree;
                    System.out.println(classTree.getSimpleName());
                    for(Tree memberTree : classTree.getMembers()) {
                        if(memberTree instanceof MethodTree) {
                            MethodTree method = (MethodTree)memberTree;
                            System.out.println("  " + method.getName());
                        }
                    }
                }
            }
            System.out.println();
        }

        long t1 = System.nanoTime();

        System.out.println("Parsed " + 0 + " classes in " + (t1-t0) / 1_000_000_000.0 + " seconds.");
    }

}
