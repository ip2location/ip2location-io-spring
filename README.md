# IP2Location.io Spring Client Library

[![License](http://img.shields.io/:license-apache-blue.svg)](LICENSE)

This is the official Spring client library for the IP2Location.io IP Geolocation API,
allowing you to lookup your own IP address, or get any of the following details
for an IP:

This Spring client library enables user to query for an enriched data set, such as country, region, district, city, latitude & longitude, ZIP code, time zone, ASN, ISP, domain, net speed, IDD code, area code, weather station data, MNC, MCC, mobile brand, elevation, usage type, address type, advertisement category and proxy data with an IP address. It supports both IPv4 and IPv6 address lookup.

## Getting Started

You'll need an IP2Location.io API key, which you can get by singing up for a free key at [https://www.ip2location.io/pricing](https://www.ip2location.io/pricing).

## Usage

### Maven

```xml
<dependency>
	<groupId>com.ip2location</groupId>
	<artifactId>ip2location-io-spring</artifactId>
	<version>1.0.1</version>
</dependency>
```

### Construction

Using this library is very simple. `IPGeolocationSpring` is exposed through a builder:

```java
        com.ip2location.Configuration config = new com.ip2location.Configuration();
        String apiKey = "YOUR_API_KEY";  // Provide your IP2Location.io API key here.
        config.setApiKey(apiKey);
        IPGeolocation ipl = new IPGeolocation(config);
        IPGeolocationSpring ipGeolocationSpring = new IPGeolocationSpring.Builder()
                // Set the IPGeolocation instance.
                .setIPGeolocation(ipl)
                // Set the InterceptorStrategy. By default we use
                // BotInterceptorStrategy.
                .interceptorStrategy(new BotInterceptorStrategy())
                // Set the IPStrategy. By default we use SimpleIPStrategy.
                .ipStrategy(new SimpleIPStrategy())
                // Set the AttributeStrategy. By default we use SessionAttributeStrategy.
                .attributeStrategy(new SessionAttributeStrategy())
                // Finally build it.
                .build();
```

### Adding to Interceptors

To use this as an interceptor in Spring, you simply need to expose your configuration and add `IPGeolocationSpring` you obtained from the builder here:

````java
package com.example.demo;

import com.ip2location.IPGeolocation;
import com.ip2location.spring.IPGeolocationSpring;
import com.ip2location.spring.strategies.attribute.*;
import com.ip2location.spring.strategies.interceptor.BotInterceptorStrategy;
import com.ip2location.spring.strategies.ip.SimpleIPStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class DemoApplicationConfiguration implements WebMvcConfigurer {
    @Bean(name = "attributeStrategy")
    public AttributeStrategy createBean() {
        AttributeStrategy bean = new RequestAttributeStrategy();
        return bean;
    }

    public void addInterceptors(InterceptorRegistry registry) {
        com.ip2location.Configuration config = new com.ip2location.Configuration();
        String apiKey = "YOUR_API_KEY";  // Provide your IP2Location.io API key here.
        config.setApiKey(apiKey);
        IPGeolocation ipl = new IPGeolocation(config);
        IPGeolocationSpring ipGeolocationSpring = new IPGeolocationSpring.Builder()
                // Set the IPGeolocation instance.
                .setIPGeolocation(ipl)
                // Set the InterceptorStrategy. By default we use
                // BotInterceptorStrategy.
                .interceptorStrategy(new BotInterceptorStrategy())
                // Set the IPStrategy. By default we use SimpleIPStrategy.
                .ipStrategy(new SimpleIPStrategy())
                // Set the AttributeStrategy. By default we use SessionAttributeStrategy.
                .attributeStrategy(new SessionAttributeStrategy())
                // Finally build it.
                .build();
        registry.addInterceptor(ipGeolocationSpring);
    }
}
````

### Accessing Value

There are two methods of getting the JsonObject that was injected into the
attributes:

1. Access it directly using the key defined in `IPGeolocationSpring`.
2. Access it using a reference to `attributeStrategy`.

The code below showcases the two different methods:

````java
package com.example.demo;

import com.google.gson.JsonObject;
import com.ip2location.IPGeolocation;
import com.ip2location.spring.IPGeolocationSpring;
import com.ip2location.spring.strategies.attribute.AttributeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class DemoApplicationController {
    @Autowired
    private AttributeStrategy attributeStrategy;

    @RequestMapping("/foo")
    public String foo(HttpServletRequest request) {
        JsonObject ipResponse = (JsonObject) attributeStrategy.getAttribute(request);

        if (ipResponse == null) {
            return "no response";
        }

        return ipResponse.toString();
    }

    @RequestMapping("/bar")
    public String bar(HttpServletRequest request) {
        JsonObject ipResponse = (JsonObject) request
                .getSession()
                .getAttribute(IPGeolocationSpring.ATTRIBUTE_KEY);

        if (ipResponse == null) {
            return "no response";
        }

        return ipResponse.toString();
    }

}
````

### `InterceptorStrategy`

The `InterceptorStrategy` allows the middleware to know when to actually run the API calls to [ip2location.io](https://www.ip2location.io/).

- `BotInterceptorStrategy` (default)
  This does some very basic checks to see if the request is coming from a spider/crawler, and ignores them.

- `TrueInterceptorStrategy`
  This runs the API calls all the time.

### `IPStrategy`

The `IPStrategy` allows the middleware to know how to extract the IP address of an incoming request.

- `SimpleIPStrategy` (default)
  This strategy simply looks at the IP of a request and uses that to extract more data using IP2Location.io.

- `XForwardedForIPStrategy`
  This strategy will extract the IP from the `X-Forwarded-For` header and if it's null then it'll extract the IP using `REMOTE_ADDR` of client.

### `AttributeStrategy`

The `AttributeStrategy` allows the middleware to know where to store the `JsonObject` from [ip2location.io](https://www.ip2location.io/).

- `SessionAttributeStrategy` (default)
  This strategy stores the JsonObject for the entire session. This would be much more efficient.

- `RequestAttributeStrategy`
  This strategy stores the JsonObject per request; this would lead to more API calls to [ip2location.io](https://www.ip2location.io/)
