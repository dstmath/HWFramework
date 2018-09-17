package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MalAppDetectReport {
    private static final int EVT_MALAPP_REPORT = 2000;
    private static final int EVT_MALAPP_REPORT_ID = 940000004;
    private static final boolean HW_DEBUG;
    private static final String ROOT_STATE_MATCH = "DEVPATH=/kernel/oases_attack";
    private static final String TAG = "MalAppDetectReport";
    private static MalAppDetectReport mInstance;
    private Context mContext;
    private UEventHandler mHandler;
    private HandlerThread mHandlerThread;
    private final UEventObserver mUEventObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            if (MalAppDetectReport.HW_DEBUG) {
                Log.d(MalAppDetectReport.TAG, "onUEvent event: " + event);
            }
            MalAppDetectReport.this.mHandler.sendMessage(MalAppDetectReport.this.mHandler.obtainMessage(2000, event));
        }
    };

    private class UEventHandler extends Handler {
        public UEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2000:
                    MalAppDetectReport.this.reportData((UEvent) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    private void reportData(UEvent event) {
        if (event != null) {
            try {
                int UID = Integer.parseInt(event.get("uid"));
                String PATCH_ID = event.get(HwSecDiagnoseConstant.MALAPP_PATCH_ID_OLD);
                if (UID != -1) {
                    JSONObject json = getApkInfo(UID, PATCH_ID);
                    if (json != null) {
                        onMalAppReport(json);
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "some data is not of the type Integer during parsing UEvent");
            }
        }
    }

    public void onMalAppReport(JSONObject json) {
        JSONArray arraypks = null;
        try {
            arraypks = json.getJSONArray(HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
        } catch (JSONException e) {
            Log.e(TAG, "getJSONArray JSONException!");
        }
        if (arraypks == null) {
            Log.w(TAG, "arraypks is null!");
            return;
        }
        int arraypksLength = arraypks.length();
        for (int index = 0; index < arraypksLength; index++) {
            try {
                JSONObject object = arraypks.getJSONObject(index);
                if (object == null) {
                    Log.w(TAG, "object is null!");
                } else {
                    Log.i(TAG, object.toString());
                    EventStream eStream = IMonitor.openEventStream(EVT_MALAPP_REPORT_ID);
                    eStream.setParam((short) 0, object.optString(HwSecDiagnoseConstant.MALAPP_APK_NAME));
                    eStream.setParam((short) 1, object.optString(HwSecDiagnoseConstant.MALAPP_APK_HASH));
                    eStream.setParam((short) 2, object.optString(HwSecDiagnoseConstant.MALAPP_APK_CERT));
                    eStream.setParam((short) 3, object.optString(HwSecDiagnoseConstant.MALAPP_PATCH_ID));
                    if (IMonitor.sendEvent(eStream)) {
                        Log.i(TAG, "sendEvent data success");
                    }
                    IMonitor.closeEventStream(eStream);
                }
            } catch (JSONException e2) {
                Log.e(TAG, "getJSONObject JSONException!");
            }
        }
    }

    private MalAppDetectReport(Context context) {
        this.mContext = context;
        this.mUEventObserver.startObserving(ROOT_STATE_MATCH);
        this.mHandlerThread = new HandlerThread("uevent handler : MalAppDetect");
        this.mHandlerThread.start();
        this.mHandler = new UEventHandler(this.mHandlerThread.getLooper());
    }

    public static void init(Context context) {
        synchronized (MalAppDetectReport.class) {
            if (mInstance == null) {
                mInstance = new MalAppDetectReport(context);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00cb A:{SYNTHETIC, Splitter: B:53:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00d0 A:{Catch:{ IOException -> 0x00d4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ab A:{SYNTHETIC, Splitter: B:43:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b0 A:{Catch:{ IOException -> 0x00b4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008b A:{SYNTHETIC, Splitter: B:33:0x008b} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0090 A:{Catch:{ IOException -> 0x0094 }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006b A:{SYNTHETIC, Splitter: B:23:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0070 A:{Catch:{ IOException -> 0x0074 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00e3 A:{SYNTHETIC, Splitter: B:61:0x00e3} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00e8 A:{Catch:{ IOException -> 0x00ec }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String calculateApkHash(String apkName) {
        Throwable th;
        InputStream inputStream = null;
        ZipFile zf = null;
        String apkDigest = null;
        boolean flag = false;
        try {
            byte[] buffer = new byte[8192];
            MessageDigest msgDigest = MessageDigest.getInstance(HwSecDiagnoseConstant.MALAPP_APK_SHA256);
            ZipFile zf2 = new ZipFile(this.mContext.getPackageManager().getApplicationInfo(apkName, 0).sourceDir);
            try {
                inputStream = zf2.getInputStream(zf2.getEntry("META-INF/MANIFEST.MF"));
                while (true) {
                    int numRead = inputStream.read(buffer);
                    if (numRead == -1) {
                        break;
                    }
                    msgDigest.update(buffer, 0, numRead);
                    flag = true;
                }
                if (flag) {
                    apkDigest = byteArray2Hex(msgDigest.digest());
                }
                if (zf2 != null) {
                    try {
                        zf2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "InputStream close failure!");
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                zf = zf2;
            } catch (NameNotFoundException e2) {
                zf = zf2;
                Log.e(TAG, "PackageManager NameNotFoundException!");
                if (zf != null) {
                }
                if (inputStream != null) {
                }
                return apkDigest;
            } catch (NoSuchAlgorithmException e3) {
                zf = zf2;
                Log.e(TAG, "SHA1 NoSuchAlgorithmException!");
                if (zf != null) {
                }
                if (inputStream != null) {
                }
                return apkDigest;
            } catch (FileNotFoundException e4) {
                zf = zf2;
                Log.e(TAG, "sourceDir file notfound!");
                if (zf != null) {
                }
                if (inputStream != null) {
                }
                return apkDigest;
            } catch (IOException e5) {
                zf = zf2;
                try {
                    Log.e(TAG, "InputStream ZipFile failure!");
                    if (zf != null) {
                    }
                    if (inputStream != null) {
                    }
                    return apkDigest;
                } catch (Throwable th2) {
                    th = th2;
                    if (zf != null) {
                    }
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zf = zf2;
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "InputStream close failure!");
                        throw th;
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (NameNotFoundException e7) {
            Log.e(TAG, "PackageManager NameNotFoundException!");
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e8) {
                    Log.e(TAG, "InputStream close failure!");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return apkDigest;
        } catch (NoSuchAlgorithmException e9) {
            Log.e(TAG, "SHA1 NoSuchAlgorithmException!");
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e10) {
                    Log.e(TAG, "InputStream close failure!");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return apkDigest;
        } catch (FileNotFoundException e11) {
            Log.e(TAG, "sourceDir file notfound!");
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e12) {
                    Log.e(TAG, "InputStream close failure!");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return apkDigest;
        } catch (IOException e13) {
            Log.e(TAG, "InputStream ZipFile failure!");
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e14) {
                    Log.e(TAG, "InputStream close failure!");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return apkDigest;
        }
        return apkDigest;
    }

    public String calculateCertHash(String apkName) {
        try {
            Signature[] signatures = this.mContext.getPackageManager().getPackageInfo(apkName, 64).signatures;
            MessageDigest msgDigest = MessageDigest.getInstance(HwSecDiagnoseConstant.MALAPP_APK_SHA256);
            if (signatures.length == 1) {
                return byteArray2Hex(msgDigest.digest(signatures[0].toByteArray()));
            }
            List<String> sigList = new ArrayList();
            for (Signature toByteArray : signatures) {
                msgDigest.reset();
                sigList.add(byteArray2Hex(msgDigest.digest(toByteArray.toByteArray())));
            }
            Collections.sort(sigList);
            StringBuffer mergeBuffer = new StringBuffer();
            int sigListSize = sigList.size();
            for (int k = 0; k < sigListSize; k++) {
                mergeBuffer.append((String) sigList.get(k));
            }
            String mergeString = mergeBuffer.toString();
            msgDigest.reset();
            return byteArray2Hex(msgDigest.digest(mergeString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getBytes UnsupportedEncodingException!");
            return null;
        } catch (NameNotFoundException e2) {
            Log.e(TAG, "PackageManager NameNotFoundException!");
            return null;
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "SHA1 NoSuchAlgorithmException!");
            return null;
        } catch (IOException e4) {
            Log.e(TAG, "IOException !");
            return null;
        }
    }

    public JSONObject getApkInfo(int UID, String PATCHID) {
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(UID);
        JSONObject pakjson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < pkgs.length; i++) {
            String apkDigest = calculateApkHash(pkgs[i]);
            String certDigest = calculateCertHash(pkgs[i]);
            if (!(apkDigest == null || certDigest == null)) {
                try {
                    JSONObject infojson = new JSONObject();
                    infojson.put(HwSecDiagnoseConstant.MALAPP_APK_NAME, pkgs[i]);
                    infojson.put(HwSecDiagnoseConstant.MALAPP_APK_HASH, apkDigest);
                    infojson.put(HwSecDiagnoseConstant.MALAPP_APK_CERT, certDigest);
                    infojson.put(HwSecDiagnoseConstant.MALAPP_PATCH_ID, PATCHID);
                    jsonArray.put(infojson);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON put JSONException!");
                }
            }
        }
        try {
            pakjson.put(HwSecDiagnoseConstant.MALAPP_APK_PACKAGES, jsonArray);
        } catch (JSONException e2) {
            Log.e(TAG, "pakjson put JSONException!");
        }
        return pakjson;
    }

    public static String byteArray2Hex(byte[] byteArray) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] resultCharArray = new char[(byteArray.length * 2)];
        int index = 0;
        for (byte b : byteArray) {
            int i = index + 1;
            resultCharArray[index] = hexDigits[(b >>> 4) & 15];
            index = i + 1;
            resultCharArray[i] = hexDigits[b & 15];
        }
        return new String(resultCharArray);
    }
}
