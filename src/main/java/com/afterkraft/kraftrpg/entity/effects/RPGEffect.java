package com.afterkraft.kraftrpg.entity.effects;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.effects.Effect;
import com.afterkraft.kraftrpg.api.entity.effects.EffectType;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;

/**
 * @author gabizou
 */

public abstract class RPGEffect implements Effect {

    protected final String name;
    protected final RPGPlugin plugin;
    protected final Spell<? extends SpellArgument> spell;
    public final Set<EffectType> types = EnumSet.noneOf(EffectType.class);
    protected long applyTime;
    private boolean persistent = false;

    private final Set<PotionEffect> potionEffects = new HashSet<PotionEffect>();
    private final Set<PotionEffect> mobEffects = new HashSet<PotionEffect>();

    public RPGEffect(Spell<? extends SpellArgument> spell, String name) {
        this(spell == null ? null : spell.plugin, spell, name);
    }

    public RPGEffect(Spell<? extends SpellArgument> spell, String name, EffectType... types) {
        this(spell.plugin, spell, name, types);
    }

    public RPGEffect(RPGPlugin plugin, Spell<? extends SpellArgument> spell, String name, EffectType... types) {
        this.name = name;
        this.spell = spell;
        if (plugin != null)
            this.plugin = plugin;
        else
            this.plugin = spell.plugin;

        if (types != null) {
            this.types.addAll(Arrays.asList(types));
        }
    }

    // ----
    // Generic Utility Methods
    // ----

    /**
     * This returns the Spell this Effect originated from, the Spell is used for description
     * and message handling, as well as broadcasting.
     * @return the {@link com.afterkraft.kraftrpg.api.spells.Spell} that created this Effect
     */
    public final Spell<? extends SpellArgument> getSpell() {
        return spell;
    }

    /**
     * Returns this individual Effect's name. (Should be as unique and recognizable as possible).
     * @return the name of this effect.
     */
    public final String getName() {
        return name;
    }

    public boolean isType(EffectType queryType) {
        return types.contains(queryType);
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void addPotionEffect(PotionEffect pEffect, boolean faked) {
        if (!faked) {
            potionEffects.add(pEffect);
        } else {
            mobEffects.add(pEffect);
        }
    }

    public void addMobEffect(PotionEffectType type, int duration, int strength, boolean faked) {
        addPotionEffect(new PotionEffect(type, duration, strength), faked);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RPGEffect)) {
            return false;
        }
        final RPGEffect other = (RPGEffect) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    // ----
    // Application Methods
    // ----

    /**
     * @return the applyTime
     */
    public long getApplyTime() {
        return applyTime;
    }

    /**
     * Attempts to apply this RPGEffect to the provided RPGEntity.
     * @param entity this effect is being applied on to.
     */
    public final void apply(IEntity entity) {
        if (entity instanceof Champion) {
            applyToPlayer((Champion) entity);
        } else if (entity instanceof Monster) {
            applyToMonster((Monster) entity);
        }
    }

    /**
     * Version specific implementation for applying this effect to an RPGChampion
     * @param player this effect is being applied to.
     */
    public void applyToPlayer(Champion player) {
        if (!player.isEntityValid()) {
            return;
        }
        this.applyTime = System.currentTimeMillis();
        if (!potionEffects.isEmpty()) {
            final Player p = player.getPlayer();
            // Apply potion effects (non-faked)
            for (final PotionEffect pEffect : potionEffects) {
                p.addPotionEffect(pEffect);
            }
            // Apply faked effects
            if(!mobEffects.isEmpty()) {
                CraftBukkitHandler.getInterface().sendFakePotionEffectPackets(mobEffects, p);
            }
        }
    }

    /**
     * Version specific implementation for applying this effect to an RPGMonster
     *
     * @param monster this effect is being applied to.
     */
    public void applyToMonster(Monster monster) {
        if (!monster.isEntityValid()) {
            return;
        }
        this.applyTime = System.currentTimeMillis();
        if (!potionEffects.isEmpty()) {
            final LivingEntity entity = monster.getEntity();
            for (final PotionEffect effect : potionEffects) {
                entity.addPotionEffect(effect);
            }
        }
    }

    /**
     * Attempts to remove this RPGEffect from the given RPGEntity
     * <p>
     * @param entity this effect is being removed by.
     */
    public final void remove(IEntity entity) {
        if (entity instanceof Champion) {
            removeFromPlayer((Champion) entity);
        } else if (entity instanceof Monster) {
            removeFromMonster((Monster) entity);
        }
    }

    /**
     * Version specific implementation for removing this effect from an RPGChampion
     *
     * @param player this effect is being removed from.
     */
    public void removeFromPlayer(Champion player) {
        if (!player.isEntityValid()) {
            return;
        }
        final Player p = player.getPlayer();
        if (!potionEffects.isEmpty()) {
            for (final PotionEffect pEffect : potionEffects) {
                p.removePotionEffect(pEffect.getType());
            }
        }
        if (!mobEffects.isEmpty()) {
            CraftBukkitHandler nmsHandler = CraftBukkitHandler.getInterface();
            nmsHandler.removeFakePotionEffectPackets(mobEffects, p);
        }
    }

    /**
     * Version specific implementation for removing this effect from an RPGMonster
     * @param monster this effect is being removed from.
     */
    public void removeFromMonster(Monster monster) {
        if (!monster.isEntityValid()) {
            return;
        }
        if (!potionEffects.isEmpty()) {
            final LivingEntity entity = monster.getEntity();
            for (final PotionEffect effect : potionEffects) {
                entity.removePotionEffect(effect.getType());
            }
        }
    }
}
