package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.Decimal;
import com.botcoin.utils.JsonUtils;
import com.botcoin.utils.Logger;
import com.github.sbouclier.result.TickerInformationResult;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class WatchTrade {
    public static double PROFIT = 0.006;
    public static double LOSS = 0.05;

    private String pair;
    KrakenApi api = new KrakenApi();

    private double investment;
    private double takeProfit;
    private double stopLoss;

    private double askPrice;
    private double bidPrice;
    private double amount;

    public WatchTrade(String pair, double investment) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        api.setKey(API.getPublicKey());
        api.setSecret(API.getPrivateKey());

        this.pair = pair;
        this.investment = investment;
        String response = this.api.queryPrivate(KrakenApi.Method.TICKER, Map.of("pair", this.pair));

        TickerInformationResult.TickerInformation tickerInfos = JsonUtils.toObject(response, TickerInformationResult.class).getResult().get(this.pair);
        this.askPrice = tickerInfos.ask.price.doubleValue();
        this.bidPrice = tickerInfos.bid.price.doubleValue();

        this.takeProfit = Decimal.round6d(askPrice + askPrice * PROFIT);
        this.stopLoss = Decimal.round6d(bidPrice - bidPrice * LOSS);
        this.amount = Decimal.roundDouble(this.investment / this.askPrice);
    }

    public WatchTrade(String pair, double stopLoss, double takeProfit, double amount) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        api.setKey(API.getPublicKey());
        api.setSecret(API.getPrivateKey());

        this.pair = pair;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.amount = amount;
    }


    public void run() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        buy();
        double price;
        do {
            price = getBidPrice();
            Logger.info(this.pair + " | " + this.stopLoss + " < " + price + " < " + this.takeProfit);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (price < this.takeProfit && price > this.stopLoss);
        sell();
    }

    public void runWithoutBuy() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        double price;
        do {
            price = getBidPrice();
            Logger.info(this.stopLoss + " < " + price + " < " + this.takeProfit);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (price < this.takeProfit && price > this.stopLoss);
        sell();
    }

    public void buy() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        double price = getAskPrice();
        Logger.info("buy at: " + price);
        HashMap<String, String> values = new HashMap();
        values.put("pair", this.pair);
        values.put("type", "buy");
        values.put("ordertype", "market");
        values.put("volume", String.valueOf(this.amount));
        values.put("oflags", "fciq");
        Logger.info(values);
        if (Prices.PROD) {
            String response = this.api.queryPrivate(KrakenApi.Method.ADD_ORDER, values);
            Logger.consolePretty(response);
        }

    }

    public void sell() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        double price = getBidPrice();
        Logger.info("sell at: " + price);
        HashMap<String, String> values = new HashMap();
        values.put("pair", this.pair);
        values.put("type", "sell");
        values.put("ordertype", "limit");
        values.put("price", String.valueOf(price));
        values.put("volume", String.valueOf(this.amount));
        values.put("oflags", "fciq");
        Logger.info(values);
        if (Prices.PROD) {
            String response = this.api.queryPrivate(KrakenApi.Method.ADD_ORDER, values);
            Logger.consolePretty(response);
        }
    }

    public double getBidPrice() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String response = API.SINGLETON.queryPrivate(KrakenApi.Method.TICKER, Map.of("pair", this.pair));
        return JsonUtils.toObject(response, TickerInformationResult.class).getResult().get(this.pair).bid.price.doubleValue();
    }

    public double getAskPrice() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String response = API.SINGLETON.queryPrivate(KrakenApi.Method.TICKER, Map.of("pair", this.pair));
        return JsonUtils.toObject(response, TickerInformationResult.class).getResult().get(this.pair).ask.price.doubleValue();
    }

    @Override
    public String toString() {
        try {
            return "TopPerformerTrade{" +
                    "pair='" + pair + '\'' +
                    ", api=" + api +
                    ", investment=" + investment +
                    ", takeProfit=" + takeProfit +
                    ", stopLoss=" + stopLoss +
                    ", openPrice=" + askPrice +
                    ", amount=" + amount +
                    ", price=" + this.getBidPrice() +
                    '}';
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
