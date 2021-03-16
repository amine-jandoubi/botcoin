package com.botcoin.secret;

public final class ApiKeys {
    private static final String API_BASE_URL = "https://api.kraken.com/0";
    private static final String API_PUBLIC_KEY = "uEN9HJFceqLUpJtLt05WpNE1Clqp3aiHho5e9xk4SRQa9diJCXOnpI0K"; //accessible on your Account page under Settings -> API Keys
    private static final String API_PRIVATE_KEY = "BrZ6m0DsVnPrxCc3GiT7bzmUhysOGyqJ7R3beEliIaZ1XZpTwDaKj7ZBPVqQS9YkMDlDvRna48lsXzF3vev9dg=="; //accessible on your Account page under Settings -> API Keys

    public static String getPublicKey() {
        return API_PUBLIC_KEY;
    }

    public static String getPrivateKey() {
        return API_PRIVATE_KEY;
    }

    public static String getAPI_BASE_URL() {
        return API_BASE_URL;
    }
}
