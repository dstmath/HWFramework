package com.google.android.mms.util;

import android.content.Context;
import android.drm.DrmConvertedStatus;
import android.drm.DrmManagerClient;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DrmConvertSession {
    private static final String TAG = "DrmConvertSession";
    private int mConvertSessionId;
    private DrmManagerClient mDrmClient;

    private DrmConvertSession(DrmManagerClient drmClient, int convertSessionId) {
        this.mDrmClient = drmClient;
        this.mConvertSessionId = convertSessionId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x001e A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x001e A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static DrmConvertSession open(Context context, String mimeType) {
        DrmManagerClient drmManagerClient = null;
        int convertSessionId = -1;
        if (!(context == null || mimeType == null || (mimeType.equals("") ^ 1) == 0)) {
            try {
                DrmManagerClient drmClient = new DrmManagerClient(context);
                try {
                    convertSessionId = drmClient.openConvertSession(mimeType);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Conversion of Mimetype: " + mimeType + " is not supported.", e);
                } catch (IllegalStateException e2) {
                    try {
                        Log.w(TAG, "Could not access Open DrmFramework.", e2);
                    } catch (IllegalArgumentException e3) {
                        drmManagerClient = drmClient;
                    } catch (IllegalStateException e4) {
                        drmManagerClient = drmClient;
                        Log.w(TAG, "DrmManagerClient didn't initialize properly.");
                        if (drmManagerClient != null) {
                        }
                        return null;
                    }
                }
                drmManagerClient = drmClient;
            } catch (IllegalArgumentException e5) {
                Log.w(TAG, "DrmManagerClient instance could not be created, context is Illegal.");
                if (drmManagerClient != null) {
                }
                return null;
            } catch (IllegalStateException e6) {
                Log.w(TAG, "DrmManagerClient didn't initialize properly.");
                if (drmManagerClient != null) {
                }
                return null;
            }
        }
        if (drmManagerClient != null || convertSessionId < 0) {
            return null;
        }
        return new DrmConvertSession(drmManagerClient, convertSessionId);
    }

    public byte[] convert(byte[] inBuffer, int size) {
        if (inBuffer != null) {
            try {
                DrmConvertedStatus convertedStatus;
                if (size != inBuffer.length) {
                    byte[] buf = new byte[size];
                    System.arraycopy(inBuffer, 0, buf, 0, size);
                    convertedStatus = this.mDrmClient.convertData(this.mConvertSessionId, buf);
                } else {
                    convertedStatus = this.mDrmClient.convertData(this.mConvertSessionId, inBuffer);
                }
                if (convertedStatus == null || convertedStatus.statusCode != 1 || convertedStatus.convertedData == null) {
                    return null;
                }
                return convertedStatus.convertedData;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Buffer with data to convert is illegal. Convertsession: " + this.mConvertSessionId, e);
                return null;
            } catch (IllegalStateException e2) {
                Log.w(TAG, "Could not convert data. Convertsession: " + this.mConvertSessionId, e2);
                return null;
            }
        }
        throw new IllegalArgumentException("Parameter inBuffer is null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:? A:{SYNTHETIC, RETURN, Catch:{ IllegalStateException -> 0x0062 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x017f A:{SYNTHETIC, Splitter: B:64:0x017f} */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN, Catch:{ IllegalStateException -> 0x0062 }} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x012e A:{SYNTHETIC, Splitter: B:53:0x012e} */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A:{SYNTHETIC, RETURN, Catch:{ IllegalStateException -> 0x0062 }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00dd A:{SYNTHETIC, Splitter: B:42:0x00dd} */
    /* JADX WARNING: Removed duplicated region for block: B:93:? A:{SYNTHETIC, RETURN, Catch:{ IllegalStateException -> 0x0062 }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a4 A:{SYNTHETIC, Splitter: B:31:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01ad A:{SYNTHETIC, Splitter: B:72:0x01ad} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int close(String filename) {
        IOException e;
        FileNotFoundException e2;
        IllegalArgumentException e3;
        SecurityException e4;
        Throwable th;
        int result = 491;
        if (this.mDrmClient == null || this.mConvertSessionId < 0) {
            return 491;
        }
        try {
            DrmConvertedStatus convertedStatus = this.mDrmClient.closeConvertSession(this.mConvertSessionId);
            if (convertedStatus == null || convertedStatus.statusCode != 1 || convertedStatus.convertedData == null) {
                return 406;
            }
            RandomAccessFile rndAccessFile = null;
            try {
                RandomAccessFile rndAccessFile2 = new RandomAccessFile(filename, "rw");
                try {
                    rndAccessFile2.seek((long) convertedStatus.offset);
                    rndAccessFile2.write(convertedStatus.convertedData);
                    if (rndAccessFile2 == null) {
                        return 200;
                    }
                    try {
                        rndAccessFile2.close();
                        return 200;
                    } catch (IOException e5) {
                        result = 492;
                        Log.w(TAG, "Failed to close File:" + filename + ".", e5);
                        return 492;
                    }
                } catch (FileNotFoundException e6) {
                    e2 = e6;
                    rndAccessFile = rndAccessFile2;
                    result = 492;
                    Log.w(TAG, "File: " + filename + " could not be found.", e2);
                    if (rndAccessFile != null) {
                    }
                } catch (IOException e7) {
                    e5 = e7;
                    rndAccessFile = rndAccessFile2;
                    result = 492;
                    Log.w(TAG, "Could not access File: " + filename + " .", e5);
                    if (rndAccessFile != null) {
                    }
                } catch (IllegalArgumentException e8) {
                    e3 = e8;
                    rndAccessFile = rndAccessFile2;
                    result = 492;
                    Log.w(TAG, "Could not open file in mode: rw", e3);
                    if (rndAccessFile != null) {
                    }
                } catch (SecurityException e9) {
                    e4 = e9;
                    rndAccessFile = rndAccessFile2;
                    try {
                        Log.w(TAG, "Access to File: " + filename + " was denied denied by SecurityManager.", e4);
                        if (rndAccessFile != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (rndAccessFile != null) {
                            try {
                                rndAccessFile.close();
                            } catch (IOException e52) {
                                result = 492;
                                Log.w(TAG, "Failed to close File:" + filename + ".", e52);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    rndAccessFile = rndAccessFile2;
                    if (rndAccessFile != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e2 = e10;
                result = 492;
                Log.w(TAG, "File: " + filename + " could not be found.", e2);
                if (rndAccessFile != null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e522) {
                    result = 492;
                    Log.w(TAG, "Failed to close File:" + filename + ".", e522);
                    return 492;
                }
            } catch (IOException e11) {
                e522 = e11;
                result = 492;
                Log.w(TAG, "Could not access File: " + filename + " .", e522);
                if (rndAccessFile != null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e5222) {
                    result = 492;
                    Log.w(TAG, "Failed to close File:" + filename + ".", e5222);
                    return 492;
                }
            } catch (IllegalArgumentException e12) {
                e3 = e12;
                result = 492;
                Log.w(TAG, "Could not open file in mode: rw", e3);
                if (rndAccessFile != null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e52222) {
                    result = 492;
                    Log.w(TAG, "Failed to close File:" + filename + ".", e52222);
                    return 492;
                }
            } catch (SecurityException e13) {
                e4 = e13;
                Log.w(TAG, "Access to File: " + filename + " was denied denied by SecurityManager.", e4);
                if (rndAccessFile != null) {
                    return 491;
                }
                try {
                    rndAccessFile.close();
                    return 491;
                } catch (IOException e522222) {
                    result = 492;
                    Log.w(TAG, "Failed to close File:" + filename + ".", e522222);
                    return 492;
                }
            }
        } catch (IllegalStateException e14) {
            Log.w(TAG, "Could not close convertsession. Convertsession: " + this.mConvertSessionId, e14);
            return result;
        }
    }
}
