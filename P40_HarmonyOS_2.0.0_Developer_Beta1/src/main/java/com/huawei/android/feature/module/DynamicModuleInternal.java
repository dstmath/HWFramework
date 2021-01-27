package com.huawei.android.feature.module;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.huawei.android.feature.utils.CommonUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class DynamicModuleInternal {
    private static Pattern SPATTERN = Pattern.compile("lib/([^/]+)/(.*\\.so)$");
    private static final String TAG = DynamicModuleInternal.class.getSimpleName();
    protected ClassLoader mClassLoader;
    protected Context mContext;
    protected DynamicModuleInfo mModuleInfo;

    public DynamicModuleInternal(Context context, DynamicModuleInfo dynamicModuleInfo) {
        this.mModuleInfo = dynamicModuleInfo;
        this.mContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0035 A[SYNTHETIC, Splitter:B:13:0x0035] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003a A[SYNTHETIC, Splitter:B:16:0x003a] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0083 A[SYNTHETIC, Splitter:B:36:0x0083] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0088 A[SYNTHETIC, Splitter:B:39:0x0088] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00a9 A[SYNTHETIC, Splitter:B:49:0x00a9] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00ae A[SYNTHETIC, Splitter:B:52:0x00ae] */
    private int copySo(ZipFile zipFile, o oVar) {
        InputStream inputStream;
        FileOutputStream fileOutputStream;
        Throwable th;
        IOException e;
        try {
            byte[] bArr = new byte[4096];
            fileOutputStream = new FileOutputStream(new File(this.mModuleInfo.mNativeLibDir, oVar.p));
            try {
                inputStream = zipFile.getInputStream(oVar.q);
                while (true) {
                    try {
                        int read = inputStream.read(bArr);
                        if (read > 0) {
                            fileOutputStream.write(bArr, 0, read);
                        } else {
                            try {
                                break;
                            } catch (IOException e2) {
                                Log.e(TAG, e2.getMessage());
                            }
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        try {
                            Log.e(TAG, e.getMessage());
                            if (fileOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            return -20;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e = e4;
                        Log.e(TAG, e.getMessage());
                        if (fileOutputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        return -20;
                    }
                }
                fileOutputStream.close();
                if (inputStream == null) {
                    return 0;
                }
                try {
                    inputStream.close();
                    return 0;
                } catch (IOException e5) {
                    Log.e(TAG, e5.getMessage());
                    return 0;
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                inputStream = null;
                Log.e(TAG, e.getMessage());
                if (fileOutputStream != null) {
                }
                if (inputStream != null) {
                }
                return -20;
            } catch (IOException e7) {
                e = e7;
                inputStream = null;
                Log.e(TAG, e.getMessage());
                if (fileOutputStream != null) {
                }
                if (inputStream != null) {
                }
                return -20;
            } catch (Throwable th3) {
                th = th3;
                inputStream = null;
                if (fileOutputStream != null) {
                }
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            inputStream = null;
            fileOutputStream = null;
            Log.e(TAG, e.getMessage());
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e9) {
                    Log.e(TAG, e9.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    return -20;
                } catch (IOException e10) {
                    Log.e(TAG, e10.getMessage());
                    return -20;
                }
            }
            return -20;
        } catch (IOException e11) {
            e = e11;
            inputStream = null;
            fileOutputStream = null;
            Log.e(TAG, e.getMessage());
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e12) {
                    Log.e(TAG, e12.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    return -20;
                } catch (IOException e13) {
                    Log.e(TAG, e13.getMessage());
                    return -20;
                }
            }
            return -20;
        } catch (Throwable th4) {
            th = th4;
            inputStream = null;
            fileOutputStream = null;
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e14) {
                    Log.e(TAG, e14.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e15) {
                    Log.e(TAG, e15.getMessage());
                }
            }
            throw th;
        }
    }

    private <T> Constructor getConstructor(Class<T> cls, List<DynamicParams> list) {
        if (list == null || list.size() == 0) {
            try {
                return cls.getConstructor(new Class[0]);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        } else {
            Class<?>[] clsArr = new Class[list.size()];
            for (int i = 0; i < list.size(); i++) {
                clsArr[i] = list.get(i).mClassType;
            }
            try {
                return cls.getConstructor(clsArr);
            } catch (NoSuchMethodException e2) {
                Log.e(TAG, e2.getMessage());
                return null;
            }
        }
    }

    private Set<o> getNativeZipEntries(HashMap<String, HashSet<o>> hashMap) {
        HashSet<o> hashSet = null;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] strArr = CommonUtils.is64Bit(this.mContext) ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
            if (strArr != null) {
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String str = strArr[i];
                    if (hashMap.containsKey(str)) {
                        hashSet = hashMap.get(str);
                        break;
                    }
                    i++;
                }
            }
        } else if (hashMap.containsKey(Build.CPU_ABI)) {
            hashSet = hashMap.get(Build.CPU_ABI);
        }
        return (hashSet != null || !hashMap.containsKey("armeabi")) ? hashSet : hashMap.get("armeabi");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x007d A[SYNTHETIC, Splitter:B:26:0x007d] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00e3 A[SYNTHETIC, Splitter:B:56:0x00e3] */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    public int extractNativeLibrary(File file) {
        IOException iOException;
        ZipFile zipFile;
        int i;
        Throwable th;
        try {
            zipFile = new ZipFile(file);
            try {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                HashMap<String, HashSet<o>> hashMap = new HashMap<>();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    String name = zipEntry.getName();
                    if (name.contains("../")) {
                        Log.e(TAG, "Unsafe zip name!");
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        return -20;
                    }
                    Matcher matcher = SPATTERN.matcher(name);
                    if (matcher.matches()) {
                        String group = matcher.group(1);
                        String group2 = matcher.group(2);
                        HashSet<o> hashSet = hashMap.get(group);
                        if (hashSet == null) {
                            hashSet = new HashSet<>();
                            hashMap.put(group, hashSet);
                        }
                        hashSet.add(new o(zipEntry, group2));
                    }
                }
                Set<o> nativeZipEntries = getNativeZipEntries(hashMap);
                if (nativeZipEntries == null) {
                    try {
                        zipFile.close();
                    } catch (IOException e2) {
                        Log.e(TAG, e2.getMessage());
                    }
                    return 0;
                }
                int i2 = 0;
                for (o oVar : nativeZipEntries) {
                    try {
                        int copySo = copySo(zipFile, oVar);
                        if (copySo != 0) {
                            try {
                                zipFile.close();
                                return copySo;
                            } catch (IOException e3) {
                                Log.e(TAG, e3.getMessage());
                                return copySo;
                            }
                        } else {
                            i2 = copySo;
                        }
                    } catch (IOException e4) {
                        iOException = e4;
                        i = i2;
                        try {
                            Log.e(TAG, iOException.getMessage());
                            if (zipFile == null) {
                                return i;
                            }
                            try {
                                zipFile.close();
                                return i;
                            } catch (IOException e5) {
                                Log.e(TAG, e5.getMessage());
                                return i;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e6) {
                                    Log.e(TAG, e6.getMessage());
                                }
                            }
                            throw th;
                        }
                    }
                }
                try {
                    zipFile.close();
                    return i2;
                } catch (IOException e7) {
                    Log.e(TAG, e7.getMessage());
                    return i2;
                }
            } catch (IOException e8) {
                iOException = e8;
                i = 0;
                Log.e(TAG, iOException.getMessage());
                if (zipFile == null) {
                }
            }
        } catch (IOException e9) {
            iOException = e9;
            zipFile = null;
            i = 0;
            Log.e(TAG, iOException.getMessage());
            if (zipFile == null) {
            }
        } catch (Throwable th3) {
            th = th3;
            zipFile = null;
            if (zipFile != null) {
            }
            throw th;
        }
    }

    public <T> T getClassInstance(String str, List<DynamicParams> list) {
        if (this.mClassLoader == null) {
            return null;
        }
        try {
            Constructor constructor = getConstructor(this.mClassLoader.loadClass(str), list);
            if (constructor == null) {
                Log.e(TAG, "can't find the constructor");
                return null;
            } else if (list == null || list.size() == 0) {
                try {
                    return (T) constructor.newInstance(new Object[0]);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            } else {
                ArrayList arrayList = new ArrayList();
                for (DynamicParams dynamicParams : list) {
                    arrayList.add(dynamicParams.mParam);
                }
                try {
                    return (T) constructor.newInstance(arrayList.toArray());
                } catch (Exception e2) {
                    Log.e(TAG, e2.getMessage());
                    return null;
                }
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, e3.getMessage());
            return null;
        }
    }

    public DynamicModuleInfo getModuleInfo() {
        return this.mModuleInfo;
    }

    public abstract int install();

    public abstract int install(boolean z);

    public boolean isDynamicModuleLoaded() {
        return this.mClassLoader != null;
    }
}
