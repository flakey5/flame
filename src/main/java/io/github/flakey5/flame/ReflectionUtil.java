package io.github.flakey5.flame;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util methods dealing with reflection bits
 */
public final class ReflectionUtil {
    /**
     * Finds all classes in a certain package
     * @param packageName Package to search
     * @return Classes in the package
     */
    @SuppressWarnings("rawtypes")
    public static Set<Class> getClassesInPackage(@NonNull String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null)
            throw new RuntimeException("stream is null");

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    /**
     * Finds a class in a certain package
     * @param className Class to find
     * @param packageName Package to look in
     * @return Class object
     */
    @SuppressWarnings("rawtypes")
    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
