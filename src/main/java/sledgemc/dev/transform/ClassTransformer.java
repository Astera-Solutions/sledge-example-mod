package sledgemc.dev.transform;

/**
 * Interface for ASM-based class transformers.
 */
public interface ClassTransformer {

    boolean shouldTransform(String className);

    byte[] transform(String className, byte[] classBytes);

    default String getName() {
        return getClass().getSimpleName();
    }

    default int getPriority() {
        return 1000;
    }
}
