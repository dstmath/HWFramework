package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/* renamed from: ad */
public class ad implements IPushChannel {
    private SSLSocket aJ;
    private InputStream aK;
    private OutputStream aL;
    private Context mContext;

    public ad(Context context) {
        this.aJ = null;
        this.mContext = context;
    }

    public boolean a(Socket socket) {
        if (socket == null || !socket.isConnected()) {
            aw.e("PushLog2828", "when init SSL Channel, socket is not ready:" + socket);
            return false;
        }
        aw.d("PushLog2828", "enter SSLChannel:init(" + socket.getRemoteSocketAddress() + ")");
        SSLContext instance = SSLContext.getInstance("TLS");
        TrustManagerFactory instance2 = TrustManagerFactory.getInstance("X509");
        KeyStore instance3 = KeyStore.getInstance("BKS");
        InputStream byteArrayInputStream = new ByteArrayInputStream(br.cm());
        byteArrayInputStream.reset();
        instance3.load(byteArrayInputStream, bj.decrypter(ax.bN()).toCharArray());
        byteArrayInputStream.close();
        instance2.init(instance3);
        instance.init(null, instance2.getTrustManagers(), null);
        InetAddress inetAddress = socket.getInetAddress();
        if (inetAddress == null) {
            return false;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, socket.getPort());
        this.aJ = (SSLSocket) instance.getSocketFactory().createSocket(socket, inetAddress.getHostAddress(), socket.getPort(), true);
        if (this.aJ == null) {
            return false;
        }
        this.aJ.setEnabledCipherSuites(ay.bO());
        aw.d("PushLog2828", "server ip:" + inetSocketAddress.getAddress().getHostAddress() + ",server port:" + inetSocketAddress.getPort() + ",socket ip:" + this.aJ.getLocalAddress().getHostAddress() + ",socket port:" + this.aJ.getLocalPort() + ",pkgName:" + this.mContext.getPackageName());
        this.aK = this.aJ.getInputStream();
        this.aL = this.aJ.getOutputStream();
        this.aJ.setSoTimeout(0);
        return true;
    }

    public boolean a(byte[] bArr) {
        try {
            if (this.aL == null || bArr == null) {
                aw.e("PushLog2828", "when send msg:" + Arrays.toString(bArr) + " dos is null, or msg is null");
                return false;
            }
            this.aL.write(bArr);
            this.aL.flush();
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2828", "call send cause:" + e.toString(), e);
            close();
        }
    }

    public ChannelType bq() {
        return ChannelType.aF;
    }

    public synchronized void close() {
        aw.d("PushLog2828", "enter SSLPushChannel:close()");
        try {
            if (this.aK != null) {
                this.aK.close();
            }
            this.aK = null;
        } catch (Throwable e) {
            aw.d("PushLog2828", "close dis error: " + e.toString(), e);
            this.aK = null;
        } catch (Throwable th) {
            this.aK = null;
        }
        try {
            if (this.aL != null) {
                this.aL.close();
            }
            this.aL = null;
        } catch (Throwable e2) {
            aw.d("PushLog2828", "close dos error: " + e2.toString(), e2);
            this.aL = null;
        } catch (Throwable th2) {
            this.aL = null;
        }
        try {
            if (!(this.aJ == null || this.aJ.isClosed())) {
                this.aJ.close();
            }
            this.aJ = null;
        } catch (Throwable e22) {
            aw.d("PushLog2828", "close socket error: " + e22.toString(), e22);
            this.aJ = null;
        } catch (Throwable th3) {
            this.aJ = null;
        }
    }

    public InputStream getInputStream() {
        return this.aK;
    }

    public Socket getSocket() {
        return this.aJ;
    }

    public boolean hasConnection() {
        return (this.aJ == null || !this.aJ.isConnected() || this.aK == null || this.aL == null) ? false : true;
    }
}
