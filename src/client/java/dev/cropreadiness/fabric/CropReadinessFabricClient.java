package dev.cropreadiness.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CropReadinessFabricClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(CropReadinessFabric.MOD_ID, "controls")
    );
    private static final KeyMapping OPEN_CONFIG = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.crop_helper.open_config", GLFW.GLFW_KEY_C, CATEGORY)
    );

    @Override
    public void onInitializeClient() {
        CropReadinessConfig.load();
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(CropReadinessRenderer::shouldRenderVanillaOutline);
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(CropReadinessRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CONFIG.consumeClick()) {
                client.setScreenAndShow(new CropReadinessConfigScreen(null));
            }
        });
    }
}
