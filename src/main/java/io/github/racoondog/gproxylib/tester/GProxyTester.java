package io.github.racoondog.gproxylib.tester;

import io.github.racoondog.gproxylib.GProxy;
import io.github.racoondog.gproxylib.SimpleProxySelector;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class GProxyTester {
    private final Iterable<GProxy> proxies;
    private final URI testUri;
    private final int timeout;
    private final ExecutorService executor;

    public GProxyTester(Iterable<GProxy> proxies, String testUrl) {
        this(proxies, testUrl, 200);
    }

    public GProxyTester(Iterable<GProxy> proxies, String testUrl, int threads) {
        this(proxies, testUrl, threads, 5);
    }

    public GProxyTester(Iterable<GProxy> proxies, String testUrl, int threads, int timeout) {
        if (threads < 0) throw new IllegalArgumentException("Threads out of range: " + threads);
        Objects.requireNonNull(proxies);
        Objects.requireNonNull(testUrl);

        try {
            this.testUri = new URI(testUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        this.proxies = proxies;
        this.executor = Executors.newFixedThreadPool(threads);
        this.timeout = timeout;
    }

    public CompletableFuture<List<GProxy>> test() {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<GProxy>> queue = new ArrayList<>();

            for (var proxy : proxies) {
                queue.add(CompletableFuture.supplyAsync(() -> testProxy(proxy), executor));
            }

            try {
                executor.awaitTermination(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return new ArrayList<>();
            }

            List<GProxy> results = new ArrayList<>();

            for (var task : queue) {
                if (task.isDone()) {
                    GProxy result = task.join();
                    if (result != null) results.add(result);
                }
            }

            return results;
        }, this.executor);
    }

    public GProxy testProxy(GProxy proxy) {
        HttpClient client = HttpClient.newBuilder()
                .proxy(new SimpleProxySelector(proxy.asProxy()))
                .connectTimeout(Duration.of(timeout, ChronoUnit.SECONDS))
                .build();

        HttpRequest request = HttpRequest.newBuilder(testUri)
                .GET()
                .build();

        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200 ? proxy : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
