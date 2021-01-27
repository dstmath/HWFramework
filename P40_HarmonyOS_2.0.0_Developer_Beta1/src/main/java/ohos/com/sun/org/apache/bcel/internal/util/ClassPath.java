package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassPath implements Serializable {
    public static final ClassPath SYSTEM_CLASS_PATH = new ClassPath();
    private String class_path;
    private PathEntry[] paths;

    public interface ClassFile {
        String getBase();

        InputStream getInputStream() throws IOException;

        String getPath();

        long getSize();

        long getTime();
    }

    public ClassPath(String str) {
        this.class_path = str;
        ArrayList arrayList = new ArrayList();
        StringTokenizer stringTokenizer = new StringTokenizer(str, SecuritySupport.getSystemProperty("path.separator"));
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            if (!nextToken.equals("")) {
                File file = new File(nextToken);
                try {
                    if (SecuritySupport.getFileExists(file)) {
                        if (file.isDirectory()) {
                            arrayList.add(new Dir(nextToken));
                        } else {
                            arrayList.add(new Zip(new ZipFile(file)));
                        }
                    }
                } catch (IOException e) {
                    PrintStream printStream = System.err;
                    printStream.println("CLASSPATH component " + file + ": " + e);
                }
            }
        }
        this.paths = new PathEntry[arrayList.size()];
        arrayList.toArray(this.paths);
    }

    public ClassPath() {
        this("");
    }

    @Override // java.lang.Object
    public String toString() {
        return this.class_path;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.class_path.hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof ClassPath) {
            return this.class_path.equals(((ClassPath) obj).class_path);
        }
        return false;
    }

    private static final void getPathComponents(String str, ArrayList arrayList) {
        if (str != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(str, File.pathSeparator);
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                if (SecuritySupport.getFileExists(new File(nextToken))) {
                    arrayList.add(nextToken);
                }
            }
        }
    }

    public static final String getClassPath() {
        try {
            String systemProperty = SecuritySupport.getSystemProperty("java.class.path");
            String systemProperty2 = SecuritySupport.getSystemProperty("sun.boot.class.path");
            String systemProperty3 = SecuritySupport.getSystemProperty("java.ext.dirs");
            ArrayList arrayList = new ArrayList();
            getPathComponents(systemProperty, arrayList);
            getPathComponents(systemProperty2, arrayList);
            ArrayList arrayList2 = new ArrayList();
            getPathComponents(systemProperty3, arrayList2);
            Iterator it = arrayList2.iterator();
            while (it.hasNext()) {
                String[] fileList = SecuritySupport.getFileList(new File((String) it.next()), new FilenameFilter() {
                    /* class ohos.com.sun.org.apache.bcel.internal.util.ClassPath.AnonymousClass1 */

                    @Override // java.io.FilenameFilter
                    public boolean accept(File file, String str) {
                        String lowerCase = str.toLowerCase();
                        return lowerCase.endsWith(".zip") || lowerCase.endsWith(".jar");
                    }
                });
                if (fileList != null) {
                    for (int i = 0; i < fileList.length; i++) {
                        arrayList.add(systemProperty3 + File.separatorChar + fileList[i]);
                    }
                }
            }
            StringBuffer stringBuffer = new StringBuffer();
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                stringBuffer.append((String) it2.next());
                if (it2.hasNext()) {
                    stringBuffer.append(File.pathSeparatorChar);
                }
            }
            return stringBuffer.toString().intern();
        } catch (SecurityException unused) {
            return "";
        }
    }

    public InputStream getInputStream(String str) throws IOException {
        return getInputStream(str, ".class");
    }

    public InputStream getInputStream(String str, String str2) throws IOException {
        InputStream inputStream;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream(str + str2);
        } catch (Exception unused) {
            inputStream = null;
        }
        if (inputStream != null) {
            return inputStream;
        }
        return getClassFile(str, str2).getInputStream();
    }

    public ClassFile getClassFile(String str, String str2) throws IOException {
        int i = 0;
        while (true) {
            PathEntry[] pathEntryArr = this.paths;
            if (i < pathEntryArr.length) {
                ClassFile classFile = pathEntryArr[i].getClassFile(str, str2);
                if (classFile != null) {
                    return classFile;
                }
                i++;
            } else {
                throw new IOException("Couldn't find: " + str + str2);
            }
        }
    }

    public ClassFile getClassFile(String str) throws IOException {
        return getClassFile(str, ".class");
    }

    public byte[] getBytes(String str, String str2) throws IOException {
        InputStream inputStream = getInputStream(str, str2);
        if (inputStream != null) {
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            byte[] bArr = new byte[inputStream.available()];
            dataInputStream.readFully(bArr);
            dataInputStream.close();
            inputStream.close();
            return bArr;
        }
        throw new IOException("Couldn't find: " + str + str2);
    }

    public byte[] getBytes(String str) throws IOException {
        return getBytes(str, ".class");
    }

    public String getPath(String str) throws IOException {
        String str2;
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf > 0) {
            str2 = str.substring(lastIndexOf);
            str = str.substring(0, lastIndexOf);
        } else {
            str2 = "";
        }
        return getPath(str, str2);
    }

    public String getPath(String str, String str2) throws IOException {
        return getClassFile(str, str2).getPath();
    }

    /* access modifiers changed from: private */
    public static abstract class PathEntry implements Serializable {
        /* access modifiers changed from: package-private */
        public abstract ClassFile getClassFile(String str, String str2) throws IOException;

        private PathEntry() {
        }
    }

    private static class Dir extends PathEntry {
        private String dir;

        Dir(String str) {
            super();
            this.dir = str;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.PathEntry
        public ClassFile getClassFile(String str, String str2) throws IOException {
            final File file = new File(this.dir + File.separatorChar + str.replace('.', File.separatorChar) + str2);
            if (SecuritySupport.getFileExists(file)) {
                return new ClassFile() {
                    /* class ohos.com.sun.org.apache.bcel.internal.util.ClassPath.Dir.AnonymousClass1 */

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(file);
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public String getPath() {
                        try {
                            return file.getCanonicalPath();
                        } catch (IOException unused) {
                            return null;
                        }
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public long getTime() {
                        return file.lastModified();
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public long getSize() {
                        return file.length();
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public String getBase() {
                        return Dir.this.dir;
                    }
                };
            }
            return null;
        }

        @Override // java.lang.Object
        public String toString() {
            return this.dir;
        }
    }

    private static class Zip extends PathEntry {
        private ZipFile zip;

        Zip(ZipFile zipFile) {
            super();
            this.zip = zipFile;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.PathEntry
        public ClassFile getClassFile(String str, String str2) throws IOException {
            ZipFile zipFile = this.zip;
            final ZipEntry entry = zipFile.getEntry(str.replace('.', '/') + str2);
            if (entry != null) {
                return new ClassFile() {
                    /* class ohos.com.sun.org.apache.bcel.internal.util.ClassPath.Zip.AnonymousClass1 */

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public InputStream getInputStream() throws IOException {
                        return Zip.this.zip.getInputStream(entry);
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public String getPath() {
                        return entry.toString();
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public long getTime() {
                        return entry.getTime();
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public long getSize() {
                        return entry.getSize();
                    }

                    @Override // ohos.com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile
                    public String getBase() {
                        return Zip.this.zip.getName();
                    }
                };
            }
            return null;
        }
    }
}
