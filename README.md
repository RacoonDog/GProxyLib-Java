# GProxyLib-Java
 
GProxyLib-Java is a java port of Ghost's [GProxyLib](https://github.com/CopeTypes/GProxyLib) libary.

# Example Usage
```java
//Scraping
GProxyScraper scraper = GProxyScraperBuilder.setupCustom("custom_url", Protocol.Http, 2500).build();
GProxyScraper scraper = GProxyScraperBuilder.setupGeonode(Protocol.Http, 2500).speed(GeonodeSpeed.Fast).anonymity(Anonymity.Anonymous).build();
GProxyScraper scraper = GProxyScraperBuilder.setupProxyScrape(Protocol.Http, 2500).country(CountryCode.US).ssl().build();

CompletableFuture<List<GProxy>> scraperFuture = scraper.scrape();

//Testing
var tester = new GProxyTester(scraperFuture.join(), "http://www.google.com");

CompletableFuture<List<GProxy>> testerFuture = tester.test();

List<GProxy> workingProxies = testerFuture.join();
```