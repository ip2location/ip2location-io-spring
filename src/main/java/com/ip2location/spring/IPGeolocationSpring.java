package com.ip2location.spring;

import com.google.gson.JsonObject;
import com.ip2location.IPGeolocation;
import com.ip2location.spring.strategies.attribute.AttributeStrategy;
import com.ip2location.spring.strategies.attribute.SessionAttributeStrategy;
import com.ip2location.spring.strategies.interceptor.BotInterceptorStrategy;
import com.ip2location.spring.strategies.interceptor.InterceptorStrategy;
import com.ip2location.spring.strategies.ip.IPStrategy;
import com.ip2location.spring.strategies.ip.SimpleIPStrategy;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Class to specify handlers for interceptions.
 */
public class IPGeolocationSpring implements HandlerInterceptor {
    /**
     * Specify the attribute key to use.
     */
    public static final String ATTRIBUTE_KEY = "IP2Location.IPGeolocation";
    private final IPGeolocation ipl;
    private final AttributeStrategy attributeStrategy;
    private final IPStrategy ipStrategy;
    private final InterceptorStrategy interceptorStrategy;

    /**
     * Constructor.
     *
     * @param ipl The IPGeolocation object.
     * @param attributeStrategy The AttributeStrategy object.
     * @param ipStrategy The IPStrategy object.
     * @param interceptorStrategy The InterceptorStrategy object.
     */
    IPGeolocationSpring(
            IPGeolocation ipl,
            AttributeStrategy attributeStrategy,
            IPStrategy ipStrategy,
            InterceptorStrategy interceptorStrategy
    ) {
        this.ipl = ipl;
        this.attributeStrategy = attributeStrategy;
        this.ipStrategy = ipStrategy;
        this.interceptorStrategy = interceptorStrategy;
    }

    /**
     * Show warning message and exit.
     *
     * @param args The command line parameters.
     */
    public static void main(String... args) {
        System.out.println("This library is not meant to be run as a standalone jar.");
        System.exit(0);
    }

    /**
     * Specify preHandle actions.
     *
     * @param request The request from the client.
     * @param response The response from the server.
     * @param handler The handler object.
     * @return True.
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (!interceptorStrategy.shouldRun(request)) {
            return true;
        }

        // Don't waste an API call if we already have it.
        // This should only happen for RequestAttributeStrategy and potentially
        // other implementations.
        if (attributeStrategy.hasAttribute(request)) {
            return true;
        }

        String ip = ipStrategy.getIPAddress(request);
        if (ip == null) {
            return true;
        }

        JsonObject myObj = ipl.Lookup(ip);
        attributeStrategy.storeAttribute(request, myObj);

        return true;
    }

    /**
     * Static builder class.
     */
    public static class Builder {
        private IPGeolocation ipl;
        private AttributeStrategy attributeStrategy = new SessionAttributeStrategy();
        private IPStrategy ipStrategy = new SimpleIPStrategy();
        private InterceptorStrategy interceptorStrategy = new BotInterceptorStrategy();

        /**
         * Sets the IPGeolocation object.
         *
         * @param ipl The IPGeolocation object.
         * @return The builder object.
         */
        public Builder setIPGeolocation(IPGeolocation ipl) {
            this.ipl = ipl;
            return this;
        }

        /**
         * Sets the AttributeStrategy object.
         *
         * @param attributeStrategy The AttributeStrategy object.
         * @return The builder object.
         */
        public Builder attributeStrategy(AttributeStrategy attributeStrategy) {
            this.attributeStrategy = attributeStrategy;
            return this;
        }

        /**
         * Sets the IPStrategy object.
         *
         * @param ipStrategy The IPStrategy object.
         * @return The builder object.
         */
        public Builder ipStrategy(IPStrategy ipStrategy) {
            this.ipStrategy = ipStrategy;
            return this;
        }

        /**
         * Sets the InterceptorStrategy object.
         *
         * @param interceptorStrategy The InterceptorStrategy object.
         * @return The builder object.
         */
        public Builder interceptorStrategy(InterceptorStrategy interceptorStrategy) {
            this.interceptorStrategy = interceptorStrategy;
            return this;
        }

        /**
         * Build new IPGeolocationSpring object.
         *
         * @return The IPGeolocationSpring object.
         */
        public IPGeolocationSpring build() {
            return new IPGeolocationSpring(ipl, attributeStrategy, ipStrategy, interceptorStrategy);
        }
    }
}
