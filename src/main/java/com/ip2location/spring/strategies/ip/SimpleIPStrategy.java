package com.ip2location.spring.strategies.ip;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Class to read the direct IP for the user.
 */
public class SimpleIPStrategy implements IPStrategy {
    /**
     * Gets the ip address using the RemoteAddress the server sees.
     * This will fail behind reverse proxies or CDNs.
     *
     * @param request The request that originated from a client to this server.
     * @return The IP address.
     */
    @Override
    public String getIPAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
