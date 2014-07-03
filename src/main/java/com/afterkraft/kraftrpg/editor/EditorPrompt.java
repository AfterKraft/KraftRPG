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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.util.StringUtil;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.conversations.TabCompletablePrompt;

public abstract class EditorPrompt implements TabCompletablePrompt {
    protected static final RPGPlugin plugin = KraftRPGPlugin.getInstance();
    protected static final EditorPrompt END_CONVERSATION = new EditorPrompt() {
        public String getName(ConversationContext context) {
            return null;
        }

        public EditorPrompt performCommand(ConversationContext context, String command) {
            return null;
        }

        public void printBanner(ConversationContext context) {
        }

        public String getPrompt(ConversationContext context) {
            return null;
        }

        public List<String> getCompletions(ConversationContext context) {
            return null;
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods for subclasses

    /**
     * Push this prompt on the stack and return the given prompt. Use this to
     * "call" another prompt.
     */
    public EditorPrompt callPrompt(ConversationContext context, EditorPrompt next) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        stack.add(this);
        return next;
    }

    public EditorPrompt commonActions(ConversationContext context, String command) {
        if (command.equals("?")) {
            EditorState.setBanner(context, true);
            return this;
        } else if (command.equals("exit") || command.equals("stop")) {
            return returnPrompt(context);
        } else if (command.equals("quit")) {
            return END_CONVERSATION;
        } else if (command.equals("save")) {
            EditorState.commit(context);
            sendMessage(context, "%sSaved.", ChatColor.GREEN);
            return this;
        }
        return null;
    }

    /**
     * Pop the previous prompt off the stack. Use this to "return to" another
     * prompt.
     */
    public EditorPrompt returnPrompt(ConversationContext context) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        return stack.remove(stack.size() - 1);
    }

    public void sendMessage(ConversationContext context, String format, Object... args) {
        Conversable who = context.getForWhom();
        if (who instanceof ConsoleCommandSender) {
            // This is required for colors in the console
            ((ConsoleCommandSender) who).sendMessage(String.format(format, args));
        } else {
            // Players are fine, because we're never modal
            who.sendRawMessage(String.format(format, args));
        }
    }

    public String getPathString(ConversationContext context) {
        List<EditorPrompt> stack = EditorState.getPromptStack(context);
        StringBuilder builder = new StringBuilder(ChatColor.GOLD.toString());
        if (EditorState.isDirty(context)) {
            builder.append("[*] ");
        }

        for (EditorPrompt prompt : stack) {
            builder.append(prompt.getName(context)).append(">");
        }
        builder.append(getName(context)).append(">");
        builder.append(ChatColor.BLUE).append(' ');
        return builder.toString();
    }

    public abstract String getName(ConversationContext context);

    ///////////////////////////////////////////////////////////////////////////
    // Required abstract methods

    /**
     * Perform the actions for this Prompt.
     * 
     * @param context context
     * @param command command, after splitting on semicolon
     * @return Next prompt to advance, or null on syntax error
     */
    public abstract EditorPrompt performCommand(ConversationContext context, String command);

    @Override
    public final String getPromptText(ConversationContext context) {
        if (EditorState.shouldPrintBanner(context)) {
            sendMessage(context, ChatColor.YELLOW + "--------------------------------------------------");
            printBanner(context);
            EditorState.setBanner(context, false);
        }
        return getPrompt(context); // TODO returned strings are color-stripped for the console
    }

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

    public abstract void printBanner(ConversationContext context);

    public abstract String getPrompt(ConversationContext context);

    ///////////////////////////////////////////////////////////////////////////
    // Bukkit API methods

    @Override
    public final boolean blocksForInput(ConversationContext context) {
        return !EditorState.hasQueuedCommands(context);
    }

    @Override
    public final Prompt acceptInput(ConversationContext context, String input) {
        // FIXME BUKKIT-5611
        if (!Bukkit.isPrimaryThread()) {
            final ConversationContext _context = context;
            final String _input = input;
            Future<Prompt> future = Bukkit.getScheduler().callSyncMethod(KraftRPGPlugin.getInstance(), new Callable<Prompt>() {
                @Override
                public Prompt call() throws Exception {
                    return EditorPrompt.this.acceptInput(_context, _input);
                }
            });
            Bukkit.getLogger().severe("(KraftRPG) Conversations API is not running on main thread!");
            boolean interrupted = Thread.interrupted();
            Prompt ret = null;
            try {
                ret = future.get(500, TimeUnit.MILLISECONDS); // 10 ticks
            } catch (InterruptedException e) {
                interrupted = true;
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                // Can't tell the player about it - this isn't the main thread!!
                ret = this;
            }
            if (interrupted) Thread.currentThread().interrupt();

            // Retry, don't abandon
            if (ret == null) return this;
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

    public abstract List<String> getCompletions(ConversationContext context);
}
