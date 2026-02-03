package sledgemc.dev.transform;

import org.spongepowered.asm.mixin.extensibility.IRemapper;

/**
 * Adapter to expose MappingService to Mixin.
 */
public class SledgeRemapper implements IRemapper {

    private final MappingService mappingService;

    public SledgeRemapper(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        return mappingService.mapMethodName(owner, name, desc);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        return mappingService.mapFieldName(owner, name);
    }

    @Override
    public String map(String typeName) {
        return mappingService.mapClassName(typeName);
    }

    @Override
    public String unmap(String typeName) {
        // Not used for runtime application usually, but good practice
        return typeName; 
    }

    @Override
    public String mapDesc(String desc) {
        // Basic descriptor remapping could be done here if needed
        return desc;
    }

    @Override
    public String unmapDesc(String desc) {
        return desc;
    }
}
