package com.botcoin.utils;

public class ThreadUtils {
    public static void sleepCatchingException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
