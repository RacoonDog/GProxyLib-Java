package io.github.racoondog.gproxylib.scraper;

import com.google.gson.*;
import io.github.racoondog.gproxylib.GProxy;
import io.github.racoondog.gproxylib.enums.Anonymity;
import io.github.racoondog.gproxylib.enums.CountryCode;
import io.github.racoondog.gproxylib.enums.Protocol;
import io.github.racoondog.gproxylib.scraper.builder.GProxyScraperBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GProxyScraper {
    protected final HttpRequest request;
    protected final Protocol targetType;
    protected final HttpClient client;
    protected final long timeoutMillis;

    public GProxyScraper(URI targetUri, Protocol targetType, long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        Duration timeout = Duration.ofMillis(timeoutMillis);

        request = HttpRequest.newBuilder(targetUri)
                .timeout(timeout)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                .GET()
                .build();

        client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        this.targetType = targetType;
    }

    public static CompletableFuture<List<GProxy>> scrapeDefault(Protocol protocol, long timeoutMillis, CountryCode countryCode) {
        return scrapeDefault(protocol, timeoutMillis, countryCode, false, Anonymity.Any);
    }

    public static CompletableFuture<List<GProxy>> scrapeDefault(Protocol protocol, long timeoutMillis, CountryCode countryCode, Anonymity anonymity) {
        return scrapeDefault(protocol, timeoutMillis, countryCode, false, anonymity);
    }

    public static CompletableFuture<List<GProxy>> scrapeDefault(Protocol protocol, long timeoutMillis, CountryCode countryCode, boolean ssl) {
        return scrapeDefault(protocol, timeoutMillis, countryCode, ssl, Anonymity.Any);
    }

    public static CompletableFuture<List<GProxy>> scrapeDefault(Protocol protocol, long timeoutMillis, CountryCode countryCode, boolean ssl, Anonymity anonymity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<List<GProxy>>> list = List.of(
                    GProxyScraperBuilder.setupProxyScrape(protocol, timeoutMillis)
                            .country(countryCode)
                            .ssl(ssl)
                            .anonymity(anonymity)
                            .build().scrape(executor),
                    GProxyScraperBuilder.setupGeonode(protocol, timeoutMillis)
                            .country(countryCode)
                            .anonymity(anonymity)
                            .build().scrape(executor)
            );

            try {
                executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return list.stream().filter(CompletableFuture::isDone).map(CompletableFuture::join).collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
        });
    }

    public CompletableFuture<List<GProxy>> scrape() {
        return scrape(Runnable::run);
    }

    public CompletableFuture<List<GProxy>> scrape(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
                if (response.statusCode() == 200) return response.body()
                        .filter(str -> !str.isEmpty())
                        .filter(str -> str.contains(":"))
                        .collect(ArrayList::new, (list, item) -> {
                            String[] tokens = item.split(":");
                            list.add(new GProxy(tokens[0], Integer.parseInt(tokens[1]), targetType));
                        }, ArrayList::addAll);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            return List.of();
        }, executor);
    }

    public static class GeonodeScraper extends GProxyScraper {
        private static final Gson GSON = new GsonBuilder().create();

        public GeonodeScraper(URI targetUri, Protocol targetType, long timeout) {
            super(targetUri, targetType, timeout);
        }

        @Override
        public CompletableFuture<List<GProxy>> scrape(Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpResponse<String> reponse = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (reponse.statusCode() == 200) {
                        String responseString = reponse.body();
                        if (responseString.isEmpty()) throw new RuntimeException("Empty response from Geonode.");

                        JsonArray proxies = GSON.fromJson(reponse.body(), JsonObject.class).get("data").getAsJsonArray();

                        return StreamSupport.stream(proxies.spliterator(), false)
                                .filter(JsonElement::isJsonObject)
                                .map(JsonElement::getAsJsonObject)
                                .filter(jsonObject -> jsonObject.get("responseTime").getAsInt() <= timeoutMillis)
                                .collect(ArrayList::new, (list, jsonObject) -> {
                                    list.add(new GProxy(
                                            jsonObject.get("ip").getAsString(),
                                            jsonObject.get("port").getAsInt(),
                                            targetType
                                    ));
                                }, ArrayList::addAll);
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return List.of();
            }, executor);
        }
    }
}
