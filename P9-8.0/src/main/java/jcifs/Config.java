package jcifs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import jcifs.util.LogStream;

public class Config {
    public static String DEFAULT_OEM_ENCODING;
    private static LogStream log = LogStream.getInstance();
    private static Properties prp = new Properties();
    public static int socketCount = 0;

    static {
        LogStream logStream;
        DEFAULT_OEM_ENCODING = "Cp850";
        FileInputStream in = null;
        try {
            String filename = System.getProperty("jcifs.properties");
            if (filename != null && filename.length() > 1) {
                in = new FileInputStream(filename);
            }
            load(in);
            if (in != null) {
                in.close();
            }
        } catch (IOException ioe) {
            logStream = log;
            if (LogStream.level > 0) {
                ioe.printStackTrace(log);
            }
        }
        int level = getInt("jcifs.util.loglevel", -1);
        if (level != -1) {
            LogStream.setLevel(level);
        }
        try {
            "".getBytes(DEFAULT_OEM_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logStream = log;
            if (LogStream.level >= 2) {
                log.println("WARNING: The default OEM encoding " + DEFAULT_OEM_ENCODING + " does not appear to be supported by this JRE. The default encoding will be US-ASCII.");
            }
            DEFAULT_OEM_ENCODING = "US-ASCII";
        }
        logStream = log;
        if (LogStream.level >= 4) {
            try {
                prp.store(log, "JCIFS PROPERTIES");
            } catch (IOException e2) {
            }
        }
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
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            LogStream logStream = log;
            if (LogStream.level <= 0) {
                return def;
            }
            nfe.printStackTrace(log);
            return def;
        }
    }

    public static int getInt(String key) {
        String s = prp.getProperty(key);
        int result = -1;
        if (s == null) {
            return result;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            LogStream logStream = log;
            if (LogStream.level <= 0) {
                return result;
            }
            nfe.printStackTrace(log);
            return result;
        }
    }

    public static long getLong(String key, long def) {
        String s = prp.getProperty(key);
        if (s == null) {
            return def;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException nfe) {
            LogStream logStream = log;
            if (LogStream.level <= 0) {
                return def;
            }
            nfe.printStackTrace(log);
            return def;
        }
    }

    public static InetAddress getInetAddress(String key, InetAddress def) {
        String addr = prp.getProperty(key);
        if (addr == null) {
            return def;
        }
        try {
            return InetAddress.getByName(addr);
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
