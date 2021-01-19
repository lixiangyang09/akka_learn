//package lxy;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.locks.LockSupport;
//
//@Slf4j
//public class App {
//
//    static public void main(@NonNull String[] args) {
//        var system = ActorSystem.create("finone");
//        ActorRef worker1 = system.actorOf(Props.create(Worker.class), "work1");
//        ActorRef worker2 = system.actorOf(Props.create(Worker.class), "work2");
//        ActorRef worker3 = system.actorOf(Props.create(Worker.class), "work3");
//        ActorRef worker4 = system.actorOf(Props.create(Worker.class), "work4");
//
//        CustomizedEventBus eventBus = new CustomizedEventBus();
//        List<String> symbols = List.of("s1", "s2", "s3");
//        List<String> tenors = List.of("t1", "t2", "t3");
//        List<String> exDestination = List.of("ex1", "ex2", "ex3");
//
//        eventBus.subscribe(worker1, MarketData.composeClassifier(null, symbols.get(0), null));
//        eventBus.subscribe(worker1, AggMarketData.composeClassifier(null, symbols.get(1), null));
//        eventBus.subscribe(worker2, MarketData.composeClassifier(null, symbols.get(0), tenors.get(0)));
//        eventBus.subscribe(worker3, MarketData.composeClassifier(exDestination.get(0), symbols.get(0), tenors.get(0)));
//
//        var random = new Random();
//
//        while (true) {
//            MessageBase msg;
//            if (random.nextBoolean()) {
//                msg = MarketData.builder()
//                        .symbol(symbols.get(random.nextInt(symbols.size())))
//                        .exDestination(exDestination.get(random.nextInt(exDestination.size())))
//                        .tenor(tenors.get(random.nextInt(tenors.size())))
//                        .build();
//            } else {
//                msg = MarketData.builder()
//                        .symbol(symbols.get(random.nextInt(symbols.size())))
//                        .exDestination(exDestination.get(random.nextInt(exDestination.size())))
//                        .tenor(tenors.get(random.nextInt(tenors.size())))
//                        .build();
//            }
//            log.info("publish msg: {}", msg);
//            eventBus.publish(msg);
//            LockSupport.parkNanos((long) 1e9);
//        }
//    }
//}
