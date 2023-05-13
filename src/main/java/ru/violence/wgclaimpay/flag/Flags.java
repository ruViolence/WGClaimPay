package ru.violence.wgclaimpay.flag;

import com.sk89q.worldguard.protection.flags.StringFlag;
import lombok.experimental.UtilityClass;
import ru.violence.wgclaimpay.util.LongFlag;

@UtilityClass
public class Flags {
    public final StringFlag BILL_PAYERS = new StringFlag("bill-payers");
    public final LongFlag BILL_SINCE = new LongFlag("bill-since");
}
