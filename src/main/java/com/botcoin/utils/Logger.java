package com.botcoin.utils;

import java.util.Date;

public class Logger {

    public static void info(Object msg) {
        System.out.println(new Date() + " [" + Thread.currentThread().getName() + "] |  " + msg);
    }

    public static void consolePretty(String string) {
        info(JsonUtils.pretify(string));
    }
}
