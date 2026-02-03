package sledgemc.dev.transform;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading and parsing of mapping files (Tiny v2) using mapping-io.
 */
public class MappingService {

    private final Path mappingsDir;
    private final Map<String, String> classMap = new HashMap<>();
    private final Map<String, String> methodMap = new HashMap<>();
    private final Map<String, String> fieldMap = new HashMap<>();

    private String activeNamespace = "intermediary";

    public MappingService(Path gameDir) {
        this.mappingsDir = gameDir.resolve("mappings");
    }

    public void loadMappings(String mcVersion, String type) {
        this.activeNamespace = type.toLowerCase();
        try {
            Files.createDirectories(mappingsDir);

            Path mappingFile;
            String downloadUrl;

            if (activeNamespace.equals("mojang")) {
                mappingFile = mappingsDir.resolve("mojang-" + mcVersion + ".txt");
                // 1.21.4 Client Mapping URL
                downloadUrl = "https://piston-data.mojang.com/v1/objects/1ced3ce4141680d220876402ec9713ddac247f0d/client.txt";
            } else {
                mappingFile = mappingsDir.resolve("intermediary-" + mcVersion + ".tiny");
                downloadUrl = "https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + mcVersion
                        + ".tiny";
            }

            // Auto-download if missing
            if (!Files.exists(mappingFile)) {
                System.out.println("[SledgeMC] Downloading " + activeNamespace + " mappings for " + mcVersion + "...");
                try (java.io.InputStream in = java.net.URI.create(downloadUrl).toURL().openStream()) {
                    Files.copy(in, mappingFile);
                }
            }

            System.out.println("[SledgeMC] Parsing mappings: " + mappingFile.getFileName());
            MemoryMappingTree tree = new MemoryMappingTree();

            // mapping-io automatically detects format (Tiny, ProGuard, etc.)
            MappingReader.read(mappingFile, tree);

            int srcId;
            int dstId = tree.getNamespaceId("official");

            if (activeNamespace.equals("mojang")) {
                srcId = tree.getNamespaceId("named");
                if (srcId == -1)
                    srcId = 0;
            } else {
                srcId = tree.getNamespaceId("intermediary");
                if (srcId == -1)
                    srcId = 0;
            }

            if (dstId == -1)
                dstId = 1;

            // Populate caches
            for (MappingTree.ClassMapping classMapping : tree.getClasses()) {
                String srcName = classMapping.getName(srcId);
                String dstName = classMapping.getName(dstId);

                if (srcName != null && dstName != null) {
                    classMap.put(srcName, dstName);

                    for (MappingTree.MethodMapping method : classMapping.getMethods()) {
                        String mSrc = method.getName(srcId);
                        String mDst = method.getName(dstId);
                        String mDesc = method.getDesc(srcId);
                        if (mSrc != null && mDst != null) {
                            methodMap.put(srcName + "." + mSrc + (mDesc != null ? mDesc : ""), mDst);
                        }
                    }

                    for (MappingTree.FieldMapping field : classMapping.getFields()) {
                        String fSrc = field.getName(srcId);
                        String fDst = field.getName(dstId);
                        if (fSrc != null && fDst != null) {
                            fieldMap.put(srcName + "." + fSrc, fDst);
                        }
                    }
                }
            }

            System.out.println("[SledgeMC] Loaded " + classMap.size() + " " + activeNamespace + " class mappings");

        } catch (Exception e) {
            System.err.println("[SledgeMC] Failed to load mappings: " + e.getMessage());
        }
    }

    // Helper to remap standard Java notation
    public String mapClassName(String name) {
        String internal = name.replace('.', '/');
        String mapped = classMap.getOrDefault(internal, internal);
        return mapped.replace('/', '.');
    }

    public String mapMethodName(String owner, String name, String desc) {
        String key = owner.replace('.', '/') + "." + name + desc;
        return methodMap.getOrDefault(key, name);
    }

    public String mapFieldName(String owner, String name) {
        String key = owner.replace('.', '/') + "." + name;
        return fieldMap.getOrDefault(key, name);
    }
}
