package dev.cropreadiness.neoforge;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.io.IOException;

final class CropReadinessConfigScreen extends Screen {
    private static final String[] NAMES = {
            "Mature crop",
            "Crop on dry soil",
            "Crop too dark",
            "Bare dry farmland",
            "Sapling obstructed"
    };
    private static final String[] DETAILS = {
            "Ready to harvest",
            "Dry farmland slows growth",
            "Light below 9 pauses growth",
            "May revert to dirt if it stays dry",
            "Nearby blocks may prevent tree growth"
    };

    private final Screen parent;
    private final EditBox[] fields = new EditBox[5];
    private String status = "";
    private int buttonY;

    CropReadinessConfigScreen(Screen parent) {
        super(Component.literal("Crop Helper Colors"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - 170;
        buttonY = Math.min(232, height - 26);
        for (int i = 0; i < fields.length; i++) {
            EditBox field = new EditBox(font, left + 235, 52 + i * 34, 90, 20, Component.literal(NAMES[i] + " color"));
            field.setMaxLength(7);
            field.setValue(CropReadinessConfig.hex(CropReadinessConfig.color(i)));
            fields[i] = addRenderableWidget(field);
        }
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(width / 2 - 154, buttonY, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Reset defaults"), button -> reset())
                .bounds(width / 2 - 50, buttonY, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(width / 2 + 54, buttonY, 100, 20)
                .build());
    }

    private void save() {
        int[] values = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            int parsed = CropReadinessConfig.parse(fields[i].getValue(), -1);
            if (parsed < 0) {
                status = "Use six-digit hex colors, for example #59D66F";
                return;
            }
            values[i] = parsed;
        }
        try {
            CropReadinessConfig.save(values);
            minecraft.setScreenAndShow(parent);
        } catch (IOException exception) {
            CropReadinessNeoForge.LOGGER.error("Could not save Crop Helper configuration", exception);
            status = "Could not save the configuration file";
        }
    }

    private void reset() {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setValue(CropReadinessConfig.hex(CropReadinessConfig.defaultColor(i)));
        }
        status = "Defaults restored; press Save to apply";
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(font, "Enter colors as #RRGGBB", width / 2, 34, 0xFFAAAAAA);
        int left = width / 2 - 170;
        for (int i = 0; i < fields.length; i++) {
            int y = 52 + i * 34;
            int rgb = CropReadinessConfig.parse(fields[i].getValue(), CropReadinessConfig.defaultColor(i));
            graphics.fill(left, y + 2, left + 16, y + 18, 0xFF000000 | rgb);
            graphics.text(font, NAMES[i], left + 24, y, 0xFFFFFFFF);
            graphics.text(font, DETAILS[i], left + 24, y + 11, 0xFFAAAAAA);
        }
        if (!status.isEmpty()) {
            graphics.centeredText(font, status, width / 2, buttonY - 14, 0xFFFFAA55);
        }
    }
}
