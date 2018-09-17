package sun.net.www.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;
import sun.net.NetProperties;
import sun.util.logging.PlatformLogger;

public class HttpCapture {
    private static volatile ArrayList<String> capFiles;
    private static boolean initialized;
    private static volatile ArrayList<Pattern> patterns;
    private File file;
    private boolean incoming;
    private BufferedWriter out;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.http.HttpCapture.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.http.HttpCapture.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.http.HttpCapture.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized void init() {
        synchronized (HttpCapture.class) {
            initialized = true;
            String rulesFile = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return NetProperties.get("sun.net.http.captureRules");
                }
            });
            if (!(rulesFile == null || rulesFile.isEmpty())) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(rulesFile));
                    try {
                        for (String line = in.readLine(); line != null; line = in.readLine()) {
                            line = line.trim();
                            if (!line.startsWith("#")) {
                                String[] s = line.split(",");
                                if (s.length == 2) {
                                    if (patterns == null) {
                                        patterns = new ArrayList();
                                        capFiles = new ArrayList();
                                    }
                                    patterns.add(Pattern.compile(s[0].trim()));
                                    capFiles.add(s[1].trim());
                                }
                            }
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    } catch (IOException e2) {
                    } catch (Throwable th) {
                        try {
                            in.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (FileNotFoundException e4) {
                    return;
                }
            }
        }
    }

    private static synchronized boolean isInitialized() {
        boolean z;
        synchronized (HttpCapture.class) {
            z = initialized;
        }
        return z;
    }

    private HttpCapture(File f, URL url) {
        this.file = null;
        this.incoming = true;
        this.out = null;
        this.file = f;
        try {
            this.out = new BufferedWriter(new FileWriter(this.file, true));
            this.out.write("URL: " + url + "\n");
        } catch (Throwable ex) {
            PlatformLogger.getLogger(HttpCapture.class.getName()).severe(null, ex);
        }
    }

    public synchronized void sent(int c) throws IOException {
        if (this.incoming) {
            this.out.write("\n------>\n");
            this.incoming = false;
            this.out.flush();
        }
        this.out.write(c);
    }

    public synchronized void received(int c) throws IOException {
        if (!this.incoming) {
            this.out.write("\n<------\n");
            this.incoming = true;
            this.out.flush();
        }
        this.out.write(c);
    }

    public synchronized void flush() throws IOException {
        this.out.flush();
    }

    public static HttpCapture getCapture(URL url) {
        if (!isInitialized()) {
            init();
        }
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }
        String s = url.toString();
        for (int i = 0; i < patterns.size(); i++) {
            if (((Pattern) patterns.get(i)).matcher(s).find()) {
                File fi;
                String f = (String) capFiles.get(i);
                if (f.indexOf("%d") >= 0) {
                    Random rand = new Random();
                    do {
                        fi = new File(f.replace((CharSequence) "%d", Integer.toString(rand.nextInt())));
                    } while (fi.exists());
                } else {
                    fi = new File(f);
                }
                return new HttpCapture(fi, url);
            }
        }
        return null;
    }
}
