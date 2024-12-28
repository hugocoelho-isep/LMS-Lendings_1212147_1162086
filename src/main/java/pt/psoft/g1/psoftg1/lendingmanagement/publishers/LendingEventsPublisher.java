package pt.psoft.g1.psoftg1.lendingmanagement.publishers;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

public interface LendingEventsPublisher {
    void sendLendingCreated(Lending lending);
    void sendLendingUpdated(Lending lending, Long currentVersion);
}
