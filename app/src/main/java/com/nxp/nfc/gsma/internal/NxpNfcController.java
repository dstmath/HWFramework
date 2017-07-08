package com.nxp.nfc.gsma.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.ApduServiceInfo.ESeInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.nxp.nfc.NxpConstants;
import com.nxp.nfc.NxpNfcAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NxpNfcController {
    public static final int BATTERY_ALL_STATES = 2;
    public static final int BATTERY_OPERATIONAL_STATE = 1;
    private static final int MW_PROTOCOL_MASK_ISO_DEP = 8;
    public static final int PROTOCOL_ISO_DEP = 16;
    public static final int SCREEN_ALL_MODES = 2;
    public static final int SCREEN_ON_AND_LOCKED_MODE = 1;
    static final String TAG = "NxpNfcController";
    public static final int TECHNOLOGY_NFC_A = 1;
    public static final int TECHNOLOGY_NFC_B = 2;
    public static final int TECHNOLOGY_NFC_F = 4;
    private NxpCallbacks mCallBack;
    private Context mContext;
    private boolean mDialogBoxFlag;
    private NfcAdapter mNfcAdapter;
    private INxpNfcController mNfcControllerService;
    private NxpNfcAdapter mNxpNfcAdapter;
    private final BroadcastReceiver mOwnerReceiver;
    private final BroadcastReceiver mReceiver;
    private final HashMap<String, ApduServiceInfo> mSeNameApduService;
    private boolean mState;

    public interface Callbacks {
        void onGetOffHostService(boolean z, String str, String str2, int i, List<String> list, List<AidGroup> list2);
    }

    public interface NxpCallbacks {
        void onNxpEnableNfcController(boolean z);
    }

    public NxpNfcController() {
        this.mNfcAdapter = null;
        this.mNxpNfcAdapter = null;
        this.mNfcControllerService = null;
        this.mState = false;
        this.mDialogBoxFlag = false;
        this.mCallBack = null;
        this.mSeNameApduService = new HashMap();
        this.mOwnerReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int state = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 0);
                Log.d(NxpNfcController.TAG, "onReceive: action: " + action + "mState: " + NxpNfcController.this.mState);
                if (state == 3 && NxpNfcController.this.mState && NxpNfcController.this.mDialogBoxFlag) {
                    NxpNfcController.this.mCallBack.onNxpEnableNfcController(true);
                    NxpNfcController.this.mDialogBoxFlag = false;
                    NxpNfcController.this.mState = false;
                    NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                    NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NxpConstants.ACTION_GSMA_ENABLE_SET_FLAG) && intent.getExtras() != null) {
                    NxpNfcController.this.mState = intent.getExtras().getBoolean("ENABLE_STATE");
                }
                if (NxpNfcController.this.mState) {
                    NxpNfcController.this.mDialogBoxFlag = true;
                    return;
                }
                NxpNfcController.this.mCallBack.onNxpEnableNfcController(false);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
            }
        };
    }

    public NxpNfcController(Context context) {
        this.mNfcAdapter = null;
        this.mNxpNfcAdapter = null;
        this.mNfcControllerService = null;
        this.mState = false;
        this.mDialogBoxFlag = false;
        this.mCallBack = null;
        this.mSeNameApduService = new HashMap();
        this.mOwnerReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int state = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 0);
                Log.d(NxpNfcController.TAG, "onReceive: action: " + action + "mState: " + NxpNfcController.this.mState);
                if (state == 3 && NxpNfcController.this.mState && NxpNfcController.this.mDialogBoxFlag) {
                    NxpNfcController.this.mCallBack.onNxpEnableNfcController(true);
                    NxpNfcController.this.mDialogBoxFlag = false;
                    NxpNfcController.this.mState = false;
                    NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                    NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NxpConstants.ACTION_GSMA_ENABLE_SET_FLAG) && intent.getExtras() != null) {
                    NxpNfcController.this.mState = intent.getExtras().getBoolean("ENABLE_STATE");
                }
                if (NxpNfcController.this.mState) {
                    NxpNfcController.this.mDialogBoxFlag = true;
                    return;
                }
                NxpNfcController.this.mCallBack.onNxpEnableNfcController(false);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
            }
        };
        this.mContext = context;
        this.mNfcAdapter = NfcAdapter.getNfcAdapter(this.mContext);
        if (this.mNfcAdapter != null) {
            this.mNxpNfcAdapter = NxpNfcAdapter.getNxpNfcAdapter(this.mNfcAdapter);
        }
        if (this.mNxpNfcAdapter != null) {
            this.mNfcControllerService = this.mNxpNfcAdapter.getNxpNfcControllerInterface();
        }
    }

    public boolean isNxpNfcEnabled() {
        return this.mNfcAdapter.isEnabled();
    }

    public void enableNxpNfcController(NxpCallbacks cb) {
        this.mCallBack = cb;
        IntentFilter ownerFilter = new IntentFilter();
        ownerFilter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mOwnerReceiver, ownerFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NxpConstants.ACTION_GSMA_ENABLE_SET_FLAG);
        this.mContext.registerReceiver(this.mReceiver, filter, "android.permission.NFC", null);
        Intent enableNfc = new Intent();
        enableNfc.setAction(NxpConstants.ACTION_GSMA_ENABLE_NFC);
        this.mContext.sendBroadcast(enableNfc, "android.permission.NFC");
    }

    public boolean isNxpHostCardEmulationSupported() {
        return this.mNxpNfcAdapter.isHostCardEmulationSupported();
    }

    private NxpOffHostService convertApduServiceToOffHostService(PackageManager pm, ApduServiceInfo apduService) {
        ESeInfo mEseInfo = apduService.getSEInfo();
        ResolveInfo resolveInfo = apduService.getResolveInfo();
        String description = apduService.getDescription();
        String sEname = NxpConstants.HOST_ID;
        if (mEseInfo.getSeId() == TECHNOLOGY_NFC_B) {
            sEname = NxpConstants.UICC_ID;
        } else if (mEseInfo.getSeId() == TECHNOLOGY_NFC_A) {
            sEname = NxpConstants.SMART_MX_ID;
        } else if (mEseInfo.getSeId() == 0) {
            sEname = NxpConstants.HOST_ID;
        } else {
            Log.e(TAG, "wrong Se Id");
        }
        Drawable banner = apduService.loadBanner(pm);
        boolean modifiable = apduService.getModifiable();
        int bannerId = apduService.getBannerId();
        int userId = apduService.getUid();
        Log.d(TAG, "convertApduServiceToOffHostService begin,modifiable=" + modifiable);
        NxpOffHostService mService = new NxpOffHostService(userId, description, sEname, resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name, modifiable);
        if (modifiable) {
            for (AidGroup group : apduService.getDynamicAidGroups()) {
                mService.mAidGroupList.add(group);
            }
        } else {
            for (AidGroup group2 : apduService.getStaticAidGroups()) {
                mService.mAidGroupList.add(group2);
            }
        }
        mService.setBanner(banner);
        mService.setContext(this.mContext);
        mService.setBannerId(bannerId);
        mService.setNxpNfcController(this);
        return mService;
    }

    private ApduServiceInfo convertOffhostServiceToApduService(NxpOffHostService mService, int userId, String pkg) {
        String description = mService.getDescription();
        boolean modifiable = mService.getModifiable();
        ArrayList<AidGroup> dynamicAidGroup = new ArrayList();
        dynamicAidGroup.addAll(mService.mAidGroupList);
        Drawable DrawableResource = mService.getBanner();
        int seId = 0;
        String seName = mService.getLocation();
        int bannerId = mService.mBannerId;
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.applicationInfo = new ApplicationInfo();
        resolveInfo.serviceInfo.packageName = pkg;
        resolveInfo.serviceInfo.name = mService.getServiceName();
        if (seName.equals(NxpConstants.UICC_ID)) {
            seId = TECHNOLOGY_NFC_B;
        } else {
            if (seName.equals(NxpConstants.SMART_MX_ID)) {
                seId = TECHNOLOGY_NFC_A;
            } else {
                if (seName.equals(NxpConstants.HOST_ID)) {
                    seId = 0;
                } else {
                    Log.e(TAG, "wrong Se name");
                }
            }
        }
        return new ApduServiceInfo(resolveInfo, false, description, null, dynamicAidGroup, false, bannerId, userId, pkg, new ESeInfo(seId, -1), null, DrawableResource, modifiable);
    }

    public boolean deleteOffHostService(int userId, String packageName, NxpOffHostService service) {
        boolean result = false;
        try {
            result = this.mNfcControllerService.deleteOffHostService(userId, packageName, convertOffhostServiceToApduService(service, userId, packageName));
        } catch (RemoteException e) {
            Log.e(TAG, "Exception:deleteOffHostService failed", e);
        }
        if (result) {
            return true;
        }
        Log.d(TAG, "GSMA: deleteOffHostService failed");
        return false;
    }

    public ArrayList<NxpOffHostService> getOffHostServices(int userId, String packageName) {
        Log.d(TAG, "getOffHostServices enter");
        List<ApduServiceInfo> apduServices = new ArrayList();
        ArrayList<NxpOffHostService> mService = new ArrayList();
        PackageManager pm = this.mContext.getPackageManager();
        try {
            for (ApduServiceInfo service : this.mNfcControllerService.getOffHostServices(userId, packageName)) {
                mService.add(convertApduServiceToOffHostService(pm, service));
            }
            return mService;
        } catch (RemoteException e) {
            Log.e(TAG, "getOffHostServices failed", e);
            return null;
        }
    }

    public NxpOffHostService getDefaultOffHostService(int userId, String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            Log.d(TAG, "getDefaultOffHostService packageName" + packageName);
            ApduServiceInfo apduService = this.mNfcControllerService.getDefaultOffHostService(userId, packageName);
            if (apduService != null) {
                return convertApduServiceToOffHostService(pm, apduService);
            }
            Log.d(TAG, "getDefaultOffHostService: Service is NULL");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "getDefaultOffHostService failed", e);
            return null;
        }
    }

    public boolean commitOffHostService(int userId, String packageName, NxpOffHostService service) {
        boolean result = false;
        String serviceName = service.getServiceName();
        ApduServiceInfo newService = convertOffhostServiceToApduService(service, userId, packageName);
        try {
            if (this.mNfcControllerService != null) {
                result = this.mNfcControllerService.commitOffHostService(userId, packageName, serviceName, newService);
            }
            if (result) {
                return true;
            }
            Log.d(TAG, "GSMA: commitOffHostService Failed");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Exception:commitOffHostService failed", e);
            return false;
        }
    }

    public boolean commitOffHostService(String packageName, String seName, String description, int bannerResId, int uid, List<String> list, List<AidGroup> aidGroups) {
        String secureElement;
        boolean result = false;
        int userId = UserHandle.myUserId();
        ArrayList<AidGroup> dynamicAidGroup = new ArrayList();
        dynamicAidGroup.addAll(aidGroups);
        int seId = 0;
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.applicationInfo = new ApplicationInfo();
        resolveInfo.serviceInfo.packageName = packageName;
        resolveInfo.serviceInfo.name = seName;
        if (seName.equals(NxpConstants.UICC_ID)) {
            secureElement = NxpConstants.UICC_ID;
        } else {
            if (seName.equals("eSE")) {
                secureElement = NxpConstants.SMART_MX_ID;
            } else {
                secureElement = NxpConstants.HOST_ID;
            }
        }
        if (secureElement.equals(NxpConstants.UICC_ID)) {
            seId = TECHNOLOGY_NFC_B;
        } else {
            if (secureElement.equals(NxpConstants.SMART_MX_ID)) {
                seId = TECHNOLOGY_NFC_A;
            } else {
                if (secureElement.equals(NxpConstants.HOST_ID)) {
                    seId = 0;
                } else {
                    Log.e(TAG, "wrong Se name");
                }
            }
        }
        ApduServiceInfo newService = new ApduServiceInfo(resolveInfo, false, description, null, dynamicAidGroup, false, bannerResId, userId, packageName, new ESeInfo(seId, -1), null, null, true);
        this.mSeNameApduService.put(seName, newService);
        try {
            if (this.mNfcControllerService != null) {
                result = this.mNfcControllerService.commitOffHostService(userId, packageName, seName, newService);
            }
            if (result) {
                return true;
            }
            Log.d(TAG, "GSMA: commitOffHostService Failed");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Exception:commitOffHostService failed", e);
            return false;
        }
    }

    public boolean deleteOffHostService(String packageName, String seName) {
        boolean result = false;
        try {
            result = this.mNfcControllerService.deleteOffHostService(UserHandle.myUserId(), packageName, (ApduServiceInfo) this.mSeNameApduService.get(seName));
        } catch (RemoteException e) {
            Log.e(TAG, "Exception:deleteOffHostService failed", e);
        }
        if (result) {
            return true;
        }
        Log.d(TAG, "GSMA: deleteOffHostService failed");
        return false;
    }

    public boolean getOffHostServices(String packageName, Callbacks callbacks) {
        boolean isLast = false;
        String seName = null;
        try {
            List<ApduServiceInfo> apduServices = this.mNfcControllerService.getOffHostServices(UserHandle.myUserId(), packageName);
            for (int i = 0; i < apduServices.size(); i += TECHNOLOGY_NFC_A) {
                if (i == apduServices.size() - 1) {
                    isLast = true;
                }
                if (TECHNOLOGY_NFC_B == ((ApduServiceInfo) apduServices.get(i)).getSEInfo().getSeId()) {
                    seName = NxpConstants.UICC_ID;
                }
                Log.d(TAG, "getOffHostServices: seName = " + seName);
                ArrayList<String> groupDescription = new ArrayList();
                for (AidGroup aidGroup : ((ApduServiceInfo) apduServices.get(i)).getAidGroups()) {
                    groupDescription.add(aidGroup.getDescription());
                }
                callbacks.onGetOffHostService(isLast, ((ApduServiceInfo) apduServices.get(i)).getDescription(), seName, ((ApduServiceInfo) apduServices.get(i)).getBannerId(), groupDescription, ((ApduServiceInfo) apduServices.get(i)).getAidGroups());
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "getOffHostServices failed", e);
            return false;
        }
    }

    public boolean getDefaultOffHostService(String packageName, Callbacks callbacks) {
        Log.d(TAG, "getDefaultOffHostService: Enter");
        String seName = null;
        try {
            ApduServiceInfo apduService = this.mNfcControllerService.getDefaultOffHostService(UserHandle.myUserId(), packageName);
            if (apduService == null) {
                Log.w(TAG, "apduService is null, return false");
                return false;
            }
            if (TECHNOLOGY_NFC_B == apduService.getSEInfo().getSeId()) {
                seName = NxpConstants.UICC_ID;
            }
            Log.d(TAG, "getDefaultOffHostService: seName = " + seName);
            ArrayList<String> groupDescription = new ArrayList();
            for (AidGroup aidGroup : apduService.getAidGroups()) {
                groupDescription.add(aidGroup.getDescription());
            }
            callbacks.onGetOffHostService(true, apduService.getDescription(), seName, apduService.getBannerId(), groupDescription, apduService.getAidGroups());
            Log.d(TAG, "getDefaultOffHostService: End");
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "getDefaultOffHostService failed", e);
            return false;
        }
    }

    public void enableMultiReception(String seName, String packageName) {
        try {
            this.mNfcControllerService.enableMultiReception(packageName, seName);
        } catch (RemoteException e) {
            Log.e(TAG, "enableMultiReception failed", e);
        }
    }
}
