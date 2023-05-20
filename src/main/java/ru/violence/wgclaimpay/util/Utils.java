package ru.violence.wgclaimpay.util;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.coreapi.common.api.util.TimeUtil;
import ru.violence.coreapi.common.message.LegacyPrinter;
import ru.violence.coreapi.common.user.User;
import ru.violence.coreapi.common.util.Check;
import ru.violence.coreapi.common.util.MathUtil;
import ru.violence.wgclaimpay.LangKeys;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.api.event.RegionBillRemovedEvent;
import ru.violence.wgclaimpay.config.Config;
import ru.violence.wgclaimpay.flag.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@UtilityClass
public class Utils {
    @Contract(pure = true)
    public int calcClaimPrice(int regionSize) {
        final double pricePerBlock = Config.Price.Claim.PER_BLOCK;
        double price = 0;

        if (Config.Price.Claim.Exponent.DIVIDER > 0) {
            final int exponentDivider = Config.Price.Claim.Exponent.DIVIDER;
            final double exponentMinPrice = Config.Price.Claim.Exponent.MIN;

            price = IntStream.range(0, regionSize)
                    .parallel()
                    .mapToDouble(i -> MathUtil.clamp(
                            pricePerBlock - Math.pow((double) i / exponentDivider, 3),
                            exponentMinPrice,
                            Double.MAX_VALUE))
                    .sum();
        } else {
            for (int i = 0; i < regionSize; i++) {
                price += pricePerBlock;
            }
        }

        int finalPrice = (int) price;

        return MathUtil.clamp(
                finalPrice,
                Config.Price.Claim.MIN > 0 ? Config.Price.Claim.MIN : 1,
                Config.Price.Claim.MAX > 0 ? Config.Price.Claim.MAX : Integer.MAX_VALUE);
    }

    @Contract(pure = true)
    public int calcBillPrice(ProtectedRegion region) {
        return calcBillPrice(region.volume());
    }

    @Contract(pure = true)
    public int calcBillPrice(int regionSize) {
        final double pricePerBlock = Config.Price.Prolongation.PER_BLOCK;
        double price = 0;

        if (Config.Price.Prolongation.Exponent.DIVIDER > 0) {
            final int exponentDivider = Config.Price.Prolongation.Exponent.DIVIDER;
            final double exponentMinPrice = Config.Price.Prolongation.Exponent.MIN;

            price = IntStream.range(0, regionSize)
                    .parallel()
                    .mapToDouble(i -> MathUtil.clamp(
                            pricePerBlock - Math.pow((double) i / exponentDivider, 3),
                            exponentMinPrice,
                            Double.MAX_VALUE))
                    .sum();
        } else {
            for (int i = 0; i < regionSize; i++) {
                price += pricePerBlock;
            }
        }

        int finalPrice = (int) price;

        return MathUtil.clamp(
                finalPrice,
                Config.Price.Prolongation.MIN > 0 ? Config.Price.Prolongation.MIN : 1,
                Config.Price.Prolongation.MAX > 0 ? Config.Price.Prolongation.MAX : Integer.MAX_VALUE);
    }

    @Contract(pure = true)
    public int calcBillPrice(@Nullable OfflinePlayer player, ProtectedRegion region, boolean split) {
        int clampedPrice = calcBillPrice(region.volume());

        if (split) {
            UUID playerUniqueId = Check.notNull(player).getUniqueId();
            int otherPayers = 0;

            for (OfflinePlayer payer : getActualPayers(region, clampedPrice)) {
                // Skip this player
                if (payer.getUniqueId().equals(playerUniqueId)) continue;

                otherPayers += 1;
            }

            if (otherPayers != 0) {
                clampedPrice /= otherPayers;
            }
        }

        return clampedPrice;
    }

