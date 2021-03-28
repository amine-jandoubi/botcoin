package com.botcoin.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;


public class LOG {

    private static Logger logger = LogManager.getLogger("");

    public static void info(Object msg) {
        logger.info(msg);
        System.out.println(new Date() + " [" + Thread.currentThread().getName() + "] |  " + msg);
    }

    public static void debug(Object msg) {
        logger.debug(msg);
    }

    public static void error(Exception ex) {
        logger.error(ex.getMessage(), ex);
        ex.printStackTrace();
    }


    public static void consolePretty(String string) {
        info(JsonUtils.pretify(string));
    }
}
