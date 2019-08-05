/*
 * ****************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2016] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 * ****************************************************************************
 */

package org.cloudfoundry.identity.uaa.web;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.servlet.ServletConfig;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RecognizeFailureDispatcherServletTest {
    private RecognizeFailureDispatcherServlet.ShutdownHelper shutdownHelper;
    private RecognizeFailureDispatcherServlet servlet;
    private ServletConfig servletConfig;
    private ConfigurableEnvironment configurableEnvironment;

    @BeforeEach
    void setup() {
        shutdownHelper = mock(RecognizeFailureDispatcherServlet.ShutdownHelper.class);
        servlet = spy(new RecognizeFailureDispatcherServlet(shutdownHelper));
        servletConfig = mock(ServletConfig.class);
        configurableEnvironment = mock(ConfigurableEnvironment.class);
    }

    @Nested
    class WhenInitThrows {
        @BeforeEach
        void setup() throws Exception {
            Mockito.doThrow(new RuntimeException("some app error", new SQLException("db error")))
                    .when(servlet)
                    .delegateInitToSuper(any());
        }

        @Test
        void callsTheShutdownHelperWithExitStrategy() throws Exception {
            mockEnvironmentProperty("uaa.onInitializationFailure", "exit");
            servlet.init(servletConfig);

            verify(shutdownHelper, times(1)).shutdown();
        }

        @Test
        void ignoresShutdownHelperWithoutExitStrategy() throws Exception {
            mockEnvironmentProperty("uaa.onInitializationFailure", null);
            servlet.init(servletConfig);

            verify(shutdownHelper, times(0)).shutdown();
        }

        private void mockEnvironmentProperty(String property, String value) {
            Mockito.doReturn(configurableEnvironment)
                    .when(servlet)
                    .getEnvironment();
            Mockito.doReturn(value)
                    .when(configurableEnvironment)
                    .getProperty(property);
        }
    }

    @Test
    void doesNotCallTheShutdownHelperWhenInitializationSucceeds() throws Exception {
        Mockito.doNothing()
                .when(servlet)
                .delegateInitToSuper(any());

        servlet.init(servletConfig);
        verify(shutdownHelper, times(0)).shutdown();
    }
}