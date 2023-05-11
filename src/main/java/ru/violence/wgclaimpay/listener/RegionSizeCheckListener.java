package ru.violence.wgclaimpay.listener;

import com.sk89q.worldguard.bukkit.event.api.RegionPreClaimEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.wgclaimpay.LangKeys;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.config.Config;

public class RegionSizeCheckListener implements Listener {
    private final WGClaimPayPlugin plugin;

    public RegionSizeCheckListener(WGClaimPayPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void on(RegionPreClaimEvent event) {
        Player player = event.getPlayer();

        // Check for bypass permission
        if (player.hasPermission("worldguard.region.unlimited")) return;
        if (player.hasPermission("wgclaimpay.bypass")) return;

        int regionSize = plugin.getWorldEdit().getSelection(player).getRegionSelector().getArea();

        if (Config.MIN_SIZE > 0 && Config.MIN_SIZE > regionSize) {
            event.setCancelled(true);
            BukkitHelper.getUser(player).sendMessage(LangKeys.REGION_TOO_SMALL.setArgs(Config.MIN_SIZE, regionSize, event.getRegionId()));
        }
    }
}
