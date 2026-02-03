package sledgemc.dev.loader;

import sledgemc.dev.api.Environment;
import sledgemc.dev.api.SledgeAPI;
import sledgemc.dev.event.EventBus;
import sledgemc.dev.transform.TransformService;
import sledgemc.dev.transform.MappingService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main modloader class - handles mod discovery, loading, and lifecycle.
 */
public class SledgeLoader {

    public static final String VERSION = "1.0.0";
    public static final String MINECRAFT_VERSION = "1.21.8";

    private static SledgeLoader instance;

    private final Environment environment;
    private final Path gameDir;
    private final Path modsDir;
    private final Path configDir;

    private final EventBus eventBus;
    private final ModDiscovery modDiscovery;
    private final TransformService transformService;
    private final MappingService mappingService;

    private final Map<String, ModContainer> loadedMods = new LinkedHashMap<>();
    private boolean initialized = false;

    public SledgeLoader(Environment environment, Path gameDir) {
        this.environment = environment;
        this.gameDir = gameDir;
        this.modsDir = gameDir.resolve("mods");
        this.configDir = gameDir.resolve("config");

        this.eventBus = new EventBus("sledgemc");
        this.modDiscovery = new ModDiscovery(modsDir);
        this.transformService = new TransformService();
        this.mappingService = new MappingService(gameDir);

        instance = this;
    }

    public void initialize() throws Exception {
        if (initialized)
            throw new IllegalStateException("Already initialized!");

        System.out.println("========================================");
        System.out.println(" SledgeMC ModLoader v" + VERSION);
        System.out.println(" Minecraft " + MINECRAFT_VERSION);
        System.out.println("========================================");

        SledgeAPI api = new SledgeAPI(environment, gameDir, MINECRAFT_VERSION, VERSION, eventBus);
        SledgeAPI.setInstance(api);

        // Discover mods
        List<ModContainer> discovered = modDiscovery.discoverMods();
        System.out.println("[SledgeMC] Found " + discovered.size() + " mod(s)");

        // Check dependencies
        Map<String, String> missing = modDiscovery.validateDependencies(discovered);
        if (!missing.isEmpty()) {
            System.err.println("[SledgeMC] Missing deps: " + missing);
        }

        Map<String, String> conflicts = modDiscovery.detectConflicts(discovered);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Mod conflicts: " + conflicts);
        }

        // Load mappings (Support to switch between intermediary and mojang)
        String mappingType = System.getProperty("sledgemc.mappings", "mojang");
        mappingService.loadMappings(MINECRAFT_VERSION, mappingType);

        // Register remapper for runtime deobfuscation
        try {
            org.spongepowered.asm.mixin.MixinEnvironment.getDefaultEnvironment().getRemappers()
                    .add(new sledgemc.dev.transform.SledgeRemapper(mappingService));
        } catch (Exception e) {
            System.err.println("[SledgeMC] Failed to register remapper: " + e.getMessage());
        }

        // Register mixins
        for (ModContainer mod : discovered) {
            // SpongePowered Mixins
            List<String> mixins = mod.getMetadata().getMixins();
            if (mixins != null && !mixins.isEmpty()) {
                transformService.registerMixinConfigs(mod.getModId(), mixins);
            }

            // InjectaCore Mixins
            List<String> injectaMixins = mod.getMetadata().getInjectaMixins();
            if (injectaMixins != null && !injectaMixins.isEmpty()) {
                for (String mixinClass : injectaMixins) {
                    try {
                        // Load the class to register it in the engine
                        Class<?> clazz = Class.forName(mixinClass, false,
                                Thread.currentThread().getContextClassLoader());
                        sledgemc.dev.injecta.core.InjectaEngine.registerMixin(clazz);
                        System.out.println("[SledgeMC] Registered Injecta mixin: " + mixinClass);
                    } catch (ClassNotFoundException e) {
                        System.err.println("[SledgeMC] Failed to find Injecta mixin: " + mixinClass);
                    }
                }
            }
        }

        // Load mods
        for (ModContainer mod : discovered) {
            try {
                if (!mod.getMetadata().supportsEnvironment(environment == Environment.CLIENT_MODE)) {
                    continue;
                }
                mod.loadEntrypoints();
                loadedMods.put(mod.getModId(), mod);
                SledgeAPI.getInstance().registerMod(mod.getModId(), mod);
                System.out.println("[SledgeMC] Loaded: " + mod.getName());
            } catch (Exception e) {
                mod.setError(e);
                e.printStackTrace();
            }
        }

        // Initialize mods
        for (ModContainer mod : loadedMods.values()) {
            try {
                mod.initialize();
            } catch (Exception e) {
                mod.setError(e);
                e.printStackTrace();
            }
        }

        // Environment-specific init
        for (ModContainer mod : loadedMods.values()) {
            if (environment == Environment.CLIENT_MODE) {
                mod.initializeClient();
            } else {
                mod.initializeServer();
            }
        }

        initialized = true;
        System.out.println("[SledgeMC] Loaded " + loadedMods.size() + " mod(s)");
    }

    public static SledgeLoader getInstance() {
        if (instance == null)
            throw new IllegalStateException("Not initialized!");
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null && instance.initialized;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Path getGameDir() {
        return gameDir;
    }

    public Path getModsDir() {
        return modsDir;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public TransformService getTransformService() {
        return transformService;
    }

    public MappingService getMappingService() {
        return mappingService;
    }

    public int getModCount() {
        return loadedMods.size();
    }

    public Collection<ModContainer> getAllMods() {
        return Collections.unmodifiableCollection(loadedMods.values());
    }

    public Optional<ModContainer> getMod(String modId) {
        return Optional.ofNullable(loadedMods.get(modId));
    }

    public boolean isModLoaded(String modId) {
        return loadedMods.containsKey(modId);
    }

    public static void main(String[] args) {
        try {
            Path gameDir = args.length > 0 ? Paths.get(args[0])
                    : Paths.get(System.getProperty("user.home"), ".minecraft");
            Environment env = args.length > 1 && "server".equalsIgnoreCase(args[1]) ? Environment.SERVER_MODE
                    : Environment.CLIENT_MODE;

            new SledgeLoader(env, gameDir).initialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
