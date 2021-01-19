package lxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


/**
 * @author lixiangyang
 */
@Builder
@ToString
public class MarketData implements TopicMessageBase {

    String exDestination;

    String symbol;

    String tenor;

    @JsonIgnore
    @Override
    public Topic getTopic() {
        return Topic.builder().symbol(symbol).tenor(tenor).exDestination(exDestination).build();
    }

    @Builder
    @ToString
    @Getter
    static class Topic implements TopicMessageBase {
        String exDestination;

        String symbol;

        String tenor;

    }




}
