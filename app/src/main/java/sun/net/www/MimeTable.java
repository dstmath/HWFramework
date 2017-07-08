package sun.net.www;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

public class MimeTable implements FileNameMap {
    private static final String fileMagic = "#sun.net.www MIME content-types table";
    private static final String filePreamble = "sun.net.www MIME content-types table";
    protected static String[] mailcapLocations;
    private static String tempFileTemplate;
    private Hashtable<String, MimeEntry> entries;
    private Hashtable<String, MimeEntry> extensionMap;

    private static class DefaultInstanceHolder {
        static final MimeTable defaultInstance = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.MimeTable.DefaultInstanceHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.MimeTable.DefaultInstanceHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.net.www.MimeTable.DefaultInstanceHolder.<clinit>():void");
        }

        private DefaultInstanceHolder() {
        }

        static MimeTable getDefaultInstance() {
            return (MimeTable) AccessController.doPrivileged(new PrivilegedAction<MimeTable>() {
                public MimeTable run() {
                    MimeTable instance = new MimeTable();
                    URLConnection.setFileNameMap(instance);
                    return instance;
                }
            });
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.MimeTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.MimeTable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.MimeTable.<clinit>():void");
    }

    MimeTable() {
        this.entries = new Hashtable();
        this.extensionMap = new Hashtable();
        load();
    }

    public static MimeTable getDefaultTable() {
        return DefaultInstanceHolder.defaultInstance;
    }

    public static FileNameMap loadTable() {
        return getDefaultTable();
    }

    public synchronized int getSize() {
        return this.entries.size();
    }

