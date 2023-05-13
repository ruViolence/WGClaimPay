package ru.violence.wgclaimpay.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.violence.wgclaimpay.WGClaimPayPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class RateLimit {
    private final Map<UUID, BukkitTask> RATE_LIMIT = new HashMap<>();

    public boolean check(@NotNull UUID playerUniqueId) {
        synchronized (RATE_LIMIT) {
            if (RATE_LIMIT.containsKey(playerUniqueId)) return true;
            RATE_LIMIT.put(playerUniqueId, Bukkit.getScheduler().runTaskLater(WGClaimPayPlugin.getInstance(), () -> {
                synchronized (RATE_LIMIT) {
                    RATE_LIMIT.remove(playerUniqueId);
                }
            }, 20 /* 1 second */));
            return false;
        }
    }
}
