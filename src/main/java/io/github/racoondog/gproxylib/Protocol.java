package io.github.racoondog.gproxylib;

import java.net.Proxy;

public enum Protocol {
    Http(Proxy.Type.HTTP),
    Socks4(Proxy.Type.SOCKS),
    Socks5(Proxy.Type.SOCKS);

    public final Proxy.Type type;

    Protocol(Proxy.Type type) {
        this.type = type;
    }
}
