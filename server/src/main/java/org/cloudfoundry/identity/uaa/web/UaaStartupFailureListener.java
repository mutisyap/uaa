package org.cloudfoundry.identity.uaa.web;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.*;

public class UaaStartupFailureListener implements LifecycleListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(UaaStartupFailureListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        String eventType = event.getType();
        Lifecycle lifecycle = event.getLifecycle();

        if (lifecycle instanceof Server && eventType.equals(Lifecycle.AFTER_START_EVENT)) {
            Server server = (Server) lifecycle;
            Consumer<Container> stopTomcat = container -> {
                try {
                    LOGGER.error(format("msg=\"initialization failure, stopping tomcat\" container=%s state=%s", container.getName(), container.getStateName()));
                    server.stop();
                    server.destroy();
                } catch (LifecycleException e) {
                    LOGGER.error(format("msg=\"failed to stop tomcat: %s\"", e.getMessage()), e);
                }
            };

            Stream.of(server.findServices())
                    .map(Service::getContainer)
                    .filter(container -> container.getState() != LifecycleState.STARTED)
                    .findFirst()
                    .ifPresent(stopTomcat);
        }
    }
}
