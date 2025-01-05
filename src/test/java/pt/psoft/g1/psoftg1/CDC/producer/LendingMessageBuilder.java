package pt.psoft.g1.psoftg1.CDC.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;

public class LendingMessageBuilder {
  private ObjectMapper mapper = new ObjectMapper();
  private LendingViewAMQP lendingViewAMQP;

  public LendingMessageBuilder withLending(LendingViewAMQP lendingViewAMQP) {
    this.lendingViewAMQP = lendingViewAMQP;
    return this;
  }

  public Message<String> build() throws JsonProcessingException {
    return MessageBuilder.withPayload(this.mapper.writeValueAsString(this.lendingViewAMQP))
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .build();
  }
}