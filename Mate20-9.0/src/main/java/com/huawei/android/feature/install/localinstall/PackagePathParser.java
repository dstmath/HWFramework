package com.huawei.android.feature.install.localinstall;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.huawei.android.feature.install.InstallStorageManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PackagePathParser extends PathParser {
    private static final String TAG = PackagePathParser.class.getSimpleName();
    private String mAssetsApkName;
    private String mAssetsDirName;
    private String mPackageName;

    public PackagePathParser(Context context, String str) {
        super(context, str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0085 A[SYNTHETIC, Splitter:B:22:0x0085] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008a A[SYNTHETIC, Splitter:B:25:0x008a] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d1 A[SYNTHETIC, Splitter:B:45:0x00d1] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00d6 A[SYNTHETIC, Splitter:B:48:0x00d6] */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A[RETURN, SYNTHETIC] */
    private File getLoadingFile(File file) {
        Context context;
        FileOutputStream fileOutputStream;
        BufferedInputStream bufferedInputStream;
        try {
            context = this.mContext.createPackageContext(this.mPackageName, 3);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "packageNotFound");
            context = null;
        } catch (Exception e2) {
            Log.d(TAG, "createPackageContext " + e2.getMessage());
            context = null;
        }
        if (context == null) {
            return null;
        }
        try {
            bufferedInputStream = new BufferedInputStream(context.getAssets().open(this.mAssetsDirName + File.separator + this.mAssetsApkName));
            try {
                fileOutputStream = new FileOutputStream(new File(file, this.mAssetsApkName));
            } catch (IOException e3) {
                e = e3;
                fileOutputStream = null;
                try {
                    Log.d(TAG, e.getMessage());
                    if (bufferedInputStream != null) {
                    }
                    if (fileOutputStream == null) {
                    }
                } catch (Throwable th) {
                    th = th;
                    if (bufferedInputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = null;
                if (bufferedInputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = bufferedInputStream.read(bArr, 0, 4096);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                File file2 = new File(file, this.mAssetsApkName);
                try {
                    bufferedInputStream.close();
                } catch (IOException e4) {
                    Log.d(TAG, e4.getMessage());
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e5) {
                    Log.d(TAG, e5.getMessage());
                }
                return file2;
            } catch (IOException e6) {
                e = e6;
                Log.d(TAG, e.getMessage());
                if (bufferedInputStream != null) {
                }
                if (fileOutputStream == null) {
                }
            }
        } catch (IOException e7) {
            e = e7;
            fileOutputStream = null;
            bufferedInputStream = null;
            Log.d(TAG, e.getMessage());
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e8) {
                    Log.d(TAG, e8.getMessage());
                }
            }
            if (fileOutputStream == null) {
                return null;
            }
            try {
                fileOutputStream.close();
                return null;
            } catch (IOException e9) {
                Log.d(TAG, e9.getMessage());
                return null;
            }
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            bufferedInputStream = null;
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e10) {
                    Log.d(TAG, e10.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e11) {
                    Log.d(TAG, e11.getMessage());
                }
            }
            throw th;
        }
    }

    public File getLoadingFile() {
        return getLoadingFile(InstallStorageManager.getBaseDir(this.mContext));
    }

    public int parsePath() {
        if (this.mOriginPath.contains("../") || this.mOriginPath.contains("./")) {
            return -19;
        }
        try {
            String[] split = this.mOriginPath.substring(10, this.mOriginPath.length()).split("/");
            this.mPackageName = split[0];
            this.mAssetsDirName = split[1];
            this.mAssetsApkName = split[2];
            return 0;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Log.d(TAG, "packagePathParser parsePath error");
            return -19;
        }
    }
}
