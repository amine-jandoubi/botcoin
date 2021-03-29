package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.LOG;
import com.botcoin.utils.ThreadUtils;
import com.botcoin.utils.VM;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Runner {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOG.info("########### [LEMSATTEK] restarted at " + new Date() + " ###########");
        while (true) {
            try {

                MovingAverageStrategy.TickerInfoDTO ticker = MovingAverageStrategy.getBest();

                if (ticker == null) {
                    LOG.info("Retrying to find another ticker in 1 minute...");
                    ThreadUtils.sleepCatchingException(60_000);
                    continue;
                }

                WatchTrade watch = new WatchTrade(ticker.pair, VM.getInt(VM.INVESTMENT_PER_TRADE));
                watch.run();

            } catch (Exception ex) {
                LOG.error(ex);
                ThreadUtils.sleepCatchingException(60_000);
                // sleep 1 minute if a connection refused error is thrown.
            }

            while (API.getBalanceInCcy("ZEUR") < VM.getInt(VM.INVESTMENT_MIN_EUR)) {
                LOG.info("Balance in EUR is lower than " + VM.getInt(VM.INVESTMENT_MIN_EUR) + " EUR, waiting " + VM.getInt(VM.INVESTMENT_TIME_TO_WAIT_WHEN_MIN_EUR) + " minutes to replay...");
                ThreadUtils.sleepCatchingException(VM.getInt(VM.INVESTMENT_TIME_TO_WAIT_WHEN_MIN_EUR) * 60_000);
            }
        }
    }
}
