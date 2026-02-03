package sledgemc.dev.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans mods directory and loads mod metadata from JARs.
 */
public class ModDiscovery {

    private static final String MOD_JSON = "sledge.mod.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path modsDir;
    private final ClassLoader parentClassLoader;

    public ModDiscovery(Path modsDir) {
        this(modsDir, ModDiscovery.class.getClassLoader());
    }

    public ModDiscovery(Path modsDir, ClassLoader parentClassLoader) {
        this.modsDir = modsDir;
        this.parentClassLoader = parentClassLoader;
    }

    public List<ModContainer> discoverMods() throws IOException {
        List<ModContainer> mods = new ArrayList<>();

        System.out.println("[SledgeMC] Searching for mods in: " + modsDir.toAbsolutePath());
        if (!Files.exists(modsDir)) {
            System.out.println("[SledgeMC] Mods directory does not exist, creating: " + modsDir.toAbsolutePath());
            Files.createDirectories(modsDir);
            return mods;
        }

        try (var stream = Files.list(modsDir)) {
            stream.filter(p -> p.toString().endsWith(".jar"))
                    .forEach(jarPath -> {
                        System.out.println("[SledgeMC] Checking JAR: " + jarPath.getFileName());
                        try {
                            ModContainer container = loadFromJar(jarPath);
                            if (container != null) {
                                mods.add(container);
                                System.out.println(
                                        "[SledgeMC] Successfully loaded mod from JAR: " + container.getModId());
                            } else {
                                System.out.println(
                                        "[SledgeMC] JAR is not a valid SledgeMC mod: " + jarPath.getFileName());
                            }
                        } catch (Exception e) {
                            System.err.println("[SledgeMC] Failed to load: " + jarPath);
                            e.printStackTrace();
                        }
                    });
        }

        ModContainer devMod = loadDevMod();
        if (devMod != null)
            mods.add(devMod);

        return mods;
    }

    private ModContainer loadFromJar(Path jarPath) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(MOD_JSON);
            if (entry == null)
                return null;

            ModMetadata metadata;
            try (InputStream is = jarFile.getInputStream(entry);
                    InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                metadata = GSON.fromJson(reader, ModMetadata.class);
            }

            if (!metadata.isValid())
                return null;

            URL jarUrl = jarPath.toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl }, parentClassLoader);

            System.out.println("[SledgeMC] Found: " + metadata.getName() + " v" + metadata.getVersion());
            return new ModContainer(metadata, jarPath, classLoader);
        }
    }

    private ModContainer loadDevMod() {
        URL resourceUrl = parentClassLoader.getResource(MOD_JSON);
        if (resourceUrl == null)
            return null;

        try (InputStream is = resourceUrl.openStream();
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            ModMetadata metadata = GSON.fromJson(reader, ModMetadata.class);
            if (!metadata.isValid())
                return null;

            URLClassLoader classLoader = new URLClassLoader(new URL[0], parentClassLoader);
            System.out.println("[SledgeMC] Found dev mod: " + metadata.getName());
            return new ModContainer(metadata, null, classLoader);

        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, String> validateDependencies(List<ModContainer> mods) {
        Map<String, String> missing = new LinkedHashMap<>();
        Set<String> loaded = new HashSet<>();

        for (ModContainer mod : mods)
            loaded.add(mod.getModId());

        for (ModContainer mod : mods) {
            Map<String, String> deps = mod.getMetadata().getDepends();
            if (deps == null)
                continue;

            for (Map.Entry<String, String> dep : deps.entrySet()) {
                String id = dep.getKey();
                if (id.equals("sledgemc") || id.equals("minecraft") || id.equals("java"))
                    continue;
                if (!loaded.contains(id))
                    missing.put(id, dep.getValue());
            }
        }
        return missing;
    }

    public Map<String, String> detectConflicts(List<ModContainer> mods) {
        Map<String, String> conflicts = new LinkedHashMap<>();
        Set<String> loaded = new HashSet<>();

        for (ModContainer mod : mods)
            loaded.add(mod.getModId());

        for (ModContainer mod : mods) {
            Map<String, String> breaks = mod.getMetadata().getBreaks();
            if (breaks == null)
                continue;

            for (String breakId : breaks.keySet()) {
                if (loaded.contains(breakId))
                    conflicts.put(mod.getModId(), breakId);
            }
        }
        return conflicts;
    }
}
