/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Gabriel Harris-Rouquette
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.afterkraft.kraftrpg.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;

/**
 * This class provides a nicer interface into the key-value store of a ConversationContext.
 *
 * Does not actually contain state.
 *
 * Instead, all of the methods take a ConversationContext.
 */
@SuppressWarnings("unchecked")
public final class EditorState {

    private static final StrTokenizer tokenizer = new StrTokenizer("", StrMatcher.charMatcher(';'));

    public static Map<Object, Object> getStableDefaultState() {
        Map<Object, Object> map = new HashMap<>();
        map.put("dirty", false);
        map.put("banner", true);
        map.put("role", null);
        return map;
    }

    public static void applyUnstableDefaultState(ConversationContext context) {
        context.setSessionData("stack", new ArrayList<EditorPrompt>());
        context.setSessionData("queue", new ArrayList<String>());
        context.setSessionData("actions", new ArrayList<EditorRunnable>());
    }

    public static boolean isDirty(ConversationContext context) {
        List<EditorRunnable> actions = (List<EditorRunnable>) context.getSessionData("actions");
        return !actions.isEmpty();
    }

    public static void commit(ConversationContext context) {
        List<EditorRunnable> actions = (List<EditorRunnable>) context.getSessionData("actions");
        for (EditorRunnable run : actions) {
            run.run(context);
        }
    }

    public static void saveAction(ConversationContext context, EditorRunnable runnable) {
        List<EditorRunnable> actions = (List<EditorRunnable>) context.getSessionData("actions");
        actions.add(runnable);
    }

    public static boolean shouldPrintBanner(ConversationContext context) {
        if (hasQueuedCommands(context)) {
            return false;
        }
        return (Boolean) context.getSessionData("banner");
    }

    public static boolean hasQueuedCommands(ConversationContext context) {
        List<String> queue = (List<String>) context.getSessionData("queue");
        return !queue.isEmpty();
    }

    // Command queue

    public static void setBanner(ConversationContext context, boolean printBanner) {
        context.setSessionData("banner", printBanner);
    }

    public static List<EditorPrompt> getPromptStack(ConversationContext context) {
        return (List<EditorPrompt>) context.getSessionData("stack");
    }

    public static void queueCommands(ConversationContext context, String unsplitCommand) {
        tokenizer.reset(unsplitCommand);
        List<String> tokens = tokenizer.getTokenList();
        List<String> queue = (List<String>) context.getSessionData("queue");
        ListIterator<String> iter = tokens.listIterator(tokens.size());
        while (iter.hasPrevious()) {
            queue.add(iter.previous());
        }
    }

    public static String popCommand(ConversationContext context) {
        List<String> queue = (List<String>) context.getSessionData("queue");
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(queue.size() - 1);
    }

    public static void clearCommandQueue(ConversationContext context) {
        context.setSessionData("queue", new ArrayList<String>());
    }

    // Role editing

    public static void clearRole(ConversationContext context) {
        context.setSessionData("role", null);
    }

    public static void setSelectedRole(ConversationContext context, Role role) {
        context.setSessionData("role", role);
    }

    public static Role getSelectedRole(ConversationContext context) {
        return (Role) context.getSessionData("role");
    }
}
