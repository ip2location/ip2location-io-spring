package com.ip2location.spring.strategies.interceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Class to read intercept all the time.
 */
public class TrueInterceptorStrategy implements InterceptorStrategy {
    /**
     * Allow to intercept all the time.
     *
     * @param request The request that originated from a client to this server.
     * @return True.
     */
    @Override
    public boolean shouldRun(HttpServletRequest request) {
        return true;
    }
}
