package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.Decimal;
import com.botcoin.utils.Logger;
import com.botcoin.utils.Pair;
import com.botcoin.utils.JsonUtils;
import com.github.sbouclier.result.OrderBookResult;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Map;

public class MarketDirection {

    public static int ITERATIONS = 30;
    public static int ITERATIONS_THRESHOLD = 20;
    public static int PERCENTAGE_THRESHOLD = 10;

    private Pair pair;
    KrakenApi api = new KrakenApi();

    public MarketDirection(Pair pair) {
        api.setKey(API.getPublicKey());
        api.setSecret(API.getPrivateKey());

        this.pair = pair;
    }

    public boolean isGoingUp() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Logger.info("Getting market direction for " + this.pair.getName() + "...");
        int numberOfGOingUp = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            AsksBidsInfo asksBidsInfo = this.getAsksBidsVolume();
            if (asksBidsInfo.getDiffPercentage() > PERCENTAGE_THRESHOLD)
                numberOfGOingUp++;
        }
        boolean isGoingUp = numberOfGOingUp > ITERATIONS_THRESHOLD;
        if (isGoingUp)
            Logger.info(this.pair.getName() + " is going Up");
        else
            Logger.info(this.pair.getName() + " is going Down");
        return isGoingUp;
    }

    public OrderBookResult.OrderBook getOrderBook() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        return JsonUtils.toObject(
                api.queryPrivate(KrakenApi.Method.DEPTH, Map.of("pair", this.pair.getName())), OrderBookResult.class).getResult().get(this.pair.getName());
    }

    public AsksBidsInfo getAsksBidsVolume() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        BigDecimal asksVolume = new BigDecimal(0);
        BigDecimal bidsVolume = new BigDecimal(0);

        OrderBookResult.OrderBook orderBook = this.getOrderBook();
        for (OrderBookResult.Market market : orderBook.asks) {
            asksVolume = asksVolume.add(market.volume);
        }

        for (OrderBookResult.Market market : orderBook.bids) {
            bidsVolume = bidsVolume.add(market.volume);
        }

        return new AsksBidsInfo(asksVolume, bidsVolume);

    }

    public AsksBidsInfo getAskBidAvg() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        OrderBookResult.OrderBook orderBook = this.getOrderBook();
        double asksVolume = 0;
        double bidsVolume = 0;
        double asksAverage = 0;
        double bidsAverage = 0;

        for (OrderBookResult.Market market : orderBook.asks) {
            asksVolume = asksVolume + market.volume.doubleValue();
            asksAverage = asksAverage + market.volume.doubleValue() * market.price.doubleValue();
        }

        for (OrderBookResult.Market market : orderBook.bids) {
            bidsVolume = bidsVolume + market.volume.doubleValue();
            bidsAverage = bidsAverage + market.volume.doubleValue() * market.price.doubleValue();
        }
        AsksBidsInfo info = new AsksBidsInfo(new BigDecimal(bidsAverage / bidsVolume), new BigDecimal(asksAverage / asksVolume));
        Logger.info(info);
        return info;

    }

    public static class AsksBidsInfo {
        private BigDecimal asksInfo;
        private BigDecimal bidsInfo;

        public AsksBidsInfo(BigDecimal asksVolume, BigDecimal bidVolume) {
            this.asksInfo = Decimal.round(asksVolume);
            this.bidsInfo = Decimal.round(bidVolume);
        }

        public BigDecimal getAsksInfo() {
            return asksInfo;
        }

        public BigDecimal getBidsInfo() {
            return bidsInfo;
        }

        public double getDiffPercentage() {
            return Double.parseDouble(
                    new DecimalFormat("#.#####").format(
                            100 * (this.asksInfo.doubleValue() - this.bidsInfo.doubleValue()) / this.bidsInfo.doubleValue()
                    )
            );
        }


        @Override
        public String toString() {
            return "AsksBidsVolume{" +
                    "asksVolume=" + asksInfo +
                    ", bidVolume=" + bidsInfo +
                    ", diff percentage" + this.getDiffPercentage() +
                    '}';
        }
    }
}