    public boolean setBillPaying(@NotNull Player player, @NotNull ProtectedRegion region, boolean paying) {
        WorldGuardPlugin worldGuard = WGClaimPayPlugin.getInstance().getWorldGuard();
        DefaultDomain owners = region.getOwners();

        // Check that player contains in the owners
        boolean isOwner = owners.contains(worldGuard.wrapPlayer(player));

        Set<String> payers = getBillPayerNames(region);

        if (paying && isOwner) {
            payers.add(player.getName().toLowerCase(Locale.ROOT));
        } else {
            payers.remove(player.getName().toLowerCase(Locale.ROOT));
        }

        region.setFlag(Flags.BILL_PAYERS, payers.isEmpty() ? null : String.join(",", payers));

        return isOwner;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Set<String> getBillPayerNames(@NotNull ProtectedRegion region) {
        DefaultDomain owners = region.getOwners();
        Set<String> result = new HashSet<>();

        String raw = region.getFlag(Flags.BILL_PAYERS);
        if (raw != null) {
            for (String s : raw.split(",")) {
                String someName = s.trim();
                if (!someName.isEmpty() && owners.contains(someName) && Bukkit.getOfflinePlayer(someName) != null) {
                    result.add(someName);
                }
            }
        }

        return result;
    }

    @Contract(pure = true)
    public boolean tryWithdrawBilling(Pair<World, ProtectedRegion> pair) {
        World world = pair.getLeft();
        ProtectedRegion region = pair.getRight();

        int price = calcBillPrice(region);
        Set<OfflinePlayer> actualPayers = getActualPayers(region, price);
        if (actualPayers.isEmpty()) return false;

        for (OfflinePlayer payer : actualPayers) {
            int splitPrice = price / actualPayers.size();
            WGClaimPayPlugin.getInstance().getLogger().info("[Prolongation] Charged " + splitPrice + " money from the " + payer.getName() + " for the region " + world.getName() + ":" + region.getId());
            WGClaimPayPlugin.getInstance().getEconomy().withdrawPlayer(payer, splitPrice);
        }

        return true;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull Set<OfflinePlayer> getActualPayers(@NotNull ProtectedRegion region, int price) {
        List<Pair<OfflinePlayer, Double>> payers = new ArrayList<>();

        { // Map payer and get balances
            for (String payerName : getBillPayerNames(region)) {
                OfflinePlayer payer = Bukkit.getOfflinePlayer(payerName);
                if (payer == null) continue;

                double balance = WGClaimPayPlugin.getInstance().getEconomy().getBalance(payer);
                payers.add(Pair.of(payer, balance));
            }

            payers.sort(Comparator.comparingDouble(Pair::getRight));
        } // Map payer and get balances

        Set<OfflinePlayer> result = new HashSet<>();

        checkBalances:
        while (true) {
            if (payers.isEmpty()) return Collections.emptySet();

            for (Iterator<Pair<OfflinePlayer, Double>> iterator = payers.iterator(); iterator.hasNext(); ) {
                Pair<OfflinePlayer, Double> pair = iterator.next();

                OfflinePlayer payer = pair.getLeft();
                double balance = pair.getRight();

                int splitPrice = price / payers.size();

                if (balance < splitPrice) {
                    iterator.remove();
                    continue checkBalances;
                }

                result.add(payer);
            }

            break;
        }

        return result;
    }

    @Contract(pure = true)
    public boolean isPayingFor(@NotNull Player player, @NotNull ProtectedRegion region) {
        return getBillPayerNames(region).contains(player.getName().toLowerCase(Locale.ROOT));
    }


    @Contract(value = "_ -> new", pure = true)
    public @NotNull List<Pair<World, ProtectedRegion>> getAllPayingRegions(@NotNull Player player) {
        return getAllPayingRegions(player, getAllOwnedRegions(player));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull List<Pair<World, ProtectedRegion>> getAllPayingRegions(@NotNull Player player, @NotNull List<Pair<World, ProtectedRegion>> regions) {
        List<Pair<World, ProtectedRegion>> result = new ArrayList<>();

        for (Pair<World, ProtectedRegion> pair : regions) {
            if (isPayingFor(player, pair.getRight())) {
                result.add(pair);
            }
        }

        return result;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull List<Pair<World, ProtectedRegion>> getAllOwnedRegions(@NotNull Player player) {
        List<Pair<World, ProtectedRegion>> result = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            for (ProtectedRegion region : getOwnedRegions(player, world)) {
                result.add(Pair.of(world, region));
            }
        }

        return result;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull List<ProtectedRegion> getOwnedRegions(@NotNull Player player, @NotNull World world) {
        WorldGuardPlugin worldGuard = WGClaimPayPlugin.getInstance().getWorldGuard();
        RegionManager manager = worldGuard.getRegionContainer().get(world);
        if (manager == null) return Collections.emptyList();

        LocalPlayer wrappedPlayer = worldGuard.wrapPlayer(player);
        List<ProtectedRegion> result = new ArrayList<>();

        for (ProtectedRegion region : getAllRegions(world)) {
            if (region.getOwners().contains(wrappedPlayer)) {
                result.add(region);
            }
        }

        return result;
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull List<Pair<World, ProtectedRegion>> getAllRegions() {
        List<Pair<World, ProtectedRegion>> result = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            for (ProtectedRegion region : getAllRegions(world)) {
                result.add(Pair.of(world, region));
            }
        }

        return result;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull List<ProtectedRegion> getAllRegions(@NotNull World world) {
        RegionManager manager = WGClaimPayPlugin.getInstance().getWorldGuard().getRegionContainer().get(world);
        return manager != null ? new ArrayList<>(manager.getRegions().values()) : Collections.emptyList();
    }

    public boolean removeRegion(@NotNull World world, @NotNull ProtectedRegion region) {
        RegionManager manager = WGClaimPayPlugin.getInstance().getWorldGuard().getRegionContainer().get(world);
        if (manager == null) return false;

        Set<ProtectedRegion> removedRegions = manager.removeRegion(region.getId());
        if (removedRegions != null) new RegionBillRemovedEvent(world, removedRegions).callEvent();

        return true;
    }

    public @NotNull String renderNextBillTimeText(@NotNull Player player, @NotNull ProtectedRegion region) {
        User user = Check.notNull(BukkitHelper.getUser(player));

        Long flag = region.getFlag(Flags.BILL_SINCE);
        long billSinceTime = flag == null ? TimeUtil.currentTimeSeconds() : flag;
        long nextPayTime = billSinceTime + Config.Price.Prolongation.TIME_SECONDS;

        int days = 0;
        int hoursNormalized = 0;
        int minutesNormalized = 0;

        long currentTimeSeconds = TimeUtil.currentTimeSeconds();
        if (nextPayTime > currentTimeSeconds) {
            long remainingSeconds = nextPayTime - currentTimeSeconds;

            days = (int) TimeUtil.toDays(remainingSeconds);
            hoursNormalized = TimeUtil.toHoursNormalized(remainingSeconds);
            minutesNormalized = TimeUtil.toMinutesNormalized(remainingSeconds);
        }

        return LegacyPrinter.print(LangKeys.NEXT_BILL_TIME.setLang(user.getLanguage()).setArgs(
                days,
                hoursNormalized,
                minutesNormalized
        ));
    }
}
