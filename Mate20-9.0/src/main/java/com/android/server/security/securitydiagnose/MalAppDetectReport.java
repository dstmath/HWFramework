package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UEventObserver;
import android.util.IMonitor;
import android.util.Log;
import com.android.server.display.HwUibcReceiver;
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
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String ROOT_STATE_MATCH = "DEVPATH=/kernel/oases_attack";
    private static final String TAG = "MalAppDetectReport";
    private static MalAppDetectReport mInstance;
    private Context mContext;
    /* access modifiers changed from: private */
    public UEventHandler mHandler;
    private HandlerThread mHandlerThread;
    private final UEventObserver mUEventObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
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
            if (msg.what == 2000) {
                MalAppDetectReport.this.reportData((UEventObserver.UEvent) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public void reportData(UEventObserver.UEvent event) {
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
                    IMonitor.EventStream eStream = IMonitor.openEventStream(EVT_MALAPP_REPORT_ID);
                    eStream.setParam(0, object.optString(HwSecDiagnoseConstant.MALAPP_APK_NAME));
                    eStream.setParam(1, object.optString(HwSecDiagnoseConstant.MALAPP_APK_HASH));
                    eStream.setParam(2, object.optString(HwSecDiagnoseConstant.MALAPP_APK_CERT));
                    eStream.setParam(3, object.optString(HwSecDiagnoseConstant.MALAPP_PATCH_ID));
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

    public String calculateApkHash(String apkName) {
        InputStream manifest = null;
        ZipFile zf = null;
        String apkDigest = null;
        boolean flag = false;
        try {
            byte[] buffer = new byte[8192];
            MessageDigest msgDigest = MessageDigest.getInstance(HwSecDiagnoseConstant.MALAPP_APK_SHA256);
            ZipFile zf2 = new ZipFile(this.mContext.getPackageManager().getApplicationInfo(apkName, 0).sourceDir);
            InputStream manifest2 = zf2.getInputStream(zf2.getEntry("META-INF/MANIFEST.MF"));
            while (true) {
                int read = manifest2.read(buffer);
                int numRead = read;
                if (read == -1) {
                    break;
                }
                msgDigest.update(buffer, 0, numRead);
                flag = true;
            }
            if (flag) {
                apkDigest = byteArray2Hex(msgDigest.digest());
            }
            try {
                zf2.close();
                if (manifest2 != null) {
                    manifest2.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "InputStream close failure!");
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "PackageManager NameNotFoundException!");
            if (zf != null) {
                zf.close();
            }
            if (manifest != null) {
                manifest.close();
            }
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "SHA1 NoSuchAlgorithmException!");
            if (zf != null) {
                zf.close();
            }
            if (manifest != null) {
                manifest.close();
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "sourceDir file notfound!");
            if (zf != null) {
                zf.close();
            }
            if (manifest != null) {
                manifest.close();
            }
        } catch (IOException e5) {
            Log.e(TAG, "InputStream ZipFile failure!");
            if (zf != null) {
                zf.close();
            }
            if (manifest != null) {
                manifest.close();
            }
        } catch (Throwable th) {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e6) {
                    Log.e(TAG, "InputStream close failure!");
                    throw th;
                }
            }
            if (manifest != null) {
                manifest.close();
            }
            throw th;
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
            List<String> sigList = new ArrayList<>();
            for (Signature byteArray : signatures) {
                msgDigest.reset();
                sigList.add(byteArray2Hex(msgDigest.digest(byteArray.toByteArray())));
            }
            Collections.sort(sigList);
            StringBuffer mergeBuffer = new StringBuffer();
            int sigListSize = sigList.size();
            for (int k = 0; k < sigListSize; k++) {
                mergeBuffer.append(sigList.get(k));
            }
            String mergeString = mergeBuffer.toString();
            msgDigest.reset();
            return byteArray2Hex(msgDigest.digest(mergeString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getBytes UnsupportedEncodingException!");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "PackageManager NameNotFoundException!");
            return null;
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "SHA1 NoSuchAlgorithmException!");
            return null;
        }
    }

    public JSONObject getApkInfo(int UID, String PATCHID) {
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(UID);
        JSONObject pakjson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (pkgs == null) {
            try {
                JSONObject infojson = new JSONObject();
                infojson.put(HwSecDiagnoseConstant.MALAPP_APK_NAME, "uid = " + UID);
                infojson.put(HwSecDiagnoseConstant.MALAPP_APK_HASH, "");
                infojson.put(HwSecDiagnoseConstant.MALAPP_APK_CERT, "");
                infojson.put(HwSecDiagnoseConstant.MALAPP_PATCH_ID, PATCHID);
                jsonArray.put(infojson);
            } catch (JSONException e) {
                Log.e(TAG, "JSON put JSONException!");
            }
        } else {
            for (int i = 0; i < pkgs.length; i++) {
                String apkDigest = calculateApkHash(pkgs[i]);
                String certDigest = calculateCertHash(pkgs[i]);
                if (!(apkDigest == null || certDigest == null)) {
                    try {
                        JSONObject infojson2 = new JSONObject();
                        infojson2.put(HwSecDiagnoseConstant.MALAPP_APK_NAME, pkgs[i]);
                        infojson2.put(HwSecDiagnoseConstant.MALAPP_APK_HASH, apkDigest);
                        infojson2.put(HwSecDiagnoseConstant.MALAPP_APK_CERT, certDigest);
                        infojson2.put(HwSecDiagnoseConstant.MALAPP_PATCH_ID, PATCHID);
                        jsonArray.put(infojson2);
                    } catch (JSONException e2) {
                        Log.e(TAG, "JSON put JSONException!");
                    }
                }
            }
        }
        try {
            pakjson.put(HwSecDiagnoseConstant.MALAPP_APK_PACKAGES, jsonArray);
        } catch (JSONException e3) {
            Log.e(TAG, "pakjson put JSONException!");
        }
        return pakjson;
    }

    public static String byteArray2Hex(byte[] byteArray) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] resultCharArray = new char[(byteArray.length * 2)];
        int index = 0;
        for (byte b : byteArray) {
            int index2 = index + 1;
            resultCharArray[index] = hexDigits[(b >>> 4) & 15];
            index = index2 + 1;
            resultCharArray[index2] = hexDigits[b & HwUibcReceiver.CurrentPacket.INPUT_MASK];
        }
        return new String(resultCharArray);
    }
}
