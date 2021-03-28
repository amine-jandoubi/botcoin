package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.Decimal;
import com.botcoin.utils.JsonUtils;
import com.botcoin.utils.LOG;
import com.github.sbouclier.result.OHLCResult;
import com.github.sbouclier.result.TickerInformationResult;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MovingAverage {

    // Configs
    public static final int MV_AVERAGE_INTERVAL_IN_MINUTES = 30;
    public static final int MV_AVERAGE_WINDOW = 15;
    public static final int MINIMUM_NUMBER_OF_PROFIT_UNDER_AVERAGE = 30;

    /**
     * The last price is under the average.
     * The number of points where the take profit is under the average is more than 40.
     */
    public static Predicate<TickerInfoDTO> PREDICATE1 = tickerINFO -> tickerINFO.difference.doubleValue() > 0;
    public static Predicate<TickerInfoDTO> PREDICATE2 = tickerINFO -> tickerINFO.avgOhlcCount.doubleValue() > 20;
    public static Predicate<TickerInfoDTO> PREDICATE3 = tickerINFO -> tickerINFO.numerOfTakeprofitLessThanAverage > MINIMUM_NUMBER_OF_PROFIT_UNDER_AVERAGE;

    public static Predicate<TickerInfoDTO> FILTER = PREDICATE1.and(PREDICATE2).and(PREDICATE3);

    /**
     * maximise the trading trafic
     */
    public static Comparator<TickerInfoDTO> MAXIMIZER = Comparator.comparingDouble(tickerInfoDTO -> tickerInfoDTO.difference.doubleValue());

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        System.out.println(getBest());
    }

    public static TickerInfoDTO getBest() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOG.info("Finding tickers doing less than the Average....");
        final Map<String, TickerInformationResult.TickerInformation> tickerInfos = API.getTickersInfos();

        List<TickerInfoDTO> tickersUnderMovingAverage = API.getPairs().getResult().keySet().parallelStream()
                .filter(pair -> pair.endsWith("EUR")).map(pair -> {
                    try {
                        TickerInfoDTO mvAvg = getTickerInfosWithMvAVGOnly(pair, MV_AVERAGE_INTERVAL_IN_MINUTES, MV_AVERAGE_WINDOW);
                        filFormTickerInformationResult(mvAvg, tickerInfos);
                        LOG.debug(mvAvg);
                        return mvAvg;
                    } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
                        e.printStackTrace();
                    }
                    return new TickerInfoDTO();
                }).filter(FILTER).collect(Collectors.toList());

        tickersUnderMovingAverage.forEach(LOG::info);

        TickerInfoDTO result = tickersUnderMovingAverage.stream().max(MAXIMIZER).orElse(null);
        LOG.info(" [BEST TICKER TO TRADE ON] -> " + result);
        return result;
    }

    public static void filFormTickerInformationResult(TickerInfoDTO tickerInfoDTO, Map<String, TickerInformationResult.TickerInformation> tickersMap) {
        TickerInformationResult.TickerInformation tickerInformationFormAPI = tickersMap.get(tickerInfoDTO.pair);
        tickerInfoDTO.ask = tickerInformationFormAPI.ask.price;
        tickerInfoDTO.bid = tickerInformationFormAPI.bid.price;
        tickerInfoDTO.spread = tickerInfoDTO.ask.add(tickerInfoDTO.bid.negate());
        tickerInfoDTO.volume = tickerInformationFormAPI.volume.today;
        tickerInfoDTO.highest = tickerInformationFormAPI.high.today;
        tickerInfoDTO.takeProfit = Decimal.format("#.######", WatchTrade.takeProfit(tickerInfoDTO.ask.doubleValue()));
    }

    public static TickerInfoDTO getTickerInfosWithMvAVGOnly(String pair, int intervalOfValues, int intervalOfMovingAverage) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        List<OHLCResult.OHLC> ohlcs = getOHLC(pair, intervalOfValues);
        TimeFunction function = createTimeFunction(ohlcs, intervalOfMovingAverage);
        int last = ohlcs.size() - 1;
        TickerInfoDTO tickerInfoDTO = new TickerInfoDTO();
        tickerInfoDTO.pair = pair;
        tickerInfoDTO.mvAvg = function.getValues().get(last).value;
        tickerInfoDTO.lastClose = ohlcs.get(last).close;
        // maybe replace it with ask or bid , think !!
        tickerInfoDTO.difference = tickerInfoDTO.mvAvg.add(tickerInfoDTO.lastClose.negate());
        tickerInfoDTO.volatility = function.volatility;
        tickerInfoDTO.avgOhlcCount = function.avgCount;
        tickerInfoDTO.numerOfTakeprofitLessThanAverage = function.takeprofitLessThanAverage;
        return tickerInfoDTO;
    }


    public static TimeFunction createTimeFunction(List<OHLCResult.OHLC> ohlcs, int interval) {
        TimeFunction timeFunction = new TimeFunction();
        BigDecimal lastPrice = ohlcs.get(ohlcs.size() - 1).close;
        BigDecimal takeProfit = WatchTrade.takeProfit(lastPrice.doubleValue());
        for (int counter = 0; counter < interval; counter++)
            timeFunction.add(ohlcs.get(counter).time, new BigDecimal(0));

        int indexLow = 0;
        int indexHiGH = indexLow + interval;

        for (int counter = indexHiGH; counter < ohlcs.size(); counter++) {
            OHLCResult.OHLC ohlc = ohlcs.get(counter);
            BigDecimal sum = new BigDecimal(0);
            for (int i = indexLow; i < indexHiGH; i++)
                sum = sum.add(ohlcs.get(i).close);

            BigDecimal avg = Decimal.format("#.######", sum.doubleValue() / interval);

            timeFunction.add(ohlc.time, avg);
            double volatility = Math.pow(ohlc.close.doubleValue() - avg.doubleValue(), 2);
            timeFunction.volatility = timeFunction.volatility.add(new BigDecimal(volatility));
            timeFunction.avgCount = timeFunction.avgCount.add(new BigDecimal(ohlc.count / ohlcs.size()));
            indexLow++;
            indexHiGH++;
            if (takeProfit.doubleValue() < avg.doubleValue())
                timeFunction.takeprofitLessThanAverage++;
        }
        timeFunction.volatility = Decimal.format("#.######", timeFunction.volatility);
        return timeFunction;
    }

    public static List<OHLCResult.OHLC> getOHLC(String pair, int intervalInMinutes) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String repsonse = API.SINGLETON.queryPrivate(KrakenApi.Method.OHLC, Map.of("pair", pair, "interval", Integer.valueOf(intervalInMinutes).toString()));
        OHLCResult ohlcResult = JsonUtils.toObject(repsonse, OHLCResult.class);
        return (List<OHLCResult.OHLC>) ((List) ohlcResult.getResult().get(pair)).stream().map(value -> JsonUtils.toObject(value.toString(), OHLCResult.OHLC.class)).collect(Collectors.toList());
    }

    public static class TickerInfoDTO {
        public String pair;
        public BigDecimal difference;
        public BigDecimal lastClose;
        public BigDecimal mvAvg;
        public BigDecimal ask;
        public BigDecimal bid;
        public BigDecimal spread;
        public BigDecimal volume;
        public BigDecimal volatility;
        public BigDecimal takeProfit;
        public BigDecimal highest;
        public long numerOfTakeprofitLessThanAverage;
        public BigDecimal avgOhlcCount;

        @Override
        public String toString() {
            return "TickerInfoDTO{" +
                    "pair='" + pair + '\'' +
                    ", difference=" + difference +
                    ", spread=" + spread +
                    ", volatility=" + volatility +
                    ", takeProfit=" + takeProfit +
                    ", MovingAVgHigherProfitCOunt=" + numerOfTakeprofitLessThanAverage +
                    ", avgOhlcCount=" + avgOhlcCount +
                    '}';
        }
    }

    public static class TimeFunction {
        private List<Value> values = new ArrayList();
        private BigDecimal volatility = new BigDecimal(0);
        private BigDecimal avgCount = new BigDecimal(0);
        private long takeprofitLessThanAverage = 0;

        public void add(int time, BigDecimal value) {
            this.values.add(new Value(time, value));
        }

        public List<Value> getValues() {
            return values;
        }

        public static class Value {
            public int time;
            public BigDecimal value;

            public Value(int time, BigDecimal value) {
                this.time = time;
                this.value = value;
            }

            @Override
            public String toString() {
                return "Value{" +
                        "time=" + time +
                        ", value=" + value +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "TimeFunction{" +
                    "values=" + values +
                    '}';
        }
    }


}
