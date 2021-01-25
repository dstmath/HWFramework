package com.huawei.okhttp3;

import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.internal.Util;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public final class Address extends AddressBase {
    @Nullable
    final CertificatePinner certificatePinner;
    final List<ConnectionSpec> connectionSpecs;
    final Dns dns;
    @Nullable
    final HostnameVerifier hostnameVerifier;
    final List<Protocol> protocols;
    @Nullable
    final Proxy proxy;
    final Authenticator proxyAuthenticator;
    final ProxySelector proxySelector;
    final SocketFactory socketFactory;
    @Nullable
    final SSLSocketFactory sslSocketFactory;
    final HttpUrl url;

    public Address(String uriHost, int uriPort, Dns dns2, SocketFactory socketFactory2, @Nullable SSLSocketFactory sslSocketFactory2, @Nullable HostnameVerifier hostnameVerifier2, @Nullable CertificatePinner certificatePinner2, Authenticator proxyAuthenticator2, @Nullable Proxy proxy2, List<Protocol> protocols2, List<ConnectionSpec> connectionSpecs2, ProxySelector proxySelector2) {
        this.url = new HttpUrl.Builder().scheme(sslSocketFactory2 != null ? "https" : "http").host(uriHost).port(uriPort).build();
        if (dns2 != null) {
            this.dns = dns2;
            if (socketFactory2 != null) {
                this.socketFactory = socketFactory2;
                if (proxyAuthenticator2 != null) {
                    this.proxyAuthenticator = proxyAuthenticator2;
                    if (protocols2 != null) {
                        this.protocols = Util.immutableList(protocols2);
                        if (connectionSpecs2 != null) {
                            this.connectionSpecs = Util.immutableList(connectionSpecs2);
                            if (proxySelector2 != null) {
                                this.proxySelector = proxySelector2;
                                this.proxy = proxy2;
                                this.sslSocketFactory = sslSocketFactory2;
                                this.hostnameVerifier = hostnameVerifier2;
                                this.certificatePinner = certificatePinner2;
                                return;
                            }
                            throw new NullPointerException("proxySelector == null");
                        }
                        throw new NullPointerException("connectionSpecs == null");
                    }
                    throw new NullPointerException("protocols == null");
                }
                throw new NullPointerException("proxyAuthenticator == null");
            }
            throw new NullPointerException("socketFactory == null");
        }
        throw new NullPointerException("dns == null");
    }

    public HttpUrl url() {
        return this.url;
    }

    public Dns dns() {
        return this.dns;
    }

    public SocketFactory socketFactory() {
        return this.socketFactory;
    }

    public Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    public List<Protocol> protocols() {
        return this.protocols;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    public ProxySelector proxySelector() {
        return this.proxySelector;
    }

    @Nullable
    public Proxy proxy() {
        return this.proxy;
    }

    @Nullable
    public SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    @Nullable
    public HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    @Nullable
    public CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    public boolean equals(@Nullable Object other) {
        return (other instanceof Address) && this.url.equals(((Address) other).url) && equalsNonHost((Address) other);
    }

    public int hashCode() {
        return (((((((((((((((((((17 * 31) + this.url.hashCode()) * 31) + this.dns.hashCode()) * 31) + this.proxyAuthenticator.hashCode()) * 31) + this.protocols.hashCode()) * 31) + this.connectionSpecs.hashCode()) * 31) + this.proxySelector.hashCode()) * 31) + Objects.hashCode(this.proxy)) * 31) + Objects.hashCode(this.sslSocketFactory)) * 31) + Objects.hashCode(this.hostnameVerifier)) * 31) + Objects.hashCode(this.certificatePinner);
    }

    /* access modifiers changed from: package-private */
    public boolean equalsNonHost(Address that) {
        return this.dns.equals(that.dns) && this.proxyAuthenticator.equals(that.proxyAuthenticator) && this.protocols.equals(that.protocols) && this.connectionSpecs.equals(that.connectionSpecs) && this.proxySelector.equals(that.proxySelector) && Objects.equals(this.proxy, that.proxy) && Objects.equals(this.sslSocketFactory, that.sslSocketFactory) && Objects.equals(this.hostnameVerifier, that.hostnameVerifier) && Objects.equals(this.certificatePinner, that.certificatePinner) && url().port() == that.url().port();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address{");
        sb.append(this.url.host());
        sb.append(":");
        StringBuilder result = sb.append(this.url.port());
        if (this.proxy != null) {
            result.append(", proxy=");
            result.append(this.proxy);
        } else {
            result.append(", proxySelector=");
            result.append(this.proxySelector);
        }
        result.append("}");
        return result.toString();
    }
}
