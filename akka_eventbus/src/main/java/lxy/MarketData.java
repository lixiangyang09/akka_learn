package lxy;

import lombok.Builder;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;


/**
 * @author lixiangyang
 */
@Builder
@ToString
public class MarketData extends MessageBase{

    String exDestination;

    String symbol;

    String tenor;

    @Override
    public Map<String, String> getClassifier() {
        Map<String ,String> classifier = new HashMap<>(4);
        classifier.put("message_type", this.getClass().getSimpleName());
        classifier.put("exDestination", exDestination);
        classifier.put("symbol", symbol);
        classifier.put("tenor", tenor);
        return classifier;
    }

    static public Map<String, String> composeClassifier(String exDestination, String symbol, String tenor) {
        Map<String ,String> classifier = new HashMap<>(4);
        classifier.put("message_type", MarketData.class.getSimpleName());
        classifier.put("exDestination", exDestination == null ? "*" : exDestination);
        classifier.put("symbol", symbol == null ? "*" : symbol);
        classifier.put("tenor", tenor == null ? "*" : tenor);
        return classifier;
    }

}
