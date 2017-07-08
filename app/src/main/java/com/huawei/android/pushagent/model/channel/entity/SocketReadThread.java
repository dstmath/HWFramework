package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.net.Proxy;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.bd.Reporter;
import defpackage.aa;
import defpackage.ac;
import defpackage.ad;
import defpackage.ae;
import defpackage.au;
import defpackage.aw;
import defpackage.bi;
import defpackage.bv;
import defpackage.j;
import defpackage.r;
import defpackage.z;
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

public abstract class SocketReadThread extends Thread {
    private j ag;
    public ConnectEntity ah;
    public Context mContext;

    public enum SocketEvent {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent.<clinit>():void");
        }
    }

    public SocketReadThread(ConnectEntity connectEntity) {
        super("SocketRead_" + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        this.ag = null;
        this.mContext = null;
        this.ah = null;
        this.ah = connectEntity;
        this.mContext = connectEntity.context;
        this.ag = connectEntity.P;
    }

    private Socket a(String str, int i, boolean z) {
        Throwable e;
        Socket socket;
        try {
            socket = new Socket();
            try {
                String property;
                String str2;
                int parseInt;
                socket.getTcpNoDelay();
                if (this instanceof z) {
                    if (bv.cs()) {
                        aw.i("PushLog2828", "isSupportCtrlSocketV2, ctrlSocket");
                        bv.c(1, au.c(socket));
                    } else {
                        au.ctrlSockets(1, au.c(socket));
                    }
                }
                if (VERSION.SDK_INT >= 11) {
                    String property2 = System.getProperty("http.proxyHost");
                    property = System.getProperty("http.proxyPort");
                    if (property == null) {
                        property = "-1";
                    }
                    str2 = property2;
                    parseInt = Integer.parseInt(property);
                } else {
                    str2 = Proxy.getHost(this.mContext);
                    parseInt = Proxy.getPort(this.mContext);
                }
                int G = au.G(this.mContext);
                a(SocketEvent.aj, new Bundle());
                aw.i("PushLog2828", "enter createSocket " + bi.w(str));
                boolean z2 = (TextUtils.isEmpty(str2) || -1 == parseInt || 1 == G) ? false : true;
                boolean ah = ae.l(this.mContext).ah();
                aw.i("PushLog2828", "useProxy is valid:" + z2 + ", allow proxy:" + ah);
                if (z && z2 && ah) {
                    aw.i("PushLog2828", "use Proxy " + str2 + ":" + parseInt + " to connect to push server.");
                    socket.connect(new InetSocketAddress(str2, parseInt), ((int) ae.l(this.mContext).H()) * 1000);
                    property = "CONNECT " + str + ":" + i;
                    socket.getOutputStream().write((property + " HTTP/1.1\r\nHost: " + property + "\r\n\r\n").getBytes("UTF-8"));
                    InputStream inputStream = socket.getInputStream();
                    StringBuilder stringBuilder = new StringBuilder(100);
                    G = 0;
                    do {
                        char read = (char) inputStream.read();
                        stringBuilder.append(read);
                        G = ((G == 0 || G == 2) && read == '\r') ? G + 1 : ((G == 1 || G == 3) && read == '\n') ? G + 1 : 0;
                    } while (G != 4);
                    property = new BufferedReader(new StringReader(stringBuilder.toString())).readLine();
                    if (property != null) {
                        aw.d("PushLog2828", "read data:" + bi.w(property));
                    }
                } else {
                    aw.i("PushLog2828", "create socket without proxy");
                    socket.connect(new InetSocketAddress(str, i), ((int) ae.l(this.mContext).H()) * 1000);
                }
                aw.i("PushLog2828", "write the lastcontectsucc_time to the pushConfig.xml file");
                socket.setSoTimeout(((int) ae.l(this.mContext).H()) * 1000);
                return socket;
            } catch (UnsupportedEncodingException e2) {
                e = e2;
                aw.d("PushLog2828", "call getBytes cause:" + e.toString(), e);
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Throwable e3) {
                        aw.d("PushLog2828", "close socket cause:" + e3.toString(), e3);
                    }
                }
                throw new PushException("create socket failed", ErrorType.w);
            } catch (SocketException e4) {
                e3 = e4;
                aw.d("PushLog2828", "call setSoTimeout cause:" + e3.toString(), e3);
                if (socket != null) {
                    socket.close();
                }
                throw new PushException("create socket failed", ErrorType.w);
            } catch (IOException e5) {
                e3 = e5;
                aw.d("PushLog2828", "call connect cause:" + e3.toString(), e3);
                if (socket != null) {
                    socket.close();
                }
                throw new PushException("create socket failed", ErrorType.w);
            } catch (Exception e6) {
                e3 = e6;
                aw.d("PushLog2828", "call createSocket cause:" + e3.toString(), e3);
                if (socket != null) {
                    socket.close();
                }
                throw new PushException("create socket failed", ErrorType.w);
            }
        } catch (UnsupportedEncodingException e7) {
            e3 = e7;
            socket = null;
            aw.d("PushLog2828", "call getBytes cause:" + e3.toString(), e3);
            if (socket != null) {
                socket.close();
            }
            throw new PushException("create socket failed", ErrorType.w);
        } catch (SocketException e8) {
            e3 = e8;
            socket = null;
            aw.d("PushLog2828", "call setSoTimeout cause:" + e3.toString(), e3);
            if (socket != null) {
                socket.close();
            }
            throw new PushException("create socket failed", ErrorType.w);
        } catch (IOException e9) {
            e3 = e9;
            socket = null;
            aw.d("PushLog2828", "call connect cause:" + e3.toString(), e3);
            if (socket != null) {
                socket.close();
            }
            throw new PushException("create socket failed", ErrorType.w);
        } catch (Exception e10) {
            e3 = e10;
            socket = null;
            aw.d("PushLog2828", "call createSocket cause:" + e3.toString(), e3);
            if (socket != null) {
                socket.close();
            }
            throw new PushException("create socket failed", ErrorType.w);
        }
    }

    private void a(SocketEvent socketEvent, Bundle bundle) {
        this.ah.a(socketEvent, bundle);
    }

    private boolean bj() {
        Socket socket = null;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            aw.d("PushLog2828", "start to create socket");
            if (this.ag == null || this.ag.D == null || this.ag.D.length() == 0) {
                aw.e("PushLog2828", "the addr is " + this.ag + " is invalid");
                return false;
            } else if (this.ag.E == null) {
                aw.e("PushLog2828", "config sslconetEntity.channelType cfgErr:" + this.ag.E + " cannot connect!!");
                return false;
            } else {
                socket = a(this.ag.D, this.ag.port, this.ag.F);
                aw.i("PushLog2828", "conetEntity.channelType:" + this.ag.E);
                switch (r.ai[this.ag.E.ordinal()]) {
                    case Reporter.ACTIVITY_CREATE /*1*/:
                        this.ah.R = new ac(this.mContext);
                        break;
                    case Reporter.ACTIVITY_RESUME /*2*/:
                        this.ah.R = new ad(this.mContext);
                        break;
                    case Reporter.ACTIVITY_PAUSE /*3*/:
                        this.ah.R = new ad(this.mContext);
                        break;
                    case Reporter.ACTIVITY_DESTROY /*4*/:
                        this.ah.R = new aa(this.mContext);
                        break;
                    default:
                        aw.e("PushLog2828", "conetEntity.channelType is invalid:" + this.ag.E);
                        PushService.c().stopService();
                        socket.close();
                        return false;
                }
                if (this.ah.R.a(socket)) {
                    socket.setSoTimeout(0);
                    aw.i("PushLog2828", "connect cost " + (System.currentTimeMillis() - currentTimeMillis) + " ms, result:" + this.ah.R.hasConnection());
                    if (this.ah.R.hasConnection()) {
                        InetSocketAddress inetSocketAddress = new InetSocketAddress(this.ag.D, this.ag.port);
                        Bundle bundle = new Bundle();
                        bundle.putString("server_ip", inetSocketAddress.getAddress().getHostAddress());
                        bundle.putInt("server_port", inetSocketAddress.getPort());
                        bundle.putString("client_ip", socket.getLocalAddress().getHostAddress());
                        bundle.putInt("client_port", socket.getLocalPort());
                        bundle.putInt("channelEntity", this.ah.ba().ordinal());
                        this.ah.a(SocketEvent.ak, bundle);
                        return true;
                    }
                    aw.e("PushLog2828", "Socket connect failed");
                    throw new PushException("create SSLSocket failed", ErrorType.w);
                }
                aw.e("PushLog2828", "call conetEntity.channel.init failed!!");
                socket.close();
                throw new PushException("init socket error", ErrorType.w);
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", "call connectSync cause " + e.toString(), e);
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e2) {
                    aw.a("PushLog2828", "close socket cause:" + e2.toString(), e2);
                }
            }
            throw new PushException(e, ErrorType.w);
        }
    }

    public abstract void bk();

    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            if (bj()) {
                currentTimeMillis = System.currentTimeMillis();
                bk();
            }
            aw.d("PushLog2828", "normal to quit.");
            Bundle bundle = new Bundle();
            bundle.putLong("connect_time", System.currentTimeMillis() - currentTimeMillis);
            a(SocketEvent.al, bundle);
            aw.i("PushLog2828", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.ah.R != null) {
                try {
                    this.ah.R.close();
                } catch (Exception e) {
                }
            }
        } catch (Throwable e2) {
            aw.d("PushLog2828", "connect occurs :" + e2.toString(), e2);
            Serializable serializable = e2.type;
            Bundle bundle2 = new Bundle();
            if (serializable != null) {
                bundle2.putSerializable("errorType", serializable);
            }
            bundle2.putString("push_exception", e2.toString());
            a(SocketEvent.al, bundle2);
            aw.i("PushLog2828", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.ah.R != null) {
                try {
                    this.ah.R.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable e22) {
            aw.d("PushLog2828", "connect cause :" + e22.toString(), e22);
            Bundle bundle3 = new Bundle();
            bundle3.putString("push_exception", e22.toString());
            a(SocketEvent.al, bundle3);
            aw.i("PushLog2828", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.ah.R != null) {
                try {
                    this.ah.R.close();
                } catch (Exception e4) {
                }
            }
        } catch (Throwable th) {
            aw.i("PushLog2828", "total connect Time:" + (System.currentTimeMillis() - currentTimeMillis) + " process quit, so close socket");
            if (this.ah.R != null) {
                try {
                    this.ah.R.close();
                } catch (Exception e5) {
                }
            }
        }
        aw.d("PushLog2828", "connect thread exit!");
    }
}
