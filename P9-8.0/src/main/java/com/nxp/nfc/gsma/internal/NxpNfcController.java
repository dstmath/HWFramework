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
    private NxpCallbacks mCallBack = null;
    private Context mContext;
    private boolean mDialogBoxFlag = false;
    private NfcAdapter mNfcAdapter = null;
    private INxpNfcController mNfcControllerService = null;
    private NxpNfcAdapter mNxpNfcAdapter = null;
    private final BroadcastReceiver mOwnerReceiver = new BroadcastReceiver() {
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
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
    private final HashMap<String, ApduServiceInfo> mSeNameApduService = new HashMap();
    private boolean mState = false;

    public interface Callbacks {
        void onGetOffHostService(boolean z, String str, String str2, int i, List<String> list, List<AidGroup> list2);
    }

    public interface NxpCallbacks {
        void onNxpEnableNfcController(boolean z);
    }

    public NxpNfcController(Context context) {
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
        this.mContext.registerReceiver(this.mReceiver, filter, NxpConstants.PERMISSIONS_NFC, null);
        Intent enableNfc = new Intent();
        enableNfc.setAction(NxpConstants.ACTION_GSMA_ENABLE_NFC);
        this.mContext.sendBroadcast(enableNfc, NxpConstants.PERMISSIONS_NFC);
    }

    private NxpOffHostService convertApduServiceToOffHostService(PackageManager pm, ApduServiceInfo apduService) {
        String sEname = null;
        ResolveInfo resolveInfo = apduService.getResolveInfo();
        String description = apduService.getDescription();
        int seId = apduService.getSEInfo().getSeId();
        if (2 == seId) {
            sEname = NxpConstants.UICC_ID;
        } else if (4 == seId) {
            sEname = "SIM2";
        } else if (1 == seId) {
            sEname = "eSE";
        } else {
            Log.e(TAG, "Wrong SE ID");
        }
        boolean modifiable = apduService.getModifiable();
        int bannerId = apduService.getBannerId();
        Drawable banner = apduService.loadBanner(pm);
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
        if (seName != null) {
            if (!seName.equals(NxpConstants.UICC_ID)) {
                if (!seName.equals("SIM1")) {
                    if (seName.equals("SIM2")) {
                        seId = 4;
                    } else {
                        if (seName.equals("eSE")) {
                            seId = 1;
                        } else {
                            Log.e(TAG, "wrong Se name");
                        }
                    }
                }
            }
            seId = 2;
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
            apduServices = this.mNfcControllerService.getOffHostServices(userId, packageName);
            if (apduServices == null || apduServices.isEmpty()) {
                return null;
            }
            for (ApduServiceInfo service : apduServices) {
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

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00d4  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0088 A:{Catch:{ RemoteException -> 0x010b }} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0096  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean commitOffHostService(String packageName, String seName, String description, int bannerResId, int uid, List<String> list, List<AidGroup> aidGroups) {
        ApduServiceInfo newService;
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
        String secureElement = null;
        if (!seName.equals(NxpConstants.UICC_ID)) {
            if (!seName.equals("SIM1")) {
                if (seName.equals("SIM2")) {
                    secureElement = NxpConstants.UICC2_ID;
                } else {
                    if (!seName.equals("eSE1")) {
                        if (!seName.equals("eSE")) {
                            Log.e(TAG, "wrong Se name");
                        }
                    }
                    secureElement = NxpConstants.SMART_MX_ID;
                }
                if (secureElement.equals(NxpConstants.UICC_ID)) {
                    if (secureElement.equals(NxpConstants.UICC2_ID)) {
                        seId = 4;
                    } else {
                        if (secureElement.equals(NxpConstants.SMART_MX_ID)) {
                            seId = 1;
                        } else {
                            if (secureElement.equals(NxpConstants.HOST_ID)) {
                                seId = 0;
                            } else {
                                Log.e(TAG, "wrong Se name");
                            }
                        }
                    }
                } else {
                    seId = 2;
                }
                newService = new ApduServiceInfo(resolveInfo, false, description, null, dynamicAidGroup, false, bannerResId, userId, packageName, new ESeInfo(seId, -1), null, null, true);
                this.mSeNameApduService.put(seName, newService);
                if (this.mNfcControllerService != null) {
                    result = this.mNfcControllerService.commitOffHostService(userId, packageName, seName, newService);
                }
                if (!result) {
                    return true;
                }
                Log.d(TAG, "GSMA: commitOffHostService Failed");
                return false;
            }
        }
        secureElement = NxpConstants.UICC_ID;
        if (secureElement.equals(NxpConstants.UICC_ID)) {
        }
        newService = new ApduServiceInfo(resolveInfo, false, description, null, dynamicAidGroup, false, bannerResId, userId, packageName, new ESeInfo(seId, -1), null, null, true);
        this.mSeNameApduService.put(seName, newService);
        try {
            if (this.mNfcControllerService != null) {
            }
            if (!result) {
            }
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
        try {
            List<ApduServiceInfo> apduServices = this.mNfcControllerService.getOffHostServices(UserHandle.myUserId(), packageName);
            for (int i = 0; i < apduServices.size(); i++) {
                String seName;
                if (i == apduServices.size() - 1) {
                    isLast = true;
                }
                int seId = ((ApduServiceInfo) apduServices.get(i)).getSEInfo().getSeId();
                if (2 == seId) {
                    seName = NxpConstants.UICC_ID;
                } else if (4 == seId) {
                    seName = "SIM2";
                } else if (1 == seId) {
                    seName = "eSE";
                } else {
                    seName = null;
                    Log.e(TAG, "Wrong SE ID");
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
            int seId = apduService.getSEInfo().getSeId();
            if (2 == seId) {
                seName = NxpConstants.UICC_ID;
            } else if (4 == seId) {
                seName = "SIM2";
            } else if (1 == seId) {
                seName = "eSE";
            } else {
                Log.e(TAG, "Wrong SE ID");
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

    public boolean isStaticOffhostService(int userId, String packageName, NxpOffHostService service) {
        boolean isStatic = false;
        List<ApduServiceInfo> apduServices = new ArrayList();
        try {
            apduServices = this.mNfcControllerService.getOffHostServices(userId, packageName);
            for (int i = 0; i < apduServices.size(); i++) {
                ApduServiceInfo sService = (ApduServiceInfo) apduServices.get(i);
                if (!sService.getModifiable() && service.getServiceName().compareTo(sService.getResolveInfo().serviceInfo.name) == 0) {
                    isStatic = true;
                }
            }
            return isStatic;
        } catch (RemoteException e) {
            Log.e(TAG, "getOffHostServices failed", e);
            return true;
        }
    }
}
