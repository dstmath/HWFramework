package com.huawei.android.feature.module;

import android.content.Context;
import android.os.Build;
import android.util.Log;
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
        this.mContext = context.getApplicationContext();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0035 A[SYNTHETIC, Splitter:B:13:0x0035] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003a A[SYNTHETIC, Splitter:B:16:0x003a] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0083 A[SYNTHETIC, Splitter:B:37:0x0083] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0088 A[SYNTHETIC, Splitter:B:40:0x0088] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a9 A[SYNTHETIC, Splitter:B:50:0x00a9] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ae A[SYNTHETIC, Splitter:B:53:0x00ae] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:34:0x0078=Splitter:B:34:0x0078, B:10:0x002a=Splitter:B:10:0x002a} */
    private int copySo(ZipFile zipFile, ad adVar) {
        InputStream inputStream;
        FileOutputStream fileOutputStream;
        try {
            byte[] bArr = new byte[4096];
            fileOutputStream = new FileOutputStream(new File(this.mModuleInfo.mNativeLibDir, adVar.C));
            try {
                inputStream = zipFile.getInputStream(adVar.D);
                while (true) {
                    try {
                        int read = inputStream.read(bArr);
                        if (read > 0) {
                            fileOutputStream.write(bArr, 0, read);
                        } else {
                            try {
                                break;
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    } catch (FileNotFoundException e2) {
                        e = e2;
                        try {
                            Log.e(TAG, e.getMessage());
                            if (fileOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            return -20;
                        } catch (Throwable th) {
                            th = th;
                            if (fileOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
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
                } catch (IOException e4) {
                    Log.e(TAG, e4.getMessage());
                    return 0;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                inputStream = null;
            } catch (IOException e6) {
                e = e6;
                inputStream = null;
                Log.e(TAG, e.getMessage());
                if (fileOutputStream != null) {
                }
                if (inputStream != null) {
                }
                return -20;
            } catch (Throwable th2) {
                th = th2;
                inputStream = null;
                if (fileOutputStream != null) {
                }
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            inputStream = null;
            fileOutputStream = null;
            Log.e(TAG, e.getMessage());
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, e8.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    return -20;
                } catch (IOException e9) {
                    Log.e(TAG, e9.getMessage());
                    return -20;
                }
            }
            return -20;
        } catch (IOException e10) {
            e = e10;
            inputStream = null;
            fileOutputStream = null;
            Log.e(TAG, e.getMessage());
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e11) {
                    Log.e(TAG, e11.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    return -20;
                } catch (IOException e12) {
                    Log.e(TAG, e12.getMessage());
                    return -20;
                }
            }
            return -20;
        } catch (Throwable th3) {
            th = th3;
            inputStream = null;
            fileOutputStream = null;
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e13) {
                    Log.e(TAG, e13.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e14) {
                    Log.e(TAG, e14.getMessage());
                }
            }
            throw th;
        }
    }

    private <T> Constructor getConstructor(Class<T> cls, List<DynamicParams> list) {
        int i = 0;
        if (list == null || list.size() == 0) {
            try {
                return cls.getConstructor(new Class[0]);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        } else {
            Class[] clsArr = new Class[list.size()];
            while (true) {
                int i2 = i;
                if (i2 < list.size()) {
                    clsArr[i2] = list.get(i2).mClassType;
                    i = i2 + 1;
                } else {
                    try {
                        return cls.getConstructor(clsArr);
                    } catch (NoSuchMethodException e2) {
                        Log.e(TAG, e2.getMessage());
                        return null;
                    }
                }
            }
        }
    }

    private Set<ad> getNativeZipEntries(HashMap<String, HashSet<ad>> hashMap) {
        Set<ad> set = null;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] strArr = Build.SUPPORTED_ABIS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String str = strArr[i];
                if (hashMap.containsKey(str)) {
                    set = hashMap.get(str);
                    break;
                }
                i++;
            }
        } else if (hashMap.containsKey(Build.CPU_ABI)) {
            set = hashMap.get(Build.CPU_ABI);
        }
        return (set != null || !hashMap.containsKey("armeabi")) ? set : hashMap.get("armeabi");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x005d A[SYNTHETIC, Splitter:B:18:0x005d] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00bf A[SYNTHETIC, Splitter:B:51:0x00bf] */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[RETURN, SYNTHETIC] */
    public int extractNatvieLibrary(File file) {
        IOException iOException;
        ZipFile zipFile;
        int i;
        try {
            zipFile = new ZipFile(file);
            try {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                HashMap hashMap = new HashMap();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    Matcher matcher = SPATTERN.matcher(zipEntry.getName());
                    if (matcher.matches()) {
                        String group = matcher.group(1);
                        String group2 = matcher.group(2);
                        HashSet hashSet = (HashSet) hashMap.get(group);
                        if (hashSet == null) {
                            hashSet = new HashSet();
                            hashMap.put(group, hashSet);
                        }
                        hashSet.add(new ad(zipEntry, group2));
                    }
                }
                Set<ad> nativeZipEntries = getNativeZipEntries(hashMap);
                if (nativeZipEntries == null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return 0;
                }
                int i2 = 0;
                for (ad copySo : nativeZipEntries) {
                    try {
                        int copySo2 = copySo(zipFile, copySo);
                        if (copySo2 != 0) {
                            try {
                                zipFile.close();
                                return copySo2;
                            } catch (IOException e2) {
                                Log.e(TAG, e2.getMessage());
                                return copySo2;
                            }
                        } else {
                            i2 = copySo2;
                        }
                    } catch (IOException e3) {
                        iOException = e3;
                        i = i2;
                        try {
                            Log.e(TAG, iOException.getMessage());
                            if (zipFile != null) {
                                return i;
                            }
                            try {
                                zipFile.close();
                                return i;
                            } catch (IOException e4) {
                                Log.e(TAG, e4.getMessage());
                                return i;
                            }
                        } catch (Throwable th) {
                            th = th;
                            if (zipFile != null) {
                            }
                            throw th;
                        }
                    }
                }
                try {
                    zipFile.close();
                    return i2;
                } catch (IOException e5) {
                    Log.e(TAG, e5.getMessage());
                    return i2;
                }
            } catch (IOException e6) {
                iOException = e6;
                i = 0;
            }
        } catch (IOException e7) {
            iOException = e7;
            zipFile = null;
            i = 0;
            Log.e(TAG, iOException.getMessage());
            if (zipFile != null) {
            }
        } catch (Throwable th2) {
            th = th2;
            zipFile = null;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e8) {
                    Log.e(TAG, e8.getMessage());
                }
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
                    return constructor.newInstance(new Object[0]);
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
                    return constructor.newInstance(arrayList.toArray());
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
