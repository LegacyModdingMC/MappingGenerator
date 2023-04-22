package io.legacymoddingmc.mappinggenerator;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.io.IOUtils;
import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.tuple.Pair;
import com.gtnewhorizons.retrofuturagradle.shadow.org.objectweb.asm.ClassReader;
import com.gtnewhorizons.retrofuturagradle.shadow.org.objectweb.asm.Opcodes;
import com.gtnewhorizons.retrofuturagradle.shadow.org.objectweb.asm.tree.ClassNode;
import com.gtnewhorizons.retrofuturagradle.shadow.org.objectweb.asm.tree.MethodNode;
import io.legacymoddingmc.mappinggenerator.name.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor
public class JarInfo {

    private final String gameVersion;

    private final Map<String, ClassInfo> data = new HashMap<>();

    @SneakyThrows
    public void load(File jar) {
        JarFile jf = new JarFile(jar);
        for(JarEntry je : Collections.list(jf.entries())) {
            if(je.getName().endsWith(".class")) {
                byte[] bytes = IOUtils.toByteArray(jf.getInputStream(je));

                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytes);
                classReader.accept(classNode, 0);

                ClassInfo ci = data.computeIfAbsent(classNode.name, x -> new ClassInfo(classNode.superName));

                for(MethodNode method : classNode.methods) {
                    ClassInfo.MethodInfo methodInfo = new ClassInfo.MethodInfo(((method.access & Opcodes.ACC_STATIC) != 0));
                    ci.methods.put(Pair.of(method.name, method.desc), methodInfo);
                }
                data.put(classNode.name, ci);
            }
        }
    }

    public ClassInfo.MethodInfo getMethodInfo(Method method) {
        return data.get(method.getClass()).getMethods().get(Pair.of(method.getMethod(), method.getDesc()));
    }

    @RequiredArgsConstructor
    public static class ClassInfo {
        @Getter
        private final String superClass;
        @Getter
        private final Map<Pair<String, String>, MethodInfo> methods = new HashMap<>();

        @RequiredArgsConstructor
        public static class MethodInfo {
            @Getter
            private final boolean isStatic;
        }
    }

}
