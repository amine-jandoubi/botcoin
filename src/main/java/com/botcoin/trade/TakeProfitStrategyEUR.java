package com.botcoin.trade;

import com.botcoin.secret.API;
import com.botcoin.utils.Decimal;
import com.botcoin.utils.Logger;
import com.botcoin.utils.Pair;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class TakeProfitStrategyEUR {

    private Pair pair;
    private BigDecimal investment;
    private BigDecimal amount;

    KrakenApi api = new KrakenApi();

    public TakeProfitStrategyEUR(double investmentInEUR, Pair pair) {
        api.setKey(API.getPublicKey());
        api.setSecret(API.getPrivateKey());

        this.pair = pair;
        this.investment = new BigDecimal(investmentInEUR);
    }

    public void run() throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        while (!new MarketDirection(this.pair).isGoingUp())
            continue;
        buy();

    }

    public void buy() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        MarketDirection.AsksBidsInfo asksBidsInfo = new MarketDirection(this.pair).getAskBidAvg();

        this.amount = Decimal.round6(this.investment.doubleValue() / asksBidsInfo.getAsksInfo().doubleValue());
        HashMap<String, String> values = new HashMap();
        values.put("pair", this.pair.getName());
        values.put("type", "buy");
        values.put("ordertype", "limit");
        values.put("price", asksBidsInfo.getAsksInfo().toString());
        values.put("volume", this.amount.toString());
        Logger.info("buy " + amount.toString() + " " + this.pair + "  at " + asksBidsInfo.getAsksInfo().toString());
        Logger.info(api.queryPrivate(KrakenApi.Method.ADD_ORDER, values));

        HashMap<String, String> valuesSell = new HashMap();
        valuesSell.put("pair", this.pair.getName());
        valuesSell.put("type", "sell");
        valuesSell.put("ordertype", "take-profit");
        valuesSell.put("price", asksBidsInfo.getBidsInfo().toString());
        valuesSell.put("volume", this.amount.toString());
        Logger.info("sell " + amount.toString() + " " + this.pair + " " + "take profit " + asksBidsInfo.getBidsInfo().toString());
        Logger.info(api.queryPrivate(KrakenApi.Method.ADD_ORDER, valuesSell));

    }


}
