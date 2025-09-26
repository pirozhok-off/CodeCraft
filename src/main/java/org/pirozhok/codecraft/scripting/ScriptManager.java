package org.pirozhok.codecraft.scripting;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.script.Bindings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ScriptManager {
    private static ScriptManager instance;
    private MinecraftServer server;
    private final Map<String, ScriptExecutionContext> runningScripts = new ConcurrentHashMap<>();
    private final Map<String, Thread> scriptThreads = new ConcurrentHashMap<>();

    public static ScriptManager getInstance() {
        if (instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        System.out.println("ScriptManager: Server set to " + (server != null ? server.toString() : "null"));
    }

    public MinecraftServer getServer() {
        // Важно: в одиночной игре сервер может быть доступен через ServerLifecycleHooks
        if (server != null) {
            return server;
        }

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            System.out.println("ScriptManager: Using server from ServerLifecycleHooks");
            return currentServer;
        }

        System.err.println("ScriptManager: No server available!");
        return null;
    }

    public Path getScriptsDir() throws IOException {
        MinecraftServer currentServer = getServer();
        if (currentServer == null) {
            throw new IllegalStateException("Server not initialized");
        }

        // Для одиночной игры используем путь к миру
        Path worldPath = currentServer.getWorldPath(new LevelResource("scripts"));
        Files.createDirectories(worldPath);
        System.out.println("ScriptManager: Scripts directory: " + worldPath);
        return worldPath;
    }

    public boolean scriptExists(String name) {
        try {
            Path scriptPath = getScriptsDir().resolve(name + ".js");
            boolean exists = Files.exists(scriptPath);
            System.out.println("ScriptManager: Script " + name + " exists: " + exists);
            return exists;
        } catch (Exception e) {
            System.err.println("ScriptManager: Error checking script existence: " + e.getMessage());
            return false;
        }
    }

    public void createScript(String name, String content) throws IOException {
        Path scriptPath = getScriptsDir().resolve(name + ".js");
        Files.writeString(scriptPath, content);
        System.out.println("ScriptManager: Script created: " + name);
    }

    public String readScript(String name) throws IOException {
        Path scriptPath = getScriptsDir().resolve(name + ".js");
        String content = Files.readString(scriptPath);
        System.out.println("ScriptManager: Script read: " + name + " (" + content.length() + " chars)");
        return content;
    }

    public void deleteScript(String name) throws IOException {
        Path scriptPath = getScriptsDir().resolve(name + ".js");
        Files.deleteIfExists(scriptPath);
        stopScript(name);
        System.out.println("ScriptManager: Script deleted: " + name);
    }

    public boolean executeScript(String name) {
        System.out.println("ScriptManager: Attempting to execute script: " + name);

        if (runningScripts.containsKey(name)) {
            System.out.println("ScriptManager: Script already running: " + name);
            return false;
        }

        // Проверяем доступность сервера
        MinecraftServer currentServer = getServer();
        if (currentServer == null) {
            System.err.println("ScriptManager: Cannot execute script - no server available");
            return false;
        }

        try {
            String scriptContent = readScript(name);

            ScriptExecutionContext context = new ScriptExecutionContext(name, scriptContent, currentServer);
            runningScripts.put(name, context);

            Thread scriptThread = new Thread(() -> {
                try {
                    System.out.println("ScriptManager: Starting script execution thread for: " + name);
                    context.execute();
                    System.out.println("ScriptManager: Script finished: " + name);
                } catch (Exception e) {
                    System.err.println("ScriptManager: Error executing script '" + name + "': " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    runningScripts.remove(name);
                    scriptThreads.remove(name);
                    System.out.println("ScriptManager: Script cleanup completed: " + name);
                }
            });

            scriptThread.setName("Script-" + name);
            scriptThreads.put(name, scriptThread);
            scriptThread.start();

            System.out.println("ScriptManager: Script execution started: " + name);
            return true;
        } catch (Exception e) {
            System.err.println("ScriptManager: Failed to execute script '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopScript(String name) {
        System.out.println("ScriptManager: Attempting to stop script: " + name);

        ScriptExecutionContext context = runningScripts.get(name);
        Thread thread = scriptThreads.get(name);

        boolean stopped = false;

        if (context != null) {
            context.stop();
            runningScripts.remove(name);
            stopped = true;
            System.out.println("ScriptManager: Script context stopped: " + name);
        }

        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            scriptThreads.remove(name);
            stopped = true;
            System.out.println("ScriptManager: Script thread interrupted: " + name);
        }

        if (!stopped) {
            System.out.println("ScriptManager: Script was not running: " + name);
        }

        return stopped;
    }

    public boolean isScriptRunning(String name) {
        boolean running = runningScripts.containsKey(name);
        System.out.println("ScriptManager: Script " + name + " running: " + running);
        return running;
    }

    public void stopAllScripts() {
        System.out.println("ScriptManager: Stopping all scripts");
        for (String scriptName : runningScripts.keySet()) {
            stopScript(scriptName);
        }
    }

    public Map<String, String> listScripts() {
        Map<String, String> scripts = new HashMap<>();
        try {
            Path scriptsDir = getScriptsDir();
            if (Files.exists(scriptsDir)) {
                Files.list(scriptsDir)
                        .filter(path -> path.toString().endsWith(".js"))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String name = fileName.substring(0, fileName.length() - 3);
                            scripts.put(name, fileName);
                        });
            }
            System.out.println("ScriptManager: Found " + scripts.size() + " scripts");
        } catch (IOException e) {
            System.err.println("ScriptManager: Error listing scripts: " + e.getMessage());
        }
        return scripts;
    }

    public static boolean executeCommandSafe(MinecraftServer server, String command) {
        MinecraftServer currentServer = server != null ? server : ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            System.err.println("ScriptManager: No server available for command execution");
            return false;
        }

        System.out.println("ScriptManager: Executing command: " + command);

        try {
            AtomicReference<Boolean> success = new AtomicReference<>(false);
            AtomicReference<Exception> exception = new AtomicReference<>(null);

            // Execute command on the main server thread
            currentServer.execute(() -> {
                try {
                    int result = currentServer.getCommands().performPrefixedCommand(
                            currentServer.createCommandSourceStack().withSuppressedOutput(),
                            command
                    );
                    success.set(result > 0);
                    System.out.println("ScriptManager: Command executed successfully: " + command + " Result: " + result);
                } catch (Exception e) {
                    System.err.println("ScriptManager: Error executing command: " + command + " - " + e.getMessage());
                    exception.set(e);
                    success.set(false);
                }
            });

            // Wait a bit for command execution
            Thread.sleep(100);
            return success.get();
        } catch (Exception e) {
            System.err.println("ScriptManager: Exception in executeCommandSafe: " + e.getMessage());
            return false;
        }
    }
}