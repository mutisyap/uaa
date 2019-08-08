package org.cloudfoundry.identity.uaa.web;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;

import java.util.stream.Stream;

public class UaaStartupFailureListener implements LifecycleListener {
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        String eventType = event.getType();
        Lifecycle lifecycle = event.getLifecycle();

        if (lifecycle instanceof Server && eventType.equals(Lifecycle.AFTER_START_EVENT)) {
            Server server = (Server) lifecycle;
            Stream.of(server.findServices()).forEach(this::throwIfNotStarted);
        }
    }

    private void throwIfNotStarted(Service service) {
        Container container = service.getContainer();
        if (container.getState() != LifecycleState.STARTED) {
            String msg = String.format("Failed to start %s: %s", container.getName(), container.getStateName());
            throw new IllegalStateException(msg);
        }
    }
}
