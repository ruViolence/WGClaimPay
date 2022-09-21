package ru.violence.wgclaimpay.command;

import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.util.Utils;

public class WGClaimPayCommand implements CommandExecutor {
    private final WGClaimPayPlugin plugin;

    public WGClaimPayCommand(WGClaimPayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (!sender.hasPermission("wgclaimpay.command.use")) return true;

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equals("test")) {
            RegionSelector selector = plugin.getWorldEdit().getSelection(player).getRegionSelector();
            int regionSize = selector.getArea();

            int price = Utils.calcPrice(plugin, regionSize);

            player.sendMessage("§eThis region costs " + price + " money");
        }

        return true;
    }
}
