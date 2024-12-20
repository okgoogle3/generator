package org.example.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utility {
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double generateDoubleInRange(int min, int max) {
        return Math.random() * (max - min) + min;
    }

    public static double generateRoundedDoubleInRange(int min, int max, int roundingOffset) {
        return round(generateDoubleInRange(min, max), roundingOffset);
    }
}
