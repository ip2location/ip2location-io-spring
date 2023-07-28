package com.ip2location.spring.strategies.interceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Class to read intercept if not bot or spider.
 */
public class BotInterceptorStrategy implements InterceptorStrategy {
    /**
     * Allow to intercept if not bot or spider.
     *
     * @param request The request that originated from a client to this server.
     * @return True if not bot or spider else is False.
     */
    @Override
    public boolean shouldRun(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }

        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("spider") || userAgent.contains("bot")) {
            return false;
        }

        return true;
    }
}
