package ru.violence.wgclaimpay.api.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegionBillExpireEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final ProtectedRegion region;
    private boolean cancel;

    public RegionBillExpireEvent(@NotNull World world, @NotNull ProtectedRegion region) {
        super(true);
        this.world = world;
        this.region = region;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public @NotNull ProtectedRegion getRegion() {
        return this.region;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
