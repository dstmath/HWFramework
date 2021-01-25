package ohos.global.innerkit.asset;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import ohos.global.resource.Entry;
import ohos.global.resource.RawFileDescriptor;
import ohos.global.resource.RawFileDescriptorImpl;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Package {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "Package");
    public static final String SYS_RESOURCE_PREFIX = "ohos:";
    private static Resources sysResource = null;
    private Resources resource = null;

    public void setResource(Resources resources) {
        this.resource = resources;
    }

    public InputStream open(String str) throws IOException {
        if (!(str == null || str.length() == 0)) {
            if (sysResource != null && str.startsWith(SYS_RESOURCE_PREFIX)) {
                return sysResource.getAssets().open(str.substring(5), 1);
            } else if (this.resource != null && !str.startsWith(SYS_RESOURCE_PREFIX)) {
                return this.resource.getAssets().open(str, 1);
            }
        }
        return null;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0050: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v1 java.lang.String) */
    public RawFileDescriptor openRawFileDescriptor(String str) throws IOException {
        AssetFileDescriptor assetFileDescriptor;
        if (str == null || str.length() == 0) {
            HiLogLabel hiLogLabel = LABEL;
            Object[] objArr = new Object[1];
            if (str == null) {
                str = "null";
            }
            objArr[0] = str;
            HiLog.error(hiLogLabel, "open file path is %{public}s", objArr);
            return null;
        }
        if (sysResource != null && str.startsWith(SYS_RESOURCE_PREFIX)) {
            assetFileDescriptor = sysResource.getAssets().openFd(str.substring(5));
        } else if (this.resource == null || str.startsWith(SYS_RESOURCE_PREFIX)) {
            return null;
        } else {
            assetFileDescriptor = this.resource.getAssets().openFd(str);
        }
        return new RawFileDescriptorImpl(new AfdAdapter(assetFileDescriptor));
    }

    public String[] list(String str) throws IOException {
        if (str == null) {
            HiLog.error(LABEL, "list path is null", new Object[0]);
            return new String[0];
        } else if (sysResource == null || !str.startsWith(SYS_RESOURCE_PREFIX)) {
            return (this.resource == null || str.startsWith(SYS_RESOURCE_PREFIX)) ? new String[0] : this.resource.getAssets().list(str);
        } else {
            return sysResource.getAssets().list(str.substring(5));
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0084: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v1 java.lang.String) */
    public Entry.Type getEntryType2(String str) {
        InputStream inputStream;
        if (str == null || str.length() == 0) {
            HiLogLabel hiLogLabel = LABEL;
            Object[] objArr = new Object[1];
            if (str == null) {
                str = "null";
            }
            objArr[0] = str;
            HiLog.error(hiLogLabel, "list path is %{public}s", objArr);
            return Entry.Type.UNKNOWN;
        }
        InputStream inputStream2 = null;
        try {
            if (sysResource != null && str.startsWith(SYS_RESOURCE_PREFIX)) {
                inputStream = sysResource.getAssets().open(str.substring(5), 1);
            } else if (this.resource == null || str.startsWith(SYS_RESOURCE_PREFIX)) {
                return Entry.Type.UNKNOWN;
            } else {
                inputStream = this.resource.getAssets().open(str, 1);
            }
            Entry.Type type = Entry.Type.FILE;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException unused) {
                    HiLog.error(LABEL, "getEntryType close error.", new Object[0]);
                }
            }
            return type;
        } catch (IOException unused2) {
            HiLog.error(LABEL, "getEntryType open error.", new Object[0]);
            Entry.Type type2 = Entry.Type.FOLDER;
            if (0 != 0) {
                try {
                    inputStream2.close();
                } catch (IOException unused3) {
                    HiLog.error(LABEL, "getEntryType close error.", new Object[0]);
                }
            }
            return type2;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream2.close();
                } catch (IOException unused4) {
                    HiLog.error(LABEL, "getEntryType close error.", new Object[0]);
                }
            }
            throw th;
        }
    }

    public void loadSystemResource(String str) {
        if (str != null && str.length() != 0 && sysResource == null && new File(str).exists()) {
            try {
                AssetManager assetManager = new AssetManager();
                AssetManager.class.getMethod("addAssetPath", String.class).invoke(assetManager, str);
                HiLog.debug(LABEL, "load system resource OK", new Object[0]);
                sysResource = new Resources(assetManager, null, null);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
                HiLog.error(LABEL, "load system resource Failed", new Object[0]);
            }
        }
    }

    public static <T> int getAResId(String str, String str2, T t) {
        if (t == null) {
            HiLog.error(LABEL, "context is null", new Object[0]);
            return 0;
        } else if (t instanceof Context) {
            T t2 = t;
            Resources resources = t2.getResources();
            if (resources != null) {
                return resources.getIdentifier(str, str2, t2.getPackageName());
            }
            HiLog.error(LABEL, "reses is null", new Object[0]);
            return 0;
        } else {
            HiLog.error(LABEL, "context is not an instance of Context", new Object[0]);
            return 0;
        }
    }

    public AssetManager getAAssetManager() {
        Resources resources = this.resource;
        if (resources == null) {
            return null;
        }
        return resources.getAssets();
    }
}
