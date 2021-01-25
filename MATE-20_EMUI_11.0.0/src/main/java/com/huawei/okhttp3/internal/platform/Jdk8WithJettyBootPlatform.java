package com.huawei.okhttp3.internal.platform;

import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.internal.Util;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocket;

/* access modifiers changed from: package-private */
public class Jdk8WithJettyBootPlatform extends Platform {
    private final Class<?> clientProviderClass;
    private final Method getMethod;
    private final Method putMethod;
    private final Method removeMethod;
    private final Class<?> serverProviderClass;

    Jdk8WithJettyBootPlatform(Method putMethod2, Method getMethod2, Method removeMethod2, Class<?> clientProviderClass2, Class<?> serverProviderClass2) {
        this.putMethod = putMethod2;
        this.getMethod = getMethod2;
        this.removeMethod = removeMethod2;
        this.clientProviderClass = clientProviderClass2;
        this.serverProviderClass = serverProviderClass2;
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        try {
            this.putMethod.invoke(null, sslSocket, Proxy.newProxyInstance(Platform.class.getClassLoader(), new Class[]{this.clientProviderClass, this.serverProviderClass}, new AlpnProvider(alpnProtocolNames(protocols))));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("failed to set ALPN", e);
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void afterHandshake(SSLSocket sslSocket) {
        try {
            this.removeMethod.invoke(null, sslSocket);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("failed to remove ALPN", e);
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    @Nullable
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            AlpnProvider provider = (AlpnProvider) Proxy.getInvocationHandler(this.getMethod.invoke(null, socket));
            if (!provider.unsupported && provider.selected == null) {
                Platform.get().log(4, "ALPN callback dropped: HTTP/2 is disabled. Is alpn-boot on the boot class path?", null);
                return null;
            } else if (provider.unsupported) {
                return null;
            } else {
                return provider.selected;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("failed to get ALPN selected protocol", e);
        }
    }

    public static Platform buildIfSupported() {
        try {
            Class<?> alpnClass = Class.forName("org.eclipse.jetty.alpn.ALPN", true, null);
            Class<?> providerClass = Class.forName("org.eclipse.jetty.alpn.ALPN$Provider", true, null);
            Class<?> clientProviderClass2 = Class.forName("org.eclipse.jetty.alpn.ALPN$ClientProvider", true, null);
            return new Jdk8WithJettyBootPlatform(alpnClass.getMethod("put", SSLSocket.class, providerClass), alpnClass.getMethod("get", SSLSocket.class), alpnClass.getMethod("remove", SSLSocket.class), clientProviderClass2, Class.forName("org.eclipse.jetty.alpn.ALPN$ServerProvider", true, null));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return null;
        }
    }

    private static class AlpnProvider implements InvocationHandler {
        private final List<String> protocols;
        String selected;
        boolean unsupported;

        AlpnProvider(List<String> protocols2) {
            this.protocols = protocols2;
        }

        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            if (args == null) {
                args = Util.EMPTY_STRING_ARRAY;
            }
            if (methodName.equals("supports") && Boolean.TYPE == returnType) {
                return true;
            }
            if (methodName.equals("unsupported") && Void.TYPE == returnType) {
                this.unsupported = true;
                return null;
            } else if (methodName.equals("protocols") && args.length == 0) {
                return this.protocols;
            } else {
                if ((methodName.equals("selectProtocol") || methodName.equals("select")) && String.class == returnType && args.length == 1 && (args[0] instanceof List)) {
                    List<?> peerProtocols = (List) args[0];
                    int size = peerProtocols.size();
                    for (int i = 0; i < size; i++) {
                        String protocol = (String) peerProtocols.get(i);
                        if (this.protocols.contains(protocol)) {
                            this.selected = protocol;
                            return protocol;
                        }
                    }
                    String str = this.protocols.get(0);
                    this.selected = str;
                    return str;
                } else if ((!methodName.equals("protocolSelected") && !methodName.equals("selected")) || args.length != 1) {
                    return method.invoke(this, args);
                } else {
                    this.selected = (String) args[0];
                    return null;
                }
            }
        }
    }
}
