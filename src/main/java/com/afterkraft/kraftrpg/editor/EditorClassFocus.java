package com.afterkraft.kraftrpg.editor;

import java.util.List;

import org.bukkit.conversations.ConversationContext;

public class EditorClassFocus extends EditorPrompt {
    @Override
    public void printBanner(ConversationContext context) {

    }

    @Override
    public String getPrompt(ConversationContext context) {
        return null;
    }

    @Override
    public String getName(ConversationContext context) {
        return EditorState.getSelectedRole(context).getName();
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        return null;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return null;
    }
}
