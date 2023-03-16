package io.github.racoondog.gproxylib;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class SimpleProxySelector extends ProxySelector {
    private final List<Proxy> list;

    public SimpleProxySelector(Proxy proxy) {
        list = List.of(proxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        /* ignore */
    }

    @Override
    public List<Proxy> select(URI uri) {
        return list;
    }
}
