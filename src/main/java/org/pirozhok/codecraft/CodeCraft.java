package org.pirozhok.codecraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pirozhok.codecraft.commands.ScriptCommand;
import org.pirozhok.codecraft.scripting.ScriptManager;
import org.slf4j.Logger;

@Mod(CodeCraft.MODID)
public class CodeCraft
{
    public static final String MODID = "codecraft";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CodeCraft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем клиентскую настройку
        modEventBus.addListener(this::clientSetup);

        // Регистрируем этот класс для событий Forge
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("CodeCraft: Mod constructor called");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        System.out.println("CodeCraft: Client setup completed");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        System.out.println("CodeCraft: Server starting event received");
        ScriptManager.getInstance().setServer(event.getServer());
        System.out.println("CodeCraft: Server instance set to ScriptManager");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        System.out.println("CodeCraft: Server stopping event received");
        ScriptManager.getInstance().stopAllScripts();
        System.out.println("CodeCraft: All scripts stopped");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        System.out.println("CodeCraft: Registering commands");
        ScriptCommand.register(event.getDispatcher());
        System.out.println("CodeCraft: Script commands registered");
    }
}
