package com.huawei.android.pushagent.model.channel.a;

import android.content.Context;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.c;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;

public class b implements a {
    private static byte[] bo;
    private static byte[] br;
    private static byte[] bs;
    private boolean bn = false;
    private Context bp;
    private Socket bq;

    public b(Context context) {
        this.bp = context;
    }

    public synchronized boolean gt(Socket socket) {
        if (hc(socket)) {
            this.bq = socket;
            try {
                byte[] gz = gz(this.bp);
                OutputStream outputStream = this.bq.getOutputStream();
                if (outputStream == null || gz.length == 0) {
                    c.sj("PushLog2951", "outputStream or secureKeyExchangeReqData is null");
                    gs();
                    return false;
                }
                c.sh("PushLog2951", "process cmdid to send to pushSrv" + com.huawei.android.pushagent.utils.d.b.sc((byte) 22));
                outputStream.write(gz);
                outputStream.flush();
                InputStream inputStream = this.bq.getInputStream();
                if (hc(socket)) {
                    int read = inputStream.read();
                    if (-1 == read) {
                        c.sh("PushLog2951", " read -1 when init secure channel, socket maybe closed");
                    } else if (23 == read) {
                        c.sh("PushLog2951", "process cmdid to receive from pushSrv" + com.huawei.android.pushagent.utils.d.b.sc((byte) 23));
                        gz = ha(inputStream);
                        if (gz != null) {
                            hf(gz);
                            hg(e.nx(gz, br));
                            this.bn = true;
                            c.sh("PushLog2951", "CustSecureChannel isInitialized success!");
                            return true;
                        }
                        c.sh("PushLog2951", "get server key error");
                    } else {
                        c.sh("PushLog2951", "cmdId is not CMD_SECUREKEYEXCHANGE_RSP");
                    }
                }
                gs();
                return false;
            } catch (Throwable e) {
                c.se("PushLog2951", "call send cause:" + e.toString(), e);
            }
        } else {
            gs();
            return false;
        }
    }

    public synchronized void gs() {
        c.sg("PushLog2951", "enter pushChannel:close()");
        this.bn = false;
        try {
            if (this.bq == null || (this.bq.isClosed() ^ 1) == 0) {
                c.sj("PushLog2951", "socket has been closed");
            } else {
                this.bq.close();
            }
            this.bq = null;
        } catch (Throwable e) {
            c.se("PushLog2951", "close socket error: " + e.toString(), e);
            this.bq = null;
        } catch (Throwable th) {
            this.bq = null;
        }
        return;
    }

    public synchronized boolean gx(byte[] bArr) {
        if (this.bq == null) {
            c.sj("PushLog2951", "socket is null");
            return false;
        } else if (this.bn) {
            try {
                byte[] gy = gy(bArr, false);
                OutputStream outputStream = this.bq.getOutputStream();
                if (outputStream == null) {
                    c.sj("PushLog2951", "outputStream is null");
                    return false;
                } else if (gy.length == 0) {
                    c.sj("PushLog2951", "data is null");
                    return false;
                } else {
                    outputStream.write(gy);
                    outputStream.flush();
                    return true;
                }
            } catch (Throwable e) {
                c.se("PushLog2951", "call send cause:" + e.toString(), e);
                gs();
                return false;
            }
        } else {
            c.sj("PushLog2951", "secure socket is not initialized, can not write any data");
            gs();
            return false;
        }
    }

    public boolean gu() {
        if (this.bq != null) {
            return this.bq.isConnected();
        }
        c.sj("PushLog2951", "socket is null");
        return false;
    }

    private boolean hc(Socket socket) {
        if (socket == null) {
            c.sj("PushLog2951", "socket is null");
            return false;
        } else if (socket.isConnected()) {
            return true;
        } else {
            c.sj("PushLog2951", "when init Channel, socket is not ready");
            return false;
        }
    }

    public Socket gw() {
        return this.bq;
    }

    public InputStream gv() {
        try {
            if (this.bq != null) {
                return new c(this, this.bq.getInputStream());
            }
            c.sj("PushLog2951", "socket is null");
            return null;
        } catch (Throwable e) {
            c.se("PushLog2951", "call socket.getInputStream cause:" + e.toString(), e);
        }
    }

    private byte[] gz(Context context) {
        byte db = (byte) g.aq(context).db();
        String dc = g.aq(context).dc();
        byte[] bArr = new byte[16];
        new SecureRandom().nextBytes(bArr);
        c.sg("PushLog2951", "ready to send SecureChannelReqMessage, save clientKey for decode serverKey");
        he(bArr);
        byte[] ny = e.ny(bArr, dc);
        if (ny == null) {
            c.sj("PushLog2951", "rsa encrypr clientKey error");
            return new byte[0];
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(22);
        byteArrayOutputStream.write(com.huawei.android.pushagent.utils.b.uq(((ny.length + 1) + 1) + 2));
        byteArrayOutputStream.write(db);
        byteArrayOutputStream.write(ny);
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] ha(InputStream inputStream) {
        hd(inputStream, new byte[2]);
        byte[] bArr = new byte[1];
        hd(inputStream, bArr);
        byte b = bArr[0];
        c.sg("PushLog2951", "login result is " + b);
        if (b == (byte) 0) {
            bArr = new byte[48];
            hd(inputStream, bArr);
            return bArr;
        }
        c.sj("PushLog2951", "secure key exchange error");
        return null;
    }

    private static void hd(InputStream inputStream, byte[] bArr) {
        int i = 0;
        while (i < bArr.length) {
            int read = inputStream.read(bArr, i, bArr.length - i);
            if (-1 == read) {
                a.xv(89);
                throw new IOException("read length return -1, invalid length");
            }
            i += read;
        }
    }

    public static byte[] gy(byte[] bArr, boolean z) {
        byte[] nz;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(48);
        if (z) {
            nz = e.nz(bArr, bs, bo);
        } else {
            nz = e.oa(bArr, bs);
        }
        if (nz == null) {
            c.sg("PushLog2951", "aes encrypt pushMsgData error");
            return new byte[0];
        }
        byteArrayOutputStream.write(com.huawei.android.pushagent.utils.b.uq((nz.length + 1) + 2));
        byteArrayOutputStream.write(nz);
        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] hb(InputStream inputStream) {
        byte[] bArr = new byte[2];
        hd(inputStream, bArr);
        bArr = new byte[(com.huawei.android.pushagent.utils.b.tw(bArr) - 3)];
        hd(inputStream, bArr);
        return bArr;
    }

    private static void he(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            c.sg("PushLog2951", "key is null");
            return;
        }
        br = new byte[bArr.length];
        System.arraycopy(bArr, 0, br, 0, bArr.length);
    }

    private static void hg(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            c.sg("PushLog2951", "key is null");
            return;
        }
        bs = new byte[bArr.length];
        System.arraycopy(bArr, 0, bs, 0, bArr.length);
    }

    private static void hf(byte[] bArr) {
        if (bArr == null || bArr.length < 16) {
            c.sf("PushLog2951", "iv is null");
            return;
        }
        bo = new byte[16];
        System.arraycopy(bArr, 0, bo, 0, 16);
    }
}
