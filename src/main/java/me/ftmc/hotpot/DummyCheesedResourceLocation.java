package me.ftmc.hotpot;

import net.minecraft.util.Identifier;

public class DummyCheesedResourceLocation extends Identifier {
    private final Identifier originalTextureLocation;

    public DummyCheesedResourceLocation(Identifier originalTextureLocation, String namespace, String path) {
        super(namespace, path);
        this.originalTextureLocation = originalTextureLocation;
    }

    public Identifier getOriginalTextureLocation() {
        return originalTextureLocation;
    }
}
