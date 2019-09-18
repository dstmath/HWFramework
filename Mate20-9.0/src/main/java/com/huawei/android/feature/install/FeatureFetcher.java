package com.huawei.android.feature.install;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FeatureFetcher implements IFetchFeature {
    /* access modifiers changed from: private */
    public static final String TAG = FeatureFetcher.class.getSimpleName();
    public Context mContext;

    public FeatureFetcher(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0091 A[SYNTHETIC, Splitter:B:26:0x0091] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0096 A[SYNTHETIC, Splitter:B:29:0x0096] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x009b A[SYNTHETIC, Splitter:B:32:0x009b] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x010f A[SYNTHETIC, Splitter:B:58:0x010f] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0114 A[SYNTHETIC, Splitter:B:61:0x0114] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0119 A[SYNTHETIC, Splitter:B:64:0x0119] */
    public boolean fetchApk(String str, Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor;
        FileOutputStream fileOutputStream;
        BufferedInputStream bufferedInputStream;
        boolean z = false;
        Log.d(TAG, "fetch APK " + str);
        try {
            if (InstallStorageManager.getUnverifyApksDir(this.mContext) != null) {
                parcelFileDescriptor = this.mContext.getContentResolver().openFileDescriptor(uri, "r");
                if (parcelFileDescriptor != null) {
                    try {
                        bufferedInputStream = new BufferedInputStream(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
                    } catch (IOException e) {
                        e = e;
                        fileOutputStream = null;
                        bufferedInputStream = null;
                        try {
                            Log.e(TAG, e.getMessage());
                            if (bufferedInputStream != null) {
                            }
                            if (fileOutputStream != null) {
                            }
                            if (parcelFileDescriptor != null) {
                            }
                            Log.d(TAG, "fetch APK " + str + " end");
                            return z;
                        } catch (Throwable th) {
                            th = th;
                            if (bufferedInputStream != null) {
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, e2.getMessage());
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e3) {
                                    Log.e(TAG, e3.getMessage());
                                }
                            }
                            if (parcelFileDescriptor != null) {
                                try {
                                    parcelFileDescriptor.close();
                                } catch (IOException e4) {
                                    Log.e(TAG, e4.getMessage());
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        fileOutputStream = null;
                        bufferedInputStream = null;
                        if (bufferedInputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        if (parcelFileDescriptor != null) {
                        }
                        throw th;
                    }
                    try {
                        fileOutputStream = new FileOutputStream(r1.getAbsolutePath() + File.separator + str + ".apk");
                        try {
                            byte[] bArr = new byte[4096];
                            while (true) {
                                int read = bufferedInputStream.read(bArr, 0, 4096);
                                if (read == -1) {
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, read);
                            }
                            z = true;
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e5) {
                                Log.e(TAG, e5.getMessage());
                            }
                            try {
                                fileOutputStream.close();
                            } catch (IOException e6) {
                                Log.e(TAG, e6.getMessage());
                            }
                            if (parcelFileDescriptor != null) {
                                try {
                                    parcelFileDescriptor.close();
                                } catch (IOException e7) {
                                    Log.e(TAG, e7.getMessage());
                                }
                            }
                        } catch (IOException e8) {
                            e = e8;
                            Log.e(TAG, e.getMessage());
                            if (bufferedInputStream != null) {
                            }
                            if (fileOutputStream != null) {
                            }
                            if (parcelFileDescriptor != null) {
                            }
                            Log.d(TAG, "fetch APK " + str + " end");
                            return z;
                        }
                    } catch (IOException e9) {
                        e = e9;
                        fileOutputStream = null;
                        Log.e(TAG, e.getMessage());
                        if (bufferedInputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        if (parcelFileDescriptor != null) {
                        }
                        Log.d(TAG, "fetch APK " + str + " end");
                        return z;
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = null;
                        if (bufferedInputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        if (parcelFileDescriptor != null) {
                        }
                        throw th;
                    }
                    Log.d(TAG, "fetch APK " + str + " end");
                } else if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e10) {
                        Log.e(TAG, e10.getMessage());
                    }
                }
            }
        } catch (IOException e11) {
            e = e11;
            parcelFileDescriptor = null;
            fileOutputStream = null;
            bufferedInputStream = null;
            Log.e(TAG, e.getMessage());
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e12) {
                    Log.e(TAG, e12.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e13) {
                    Log.e(TAG, e13.getMessage());
                }
            }
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e14) {
                    Log.e(TAG, e14.getMessage());
                }
            }
            Log.d(TAG, "fetch APK " + str + " end");
            return z;
        } catch (Throwable th4) {
            th = th4;
            parcelFileDescriptor = null;
            fileOutputStream = null;
            bufferedInputStream = null;
            if (bufferedInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            if (parcelFileDescriptor != null) {
            }
            throw th;
        }
        return z;
    }

    public void fetch(InstallSessionState installSessionState, FeatureFetchListener featureFetchListener) {
        InstallBgExecutor.getExecutor().execute(new h(this, installSessionState, featureFetchListener));
    }

    public void fetch(InstallSessionState installSessionState, InstallSessionStateNotifier installSessionStateNotifier) {
        InstallBgExecutor.getExecutor().execute(new g(this, installSessionState, installSessionStateNotifier));
    }
}
