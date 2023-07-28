package com.ip2location.spring.strategies.attribute;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for attribute
 */
public interface AttributeStrategy {
    /**
     * Store the response.
     *
     * @param request The request that originated from a client to this server.
     * @param response The JsonObject returned by the IP2Location.io API.
     */
    void storeAttribute(HttpServletRequest request, JsonObject response);

    /**
     * Get the stored response.
     *
     * @param request The request that originated from a client to this server.
     * @return The stored JsonObject returned by the IP2Location.io API.
     */
    JsonObject getAttribute(HttpServletRequest request);

    /**
     * Check if has attribute.
     *
     * @param request The request that originated from a client to this server.
     * @return True if has attribute else False.
     */
    default boolean hasAttribute(HttpServletRequest request) {
        return getAttribute(request) != null;
    }
}
