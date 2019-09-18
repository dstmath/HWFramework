package com.nxp.nfc.gsma.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.NxpAidGroup;
import android.nfc.cardemulation.NxpApduServiceInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.nxp.nfc.NxpConstants;
import com.nxp.nfc.NxpNfcAdapter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public NxpCallbacks mCallBack = null;
    Context mContext;
    /* access modifiers changed from: private */
    public boolean mDialogBoxFlag = false;
    private NfcAdapter mNfcAdapter = null;
    private INxpNfcController mNfcControllerService = null;
    private NxpNfcAdapter mNxpNfcAdapter = null;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mOwnerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 0);
            Log.d(NxpNfcController.TAG, "onReceive: action: " + action + "mState: " + NxpNfcController.this.mState);
            if (state == 3 && NxpNfcController.this.mState && NxpNfcController.this.mDialogBoxFlag) {
                NxpNfcController.this.mCallBack.onNxpEnableNfcController(true);
                boolean unused = NxpNfcController.this.mDialogBoxFlag = false;
                boolean unused2 = NxpNfcController.this.mState = false;
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
            }
        }
    };
    /* access modifiers changed from: private */
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NxpConstants.ACTION_GSMA_ENABLE_SET_FLAG) && intent.getExtras() != null) {
                boolean unused = NxpNfcController.this.mState = intent.getExtras().getBoolean("ENABLE_STATE");
            }
            if (!NxpNfcController.this.mState) {
                NxpNfcController.this.mCallBack.onNxpEnableNfcController(false);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mOwnerReceiver);
                NxpNfcController.this.mContext.unregisterReceiver(NxpNfcController.this.mReceiver);
                return;
            }
            boolean unused2 = NxpNfcController.this.mDialogBoxFlag = true;
        }
    };
    private final HashMap<String, NxpApduServiceInfo> mSeNameApduService = new HashMap<>();
    /* access modifiers changed from: private */
    public boolean mState = false;

    public interface Callbacks {
        void onGetOffHostService(boolean z, String str, String str2, int i, List<String> list, List<AidGroup> list2);
    }

    public interface NxpCallbacks {
        void onNxpEnableNfcController(boolean z);
    }

    public NxpNfcController() {
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

    private NxpOffHostService convertApduServiceToOffHostService(PackageManager pm, NxpApduServiceInfo apduService) {
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
        int i = seId;
        Drawable banner2 = banner;
        NxpOffHostService mService = new NxpOffHostService(userId, description, sEname, resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name, modifiable);
        if (modifiable) {
            Iterator<NxpAidGroup> it = apduService.getDynamicNxpAidGroups().iterator();
            while (it.hasNext()) {
                mService.mNxpAidGroupList.add(it.next());
            }
        } else {
            Iterator<NxpAidGroup> it2 = apduService.getStaticNxpAidGroups().iterator();
            while (it2.hasNext()) {
                mService.mNxpAidGroupList.add(it2.next());
            }
        }
        mService.setContext(this.mContext);
        mService.setBannerId(bannerId);
        mService.setBanner(banner2);
        mService.setNxpNfcController(this);
        return mService;
    }

    private NxpApduServiceInfo convertOffhostServiceToApduService(NxpOffHostService mService, int userId, String pkg) {
        NxpOffHostService nxpOffHostService = mService;
        String description = mService.getDescription();
        boolean modifiable = mService.getModifiable();
        ArrayList<NxpAidGroup> dynamicNxpAidGroup = new ArrayList<>();
        dynamicNxpAidGroup.addAll(nxpOffHostService.mNxpAidGroupList);
        Drawable banner = mService.getBanner();
        byte[] byteArrayBanner = null;
        if (banner != null) {
            Bitmap bitmap = ((BitmapDrawable) banner).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArrayBanner = stream.toByteArray();
        }
        byte[] byteArrayBanner2 = byteArrayBanner;
        int seId = 0;
        String seName = mService.getLocation();
        int bannerId = nxpOffHostService.mBannerId;
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.applicationInfo = new ApplicationInfo();
        resolveInfo.serviceInfo.packageName = pkg;
        resolveInfo.serviceInfo.name = mService.getServiceName();
        if (seName != null) {
            if (seName.equals("SIM") || seName.equals(NxpConstants.UICC_ID)) {
                seId = 2;
            } else if (seName.equals("SIM2")) {
                seId = 4;
            } else if (seName.equals("eSE")) {
                seId = 1;
            } else {
                Log.e(TAG, "wrong Se name");
            }
        }
        int seId2 = seId;
        int i = seId2;
        ResolveInfo resolveInfo2 = resolveInfo;
        int i2 = bannerId;
        String str = seName;
        ArrayList<NxpAidGroup> arrayList = dynamicNxpAidGroup;
        NxpApduServiceInfo apduService = new NxpApduServiceInfo(resolveInfo, false, description, null, dynamicNxpAidGroup, false, bannerId, userId, pkg, new NxpApduServiceInfo.ESeInfo(seId2, -1), null, byteArrayBanner2, modifiable);
        return apduService;
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
        new ArrayList();
        ArrayList<NxpOffHostService> mService = new ArrayList<>();
        PackageManager pm = this.mContext.getPackageManager();
        try {
            List<NxpApduServiceInfo> apduServices = this.mNfcControllerService.getOffHostServices(userId, packageName);
            if (apduServices == null || apduServices.isEmpty()) {
                return null;
            }
            for (NxpApduServiceInfo service : apduServices) {
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
            NxpApduServiceInfo apduService = this.mNfcControllerService.getDefaultOffHostService(userId, packageName);
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
        NxpApduServiceInfo newService = convertOffhostServiceToApduService(service, userId, packageName);
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

    public boolean commitOffHostService(String packageName, String seName, String description, int bannerResId, int uid, List<String> list, List<NxpAidGroup> nxpAidGroups) {
        boolean result;
        String str = packageName;
        String str2 = seName;
        int userId = UserHandle.myUserId();
        ArrayList<NxpAidGroup> dynamicNxpAidGroup = new ArrayList<>();
        dynamicNxpAidGroup.addAll(nxpAidGroups);
        int seId = 0;
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.applicationInfo = new ApplicationInfo();
        resolveInfo.serviceInfo.packageName = str;
        resolveInfo.serviceInfo.name = str2;
        String secureElement = null;
        if (str2.equals("SIM") || str2.equals(NxpConstants.UICC_ID)) {
            secureElement = NxpConstants.UICC_ID;
        } else if (str2.equals("SIM2")) {
            secureElement = NxpConstants.UICC2_ID;
        } else if (str2.equals("eSE1") || str2.equals("eSE")) {
            secureElement = NxpConstants.SMART_MX_ID;
        } else {
            Log.e(TAG, "wrong Se name");
        }
        String secureElement2 = secureElement;
        if (secureElement2.equals(NxpConstants.UICC_ID)) {
            seId = 2;
        } else if (secureElement2.equals(NxpConstants.UICC2_ID)) {
            seId = 4;
        } else if (secureElement2.equals(NxpConstants.SMART_MX_ID)) {
            seId = 1;
        } else if (secureElement2.equals(NxpConstants.HOST_ID)) {
            seId = 0;
        } else {
            Log.e(TAG, "wrong Se name");
        }
        int seId2 = seId;
        int i = seId2;
        String str3 = secureElement2;
        ResolveInfo resolveInfo2 = resolveInfo;
        ArrayList<NxpAidGroup> arrayList = dynamicNxpAidGroup;
        int userId2 = userId;
        NxpApduServiceInfo newService = new NxpApduServiceInfo(resolveInfo, false, description, null, dynamicNxpAidGroup, false, bannerResId, userId, str, new NxpApduServiceInfo.ESeInfo(seId2, -1), null, null, true);
        String str4 = seName;
        this.mSeNameApduService.put(str4, newService);
        try {
            if (this.mNfcControllerService != null) {
                try {
                    result = this.mNfcControllerService.commitOffHostService(userId2, packageName, str4, newService);
                } catch (RemoteException e) {
                    e = e;
                    Log.e(TAG, "Exception:commitOffHostService failed", e);
                    return false;
                }
            } else {
                String str5 = packageName;
                result = false;
            }
            if (result) {
                return true;
            }
            Log.d(TAG, "GSMA: commitOffHostService Failed");
            return false;
        } catch (RemoteException e2) {
            e = e2;
            int i2 = userId2;
            String str6 = packageName;
            Log.e(TAG, "Exception:commitOffHostService failed", e);
            return false;
        }
    }

    public boolean deleteOffHostService(String packageName, String seName) {
        boolean result = false;
        try {
            result = this.mNfcControllerService.deleteOffHostService(UserHandle.myUserId(), packageName, this.mSeNameApduService.get(seName));
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
        String seName;
        boolean isLast = false;
        try {
            try {
                List<NxpApduServiceInfo> apduServices = this.mNfcControllerService.getOffHostServices(UserHandle.myUserId(), packageName);
                for (int i = 0; i < apduServices.size(); i++) {
                    if (i == apduServices.size() - 1) {
                        isLast = true;
                    }
                    int seId = apduServices.get(i).getSEInfo().getSeId();
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
                    ArrayList<String> groupDescription = new ArrayList<>();
                    Iterator<NxpAidGroup> it = apduServices.get(i).getNxpAidGroups().iterator();
                    while (it.hasNext()) {
                        groupDescription.add(it.next().getDescription());
                    }
                    callbacks.onGetOffHostService(isLast, apduServices.get(i).getDescription(), seName, apduServices.get(i).getBannerId(), groupDescription, apduServices.get(i).getAidGroups());
                }
                return true;
            } catch (RemoteException e) {
                e = e;
                Log.e(TAG, "getOffHostServices failed", e);
                return false;
            }
        } catch (RemoteException e2) {
            e = e2;
            String str = packageName;
            Log.e(TAG, "getOffHostServices failed", e);
            return false;
        }
    }

    public boolean getDefaultOffHostService(String packageName, Callbacks callbacks) {
        Log.d(TAG, "getDefaultOffHostService: Enter");
        String seName = null;
        try {
            try {
                NxpApduServiceInfo apduService = this.mNfcControllerService.getDefaultOffHostService(UserHandle.myUserId(), packageName);
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
                    try {
                        Log.e(TAG, "Wrong SE ID");
                    } catch (RemoteException e) {
                        e = e;
                    }
                }
                String seName2 = seName;
                try {
                    Log.d(TAG, "getDefaultOffHostService: seName = " + seName2);
                    ArrayList<String> groupDescription = new ArrayList<>();
                    Iterator<NxpAidGroup> it = apduService.getNxpAidGroups().iterator();
                    while (it.hasNext()) {
                        groupDescription.add(it.next().getDescription());
                    }
                    ArrayList<String> arrayList = groupDescription;
                    callbacks.onGetOffHostService(true, apduService.getDescription(), seName2, apduService.getBannerId(), groupDescription, apduService.getAidGroups());
                    Log.d(TAG, "getDefaultOffHostService: End");
                    return true;
                } catch (RemoteException e2) {
                    e = e2;
                    Log.e(TAG, "getDefaultOffHostService failed", e);
                    return false;
                }
            } catch (RemoteException e3) {
                e = e3;
                Log.e(TAG, "getDefaultOffHostService failed", e);
                return false;
            }
        } catch (RemoteException e4) {
            e = e4;
            String str = packageName;
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
        new ArrayList();
        try {
            List<NxpApduServiceInfo> nxpApduServices = this.mNfcControllerService.getOffHostServices(userId, packageName);
            for (int i = 0; i < nxpApduServices.size(); i++) {
                NxpApduServiceInfo sService = nxpApduServices.get(i);
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
