package ru.violence.wgclaimpay.api.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RegionBillRemovedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final Set<ProtectedRegion> region;

    public RegionBillRemovedEvent(@NotNull World world, @NotNull Set<ProtectedRegion> region) {
        super(true);
        this.world = world;
        this.region = region;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public @NotNull Set<ProtectedRegion> getRegions() {
        return this.region;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
