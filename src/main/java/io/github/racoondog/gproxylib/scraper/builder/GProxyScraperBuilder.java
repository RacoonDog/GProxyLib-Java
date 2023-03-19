package io.github.racoondog.gproxylib.scraper.builder;

import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;

import java.util.Objects;

public abstract class GProxyScraperBuilder {
    public final long timeoutMillis;
    public final Protocol protocol;

    public GProxyScraperBuilder(Protocol protocol, long timeoutMillis) {
        if (timeoutMillis < 0) throw new IllegalArgumentException("Timeout cannot be lower than 0.");
        this.timeoutMillis = timeoutMillis;
        this.protocol = protocol;
    }

    public static CustomScraperBuilder setupCustom(String url, Protocol protocol, long timeoutMillis) {
        return new CustomScraperBuilder(Objects.requireNonNull(url), protocol, timeoutMillis);
    }

    public static ProxyScrapeScraperBuilder setupProxyScrape(Protocol protocol, long timeoutMillis) {
        return new ProxyScrapeScraperBuilder(protocol, timeoutMillis);
    }

    public static GeonodeScraperBuilder setupGeonode(Protocol protocol, long timeoutMillis) {
        return new GeonodeScraperBuilder(protocol, timeoutMillis);
    }

    public abstract GProxyScraper build();
}
