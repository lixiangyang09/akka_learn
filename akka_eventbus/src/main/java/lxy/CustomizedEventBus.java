package lxy;
import akka.actor.ActorRef;
import akka.event.japi.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @author lixiangyang
 * 思路：在数据结构中定义getClassifier和composeClassifier方法，
 * getClassifier，用于在eventBus中，对每一个publish的对象调用，获取用于区分订阅的维度和取值
 * composeClassifier，用于在订阅时指定订阅的条件
 *
 * 当Subscribe时，将所有的维度（除message_type外）按key排序，并拼成一个key(比如：message_type#symbol1#configId2)，
 * 把actorRef放到每一个符合的key下边。
 * 查询时，取出数据的所有的维度和取值，遍历替换每一位为*，用拼好的key去获取actorRef即可
 *
 * 优点：逻辑清晰，简单
 * 缺点：每个数据都需要多次查询，且查询的key的个数，是维度个数的2次幂；
 *      在订阅的时候，需要指定所有的维度，不限定的需要明确给出*
 */
@Slf4j
public class CustomizedEventBus implements EventBus<MessageBase, ActorRef, Map<String, String>> {
    /**
     *    用于存储subscriber和Classifier的映射关系
      */
    Map<String, Set<ActorRef>> subscriberKeyMap = new ConcurrentHashMap<>();
    CopyOnWriteArraySet<ActorRef> subscriberSet = new CopyOnWriteArraySet<>();

    @Override
    public boolean subscribe(ActorRef subscriber, Map<String, String> to) {
        String subscriberKey = classifier2String(to);
        log.info("lixiangyang debug. subscribe {} to {}", subscriber.path().name(), subscriberKey);
        var actorRefSet = subscriberKeyMap.computeIfAbsent(subscriberKey, key -> new CopyOnWriteArraySet<>());
        actorRefSet.add(subscriber);
        subscriberSet.add(subscriber);
        return true;
    }

    @Override
    public void publish(MessageBase msg) {
        List<String> keys = composeKeys(msg);
        log.info("lixiangyang debug. keys: {}", keys);
        for (var key : keys) {
            if (subscriberKeyMap.containsKey(key)) {
                Set<ActorRef> actorRefs = subscriberKeyMap.get(key);
                for (var actorRef : actorRefs) {
                    if (subscriberSet.contains(actorRef)) {
                        actorRef.tell(msg, ActorRef.noSender());
                    }
                }
            }
        }
    }

    @Override
    public boolean unsubscribe(ActorRef subscriber, Map<String ,String> to) {
        unsubscribe(subscriber);
        String subscriberKey = classifier2String(to);
        if (subscriberKeyMap.containsKey(subscriberKey)) {
            Set<ActorRef> actorRefs = subscriberKeyMap.get(subscriberKey);
            actorRefs.remove(subscriber);
            if (actorRefs.isEmpty()) {
                subscriberKeyMap.remove(subscriberKey);
            }
        }
        return true;
    }

    @Override
    public void unsubscribe(ActorRef subscriber) {
        subscriberSet.remove(subscriber);
    }

    TreeSet<String> sortKeys(Set<String> keys) {
        return new TreeSet<>(keys);
    }

    List<String> composeKeys(MessageBase msg) {
        var classifier = msg.getClassifier();
        log.info("message classifier: {}", classifier);
        String messageType = classifier.get("message_type");
        List<String> res = new ArrayList<>();
        res.add(messageType + "#");
        for (var key : sortKeys(classifier.keySet())) {
            if (key.compareTo("message_type") ==0) {
                continue;
            }
            List<String> tmp = new ArrayList<>(res.size() * 2);
            for (var tmpKey : res) {
                tmp.add(tmpKey + classifier.get(key) + "#");
                tmp.add(tmpKey + "*#");
            }
            res = tmp;
        }
        return res;
    }

    String classifier2String(Map<String, String> classifier) {
        StringBuilder stringBuilder = new StringBuilder();
        String messageType = classifier.get("message_type");
        stringBuilder.append(messageType);
        stringBuilder.append("#");
        SortedSet<String> keys = new TreeSet<>(classifier.keySet());
        for (String key : keys) {
            if (key.compareTo("message_type") ==0) {
                continue;
            }
            String value = classifier.get(key);
            stringBuilder.append(value);
            stringBuilder.append("#");
        }
        return stringBuilder.toString();
    }

}
