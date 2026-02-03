package sledgemc.dev.loader;

import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fabric-style bootstrap entry point for SledgeMC.
 */
public class SledgeBootstrap {

    static {
        System.err.println("\n[SledgeMC] STATIC INITIALIZER HIT - CLASS LOADED SUCCESSFULLY\n");
    }

    public static void main(String[] args) {
        // More prominent banner for TLauncher console
        System.out.println("\n\n");
        System.out.println("############################################################");
        System.out.println("#                                                          #");
        System.out.println("#                SLEDGE MC BOOTSTRAP ACTIVE                #");
        System.out.println("#                ==========================                #");
        System.out.println("#                                                          #");
        System.out.println("############################################################");
        System.out.println("\n");

        try {
            // 1. Setup ClassPath for transformation
            String cp = System.getProperty("java.class.path");
            String[] cpEntries = cp.split(File.pathSeparator);
            List<URL> urls = new ArrayList<>();
            for (String entry : cpEntries) {
                urls.add(new File(entry).toURI().toURL());
            }

            // 2. Initialize SledgeClassLoader with captured classpath
            SledgeClassLoader loader = new SledgeClassLoader(urls.toArray(new URL[0]),
                    SledgeBootstrap.class.getClassLoader().getParent());
            Thread.currentThread().setContextClassLoader(loader);

            System.out.println("[SledgeMC] SledgeClassLoader initialized with " + urls.size() + " entries");

            // 3. Initialize Mixin
            initMixin(loader);

            // 4. Initialize SledgeLoader (Mod discovery & transformation setup)
            initSledgeLoader(loader, args);

            // 5. Fix Arguments
            String[] finalArgs = verifyArgs(args);

            // 6. Find and Launch Minecraft
            String mainClass = "net.minecraft.client.main.Main";
            System.out.println("[SledgeMC] HANDING OVER TO MINECRAFT MAIN...");

            Class<?> mcMain = Class.forName(mainClass, true, loader);
            Method mainMethod = mcMain.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) finalArgs);

        } catch (Exception e) {
            System.err.println("[SledgeMC] FATAL BOOTSTRAP ERROR!");
            e.printStackTrace();
            // Try to show a popup if possible in case console is hidden
            try {
                Class.forName("javax.swing.JOptionPane")
                        .getMethod("showMessageDialog", java.awt.Component.class, Object.class, String.class, int.class)
                        .invoke(null, null, "SledgeMC Bootstrap Failed: " + e.toString(), "SledgeMC Error", 0);
            } catch (Exception ignored) {
            }
            System.exit(1);
        }
    }

    private static void initSledgeLoader(ClassLoader loader, String[] args) {
        try {
            System.out.println("[SledgeMC] Initializing SledgeLoader...");

            // Find gameDir from args or default
            Path gameDir = Paths.get(System.getProperty("user.home"), ".minecraft");
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("--gameDir")) {
                    gameDir = Paths.get(args[i + 1]);
                    break;
                }
            }
            System.out.println("[SledgeMC] Resolved gameDir: " + gameDir.toAbsolutePath());

            // Load SledgeLoader via Reflection using our new ClassLoader
            Class<?> sledgeLoaderClass = Class.forName("sledgemc.dev.loader.SledgeLoader", true, loader);
            Class<?> envClass = Class.forName("sledgemc.dev.api.Environment", true, loader);
            Object clientEnv = envClass.getField("CLIENT_MODE").get(null);

            Object sledgeLoader = sledgeLoaderClass.getConstructor(envClass, Path.class).newInstance(clientEnv,
                    gameDir);
            sledgeLoaderClass.getMethod("initialize").invoke(sledgeLoader);

            System.out.println("[SledgeMC] SledgeLoader initialized and mods loaded!");
        } catch (Exception e) {
            System.err.println("[SledgeMC] SledgeLoader initialization failed!");
            e.printStackTrace();
        }
    }

    private static String[] verifyArgs(String[] args) {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        if (!argList.contains("--accessToken")) {
            argList.add("--accessToken");
            argList.add("0");
        }
        if (!argList.contains("--uuid")) {
            argList.add("--uuid");
            argList.add("0");
        }
        if (!argList.contains("--username")) {
            argList.add("--username");
            argList.add("SledgePlayer");
        }
        if (!argList.contains("--userType")) {
            argList.add("--userType");
            argList.add("msa");
        }
        if (!argList.contains("--version")) {
            argList.add("--version");
            argList.add("SledgeMC");
        }
        return argList.toArray(new String[0]);
    }

    private static void initMixin(ClassLoader loader) {
        try {
            System.setProperty("mixin.service", "sledgemc.dev.loader.SledgeMixinService");
            System.out.println("[SledgeMC] Bootstrapping Mixin...");
            Class<?> mixinBootstrap = Class.forName("org.spongepowered.asm.launch.MixinBootstrap", true, loader);
            mixinBootstrap.getMethod("init").invoke(null);
            System.out.println("[SledgeMC] Mixin initialized successfully");
        } catch (Exception e) {
            System.err.println("[SledgeMC] Mixin initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
