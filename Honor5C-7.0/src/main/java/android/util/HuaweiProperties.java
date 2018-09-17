package android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HuaweiProperties {
    private static String TAG;
    public static String VENDOR_HUAWEI_CONFIG_PATH;
    private static ArrayMap<String, String> mVendorPropertiesArrayMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.HuaweiProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.HuaweiProperties.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.HuaweiProperties.<clinit>():void");
    }

    private static void loadAllProperties(String filePath) {
        IOException e;
        Throwable th;
        Iterable propertiesString = null;
        if (new File(filePath).exists()) {
            Properties prop = new Properties();
            FileInputStream fileInputStream = null;
            try {
                FileInputStream str = new FileInputStream(filePath);
                try {
                    prop.loadFromXML(str);
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e2) {
                            Slog.w(TAG, "get Hw property execption : " + e2);
                        }
                    }
                    fileInputStream = str;
                } catch (IOException e3) {
                    e2 = e3;
                    fileInputStream = str;
                    try {
                        Slog.w(TAG, "close file execption : " + e2);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                Slog.w(TAG, "get Hw property execption : " + e22);
                            }
                        }
                        propertiesString = prop.stringPropertyNames();
                        if (r6 != null) {
                            for (String key : r6) {
                                mVendorPropertiesArrayMap.put(key, prop.getProperty(key));
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222) {
                                Slog.w(TAG, "get Hw property execption : " + e222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = str;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222 = e4;
                Slog.w(TAG, "close file execption : " + e222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                propertiesString = prop.stringPropertyNames();
                if (r6 != null) {
                    for (String key2 : r6) {
                        mVendorPropertiesArrayMap.put(key2, prop.getProperty(key2));
                    }
                }
            }
            try {
                propertiesString = prop.stringPropertyNames();
            } catch (NumberFormatException e5) {
                Slog.w(TAG, "get Hw property execption : " + e5);
            }
            if (r6 != null) {
                for (String key22 : r6) {
                    mVendorPropertiesArrayMap.put(key22, prop.getProperty(key22));
                }
            }
        }
    }

    public static String getProperties(String filePath, String searchProperty) {
        if (new File(filePath).exists()) {
            return (String) mVendorPropertiesArrayMap.get(searchProperty);
        }
        return null;
    }
}
