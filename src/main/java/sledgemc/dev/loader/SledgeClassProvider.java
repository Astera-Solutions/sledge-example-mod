package sledgemc.dev.loader;

import org.spongepowered.asm.service.IClassProvider;
import java.net.URL;

/**
 * Class provider for SledgeMC Mixin service.
 */
public class SledgeClassProvider implements IClassProvider {

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, SledgeClassProvider.class.getClassLoader());
    }
}
