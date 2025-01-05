package pt.psoft.g1.psoftg1.CDC.consumer;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Message;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {LendingEventRabbitmqReceiver.class, LendingService.class}
)
@PactConsumerTest
@PactTestFor(providerName = "lending_event-producer", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4)
public class LendingCDCDefinitionTest {

    @MockBean
    LendingService lendingService;

    @Autowired
    LendingEventRabbitmqReceiver listener;

    @Pact(consumer = "lending_created-consumer")
    V4Pact createLendingCreatedPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("lendingNumber", "123456");
        body.stringType("isbn", "9781234567897");
        body.stringType("readerNumber", "reader123");
        body.stringType("commentary", "First lending");
        body.stringMatcher("returnedDate", "\\d{4}-\\d{2}-\\d{2}", "2023-10-01");
        body.stringMatcher("version", "[0-9]+", "1");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("Content-Type", "application/json");

        return builder.expectsToReceive("a lending created event")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(consumer = "lending_updated-consumer")
    V4Pact createLendingUpdatedPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("lendingNumber", "123456");
        body.stringType("isbn", "9781234567897");
        body.stringType("readerNumber", "reader123");
        body.stringType("commentary", "Updated lending");
        body.stringMatcher("returnedDate", "\\d{4}-\\d{2}-\\d{2}", "2023-10-02");
        body.stringMatcher("version", "[0-9]+", "2");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("Content-Type", "application/json");

        return builder.expectsToReceive("a lending updated event")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createLendingCreatedPact")
    void testLendingCreated(List<V4Interaction.AsynchronousMessage> messages) throws Exception {
        String jsonReceived = messages.get(0).contentsAsString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(jsonReceived.getBytes(StandardCharsets.UTF_8), messageProperties);

        Assertions.assertDoesNotThrow(() -> {
            listener.receiveLendingCreated(message);
        });

        Mockito.verify(lendingService, Mockito.times(1)).create(ArgumentMatchers.any(LendingViewAMQP.class));
    }

    @Test
    @PactTestFor(pactMethod = "createLendingUpdatedPact")
    void testLendingUpdated(List<V4Interaction.AsynchronousMessage> messages) throws Exception {
        String jsonReceived = messages.get(0).contentsAsString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(jsonReceived.getBytes(StandardCharsets.UTF_8), messageProperties);

        Assertions.assertDoesNotThrow(() -> {
            listener.receiveLendingUpdated(message);
        });

        Mockito.verify(lendingService, Mockito.times(1)).setReturned(ArgumentMatchers.any(LendingViewAMQP.class));
    }
}