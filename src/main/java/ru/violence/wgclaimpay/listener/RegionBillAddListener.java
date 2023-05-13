package ru.violence.wgclaimpay.listener;

import com.sk89q.worldguard.bukkit.event.api.RegionAddedEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.util.Utils;

public class RegionBillAddListener implements Listener {
    private final WGClaimPayPlugin plugin;

    public RegionBillAddListener(WGClaimPayPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(RegionAddedEvent event) {
        Player player = event.getPlayer();
        ProtectedRegion region = event.getRegion();

        Bukkit.getScheduler().runTaskLater(plugin, () -> Utils.setBillPaying(player, region, true), 5);
    }
}
