package io.github.racoondog.gproxylib.scraper.builder;

import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;

import java.net.URI;
import java.net.URISyntaxException;

public class CustomScraperBuilder extends GProxyScraperBuilder {
    private final URI uri;

    public CustomScraperBuilder(String url, Protocol protocol, int timeoutMillis) {
        super(protocol, timeoutMillis);

        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GProxyScraper build() {
        return new GProxyScraper(uri, protocol, timeoutMillis);
    }
}
