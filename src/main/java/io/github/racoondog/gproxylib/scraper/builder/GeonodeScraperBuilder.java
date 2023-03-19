package io.github.racoondog.gproxylib.scraper.builder;

import io.github.racoondog.gproxylib.enums.Anonymity;
import io.github.racoondog.gproxylib.enums.CountryCode;
import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;

import java.net.URI;
import java.net.URISyntaxException;

public class GeonodeScraperBuilder extends GProxyScraperBuilder {
    private static final String GEONODE_REQUEST_URL = "https://proxylist.geonode.com/api/proxy-list?sort_by=lastChecked&sort_type=desc";
    private final StringBuilder requestUrlBuilder = new StringBuilder(GEONODE_REQUEST_URL);
    private int limit = 500;
    private int page = 1;

    public GeonodeScraperBuilder(Protocol protocol, long timeoutMillis) {
        super(protocol, timeoutMillis);

        requestUrlBuilder.append(switch (protocol) {
            case Http -> "&protocols=http";
            case Socks4 -> "&protocols=socks4";
            case Socks5 -> "&protocols=socks5";
        });
    }

    public GeonodeScraperBuilder limit(int limit) {
        if (limit < 0) throw new IllegalArgumentException("Limit cannot be lower than 0");
        this.limit = limit;
        return this;
    }

    public GeonodeScraperBuilder page(int page) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be lower than 0");
        this.page = page;
        return this;
    }

    public GeonodeScraperBuilder lastCheck(String filter) {
        if (!filter.equals("any")) requestUrlBuilder.append("&filterLastChecked=").append(filter);
        return this;
    }

    public GeonodeScraperBuilder uptime(String filter) {
        if (!filter.equals("any")) requestUrlBuilder.append("&filterUpTime=").append(filter);
        return this;
    }

    public GeonodeScraperBuilder googlePassed() {
        requestUrlBuilder.append("&google=true");
        return this;
    }

    public GeonodeScraperBuilder speed(GeonodeSpeed speed) {
        requestUrlBuilder.append("&speed=").append(speed.id);
        return this;
    }

    public GeonodeScraperBuilder country(CountryCode countryCode) {
        if (countryCode != CountryCode.Any) requestUrlBuilder.append("&country=").append(countryCode.name());
        return this;
    }

    public GeonodeScraperBuilder anonymity(Anonymity anonymity) {
        if (protocol == Protocol.Http && anonymity != Anonymity.Any) switch (anonymity) {
            case Anonymous -> requestUrlBuilder.append("&anonymityLevel=anonymous");
            case Elite -> requestUrlBuilder.append("&anonymityLevel=elite");
        }

        return this;
    }

    @Override
    public GProxyScraper build() {
        requestUrlBuilder.append("&limit=").append(limit).append("&page=").append(page);

        try {
            return new GProxyScraper.GeonodeScraper(
                    new URI(requestUrlBuilder.toString()),
                    protocol,
                    timeoutMillis
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public enum GeonodeSpeed {
        Fast("fast"),
        Medium("medium"),
        Slow("slow");

        public final String id;

        GeonodeSpeed(String id) {
            this.id = id;
        }
    }
}
