package ru.violence.wgclaimpay.listener;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.bukkit.event.api.RegionPreClaimEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.wgclaimpay.LangKeys;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionPreClaimListener implements Listener {
    private final WGClaimPayPlugin plugin;
    private final @Nullable Confirmation confirmation;

    public RegionPreClaimListener(WGClaimPayPlugin plugin) {
        this.plugin = plugin;
        this.confirmation = plugin.isConfirmation() ? new Confirmation(plugin) : null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(RegionPreClaimEvent event) {
        Player player = event.getPlayer();

        // Check for bypass permission
        if (event.getPlayer().hasPermission("worldguard.region.unlimited")) return;
        if (event.getPlayer().hasPermission("wgclaimpay.bypass")) return;

        String regionId = event.getRegionId();
        RegionSelector selector = plugin.getWorldEdit().getSelection(player).getRegionSelector();
        int regionSize = selector.getArea();

        if (plugin.getMinSize() > 0 && plugin.getMinSize() > regionSize) {
            event.setCancelled(true);
            BukkitHelper.getUser(player).sendMessage(LangKeys.REGION_TOO_SMALL.setArgs(plugin.getMinSize(), regionSize, regionId));
            return;
        }

        double balance = plugin.getEconomy().getBalance(player);
        int price = Utils.calcPrice(plugin, regionSize);

        try {
            if (!checkConfirmation(player, selector, regionSize)) {
                event.setCancelled(true);
                BukkitHelper.getUser(player).sendMessage(LangKeys.AWAITING_CONFIRMATION.setArgs(price, regionSize, regionId));
                return;
            }

            if (balance < price || !plugin.getEconomy().withdrawPlayer(player, price).transactionSuccess()) {
                event.setCancelled(true);
                BukkitHelper.getUser(player).sendMessage(LangKeys.NOT_ENOUGH_MONEY.setArgs(price, balance, regionSize, regionId));
                return;
            }

            plugin.getLogger().info("Charged " + price + " money from the " + player.getName() + " for the region " + event.getRegionId() + " (" + regionSize + ")");
            BukkitHelper.getUser(player).sendMessage(LangKeys.PAID_FOR_REGION.setArgs(price, regionSize, regionId));
        } catch (IncompleteRegionException e) {
            event.setCancelled(true);
            player.sendMessage("§cError: " + e.getClass().getName());
        }
    }

    private boolean checkConfirmation(@NotNull Player player, @NotNull RegionSelector selector, int regionSize) throws IncompleteRegionException {
        if (confirmation == null) return true;

        Claim claim = new Claim(selector.getRegion(), regionSize);

        // If already awaiting
        if (claim.equals(confirmation.takeConfirmation(player))) return true;

        confirmation.addConfirmation(player, claim);
        return false;
    }

    private static class Confirmation {
        private static final long AWAITING_TIME = 15 * 1000;
        private final Map<UUID, Claim> confirmations = new HashMap<>();

        public Confirmation(WGClaimPayPlugin plugin) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                long currentTime = System.currentTimeMillis();
                confirmations.values().removeIf(claim -> currentTime >= claim.expirationTime);
            }, 0, 20);
        }

        @Nullable Claim takeConfirmation(@NotNull Player player) {
            return confirmations.remove(player.getUniqueId());
        }

        void addConfirmation(@NotNull Player player, @NotNull Claim claim) {
            confirmations.put(player.getUniqueId(), claim);
        }
    }

    @RequiredArgsConstructor
    private static class Claim {
        private final @NotNull Region region;
        private final int size;
        private final long expirationTime = System.currentTimeMillis() + Confirmation.AWAITING_TIME;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Claim claim = (Claim) o;

            if (size != claim.size) return false;
            return region.equals(claim.region);
        }

        @Override
        public int hashCode() {
            int result = region.hashCode();
            result = 31 * result + size;
            return result;
        }
    }
}
