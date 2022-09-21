package ru.violence.wgclaimpay.util;

import lombok.experimental.UtilityClass;
import ru.violence.coreapi.common.util.MathUtil;
import ru.violence.wgclaimpay.WGClaimPayPlugin;

import java.util.stream.IntStream;

@UtilityClass
public class Utils {
    public int calcPrice(WGClaimPayPlugin plugin, int regionSize) {
        final double pricePerBlock = plugin.getPricePerBlock();
        double price = 0;

        if (plugin.getExponentDivider() > 0) {
            final int exponentDivider = plugin.getExponentDivider();
            final double exponentMinPrice = plugin.getExponentMinPrice();

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
                plugin.getMinPrice() > 0 ? plugin.getMinPrice() : 1,
                plugin.getMaxPrice() > 0 ? plugin.getMaxPrice() : Integer.MAX_VALUE);
    }
}
