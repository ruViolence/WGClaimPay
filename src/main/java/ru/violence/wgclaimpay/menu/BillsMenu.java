package ru.violence.wgclaimpay.menu;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Button;
import ru.violence.coreapi.bukkit.api.menu.button.builder.ButtonBuilder;
import ru.violence.coreapi.bukkit.api.menu.listener.ClickListener;
import ru.violence.coreapi.bukkit.api.util.ItemBuilder;
import ru.violence.coreapi.bukkit.api.util.RendererHelper;
import ru.violence.wgclaimpay.LangKeys;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.util.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BillsMenu {
    private final int MAX_ELEMENTS_ON_PAGE = MenuHelper.getInsideBorder(6).length;

    public void openAsync(@NotNull Player player, int page) {
        Runnable runnable = () -> {
            List<Pair<World, ProtectedRegion>> ownedRegions = Utils.getAllOwnedRegions(player);

            Menu menu = Menu.newBuilder(WGClaimPayPlugin.getInstance())
                    .title(RendererHelper.legacy(player, LangKeys.MENU_BILLS_TITLE.setArgs(page + 1, getPagesAmount(ownedRegions))))
                    .size(54)
                    .clickListener(ClickListener.cancel())
                    .build();
            fill(menu, player, page, ownedRegions);
            Bukkit.getScheduler().runTask(WGClaimPayPlugin.getInstance(), () -> menu.open(player));
        };

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), runnable);
        } else {
            runnable.run();
        }
    }

    private void fill(@NotNull Menu menu, @NotNull Player player, int page, @NotNull List<Pair<World, ProtectedRegion>> ownedRegions) {
        List<Pair<World, ProtectedRegion>> regions = ownedRegions
                .stream()
                .sorted(Comparator.comparing(pair -> pair.getRight().getId()))
                .collect(Collectors.toList());

        int[] pageSlots = MenuHelper.getInsideBorder(menu);
        int offset = getOffset(page);
        for (int i = 0; i < pageSlots.length; i++) {
            int offsetIndex = i + offset;
            Pair<World, ProtectedRegion> element = regions.size() > offsetIndex ? regions.get(offsetIndex) : null;
            int slot = pageSlots[i];
            if (element == null) {
                menu.setButton(slot, (Button) null);
                continue;
            }
            menu.setButton(slot, getRegionButton(player, page, element));
        }

        if (hasElementsForPage(ownedRegions, page - 1)) {
            menu.setButton(48, Button.simple(new ItemBuilder(Material.PAPER).display(RendererHelper.legacy(player, LangKeys.MENU_BILLS_BUTTON_PAGE_PREV)).build()).action(clickEvent -> {
                clickEvent.getMenu().setWaiting(true);
                openAsync(player, page - 1);
            }));
        }
        if (hasElementsForPage(ownedRegions, page + 1)) {
            menu.setButton(50, Button.simple(new ItemBuilder(Material.PAPER).display(RendererHelper.legacy(player, LangKeys.MENU_BILLS_BUTTON_PAGE_NEXT)).build()).action(clickEvent -> {
                clickEvent.getMenu().setWaiting(true);
                openAsync(player, page + 1);
            }));
        }

        menu.setButton(4, getTotalButton(player, ownedRegions));

        MenuHelper.fillBorder(menu);
    }

    @NotNull
    private ButtonBuilder getTotalButton(@NotNull Player player, @NotNull List<Pair<World, ProtectedRegion>> ownedRegions) {
        List<Pair<World, ProtectedRegion>> payingRegions = Utils.getAllPayingRegions(player, ownedRegions);

        int totalSplit = 0;
        int totalMax = 0;

        for (Pair<World, ProtectedRegion> pair : payingRegions) {
            totalSplit += Utils.calcBillPrice(player, pair.getRight(), true);
            totalMax += Utils.calcBillPrice(player, pair.getRight(), false);
        }

        return Button.simple(new ItemBuilder(Material.GOLD_INGOT).display(
                RendererHelper.legacy(player, LangKeys.MENU_BILLS_BUTTON_TOTAL.setArgs(
                        payingRegions.size(),
                        totalSplit,
                        totalMax
                ))).build());
    }

    private @NotNull ButtonBuilder getRegionButton(@NotNull Player player, int page, Pair<World, ProtectedRegion> pair) {
        return Utils.isPayingFor(player, pair.getRight()) ? getButtonEnabled(player, page, pair) : getButtonDisabled(player, page, pair);
    }

    private @NotNull ButtonBuilder getButtonEnabled(@NotNull Player player, int page, Pair<World, ProtectedRegion> pair) {
        World world = pair.getLeft();
        ProtectedRegion region = pair.getRight();

        return Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 1).display(
                RendererHelper.legacy(player, LangKeys.MENU_BILLS_BUTTON_ENABLED.setArgs(
                        world.getName(),
                        region.getId(),
                        Utils.calcBillPrice(player, region, true),
                        Utils.calcBillPrice(player, region, false),
                        Utils.renderNextBillTimeText(player, region)
                ))).build()).action(clickEvent -> {
            clickEvent.getMenu().setWaiting(true);
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), () -> {
                Utils.setBillPaying(player, pair.getRight(), false);
                openAsync(player, page);
            });
        });
    }

    private @NotNull ButtonBuilder getButtonDisabled(@NotNull Player player, int page, Pair<World, ProtectedRegion> pair) {
        World world = pair.getLeft();
        ProtectedRegion region = pair.getRight();

        return Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 8).display(
                RendererHelper.legacy(player, LangKeys.MENU_BILLS_BUTTON_DISABLED.setArgs(
                        world.getName(),
                        region.getId(),
                        Utils.calcBillPrice(player, region, true),
                        Utils.calcBillPrice(player, region, false),
                        Utils.renderNextBillTimeText(player, region)
                ))).build()).action(clickEvent -> {
            clickEvent.getMenu().setWaiting(true);
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), () -> {
                Utils.setBillPaying(player, pair.getRight(), true);
                openAsync(player, page);
            });
        });
    }

    private int getOffset(int page) {
        return page * MAX_ELEMENTS_ON_PAGE;
    }

    private boolean hasElementsForPage(@NotNull List<Pair<World, ProtectedRegion>> ownedRegions, int page) {
        return page >= 0 && getOffset(page) < ownedRegions.size();
    }

    private int getPagesAmount(@NotNull List<Pair<World, ProtectedRegion>> ownedRegions) {
        return ownedRegions.size() / MAX_ELEMENTS_ON_PAGE + 1;
    }
}
