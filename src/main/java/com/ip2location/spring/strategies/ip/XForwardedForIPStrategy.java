package com.ip2location.spring.strategies.ip;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Class to read the proxy IP for the user if exists
 */public class XForwardedForIPStrategy implements IPStrategy {
    /**
     * Gets the ip address using the X-Forwarded-For header.
     *
     * @param request The request that originated from a client to this server.
     * @return The IP address.
     */
    @Override
    public String getIPAddress(HttpServletRequest request) {
        String XforwardedFor = request.getHeader("X-Forwarded-For");
        if (XforwardedFor != null) {
            return XforwardedFor.split(",", 0)[0];
        }
        return request.getRemoteAddr();
    }
}
