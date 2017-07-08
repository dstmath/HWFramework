package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.io.InputStream;
import java.net.Socket;

/* renamed from: ac */
public class ac implements IPushChannel {
    private Socket socket;

    public ac(Context context) {
    }

    public boolean a(Socket socket) {
        aw.d("PushLog2828", "enter NormalChannel:init(" + socket.getRemoteSocketAddress() + ")");
        if (socket.isConnected()) {
            this.socket = socket;
            return true;
        }
        aw.e("PushLog2828", "when init SSL Channel, socket is not ready:" + socket);
        return false;
    }

    public boolean a(byte[] bArr) {
        if (this.socket == null || this.socket.getOutputStream() == null) {
            aw.e("PushLog2828", "when call send, socket is not ready!!");
            return false;
        }
        this.socket.getOutputStream().write(bArr);
        this.socket.getOutputStream().flush();
        return true;
    }

    public ChannelType bq() {
        return ChannelType.aE;
    }

    public void close() {
        if (this.socket != null) {
            this.socket.close();
        }
    }

    public InputStream getInputStream() {
        if (this.socket != null) {
            try {
                return this.socket.getInputStream();
            } catch (Throwable e) {
                aw.d("PushLog2828", "call socket.getInputStream cause:" + e.toString(), e);
            }
        }
        return null;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public boolean hasConnection() {
        return this.socket != null && this.socket.isConnected();
    }
}
