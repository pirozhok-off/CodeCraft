package org.pirozhok.codecraft.scripting;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class MappetContext {
    private final MinecraftServer server;
    private final String scriptName;

    public MappetContext(MinecraftServer server, String scriptName) {
        this.server = server;
        this.scriptName = scriptName;
        System.out.println("MappetContext: Created for script: " + scriptName + ", server: " + (server != null ? "available" : "null"));
    }

    public MinecraftServer getSubject() {
        return server;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public boolean executeCommand(String command) {
        System.out.println("MappetContext: executeCommand: " + command);
        boolean result = ScriptManager.executeCommandSafe(server, command);
        System.out.println("MappetContext: Command result: " + result);
        return result;
    }

    public boolean exec(String command) {
        return executeCommand(command);
    }

    // Mappet-style command execution
    public boolean command(String command) {
        return executeCommand(command);
    }

    public void sendMessage(String message) {
        System.out.println("MappetContext: sendMessage: " + message);
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        } else {
            System.err.println("MappetContext: Cannot send message - server is null");
        }
    }

    public void tellraw(String jsonMessage) {
        System.out.println("MappetContext: tellraw: " + jsonMessage);
        executeCommand("tellraw @a " + jsonMessage);
    }

    public void tellrawText(String text) {
        tellraw("{\"text\":\"" + escapeJson(text) + "\"}");
    }

    public void tellrawGold(String text) {
        tellraw("{\"text\":\"" + escapeJson(text) + "\",\"color\":\"gold\"}");
    }

    public void tellrawRed(String text) {
        tellraw("{\"text\":\"" + escapeJson(text) + "\",\"color\":\"red\"}");
    }

    public void tellrawGreen(String text) {
        tellraw("{\"text\":\"" + escapeJson(text) + "\",\"color\":\"green\"}");
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Additional Mappet-like methods
    public void log(String message) {
        System.out.println("[" + scriptName + "] " + message);
    }

    public void error(String message) {
        System.err.println("[" + scriptName + "] ERROR: " + message);
    }
}