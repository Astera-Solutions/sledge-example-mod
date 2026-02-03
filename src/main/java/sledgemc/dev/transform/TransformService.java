package sledgemc.dev.transform;

import java.util.*;

/**
 * Handles mixin config registration and class transformers.
 */
public class TransformService {

    private final Map<String, List<String>> mixinConfigs = new LinkedHashMap<>();
    private final List<ClassTransformer> transformers = new ArrayList<>();
    private boolean initialized = false;

    public void registerMixinConfigs(String modId, List<String> configs) {
        mixinConfigs.computeIfAbsent(modId, k -> new ArrayList<>()).addAll(configs);
    }

    public void registerTransformer(ClassTransformer transformer) {
        transformers.add(transformer);
    }

    public void initialize() {
        if (initialized)
            return;

        // In production, call MixinBootstrap.init() and Mixins.addConfiguration() here
        for (Map.Entry<String, List<String>> entry : mixinConfigs.entrySet()) {
            for (String config : entry.getValue()) {
                System.out.println("[SledgeMC] Mixin config: " + config);
            }
        }

        initialized = true;
    }

    public byte[] transform(String className, byte[] classBytes) {
        byte[] result = classBytes;
        for (ClassTransformer transformer : transformers) {
            if (transformer.shouldTransform(className)) {
                result = transformer.transform(className, result);
            }
        }
        return result;
    }

    public int getMixinConfigCount() {
        return mixinConfigs.values().stream().mapToInt(List::size).sum();
    }

    public int getTransformerCount() {
        return transformers.size();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
