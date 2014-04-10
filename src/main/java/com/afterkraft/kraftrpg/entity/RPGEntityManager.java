package com.afterkraft.kraftrpg.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.storage.RPGStorage;

/**
 * @author gabizou
 */
public class RPGEntityManager implements EntityManager {

    private final KraftRPGPlugin plugin;
    private final Map<UUID, Champion> champions;
    private final Map<UUID, Monster> monsters;

    private RPGStorage storage;

    public RPGEntityManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.champions = new HashMap<UUID, Champion>();
        this.monsters = new ConcurrentHashMap<UUID, Monster>();
        Bukkit.getScheduler().runTaskTimer(plugin, new RPGEntityTask(), 100, 1000);
        this.storage = this.plugin.getStorageManager().getStorage();
    }

    public final IEntity getEntity(LivingEntity entity) {
        if (entity instanceof Player) {
            return this.getChampion((Player) entity);
        } else {
            return this.getMonster(entity);
        }
    }

    @Override
    public void initialize() {

    }

    public void shutdown() {}

    public Champion getChampion(Player player) {
        if (player == null) {
            return null;
        }
        Champion champion = this.champions.get(player.getUniqueId());
        if (champion != null) {
            if (!champion.isEntityValid() || (champion.getPlayer().getEntityId() != player.getEntityId())) {
                plugin.log(Level.WARNING, "Duplicate Champion object found! Please make sure Champions are properly removed!");
                champion.clearEffects();
                champion.setPlayer(player);
            }
        } else {
            champion = createNewChampion(player);
            plugin.getStorageManager().getStorage().saveChampion(champion, true);
        }
        return champion;
    }

    protected Champion createNewChampion(Player player) {
        return new RPGChampion(this.plugin, player, this.plugin.getRoleManager().getDefaultPrimaryRole(), null);
    }

    public Monster getMonster(LivingEntity entity) {
        return null;
    }

    public boolean isMonsterSetup(LivingEntity entity) {
        return this.monsters.containsKey(entity.getUniqueId());
    }

    /**
     * Attempts to add the given RPGChampion to this player mapping. This should
     * only be used to add an RPGChampion for custom RPGPlayers that aren't
     * covered by KraftRPG.
     *
     * @param player - The RPGChampion to add to the player mapping
     * @return true if the RPGChampion addition was successful
     */
    public boolean addChampion(Champion player) {
        if (!player.isEntityValid() || this.champions.containsKey(player.getPlayer().getUniqueId())) {
            return false;
        }
        this.champions.put(player.getPlayer().getUniqueId(), player);
        return true;
    }

    public boolean addMonster(Monster monster) {
        if (!monster.isEntityValid()) {
            return false;
        }
        final UUID id = monster.getEntity().getUniqueId();
        if (this.monsters.containsKey(id)) {
            return false;
        } else {
            this.monsters.put(id, monster);
            return true;
        }
    }

    public Monster getMonster(UUID uuid) {
        return this.monsters.get(uuid);
    }

    @Override
    public Champion getChampion(UUID uuid) {
        return null;
    }

    /**
     * A Timer task to remove the potentially GC'ed LivingEntities either due to
     * death, chunk unload, or reload.
     */
    private class RPGEntityTask implements Runnable {

        @Override
        public void run() {
            Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
            Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;

            Iterator<Map.Entry<UUID, Champion>> playerIterator = rpgPlayerMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Monster>> monsterIterator = monsterMap.entrySet().iterator();

            while (playerIterator.hasNext()) {
                Champion tempPlayer = playerIterator.next().getValue();
                if (!tempPlayer.isEntityValid()) {
                    playerIterator.remove();
                }
            }

            while (monsterIterator.hasNext()) {
                Monster tempMonster = monsterIterator.next().getValue();
                if (!tempMonster.isEntityValid()) {
                    monsterIterator.remove();
                }
            }

        }
    }
}
