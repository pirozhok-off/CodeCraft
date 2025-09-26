package org.pirozhok.codecraft.scripting;

import net.minecraft.server.MinecraftServer;

import javax.script.Bindings;
import javax.script.ScriptException;

public class ScriptExecutionContext {
    private final String scriptName;
    private final String scriptContent;
    private final MinecraftServer server;
    private volatile boolean running = false;
    private UniversalScriptEngine scriptEngine;

    public ScriptExecutionContext(String scriptName, String scriptContent, MinecraftServer server) {
        this.scriptName = scriptName;
        this.scriptContent = scriptContent;
        this.server = server;
    }

    public void execute() throws ScriptException {
        running = true;
        System.out.println("ScriptExecutionContext: Starting execution of: " + scriptName);

        try {
            scriptEngine = new UniversalScriptEngine();
            System.out.println("ScriptExecutionContext: Engine created: " + scriptEngine.getEngineName());

            // Create Mappet-like API
            Bindings bindings = scriptEngine.createBindings();

            // Global objects
            bindings.put("server", server);
            bindings.put("global", new GlobalAPI(server, scriptName));
            bindings.put("console", new ScriptConsole(scriptName));

            // Mappet-style context object
            MappetContext mappetContext = new MappetContext(server, scriptName);
            bindings.put("c", mappetContext);
            bindings.put("context", mappetContext);

            // Wrap script for better error handling
            String wrappedScript = wrapScript(scriptContent);
            System.out.println("ScriptExecutionContext: Executing wrapped script");

            // Execute script
            scriptEngine.eval(wrappedScript, bindings);

            System.out.println("ScriptExecutionContext: Script executed successfully: " + scriptName);

        } catch (Exception e) {
            System.err.println("ScriptExecutionContext: Error during script execution '" + scriptName + "': " + e.getMessage());
            e.printStackTrace();
            // Отправляем сообщение об ошибке в чат, если сервер доступен
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(
                        net.minecraft.network.chat.Component.literal("Ошибка скрипта " + scriptName + ": " + e.getMessage()),
                        false
                );
            }
        } finally {
            running = false;
            System.out.println("ScriptExecutionContext: Execution finished: " + scriptName);
        }
    }

    private String wrapScript(String script) {
        // Wrap in try-catch for better error handling and ensure main function is called
        return "(function() {\n" +
                "try {\n" +
                "   " + script + "\n" +
                "   // Automatically call main function if it exists\n" +
                "   if (typeof main === 'function') {\n" +
                "       main(c);\n" +
                "   } else if (typeof handler === 'function') {\n" +
                "       handler(context);\n" +
                "   } else if (typeof init === 'function') {\n" +
                "       init(c);\n" +
                "   }\n" +
                "} catch (e) {\n" +
                "   console.error('Script error: ' + e);\n" +
                "   if (typeof context !== 'undefined') context.sendMessage('Ошибка скрипта: ' + e);\n" +
                "}\n" +
                "})();";
    }

    public void stop() {
        running = false;
        System.out.println("ScriptExecutionContext: Stop requested for: " + scriptName);
    }

    public boolean isRunning() {
        return running;
    }
}