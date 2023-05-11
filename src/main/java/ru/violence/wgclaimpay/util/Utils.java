package ru.violence.wgclaimpay.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import ru.violence.coreapi.common.util.MathUtil;
import ru.violence.wgclaimpay.config.Config;

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
}
