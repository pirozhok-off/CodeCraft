package org.pirozhok.codecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.pirozhok.codecraft.CodeCraft;
import org.pirozhok.codecraft.client.gui.ScriptEditorScreen;

@Mod.EventBusSubscriber(modid = CodeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (KeyBindings.OPEN_EDITOR.consumeClick()) {
                Minecraft.getInstance().setScreen(new ScriptEditorScreen());
            }
        }
    }
}

@Mod.EventBusSubscriber(modid = CodeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class KeyBindingRegistry {

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_EDITOR);
    }
}