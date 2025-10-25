package dev.challenge.servicea.replication;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Force422 {
    private final AtomicInteger successCounter = new AtomicInteger(0);

    public void registerLocalCreateSuccess() {
        successCounter.incrementAndGet();
    }

    public boolean shouldForceNext422AndReset() {
        if (successCounter.get() > 3) {
            successCounter.set(0);
            return true;
        }
        return false;
    }
}
