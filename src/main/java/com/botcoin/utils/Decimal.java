package com.botcoin.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Decimal {


    public static BigDecimal format(String format, double toROund) {
        return new BigDecimal(new DecimalFormat(format).format(toROund).replace(",", "."));
    }

    public static BigDecimal format(String format, BigDecimal toROund) {
        return new BigDecimal(new DecimalFormat(format).format(toROund).replace(",", "."));
    }

    public static String getFormat(double number) {
        int index = new StringBuilder(String.valueOf(number)).reverse().toString().indexOf(".");
        StringBuilder sb = new StringBuilder("#.");
        for (int i = 0; i < index; i++)
            sb.append("#");
        return sb.toString();
    }

    public static BigDecimal formatLike(double numnberFOrmat, double number) {
        return format(getFormat(numnberFOrmat), number);
    }


}
