package jcifs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import jcifs.util.LogStream;

public class Config {
    public static String DEFAULT_OEM_ENCODING;
    private static LogStream log;
    private static Properties prp;
    public static int socketCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.Config.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.Config.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.Config.<clinit>():void");
    }

    public static void registerSmbURLHandler() {
        String ver = System.getProperty("java.version");
        if (ver.startsWith("1.1.") || ver.startsWith("1.2.")) {
            throw new RuntimeException("jcifs-0.7.0b4+ requires Java 1.3 or above. You are running " + ver);
        }
        String pkgs = System.getProperty("java.protocol.handler.pkgs");
        if (pkgs == null) {
            System.setProperty("java.protocol.handler.pkgs", "jcifs");
        } else if (pkgs.indexOf("jcifs") == -1) {
            System.setProperty("java.protocol.handler.pkgs", pkgs + "|jcifs");
        }
    }

    Config() {
    }

    public static void setProperties(Properties prp) {
        prp = new Properties(prp);
        try {
            prp.putAll(System.getProperties());
        } catch (SecurityException e) {
            LogStream logStream = log;
            if (LogStream.level > 1) {
                log.println("SecurityException: jcifs will ignore System properties");
            }
        }
    }

    public static void load(InputStream in) throws IOException {
        if (in != null) {
            prp.load(in);
        }
        try {
            prp.putAll(System.getProperties());
        } catch (SecurityException e) {
            LogStream logStream = log;
            if (LogStream.level > 1) {
                log.println("SecurityException: jcifs will ignore System properties");
            }
        }
    }

    public static void store(OutputStream out, String header) throws IOException {
        prp.store(out, header);
    }

    public static void list(PrintStream out) throws IOException {
        prp.list(out);
    }

    public static Object setProperty(String key, String value) {
        return prp.setProperty(key, value);
    }

    public static Object get(String key) {
        return prp.get(key);
    }

    public static String getProperty(String key, String def) {
        return prp.getProperty(key, def);
    }

    public static String getProperty(String key) {
        return prp.getProperty(key);
    }

    public static int getInt(String key, int def) {
        String s = prp.getProperty(key);
        if (s != null) {
            try {
                def = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    nfe.printStackTrace(log);
                }
            }
        }
        return def;
    }

    public static int getInt(String key) {
        String s = prp.getProperty(key);
        int result = -1;
        if (s != null) {
            try {
                result = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    nfe.printStackTrace(log);
                }
            }
        }
        return result;
    }

    public static long getLong(String key, long def) {
        String s = prp.getProperty(key);
        if (s != null) {
            try {
                def = Long.parseLong(s);
            } catch (NumberFormatException nfe) {
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    nfe.printStackTrace(log);
                }
            }
        }
        return def;
    }

    public static InetAddress getInetAddress(String key, InetAddress def) {
        String addr = prp.getProperty(key);
        if (addr != null) {
            try {
                def = InetAddress.getByName(addr);
            } catch (UnknownHostException uhe) {
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    log.println(addr);
                    uhe.printStackTrace(log);
                }
            }
        }
        return def;
    }

    public static InetAddress getLocalHost() {
        String addr = prp.getProperty("jcifs.smb.client.laddr");
        if (addr != null) {
            try {
                return InetAddress.getByName(addr);
            } catch (UnknownHostException uhe) {
                LogStream logStream = log;
                if (LogStream.level > 0) {
                    log.println("Ignoring jcifs.smb.client.laddr address: " + addr);
                    uhe.printStackTrace(log);
                }
            }
        }
        return null;
    }

    public static boolean getBoolean(String key, boolean def) {
        String b = getProperty(key);
        if (b != null) {
            return b.toLowerCase().equals("true");
        }
        return def;
    }

    public static InetAddress[] getInetAddressArray(String key, String delim, InetAddress[] def) {
        String p = getProperty(key);
        if (p == null) {
            return def;
        }
        StringTokenizer tok = new StringTokenizer(p, delim);
        int len = tok.countTokens();
        InetAddress[] arr = new InetAddress[len];
        int i = 0;
        while (i < len) {
            String addr = tok.nextToken();
            try {
                arr[i] = InetAddress.getByName(addr);
                i++;
            } catch (UnknownHostException uhe) {
                LogStream logStream = log;
                if (LogStream.level <= 0) {
                    return def;
                }
                log.println(addr);
                uhe.printStackTrace(log);
                return def;
            }
        }
        return arr;
    }
}
