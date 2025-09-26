package org.pirozhok.codecraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pirozhok.codecraft.client.gui.widget.MultiLineTextArea;
import org.pirozhok.codecraft.scripting.ScriptManager;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class ScriptEditorScreen extends Screen {
    private MultiLineTextArea codeEditor;
    private Button saveButton;
    private Button runButton;
    private Button stopButton;
    private Button deleteButton;
    private String currentScriptName;

    public ScriptEditorScreen() {
        super(Component.translatable("gui.codecraft.script_editor"));
        this.currentScriptName = null;
    }

    public ScriptEditorScreen(String scriptName) {
        super(Component.translatable("gui.codecraft.script_editor"));
        this.currentScriptName = scriptName;
    }

    @Override
    protected void init() {
        super.init();

        // Code editor
        this.codeEditor = new MultiLineTextArea(this.font, 10, 50, this.width - 20, this.height - 100,
                Component.translatable("gui.codecraft.code_editor"));

        if (currentScriptName != null) {
            try {
                String content = ScriptManager.getInstance().readScript(currentScriptName);
                this.codeEditor.setValue(content);
            } catch (IOException e) {
                this.codeEditor.setValue("// Ошибка чтения скрипта: " + e.getMessage());
            }
        } else {
            // Use safe template for new script
            this.codeEditor.setValue(Component.translatable("gui.codecraft.script_template").getString());
        }
        this.addRenderableWidget(this.codeEditor);

        // Buttons
        int buttonY = this.height - 40;
        int buttonWidth = 80;

        this.saveButton = Button.builder(Component.translatable("gui.codecraft.button.save"), this::onSave)
                .pos(10, buttonY)
                .size(buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.saveButton);

        this.runButton = Button.builder(Component.translatable("gui.codecraft.button.run"), this::onRun)
                .pos(100, buttonY)
                .size(buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.runButton);

        this.stopButton = Button.builder(Component.translatable("gui.codecraft.button.stop"), this::onStop)
                .pos(190, buttonY)
                .size(buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.stopButton);

        this.deleteButton = Button.builder(Component.translatable("gui.codecraft.button.delete"), this::onDelete)
                .pos(280, buttonY)
                .size(buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.deleteButton);

        // Close button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.codecraft.button.close"), button -> this.onClose())
                .pos(this.width - 90, buttonY)
                .size(80, 20)
                .build());

        updateButtonStates();

        // Set focus to code editor
        this.setInitialFocus(this.codeEditor);
    }

    private void onSave(Button button) {
        if (currentScriptName == null) {
            // For new script, show name input screen
            this.minecraft.setScreen(new ScriptNameScreen(this, codeEditor.getValue()));
        } else {
            saveScript(currentScriptName);
        }
    }

    public void saveScript(String name) {
        try {
            ScriptManager.getInstance().createScript(name, codeEditor.getValue());
            this.currentScriptName = name;
            updateButtonStates();
        } catch (IOException e) {
            // Handle error
        }
    }

    private void onRun(Button button) {
        if (currentScriptName != null) {
            ScriptManager.getInstance().executeScript(currentScriptName);
            updateButtonStates();
        }
    }

    private void onStop(Button button) {
        if (currentScriptName != null) {
            ScriptManager.getInstance().stopScript(currentScriptName);
            updateButtonStates();
        }
    }

    private void onDelete(Button button) {
        if (currentScriptName != null) {
            try {
                ScriptManager.getInstance().deleteScript(currentScriptName);
                this.onClose();
            } catch (IOException e) {
                // Handle error
            }
        }
    }

    private void updateButtonStates() {
        boolean hasScript = currentScriptName != null;
        this.saveButton.setMessage(Component.translatable(
                hasScript ? "gui.codecraft.button.save" : "gui.codecraft.button.save_new"));
        this.runButton.active = hasScript && !ScriptManager.getInstance().isScriptRunning(currentScriptName);
        this.stopButton.active = hasScript && ScriptManager.getInstance().isScriptRunning(currentScriptName);
        this.deleteButton.active = hasScript;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        String status = "";
        if (currentScriptName != null) {
            status = ScriptManager.getInstance().isScriptRunning(currentScriptName) ?
                    Component.translatable("gui.codecraft.status.running").getString() :
                    Component.translatable("gui.codecraft.status.stopped").getString();
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.codecraft.current_script", currentScriptName, status),
                    10, 35, 0xFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.codecraft.new_script"),
                    10, 35, 0xFFFFFF, false);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle Ctrl+S for saving
        if (keyCode == InputConstants.KEY_S && (modifiers & InputConstants.MOD_CONTROL) != 0) {
            if (currentScriptName != null) {
                saveScript(currentScriptName);
            } else {
                this.minecraft.setScreen(new ScriptNameScreen(this, codeEditor.getValue()));
            }
            return true;
        }

        // Handle ESC for closing
        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ScriptListScreen());
    }

    // Method to set script name after saving
    public void setScriptName(String name) {
        this.currentScriptName = name;
        updateButtonStates();
    }
}