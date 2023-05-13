package ru.violence.wgclaimpay.task;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.common.util.TimeUtil;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.config.Config;
import ru.violence.wgclaimpay.flag.Flags;
import ru.violence.wgclaimpay.util.Utils;

public class ProlongationTask extends BukkitRunnable {
    private final WGClaimPayPlugin plugin;

    public ProlongationTask(@NotNull WGClaimPayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Pair<World, ProtectedRegion> pair : Utils.getAllRegions()) {
            World world = pair.getLeft();
            ProtectedRegion region = pair.getRight();

            // Skip non-player regions
            if (region.getOwners().size() == 0) continue;

            // Recently created region
            Long billSince = region.getFlag(Flags.BILL_SINCE);
            if (billSince == null) {
                region.setFlag(Flags.BILL_SINCE, TimeUtil.currentTimeSeconds());
                continue;
            }

            // It's not time to pay yet
            if (billSince + Config.Price.Prolongation.TIME_SECONDS >= TimeUtil.currentTimeSeconds()) continue;

            // Successful withdraw
            if (Utils.tryWithdrawBilling(pair)) {
                region.setFlag(Flags.BILL_SINCE, TimeUtil.currentTimeSeconds());
            } else {
                plugin.getLogger().info("[Prolongation] Removing region " + world.getName() + ":" + region.getId());
                Utils.removeRegion(world, region);
            }
        }
    }
}
