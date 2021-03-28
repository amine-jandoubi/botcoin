package com.botcoin.utils;

public class ThreadUtils {
    public static void sleepCatchingException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleep5CatchingException() {
        sleepCatchingException(5_000);
    }
}
