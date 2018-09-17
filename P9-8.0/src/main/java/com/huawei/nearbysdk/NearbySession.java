package com.huawei.nearbysdk;

import android.content.Context;
import com.huawei.nearbysdk.NearbyAdapter.NAdapterGetCallback;
import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NearbySession {
    public static final int DEFAULT_TIME_OUT_MS = 5000;
    public static final int SESSION_NONE_BUSINESSID = -1;
    static final String TAG = "NearbySession";
    private static int mBusinessId;
    private static HashMap<Long, IAuthGetCallback> mMapAuthIDAndCB = new HashMap();
    private static HashMap<Long, NearbyDevice> mMapAuthIDDevice = new HashMap();
    private static NearbyAdapter mNearbyAdapter;
    private static List<NearbySession> mSessionList = new ArrayList();
    private boolean mAuthResult = true;
    private NearbyDevice mNearbyDevice;

    public interface IAuthGetCallback {
        void getAuthResult(boolean z);
    }

    public static boolean createNearbySession(Context context, final int businessId, final NearbyDevice device, final int timeoutMs) {
        mBusinessId = businessId;
        boolean result = false;
        if (getNearbySession(device) != null) {
            HwLog.e(TAG, "createNearbySession  find device in mSessionList.Need not create another.");
        } else {
            mSessionList.add(new NearbySession(device));
        }
        if (mNearbyAdapter != null) {
            HwLog.d(TAG, "open Session");
            result = mNearbyAdapter.open(BusinessTypeEnum.Token, businessId, device, timeoutMs);
        } else if (context == null) {
            HwLog.d(TAG, "createNearbySession faild.  context = null.");
            return false;
        } else {
            HwLog.d(TAG, "create NearbyAdapter.");
            NearbyAdapter.getNearbyAdapter(context, new NAdapterGetCallback() {
                public void onAdapterGet(NearbyAdapter adapter) {
                    NearbySession.mNearbyAdapter = adapter;
                    NearbySession.mNearbyAdapter.open(BusinessTypeEnum.Token, businessId, device, timeoutMs);
                }
            });
        }
        return result;
    }

    private NearbySession(NearbyDevice device) {
        HwLog.d(TAG, "new NearbySession");
        this.mNearbyDevice = device;
    }

    public int write(byte[] message) {
        return mNearbyAdapter.write(BusinessTypeEnum.Token, mBusinessId, this.mNearbyDevice, message);
    }

    public void close() {
        HwLog.d(TAG, "close");
        mNearbyAdapter.close(BusinessTypeEnum.Token, mBusinessId, this.mNearbyDevice);
        for (NearbySession session : mSessionList) {
            if (session.getNearbyDevice().equals(this.mNearbyDevice)) {
                HwLog.d(TAG, "delete session");
                mSessionList.remove(session);
                if (mNearbyAdapter != null) {
                    NearbyAdapter nearbyAdapter = mNearbyAdapter;
                    NearbyAdapter.removeSessionkey(this.mNearbyDevice.getSummary());
                }
                return;
            }
        }
    }

    public boolean getAuthResult() {
        return this.mAuthResult;
    }

    public NearbyDevice getNearbyDevice() {
        return this.mNearbyDevice;
    }

    public void setAuthResult(boolean result) {
        this.mAuthResult = result;
    }

    public static NearbySession getNearbySession(NearbyDevice device) {
        for (NearbySession session : mSessionList) {
            if (session.mNearbyDevice.equals(device)) {
                return session;
            }
        }
        return null;
    }

    public static List<NearbySession> getSessionList() {
        return mSessionList;
    }

    public boolean startAuth(IAuthGetCallback cb) {
        if (mNearbyAdapter == null) {
            return false;
        }
        mNearbyAdapter.registerAuthentification(mBusinessId, new AuthListener() {
            public void onAuthentificationResult(long authId, boolean result, byte[] sessionKey, NearbyDevice nearbyDevice) {
                IAuthGetCallback cb = (IAuthGetCallback) NearbySession.mMapAuthIDAndCB.get(Long.valueOf(authId));
                NearbySession.getNearbySession(nearbyDevice).setAuthResult(result);
                if (cb != null) {
                    HwLog.d(NearbySession.TAG, "onAuthentificationResult result = " + result);
                    cb.getAuthResult(result);
                }
                NearbySession.mMapAuthIDDevice.remove(Long.valueOf(authId));
                NearbySession.mMapAuthIDAndCB.remove(Long.valueOf(authId));
            }
        });
        long authId = mNearbyAdapter.startAuthentification(this.mNearbyDevice, 1);
        if (authId == -1) {
            return false;
        }
        mMapAuthIDAndCB.put(Long.valueOf(authId), cb);
        mMapAuthIDDevice.put(Long.valueOf(authId), this.mNearbyDevice);
        return true;
    }
}
