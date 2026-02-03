package sledgemc.dev.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom ClassLoader for SledgeMC that handles bytecode transformation.
 */
public class SledgeClassLoader extends URLClassLoader {

    private final Set<String> forcedClasses = new HashSet<>();

    public SledgeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);

        // Define packages that MUST be loaded by this classloader for isolation
        forcedClasses.add("sledgemc.dev.loader.");
        forcedClasses.add("sledgemc.dev.api.");
        forcedClasses.add("org.spongepowered.asm.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. Check if already loaded
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                // 2. Check if it's a class we want to transform or isolate
                boolean transformable = false;
                if (name.startsWith("net.minecraft.") ||
                        name.startsWith("com.mojang.") ||
                        name.startsWith("sledgemc.dev.loader.") ||
                        name.startsWith("sledgemc.dev.api.")) {
                    transformable = true;
                }

                if (transformable) {
                    try {
                        c = findClass(name);
                    } catch (ClassNotFoundException e) {
                        // Fallback to parent
                    }
                }
            }

            if (c == null) {
                c = super.loadClass(name, resolve);
            } else if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = getClassBytes(name);
        if (bytes != null) {
            // Apply InjectaCore transformations
            bytes = sledgemc.dev.injecta.core.InjectaTransformer.transform(name, bytes);

            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }

    private byte[] getClassBytes(String name) {
        String path = name.replace('.', '/') + ".class";
        try (InputStream is = getResourceAsStream(path)) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (IOException e) {
            // Ignore
        }
        return null;
    }
}
