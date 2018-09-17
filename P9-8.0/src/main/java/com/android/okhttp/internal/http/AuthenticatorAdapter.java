package com.android.okhttp.internal.http;

import com.android.okhttp.Authenticator;
import com.android.okhttp.Credentials;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.squareup.okhttp.Challenge;
import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

public final class AuthenticatorAdapter implements Authenticator {
    public static final Authenticator INSTANCE = new AuthenticatorAdapter();

    public Request authenticate(Proxy proxy, Response response) throws IOException {
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        HttpUrl url = request.httpUrl();
        int size = challenges.size();
        for (int i = 0; i < size; i++) {
            com.android.okhttp.Challenge challenge = (com.android.okhttp.Challenge) challenges.get(i);
            if ("Basic".equalsIgnoreCase(challenge.getScheme())) {
                PasswordAuthentication auth = java.net.Authenticator.requestPasswordAuthentication(url.host(), getConnectToInetAddress(proxy, url), url.port(), url.scheme(), challenge.getRealm(), challenge.getScheme(), url.url(), RequestorType.SERVER);
                if (auth != null) {
                    return request.newBuilder().header("Authorization", Credentials.basic(auth.getUserName(), new String(auth.getPassword()))).build();
                }
            }
        }
        return null;
    }

    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        HttpUrl url = request.httpUrl();
        int size = challenges.size();
        for (int i = 0; i < size; i++) {
            com.android.okhttp.Challenge challenge = (com.android.okhttp.Challenge) challenges.get(i);
            if ("Basic".equalsIgnoreCase(challenge.getScheme())) {
                InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
                PasswordAuthentication auth = java.net.Authenticator.requestPasswordAuthentication(proxyAddress.getHostName(), getConnectToInetAddress(proxy, url), proxyAddress.getPort(), url.scheme(), challenge.getRealm(), challenge.getScheme(), url.url(), RequestorType.PROXY);
                if (auth != null) {
                    return request.newBuilder().header("Proxy-Authorization", Credentials.basic(auth.getUserName(), new String(auth.getPassword()))).build();
                }
            }
        }
        return null;
    }

    private InetAddress getConnectToInetAddress(Proxy proxy, HttpUrl url) throws IOException {
        if (proxy == null || proxy.type() == Type.DIRECT) {
            return InetAddress.getByName(url.host());
        }
        return ((InetSocketAddress) proxy.address()).getAddress();
    }
}
