package org.pirozhok.codecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
            "key.codecraft.open_editor",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL,
            "key.categories.codecraft"
    );
}