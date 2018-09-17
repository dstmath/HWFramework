package sun.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaIndex {
    static final /* synthetic */ boolean -assertionsDisabled = (MetaIndex.class.desiredAssertionStatus() ^ 1);
    private static volatile Map<File, MetaIndex> jarMap;
    private String[] contents;
    private boolean isClassOnlyJar;

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
                    if (line == null || (line.equals("% VERSION 2") ^ 1) != 0) {
                        reader.close();
                        return;
                    }
                    while (true) {
                        line = reader.readLine();
                        if (line != null) {
                            switch (line.charAt(0)) {
                                case '!':
                                case '#':
                                case '@':
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
                                    contents.-java_util_stream_Collectors-mthref-2(line);
                                    break;
                            }
                        }
                        if (curJarName != null) {
                            if (contents.size() > 0) {
                                map.put(new File(dir, curJarName), new MetaIndex(contents, isCurJarContainClassOnly));
                            }
                        }
                        reader.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean mayContain(String entry) {
        if (this.isClassOnlyJar && (entry.endsWith(".class") ^ 1) != 0) {
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
        if (-assertionsDisabled || jarMap != null) {
            return jarMap;
        }
        throw new AssertionError();
    }
}
