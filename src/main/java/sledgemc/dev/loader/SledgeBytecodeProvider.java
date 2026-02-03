package sledgemc.dev.loader;

import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.IOException;
import java.io.InputStream;

/**
 * Minimal Bytecode provider for SledgeMC Mixin service.
 */
public class SledgeBytecodeProvider implements IClassBytecodeProvider {

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int flags)
            throws ClassNotFoundException, IOException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null)
                throw new ClassNotFoundException(name);
            byte[] bytes = is.readAllBytes();

            ClassNode node = new ClassNode();
            ClassReader reader = new ClassReader(bytes);
            reader.accept(node, flags);
            return node;
        }
    }
}
