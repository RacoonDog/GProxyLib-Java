package io.github.racoondog.gproxylib.tester;

import io.github.racoondog.gproxylib.GProxy;

import java.net.URI;
import java.net.URISyntaxException;
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
    private final HttpRequest request;
    private final long timeout;
    private final ExecutorService executor;

    public GProxyTester(Iterable<GProxy> proxies, String testUrl) {
        this(proxies, testUrl, 200);
    }

    public GProxyTester(Iterable<GProxy> proxies, String testUrl, int threads) {
        this(proxies, testUrl, threads, 5);
    }

    public GProxyTester(Iterable<GProxy> proxies, String testUrl, int threads, long timeout) {
        if (threads < 0) throw new IllegalArgumentException("Threads out of range: " + threads);

        try {
            this.request = HttpRequest.newBuilder(new URI(Objects.requireNonNull(testUrl)))
                    .timeout(Duration.of(timeout, ChronoUnit.SECONDS))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        this.proxies = Objects.requireNonNull(proxies);
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
        if (proxy.isUnresolved()) return null;

        try {
            HttpResponse<Void> response = proxy.createClient().send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200 ? proxy : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
