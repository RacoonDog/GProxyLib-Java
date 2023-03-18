package io.github.racoondog.gproxylibtest;

import io.github.racoondog.gproxylib.enums.CountryCode;
import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;

public class Main {
    public static void main(String[] args) {
        GProxyScraper.scrapeDefault(Protocol.Socks4, 1000, CountryCode.US).join().forEach(gproxy -> System.out.printf("%s:%s%n", gproxy.ip, gproxy.port));
    }
}
