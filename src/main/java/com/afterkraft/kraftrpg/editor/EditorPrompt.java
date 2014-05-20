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

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.conversations.TabCompletablePrompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class EditorPrompt implements TabCompletablePrompt {
    protected static final RPGPlugin plugin = KraftRPGPlugin.getInstance();

    ///////////////////////////////////////////////////////////////////////////
    // Required abstract methods
    public abstract void printBanner(ConversationContext context);
    public abstract String getPrompt(ConversationContext context);

    public abstract String getName(ConversationContext context);

    /**
     * Perform the actions for this Prompt.
     *
     * @param context context
     * @param command command, after splitting on semicolon
     * @return Next prompt to advance, or null on syntax error
     */
    public abstract EditorPrompt performCommand(ConversationContext context, String command);

    public abstract List<String> getCompletions(ConversationContext context);

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods for subclasses

    public void sendMessage(ConversationContext context, String string) {
        Conversable who = context.getForWhom();
        if (who instanceof ConsoleCommandSender) {
            // This is required for colors in the console
            ((ConsoleCommandSender) who).sendMessage(string);
        } else {
            // Players are fine, because we're never modal
            who.sendRawMessage(string);
        }
    }

    public String getPathString(ConversationContext context) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        StringBuilder builder = new StringBuilder(ChatColor.GOLD.toString());
        for (EditorPrompt prompt : stack) {
            builder.append(prompt.getName(context)).append(">");
        }
        builder.append(getName(context)).append(">");
        builder.append(ChatColor.BLUE).append(' ');
        return builder.toString();
    }

    /**
     * Push this prompt on the stack and return the given prompt.
     * Use this to "call" another prompt.
     */
    public EditorPrompt callPrompt(ConversationContext context, EditorPrompt next) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        stack.add(this);
        return next;
    }

    /**
     * Pop the previous prompt off the stack.
     * Use this to "return to" another prompt.
     */
    public EditorPrompt returnPrompt(ConversationContext context) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        return stack.remove(stack.size() - 1);
    }

    public EditorPrompt commonActions(ConversationContext context, String command) {
        if (command.equals("?")) {
            EditorState.setBanner(context, true);
            return this;
        } else if (command.equals("exit") || command.equals("stop")) {
            return returnPrompt(context);
        } else if (command.equals("quit")) {
            return END_CONVERSATION;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Bukkit API methods

    @Override
    public final String getPromptText(ConversationContext context) {
        if (EditorState.shouldPrintBanner(context)) {
            printBanner(context);
        }
        sendMessage(context, getPrompt(context));
        return null; // returned strings are color-stripped
    }

    @Override
    public final boolean blocksForInput(ConversationContext context) {
        return !EditorState.hasQueuedCommands(context);
    }

    @Override
    public final Prompt acceptInput(ConversationContext context, String input) {
        // BUKKIT-5611
        if (!Bukkit.isPrimaryThread()) {
            final ConversationContext _context = context;
            final String _input = input;
            Future<Prompt> future = Bukkit.getScheduler().callSyncMethod(KraftRPGPlugin.getInstance(), new Callable<Prompt>() {
                @Override
                public Prompt call() throws Exception {
                    return EditorPrompt.this.acceptInput(_context, _input);
                }
            });
            new RuntimeException("Prompt called from non-main thread!").printStackTrace();
            boolean interrupted = Thread.interrupted();
            Prompt ret = null;
            try {
                ret = future.get();
            } catch (InterruptedException e) {
                interrupted = true;
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
            if (interrupted) Thread.currentThread().interrupt();
            return ret;
        }

        if (input != null) {
            EditorState.queueCommands(context, input);
        }

        String command = EditorState.popCommand(context);
        if (command == null) return this;

        sendMessage(context, getPathString(context) + ChatColor.RESET + command);
        EditorPrompt next = performCommand(context, command);
        if (next == null) {
            // Syntax error
            EditorState.clearCommandQueue(context);
            EditorState.setBanner(context, false);
            return this;
        } else if (next == EditorPrompt.END_CONVERSATION) {
            return Prompt.END_OF_CONVERSATION;
        } else {
            EditorState.setBanner(context, true);
            return next;
        }
    }

    @Override
    public List<String> onTabComplete(ConversationContext context, String fullMessage, String lastToken) {
        List<String> matches = new ArrayList<String>();
        StringUtil.copyPartialMatches(lastToken, getCompletions(context), matches);
        return matches;
    }

    protected static final EditorPrompt END_CONVERSATION = new EditorPrompt() {
        public String getName(ConversationContext context) { return null; }
        public void printBanner(ConversationContext context) { }
        public String getPrompt(ConversationContext context) { return null; }
        public EditorPrompt performCommand(ConversationContext context, String command) { return null; }
        public List<String> getCompletions(ConversationContext context) { return null; }
    };
}
