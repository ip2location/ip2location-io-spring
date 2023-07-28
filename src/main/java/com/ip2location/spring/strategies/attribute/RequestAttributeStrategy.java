package com.ip2location.spring.strategies.attribute;

import com.google.gson.JsonObject;
import com.ip2location.spring.IPGeolocationSpring;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Class to store and read attribute for each request.
 */
public class RequestAttributeStrategy implements AttributeStrategy {
    /**
     * Store the response.
     *
     * @param request The request that originated from a client to this server.
     * @param response The JsonObject returned by the IP2Location.io API.
     */
    @Override
    public void storeAttribute(HttpServletRequest request, JsonObject response) {
        request.setAttribute(IPGeolocationSpring.ATTRIBUTE_KEY, response);
    }

    /**
     * Get the stored response.
     *
     * @param request The request that originated from a client to this server.
     * @return The stored JsonObject returned by the IP2Location.io API.
     */
    @Override
    public JsonObject getAttribute(HttpServletRequest request) {
        return (JsonObject) request.getAttribute(IPGeolocationSpring.ATTRIBUTE_KEY);
    }
}
