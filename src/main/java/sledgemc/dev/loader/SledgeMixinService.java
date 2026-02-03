package sledgemc.dev.loader;

import org.spongepowered.asm.service.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.util.ReEntranceLock;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Custom Mixin service for SledgeMC.
 */
public class SledgeMixinService extends MixinServiceAbstract {

    private final IClassProvider classProvider = new SledgeClassProvider();
    private final IClassBytecodeProvider bytecodeProvider = new SledgeBytecodeProvider();

    @Override
    public String getName() {
        return "SledgeMC";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.mixin.transformer.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleVirtual(getName());
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    // Mandatory for Mixin 0.8+
    @Override
    public ReEntranceLock getReEntranceLock() {
        return this.lock;
    }
}
