package jcifs.netbios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import jcifs.smb.SmbFileInputStream;
import jcifs.util.LogStream;

public class Lmhosts {
    private static final String FILENAME = null;
    private static final Hashtable TAB = null;
    private static int alt;
    private static long lastModified;
    private static LogStream log;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.netbios.Lmhosts.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.netbios.Lmhosts.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.netbios.Lmhosts.<clinit>():void");
    }

    public static synchronized NbtAddress getByName(String host) {
        NbtAddress byName;
        synchronized (Lmhosts.class) {
            byName = getByName(new Name(host, 32, null));
        }
        return byName;
    }

    static synchronized NbtAddress getByName(Name name) {
        NbtAddress result;
        LogStream logStream;
        synchronized (Lmhosts.class) {
            result = null;
            try {
                if (FILENAME != null) {
                    File f = new File(FILENAME);
                    long lm = f.lastModified();
                    if (lm > lastModified) {
                        lastModified = lm;
                        TAB.clear();
                        alt = 0;
                        populate(new FileReader(f));
                    }
                    result = (NbtAddress) TAB.get(name);
                }
            } catch (FileNotFoundException fnfe) {
                logStream = log;
                if (LogStream.level > 1) {
                    log.println("lmhosts file: " + FILENAME);
                    fnfe.printStackTrace(log);
                }
            } catch (IOException ioe) {
                logStream = log;
                if (LogStream.level > 0) {
                    ioe.printStackTrace(log);
                }
            }
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void populate(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        while (true) {
            String line = br.readLine();
            if (line != null) {
                line = line.toUpperCase().trim();
                if (line.length() != 0) {
                    if (line.charAt(0) == '#') {
                        if (!line.startsWith("#INCLUDE ")) {
                            if (!line.startsWith("#BEGIN_ALTERNATE")) {
                                if (line.startsWith("#END_ALTERNATE") && alt > 0) {
                                    break;
                                }
                            }
                            alt++;
                        } else {
                            String url = "smb:" + line.substring(line.indexOf(92)).replace('\\', '/');
                            if (alt > 0) {
                                try {
                                    populate(new InputStreamReader(new SmbFileInputStream(url)));
                                    alt--;
                                    do {
                                        line = br.readLine();
                                        if (line == null) {
                                            break;
                                        }
                                    } while (!line.toUpperCase().trim().startsWith("#END_ALTERNATE"));
                                } catch (IOException ioe) {
                                    log.println("lmhosts URL: " + url);
                                    ioe.printStackTrace(log);
                                }
                            } else {
                                populate(new InputStreamReader(new SmbFileInputStream(url)));
                            }
                        }
                    } else if (Character.isDigit(line.charAt(0))) {
                        char[] data = line.toCharArray();
                        char c = '.';
                        int i = 0;
                        int ip = 0;
                        while (i < data.length && c == '.') {
                            int b = 0;
                            while (i < data.length) {
                                c = data[i];
                                if (c >= '0' && c <= '9') {
                                    b = ((b * 10) + c) - 48;
                                    i++;
                                }
                            }
                            ip = (ip << 8) + b;
                            i++;
                        }
                        while (i < data.length && Character.isWhitespace(data[i])) {
                            i++;
                        }
                        int j = i;
                        while (j < data.length && !Character.isWhitespace(data[j])) {
                            j++;
                        }
                        Name name = new Name(line.substring(i, j), 32, null);
                        TAB.put(name, new NbtAddress(name, ip, false, 0, false, false, true, true, NbtAddress.UNKNOWN_MAC_ADDRESS));
                    }
                }
            } else {
                return;
            }
        }
        alt--;
        throw new IOException("no lmhosts alternate includes loaded");
    }
}
