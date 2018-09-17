package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.net.Proxy;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.b.d;
import com.huawei.android.pushagent.datatype.exception.PushException;
import com.huawei.android.pushagent.datatype.exception.PushException.ErrorType;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.channel.a.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class a extends Thread {
    private d al = null;
    protected b am = null;
    protected Context an = null;

    protected abstract void fm();

    public a(b bVar) {
        super("SocketRead_" + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        this.am = bVar;
        this.an = bVar.at;
        this.al = bVar.au;
    }

    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        Bundle bundle = new Bundle();
        try {
            if (fw()) {
                currentTimeMillis = System.currentTimeMillis();
                fm();
            }
            c.sg("PushLog2951", "normal to quit.");
            c.sh("PushLog2951", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.am.av != null) {
                try {
                    this.am.av.gs();
                } catch (Exception e) {
                    c.sf("PushLog2951", e.toString());
                }
            }
        } catch (Throwable e2) {
            c.se("PushLog2951", "connect occurs :" + e2.toString(), e2);
            Serializable serializable = e2.type;
            if (serializable != null) {
                bundle.putSerializable("errorType", serializable);
            }
            c.sh("PushLog2951", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.am.av != null) {
                try {
                    this.am.av.gs();
                } catch (Exception e3) {
                    c.sf("PushLog2951", e3.toString());
                }
            }
        } catch (Throwable e22) {
            c.se("PushLog2951", "connect cause :" + e22.toString(), e22);
            c.sh("PushLog2951", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.am.av != null) {
                try {
                    this.am.av.gs();
                } catch (Exception e32) {
                    c.sf("PushLog2951", e32.toString());
                }
            }
        } catch (Throwable th) {
            c.sh("PushLog2951", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.am.av != null) {
                try {
                    this.am.av.gs();
                } catch (Exception e4) {
                    c.sf("PushLog2951", e4.toString());
                }
            }
        }
        c.sg("PushLog2951", "connect thread exit!");
        fz(SocketReadThread$SocketEvent.SocketEvent_CLOSE, bundle);
    }

    private boolean fw() {
        Socket socket = null;
        try {
            c.sg("PushLog2951", "start to create socket");
            long currentTimeMillis = System.currentTimeMillis();
            if (this.al == null || this.al.xd() == null || this.al.xd().length() == 0) {
                c.sf("PushLog2951", "the addr is " + this.al + " is invalid");
                return false;
            }
            socket = fx(this.al.xd(), this.al.xe(), this.al.xf());
            if (socket == null) {
                throw new PushException("create socket failed", ErrorType.Err_Connect);
            }
            this.am.av = new b(this.an);
            if (this.am.av.gt(socket)) {
                socket.setSoTimeout(0);
                c.sh("PushLog2951", "connect success cost " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
                if (this.am.av.gu()) {
                    com.huawei.android.pushagent.a.a.xv(53);
                    this.am.fs(SocketReadThread$SocketEvent.SocketEvent_CONNECTED, new Bundle());
                    return true;
                }
                c.sf("PushLog2951", "Socket connect failed");
                throw new PushException("create SSLSocket failed", ErrorType.Err_Connect);
            }
            com.huawei.android.pushagent.a.a.xv(52);
            c.sf("PushLog2951", "call connectMode.protocol.init failed!!");
            socket.close();
            throw new PushException("init socket error", ErrorType.Err_Connect);
        } catch (Throwable e) {
            c.sf("PushLog2951", "call connectSync cause:" + com.huawei.android.pushagent.utils.b.tj(e));
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e2) {
                    c.si("PushLog2951", "close socket cause:" + e2.toString(), e2);
                }
            }
            throw new PushException(e, ErrorType.Err_Connect);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f2 A:{SYNTHETIC, Splitter: B:30:0x00f2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Socket fx(String str, int i, boolean z) {
        Throwable e;
        boolean z2 = true;
        Socket socket;
        try {
            socket = new Socket();
            try {
                Object property;
                int parseInt;
                if (this instanceof com.huawei.android.pushagent.model.channel.entity.a.b) {
                    if (com.huawei.android.pushagent.utils.tools.d.qo()) {
                        c.sh("PushLog2951", "isSupportCtrlSocketV2, ctrlSocket");
                        com.huawei.android.pushagent.utils.tools.d.qp(1, com.huawei.android.pushagent.utils.b.tk(socket));
                    } else {
                        com.huawei.android.pushagent.utils.b.tl(1, com.huawei.android.pushagent.utils.b.tk(socket));
                    }
                }
                if (VERSION.SDK_INT >= 11) {
                    property = System.getProperty("http.proxyHost");
                    String property2 = System.getProperty("http.proxyPort");
                    if (property2 == null) {
                        property2 = "-1";
                    }
                    parseInt = Integer.parseInt(property2);
                } else {
                    property = Proxy.getHost(this.an);
                    parseInt = Proxy.getPort(this.an);
                }
                int tm = com.huawei.android.pushagent.utils.b.tm(this.an);
                fz(SocketReadThread$SocketEvent.SocketEvent_CONNECTING, new Bundle());
                c.sh("PushLog2951", "enter createSocket, ip is: " + com.huawei.android.pushagent.utils.a.d.nr(str));
                if (TextUtils.isEmpty(property) || -1 == parseInt) {
                    z2 = false;
                } else if (1 == tm) {
                    z2 = false;
                }
                boolean ar = g.aq(this.an).ar();
                c.sh("PushLog2951", "useProxy is valid:" + z2 + ", allow proxy:" + ar);
                if (z && z2 && ar) {
                    fy(socket, str, i, property, parseInt);
                } else {
                    c.sh("PushLog2951", "create socket without proxy");
                    socket.connect(new InetSocketAddress(str, i), ((int) g.aq(this.an).as()) * 1000);
                }
                socket.setSoTimeout(((int) g.aq(this.an).at()) * 1000);
                return socket;
            } catch (UnsupportedEncodingException e2) {
                e = e2;
                c.se("PushLog2951", "call getBytes cause:" + e.toString(), e);
                com.huawei.android.pushagent.a.a.xv(50);
                if (socket != null) {
                }
                throw new PushException("create socket failed", ErrorType.Err_Connect);
            } catch (SocketException e3) {
                e = e3;
                c.sf("PushLog2951", "call connectSync cause SocketException :" + com.huawei.android.pushagent.utils.b.tj(e));
                com.huawei.android.pushagent.a.a.xv(50);
                if (socket != null) {
                }
                throw new PushException("create socket failed", ErrorType.Err_Connect);
            } catch (IOException e4) {
                e = e4;
                c.sf("PushLog2951", "call connectSync cause IOException:" + com.huawei.android.pushagent.utils.b.tj(e));
                com.huawei.android.pushagent.a.a.xv(50);
                if (socket != null) {
                }
                throw new PushException("create socket failed", ErrorType.Err_Connect);
            } catch (Exception e5) {
                e = e5;
                c.sf("PushLog2951", "call connectSync cause Exception:" + com.huawei.android.pushagent.utils.b.tj(e));
                com.huawei.android.pushagent.a.a.xv(50);
                if (socket != null) {
                }
                throw new PushException("create socket failed", ErrorType.Err_Connect);
            }
        } catch (UnsupportedEncodingException e6) {
            e = e6;
            socket = null;
            c.se("PushLog2951", "call getBytes cause:" + e.toString(), e);
            com.huawei.android.pushagent.a.a.xv(50);
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e7) {
                    c.sf("PushLog2951", "call socket.close cause IOException:" + com.huawei.android.pushagent.utils.b.tj(e7));
                }
            }
            throw new PushException("create socket failed", ErrorType.Err_Connect);
        } catch (SocketException e8) {
            e7 = e8;
            socket = null;
            c.sf("PushLog2951", "call connectSync cause SocketException :" + com.huawei.android.pushagent.utils.b.tj(e7));
            com.huawei.android.pushagent.a.a.xv(50);
            if (socket != null) {
            }
            throw new PushException("create socket failed", ErrorType.Err_Connect);
        } catch (IOException e9) {
            e7 = e9;
            socket = null;
            c.sf("PushLog2951", "call connectSync cause IOException:" + com.huawei.android.pushagent.utils.b.tj(e7));
            com.huawei.android.pushagent.a.a.xv(50);
            if (socket != null) {
            }
            throw new PushException("create socket failed", ErrorType.Err_Connect);
        } catch (Exception e10) {
            e7 = e10;
            socket = null;
            c.sf("PushLog2951", "call connectSync cause Exception:" + com.huawei.android.pushagent.utils.b.tj(e7));
            com.huawei.android.pushagent.a.a.xv(50);
            if (socket != null) {
            }
            throw new PushException("create socket failed", ErrorType.Err_Connect);
        }
    }

    private void fy(Socket socket, String str, int i, String str2, int i2) {
        if (socket == null) {
            c.sf("PushLog2951", "socket is null");
            return;
        }
        c.sh("PushLog2951", "use Proxy " + str2 + ":" + i2 + " to connect to push server.");
        socket.connect(new InetSocketAddress(str2, i2), ((int) g.aq(this.an).as()) * 1000);
        String str3 = "CONNECT " + str + ":" + i;
        socket.getOutputStream().write((str3 + " HTTP/1.1\r\nHost: " + str3 + "\r\n\r\n").getBytes("UTF-8"));
        InputStream inputStream = socket.getInputStream();
        StringBuilder stringBuilder = new StringBuilder(100);
        int i3 = 0;
        do {
            char read = (char) inputStream.read();
            stringBuilder.append(read);
            if ((i3 == 0 || i3 == 2) && read == 13) {
                i3++;
            } else if ((i3 == 1 || i3 == 3) && read == 10) {
                i3++;
            } else {
                i3 = 0;
            }
        } while (i3 != 4);
        str3 = com.huawei.android.pushagent.utils.d.uv(new BufferedReader(new StringReader(stringBuilder.toString())));
        if (str3 != null) {
            c.sg("PushLog2951", "read data:" + com.huawei.android.pushagent.utils.a.d.nr(str3));
        }
    }

    private void fz(SocketReadThread$SocketEvent socketReadThread$SocketEvent, Bundle bundle) {
        this.am.fs(socketReadThread$SocketEvent, bundle);
    }
}
