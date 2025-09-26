package org.pirozhok.codecraft.scripting;

public class ScriptConsole {
    private final String scriptName;

    public ScriptConsole(String scriptName) {
        this.scriptName = scriptName;
    }

    public void log(String message) {
        System.out.println("[" + scriptName + "] " + message);
    }

    public void error(String message) {
        System.err.println("[" + scriptName + "] ERROR: " + message);
    }

    public void warn(String message) {
        System.out.println("[" + scriptName + "] WARN: " + message);
    }
}