    public synchronized String getContentTypeFor(String fileName) {
        MimeEntry entry = findByFileName(fileName);
        if (entry == null) {
            return null;
        }
        return entry.getType();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void add(MimeEntry m) {
        this.entries.put(m.getType(), m);
        String[] exts = m.getExtensions();
        if (exts != null) {
            int i = 0;
            while (true) {
                if (i < exts.length) {
                    this.extensionMap.put(exts[i], m);
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public synchronized MimeEntry remove(String type) {
        return remove((MimeEntry) this.entries.get(type));
    }

    public synchronized MimeEntry remove(MimeEntry entry) {
        String[] extensionKeys = entry.getExtensions();
        if (extensionKeys != null) {
            for (Object remove : extensionKeys) {
                this.extensionMap.remove(remove);
            }
        }
        return (MimeEntry) this.entries.remove(entry.getType());
    }

    public synchronized MimeEntry find(String type) {
        MimeEntry entry = (MimeEntry) this.entries.get(type);
        if (entry == null) {
            Enumeration<MimeEntry> e = this.entries.elements();
            while (e.hasMoreElements()) {
                MimeEntry wild = (MimeEntry) e.nextElement();
                if (wild.matches(type)) {
                    return wild;
                }
            }
        }
        return entry;
    }

    public MimeEntry findByFileName(String fname) {
        String ext = "";
        int i = fname.lastIndexOf(35);
        if (i > 0) {
            fname = fname.substring(0, i - 1);
        }
        i = Math.max(Math.max(fname.lastIndexOf(46), fname.lastIndexOf(47)), fname.lastIndexOf(63));
        if (i != -1 && fname.charAt(i) == '.') {
            ext = fname.substring(i).toLowerCase();
        }
        return findByExt(ext);
    }

    public synchronized MimeEntry findByExt(String fileExtension) {
        return (MimeEntry) this.extensionMap.get(fileExtension);
    }

    public synchronized MimeEntry findByDescription(String description) {
        Enumeration<MimeEntry> e = elements();
        while (e.hasMoreElements()) {
            MimeEntry entry = (MimeEntry) e.nextElement();
            if (description.equals(entry.getDescription())) {
                return entry;
            }
        }
        return find(description);
    }

    String getTempFileTemplate() {
        return tempFileTemplate;
    }

    public synchronized Enumeration<MimeEntry> elements() {
        return this.entries.elements();
    }

    public synchronized void load() {
        Properties entries = new Properties();
        File file = null;
        try {
            String userTablePath = System.getProperty("content.types.user.table");
            if (userTablePath != null) {
                File file2 = new File(userTablePath);
                try {
                    if (file2.exists()) {
                        file = file2;
                    } else {
                        file = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "content-types.properties");
                    }
                } catch (IOException e) {
                    file = file2;
                    System.err.println("Warning: default mime table not found: " + file.getPath());
                }
            }
            file = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "content-types.properties");
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            entries.load(is);
            is.close();
            parse(entries);
        } catch (IOException e2) {
            System.err.println("Warning: default mime table not found: " + file.getPath());
        }
    }

    void parse(Properties entries) {
        String tempFileTemplate = (String) entries.get("temp.file.template");
        if (tempFileTemplate != null) {
            entries.remove("temp.file.template");
            tempFileTemplate = tempFileTemplate;
        }
        Enumeration<?> types = entries.propertyNames();
        while (types.hasMoreElements()) {
            String type = (String) types.nextElement();
            parse(type, entries.getProperty(type));
        }
    }

    void parse(String type, String attrs) {
        MimeEntry newEntry = new MimeEntry(type);
        StringTokenizer tokenizer = new StringTokenizer(attrs, ";");
        while (tokenizer.hasMoreTokens()) {
            parse(tokenizer.nextToken(), newEntry);
        }
        add(newEntry);
    }

    void parse(String pair, MimeEntry entry) {
        String name = null;
        String value = null;
        boolean gotName = false;
        StringTokenizer tokenizer = new StringTokenizer(pair, "=");
        while (tokenizer.hasMoreTokens()) {
            if (gotName) {
                value = tokenizer.nextToken().trim();
            } else {
                name = tokenizer.nextToken().trim();
                gotName = true;
            }
        }
        fill(entry, name, value);
    }

    void fill(MimeEntry entry, String name, String value) {
        if ("description".equalsIgnoreCase(name)) {
            entry.setDescription(value);
        } else if ("action".equalsIgnoreCase(name)) {
            entry.setAction(getActionCode(value));
        } else if ("application".equalsIgnoreCase(name)) {
            entry.setCommand(value);
        } else if ("icon".equalsIgnoreCase(name)) {
            entry.setImageFileName(value);
        } else if ("file_extensions".equalsIgnoreCase(name)) {
            entry.setExtensions(value);
        }
    }

    String[] getExtensions(String list) {
        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        int n = tokenizer.countTokens();
        String[] extensions = new String[n];
        for (int i = 0; i < n; i++) {
            extensions[i] = tokenizer.nextToken();
        }
        return extensions;
    }

    int getActionCode(String action) {
        for (int i = 0; i < MimeEntry.actionKeywords.length; i++) {
            if (action.equalsIgnoreCase(MimeEntry.actionKeywords[i])) {
                return i;
            }
        }
        return 0;
    }

    public synchronized boolean save(String filename) {
        if (filename == null) {
            filename = System.getProperty("user.home" + File.separator + "lib" + File.separator + "content-types.properties");
        }
        return saveAsProperties(new File(filename));
    }

    public Properties getAsProperties() {
        Properties properties = new Properties();
        Enumeration<MimeEntry> e = elements();
        while (e.hasMoreElements()) {
            MimeEntry entry = (MimeEntry) e.nextElement();
            properties.put(entry.getType(), entry.toProperty());
        }
        return properties;
    }

    protected boolean saveAsProperties(File file) {
        IOException e;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream os = new FileOutputStream(file);
            try {
                Properties properties = getAsProperties();
                properties.put("temp.file.template", tempFileTemplate);
                String user = System.getProperty("user.name");
                if (user != null) {
                    properties.save(os, filePreamble + ("; customized for " + user));
                } else {
                    properties.save(os, filePreamble);
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e2) {
                    }
                }
                return true;
            } catch (IOException e3) {
                e = e3;
                fileOutputStream = os;
                try {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = os;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            e.printStackTrace();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        }
    }
}
