package com.botcoin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

public class VM {
    // API Specifications
    public static String API_PUBLIC_HOST = "api.public.host";
    public static String API_PUBLIC_KEY = "api.public.key";
    public static String API_PRIVATE_KEY = "api.private.key";

    // Moving average strategy specifications
    public static String MV_INTERVAL_IN_MINUTES = "mv.intervalInMinutes";
    public static String MV_MOVING_WINDOW = "mv.movingWindow";
    public static String MV_MINIMUM_OHLC_COUNT_AVG = "mv.minimumOhlcCountAvg";
    public static String MV_MINIMUM_CLOSE_TO_AVG_DIFF = "mv.minimumCloseToAVGDIff";

    // INVESTMENT Specifications
    public static String INVESTMENT_MIN_EUR = "investment.minimumEur";
    public static String INVESTMENT_PER_TRADE = "investment.perTrade";

    //ENVIRONMENT
    public static String ENV_NAME = "env.name";


    public static Properties PROPERTIES = new Properties();

    static {
        final File propertiesFile = new File(System.getProperty("config"));
        if (!propertiesFile.exists())
            throw new IllegalStateException("Config file should be given as an argument to the JVM : -Dconfig");
        try {
            PROPERTIES.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            LOG.error(e);
            throw new IllegalStateException("Config file should be given as an argument to the JVM : -Dconfig");
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.valueOf(PROPERTIES.getProperty(key)).intValue();
    }

    public static BigDecimal getDecimal(String key) {
        return new BigDecimal(PROPERTIES.getProperty(key));
    }

    public static boolean isProd() {
        return VM.get(VM.ENV_NAME) != null && VM.get(VM.ENV_NAME).equalsIgnoreCase("prod");
    }

    public static void main(String[] args) {
        LOG.info(VM.get(VM.API_PRIVATE_KEY));
    }
}
