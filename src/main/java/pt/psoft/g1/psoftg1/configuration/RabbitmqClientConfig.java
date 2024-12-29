package pt.psoft.g1.psoftg1.configuration;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingEvents;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

@Profile("!test")
@Configuration
public class RabbitmqClientConfig {
    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.lendings");
    }

    private static class ReceiverConfig {

        @Bean(name = "autoDeleteQueue_Lending_Created")
        public Queue autoDeleteQueue_Lending_Created() {
            System.out.println("autoDeleteQueue_Lending_Created created!");
            return new AnonymousQueue();
        }

        @Bean
        public Queue autoDeleteQueue_Lending_Updated() {
            System.out.println("autoDeleteQueue_Lending_Updated updated!");
            return new AnonymousQueue();
        }


        @Bean
        public Binding binding1(DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_Created)
                    .to(direct)
                    .with(LendingEvents.LENDING_CREATED);
        }

        @Bean
        public Binding binding2(DirectExchange direct,
                                Queue autoDeleteQueue_Lending_Updated) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_Updated)
                    .to(direct)
                    .with(LendingEvents.LENDING_UPDATED);
        }

        @Bean
        public LendingEventRabbitmqReceiver receiver(LendingService lendingService, @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
            return new LendingEventRabbitmqReceiver(lendingService);
        }
    }
}
