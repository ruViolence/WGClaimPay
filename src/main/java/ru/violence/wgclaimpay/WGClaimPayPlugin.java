package ru.violence.wgclaimpay;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.violence.coreapi.common.util.Check;
import ru.violence.wgclaimpay.command.WGClaimPayCommand;
import ru.violence.wgclaimpay.listener.RegionPreClaimListener;

public class WGClaimPayPlugin extends JavaPlugin {
    private @Getter WorldEditPlugin worldEdit;
    private @Getter Economy economy;
    // Config values
    private @Getter int minSize;
    private @Getter int minPrice;
    private @Getter int maxPrice;
    private @Getter double pricePerBlock;
    private @Getter int exponentDivider;
    private @Getter double exponentMinPrice;
    private @Getter boolean confirmation;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        hookVault();
        hookWorldEdit();

        getServer().getPluginManager().registerEvents(new RegionPreClaimListener(this), this);
        getServer().getPluginCommand("wgclaimpay").setExecutor(new WGClaimPayCommand(this));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.minSize = getConfig().getInt("min-size");
        this.minPrice = getConfig().getInt("min-price");
        this.maxPrice = getConfig().getInt("max-price");
        this.pricePerBlock = getConfig().getDouble("price-per-block");
        this.exponentDivider = getConfig().getInt("exponent.divider");
        this.exponentMinPrice = getConfig().getDouble("exponent.min-price");
        this.confirmation = getConfig().getBoolean("confirmation");
        Check.moreThan(pricePerBlock, 0);
    }

    private void hookVault() {
        hook:
        {
            if (getServer().getPluginManager().getPlugin("Vault") == null) break hook;

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) break hook;

            economy = rsp.getProvider();
            return;
        }

        throw new RuntimeException("Vault plugin not found");
    }

    private void hookWorldEdit() {
        try {
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        } catch (Exception e) {
            throw new RuntimeException("WorldEdit plugin not found");
        }
    }
}
