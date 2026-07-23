package dev.cropreadiness.fabric;

import net.fabricmc.api.ModInitializer;

public final class CropReadinessFabric implements ModInitializer {
    public static final String MOD_ID = "crop_helper";

    @Override
    public void onInitialize() {
        // Client-only behavior is registered by CropReadinessFabricClient.
    }
}
