package org.pirozhok.codecraft.client.gui;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScriptNameScreen extends Screen {
    private final Screen parentScreen;
    private final String scriptContent;
    private EditBox nameField;
    private Button saveButton;

    public ScriptNameScreen(Screen parentScreen, String scriptContent) {
        super(Component.translatable("gui.codecraft.script_name_title"));
        this.parentScreen = parentScreen;
        this.scriptContent = scriptContent;
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20,
                Component.translatable("gui.codecraft.script_name"));
        this.nameField.setMaxLength(50);
        this.nameField.setValue("new_script");
        this.addRenderableWidget(this.nameField);

        this.saveButton = Button.builder(Component.translatable("gui.codecraft.button.save"), this::onSave)
                .pos(this.width / 2 - 50, this.height / 2 + 20)
                .size(100, 20)
                .build();
        this.addRenderableWidget(this.saveButton);

        this.setInitialFocus(this.nameField);
    }

    private void onSave(Button button) {
        String name = nameField.getValue().trim();
        if (!name.isEmpty()) {
            if (parentScreen instanceof ScriptEditorScreen) {
                ((ScriptEditorScreen) parentScreen).setScriptName(name);
                ((ScriptEditorScreen) parentScreen).saveScript(name);
            }
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }
}