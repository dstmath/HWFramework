package android.net;

import android.util.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class PrivateDnsConnectivityChecker {
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int PRIVATE_DNS_PORT = 853;
    private static final String TAG = "NetworkUtils";

    private PrivateDnsConnectivityChecker() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
        if (r4 != null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0059, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        throw r6;
     */
    public static boolean canConnectToPrivateDnsServer(String hostname) {
        SocketFactory factory = SSLSocketFactory.getDefault();
        TrafficStats.setThreadStatsTag(TrafficStats.TAG_SYSTEM_APP);
        try {
            SSLSocket socket = (SSLSocket) factory.createSocket();
            socket.setSoTimeout(5000);
            socket.connect(new InetSocketAddress(hostname, 853));
            if (!socket.isConnected()) {
                Log.w(TAG, String.format("Connection to %s failed.", hostname));
                socket.close();
                return false;
            }
            socket.startHandshake();
            Log.w(TAG, String.format("TLS handshake to %s succeeded.", hostname));
            socket.close();
            return true;
        } catch (IOException e) {
            Log.w(TAG, String.format("TLS handshake to %s failed.", hostname), e);
            return false;
        }
    }
}
