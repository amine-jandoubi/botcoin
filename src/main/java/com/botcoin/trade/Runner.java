package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.LOG;
import com.botcoin.utils.ThreadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Runner {

    public static boolean PROD = true;
    static double total = 70;
    static int invests = 1;
    static double investment = total / invests;


    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOG.info("########### [LEMSATTEK] restarted at" + new Date() + " ###########");
        while (Files.exists(Path.of("C:\\TRADING\\run"))) {
            try {

                MovingAverage.TickerInfoDTO ticker = MovingAverage.getBest();

                if (ticker == null) {
                    LOG.info("Retrying to find another ticker in 1 minute...");
                    ThreadUtils.sleepCatchingException(60_000);
                    continue;
                }

                WatchTrade watch = new WatchTrade(ticker.pair, investment);
                watch.run();

            } catch (Exception ex) {
                LOG.error(ex);
                ThreadUtils.sleepCatchingException(60_000);
                // sleep 1 minute if a connection refused error is thrown.
            }

            while (API.getBalanceInCcy("ZEUR") < 20) {
                LOG.info("balance < 20 waiting 1 minute to replay...");
                ThreadUtils.sleepCatchingException(60_000);
            }
        }
    }
}
