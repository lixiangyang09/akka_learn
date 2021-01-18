package lxy;

import akka.actor.AbstractActor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lixiangyang
 */
@Slf4j
public class Worker extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MarketData.class, this::handleMarketData)
                .match(AggMarketData.class, this::handleAggMarketData)
                .build();
    }

    void handleMarketData(MarketData md) {
        log.info("{} recv MarketData: {}", getSelf().path().name(), md);
    }

    void handleAggMarketData(AggMarketData md) {
        log.info("{} recv AggMarketData: {}", getSelf().path().name(), md);
    }

}
