package io.github.racoondog.gproxylibtest;

import io.github.racoondog.gproxylib.enums.CountryCode;
import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.GProxyScraper;
import io.github.racoondog.gproxylib.tester.GProxyTester;

public class Main {
    public static void main(String[] args) {
        new GProxyTester(GProxyScraper.scrapeDefault(Protocol.Socks4, 1000, CountryCode.Any).join(), "https://www.google.com").test().join().forEach(gproxy -> System.out.printf("%s:%s%n", gproxy.ip, gproxy.port));
    }
}
