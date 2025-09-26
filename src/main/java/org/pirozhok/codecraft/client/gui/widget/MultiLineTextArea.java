package org.pirozhok.codecraft.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MultiLineTextArea extends AbstractWidget {
    private final Font font;
    private String text = "";
    private int scrollOffset = 0;
    private int cursorLine = 0;
    private int cursorPos = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0;
    private boolean isDragging = false;

    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 4;
    private static final int CURSOR_BLINK_INTERVAL = 300;
    private long lastCursorBlink = 0;
    private boolean cursorVisible = true;

    // Patterns for syntax highlighting
    private static final Pattern COMMENT_PATTERN = Pattern.compile("//.*");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(function|var|let|const|if|else|for|while|return|true|false|null)\\b");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\b(main|handler|init)\\b");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"[^\"]*\"|'[^']*'");

    public MultiLineTextArea(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.font = font;
    }

    public void setValue(String text) {
        this.text = text != null ? text : "";
        this.cursorLine = 0;
        this.cursorPos = 0;
        this.scrollOffset = 0;
        this.selectionStart = -1;
        this.selectionEnd = -1;
    }

    public String getValue() {
        return text;
    }

    private List<String> getLines() {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add("");
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
        }
        lines.add(currentLine.toString());

        return lines;
    }

    private void setTextFromLines(List<String> lines) {
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) newText.append('\n');
            newText.append(lines.get(i));
        }
        this.text = newText.toString();
    }

    private int getVisibleLines() {
        return Math.max(1, (height - PADDING * 2) / LINE_HEIGHT);
    }

    private void ensureCursorVisible() {
        List<String> lines = getLines();

        // Adjust scroll to make cursor line visible
        if (cursorLine < scrollOffset) {
            scrollOffset = cursorLine;
        } else if (cursorLine >= scrollOffset + getVisibleLines()) {
            scrollOffset = cursorLine - getVisibleLines() + 1;
        }

        scrollOffset = Mth.clamp(scrollOffset, 0, Math.max(0, lines.size() - getVisibleLines()));
    }

    private int getLineAtPos(double mouseY) {
        int line = scrollOffset + (int)((mouseY - getY() - PADDING) / LINE_HEIGHT);
        List<String> lines = getLines();
        return Mth.clamp(line, 0, Math.max(0, lines.size() - 1));
    }

    private int getPosInLine(int line, double mouseX) {
        List<String> lines = getLines();
        if (line < 0 || line >= lines.size()) return 0;

        String lineText = lines.get(line);
        int pos = 0;
        float currentWidth = 0;
        float targetWidth = (float)(mouseX - getX() - PADDING);

        for (int i = 0; i < lineText.length(); i++) {
            float charWidth = font.width(String.valueOf(lineText.charAt(i)));
            if (currentWidth + charWidth / 2 > targetWidth) {
                break;
            }
            currentWidth += charWidth;
            pos++;
        }

        return Math.min(pos, lineText.length());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF202020);
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0xFF000000);

        // Update cursor blink
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCursorBlink > CURSOR_BLINK_INTERVAL) {
            cursorVisible = !cursorVisible;
            lastCursorBlink = currentTime;
        }

        List<String> lines = getLines();
        int visibleLines = getVisibleLines();

        // Render text with syntax highlighting
        int textY = getY() + PADDING;
        for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + visibleLines); i++) {
            renderSyntaxHighlightedLine(guiGraphics, lines.get(i), getX() + PADDING, textY);
            textY += LINE_HEIGHT;
        }

        // Render cursor
        if (isFocused() && cursorVisible && cursorLine >= scrollOffset && cursorLine < scrollOffset + visibleLines) {
            int cursorY = getY() + PADDING + (cursorLine - scrollOffset) * LINE_HEIGHT;
            String currentLine = lines.get(cursorLine);
            String beforeCursor = currentLine.substring(0, Math.min(cursorPos, currentLine.length()));
            int cursorX = getX() + PADDING + font.width(beforeCursor);

            guiGraphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
        }

        // Render scroll bar if needed
        if (lines.size() > visibleLines) {
            int scrollBarHeight = Math.max(10, (int)((float)visibleLines / lines.size() * height));
            int scrollBarY = getY() + (int)((float)scrollOffset / lines.size() * height);
            guiGraphics.fill(getX() + width - 5, scrollBarY, getX() + width - 2, scrollBarY + scrollBarHeight, 0xFF666666);
        }
    }

    private void renderSyntaxHighlightedLine(GuiGraphics guiGraphics, String line, int x, int y) {
        if (line.isEmpty()) return;

        // Split line into tokens for syntax highlighting
        List<TextToken> tokens = parseSyntaxTokens(line);

        int currentX = x;
        for (TextToken token : tokens) {
            int color = getTokenColor(token.type);
            guiGraphics.drawString(font, token.text, currentX, y, color, false);
            currentX += font.width(token.text);
        }
    }

    private List<TextToken> parseSyntaxTokens(String line) {
        List<TextToken> tokens = new ArrayList<>();
        if (line.isEmpty()) {
            tokens.add(new TextToken("", TokenType.NORMAL));
            return tokens;
        }

        StringBuilder currentToken = new StringBuilder();
        TokenType currentType = TokenType.NORMAL;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            TokenType charType = getCharType(line, i);

            if (currentType != charType && currentToken.length() > 0) {
                tokens.add(new TextToken(currentToken.toString(), currentType));
                currentToken.setLength(0);
            }

            currentType = charType;
            currentToken.append(c);
        }

        if (currentToken.length() > 0) {
            tokens.add(new TextToken(currentToken.toString(), currentType));
        }

        return tokens;
    }

    private TokenType getCharType(String line, int position) {
        // Check for comments
        if (position >= 1 && line.charAt(position-1) == '/' && line.charAt(position) == '/') {
            return TokenType.COMMENT;
        }
        if (position > 0) {
            // Check if we're inside a comment
            for (int i = 0; i < position; i++) {
                if (i < position - 1 && line.charAt(i) == '/' && line.charAt(i+1) == '/') {
                    return TokenType.COMMENT;
                }
            }
        }

        // Check for strings
        boolean inString = false;
        char stringChar = '"';
        for (int i = 0; i <= position; i++) {
            if (i > 0 && line.charAt(i-1) != '\\') {
                if (line.charAt(i) == '"' || line.charAt(i) == '\'') {
                    if (!inString) {
                        inString = true;
                        stringChar = line.charAt(i);
                    } else if (line.charAt(i) == stringChar) {
                        inString = false;
                    }
                }
            }
        }
        if (inString) return TokenType.STRING;

        // Check for keywords (simplified)
        if (position == 0 || !Character.isLetterOrDigit(line.charAt(position-1))) {
            String remaining = line.substring(position);
            if (remaining.startsWith("function") && isWordBoundary(remaining, "function")) {
                return TokenType.KEYWORD;
            }
            if (remaining.startsWith("var") && isWordBoundary(remaining, "var")) {
                return TokenType.KEYWORD;
            }
            if (remaining.startsWith("main") && isWordBoundary(remaining, "main")) {
                return TokenType.FUNCTION;
            }
            if (remaining.startsWith("handler") && isWordBoundary(remaining, "handler")) {
                return TokenType.FUNCTION;
            }
        }

        return TokenType.NORMAL;
    }

    private boolean isWordBoundary(String text, String word) {
        if (text.length() > word.length()) {
            char nextChar = text.charAt(word.length());
            return !Character.isLetterOrDigit(nextChar);
        }
        return true;
    }

    private int getTokenColor(TokenType type) {
        switch (type) {
            case COMMENT:
                return 0xFF808080; // Gray
            case KEYWORD:
                return 0xFF569CD6; // Light blue
            case FUNCTION:
                return 0xFFDCDCAA; // Yellow
            case STRING:
                return 0xFFCE9178; // Orange
            case NORMAL:
            default:
                return 0xFFFFFFFF; // White
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;

        setFocused(true);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 250) {
            // Double click - select word
            selectWordAt(mouseX, mouseY);
        } else {
            // Single click - move cursor
            cursorLine = getLineAtPos(mouseY);
            List<String> lines = getLines();
            if (cursorLine < lines.size()) {
                cursorPos = getPosInLine(cursorLine, mouseX);
            } else {
                cursorPos = 0;
            }
            selectionStart = -1;
            selectionEnd = -1;
        }

        lastClickTime = currentTime;
        isDragging = true;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (isDragging) {
            int newLine = getLineAtPos(mouseY);
            int newPos = getPosInLine(newLine, mouseX);

            if (selectionStart == -1) {
                selectionStart = getCursorPosition();
            }

            cursorLine = newLine;
            cursorPos = newPos;
            selectionEnd = getCursorPosition();
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        isDragging = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;

        List<String> lines = getLines();
        if (lines.isEmpty()) {
            lines.add("");
        }

        // Ensure cursor is within valid bounds
        cursorLine = Mth.clamp(cursorLine, 0, lines.size() - 1);
        String currentLine = lines.get(cursorLine);
        cursorPos = Mth.clamp(cursorPos, 0, currentLine.length());

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
                // Insert new line
                String beforeCursor = currentLine.substring(0, cursorPos);
                String afterCursor = currentLine.substring(cursorPos);

                lines.set(cursorLine, beforeCursor);
                lines.add(cursorLine + 1, afterCursor);
                setTextFromLines(lines);

                cursorLine++;
                cursorPos = 0;
                break;

            case GLFW.GLFW_KEY_BACKSPACE:
                if (cursorPos > 0) {
                    // Delete character before cursor
                    String newLine = currentLine.substring(0, cursorPos - 1) + currentLine.substring(cursorPos);
                    lines.set(cursorLine, newLine);
                    setTextFromLines(lines);
                    cursorPos--;
                } else if (cursorLine > 0) {
                    // Merge with previous line
                    String prevLine = lines.get(cursorLine - 1);
                    String mergedLine = prevLine + currentLine;
                    lines.set(cursorLine - 1, mergedLine);
                    lines.remove(cursorLine);
                    setTextFromLines(lines);

                    cursorLine--;
                    cursorPos = prevLine.length();
                }
                break;

            case GLFW.GLFW_KEY_DELETE:
                if (cursorPos < currentLine.length()) {
                    // Delete character after cursor
                    String newLine = currentLine.substring(0, cursorPos) + currentLine.substring(cursorPos + 1);
                    lines.set(cursorLine, newLine);
                    setTextFromLines(lines);
                } else if (cursorLine < lines.size() - 1) {
                    // Merge with next line
                    String nextLine = lines.get(cursorLine + 1);
                    String mergedLine = currentLine + nextLine;
                    lines.set(cursorLine, mergedLine);
                    lines.remove(cursorLine + 1);
                    setTextFromLines(lines);
                }
                break;

            case GLFW.GLFW_KEY_UP:
                if (cursorLine > 0) {
                    cursorLine--;
                    String newLine = lines.get(cursorLine);
                    cursorPos = Math.min(cursorPos, newLine.length());
                }
                break;

            case GLFW.GLFW_KEY_DOWN:
                if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    String newLine = lines.get(cursorLine);
                    cursorPos = Math.min(cursorPos, newLine.length());
                }
                break;

            case GLFW.GLFW_KEY_LEFT:
                if (cursorPos > 0) {
                    cursorPos--;
                } else if (cursorLine > 0) {
                    cursorLine--;
                    cursorPos = lines.get(cursorLine).length();
                }
                break;

            case GLFW.GLFW_KEY_RIGHT:
                if (cursorPos < currentLine.length()) {
                    cursorPos++;
                } else if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorPos = 0;
                }
                break;

            case GLFW.GLFW_KEY_HOME:
                cursorPos = 0;
                break;

            case GLFW.GLFW_KEY_END:
                cursorPos = currentLine.length();
                break;

            case GLFW.GLFW_KEY_V:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    // Handle paste
                    String clipboard = getClipboard();
                    insertText(clipboard);
                }
                break;

            case GLFW.GLFW_KEY_C:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && hasSelection()) {
                    // Handle copy
                    String selectedText = getSelectedText();
                    setClipboard(selectedText);
                }
                break;

            case GLFW.GLFW_KEY_X:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && hasSelection()) {
                    // Handle cut
                    String selectedText = getSelectedText();
                    setClipboard(selectedText);
                    deleteSelection();
                }
                break;

            case GLFW.GLFW_KEY_A:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    // Select all
                    selectionStart = 0;
                    selectionEnd = getTextLength();
                    cursorLine = lines.size() - 1;
                    cursorPos = lines.get(lines.size() - 1).length();
                }
                break;

            default:
                return false;
        }

        ensureCursorVisible();
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused()) return false;

        // Insert character (simplified - only basic characters)
        if (codePoint >= 32 && codePoint != 127) { // Printable characters
            insertText(String.valueOf(codePoint));
            return true;
        }

        return false;
    }

    private void insertText(String textToInsert) {
        List<String> lines = getLines();
        if (lines.isEmpty()) {
            lines.add("");
        }

        // Ensure cursor is within valid bounds
        cursorLine = Mth.clamp(cursorLine, 0, lines.size() - 1);
        String currentLine = lines.get(cursorLine);
        cursorPos = Mth.clamp(cursorPos, 0, currentLine.length());

        String newLine = currentLine.substring(0, cursorPos) + textToInsert + currentLine.substring(cursorPos);
        lines.set(cursorLine, newLine);
        setTextFromLines(lines);

        cursorPos += textToInsert.length();
        ensureCursorVisible();
    }

    private void selectWordAt(double mouseX, double mouseY) {
        // Simplified word selection
        cursorLine = getLineAtPos(mouseY);
        List<String> lines = getLines();
        if (cursorLine >= lines.size()) {
            cursorLine = Math.max(0, lines.size() - 1);
        }

        cursorPos = getPosInLine(cursorLine, mouseX);

        String currentLine = lines.get(cursorLine);
        if (currentLine.isEmpty()) return;

        // Ensure cursorPos is within bounds
        cursorPos = Mth.clamp(cursorPos, 0, currentLine.length());

        // Find word boundaries
        int start = cursorPos;
        int end = cursorPos;

        while (start > 0 && Character.isLetterOrDigit(currentLine.charAt(start - 1))) {
            start--;
        }

        while (end < currentLine.length() && Character.isLetterOrDigit(currentLine.charAt(end))) {
            end++;
        }

        selectionStart = getPositionInText(cursorLine, start);
        selectionEnd = getPositionInText(cursorLine, end);
        cursorPos = end;
    }

    private int getCursorPosition() {
        return getPositionInText(cursorLine, cursorPos);
    }

    private int getPositionInText(int line, int posInLine) {
        List<String> lines = getLines();
        int position = 0;
        for (int i = 0; i < line && i < lines.size(); i++) {
            position += lines.get(i).length() + 1; // +1 for newline
        }
        return position + Math.min(posInLine, lines.get(Math.min(line, lines.size() - 1)).length());
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);

        // Ensure bounds are valid
        start = Mth.clamp(start, 0, text.length());
        end = Mth.clamp(end, 0, text.length());

        if (start >= end) return "";

        return text.substring(start, end);
    }

    private void deleteSelection() {
        if (!hasSelection()) return;

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);

        // Ensure bounds are valid
        start = Mth.clamp(start, 0, text.length());
        end = Mth.clamp(end, 0, text.length());

        if (start >= end) {
            selectionStart = -1;
            selectionEnd = -1;
            return;
        }

        text = text.substring(0, start) + text.substring(end);

        // Update cursor position
        String beforeCursor = text.substring(0, start);
        String[] linesBefore = beforeCursor.split("\n", -1);
        cursorLine = Math.max(0, linesBefore.length - 1);
        if (cursorLine < linesBefore.length) {
            cursorPos = Math.min(linesBefore[linesBefore.length - 1].length(), linesBefore[linesBefore.length - 1].length());
        } else {
            cursorPos = 0;
        }

        selectionStart = -1;
        selectionEnd = -1;
    }

    private int getTextLength() {
        return text.length();
    }

    private String getClipboard() {
        try {
            return net.minecraft.client.Minecraft.getInstance().keyboardHandler.getClipboard();
        } catch (Exception e) {
            return "";
        }
    }

    private void setClipboard(String text) {
        try {
            net.minecraft.client.Minecraft.getInstance().keyboardHandler.setClipboard(text);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() &&
                mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
    }

    // Helper classes for syntax highlighting
    private enum TokenType {
        NORMAL, COMMENT, KEYWORD, FUNCTION, STRING
    }

    private static class TextToken {
        final String text;
        final TokenType type;

        TextToken(String text, TokenType type) {
            this.text = text;
            this.type = type;
        }
    }
}