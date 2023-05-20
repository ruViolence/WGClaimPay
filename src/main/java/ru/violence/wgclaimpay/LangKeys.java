package ru.violence.wgclaimpay;

import lombok.experimental.UtilityClass;
import ru.violence.coreapi.common.message.MessageKey;

@UtilityClass
public class LangKeys {
    public final MessageKey REGION_TOO_SMALL = MessageKey.of("wgclaimpay.region-too-small");
    public final MessageKey NOT_ENOUGH_MONEY = MessageKey.of("wgclaimpay.not-enough-money");
    public final MessageKey AWAITING_CONFIRMATION = MessageKey.of("wgclaimpay.awaiting-confirmation");
    public final MessageKey PAID_FOR_REGION = MessageKey.of("wgclaimpay.paid-for-region");
    public final MessageKey NEXT_BILL_TIME = MessageKey.of("wgclaimpay.next-bill-time");

    public final MessageKey MENU_BILLS_TITLE = MessageKey.of("wgclaimpay.menu.bills.title");
    public final MessageKey MENU_BILLS_BUTTON_TOTAL = MessageKey.of("wgclaimpay.menu.bills.button.total");
    public final MessageKey MENU_BILLS_BUTTON_ENABLED = MessageKey.of("wgclaimpay.menu.bills.button.enabled");
    public final MessageKey MENU_BILLS_BUTTON_DISABLED = MessageKey.of("wgclaimpay.menu.bills.button.disabled");
    public final MessageKey MENU_BILLS_BUTTON_PAGE_NEXT = MessageKey.of("wgclaimpay.menu.bills.button.page-next");
    public final MessageKey MENU_BILLS_BUTTON_PAGE_PREV = MessageKey.of("wgclaimpay.menu.bills.button.page-prev");
}
