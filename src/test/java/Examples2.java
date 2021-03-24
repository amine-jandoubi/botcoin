import com.botcoin.trade.Performance;
import com.botcoin.utils.Logger;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public class Examples2 {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        double investment = 100;
        double minVolume = 5000;

        List<Performance.Trade> tradesInvestment = Performance.calculatePers().stream().filter(trade -> trade.minEnterPrice < investment && trade.numberOfTrades > minVolume).collect(Collectors.toList());
        tradesInvestment.forEach(Logger::info);

    }

}
