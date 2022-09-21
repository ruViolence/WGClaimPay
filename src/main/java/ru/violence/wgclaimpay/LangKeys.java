package ru.violence.wgclaimpay;

import lombok.experimental.UtilityClass;
import ru.violence.coreapi.common.message.MessageKey;

@UtilityClass
public class LangKeys {
    public final MessageKey REGION_TOO_SMALL = MessageKey.of("wgclaimpay.region-too-small");
    public final MessageKey NOT_ENOUGH_MONEY = MessageKey.of("wgclaimpay.not-enough-money");
    public final MessageKey AWAITING_CONFIRMATION = MessageKey.of("wgclaimpay.awaiting-confirmation");
    public final MessageKey PAID_FOR_REGION = MessageKey.of("wgclaimpay.paid-for-region");
}
