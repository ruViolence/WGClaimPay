package ru.violence.wgclaimpay.config;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.FileConfiguration;
import ru.violence.coreapi.common.util.Check;
import ru.violence.wgclaimpay.WGClaimPayPlugin;

@UtilityClass
public class Config {
    public static int MIN_SIZE;

    public static void load(WGClaimPayPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        MIN_SIZE = config.getInt("min-size");

        Price.Claim.ENABLED = config.getBoolean("price.claim.enabled");
        Price.Claim.MIN = config.getInt("price.claim.min");
        Price.Claim.MAX = config.getInt("price.claim.max");
        Price.Claim.PER_BLOCK = config.getDouble("price.claim.per-block");
        Price.Claim.Exponent.DIVIDER = config.getInt("price.claim.exponent.divider");
        Price.Claim.Exponent.MIN = config.getDouble("price.claim.exponent.min");
        Price.Claim.Confirmation.ENABLED = config.getBoolean("price.claim.confirmation.enabled");
        Price.Claim.Confirmation.EXPIRE_AFTER = config.getInt("price.claim.confirmation.expire-after");

        if (Price.Claim.ENABLED) Check.moreThan(Price.Claim.PER_BLOCK, 0);
    }

    public static class Price {
        public static class Claim {
            public static boolean ENABLED;
            public static int MIN;
            public static int MAX;
            public static double PER_BLOCK;

            public static class Exponent {
                public static int DIVIDER;
                public static double MIN;
            }

            public static class Confirmation {
                public static boolean ENABLED;
                public static int EXPIRE_AFTER;
            }
        }
    }
}
