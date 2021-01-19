package lxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.ToString;


public interface TopicMessageBase extends MessageBase{
    @JsonIgnore
    default TopicMessageBase getTopic() {return this;}
}
