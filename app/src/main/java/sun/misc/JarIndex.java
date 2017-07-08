package sun.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarIndex {
    public static final String INDEX_NAME = "META-INF/INDEX.LIST";
    private static final boolean metaInfFilenames = false;
    private HashMap indexMap;
    private String[] jarFiles;
    private HashMap jarMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.JarIndex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.JarIndex.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.JarIndex.<clinit>():void");
    }

    public JarIndex() {
        this.indexMap = new HashMap();
        this.jarMap = new HashMap();
    }

    public JarIndex(InputStream is) throws IOException {
        this();
        read(is);
    }

    public JarIndex(String[] files) throws IOException {
        this();
        this.jarFiles = files;
        parseJars(files);
    }

    public static JarIndex getJarIndex(JarFile jar) throws IOException {
        return getJarIndex(jar, null);
    }

    public static JarIndex getJarIndex(JarFile jar, MetaIndex metaIndex) throws IOException {
        JarIndex jarIndex = null;
        if (metaIndex != null && !metaIndex.mayContain(INDEX_NAME)) {
            return null;
        }
        JarEntry e = jar.getJarEntry(INDEX_NAME);
        if (e != null) {
            jarIndex = new JarIndex(jar.getInputStream(e));
        }
        return jarIndex;
    }

    public String[] getJarFiles() {
        return this.jarFiles;
    }

    private void addToList(String key, String value, HashMap t) {
        LinkedList list = (LinkedList) t.get(key);
        if (list == null) {
            list = new LinkedList();
            list.add(value);
            t.put(key, list);
        } else if (!list.contains(value)) {
            list.add(value);
        }
    }

    public LinkedList get(String fileName) {
        LinkedList jarFiles = (LinkedList) this.indexMap.get(fileName);
        if (jarFiles != null) {
            return jarFiles;
        }
        int pos = fileName.lastIndexOf("/");
        if (pos != -1) {
            return (LinkedList) this.indexMap.get(fileName.substring(0, pos));
        }
        return jarFiles;
    }

    public void add(String fileName, String jarName) {
        String packageName;
        int pos = fileName.lastIndexOf("/");
        if (pos != -1) {
            packageName = fileName.substring(0, pos);
        } else {
            packageName = fileName;
        }
        addToList(packageName, jarName, this.indexMap);
        addToList(jarName, packageName, this.jarMap);
    }

    private void addExplicit(String fileName, String jarName) {
        addToList(fileName, jarName, this.indexMap);
        addToList(jarName, fileName, this.jarMap);
    }

    private void parseJars(String[] files) throws IOException {
        if (files != null) {
            for (String currentJar : files) {
                ZipFile zrf = new ZipFile(currentJar.replace('/', File.separatorChar));
                Enumeration entries = zrf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String fileName = entry.getName();
                    if (!(fileName.equals("META-INF/") || fileName.equals(INDEX_NAME) || fileName.equals(JarFile.MANIFEST_NAME))) {
                        if (!metaInfFilenames) {
                            add(fileName, currentJar);
                        } else if (!fileName.startsWith("META-INF/")) {
                            add(fileName, currentJar);
                        } else if (!entry.isDirectory()) {
                            addExplicit(fileName, currentJar);
                        }
                    }
                }
                zrf.close();
            }
        }
    }

    public void write(OutputStream out) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
        bw.write("JarIndex-Version: 1.0\n\n");
        if (this.jarFiles != null) {
            for (String jar : this.jarFiles) {
                bw.write(jar + "\n");
                LinkedList jarlist = (LinkedList) this.jarMap.get(jar);
                if (jarlist != null) {
                    Iterator listitr = jarlist.iterator();
                    while (listitr.hasNext()) {
                        bw.write(((String) listitr.next()) + "\n");
                    }
                }
                bw.write("\n");
            }
            bw.flush();
        }
    }

    public void read(InputStream is) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));
        String currentJar = null;
        Vector jars = new Vector();
        do {
            line = br.readLine();
            if (line == null) {
                break;
            }
        } while (!line.endsWith(".jar"));
        while (line != null) {
            if (line.length() != 0) {
                if (line.endsWith(".jar")) {
                    currentJar = line;
                    jars.add(line);
                } else {
                    String name = line;
                    addToList(name, currentJar, this.indexMap);
                    addToList(currentJar, name, this.jarMap);
                }
            }
            line = br.readLine();
        }
        this.jarFiles = (String[]) jars.toArray(new String[jars.size()]);
    }

    public void merge(JarIndex toIndex, String path) {
        for (Entry e : this.indexMap.entrySet()) {
            String packageName = (String) e.getKey();
            Iterator listItr = ((LinkedList) e.getValue()).iterator();
            while (listItr.hasNext()) {
                String jarName = (String) listItr.next();
                if (path != null) {
                    jarName = path.concat(jarName);
                }
                toIndex.add(packageName, jarName);
            }
        }
    }
}
