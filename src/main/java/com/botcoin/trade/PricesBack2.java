package com.botcoin.trade;

import com.botcoin.utils.Logger;
import com.botcoin.utils.ThreadUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class PricesBack2 {

    public static boolean PROD = true;
    static double total = 100;
    static int invests = 2;
    static double investment = total / invests;
    static double minVolume = 10000;
    static List<Future<String>> futures = Collections.synchronizedList(new ArrayList<>());

    static ExecutorService executor
            = Executors.newFixedThreadPool(invests);

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException, MessagingException {

                List<Performance.Trade> tradesInvestment = Performance.calculatePers().stream().filter(trade -> trade.minEnterPrice < investment && trade.numberOfTrades > minVolume).collect(Collectors.toList());
                tradesInvestment.forEach(Logger::info);
    }
}
