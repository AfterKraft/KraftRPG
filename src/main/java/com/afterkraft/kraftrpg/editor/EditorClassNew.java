package com.afterkraft.kraftrpg.editor;

import org.bukkit.conversations.ConversationContext;

import java.util.List;

public class EditorClassNew extends EditorPrompt {
    @Override
    public void printBanner(ConversationContext context) {

    }

    @Override
    public String getPrompt(ConversationContext context) {
        return null;
    }

    @Override
    public String getName(ConversationContext context) {
        return "new";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        return null;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return null;
    }
}
