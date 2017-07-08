package sun.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MetaIndex {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static volatile Map<File, MetaIndex> jarMap;
    private String[] contents;
    private boolean isClassOnlyJar;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.MetaIndex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.MetaIndex.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.MetaIndex.<clinit>():void");
    }

    public static MetaIndex forJar(File jar) {
        return (MetaIndex) getJarMap().get(jar);
    }

    public static synchronized void registerDirectory(File dir) {
        synchronized (MetaIndex.class) {
            File indexFile = new File(dir, "meta-index");
            if (indexFile.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(indexFile));
                    String curJarName = null;
                    boolean isCurJarContainClassOnly = false;
                    List<String> contents = new ArrayList();
                    Map<File, MetaIndex> map = getJarMap();
                    dir = dir.getCanonicalFile();
                    String line = reader.readLine();
                    if (line == null || !line.equals("% VERSION 2")) {
                        reader.close();
                        return;
                    }
                    while (true) {
                        line = reader.readLine();
                        if (line != null) {
                            switch (line.charAt(0)) {
                                case '!':
                                case '#':
                                case Pattern.UNICODE_CASE /*64*/:
                                    if (curJarName != null) {
                                        if (contents.size() > 0) {
                                            map.put(new File(dir, curJarName), new MetaIndex(contents, isCurJarContainClassOnly));
                                            contents.clear();
                                        }
                                    }
                                    curJarName = line.substring(2);
                                    if (line.charAt(0) != '!') {
                                        if (!isCurJarContainClassOnly) {
                                            break;
                                        }
                                        isCurJarContainClassOnly = false;
                                        break;
                                    }
                                    isCurJarContainClassOnly = true;
                                    break;
                                case '%':
                                    break;
                                default:
                                    contents.add(line);
                                    break;
                            }
                        }
                        if (curJarName != null && contents.size() > 0) {
                            map.put(new File(dir, curJarName), new MetaIndex(contents, isCurJarContainClassOnly));
                        }
                        reader.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean mayContain(String entry) {
        if (this.isClassOnlyJar && !entry.endsWith(".class")) {
            return false;
        }
        String[] conts = this.contents;
        for (String startsWith : conts) {
            if (entry.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private MetaIndex(List<String> entries, boolean isClassOnlyJar) throws IllegalArgumentException {
        if (entries == null) {
            throw new IllegalArgumentException();
        }
        this.contents = (String[]) entries.toArray(new String[0]);
        this.isClassOnlyJar = isClassOnlyJar;
    }

    private static Map<File, MetaIndex> getJarMap() {
        if (jarMap == null) {
            synchronized (MetaIndex.class) {
                if (jarMap == null) {
                    jarMap = new HashMap();
                }
            }
        }
        if (!-assertionsDisabled) {
            Object obj;
            if (jarMap != null) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return jarMap;
    }
}
