package com.ip2location.spring.strategies.interceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for interception
 */
public interface InterceptorStrategy {
    /**
     * Whether to allow interception
     *
     * @param request The request that originated from a client to this server.
     * @return True or False.
     */
    boolean shouldRun(HttpServletRequest request);
}
