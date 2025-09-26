package org.pirozhok.codecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.pirozhok.codecraft.scripting.ScriptManager;

public class ScriptCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("script")
                .then(Commands.literal("list")
                        .executes(ScriptCommand::listScripts)
                )
                .then(Commands.literal("exec")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> executeScript(context, StringArgumentType.getString(context, "name")))
                        )
                )
                .then(Commands.literal("stop")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> stopScript(context, StringArgumentType.getString(context, "name")))
                        )
                )
                .then(Commands.literal("reload")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> reloadScript(context, StringArgumentType.getString(context, "name")))
                        )
                )
        );
    }

    private static int listScripts(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ScriptManager manager = ScriptManager.getInstance();

        var scripts = manager.listScripts();
        if (scripts.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("gui.codecraft.no_scripts"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("gui.codecraft.script_list"), false);
            scripts.keySet().forEach(name -> {
                String status = manager.isScriptRunning(name) ?
                        Component.translatable("gui.codecraft.status.running").getString() :
                        Component.translatable("gui.codecraft.status.stopped").getString();
                source.sendSuccess(() -> Component.literal("- " + name + " " + status), false);
            });
        }
        return scripts.size();
    }

    private static int executeScript(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        ScriptManager manager = ScriptManager.getInstance();

        if (!manager.scriptExists(name)) {
            source.sendFailure(Component.translatable("message.codecraft.script.not_found", name));
            return 0;
        }

        if (manager.isScriptRunning(name)) {
            source.sendFailure(Component.translatable("message.codecraft.script.already_running", name));
            return 0;
        }

        if (manager.executeScript(name)) {
            source.sendSuccess(() -> Component.translatable("message.codecraft.script.started", name), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("message.codecraft.script.failed_start", name));
            return 0;
        }
    }

    private static int stopScript(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        ScriptManager manager = ScriptManager.getInstance();

        if (manager.stopScript(name)) {
            source.sendSuccess(() -> Component.translatable("message.codecraft.script.stopped", name), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("message.codecraft.script.not_running", name));
            return 0;
        }
    }

    private static int reloadScript(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        ScriptManager manager = ScriptManager.getInstance();

        if (!manager.scriptExists(name)) {
            source.sendFailure(Component.translatable("message.codecraft.script.not_found", name));
            return 0;
        }

        boolean wasRunning = manager.isScriptRunning(name);
        manager.stopScript(name);

        if (manager.executeScript(name)) {
            source.sendSuccess(() -> Component.translatable("message.codecraft.script.reloaded", name), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("message.codecraft.script.failed_reload", name));
            // Попытаться перезапустить, если скрипт был запущен
            if (wasRunning) {
                manager.executeScript(name);
            }
            return 0;
        }
    }
}