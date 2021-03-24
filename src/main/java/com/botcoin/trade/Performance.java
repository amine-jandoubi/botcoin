package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.JsonUtils;
import com.botcoin.utils.Logger;
import com.botcoin.utils.ThreadUtils;
import com.github.sbouclier.result.AssetPairsResult;
import com.github.sbouclier.result.TickerInformationResult;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class Performance {
    private static int ITERATIONS = 2;
    Map<String, Trade> map = new HashMap<>();

    public static List<Trade> calculatePers() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Map<String, Trade> map = new HashMap<>();
        String response1 = API.SINGLETON.queryPrivate(KrakenApi.Method.ASSET_PAIRS);
        AssetPairsResult pairs = JsonUtils.toObject(response1, AssetPairsResult.class);
        List<String> euroCurrencies = pairs.getResult().keySet().stream().filter(name -> name.endsWith("EUR")).collect(Collectors.toList());
        StringJoiner joiner = new StringJoiner(",");
        for (String s : euroCurrencies)
            joiner.add(s);

        List<Trade> trades = new ArrayList<>();

        for (int i = 0; i < ITERATIONS; i++) {
            String response2 = API.SINGLETON.queryPublic(KrakenApi.Method.TICKER, Map.of("pair", joiner.toString()));
            TickerInformationResult tickers = JsonUtils.toObject(response2, TickerInformationResult.class);
            tickers.getResult().forEach((ticker, infos) -> {
                if (map.containsKey(ticker)) {
                    Trade storedTrade = map.get(ticker);
                    double newPerf = getPerf(infos.bid.price.doubleValue(), storedTrade.bid);
                    storedTrade.perf = storedTrade.perf + newPerf;
                    storedTrade.bid = infos.bid.price.doubleValue();
                    storedTrade.asc = infos.ask.price.doubleValue();
                    storedTrade.diffToHigh = getPerf(infos.high.last24hours.doubleValue(), infos.bid.price.doubleValue());
                    storedTrade.spread = getAvg(storedTrade.spread, storedTrade.asc - storedTrade.bid);


                } else {
                    Trade trade = new Trade();
                    trade.perf = 0;
                    trade.bid = infos.bid.price.doubleValue();
                    trade.asc = infos.ask.price.doubleValue();
                    trade.spread = trade.asc - trade.bid;
                    trade.ticker = ticker;
                    trade.numberOfTrades = infos.volume.today.doubleValue();
                    trade.perf24h = infos.todayOpenPrice.doubleValue() - infos.lastTradeClosed.price.doubleValue();
                    trade.minEnterPrice = pairs.getResult().get(ticker).ordermin.doubleValue() * infos.ask.price.doubleValue();
                    map.put(ticker, trade);
                }
            });

            trades = map.values().stream().collect(Collectors.toList());
            Collections.sort(trades, Collections.reverseOrder(Comparator.comparingDouble(t -> t.perf)));
            Logger.info("Calculating performance, " + (ITERATIONS - i) + " seconds to wait... ->" + trades.get(0).ticker + " #perf: " + trades.get(0).perf);
            ThreadUtils.sleepCatchingException(60000);
        }
        List<Trade> top5 = trades.subList(0, 5);
        Collections.sort(top5, Comparator.comparingDouble(t -> t.spread));
        return top5;
    }

    public static Double getPerf(double d1, double d2) {
        return (d1 - d2) / d2;
    }

    public static Double getAvg(double d1, double d2) {
        return 0.5 * (d1 + d2);
    }


    public static class Trade {
        public String ticker;
        public double bid;
        public double asc;
        public double perf;
        public double minEnterPrice;
        public double numberOfTrades;
        public double spread;
        public double perf24h;
        public double diffToHigh;

        @Override
        public String toString() {
            return "Trade{" +
                    "ticker='" + ticker + '\'' +
                    ", bid=" + bid +
                    ", asc=" + asc +
                    ", perf=" + perf +
                    ", minEnterPrice=" + minEnterPrice +
                    ", numberOfTrades=" + numberOfTrades +
                    ", spread=" + spread +
                    ", perf24h=" + perf24h +
                    ", diffToHigh=" + diffToHigh +
                    '}';
        }
    }
}
