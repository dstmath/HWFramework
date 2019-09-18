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

    public static DrmConvertSession open(Context context, String mimeType) {
        DrmManagerClient drmClient = null;
        int convertSessionId = -1;
        if (!(context == null || mimeType == null || mimeType.equals(""))) {
            try {
                drmClient = new DrmManagerClient(context);
                try {
                    convertSessionId = drmClient.openConvertSession(mimeType);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Conversion of Mimetype: " + mimeType + " is not supported.", e);
                } catch (IllegalStateException e2) {
                    Log.w(TAG, "Could not access Open DrmFramework.", e2);
                }
            } catch (IllegalArgumentException e3) {
                Log.w(TAG, "DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e4) {
                Log.w(TAG, "DrmManagerClient didn't initialize properly.");
            }
        }
        if (drmClient == null || convertSessionId < 0) {
            return null;
        }
        return new DrmConvertSession(drmClient, convertSessionId);
    }

    public byte[] convert(byte[] inBuffer, int size) {
        DrmConvertedStatus convertedStatus;
        if (inBuffer != null) {
            try {
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
        } else {
            throw new IllegalArgumentException("Parameter inBuffer is null");
        }
    }

    public int close(String filename) {
        RandomAccessFile rndAccessFile;
        String str;
        String str2;
        int result = 491;
        if (this.mDrmClient == null || this.mConvertSessionId < 0) {
            return 491;
        }
        try {
            DrmConvertedStatus convertedStatus = this.mDrmClient.closeConvertSession(this.mConvertSessionId);
            if (convertedStatus == null || convertedStatus.statusCode != 1 || convertedStatus.convertedData == null) {
                return 406;
            }
            rndAccessFile = null;
            try {
                rndAccessFile = new RandomAccessFile(filename, "rw");
                rndAccessFile.seek((long) convertedStatus.offset);
                rndAccessFile.write(convertedStatus.convertedData);
                try {
                    rndAccessFile.close();
                    return 200;
                } catch (IOException e) {
                    e = e;
                    result = 492;
                    str = TAG;
                    str2 = "Failed to close File:" + filename + ".";
                    Log.w(str, str2, e);
                    return result;
                }
            } catch (FileNotFoundException e2) {
                Log.w(TAG, "File: " + filename + " could not be found.");
                if (rndAccessFile == null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e3) {
                    e = e3;
                    result = 492;
                    str = TAG;
                    str2 = "Failed to close File:" + filename + ".";
                    Log.w(str, str2, e);
                    return result;
                }
            } catch (IOException e4) {
                Log.w(TAG, "Could not access File: " + filename + " .", e4);
                if (rndAccessFile == null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e5) {
                    e = e5;
                    result = 492;
                    str = TAG;
                    str2 = "Failed to close File:" + filename + ".";
                    Log.w(str, str2, e);
                    return result;
                }
            } catch (IllegalArgumentException e6) {
                Log.w(TAG, "Could not open file in mode: rw", e6);
                if (rndAccessFile == null) {
                    return 492;
                }
                try {
                    rndAccessFile.close();
                    return 492;
                } catch (IOException e7) {
                    e = e7;
                    result = 492;
                    str = TAG;
                    str2 = "Failed to close File:" + filename + ".";
                    Log.w(str, str2, e);
                    return result;
                }
            } catch (SecurityException e8) {
                Log.w(TAG, "Access to File: " + filename + " was denied denied by SecurityManager.", e8);
                if (rndAccessFile == null) {
                    return 491;
                }
                try {
                    rndAccessFile.close();
                    return 491;
                } catch (IOException e9) {
                    e = e9;
                    result = 492;
                    str = TAG;
                    str2 = "Failed to close File:" + filename + ".";
                    Log.w(str, str2, e);
                    return result;
                }
            }
        } catch (IllegalStateException e10) {
            Log.w(TAG, "Could not close convertsession. Convertsession: " + this.mConvertSessionId, e10);
            return result;
        } catch (Throwable e11) {
            if (rndAccessFile != null) {
                try {
                    rndAccessFile.close();
                } catch (IOException e12) {
                    result = 492;
                    Log.w(TAG, "Failed to close File:" + filename + ".", e12);
                }
            }
            throw e11;
        }
    }
}
