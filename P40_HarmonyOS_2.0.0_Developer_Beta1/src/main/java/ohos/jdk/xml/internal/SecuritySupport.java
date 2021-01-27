package ohos.jdk.xml.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

class SecuritySupport {
    static final Properties cacheProps = new Properties();
    static volatile boolean firstTime = true;

    private SecuritySupport() {
    }

    public static String getSystemProperty(final String str) {
        return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            /* class ohos.jdk.xml.internal.SecuritySupport.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public String run() {
                return System.getProperty(str);
            }
        });
    }

    public static <T> T getJAXPSystemProperty(Class<T> cls, String str, String str2) {
        String jAXPSystemProperty = getJAXPSystemProperty(str);
        if (jAXPSystemProperty == null) {
            jAXPSystemProperty = str2;
        }
        if (Integer.class.isAssignableFrom(cls)) {
            return cls.cast(Integer.valueOf(Integer.parseInt(jAXPSystemProperty)));
        }
        if (Boolean.class.isAssignableFrom(cls)) {
            return cls.cast(Boolean.valueOf(Boolean.parseBoolean(jAXPSystemProperty)));
        }
        return cls.cast(jAXPSystemProperty);
    }

    public static String getJAXPSystemProperty(String str) {
        String systemProperty = getSystemProperty(str);
        return systemProperty == null ? readJAXPProperty(str) : systemProperty;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005c, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006d, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0068 A[SYNTHETIC, Splitter:B:35:0x0068] */
    public static String readJAXPProperty(String str) {
        FileInputStream fileInputStream;
        Throwable th;
        String str2 = null;
        InputStream inputStream = null;
        str2 = null;
        try {
            if (firstTime) {
                synchronized (cacheProps) {
                    try {
                        if (firstTime) {
                            File file = new File(getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties");
                            if (getFileExists(file)) {
                                fileInputStream = getFileInputStream(file);
                                cacheProps.load(fileInputStream);
                            } else {
                                fileInputStream = null;
                            }
                            firstTime = false;
                        } else {
                            fileInputStream = null;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } else {
                fileInputStream = null;
            }
            str2 = cacheProps.getProperty(str);
        } catch (IOException unused) {
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException unused2) {
                }
            }
            throw th;
        }
        return str2;
    }

    static boolean getFileExists(final File file) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            /* class ohos.jdk.xml.internal.SecuritySupport.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
            public Boolean run() {
                return file.exists() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }

    static FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<FileInputStream>() {
                /* class ohos.jdk.xml.internal.SecuritySupport.AnonymousClass3 */

                @Override // java.security.PrivilegedExceptionAction
                public FileInputStream run() throws Exception {
                    return new FileInputStream(file);
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((FileNotFoundException) e.getException());
        }
    }
}
