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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.conversations.TabCompletablePrompt;
import com.afterkraft.kraftrpg.api.effects.common.InvisibiliytEffect;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.util.PlayerUtil;

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
        Champion c = this.plugin.getEntityManager().getChampion(p);
        c.clearEffects();
        if (c.hasParty()) {
            Party party = c.getParty();
            party.removeMember(c, true);
        }
        c.cancelStalledSkill(false);
        this.plugin.getCombatTracker().leaveCombat(c, LeaveCombatReason.LOGOUT);
        this.plugin.getStorage().saveChampion(c);
        this.plugin.getEntityManager().removeChampion(c);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKickEvent(PlayerKickEvent event) {
        final Player player = event.getPlayer();
        final Champion champion = this.plugin.getEntityManager().getChampion(player);
        this.plugin.getCombatTracker().leaveCombat(champion, LeaveCombatReason.KICK);
        champion.recalculateMaxHealth();
        PlayerUtil.syncronizeExperienceBar(champion);
        champion.updateInventory();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Champion champion = this.plugin.getEntityManager().getChampion(player);
        champion.recalculateMaxHealth();
        PlayerUtil.syncronizeExperienceBar(champion);
        champion.updateInventory();
        if (!player.hasPermission("kraftrpg.admin.invisibility.see")) {
            for (UUID onlinePlayer : InvisibiliytEffect.getInvisiblePlayers()) {
                player.hidePlayer(Bukkit.getPlayer(onlinePlayer));
            }
        }
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
