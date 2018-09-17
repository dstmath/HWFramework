package sun.net.www.protocol.gopher;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import sun.net.NetworkClient;
import sun.net.www.MessageHeader;
import sun.net.www.URLConnection;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;

public class GopherClient extends NetworkClient implements Runnable {
    @Deprecated
    public static String gopherProxyHost;
    @Deprecated
    public static int gopherProxyPort;
    @Deprecated
    public static boolean useGopherProxy;
    URLConnection connection;
    String gkey;
    int gtype;
    PipedOutputStream os;
    URL u;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.gopher.GopherClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.gopher.GopherClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.gopher.GopherClient.<clinit>():void");
    }

    GopherClient(URLConnection connection) {
        this.connection = connection;
    }

    public static boolean getUseGopherProxy() {
        return ((Boolean) AccessController.doPrivileged(new GetBooleanAction("gopherProxySet"))).booleanValue();
    }

    public static String getGopherProxyHost() {
        String host = (String) AccessController.doPrivileged(new GetPropertyAction("gopherProxyHost"));
        if ("".equals(host)) {
            return null;
        }
        return host;
    }

    public static int getGopherProxyPort() {
        return ((Integer) AccessController.doPrivileged(new GetIntegerAction("gopherProxyPort", 80))).intValue();
    }

    InputStream openStream(URL u) throws IOException {
        this.u = u;
        this.os = this.os;
        int i = 0;
        String s = u.getFile();
        int limit = s.length();
        int c = 49;
        while (i < limit) {
            c = s.charAt(i);
            if (c != 47) {
                break;
            }
            i++;
        }
        if (c == 47) {
            c = 49;
        }
        this.gtype = c;
        if (i < limit) {
            i++;
        }
        this.gkey = s.substring(i);
        openServer(u.getHost(), u.getPort() <= 0 ? 70 : u.getPort());
        MessageHeader msgh = new MessageHeader();
        switch (this.gtype) {
            case 48:
            case 55:
                msgh.add("content-type", "text/plain");
                break;
            case 49:
                msgh.add("content-type", "text/html");
                break;
            case 73:
            case 103:
                msgh.add("content-type", "image/gif");
                break;
            default:
                msgh.add("content-type", "content/unknown");
                break;
        }
        if (this.gtype != 55) {
            this.serverOutput.print(decodePercent(this.gkey) + "\r\n");
            this.serverOutput.flush();
        } else {
            i = this.gkey.indexOf(63);
            if (i >= 0) {
                this.serverOutput.print(decodePercent(this.gkey.substring(0, i) + "\t" + this.gkey.substring(i + 1) + "\r\n"));
                this.serverOutput.flush();
                msgh.add("content-type", "text/html");
            } else {
                msgh.add("content-type", "text/html");
            }
        }
        this.connection.setProperties(msgh);
        if (msgh.findValue("content-type") != "text/html") {
            return new GopherInputStream(this, this.serverInput);
        }
        this.os = new PipedOutputStream();
        PipedInputStream ret = new PipedInputStream();
        ret.connect(this.os);
        new Thread((Runnable) this).start();
        return ret;
    }

    private String decodePercent(String s) {
        if (s == null || s.indexOf(37) < 0) {
            return s;
        }
        int limit = s.length();
        char[] d = new char[limit];
        int sp = 0;
        int dp = 0;
        while (sp < limit) {
            int c = s.charAt(sp);
            if (c == 37 && sp + 2 < limit) {
                int s1 = s.charAt(sp + 1);
                int s2 = s.charAt(sp + 2);
                if (48 <= s1 && s1 <= 57) {
                    s1 -= 48;
                } else if (97 <= s1 && s1 <= 102) {
                    s1 = (s1 - 97) + 10;
                } else if (65 > s1 || s1 > 70) {
                    s1 = -1;
                } else {
                    s1 = (s1 - 65) + 10;
                }
                if (48 <= s2 && s2 <= 57) {
                    s2 -= 48;
                } else if (97 <= s2 && s2 <= 102) {
                    s2 = (s2 - 97) + 10;
                } else if (65 > s2 || s2 > 70) {
                    s2 = -1;
                } else {
                    s2 = (s2 - 65) + 10;
                }
                if (s1 >= 0 && s2 >= 0) {
                    c = (s1 << 4) | s2;
                    sp += 2;
                }
            }
            int dp2 = dp + 1;
            d[dp] = (char) c;
            sp++;
            dp = dp2;
        }
        return new String(d, 0, dp);
    }

    private String encodePercent(String s) {
        if (s == null) {
            return s;
        }
        int limit = s.length();
        char[] d = null;
        int dp = 0;
        for (int sp = 0; sp < limit; sp++) {
            int c = s.charAt(sp);
            char[] nd;
            if (c <= 32 || c == 34 || c == 37) {
                if (d == null) {
                    d = s.toCharArray();
                }
                if (dp + 3 >= d.length) {
                    nd = new char[(dp + 10)];
                    System.arraycopy(d, 0, nd, 0, dp);
                    d = nd;
                }
                d[dp] = '%';
                int dig = (c >> 4) & 15;
                d[dp + 1] = (char) (dig < 10 ? dig + 48 : dig + 55);
                dig = c & 15;
                d[dp + 2] = (char) (dig < 10 ? dig + 48 : dig + 55);
                dp += 3;
            } else {
                if (d != null) {
                    if (dp >= d.length) {
                        nd = new char[(dp + 10)];
                        System.arraycopy(d, 0, nd, 0, dp);
                        d = nd;
                    }
                    d[dp] = (char) c;
                }
                dp++;
            }
        }
        if (d != null) {
            s = new String(d, 0, dp);
        }
        return s;
    }

    public void run() {
        int qpos = -1;
        try {
            String title;
            int i = this.gtype;
            if (r0 == 55) {
                qpos = this.gkey.indexOf(63);
                if (qpos < 0) {
                    String str = "<html><head><title>Searchable Gopher Index</title></head>\n<body><h1>Searchable Gopher Index</h1><isindex>\n</body></html>\n";
                    new PrintStream(this.os, false, encoding).print(r21);
                    try {
                        closeServer();
                        this.os.close();
                        return;
                    } catch (IOException e) {
                        return;
                    }
                }
            }
            i = this.gtype;
            if (r0 != 49) {
                i = this.gtype;
                if (r0 != 55) {
                    byte[] buf = new byte[Modifier.STRICT];
                    while (true) {
                        try {
                            int n = this.serverInput.read(buf);
                            if (n >= 0) {
                                this.os.write(buf, 0, n);
                            }
                        } catch (Exception e2) {
                        }
                        closeServer();
                        this.os.close();
                        return;
                    }
                }
            }
            PrintStream ps = new PrintStream(this.os, false, encoding);
            i = this.gtype;
            if (r0 == 55) {
                title = "Results of searching for \"" + this.gkey.substring(qpos + 1) + "\" on " + this.u.getHost();
            } else {
                title = "Gopher directory " + this.gkey + " from " + this.u.getHost();
            }
            ps.print("<html><head><title>");
            ps.print(title);
            ps.print("</title></head>\n<body>\n<H1>");
            ps.print(title);
            ps.print("</h1><dl compact>\n");
            DataInputStream ds = new DataInputStream(this.serverInput);
            while (true) {
                String s = ds.readLine();
                if (s != null) {
                    int key;
                    int t1;
                    int t2;
                    int t3;
                    String port;
                    String host;
                    int len = s.length();
                    while (len > 0) {
                        if (s.charAt(len - 1) <= ' ') {
                            len--;
                        } else if (len > 0) {
                            key = s.charAt(0);
                            t1 = s.indexOf(9);
                            if (t1 <= 0) {
                                t2 = s.indexOf(9, t1 + 1);
                            } else {
                                t2 = -1;
                            }
                            if (t2 <= 0) {
                                t3 = s.indexOf(9, t2 + 1);
                            } else {
                                t3 = -1;
                            }
                            if (t3 < 0) {
                                continue;
                            } else {
                                if (t3 + 1 >= len) {
                                    port = ":" + s.substring(t3 + 1, len);
                                } else {
                                    port = "";
                                }
                                if (t2 + 1 >= t3) {
                                    host = s.substring(t2 + 1, t3);
                                } else {
                                    host = this.u.getHost();
                                }
                                ps.print("<dt><a href=\"gopher://" + host + port + "/" + s.substring(0, 1) + encodePercent(s.substring(t1 + 1, t2)) + "\">\n");
                                ps.print("<img align=middle border=0 width=25 height=32 src=");
                                switch (key) {
                                    case 48:
                                        ps.print(System.getProperty("java.net.ftp.imagepath.text"));
                                        break;
                                    case 49:
                                        ps.print(System.getProperty("java.net.ftp.imagepath.directory"));
                                        break;
                                    case 103:
                                        ps.print(System.getProperty("java.net.ftp.imagepath.gif"));
                                        break;
                                    default:
                                        ps.print(System.getProperty("java.net.ftp.imagepath.file"));
                                        break;
                                }
                                ps.print(".gif align=middle><dd>\n");
                                ps.print(s.substring(1, t1) + "</a>\n");
                            }
                        }
                    }
                    if (len > 0) {
                        key = s.charAt(0);
                        t1 = s.indexOf(9);
                        if (t1 <= 0) {
                            t2 = -1;
                        } else {
                            t2 = s.indexOf(9, t1 + 1);
                        }
                        if (t2 <= 0) {
                            t3 = -1;
                        } else {
                            t3 = s.indexOf(9, t2 + 1);
                        }
                        if (t3 < 0) {
                            continue;
                        } else {
                            if (t3 + 1 >= len) {
                                port = "";
                            } else {
                                port = ":" + s.substring(t3 + 1, len);
                            }
                            if (t2 + 1 >= t3) {
                                host = this.u.getHost();
                            } else {
                                host = s.substring(t2 + 1, t3);
                            }
                            ps.print("<dt><a href=\"gopher://" + host + port + "/" + s.substring(0, 1) + encodePercent(s.substring(t1 + 1, t2)) + "\">\n");
                            ps.print("<img align=middle border=0 width=25 height=32 src=");
                            switch (key) {
                                case 48:
                                    ps.print(System.getProperty("java.net.ftp.imagepath.text"));
                                    break;
                                case 49:
                                    ps.print(System.getProperty("java.net.ftp.imagepath.directory"));
                                    break;
                                case 103:
                                    ps.print(System.getProperty("java.net.ftp.imagepath.gif"));
                                    break;
                                default:
                                    ps.print(System.getProperty("java.net.ftp.imagepath.file"));
                                    break;
                            }
                            ps.print(".gif align=middle><dd>\n");
                            ps.print(s.substring(1, t1) + "</a>\n");
                        }
                    }
                } else {
                    ps.print("</dl></body>\n");
                    ps.close();
                    closeServer();
                    this.os.close();
                    return;
                }
            }
        } catch (UnsupportedEncodingException e3) {
            throw new InternalError(encoding + " encoding not found");
        } catch (IOException e4) {
            try {
                closeServer();
                this.os.close();
            } catch (IOException e5) {
            }
        } catch (Throwable th) {
            try {
                closeServer();
                this.os.close();
            } catch (IOException e6) {
            }
        }
    }
}
