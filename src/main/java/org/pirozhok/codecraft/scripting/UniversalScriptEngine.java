package org.pirozhok.codecraft.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

public class UniversalScriptEngine {
    private ScriptEngine engine;
    private String engineName;

    public UniversalScriptEngine() {
        this.engine = createEngine();
    }

    private ScriptEngine createEngine() {
        List<String> enginePreferences = Arrays.asList("graal.js", "js", "JavaScript", "nashorn");

        for (String engineName : enginePreferences) {
            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
                if (engine != null) {
                    this.engineName = engineName;
                    System.out.println("UniversalScriptEngine: Using script engine: " + engineName);
                    return engine;
                }
            } catch (Exception e) {
                System.err.println("UniversalScriptEngine: Failed to initialize engine " + engineName + ": " + e.getMessage());
            }
        }

        // Fallback: try to load GraalVM engine manually
        try {
            System.out.println("UniversalScriptEngine: Trying to load GraalVM engine manually...");
            ScriptEngine graalEngine = new ScriptEngineManager().getEngineByName("graal.js");
            if (graalEngine != null) {
                this.engineName = "graal.js";
                System.out.println("UniversalScriptEngine: GraalVM engine loaded successfully");
                return graalEngine;
            }
        } catch (Exception e) {
            System.err.println("UniversalScriptEngine: Failed to load GraalVM engine: " + e.getMessage());
        }

        throw new RuntimeException("No suitable JavaScript engine found. Please install GraalVM or use Java with Nashorn.");
    }

    public Object eval(String script) throws ScriptException {
        return engine.eval(script);
    }

    public Object eval(String script, javax.script.Bindings bindings) throws ScriptException {
        return engine.eval(script, bindings);
    }

    public void put(String key, Object value) {
        engine.put(key, value);
    }

    public javax.script.Bindings createBindings() {
        return engine.createBindings();
    }

    public String getEngineName() {
        return engineName;
    }
}