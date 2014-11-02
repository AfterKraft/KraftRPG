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
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.conversations.TabCompletablePrompt;
import com.afterkraft.kraftrpg.api.effects.common.InvisibiliytEffect;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.util.PlayerUtil;

/**
 * Player listeners
 */
public class PlayerListener extends AbstractListener {

    protected PlayerListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event) {
        Player player = event.getPlayer();
        if (player.isConversing()) {
            Conversation conversation = RpgCommon.getHandler().getCurrentConversation(player);
            if (conversation == null) {
                return;
            }
            Prompt prompt = RpgCommon.getHandler().getCurrentPrompt(conversation);

            if (prompt instanceof TabCompletablePrompt) {
                ((TabCompletablePrompt) prompt)
                        .onTabComplete(conversation.getContext(), event.getChatMessage(),
                                       event.getLastToken());
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
