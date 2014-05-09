package com.afterkraft.kraftrpg.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ActiveSkill;
import com.afterkraft.kraftrpg.api.skills.ISkill;

public class SkillCommand implements TabExecutor {
    private RPGPlugin plugin;

    public SkillCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Champion champ = plugin.getEntityManager().getChampion((Player) sender);

        if (args.length == 0) {
            String lastArg = args[args.length - 1];
            List<String> temp = new ArrayList<String>();

            StringUtil.copyPartialMatches(lastArg, champ.getActiveSkillNames(), temp);
            return temp;
        } else {
            String skillName = args[0];

            ISkill sk = plugin.getSkillManager().getSkill(skillName);
            if (!(sk instanceof Active)) return null;
            Active skill = (Active) sk;

            return skill.tabComplete(champ, args, 1);
        }
    }

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        return false;
    }

}
