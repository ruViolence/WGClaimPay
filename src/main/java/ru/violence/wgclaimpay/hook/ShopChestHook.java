package ru.violence.wgclaimpay.hook;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.api.event.RegionBillRemovedEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShopChestHook implements Listener {
    private ShopChestHook() {}

    public static void init(WGClaimPayPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new ShopChestHook(), plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRegionExpire(RegionBillRemovedEvent event) {
        removeShops(event.getWorld(), event.getRegions());
    }

    private static void removeShops(@NotNull World world, @NotNull Collection<ProtectedRegion> regions) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ShopChest")) return;

        Database shopDatabase = ShopChest.getInstance().getShopDatabase();
        ShopUtils shopUtils = ShopChest.getInstance().getShopUtils();

        for (ProtectedRegion region : regions) {
            int chunkXMin = region.getMinimumPoint().getBlockX() >> 4;
            int chunkZMin = region.getMinimumPoint().getBlockZ() >> 4;

            int chunkXMax = region.getMaximumPoint().getBlockX() >> 4;
            int chunkZMax = region.getMaximumPoint().getBlockZ() >> 4;

            Set<Chunk> chunks = new HashSet<>();

            for (int chunkX = chunkXMin; chunkX <= chunkXMax; chunkX++) {
                for (int chunkZ = chunkZMin; chunkZ <= chunkZMax; chunkZ++) {
                    if (!world.isChunkGenerated(chunkX, chunkZ)) continue;
                    chunks.add(world.getChunkAt(chunkX, chunkZ));
                }
            }

            shopDatabase.getShopsInChunks(chunks.toArray(new Chunk[0]), new Callback<Collection<Shop>>(ShopChest.getInstance()) {
                @Override
                public void onResult(Collection<Shop> shops) {
                    for (Shop shop : shops) {
                        Location location = shop.getLocation();

                        if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                            Shop loadedShop = shopUtils.getShop(location);
                            if (loadedShop != null) shop = loadedShop;

                            shopUtils.removeShop(shop, true);
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
