package lxy;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.event.japi.EventBus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @author lixiangyang
 * 思路：在数据结构中定义getClassifier
 * getClassifier，用于在eventBus中，对每一个publish的对象调用，获取用于区分订阅的维度和取值
 * 订阅的时候，只需要给定希望订阅的维度和值即可
 *
 * 两个map，KeyValueMap一个存储了field=value的actorRef，key为message_type + field + field_value
 * 另一个KeyMap存储了某个filed下的所有actorRef,key为message_type + field
 * 查询的时候,当给定一个消息为(message_type="A", field1="b", field2="c")时，
 * 所有符合条件的actorRef为 KeyValueMap[A] + KeyValueMap[A+field1+b] + KeyValueMap[A+field2+c] - (KeyMap[A+field1] - KeyValueMap[A+field1+b]) - (KeyMap[A+field2] - KeyValueMap[A+field2+c])
 *
 * 优点：查询的key的个数与维度的个数为线性关系
 * 缺点：逻辑稍微复杂一些，取消订阅的时候，需要遍历所有的key
 *
 */
@Slf4j
public class CustomizedEventBus2 implements EventBus<TopicMessageBase, ActorRef, TopicMessageBase> {
    /**
     *    用于存储subscriber和Classifier的映射关系
     *    key class+field+value
      */
    Map<String, Set<ActorRef>> subscriberKeyValueMap = new ConcurrentHashMap<>();

    /**
     *    用于存储subscriber和Classifier的映射关系
     *    key class+field
     */
    Map<String, Set<ActorRef>> subscriberKeyMap = new ConcurrentHashMap<>();

    final ObjectMapper oMapper;

    public CustomizedEventBus2() {
        oMapper = new ObjectMapper();
        oMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public boolean subscribe(ActorRef subscriber, TopicMessageBase to) {

        Map<String, String> subscriberKeys = classifier2Keys(to);
        log.info("lixiangyang debug. subscribe {} to {}", subscriber.path().name(), subscriberKeys);
        for (var entry : subscriberKeys.entrySet()) {
            var subscriberKeyValue = subscriberKeyValueMap.computeIfAbsent(entry.getValue(), key -> new CopyOnWriteArraySet<>());
            subscriberKeyValue.add(subscriber);
            // 如果是定义的类型，则不用做排除
            if (!entry.getKey().isEmpty()) {
                var subscriberKey = subscriberKeyMap.computeIfAbsent(entry.getKey(), key -> new CopyOnWriteArraySet<>());
                subscriberKey.add(subscriber);
            }
        }
        return true;
    }

    @Override
    public void publish(TopicMessageBase msg) {
        Map<String, String> keys = classifier2Keys(msg.getTopic());
        log.info("lixiangyang debug. keys: {}", keys);
        Set<ActorRef> candidates = new HashSet<>();
        Set<ActorRef> excludes = new HashSet<>();
        for (var key : keys.entrySet()) {
            log.info("key: {}, value: {}", key.getKey(), key.getValue());
            var keyValueMap = subscriberKeyValueMap.get(key.getValue());
            if (keyValueMap != null) {
                print(keyValueMap);
                candidates.addAll(keyValueMap);
            }
            Set<ActorRef> excludeTmp = new HashSet<>();
            var keyMap = subscriberKeyMap.get(key.getKey());
            if (keyMap != null) {
                print(keyMap);
                excludeTmp.addAll(keyMap);
                print(excludeTmp);

                if (keyValueMap != null) {
                    excludeTmp.removeAll(keyValueMap);
                }
                print(excludeTmp);
            }
            excludes.addAll(excludeTmp);
        }
        print(candidates);
        print(excludes);

        candidates.removeAll(excludes);
        print(candidates);

        for (var actor : candidates) {
            actor.tell(msg, ActorRef.noSender());
        }
    }

    void print(Set<ActorRef> tmp) {
        log.info("======start=====");
        for (var k : tmp) {
            log.info("{}", k.path().name());
        }
        log.info("======end=====");

    }

    @Override
    public boolean unsubscribe(ActorRef subscriber, TopicMessageBase to) {
        unsubscribe(subscriber);
        return true;
    }

    @Override
    public void unsubscribe(ActorRef subscriber) {
        for (var entry : subscriberKeyValueMap.entrySet()) {
            entry.getValue().remove(subscriber);
        }
        for (var entry : subscriberKeyMap.entrySet()) {
            entry.getValue().remove(subscriber);
        }
    }


    Map<String, String> composeKeys(TopicMessageBase msg) {
        var classifier = msg.getTopic();
        log.info("message classifier: {}", classifier);
        return classifier2Keys(classifier);
    }

    Map<String, String> classifier2Keys(TopicMessageBase topic) {
        var classifier = topic2Map(topic);
        Map<String ,String> keyValues = new HashMap<>();
        String messageType = classifier.get("message_type");
        String resKey;
        String resValue;
        for (String key : classifier.keySet()) {
            // type 类型，单独存储即可
            if (key.compareTo("message_type") ==0) {
                resKey = "";
                resValue = messageType;
            } else {
                resKey = String.format("%s#%s", messageType, key);
                resValue = String.format("%s#%s", resKey, classifier.get(key));
            }
            keyValues.put(resKey, resValue);
        }
        return keyValues;
    }

    Map<String, String> topic2Map(TopicMessageBase topic) {
        Map<String, String> map = oMapper.convertValue(topic, Map.class);
        String fullClassName = topic.getClass().getCanonicalName();
        map.put("message_type", fullClassName);
        log.info("topic2Map, topic: {}, map: {}", topic, map);
        return map;
    }
}
