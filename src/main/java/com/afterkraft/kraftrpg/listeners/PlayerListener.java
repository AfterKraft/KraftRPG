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
package com.afterkraft.kraftrpg.listeners;

import com.afterkraft.kraftrpg.api.conversations.TabCompletablePrompt;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.*;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;

public class PlayerListener extends AbstractListener {

    protected PlayerListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event) {
        Player player = event.getPlayer();
        if (player.isConversing()) {
            Conversation conversation = CraftBukkitHandler.getInterface().getCurrentConversation(player);
            if (conversation == null) return;
            Prompt prompt = CraftBukkitHandler.getInterface().getCurrentPrompt(conversation);

            if (prompt instanceof TabCompletablePrompt) {
                ((TabCompletablePrompt) prompt).onTabComplete(conversation.getContext(), event.getChatMessage(), event.getLastToken());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        Champion c = plugin.getEntityManager().getChampion(p);

        plugin.getStorage().saveChampion(c);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKickEvent(PlayerKickEvent event) {

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExpEvent(PlayerExpChangeEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFishEvent(PlayerFishEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTameEvent(EntityTameEvent event) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {

    }
}
