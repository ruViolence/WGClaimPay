package ru.violence.wgclaimpay;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.violence.coreapi.common.util.Check;
import ru.violence.wgclaimpay.command.WGClaimPayCommand;
import ru.violence.wgclaimpay.config.Config;
import ru.violence.wgclaimpay.flag.Flags;
import ru.violence.wgclaimpay.hook.ShopChestHook;
import ru.violence.wgclaimpay.listener.RegionBillAddListener;
import ru.violence.wgclaimpay.listener.RegionClaimPayListener;
import ru.violence.wgclaimpay.listener.RegionSizeCheckListener;
import ru.violence.wgclaimpay.task.ProlongationTask;

public class WGClaimPayPlugin extends JavaPlugin {
    private static WGClaimPayPlugin instance;
    private @Getter WorldEditPlugin worldEdit;
    private @Getter WorldGuardPlugin worldGuard;
    private @Getter Economy economy;

    public static WGClaimPayPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        hookWorldEdit();
        hookWorldGuard();

        worldGuard.getFlagRegistry().register(Flags.BILL_PAYERS);
        worldGuard.getFlagRegistry().register(Flags.BILL_SINCE);
        worldGuard.getFlagRegistry().register(Flags.BILL_BYPASS);
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        hookVault();

        if (Bukkit.getPluginManager().isPluginEnabled("ShopChest")) ShopChestHook.init(this);

        if (Config.MIN_SIZE > 0) {
            getServer().getPluginManager().registerEvents(new RegionSizeCheckListener(this), this);
        }
        if (Config.Price.Claim.ENABLED) {
            getServer().getPluginManager().registerEvents(new RegionClaimPayListener(this), this);
        }
        if (Config.Price.Prolongation.ENABLED) {
            getServer().getPluginManager().registerEvents(new RegionBillAddListener(this), this);
            new ProlongationTask(this).runTaskTimer(this, 1, 10 * 60 * 20);
        }
        getServer().getPluginCommand("wgclaimpay").setExecutor(new WGClaimPayCommand(this));
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Config.load(this);
    }

    private void hookVault() {
        hook:
        try {
            if (getServer().getPluginManager().getPlugin("Vault") == null) break hook;

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) break hook;

            economy = Check.notNull(rsp.getProvider());
            return;
        } catch (Exception ignored) {}

        throw new RuntimeException("Vault plugin not found");
    }

    private void hookWorldEdit() {
        try {
            worldEdit = (WorldEditPlugin) Check.notNull(getServer().getPluginManager().getPlugin("WorldEdit"));
        } catch (Exception e) {
            throw new RuntimeException("WorldEdit plugin not found");
        }
    }

    private void hookWorldGuard() {
        try {
            worldGuard = (WorldGuardPlugin) Check.notNull(getServer().getPluginManager().getPlugin("WorldGuard"));
        } catch (Exception e) {
            throw new RuntimeException("WorldGuard plugin not found");
        }
    }
}
