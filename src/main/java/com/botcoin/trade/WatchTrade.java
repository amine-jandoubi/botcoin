package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.*;
import com.github.sbouclier.result.OpenOrdersResult;
import com.github.sbouclier.result.TickerInformationResult;
import com.github.sbouclier.result.common.OrderDirection;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class WatchTrade {

    public static double PROFIT = 0.006;

    public static String LEVERAGE = "2:1";

    private static final int TRY_NUMBER_TO_BUY_LOWER = 10;
    private static final int TRY_NUMBER_TO_ENTER_MARKET = 5;

    private String pair;

    private double investment;

    private double buyPrice;

    private double takeProfit;

    private BigDecimal amount;


    public WatchTrade(String pair, double investment) {
        this.pair = pair;
        this.investment = investment;
    }

    public static BigDecimal takeProfit(double askPrice) {
        return Decimal.formatLike(askPrice, askPrice + askPrice * PROFIT);
    }

    public void run() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        boolean bought = buyWhenMaker();
        LOG.info(this.pair + " is bought at " + buyPrice + ", taking profit " + takeProfit);
        if (bought) {
            ThreadUtils.sleepCatchingException(5_000);
            sellWhenMaker();
        }

    }

    public double getPriceBetweenAskAndBid() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        double askPlusBid = 0.5 * (getAskPrice() + getBidPrice());
        return Decimal.formatLike(getAskPrice(), askPlusBid).doubleValue();
    }

    public boolean buyWhenMaker() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        for (int tryNumberToBuy = 0; tryNumberToBuy < TRY_NUMBER_TO_BUY_LOWER; tryNumberToBuy++) {
            double price = getPriceBetweenAskAndBid();
            this.takeProfit = takeProfit(price).doubleValue();
            this.amount = Decimal.format("#.######", this.investment / price);
            int tryNumberInOrderBook = TRY_NUMBER_TO_ENTER_MARKET;

            if (makeBuyOrder(price).contains("Insufficient funds"))
                return false;

            boolean notYetBought = true;
            while (isInOrderBook(OrderDirection.BUY).isEmpty() && notYetBought) {
                LOG.debug(this.pair + " Buy order of " + this.pair + " not yet in Order Book: " + this.getAskPrice() + " demand is:" + price);
                ThreadUtils.sleepCatchingException(1_000);
                LOG.info("Try number to buy " + this.pair + " " + tryNumberInOrderBook + "/" + TRY_NUMBER_TO_ENTER_MARKET);
                if (tryNumberInOrderBook-- < 1)
                    notYetBought = false;
            }

            while (isInOrderBook(OrderDirection.BUY).isPresent()) {
                LOG.debug(this.pair + " not yet executed: " + this.getAskPrice() + " demand is:" + price);
                ThreadUtils.sleepCatchingException(5_000);
            }
            this.buyPrice = price;
            return true;
        }
        return false;
    }

    public void sellWhenMaker() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        makeSellOrder(this.takeProfit);
        int tryNumber = TRY_NUMBER_TO_BUY_LOWER;
        while (isInOrderBook(OrderDirection.SELL).isEmpty()) {
            LOG.debug(this.pair + " SELL order of " + this.pair + " not yet in Order Book: " + this.getBidPrice() + " demand is:" + this.takeProfit);
            ThreadUtils.sleepCatchingException(5_000);
            LOG.info("Try number to sell " + tryNumber + "/" + TRY_NUMBER_TO_BUY_LOWER);
            if (tryNumber-- == 0)
                return;
        }
    }


    public static Optional<OpenOrdersResult.OpenOrder> isInOrderBook(OrderDirection orderDirection) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String response = API.SINGLETON.queryPrivate(KrakenApi.Method.OPEN_ORDERS);
        OpenOrdersResult openOrdersResult = JsonUtils.toObject(response, OpenOrdersResult.class);
        if (openOrdersResult.getResult() == null || openOrdersResult.getResult().open == null)
            return Optional.empty(); // tolerate errors
        return openOrdersResult.getResult().open.values().stream().filter(order -> order.description.orderDirection.equals(orderDirection)).findAny();
    }


    public String makeBuyOrder(double price) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOG.info("buy at: " + price);
        HashMap<String, String> values = new HashMap();
        values.put("pair", this.pair);
        values.put("type", "buy");
        values.put("price", String.valueOf(price));
        values.put("ordertype", "limit");
        values.put("volume", this.amount.toString());
        values.put("oflags", "fciq,post");
        values.put("leverage ", LEVERAGE);
        LOG.info(values);
        if (VM.isProd()) {
            String response = API.SINGLETON.queryPrivate(KrakenApi.Method.ADD_ORDER, values);
            LOG.consolePretty(response);
            return response;
        }
        return null;
    }

    public String makeSellOrder(double price) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOG.info("sell at: " + price);
        HashMap<String, String> values = new HashMap();
        values.put("pair", this.pair);
        values.put("type", "sell");
        values.put("price", String.valueOf(price));
        values.put("ordertype", "limit");
        values.put("volume", this.amount.toString());
        values.put("oflags", "fciq,post");
        LOG.info(values);
        if (VM.isProd()) {
            String response = API.SINGLETON.queryPrivate(KrakenApi.Method.ADD_ORDER, values);
            while (response.contains("Rate limit exceeded")) {
                ThreadUtils.sleepCatchingException(5_000);
                response = API.SINGLETON.queryPrivate(KrakenApi.Method.ADD_ORDER, values);
            }
            LOG.consolePretty(response);
            return response;
        }
        return null;
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
        return "WatchTrade{" +
                "pair='" + pair + '\'' +
                ", investment=" + investment +
                ", takeProfit=" + takeProfit +
                ", amount=" + amount +
                '}';
    }
}
