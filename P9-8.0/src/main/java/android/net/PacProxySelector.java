package android.net;

import android.content.IntentFilter;
import android.os.ServiceManager;
import android.util.Log;
import com.android.net.IProxyService;
import com.android.net.IProxyService.Stub;
import com.google.android.collect.Lists;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class PacProxySelector extends ProxySelector {
    private static final String PROXY = "PROXY ";
    public static final String PROXY_SERVICE = "com.android.net.IProxyService";
    private static final String SOCKS = "SOCKS ";
    private static final String TAG = "PacProxySelector";
    private final List<Proxy> mDefaultList;
    private IProxyService mProxyService = Stub.asInterface(ServiceManager.getService(PROXY_SERVICE));

    public PacProxySelector() {
        if (this.mProxyService == null) {
            Log.e(TAG, "PacManager: no proxy service");
        }
        this.mDefaultList = Lists.newArrayList(new Proxy[]{Proxy.NO_PROXY});
    }

    public List<Proxy> select(URI uri) {
        if (this.mProxyService == null) {
            this.mProxyService = Stub.asInterface(ServiceManager.getService(PROXY_SERVICE));
        }
        if (this.mProxyService == null) {
            Log.e(TAG, "select: no proxy service return NO_PROXY");
            return Lists.newArrayList(new Proxy[]{Proxy.NO_PROXY});
        }
        String urlString;
        String response = null;
        try {
            if (!IntentFilter.SCHEME_HTTP.equalsIgnoreCase(uri.getScheme())) {
                uri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/", null, null);
            }
            urlString = uri.toURL().toString();
        } catch (URISyntaxException e) {
            urlString = uri.getHost();
        } catch (MalformedURLException e2) {
            urlString = uri.getHost();
        }
        try {
            response = this.mProxyService.resolvePacFile(uri.getHost(), urlString);
        } catch (Exception e3) {
            Log.e(TAG, "Error resolving PAC File", e3);
        }
        if (response == null) {
            return this.mDefaultList;
        }
        return parseResponse(response);
    }

    private static List<Proxy> parseResponse(String response) {
        String[] split = response.split(";");
        List<Proxy> ret = Lists.newArrayList();
        for (String s : split) {
            String trimmed = s.trim();
            Proxy proxy;
            if (trimmed.equals("DIRECT")) {
                ret.add(Proxy.NO_PROXY);
            } else if (trimmed.startsWith(PROXY)) {
                proxy = proxyFromHostPort(Type.HTTP, trimmed.substring(PROXY.length()));
                if (proxy != null) {
                    ret.add(proxy);
                }
            } else if (trimmed.startsWith(SOCKS)) {
                proxy = proxyFromHostPort(Type.SOCKS, trimmed.substring(SOCKS.length()));
                if (proxy != null) {
                    ret.add(proxy);
                }
            }
        }
        if (ret.size() == 0) {
            ret.add(Proxy.NO_PROXY);
        }
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{ExcHandler: java.lang.NumberFormatException (r0_0 'e' java.lang.RuntimeException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x001b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x001c, code:
            android.util.Log.d(TAG, "Unable to parse proxy " + r8 + android.net.wifi.WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + r0);
     */
    /* JADX WARNING: Missing block: B:5:0x0042, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Proxy proxyFromHostPort(Type type, String hostPortString) {
        try {
            String[] hostPort = hostPortString.split(":");
            return new Proxy(type, InetSocketAddress.createUnresolved(hostPort[0], Integer.parseInt(hostPort[1])));
        } catch (RuntimeException e) {
        }
    }

    public void connectFailed(URI uri, SocketAddress address, IOException failure) {
    }
}
