package com.ip2location.spring.strategies.ip;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for how to read the client IP address
 */
public interface IPStrategy {
    /**
     * Gets the IP Address from the request
     *
     * @param request The request that originated from a client to this server
     * @return The IP address
     */
    String getIPAddress(HttpServletRequest request);
}
