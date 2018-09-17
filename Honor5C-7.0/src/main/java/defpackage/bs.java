package defpackage;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/* renamed from: bs */
class bs extends SSLSocketFactory {
    private SSLSocketFactory cl;

    public bs(SSLSocketFactory sSLSocketFactory) {
        this.cl = sSLSocketFactory;
    }

    private void d(Socket socket) {
        aw.d("PushLog2828", "enter setEnableSafeCipherSuites");
        if (socket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) socket;
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            if (enabledCipherSuites == null || enabledCipherSuites.length == 0) {
                aw.w("PushLog2828", "Current enabled cipherSuites is invalid!");
                return;
            }
            List arrayList = new ArrayList();
            for (String str : enabledCipherSuites) {
                if (!str.contains("RC4")) {
                    arrayList.add(str);
                }
            }
            sSLSocket.setEnabledCipherSuites((String[]) arrayList.toArray(new String[arrayList.size()]));
            return;
        }
        aw.e("PushLog2828", "socket is not instanceof SSLSocket");
    }

    public Socket createSocket() {
        Socket createSocket = this.cl.createSocket();
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(String str, int i) {
        Socket createSocket = this.cl.createSocket(str, i);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) {
        Socket createSocket = this.cl.createSocket(str, i, inetAddress, i2);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(InetAddress inetAddress, int i) {
        Socket createSocket = this.cl.createSocket(inetAddress, i);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) {
        Socket createSocket = this.cl.createSocket(inetAddress, i, inetAddress2, i2);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) {
        Socket createSocket = this.cl.createSocket(socket, str, i, z);
        d(createSocket);
        return createSocket;
    }

    public String[] getDefaultCipherSuites() {
        return this.cl.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.cl.getSupportedCipherSuites();
    }
}
