package lxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixiangyang
 */
@Builder
@ToString
public class AggMarketData implements TopicMessageBase {

    String configId;

    String symbol;

    String tenor;


    static public Map<String, String> composeClassifier(String configId, String symbol, String tenor) {
        Map<String ,String> classifier = new HashMap<>(4);
        classifier.put("message_type", AggMarketData.class.getSimpleName());
        classifier.put("configId", configId == null ? "*" : configId);
        classifier.put("symbol", symbol == null ? "*" : symbol);
        classifier.put("tenor", tenor == null ? "*" : tenor);
        return classifier;
    }

    @Builder
    static class Topic implements TopicMessageBase {

        String symbol;

        String tenor;
    }

    @JsonIgnore
    @Override
    public MarketData.Topic getTopic() {
        return MarketData.Topic.builder().symbol(symbol).tenor(tenor).build();
    }
}