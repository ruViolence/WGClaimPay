package ru.violence.wgclaimpay;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.violence.coreapi.common.util.Check;
import ru.violence.wgclaimpay.command.WGClaimPayCommand;
import ru.violence.wgclaimpay.config.Config;
import ru.violence.wgclaimpay.listener.RegionClaimPayListener;
import ru.violence.wgclaimpay.listener.RegionSizeCheckListener;

public class WGClaimPayPlugin extends JavaPlugin {
    private @Getter WorldEditPlugin worldEdit;
    private @Getter Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        hookVault();
        hookWorldEdit();

        if (Config.MIN_SIZE > 0) {
            getServer().getPluginManager().registerEvents(new RegionSizeCheckListener(this), this);
        }
        if (Config.Price.Claim.ENABLED) {
            getServer().getPluginManager().registerEvents(new RegionClaimPayListener(this), this);
        }
        getServer().getPluginCommand("wgclaimpay").setExecutor(new WGClaimPayCommand(this));
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
}
