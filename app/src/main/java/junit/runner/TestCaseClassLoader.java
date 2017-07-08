package junit.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TestCaseClassLoader extends ClassLoader {
    static final String EXCLUDED_FILE = "excluded.properties";
    private String[] defaultExclusions;
    private Vector fExcluded;
    private Vector fPathItems;

    public TestCaseClassLoader() {
        this(System.getProperty("java.class.path"));
    }

    public TestCaseClassLoader(String classPath) {
        this.defaultExclusions = new String[]{"junit.framework.", "junit.extensions.", "junit.runner."};
        scanPath(classPath);
        readExcludedPackages();
    }

    private void scanPath(String classPath) {
        String separator = System.getProperty("path.separator");
        this.fPathItems = new Vector(10);
        StringTokenizer st = new StringTokenizer(classPath, separator);
        while (st.hasMoreTokens()) {
            this.fPathItems.addElement(st.nextToken());
        }
    }

    public URL getResource(String name) {
        return ClassLoader.getSystemResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    public boolean isExcluded(String name) {
        for (int i = 0; i < this.fExcluded.size(); i++) {
            if (name.startsWith((String) this.fExcluded.elementAt(i))) {
                return true;
            }
        }
        return false;
    }

    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        if (isExcluded(name)) {
            try {
                return findSystemClass(name);
            } catch (ClassNotFoundException e) {
            }
        }
        if (c == null) {
            byte[] data = lookupClassData(name);
            if (data == null) {
                throw new ClassNotFoundException();
            }
            c = defineClass(name, data, 0, data.length);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private byte[] lookupClassData(String className) throws ClassNotFoundException {
        for (int i = 0; i < this.fPathItems.size(); i++) {
            byte[] data;
            String path = (String) this.fPathItems.elementAt(i);
            String fileName = className.replace('.', '/') + ".class";
            if (isJar(path)) {
                data = loadJarData(path, fileName);
            } else {
                data = loadFileData(path, fileName);
            }
            if (data != null) {
                return data;
            }
        }
        throw new ClassNotFoundException(className);
    }

    boolean isJar(String pathEntry) {
        if (pathEntry.endsWith(".jar") || pathEntry.endsWith(".apk")) {
            return true;
        }
        return pathEntry.endsWith(".zip");
    }

    private byte[] loadFileData(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            return getClassData(file);
        }
        return null;
    }

    private byte[] getClassData(File f) {
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            while (true) {
                int n = stream.read(b);
                if (n != -1) {
                    out.write(b, 0, n);
                } else {
                    stream.close();
                    out.close();
                    return out.toByteArray();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] loadJarData(String path, String fileName) {
        InputStream inputStream = null;
        File archive = new File(path);
        if (!archive.exists()) {
            return null;
        }
        try {
            ZipFile zipFile = new ZipFile(archive);
            ZipEntry entry = zipFile.getEntry(fileName);
            if (entry == null) {
                return null;
            }
            int size = (int) entry.getSize();
            try {
                inputStream = zipFile.getInputStream(entry);
                byte[] data = new byte[size];
                for (int pos = 0; pos < size; pos += inputStream.read(data, pos, data.length - pos)) {
                }
                zipFile.close();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
                return data;
            } catch (IOException e2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
            }
        } catch (IOException e5) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readExcludedPackages() {
        this.fExcluded = new Vector(10);
        for (Object addElement : this.defaultExclusions) {
            this.fExcluded.addElement(addElement);
        }
        InputStream is = getClass().getResourceAsStream(EXCLUDED_FILE);
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                try {
                    is.close();
                } catch (IOException e) {
                }
                Enumeration e2 = p.propertyNames();
                while (e2.hasMoreElements()) {
                    String key = (String) e2.nextElement();
                    if (key.startsWith("excluded.")) {
                        String path = p.getProperty(key).trim();
                        if (path.endsWith("*")) {
                            path = path.substring(0, path.length() - 1);
                        }
                        if (path.length() > 0) {
                            this.fExcluded.addElement(path);
                        }
                    }
                }
            } catch (IOException e3) {
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
        }
    }
}
