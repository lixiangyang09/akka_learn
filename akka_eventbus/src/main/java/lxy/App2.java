package lxy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class App2 {

    static public void main(@NonNull String[] args) {
        var system = ActorSystem.create("finone");
        ActorRef worker1 = system.actorOf(Props.create(Worker.class), "work1");
        ActorRef worker2 = system.actorOf(Props.create(Worker.class), "work2");
        ActorRef worker3 = system.actorOf(Props.create(Worker.class), "work3");
        ActorRef worker4 = system.actorOf(Props.create(Worker.class), "work4");

        CustomizedEventBus2 eventBus = new CustomizedEventBus2();
        List<String> symbols = List.of("s1", "s2", "s3");
        List<String> tenors = List.of("t1", "t2", "t3");
        List<String> exDestination = List.of("ex1", "ex2", "ex3");

        eventBus.subscribe(worker3, MarketData.Topic.builder().symbol(symbols.get(0)).tenor(tenors.get(0)).exDestination(exDestination.get(0)).build());
        eventBus.subscribe(worker3, MarketData.Topic.builder().symbol(symbols.get(2)).tenor(tenors.get(0)).exDestination(exDestination.get(0)).build());

        eventBus.subscribe(worker1, MarketData.Topic.builder().symbol(symbols.get(0)).build());

        eventBus.subscribe(worker4, MarketData.Topic.builder().build());

        eventBus.subscribe(worker1, AggMarketData.Topic.builder().symbol(symbols.get(0)).build());
        eventBus.subscribe(worker2, MarketData.Topic.builder().symbol(symbols.get(0)).tenor(tenors.get(0)).build());
        eventBus.publish(MarketData.builder()
                .symbol(symbols.get(0))
                .exDestination(exDestination.get(0))
                .tenor(tenors.get(1))
                .build());

        eventBus.publish(MarketData.builder()
                .symbol(symbols.get(0))
                .exDestination(exDestination.get(1))
                .tenor(tenors.get(0))
                .build());

        eventBus.publish(MarketData.builder()
                .symbol(symbols.get(0))
                .exDestination(exDestination.get(2))
                .tenor(tenors.get(2))
                .build());

        eventBus.publish(MarketData.builder()
                .symbol(symbols.get(1))
                .exDestination(exDestination.get(0))
                .tenor(tenors.get(0))
                .build());

        var random = new Random();

        while (true) {
            TopicMessageBase msg;
            if (random.nextBoolean()) {
                msg = MarketData.builder()
                        .symbol(symbols.get(random.nextInt(symbols.size())))
                        .exDestination(exDestination.get(random.nextInt(exDestination.size())))
                        .tenor(tenors.get(random.nextInt(tenors.size())))
                        .build();
            } else {
                msg = MarketData.builder()
                        .symbol(symbols.get(random.nextInt(symbols.size())))
                        .exDestination(exDestination.get(random.nextInt(exDestination.size())))
                        .tenor(tenors.get(random.nextInt(tenors.size())))
                        .build();
            }
            log.info("publish msg: {}", msg);
            eventBus.publish(msg);
            LockSupport.parkNanos((long) 1e9);
        }
    }
}
