package io.github.racoondog.gproxylib.scraper.builder;

import io.github.racoondog.gproxylib.enums.Anonymity;
import io.github.racoondog.gproxylib.enums.CountryCode;
import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;

import java.net.URI;
import java.net.URISyntaxException;

public class ProxyScrapeScraperBuilder extends GProxyScraperBuilder {
    private static final String PROXY_SCRAPE_REQUEST_URL = "https://api.proxyscrape.com/v2/?request=getproxies";
    private final StringBuilder requestUrlBuilder = new StringBuilder(PROXY_SCRAPE_REQUEST_URL);

    public ProxyScrapeScraperBuilder(Protocol protocol, int timeoutMillis) {
        super(protocol, timeoutMillis);
        requestUrlBuilder.append("&timeout=").append(timeoutMillis);

        requestUrlBuilder.append(switch (protocol) {
            case Http -> "&protocol=http";
            case Socks4 -> "&protocol=socks4";
            case Socks5 -> "&protocol=socks5";
        });
    }

    public ProxyScrapeScraperBuilder ssl() {
        if (protocol == Protocol.Http) requestUrlBuilder.append("&ssl=1");
        return this;
    }

    public ProxyScrapeScraperBuilder ssl(boolean ssl) {
        if (protocol == Protocol.Http && ssl) ssl();
        return this;
    }

    public ProxyScrapeScraperBuilder anonymity(Anonymity anonymity) {
        if (protocol == Protocol.Http && anonymity != Anonymity.Any) switch (anonymity) {
            case Anonymous -> requestUrlBuilder.append("&anonymity=elite");
            case Elite -> requestUrlBuilder.append("&anonymity=anonymous");
        }
        return this;
    }

    public ProxyScrapeScraperBuilder country(CountryCode countryCode) {
        requestUrlBuilder.append("&country=").append(countryCode.name());
        return this;
    }

    @Override
    public GProxyScraper build() {
        try {
            return new GProxyScraper(
                    new URI(requestUrlBuilder.toString()),
                    protocol,
                    timeoutMillis
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
