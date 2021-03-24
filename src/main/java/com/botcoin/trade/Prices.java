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

public class Prices {

    public static boolean PROD = true;
    static double total = 100;
    static int invests = 1;
    static double investment = total / invests;
    static double minVolume = 4000;
    static List<Future<String>> futures = Collections.synchronizedList(new ArrayList<>());

    static ExecutorService executor
            = Executors.newFixedThreadPool(invests);

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException, MessagingException {
        while (Files.exists(Path.of("C:\\TRADING\\run"))) {
            if (futures.size() < invests) {
                List<Performance.Trade> tradesInvestment = Performance.calculatePers().stream().filter(trade -> trade.minEnterPrice < investment).collect(Collectors.toList());
                tradesInvestment.forEach(Logger::info);
                futures.add(run(tradesInvestment.get(0).ticker, investment));
            }

            futures = futures.stream().filter(f -> !(f.isDone())).collect(Collectors.toList());

            ThreadUtils.sleepCatchingException(5000);
        }
        executor.shutdown();
    }


    public static Future<String> run(String pair, double investment) {
        return executor.submit(() -> {
            try {
                WatchTrade watch = new WatchTrade(pair, investment);

                watch.run();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return pair;
        });
    }
}
