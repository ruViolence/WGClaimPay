package ru.violence.wgclaimpay.menu;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Button;
import ru.violence.coreapi.bukkit.api.menu.button.Buttons;
import ru.violence.coreapi.bukkit.api.util.MessageUtil;
import ru.violence.coreapi.bukkit.util.ItemBuilder;
import ru.violence.wgclaimpay.LangKeys;
import ru.violence.wgclaimpay.WGClaimPayPlugin;
import ru.violence.wgclaimpay.util.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BillsMenu extends Menu {
    private static final int MAX_ELEMENTS_ON_PAGE = MenuHelper.getInsideBorder(6).length;
    private final int page;
    private final List<Pair<World, ProtectedRegion>> ownedRegions;
    private boolean lockButtons = false;

    private BillsMenu(Player player, int page, @NotNull List<Pair<World, ProtectedRegion>> ownedRegions) {
        super(player, MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_TITLE.setArgs(page + 1, getPagesAmount(ownedRegions))), 54);
        this.page = page;
        this.ownedRegions = ownedRegions;
    }

    public static void openAsync(@NotNull Player player, int page) {
        Runnable runnable = () -> {
            BillsMenu menu = new BillsMenu(player, page, Utils.getAllOwnedRegions(player));
            menu.fill();
            Bukkit.getScheduler().runTask(WGClaimPayPlugin.getInstance(), menu::open);
        };

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void onInitialize() {
        // NOOP because of fill()
    }

    @Override
    public void initialize() {
        // NOOP
    }

    private void fill() {
        List<Pair<World, ProtectedRegion>> regions = ownedRegions
                .stream()
                .sorted(Comparator.comparing(pair -> pair.getRight().getId()))
                .collect(Collectors.toList());

        int[] pageSlots = MenuHelper.getInsideBorder(this);
        int offset = getOffset(page);
        for (int i = 0; i < pageSlots.length; i++) {
            int offsetIndex = i + offset;
            Pair<World, ProtectedRegion> element = regions.size() > offsetIndex ? regions.get(offsetIndex) : null;
            int slot = pageSlots[i];
            if (element == null) {
                setButton(slot, null);
                continue;
            }
            setButton(slot, getRegionButton(element));
        }

        if (hasElementsForPage(ownedRegions, page - 1)) {
            setButton(48, Buttons.makeSimple(new ItemBuilder(Material.PAPER).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_BUTTON_PAGE_PREV)), (p, m, b, c) -> {
                if (checkLockButtons()) return;
                openAsync(player, page - 1);
            }));
        }
        if (hasElementsForPage(ownedRegions, page + 1)) {
            setButton(50, Buttons.makeSimple(new ItemBuilder(Material.PAPER).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_BUTTON_PAGE_NEXT)), (p, m, b, c) -> {
                if (checkLockButtons()) return;
                openAsync(player, page + 1);
            }));
        }

        setButton(4, getTotalButton());

        MenuHelper.fillBorder(this);
    }

    @NotNull
    private Button getTotalButton() {
        List<Pair<World, ProtectedRegion>> payingRegions = Utils.getAllPayingRegions(player, ownedRegions);

        int totalSplit = 0;
        int totalMax = 0;

        for (Pair<World, ProtectedRegion> pair : payingRegions) {
            totalSplit += Utils.calcBillPrice(player, pair.getRight(), true);
            totalMax += Utils.calcBillPrice(player, pair.getRight(), false);
        }

        return Buttons.makeDummy(new ItemBuilder(Material.GOLD_INGOT).setDescription(
                MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_BUTTON_TOTAL.setArgs(
                        payingRegions.size(),
                        totalSplit,
                        totalMax
                ))
        ));
    }

    private @NotNull Button getRegionButton(Pair<World, ProtectedRegion> pair) {
        return Utils.isPayingFor(player, pair.getRight()) ? getButtonEnabled(pair) : getButtonDisabled(pair);
    }

    private @NotNull Button getButtonEnabled(Pair<World, ProtectedRegion> pair) {
        World world = pair.getLeft();
        ProtectedRegion region = pair.getRight();

        return Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 1).setDescription(
                MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_BUTTON_ENABLED.setArgs(
                        world.getName(),
                        region.getId(),
                        Utils.calcBillPrice(player, region, true),
                        Utils.calcBillPrice(player, region, false)
                ))), (p, m, b, c) -> {
            if (checkLockButtons()) return;
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), () -> {
                Utils.setBillPaying(player, pair.getRight(), false);
                openAsync(player, page);
            });
        });
    }

    private @NotNull Button getButtonDisabled(Pair<World, ProtectedRegion> pair) {
        World world = pair.getLeft();
        ProtectedRegion region = pair.getRight();

        return Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 8).setDescription(
                MessageUtil.renderLegacy(player, LangKeys.MENU_BILLS_BUTTON_DISABLED.setArgs(
                        world.getName(),
                        region.getId(),
                        Utils.calcBillPrice(player, region, true),
                        Utils.calcBillPrice(player, region, false)
                ))), (p, m, b, c) -> {
            if (checkLockButtons()) return;
            Bukkit.getScheduler().runTaskAsynchronously(WGClaimPayPlugin.getInstance(), () -> {
                Utils.setBillPaying(player, pair.getRight(), true);
                openAsync(player, page);
            });
        });
    }

    private static int getOffset(int page) {
        return page * MAX_ELEMENTS_ON_PAGE;
    }

    private static boolean hasElementsForPage(@NotNull List<Pair<World, ProtectedRegion>> ownedRegions, int page) {
        return page >= 0 && getOffset(page) < ownedRegions.size();
    }

    private static int getPagesAmount(@NotNull List<Pair<World, ProtectedRegion>> ownedRegions) {
        return ownedRegions.size() / MAX_ELEMENTS_ON_PAGE + 1;
    }

    private boolean checkLockButtons() {
        if (lockButtons) return true;
        lockButtons = true;
        return false;
    }
}
