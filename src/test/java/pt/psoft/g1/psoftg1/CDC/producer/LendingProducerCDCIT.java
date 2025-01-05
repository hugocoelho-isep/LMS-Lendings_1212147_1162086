package pt.psoft.g1.psoftg1.CDC.producer;

import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

import java.time.LocalDate;
import java.util.HashMap;

import pt.psoft.g1.psoftg1.TestConfig;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.publishers.impl.LendingEventsRabbitmqPublisherImpl;
import pt.psoft.g1.psoftg1.lendingmanagement.publishers.LendingEventsPublisher;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

@Import(TestConfig.class)
@SpringBootTest(
         webEnvironment = SpringBootTest.WebEnvironment.NONE,
         classes = {LendingEventsRabbitmqPublisherImpl.class, LendingService.class}
         , properties = {
                "stubrunner.amqp.mockConnection=true",
                "spring.profiles.active=test"
        }
)
@Provider("lending_event-producer")
@PactFolder("target/pacts")
public class LendingProducerCDCIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(LendingProducerCDCIT.class);

    @Autowired
    LendingEventsPublisher lendingEventsPublisher;

    @MockBean
    RabbitTemplate template;

    @MockBean (name = "directExchangeLendings")
    DirectExchange direct;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testTemplate(Pact pact, Interaction interaction, PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @PactVerifyProvider("a lending created event")
    public MessageAndMetadata lendingCreated() throws JsonProcessingException {

        LendingViewAMQP lendingViewAMQP = new LendingViewAMQP();
        lendingViewAMQP.setLendingNumber("123456");
        lendingViewAMQP.setCommentary("First lending");
        lendingViewAMQP.setReturnedDate(LocalDate.of(2023, 10, 1));
        lendingViewAMQP.setVersion(1L);

        Message<String> message = new LendingMessageBuilder().withLending(lendingViewAMQP).build();

        return generateMessageAndMetadata(message);
    }

    @PactVerifyProvider("a lending updated event")
    public MessageAndMetadata lendingUpdated() throws JsonProcessingException {

        LendingViewAMQP lendingViewAMQP = new LendingViewAMQP();
        lendingViewAMQP.setLendingNumber("123456");
        lendingViewAMQP.setCommentary("Updated lending");
        lendingViewAMQP.setReturnedDate(LocalDate.of(2023, 10, 2));
        lendingViewAMQP.setVersion(2L);

        Message<String> message = new LendingMessageBuilder().withLending(lendingViewAMQP).build();

        return generateMessageAndMetadata(message);
    }

    private MessageAndMetadata generateMessageAndMetadata(Message<String> message) {
        HashMap<String, Object> metadata = new HashMap<>();
        message.getHeaders().forEach((k, v) -> metadata.put(k, v));

        return new MessageAndMetadata(message.getPayload().getBytes(), metadata);
    }
}