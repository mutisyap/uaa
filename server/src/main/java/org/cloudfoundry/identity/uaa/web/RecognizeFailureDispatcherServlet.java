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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class RecognizeFailureDispatcherServlet extends DispatcherServlet {
    private static Logger logger = LoggerFactory.getLogger(RecognizeFailureDispatcherServlet.class);
    protected static final String HEADER = "X-Cf-Uaa-Error";
    private final ShutdownHelper shutdownHelper;

    public RecognizeFailureDispatcherServlet(ShutdownHelper shutdownHelper) {
        super();
        this.shutdownHelper = shutdownHelper;
    }

    @Override
    public void init(ServletConfig config) {
        try {
            delegateInitToSuper(config);
        } catch (Exception e) {
            logger.error("Fatal error: Unable to start UAA application.", e);
            if ("exit".equals(getEnvironment().getProperty("uaa.onInitializationFailure"))) {
                shutdownHelper.shutdown();
            }
        }
    }

    void delegateInitToSuper(ServletConfig config) throws ServletException {
        super.init(config);
    }

    static class ShutdownHelper {
        public void shutdown() {
            System.exit(2);
        }
    }
}
