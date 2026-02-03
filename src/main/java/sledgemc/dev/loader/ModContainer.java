package sledgemc.dev.loader;

import sledgemc.dev.api.ClientInit;
import sledgemc.dev.api.SledgeInit;
import sledgemc.dev.api.ServerInit;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for a single mod instance.
 */
public class ModContainer {

    private final ModMetadata metadata;
    private final Path modPath;
    private final URLClassLoader classLoader;

    private ModState state = ModState.DISCOVERED;
    private SledgeInit mainInitializer;
    private ClientInit clientInitializer;
    private ServerInit serverInitializer;
    private List<Object> entrypoints = new ArrayList<>();
    private Throwable lastError;

    public ModContainer(ModMetadata metadata, Path modPath, URLClassLoader classLoader) {
        this.metadata = metadata;
        this.modPath = modPath;
        this.classLoader = classLoader;
    }

    public void loadEntrypoints() throws Exception {
        if (state != ModState.DISCOVERED) {
            throw new IllegalStateException("Cannot load entrypoints in state: " + state);
        }

        state = ModState.LOADING;

        ModMetadata.EntrypointsConfig entryConfig = metadata.getEntrypoints();
        if (entryConfig == null) {
            state = ModState.LOADED;
            return;
        }

        for (String className : entryConfig.getMain()) {
            Object instance = loadClass(className);
            if (instance instanceof SledgeInit)
                mainInitializer = (SledgeInit) instance;
            entrypoints.add(instance);
        }

        for (String className : entryConfig.getClient()) {
            Object instance = loadClass(className);
            if (instance instanceof ClientInit)
                clientInitializer = (ClientInit) instance;
            entrypoints.add(instance);
        }

        for (String className : entryConfig.getServer()) {
            Object instance = loadClass(className);
            if (instance instanceof ServerInit)
                serverInitializer = (ServerInit) instance;
            entrypoints.add(instance);
        }

        state = ModState.LOADED;
    }

    public void initialize() throws Exception {
        if (state != ModState.LOADED) {
            throw new IllegalStateException("Cannot initialize in state: " + state);
        }

        state = ModState.INITIALIZING;
        if (mainInitializer != null)
            mainInitializer.onInitialize();
        state = ModState.READY;
    }

    public void initializeClient() {
        if (clientInitializer != null)
            clientInitializer.onInitializeClient();
    }

    public void initializeServer() {
        if (serverInitializer != null)
            serverInitializer.onInitializeServer();
    }

    private Object loadClass(String className) throws Exception {
        return classLoader.loadClass(className).getDeclaredConstructor().newInstance();
    }

    public void setError(Throwable error) {
        this.lastError = error;
        this.state = ModState.ERRORED;
    }

    public ModMetadata getMetadata() {
        return metadata;
    }

    public String getModId() {
        return metadata.getId();
    }

    public String getVersion() {
        return metadata.getVersion();
    }

    public String getName() {
        return metadata.getName();
    }

    public Path getModPath() {
        return modPath;
    }

    public ModState getState() {
        return state;
    }

    public Throwable getLastError() {
        return lastError;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public List<Object> getEntrypoints() {
        return entrypoints;
    }

    public boolean isReady() {
        return state == ModState.READY;
    }

    public boolean hasError() {
        return state == ModState.ERRORED;
    }

    @Override
    public String toString() {
        return String.format("ModContainer{id='%s', state=%s}", getModId(), state);
    }

    public enum ModState {
        DISCOVERED, LOADING, LOADED, INITIALIZING, READY, ERRORED, DISABLED
    }
}
