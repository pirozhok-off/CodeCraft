package org.pirozhok.codecraft.client.gui;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pirozhok.codecraft.scripting.ScriptManager;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ScriptListScreen extends Screen {
    private Map<String, String> scripts;

    public ScriptListScreen() {
        super(Component.translatable("gui.codecraft.script_list"));
    }

    @Override
    protected void init() {
        super.init();

        // Load script names
        scripts = ScriptManager.getInstance().listScripts();

        // Script buttons
        int y = 40;
        for (String name : scripts.keySet()) {
            final String scriptName = name;

            // Script name button
            this.addRenderableWidget(Button.builder(Component.literal(name), button -> openScript(scriptName))
                    .pos(20, y)
                    .size(150, 20)
                    .build());

            // Run button
            this.addRenderableWidget(Button.builder(Component.translatable("gui.codecraft.button.run"), button -> runScript(scriptName))
                    .pos(180, y)
                    .size(60, 20)
                    .build());

            // Stop button
            Button stopBtn = Button.builder(Component.translatable("gui.codecraft.button.stop"), button -> stopScript(scriptName))
                    .pos(250, y)
                    .size(60, 20)
                    .build();
            stopBtn.active = ScriptManager.getInstance().isScriptRunning(scriptName);
            this.addRenderableWidget(stopBtn);

            y += 25;

            if (y > this.height - 60) break; // Prevent overflow
        }

        // New script button
        if (scripts.isEmpty()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.codecraft.button.new_script"), button -> newScript())
                    .pos(this.width / 2 - 50, this.height / 2)
                    .size(100, 20)
                    .build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.codecraft.button.new_script"), button -> newScript())
                    .pos(20, y + 10)
                    .size(100, 20)
                    .build());
        }

        // Close button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.codecraft.button.close"), button -> this.onClose())
                .pos(this.width - 80, this.height - 30)
                .size(60, 20)
                .build());
    }

    private void openScript(String name) {
        this.minecraft.setScreen(new ScriptEditorScreen(name));
    }

    private void runScript(String name) {
        ScriptManager.getInstance().executeScript(name);
        this.minecraft.setScreen(new ScriptListScreen()); // Refresh
    }

    private void stopScript(String name) {
        ScriptManager.getInstance().stopScript(name);
        this.minecraft.setScreen(new ScriptListScreen()); // Refresh
    }

    private void newScript() {
        this.minecraft.setScreen(new ScriptEditorScreen());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        if (scripts.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.codecraft.no_scripts"),
                    this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}