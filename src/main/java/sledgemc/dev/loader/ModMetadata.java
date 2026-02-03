package sledgemc.dev.loader;

import com.google.gson.annotations.SerializedName;
import java.util.*;

/**
 * Mod metadata parsed from sledge.mod.json
 */
public class ModMetadata {

    @SerializedName("modId")
    private String id;

    @SerializedName("version")
    private String version;

    @SerializedName("info")
    private ModInfo info;

    @SerializedName("loaders")
    private LoaderConfig loaders;

    @SerializedName("transformers")
    private TransformerConfig transformers;

    @SerializedName("dependencies")
    private Map<String, String> dependencies = new HashMap<>();

    @SerializedName("env")
    private String environment = "*";

    // Getters for compatibility
    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return (info != null && info.name != null) ? info.name : id;
    }

    public String getDescription() {
        return (info != null && info.description != null) ? info.description : "";
    }

    public List<String> getAuthors() {
        return (info != null && info.contributors != null) ? info.contributors : Collections.emptyList();
    }

    public EntrypointsConfig getEntrypoints() {
        if (loaders == null)
            return new EntrypointsConfig();
        EntrypointsConfig config = new EntrypointsConfig();
        config.main = loaders.common;
        config.client = loaders.client;
        config.server = loaders.server;
        return config;
    }

    public Map<String, String> getDepends() {
        return dependencies;
    }

    public Map<String, String> getBreaks() {
        return Collections.emptyMap(); // Removed from new schema for simplicity
    }

    public List<String> getMixins() {
        return (transformers != null && transformers.mixins != null) ? transformers.mixins : Collections.emptyList();
    }

    public List<String> getInjectaMixins() {
        return (transformers != null && transformers.injecta != null) ? transformers.injecta : Collections.emptyList();
    }

    public boolean supportsEnvironment(boolean isClient) {
        if ("*".equals(environment))
            return true;
        return isClient ? "client".equalsIgnoreCase(environment) : "server".equalsIgnoreCase(environment);
    }

    public boolean isValid() {
        return id != null && !id.isEmpty() && version != null && !version.isEmpty();
    }

    public static class ModInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("description")
        private String description;
        @SerializedName("contributors")
        private List<String> contributors = new ArrayList<>();
    }

    public static class LoaderConfig {
        @SerializedName("common")
        private List<String> common = new ArrayList<>();
        @SerializedName("client")
        private List<String> client = new ArrayList<>();
        @SerializedName("server")
        private List<String> server = new ArrayList<>();
    }

    public static class TransformerConfig {
        @SerializedName("mixins")
        private List<String> mixins = new ArrayList<>();
        @SerializedName("injecta")
        private List<String> injecta = new ArrayList<>();
    }

    public static class EntrypointsConfig {
        public List<String> main = new ArrayList<>();
        public List<String> client = new ArrayList<>();
        public List<String> server = new ArrayList<>();

        public List<String> getMain() {
            return main;
        }

        public List<String> getClient() {
            return client;
        }

        public List<String> getServer() {
            return server;
        }

        public List<String> getPreLaunch() {
            return Collections.emptyList();
        }
    }
}
