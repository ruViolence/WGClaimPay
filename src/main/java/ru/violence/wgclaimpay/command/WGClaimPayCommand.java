package ru.violence.wgclaimpay.command;

import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.menu.BillsMenu;
import ru.violence.wgclaimpay.util.RateLimit;
import ru.violence.wgclaimpay.util.Utils;

import java.util.Locale;

public class WGClaimPayCommand implements CommandExecutor {
    private final WGClaimPayPlugin plugin;

    public WGClaimPayCommand(WGClaimPayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length == 0) return true;

        Player player = (Player) sender;

        if (args[0].equals("reload") && sender.hasPermission("wgclaimpay.command.reload")) {
            WGClaimPayPlugin.getInstance().reloadConfig();
            sender.sendMessage("Config reloaded");
            return true;
        }

        if (args[0].equals("bills") && sender.hasPermission("wgclaimpay.command.bills")) {
            if (RateLimit.check(player.getUniqueId())) return true;
            BillsMenu.openAsync(player, 0);
            return true;
        }

        if (args[0].equals("test") && sender.hasPermission("wgclaimpay.command.test")) {
            RegionSelector selector = plugin.getWorldEdit().getSelection(player).getRegionSelector();
            int regionSize = selector.getArea();

            String type = args.length > 1 ? args[1] : "";
            switch (type.toLowerCase(Locale.ROOT)) {
                case "bill":
                case "prolong":
                case "prolongation": {
                    int price = Utils.calcBillPrice(regionSize);

                    player.sendMessage("§eThis region costs " + price + " money to prolong");
                    break;
                }
                default:
                case "claim": {
                    int price = Utils.calcClaimPrice(regionSize);

                    player.sendMessage("§eThis region costs " + price + " money to claim");
                    break;
                }
            }
            return true;
        }

        return true;
    }
}
