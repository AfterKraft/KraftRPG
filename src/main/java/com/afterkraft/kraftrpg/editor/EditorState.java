/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.editor;

import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import java.util.*;

/**
 * This class provides a nicer interface into the key-value store of a
 * ConversationContext.
 *
 * Does not actually contain state.
 *
 * Instead, all of the methods take a ConversationContext.
 */
@SuppressWarnings("unchecked")
public final class EditorState {

    public static Map<Object, Object> getStableDefaultState() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("dirty", false);
        map.put("banner", true);
        map.put("role", null);
        return map;
    }

    public static void applyUnstableDefaultState(ConversationContext context) {
        context.setSessionData("stack", new ArrayList<EditorPrompt>());
        context.setSessionData("queue", new ArrayList<String>());
    }

    public static boolean isDirty(ConversationContext context) {
        return (Boolean) context.getSessionData("dirty");
    }

    public static void setDirty(ConversationContext context, boolean dirty) {
        context.setSessionData("dirty", dirty);
    }

    public static boolean shouldPrintBanner(ConversationContext context) {
        return (Boolean) context.getSessionData("banner");
    }

    public static void setBanner(ConversationContext context, boolean printBanner) {
        context.setSessionData("banner", printBanner);
    }

    public static List<EditorPrompt> getPromptStack(ConversationContext context) {
        return (List<EditorPrompt>) context.getSessionData("stack");
    }

    // Command queue

    private static final StrTokenizer tokenizer = new StrTokenizer("", StrMatcher.charMatcher(';'));

    public static void queueCommands(ConversationContext context, String unsplitCommand) {
        tokenizer.reset(unsplitCommand);
        List<String> tokens = tokenizer.getTokenList();
        List<String> queue = (List<String>) context.getSessionData("queue");
        ListIterator<String> iter = tokens.listIterator(tokens.size());
        while (iter.hasPrevious()) {
            queue.add(iter.previous());
        }
    }

    public static boolean hasQueuedCommands(ConversationContext context) {
        List<String> queue = (List<String>) context.getSessionData("queue");
        return !queue.isEmpty();
    }

    public static String popCommand(ConversationContext context) {
        List<String> queue = (List<String>) context.getSessionData("queue");
        if (queue.isEmpty()) return null;
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
