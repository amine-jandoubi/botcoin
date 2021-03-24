package com.botcoin.utils;

public enum Pair {
    BITCOIN_EUR("XXBTZEUR"),
    USDT_EUR("USDTEUR"),
    DOT_EUR("DOTEUR"),
    KSM_EUR("KSMEUR");

    private String name;

    Pair(java.lang.String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
