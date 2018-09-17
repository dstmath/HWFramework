package android.vr;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.util.Slog;
import android.vr.IVRManagerService.Stub;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.microedition.khronos.opengles.GL10;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class VRManagerService extends Stub {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "VRManagerService";
    private static final String VR_SOCKET = "vr_daemon";
    private final byte[] buf;
    private Context mContext;
    private InputStream mIn;
    private OutputStream mOut;
    private LocalSocket mSocket;

    public VRManagerService(Context context) {
        this.buf = new byte[GL10.GL_STENCIL_BUFFER_BIT];
        this.mContext = context;
    }

    private boolean connect() {
        if (this.mSocket != null) {
            return true;
        }
        try {
            this.mSocket = new LocalSocket();
            this.mSocket.connect(new LocalSocketAddress(VR_SOCKET, Namespace.RESERVED));
            this.mIn = this.mSocket.getInputStream();
            this.mOut = this.mSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            disconnect();
            return LOCAL_DEBUG;
        }
    }

    private void disconnect() {
        IoUtils.closeQuietly(this.mSocket);
        IoUtils.closeQuietly(this.mIn);
        IoUtils.closeQuietly(this.mOut);
        this.mSocket = null;
        this.mIn = null;
        this.mOut = null;
    }

    private boolean readFully(byte[] buffer, int len) {
        try {
            Streams.readFully(this.mIn, buffer, 0, len);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "read exception");
            disconnect();
            return LOCAL_DEBUG;
        }
    }

    private int readReply() {
        if (!readFully(this.buf, 2)) {
            return -1;
        }
        int len = (this.buf[0] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) | ((this.buf[1] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) << 8);
        if (len < 1 || len > this.buf.length) {
            Slog.e(TAG, "invalid reply length (" + len + ")");
            disconnect();
            return -1;
        } else if (readFully(this.buf, len)) {
            return len;
        } else {
            return -1;
        }
    }

    private boolean writeCommand(String cmdString) {
        byte[] cmd = cmdString.getBytes(StandardCharsets.UTF_8);
        int len = cmd.length;
        if (len < 1 || len > this.buf.length) {
            return LOCAL_DEBUG;
        }
        this.buf[0] = (byte) (len & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        this.buf[1] = (byte) ((len >> 8) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        try {
            this.mOut.write(this.buf, 0, 2);
            this.mOut.write(cmd, 0, len);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "write error");
            disconnect();
            return LOCAL_DEBUG;
        }
    }

    private synchronized String transact(String cmd) {
        if (!connect()) {
            Slog.e(TAG, "connection failed");
            return "-1";
        } else if (writeCommand(cmd)) {
            int replyLength = readReply();
            if (replyLength > 0) {
                return new String(this.buf, 0, replyLength, StandardCharsets.UTF_8);
            }
            return "-1";
        } else {
            Slog.e(TAG, "writeCommand failed");
            return "-1";
        }
    }

    public double getVsync() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        String ret = transact(new StringBuilder("vsync").toString());
        int indexOfSpace = ret.indexOf(32);
        int r = Integer.parseInt(ret.substring(0, indexOfSpace));
        if (r == 0) {
            return Double.parseDouble(ret.substring(indexOfSpace + 1));
        }
        Slog.e(TAG, "getCurrentVsync ret=" + r);
        return -1.0d;
    }

    private boolean setFrontBuffer(boolean set) {
        StringBuilder builder = new StringBuilder("setFrontBuffer");
        builder.append(set ? " 1" : " 0");
        String ret = transact(builder.toString());
        Slog.i(TAG, "setFrontBuffer ret=" + ret);
        if (Integer.parseInt(ret) == 0) {
            return true;
        }
        Slog.e(TAG, "setFrontBuffer fail");
        return LOCAL_DEBUG;
    }

    public boolean startFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        return setFrontBuffer(true);
    }

    public boolean stopFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        return setFrontBuffer(LOCAL_DEBUG);
    }

    public int setSchedFifo(int tid, int rtPriority) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        StringBuilder builder = new StringBuilder("setSchedFifo");
        builder.append(" ");
        builder.append(tid);
        builder.append(" ");
        builder.append(rtPriority);
        if (Integer.parseInt(transact(builder.toString())) == 0) {
            return 0;
        }
        Slog.e(TAG, "setSchedFifo fail");
        return -1;
    }
}
