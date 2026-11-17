# IP2Location.io Spring Middleware

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**IP2Location.io Spring Middleware** integrates IP geolocation and network intelligence into Spring MVC applications through a request interceptor. It looks up a visitor's IPv4 or IPv6 address using the [IP2Location.io API](https://www.ip2location.io/) and makes the response available to your application.

**Quick links:** [Get an API key](https://www.ip2location.io/pricing) · [API documentation](https://www.ip2location.io/ip2location-documentation) · [GitHub issues](https://github.com/ip2location/ip2location-io-spring/issues)

## Features

- **IP geolocation:** Country, region, city, coordinates, ZIP/postal code, and time zone where available.
- **Network intelligence:** ASN, ISP, domain, connection/usage type, and other eligible network details.
- **Proxy information:** Available proxy and security-related fields, depending on the API plan.
- **Automatic request enrichment:** Run IP lookups via a Spring MVC interceptor and access results from request or session attributes.
- **Configurable strategies:** Control which requests are looked up, which client IP is used, and where results are stored.

> Returned fields depend on the queried IP address and your IP2Location.io subscription plan. IP geolocation is approximate and is not GPS positioning.

## How It Works

1. A client sends an HTTP request to your Spring application.
2. `IPGeolocationSpring` evaluates the request using the configured `InterceptorStrategy`.
3. The selected `IPStrategy` determines the IP address used for the IP2Location.io lookup.
4. The resulting `JsonObject` is stored using the configured `AttributeStrategy`, ready for controller code to read.

## Requirements

- Java 17 or later (the repository's Maven build targets Java 17).
- A Spring MVC application; the project declares Spring Boot 3.1.2 and Jakarta Servlet 6.0 as provided dependencies.
- Maven or another build tool that can consume Maven dependencies.
- An [IP2Location.io API key](https://www.ip2location.io/pricing).

## Installation

Add the following dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.ip2location</groupId>
    <artifactId>ip2location-io-spring</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Usage

The examples below show how to construct the middleware, register it as a Spring MVC interceptor, and read IP intelligence results.

### 1. Construct the middleware

`IPGeolocationSpring` uses a builder to configure the IP2Location.io client and three strategies. The following example explicitly selects the default strategies:

```java
com.ip2location.Configuration config = new com.ip2location.Configuration();
config.setApiKey(System.getenv("IP2LOCATION_API_KEY"));

IPGeolocation lookup = new IPGeolocation(config);
IPGeolocationSpring interceptor = new IPGeolocationSpring.Builder()
        .setIPGeolocation(lookup)
        .interceptorStrategy(new BotInterceptorStrategy())
        .ipStrategy(new SimpleIPStrategy())
        .attributeStrategy(new SessionAttributeStrategy())
        .build();
```

This is a construction example; register the interceptor with Spring MVC as shown next. Use the relevant imports from the configuration example below.

### 2. Register the interceptor

Create a Spring configuration class. This example stores the lookup result **per request**, so the controller can access it through `RequestAttributeStrategy`.

```java
import com.ip2location.IPGeolocation;
import com.ip2location.spring.IPGeolocationSpring;
import com.ip2location.spring.strategies.attribute.AttributeStrategy;
import com.ip2location.spring.strategies.attribute.RequestAttributeStrategy;
import com.ip2location.spring.strategies.interceptor.BotInterceptorStrategy;
import com.ip2location.spring.strategies.ip.SimpleIPStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IP2LocationConfig implements WebMvcConfigurer {

    @Bean
    public AttributeStrategy attributeStrategy() {
        return new RequestAttributeStrategy();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String apiKey = System.getenv("IP2LOCATION_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("IP2LOCATION_API_KEY is not set");
        }

        com.ip2location.Configuration config = new com.ip2location.Configuration();
        config.setApiKey(apiKey);

        IPGeolocation lookup = new IPGeolocation(config);
        IPGeolocationSpring interceptor = new IPGeolocationSpring.Builder()
                .setIPGeolocation(lookup)
                .interceptorStrategy(new BotInterceptorStrategy())
                .ipStrategy(new SimpleIPStrategy())
                .attributeStrategy(attributeStrategy())
                .build();

        registry.addInterceptor(interceptor);
    }
}
```

Set `IP2LOCATION_API_KEY` in your application's environment before starting it. **Do not commit API keys to source control.**

### 3. Access the geolocation result

```java
import com.google.gson.JsonObject;
import com.ip2location.spring.strategies.attribute.AttributeStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IPInfoController {
    private final AttributeStrategy attributeStrategy;

    public IPInfoController(AttributeStrategy attributeStrategy) {
        this.attributeStrategy = attributeStrategy;
    }

    @GetMapping("/ip-info")
    public String getIPInfo(HttpServletRequest request) {
        JsonObject result = (JsonObject) attributeStrategy.getAttribute(request);
        return result != null ? result.toString() : "No IP information available";
    }
}
```

Visit `/ip-info` to view the available lookup data for the requesting IP address.

### 4. Access results using a session attribute

The default `SessionAttributeStrategy` stores results in the HTTP session. For a controller using that strategy, retrieve the value with `IPGeolocationSpring.ATTRIBUTE_KEY`:

```java
import com.google.gson.JsonObject;
import com.ip2location.spring.IPGeolocationSpring;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IPSessionController {
    @GetMapping("/ip-session-info")
    public String getIPInfo(HttpServletRequest request) {
        JsonObject result = (JsonObject) request.getSession()
                .getAttribute(IPGeolocationSpring.ATTRIBUTE_KEY);
        return result != null ? result.toString() : "No IP information available";
    }
}
```

**Important:** Use `RequestAttributeStrategy` for the example in step 3 or `SessionAttributeStrategy` for step 4. The controller and interceptor must use matching storage strategies.

## Configuration Strategies

The middleware provides three categories of strategies. The defaults below match the repository's documented builder behavior.

| Strategy | Option | Behavior |
|---|---|---|
| `InterceptorStrategy` | `BotInterceptorStrategy` **(default)** | Skips requests that match basic spider/crawler checks. |
| | `TrueInterceptorStrategy` | Performs the lookup for every intercepted request. |
| `IPStrategy` | `SimpleIPStrategy` **(default)** | Uses the request's client IP address. |
| | `XForwardedForIPStrategy` | Uses `X-Forwarded-For` when present, otherwise the request IP. |
| `AttributeStrategy` | `SessionAttributeStrategy` **(default)** | Stores the lookup result in the HTTP session. |
| | `RequestAttributeStrategy` | Stores the lookup result on the current request. |

**Reverse-proxy caution:** Only use `XForwardedForIPStrategy` when a trusted proxy controls or sanitizes `X-Forwarded-For`. Untrusted clients may forge this header.

**Lookup frequency:** Request-scoped storage can result in more API lookups than session-scoped storage. Choose based on data freshness, your application requirements, and API quota.

## Understanding ASN Lookup

An **Autonomous System Number (ASN)** identifies a network or group of networks managed under a common routing policy. IP2Location.io can return available ASN and network-operator information alongside geolocation results.

Common uses include traffic analysis, identifying hosting or network providers, and investigating network-related activity. Data availability depends on your plan; an ASN does not identify an individual user.

For offline IP geolocation and ASN lookups, explore [IP2Location LITE](https://www.ip2location.com/database/lite).

## Common Use Cases

- **Localization:** Adapt regional content based on approximate visitor location.
- **Analytics:** Enrich server-side events with country, ISP, or ASN information.
- **Security review:** Use available proxy and network signals alongside other fraud indicators.
- **Network diagnostics:** Investigate traffic patterns by provider or Autonomous System.

## Troubleshooting

| Issue | What to check |
|---|---|
| No lookup data in the controller | Confirm the interceptor is registered and that the controller reads from the same attribute strategy used by the interceptor. |
| Requests from bots return no data | `BotInterceptorStrategy` skips some crawler-like requests; choose another strategy if appropriate. |
| Wrong client IP behind a proxy | Review proxy configuration and your selected `IPStrategy`; do not trust arbitrary forwarded headers. |
| API authentication or quota errors | Verify your API key, plan limits, and outbound connectivity. |
| Missing response fields | Some fields are plan-dependent or unavailable for the IP address. |
| Unexpectedly frequent lookups | Review request-scoped versus session-scoped storage. |

## Frequently Asked Questions

**Does the middleware support IPv6?**  
Yes, the underlying IP2Location.io geolocation API supports both IPv4 and IPv6 lookups.

**Can I use it without an API key?**  
The setup documented here uses the Java SDK configured with an API key. Obtain a key from [IP2Location.io](https://www.ip2location.io/pricing).

**Is geolocation exact?**  
No. IP geolocation estimates network-related location and should not be used as a precise physical location or proof of identity.

**Does the interceptor run on every request?**  
That depends on the configured interceptor strategy and which routes the interceptor applies to.

## Related IP2Location Products

| Product | Purpose |
|---|---|
| [IP2Location.io](https://www.ip2location.io/) | Hosted IP geolocation, ASN, and network intelligence APIs. |
| [IP2Location LITE](https://www.ip2location.com/database/lite) | Free downloadable IP geolocation and ASN databases for offline use. |
| [IP2Location Database](https://www.ip2location.com/database/ip2location) | Commercial offline IP geolocation databases. |
| [IP2Proxy](https://www.ip2location.com/database/proxy) | IP proxy, VPN, TOR and anonymizer intelligence. |
| [IP2WHOIS](https://www.ip2whois.com/) | Domain WHOIS lookup services. |

## Support

- [IP2Location.io API documentation](https://www.ip2location.io/ip2location-documentation)
- [API plans and pricing](https://www.ip2location.io/pricing)
- [Report issues or request features](https://github.com/ip2location/ip2location-io-spring/issues)

## License

Licensed under the **Apache License 2.0**. See [LICENSE](https://github.com/ip2location/ip2location-io-spring/blob/main/LICENSE).
