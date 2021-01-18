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
public class AggMarketData extends MessageBase{

    String configId;

    String symbol;

    String tenor;

    @Override
    public Map<String, String> getClassifier() {
        Map<String ,String> classifier = new HashMap<>(4);
        classifier.put("message_type", this.getClass().getSimpleName());
        classifier.put("configId", configId);
        classifier.put("symbol", symbol);
        classifier.put("tenor", tenor);
        return classifier;
    }

    static public Map<String, String> composeClassifier(String configId, String symbol, String tenor) {
        Map<String ,String> classifier = new HashMap<>(4);
        classifier.put("message_type", AggMarketData.class.getSimpleName());
        classifier.put("configId", configId == null ? "*" : configId);
        classifier.put("symbol", symbol == null ? "*" : symbol);
        classifier.put("tenor", tenor == null ? "*" : tenor);
        return classifier;
    }
}