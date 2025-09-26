package org.pirozhok.codecraft.scripting;

import net.minecraft.server.MinecraftServer;

public class GlobalAPI {
    private final MinecraftServer server;
    private final String scriptName;

    public GlobalAPI(MinecraftServer server, String scriptName) {
        this.server = server;
        this.scriptName = scriptName;
    }

    public void print(Object obj) {
        System.out.println("[" + scriptName + "] " + String.valueOf(obj));
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}