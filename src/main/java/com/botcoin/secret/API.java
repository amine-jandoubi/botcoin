package com.botcoin.secret;

import com.botcoin.utils.JsonUtils;
import com.botcoin.utils.LOG;
import com.botcoin.utils.VM;
import com.github.sbouclier.result.AccountBalanceResult;
import com.github.sbouclier.result.AssetPairsResult;
import com.github.sbouclier.result.ServerTimeResult;
import com.github.sbouclier.result.TickerInformationResult;
import edu.self.kraken.api.KrakenApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public final class API {
    public static KrakenApi SINGLETON = new KrakenApi();

    static {
        SINGLETON.setKey(VM.get(VM.API_PUBLIC_HOST));
        SINGLETON.setSecret(VM.get(VM.API_PRIVATE_KEY));
    }




    public static AssetPairsResult getPairs() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String response = API.SINGLETON.queryPrivate(KrakenApi.Method.ASSET_PAIRS);
        return JsonUtils.toObject(response, AssetPairsResult.class);
    }

    public static String getPairsNames() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        List<String> euroCurrencies = API.getPairs().getResult().keySet().stream().filter(name -> name.endsWith("EUR")).collect(Collectors.toList());
        StringJoiner joiner = new StringJoiner(",");
        for (String s : euroCurrencies)
            joiner.add(s);
        return joiner.toString();
    }

    public static Date getServerTime() throws IOException {
        ServerTimeResult.ServerTime result = JsonUtils.toObject(API.SINGLETON.queryPublic(KrakenApi.Method.TIME, Map.of("", "")), ServerTimeResult.class).getResult();
        LOG.info(result);
        return new Date(result.unixtime);
    }

    public static Map<String, TickerInformationResult.TickerInformation> getTickersInfos() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String response2 = API.SINGLETON.queryPublic(KrakenApi.Method.TICKER, Map.of("pair", getPairsNames()));
        return JsonUtils.toObject(response2, TickerInformationResult.class).getResult();
    }

    public static double getBalanceInCcy(String ccy) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        try {
            String response = API.SINGLETON.queryPrivate(KrakenApi.Method.BALANCE);
            AccountBalanceResult balance = JsonUtils.toObject(response, AccountBalanceResult.class);
            if (balance == null)
                return 0;
            if (balance.getResult() == null)
                return 0;

            BigDecimal bal = balance.getResult().get(ccy);
            if (bal != null)
                return bal.doubleValue();

            return 0;
        } catch (Exception ex) {
            return 0;
        }
    }
}
