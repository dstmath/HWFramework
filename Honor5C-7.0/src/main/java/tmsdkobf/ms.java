package tmsdkobf;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;
import tmsdk.common.utils.h.a;

/* compiled from: Unknown */
public final class ms {
    private static final String[][] Bk = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ms.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ms.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ms.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(Context context, Object obj, String str, String str2) {
        int i = 0;
        FileOutputStream fileOutputStream = null;
        int i2 = -2;
        if (obj == null || str == null || str2 == null) {
            return -57;
        }
        try {
            fileOutputStream = context.openFileOutput(str2, 0);
            fi fiVar = new fi();
            fiVar.Z("UTF-8");
            fiVar.put(str, obj);
            byte[] encrypt = TccCryptor.encrypt(fiVar.m(), null);
            if (encrypt == null) {
                i = -2;
            } else {
                fileOutputStream.write(encrypt);
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e2) {
            i2 = -1;
            e2.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (IOException e32) {
            i2 = ErrorCode.ERR_FILE_OP;
            e32.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized String a(Context context, String str, String str2) {
        boolean z = true;
        FileOutputStream fileOutputStream = null;
        synchronized (ms.class) {
            File file;
            String str3;
            File file2;
            boolean exists;
            boolean isUpdatableAssetFile;
            String str4;
            File file3;
            File file4;
            FileOutputStream fileOutputStream2;
            InputStream inputStream;
            InputStream open;
            byte[] bArr;
            int read;
            if (str2 != null) {
                if (!str2.equals("")) {
                    file = new File(str2);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }
                    str3 = str2 + File.separator + str;
                    file2 = new File(str3);
                    exists = file2.exists();
                    isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                    if (!str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                        if (!str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                            z = false;
                            if (!exists && isUpdatableAssetFile) {
                                str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                                if (str4 != null) {
                                    str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                                    file3 = new File(str4);
                                    file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                                    if (file3.exists()) {
                                        file3.delete();
                                    }
                                    if (file4.exists()) {
                                        file4.delete();
                                    }
                                }
                            }
                            if (exists) {
                                if (!str.equals("MToken.zip")) {
                                    if (str.equals(UpdateConfig.VIRUS_BASE_NAME) || str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                                    }
                                    if (!str.equals(UpdateConfig.LOCATION_NAME) || !o(context)) {
                                        if (!isUpdatableAssetFile || str.equals(UpdateConfig.VIRUS_BASE_NAME) || str.equals(UpdateConfig.VIRUS_BASE_EN_NAME) || str.equals(UpdateConfig.LOCATION_NAME) || !d(context, str)) {
                                            if (isUpdatableAssetFile) {
                                                fileOutputStream2 = null;
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e) {
                                                    }
                                                }
                                                if (fileOutputStream2 != null) {
                                                    try {
                                                        fileOutputStream2.close();
                                                    } catch (IOException e2) {
                                                    }
                                                }
                                                return str3;
                                            }
                                        }
                                    }
                                }
                            }
                            a(file2);
                            open = context.getResources().getAssets().open(str, 1);
                            try {
                                fileOutputStream2 = new FileOutputStream(file2);
                                try {
                                    bArr = new byte[8192];
                                    while (true) {
                                        read = open.read(bArr);
                                        if (read <= 0) {
                                            break;
                                        }
                                        fileOutputStream2.write(bArr, 0, read);
                                    }
                                    fileOutputStream2.getChannel().force(true);
                                    fileOutputStream2.flush();
                                    inputStream = open;
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    if (fileOutputStream2 != null) {
                                        fileOutputStream2.close();
                                    }
                                    return str3;
                                } catch (IOException e3) {
                                    fileOutputStream = fileOutputStream2;
                                } catch (Throwable th) {
                                    Throwable th2 = th;
                                    fileOutputStream = fileOutputStream2;
                                    r0 = th2;
                                }
                            } catch (IOException e4) {
                                try {
                                    d.c("getCommonFilePath", "getCommonFilePath error");
                                    str4 = "";
                                    if (open != null) {
                                        try {
                                            open.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e6) {
                                        }
                                    }
                                    return str4;
                                } catch (Throwable th3) {
                                    Throwable th4;
                                    th4 = th3;
                                    if (open != null) {
                                        try {
                                            open.close();
                                        } catch (IOException e7) {
                                        }
                                    }
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    throw th4;
                                }
                            }
                        }
                    }
                }
            }
            str2 = context.getFilesDir().toString();
            file = new File(str2);
            if (!file.exists()) {
                str3 = str2 + File.separator + str;
                file2 = new File(str3);
                exists = file2.exists();
                isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                    if (str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                        z = false;
                        str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                        if (str4 != null) {
                            str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                            file3 = new File(str4);
                            file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                            if (file3.exists()) {
                                file3.delete();
                            }
                            if (file4.exists()) {
                                file4.delete();
                            }
                        }
                        if (exists) {
                            if (str.equals("MToken.zip")) {
                                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                                    if (!str.equals(UpdateConfig.LOCATION_NAME)) {
                                    }
                                    if (!isUpdatableAssetFile) {
                                    }
                                    if (isUpdatableAssetFile) {
                                        fileOutputStream2 = null;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        if (fileOutputStream2 != null) {
                                            fileOutputStream2.close();
                                        }
                                        return str3;
                                    }
                                }
                            }
                        }
                        a(file2);
                        open = context.getResources().getAssets().open(str, 1);
                        fileOutputStream2 = new FileOutputStream(file2);
                        bArr = new byte[8192];
                        while (true) {
                            read = open.read(bArr);
                            if (read <= 0) {
                                fileOutputStream2.write(bArr, 0, read);
                            } else {
                                break;
                                fileOutputStream2.getChannel().force(true);
                                fileOutputStream2.flush();
                                inputStream = open;
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (fileOutputStream2 != null) {
                                    fileOutputStream2.close();
                                }
                                return str3;
                            }
                        }
                    }
                }
            }
            file.mkdirs();
            str3 = str2 + File.separator + str;
            try {
                file2 = new File(str3);
                exists = file2.exists();
                isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                    if (str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                        z = false;
                        str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                        if (str4 != null) {
                            str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                            file3 = new File(str4);
                            file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                            if (file3.exists()) {
                                file3.delete();
                            }
                            if (file4.exists()) {
                                file4.delete();
                            }
                        }
                        if (exists) {
                            if (str.equals("MToken.zip")) {
                                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                                    if (!str.equals(UpdateConfig.LOCATION_NAME)) {
                                    }
                                    if (!isUpdatableAssetFile) {
                                    }
                                    if (isUpdatableAssetFile) {
                                        fileOutputStream2 = null;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        if (fileOutputStream2 != null) {
                                            fileOutputStream2.close();
                                        }
                                        return str3;
                                    }
                                }
                            }
                        }
                        a(file2);
                        open = context.getResources().getAssets().open(str, 1);
                        fileOutputStream2 = new FileOutputStream(file2);
                        bArr = new byte[8192];
                        while (true) {
                            read = open.read(bArr);
                            if (read <= 0) {
                                break;
                                fileOutputStream2.getChannel().force(true);
                                fileOutputStream2.flush();
                                inputStream = open;
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (fileOutputStream2 != null) {
                                    fileOutputStream2.close();
                                }
                                return str3;
                            }
                            fileOutputStream2.write(bArr, 0, read);
                        }
                    }
                }
            } catch (IOException e9) {
                open = null;
                d.c("getCommonFilePath", "getCommonFilePath error");
                str4 = "";
                if (open != null) {
                    open.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return str4;
            } catch (Throwable th5) {
                th4 = th5;
                open = null;
                if (open != null) {
                    open.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th4;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T> ArrayList<T> a(Context context, String str, String str2, T t) {
        FileInputStream openFileInput;
        ByteArrayOutputStream byteArrayOutputStream;
        ArrayList<T> arrayList;
        ByteArrayOutputStream byteArrayOutputStream2;
        FileInputStream fileInputStream;
        Object obj;
        ByteArrayOutputStream byteArrayOutputStream3;
        Throwable th;
        ArrayList<T> arrayList2 = null;
        if (str == null || str2 == null) {
            return null;
        }
        try {
            openFileInput = context.openFileInput(str2);
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = openFileInput.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    bArr = byteArrayOutputStream.toByteArray();
                    fi fiVar = new fi();
                    fiVar.Z("UTF-8");
                    fiVar.b(TccCryptor.decrypt(bArr, null));
                    arrayList = new ArrayList();
                    arrayList.add(t);
                    arrayList2 = (ArrayList) fiVar.a(str, (Object) arrayList);
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e3) {
                    byteArrayOutputStream2 = byteArrayOutputStream;
                    fileInputStream = openFileInput;
                    Object obj2 = arrayList;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream2 != null) {
                        try {
                            byteArrayOutputStream2.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    obj = byteArrayOutputStream3;
                    return arrayList2;
                } catch (IOException e5) {
                    arrayList2 = arrayList;
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return arrayList2;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (FileNotFoundException e7) {
                fileInputStream = openFileInput;
                byteArrayOutputStream3 = null;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (byteArrayOutputStream2 != null) {
                    byteArrayOutputStream2.close();
                }
                obj = byteArrayOutputStream3;
                return arrayList2;
            } catch (IOException e8) {
                byteArrayOutputStream = null;
                if (openFileInput != null) {
                    openFileInput.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return arrayList2;
            } catch (Throwable th3) {
                Throwable th4 = th3;
                byteArrayOutputStream = null;
                th = th4;
                if (openFileInput != null) {
                    try {
                        openFileInput.close();
                    } catch (IOException e62) {
                        e62.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            fileInputStream = null;
            byteArrayOutputStream3 = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (byteArrayOutputStream2 != null) {
                byteArrayOutputStream2.close();
            }
            obj = byteArrayOutputStream3;
            return arrayList2;
        } catch (IOException e10) {
            byteArrayOutputStream = null;
            openFileInput = null;
            if (openFileInput != null) {
                openFileInput.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return arrayList2;
        } catch (Throwable th32) {
            openFileInput = null;
            th = th32;
            byteArrayOutputStream = null;
            if (openFileInput != null) {
                openFileInput.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
        return arrayList2;
    }

    public static void a(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private static boolean a(Context context, String str, boolean z) {
        InputStream open;
        int i;
        int i2;
        Exception e;
        InputStream fileInputStream;
        byte[] bArr;
        int i3;
        int i4;
        Exception e2;
        Throwable th;
        boolean z2 = false;
        if (z) {
            return true;
        }
        try {
            open = context.getAssets().open(str);
            try {
                byte[] bArr2 = new byte[28];
                open.read(bArr2);
                i = (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16)) | ((bArr2[7] & 255) << 24);
                try {
                    i2 = ((bArr2[27] & 255) << 24) | (((bArr2[24] & 255) | ((bArr2[25] & 255) << 8)) | ((bArr2[26] & 255) << 16));
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (Exception e4) {
                    e = e4;
                    try {
                        e.printStackTrace();
                        if (open != null) {
                            try {
                                open.close();
                            } catch (IOException e5) {
                            }
                        }
                        i2 = 0;
                        fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
                        try {
                            bArr = new byte[28];
                            fileInputStream.read(bArr);
                            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
                            try {
                                i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e6) {
                                    }
                                }
                            } catch (Exception e7) {
                                e2 = e7;
                                try {
                                    e2.printStackTrace();
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    i4 = 0;
                                    if (i != i3) {
                                        return z2;
                                    }
                                    z2 = true;
                                    return z2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e9) {
                                        }
                                    }
                                    throw th;
                                }
                            }
                        } catch (Exception e10) {
                            e2 = e10;
                            i3 = 0;
                            e2.printStackTrace();
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            i4 = 0;
                            if (i != i3) {
                                return z2;
                            }
                            z2 = true;
                            return z2;
                        }
                        if (i != i3) {
                            return z2;
                        }
                        z2 = true;
                        return z2;
                    } catch (Throwable th3) {
                        th = th3;
                        if (open != null) {
                            try {
                                open.close();
                            } catch (IOException e11) {
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e12) {
                e = e12;
                i = 0;
                e.printStackTrace();
                if (open != null) {
                    open.close();
                }
                i2 = 0;
                fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
                bArr = new byte[28];
                fileInputStream.read(bArr);
                i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
                i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (i != i3) {
                    return z2;
                }
                z2 = true;
                return z2;
            }
        } catch (Exception e13) {
            e = e13;
            open = null;
            i = 0;
            e.printStackTrace();
            if (open != null) {
                open.close();
            }
            i2 = 0;
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
            bArr = new byte[28];
            fileInputStream.read(bArr);
            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (i != i3) {
                return z2;
            }
            z2 = true;
            return z2;
        } catch (Throwable th4) {
            th = th4;
            open = null;
            if (open != null) {
                open.close();
            }
            throw th;
        }
        try {
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
            bArr = new byte[28];
            fileInputStream.read(bArr);
            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Exception e14) {
            fileInputStream = open;
            e2 = e14;
            i3 = 0;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i4 = 0;
            if (i != i3) {
                return z2;
            }
            z2 = true;
            return z2;
        } catch (Throwable th5) {
            th = th5;
            fileInputStream = open;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        if (i != i3 || r0 > r3) {
            z2 = true;
        }
        return z2;
    }

    public static String[] b(File file) {
        BufferedInputStream bufferedInputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
            } catch (FileNotFoundException e3) {
                e = e3;
                byteArrayOutputStream = null;
                try {
                    e.printStackTrace();
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e222 = e6;
                byteArrayOutputStream = null;
                e222.printStackTrace();
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = null;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                while (true) {
                    int read = bufferedInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
                String[] split = new String(byteArrayOutputStream.toByteArray()).split("\\n");
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e52) {
                        e52.printStackTrace();
                    }
                }
                return split;
            } catch (FileNotFoundException e7) {
                e = e7;
                e.printStackTrace();
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return null;
            } catch (IOException e8) {
                e22222 = e8;
                e22222.printStackTrace();
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return null;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            e.printStackTrace();
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (IOException e10) {
            e22222 = e10;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            e22222.printStackTrace();
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (Throwable th4) {
            th = th4;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
    }

    public static boolean cy(String str) {
        return new File(str).exists();
    }

    public static String cz(String str) {
        ByteArrayOutputStream byteArrayOutputStream;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedInputStream bufferedInputStream = null;
        BufferedInputStream bufferedInputStream2;
        try {
            bufferedInputStream2 = new BufferedInputStream(new FileInputStream(str));
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = bufferedInputStream2.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    String str2 = new String(byteArrayOutputStream.toByteArray());
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (bufferedInputStream2 != null) {
                        try {
                            bufferedInputStream2.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return str2;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bufferedInputStream = bufferedInputStream2;
                    try {
                        e.printStackTrace();
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        return "";
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedInputStream2 = bufferedInputStream;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        if (bufferedInputStream2 != null) {
                            try {
                                bufferedInputStream2.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222 = e5;
                    try {
                        e222.printStackTrace();
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        if (bufferedInputStream2 != null) {
                            try {
                                bufferedInputStream2.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        return "";
                    } catch (Throwable th3) {
                        th = th3;
                        if (byteArrayOutputStream != null) {
                            byteArrayOutputStream.close();
                        }
                        if (bufferedInputStream2 != null) {
                            bufferedInputStream2.close();
                        }
                        throw th;
                    }
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                byteArrayOutputStream = null;
                bufferedInputStream = bufferedInputStream2;
                e.printStackTrace();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                return "";
            } catch (IOException e7) {
                e22222 = e7;
                byteArrayOutputStream = null;
                e22222.printStackTrace();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream2 != null) {
                    bufferedInputStream2.close();
                }
                return "";
            } catch (Throwable th4) {
                th = th4;
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream2 != null) {
                    bufferedInputStream2.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            byteArrayOutputStream = null;
            e.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            return "";
        } catch (IOException e9) {
            e22222 = e9;
            byteArrayOutputStream = null;
            bufferedInputStream2 = null;
            e22222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream2 != null) {
                bufferedInputStream2.close();
            }
            return "";
        } catch (Throwable th5) {
            th = th5;
            byteArrayOutputStream = null;
            bufferedInputStream2 = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream2 != null) {
                bufferedInputStream2.close();
            }
            throw th;
        }
    }

    private static boolean d(Context context, String str) {
        InputStream open;
        int i;
        int i2;
        Exception e;
        boolean z;
        Throwable th;
        FileInputStream fileInputStream = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + str);
        if (!file.exists()) {
            return true;
        }
        try {
            open = context.getAssets().open(str, 1);
            try {
                i = mr.c(open).Bi;
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                i2 = i;
            } catch (Exception e3) {
                e = e3;
                try {
                    e.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    z = false;
                    if (i2 != 0) {
                        return false;
                    }
                    try {
                        open = new FileInputStream(file);
                        try {
                            i = mr.c(open).Bi;
                            if (open != null) {
                                try {
                                    open.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                        } catch (Exception e5) {
                            e = e5;
                            InputStream inputStream = open;
                            try {
                                e.printStackTrace();
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e42) {
                                        e42.printStackTrace();
                                    }
                                }
                                i = 0;
                                return i2 > i;
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e222) {
                                        e222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = open;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    } catch (Exception e6) {
                        e = e6;
                        e.printStackTrace();
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        i = 0;
                        if (i2 > i) {
                        }
                        return i2 > i;
                    }
                    if (i2 > i) {
                    }
                    return i2 > i;
                } catch (Throwable th4) {
                    th = th4;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e7) {
            e = e7;
            open = null;
            e.printStackTrace();
            if (open != null) {
                open.close();
            }
            z = false;
            if (i2 != 0) {
                return false;
            }
            open = new FileInputStream(file);
            i = mr.c(open).Bi;
            if (open != null) {
                open.close();
            }
            if (i2 > i) {
            }
            return i2 > i;
        } catch (Throwable th5) {
            th = th5;
            open = null;
            if (open != null) {
                open.close();
            }
            throw th;
        }
        if (i2 != 0) {
            return false;
        }
        open = new FileInputStream(file);
        i = mr.c(open).Bi;
        if (open != null) {
            open.close();
        }
        if (i2 > i) {
        }
        return i2 > i;
    }

    public static boolean dE() {
        String str = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File file = new File(str);
        if (!file.isDirectory() && !file.mkdirs()) {
            return false;
        }
        file = new File(str, ".probe");
        try {
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return false;
            }
            file.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean eV() {
        nc ncVar = new nc("tms");
        String string = ncVar.getString("soft_version", "");
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION);
        if (string.equals(strFromEnvMap)) {
            return false;
        }
        ncVar.a("soft_version", strFromEnvMap, true);
        return true;
    }

    public static boolean eW() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState != null ? externalStorageState.equals("mounted") : false;
    }

    public static List<String> eX() {
        BufferedReader bufferedReader;
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        List<String> arrayList = new ArrayList();
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            try {
                String str = "^/(?:sys|system|dev|cache|proc|acct|data|efs|osh|pds|(?:mnt/asec)|(?:mnt/obb)|(?:mnt/secure))/*.*$";
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split("\\s+");
                    if (split.length >= 4 && split[3].startsWith("rw")) {
                        CharSequence charSequence = split[1];
                        if (charSequence.equals("/")) {
                            continue;
                        } else {
                            Matcher matcher = Pattern.compile(str).matcher(charSequence);
                            if (matcher != null) {
                                if (matcher.find()) {
                                }
                            }
                            if (!arrayList.contains(charSequence)) {
                                arrayList.add(charSequence);
                            }
                        }
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return arrayList;
            } catch (FileNotFoundException e4) {
                e2 = e4;
            } catch (IOException e5) {
                e3 = e5;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            bufferedReader = null;
            try {
                e2.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e8) {
            e32 = e8;
            bufferedReader = null;
            e32.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            throw th;
        }
    }

    public static List<String> eY() {
        List<String> arrayList = new ArrayList();
        try {
            arrayList.add(Environment.getExternalStorageDirectory().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        arrayList.add("/storage");
        arrayList.add("/mnt/sdcard");
        arrayList.add("/mnt/sdcard-ext");
        arrayList.add("/storage/sdcard1");
        arrayList.addAll(eX());
        List<String> arrayList2 = new ArrayList();
        for (String file : arrayList) {
            Object canonicalPath;
            File file2 = new File(file);
            if (file2.exists() && file2.canRead()) {
                try {
                    canonicalPath = file2.getCanonicalPath();
                } catch (IOException e2) {
                }
                if (!(canonicalPath == null || arrayList2.contains(canonicalPath))) {
                    arrayList2.add(canonicalPath);
                }
            }
            canonicalPath = null;
            arrayList2.add(canonicalPath);
        }
        arrayList.clear();
        return arrayList2;
    }

    private static boolean o(Context context) {
        int i;
        int i2;
        Exception e;
        Throwable th;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(UpdateConfig.LOCATION_NAME, 1);
            byte[] bArr = new byte[8];
            inputStream.read(bArr);
            i = ((bArr[7] & 255) << 24) | (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16));
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
            i = 0;
        } catch (Throwable th2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        }
        InputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + UpdateConfig.LOCATION_NAME);
            try {
                byte[] bArr2 = new byte[8];
                fileInputStream.read(bArr2);
                i2 = ((bArr2[7] & 255) << 24) | (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16));
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (Exception e7) {
                e = e7;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    i2 = 0;
                    return i > i2;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e9) {
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e10) {
            Exception exception = e10;
            fileInputStream = inputStream;
            e = exception;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i2 = 0;
            if (i > i2) {
            }
        } catch (Throwable th4) {
            th = th4;
            fileInputStream = inputStream;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        if (i > i2) {
        }
    }

    public static int p(long j) {
        int i = 1;
        if (!eW()) {
            return 1;
        }
        if (!dE()) {
            return 2;
        }
        a aVar = new a();
        h.a(aVar);
        if (aVar.Ld < j) {
            i = 0;
        }
        return i == 0 ? 3 : 0;
    }

    public static List<String> p(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        List<String> arrayList = new ArrayList();
        try {
            Object[] objArr = (Object[]) storageManager.getClass().getMethod("getVolumeList", new Class[0]).invoke(storageManager, new Object[0]);
            if (objArr != null && objArr.length > 0) {
                Method declaredMethod = objArr[0].getClass().getDeclaredMethod("getPath", new Class[0]);
                Method method = storageManager.getClass().getMethod("getVolumeState", new Class[]{String.class});
                for (Object invoke : objArr) {
                    String str = (String) declaredMethod.invoke(invoke, new Object[0]);
                    if (str != null) {
                        if ("mounted".equals(method.invoke(storageManager, new Object[]{str}))) {
                            arrayList.add(str);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static final String q(String str, String str2) {
        String decode = Uri.decode(str);
        if (decode != null) {
            int indexOf = decode.indexOf(63);
            if (indexOf > 0) {
                decode = decode.substring(0, indexOf);
            }
            if (!decode.endsWith("/")) {
                indexOf = decode.lastIndexOf(47) + 1;
                if (indexOf > 0) {
                    decode = decode.substring(indexOf);
                    if (decode == null) {
                        decode = str2;
                    }
                    return decode == null ? decode : "downloadfile";
                }
            }
        }
        decode = null;
        if (decode == null) {
            decode = str2;
        }
        if (decode == null) {
        }
    }
}
