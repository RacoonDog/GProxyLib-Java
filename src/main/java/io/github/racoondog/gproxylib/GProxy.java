package io.github.racoondog.gproxylib;


import io.github.racoondog.gproxylib.enums.Protocol;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.http.HttpClient;
import java.util.Objects;

public final class GProxy {
    public final String ip;
    public final int port;
    public final Protocol type;
    @Nullable
    private Proxy proxy = null;
    @Nullable
    private InetSocketAddress address = null;
    @Nullable
    private HttpClient client = null;

    public GProxy(String ip, int port, Protocol type) {
        if (port < 0 || port > 0xFFFF) throw new IllegalArgumentException("Port out of range: " + port);
        Objects.requireNonNull(ip);
        this.ip = ip;
        this.port = port;
        this.type = type;
    }

    public InetSocketAddress getAddress() {
        if (address == null) address = new InetSocketAddress(ip, port);
        return address;
    }

    public boolean isUnresolved() {
        return getAddress().isUnresolved();
    }

    public Proxy asProxy() {
        if (proxy == null) proxy = new Proxy(type.type, getAddress());
        return proxy;
    }

    public HttpClient createClient() {
        if (client == null) client = HttpClient.newBuilder()
                .proxy(new SimpleProxySelector(asProxy()))
                .build();
        return client;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        var other = (GProxy) obj;
        return this.type == other.type &&
                this.port == other.port &&
                this.ip.equals(other.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, type);
    }

    @Override
    public String toString() {
        return "GProxy[" +
                "ip=" + ip + ", " +
                "port=" + port + ", " +
                "type=" + type + ']';
    }
}
