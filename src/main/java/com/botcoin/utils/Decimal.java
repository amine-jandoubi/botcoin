package com.botcoin.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Decimal {

    public static BigDecimal round(BigDecimal toROund) {
        return new BigDecimal(new DecimalFormat("#.#").format(toROund));
    }

    public static BigDecimal round2(double toROund) {
        return new BigDecimal(new DecimalFormat("#.##").format(toROund));
    }

    public static BigDecimal round(double toROund) {
        return new BigDecimal(new DecimalFormat("#.#").format(toROund));
    }

    public static double roundDouble(double toROund) {
        return new BigDecimal(new DecimalFormat("#.####").format(toROund)).doubleValue();
    }


    public static BigDecimal round6(double toROund) {
        return new BigDecimal(new DecimalFormat("#.######").format(toROund));
    }

    public static double round6d(double toROund) {
        return new BigDecimal(new DecimalFormat("#.######").format(toROund)).doubleValue();
    }

}
