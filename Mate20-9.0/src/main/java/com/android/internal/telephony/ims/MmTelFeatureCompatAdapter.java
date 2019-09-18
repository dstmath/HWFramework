package com.android.internal.telephony.ims;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.util.Log;
import com.android.ims.ImsConfigListener;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MmTelFeatureCompatAdapter extends MmTelFeature {
    public static final String ACTION_IMS_INCOMING_CALL = "com.android.ims.IMS_INCOMING_CALL";
    public static final int FEATURE_DISABLED = 0;
    public static final int FEATURE_ENABLED = 1;
    public static final int FEATURE_TYPE_UNKNOWN = -1;
    public static final int FEATURE_TYPE_UT_OVER_LTE = 4;
    public static final int FEATURE_TYPE_UT_OVER_WIFI = 5;
    public static final int FEATURE_TYPE_VIDEO_OVER_LTE = 1;
    public static final int FEATURE_TYPE_VIDEO_OVER_WIFI = 3;
    public static final int FEATURE_TYPE_VOICE_OVER_LTE = 0;
    public static final int FEATURE_TYPE_VOICE_OVER_WIFI = 2;
    public static final int FEATURE_UNKNOWN = -1;
    private static final Map<Integer, Integer> REG_TECH_TO_NET_TYPE = new HashMap(2);
    private static final String TAG = "MmTelFeatureCompat";
    private static final int WAIT_TIMEOUT_MS = 2000;
    /* access modifiers changed from: private */
    public final MmTelInterfaceAdapter mCompatFeature;
    private final IImsRegistrationListener mListener = new IImsRegistrationListener.Stub() {
        public void registrationConnected() throws RemoteException {
        }

        public void registrationProgressing() throws RemoteException {
        }

        public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
        }

        public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
        }

        public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
            Log.i(MmTelFeatureCompatAdapter.TAG, "registrationDisconnected: resetting MMTEL capabilities.");
            MmTelFeatureCompatAdapter.this.notifyCapabilitiesStatusChanged(new MmTelFeature.MmTelCapabilities());
        }

        public void registrationResumed() throws RemoteException {
        }

        public void registrationSuspended() throws RemoteException {
        }

        public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
        }

        public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
            MmTelFeatureCompatAdapter.this.notifyCapabilitiesStatusChanged(MmTelFeatureCompatAdapter.this.convertCapabilities(enabledFeatures));
        }

        public void voiceMessageCountUpdate(int count) throws RemoteException {
            MmTelFeatureCompatAdapter.this.notifyVoiceMessageCountUpdate(count);
        }

        public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
        }

        public void registrationChangeFailed(int targetAccessTech, ImsReasonInfo imsReasonInfo) throws RemoteException {
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(MmTelFeatureCompatAdapter.TAG, "onReceive");
            if (intent.getAction().equals(MmTelFeatureCompatAdapter.ACTION_IMS_INCOMING_CALL)) {
                Log.i(MmTelFeatureCompatAdapter.TAG, "onReceive : incoming call intent.");
                try {
                    MmTelFeatureCompatAdapter.this.notifyIncomingCallSession(MmTelFeatureCompatAdapter.this.mCompatFeature.getPendingCallSession(MmTelFeatureCompatAdapter.this.mSessionId, intent.getStringExtra("android:imsCallID")), intent.getExtras());
                } catch (RemoteException e) {
                    Log.w(MmTelFeatureCompatAdapter.TAG, "onReceive: Couldn't get Incoming call session.");
                }
            }
        }
    };
    private ImsRegistrationCompatAdapter mRegCompatAdapter;
    /* access modifiers changed from: private */
    public int mSessionId = -1;

    private static class ConfigListener extends ImsConfigListener.Stub {
        private final int mCapability;
        private final CountDownLatch mLatch;
        private final int mTech;

        public ConfigListener(int capability, int tech, CountDownLatch latch) {
            this.mCapability = capability;
            this.mTech = tech;
            this.mLatch = latch;
        }

        public void onGetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
            if (feature == this.mCapability && network == this.mTech) {
                this.mLatch.countDown();
                getFeatureValueReceived(value);
                return;
            }
            Log.i(MmTelFeatureCompatAdapter.TAG, "onGetFeatureResponse: response different than requested: feature=" + feature + " and network=" + network);
        }

        public void onSetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
            if (feature == this.mCapability && network == this.mTech) {
                this.mLatch.countDown();
                setFeatureValueReceived(value);
                return;
            }
            Log.i(MmTelFeatureCompatAdapter.TAG, "onSetFeatureResponse: response different than requested: feature=" + feature + " and network=" + network);
        }

        public void onGetVideoQuality(int status, int quality) throws RemoteException {
        }

        public void onSetVideoQuality(int status) throws RemoteException {
        }

        public void getFeatureValueReceived(int value) {
        }

        public void setFeatureValueReceived(int value) {
        }
    }

    private class ImsRegistrationListenerBase extends IImsRegistrationListener.Stub {
        private ImsRegistrationListenerBase() {
        }

        public void registrationConnected() throws RemoteException {
        }

        public void registrationProgressing() throws RemoteException {
        }

        public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
        }

        public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
        }

        public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
        }

        public void registrationResumed() throws RemoteException {
        }

        public void registrationSuspended() throws RemoteException {
        }

        public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
        }

        public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
        }

        public void voiceMessageCountUpdate(int count) throws RemoteException {
        }

        public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
        }

        public void registrationChangeFailed(int targetAccessTech, ImsReasonInfo imsReasonInfo) throws RemoteException {
        }
    }

    static {
        REG_TECH_TO_NET_TYPE.put(0, 13);
        REG_TECH_TO_NET_TYPE.put(1, 18);
    }

    public MmTelFeatureCompatAdapter(Context context, int slotId, MmTelInterfaceAdapter compatFeature) {
        initialize(context, slotId);
        this.mCompatFeature = compatFeature;
    }

    public boolean queryCapabilityConfiguration(int capability, int radioTech) {
        int capConverted = convertCapability(capability, radioTech);
        CountDownLatch latch = new CountDownLatch(1);
        int[] returnValue = {-1};
        int regTech = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(radioTech), -1).intValue();
        try {
            IImsConfig configInterface = this.mCompatFeature.getConfigInterface();
            final int[] iArr = returnValue;
            AnonymousClass3 r1 = new ConfigListener(capConverted, regTech, latch) {
                public void getFeatureValueReceived(int value) {
                    iArr[0] = value;
                }
            };
            configInterface.getFeatureValue(capConverted, regTech, r1);
        } catch (RemoteException e) {
            Log.w(TAG, "queryCapabilityConfiguration");
        }
        try {
            latch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e2) {
            Log.w(TAG, "queryCapabilityConfiguration - error waiting: " + e2.getMessage());
        }
        if (returnValue[0] == 1) {
            return true;
        }
        return false;
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallbackProxy c) {
        int i;
        int i2;
        if (request != null) {
            try {
                IImsConfig imsConfig = this.mCompatFeature.getConfigInterface();
                Iterator it = request.getCapabilitiesToDisable().iterator();
                while (true) {
                    i = -1;
                    i2 = 1;
                    if (!it.hasNext()) {
                        break;
                    }
                    CapabilityChangeRequest.CapabilityPair cap = (CapabilityChangeRequest.CapabilityPair) it.next();
                    CountDownLatch latch = new CountDownLatch(1);
                    int capConverted = convertCapability(cap.getCapability(), cap.getRadioTech());
                    int radioTechConverted = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(cap.getRadioTech()), -1).intValue();
                    Log.i(TAG, "changeEnabledCapabilities - cap: " + capConverted + " radioTech: " + radioTechConverted + " disabled");
                    AnonymousClass4 r10 = r1;
                    final ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy = c;
                    int capConverted2 = capConverted;
                    final CapabilityChangeRequest.CapabilityPair capabilityPair = cap;
                    AnonymousClass4 r1 = new ConfigListener(capConverted, radioTechConverted, latch) {
                        public void setFeatureValueReceived(int value) {
                            if (value != 0) {
                                if (capabilityCallbackProxy != null) {
                                    capabilityCallbackProxy.onChangeCapabilityConfigurationError(capabilityPair.getCapability(), capabilityPair.getRadioTech(), -1);
                                } else {
                                    return;
                                }
                            }
                            Log.i(MmTelFeatureCompatAdapter.TAG, "changeEnabledCapabilities - setFeatureValueReceived with value " + value);
                        }
                    };
                    imsConfig.setFeatureValue(capConverted2, radioTechConverted, 0, r10);
                    latch.await(2000, TimeUnit.MILLISECONDS);
                }
                for (CapabilityChangeRequest.CapabilityPair cap2 : request.getCapabilitiesToEnable()) {
                    CountDownLatch latch2 = new CountDownLatch(i2);
                    int capConverted3 = convertCapability(cap2.getCapability(), cap2.getRadioTech());
                    int radioTechConverted2 = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(cap2.getRadioTech()), Integer.valueOf(i)).intValue();
                    Log.i(TAG, "changeEnabledCapabilities - cap: " + capConverted3 + " radioTech: " + radioTechConverted2 + " enabled");
                    AnonymousClass5 r12 = r1;
                    final ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy2 = c;
                    int radioTechConverted3 = radioTechConverted2;
                    final CapabilityChangeRequest.CapabilityPair capabilityPair2 = cap2;
                    AnonymousClass5 r13 = new ConfigListener(capConverted3, radioTechConverted2, latch2) {
                        public void setFeatureValueReceived(int value) {
                            if (value != 1) {
                                if (capabilityCallbackProxy2 != null) {
                                    capabilityCallbackProxy2.onChangeCapabilityConfigurationError(capabilityPair2.getCapability(), capabilityPair2.getRadioTech(), -1);
                                } else {
                                    return;
                                }
                            }
                            Log.i(MmTelFeatureCompatAdapter.TAG, "changeEnabledCapabilities - setFeatureValueReceived with value " + value);
                        }
                    };
                    imsConfig.setFeatureValue(capConverted3, radioTechConverted3, 1, r12);
                    latch2.await(2000, TimeUnit.MILLISECONDS);
                    i2 = 1;
                    i = -1;
                }
            } catch (RemoteException | InterruptedException e) {
                Log.w(TAG, "changeEnabledCapabilities: Error processing: " + e.getMessage());
            }
        }
    }

    public ImsCallProfile createCallProfile(int callSessionType, int callType) {
        try {
            return this.mCompatFeature.createCallProfile(this.mSessionId, callSessionType, callType);
        } catch (RemoteException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public IImsCallSession createCallSessionInterface(ImsCallProfile profile) throws RemoteException {
        return this.mCompatFeature.createCallSession(this.mSessionId, profile);
    }

    public IImsUt getUtInterface() throws RemoteException {
        return this.mCompatFeature.getUtInterface();
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        return this.mCompatFeature.getEcbmInterface();
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        return this.mCompatFeature.getMultiEndpointInterface();
    }

    public int getFeatureState() {
        try {
            return this.mCompatFeature.getFeatureState();
        } catch (RemoteException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setUiTtyMode(int mode, Message onCompleteMessage) {
        try {
            this.mCompatFeature.setUiTTYMode(mode, onCompleteMessage);
        } catch (RemoteException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void onFeatureRemoved() {
        this.mContext.unregisterReceiver(this.mReceiver);
        try {
            this.mCompatFeature.endSession(this.mSessionId);
            this.mCompatFeature.removeRegistrationListener(this.mListener);
            if (this.mRegCompatAdapter != null) {
                this.mCompatFeature.removeRegistrationListener(this.mRegCompatAdapter.getRegistrationListener());
            }
        } catch (RemoteException e) {
            Log.w(TAG, "onFeatureRemoved: Couldn't end session: " + e.getMessage());
        }
    }

    public void onFeatureReady() {
        Log.i(TAG, "onFeatureReady called!");
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(ACTION_IMS_INCOMING_CALL));
        try {
            this.mSessionId = this.mCompatFeature.startSession(createIncomingCallPendingIntent(), new ImsRegistrationListenerBase());
            this.mCompatFeature.addRegistrationListener(this.mListener);
            this.mCompatFeature.addRegistrationListener(this.mRegCompatAdapter.getRegistrationListener());
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't start compat feature: " + e.getMessage());
        }
    }

    public void enableIms() throws RemoteException {
        this.mCompatFeature.turnOnIms();
    }

    public void disableIms() throws RemoteException {
        this.mCompatFeature.turnOffIms();
    }

    public IImsConfig getOldConfigInterface() {
        try {
            return this.mCompatFeature.getConfigInterface();
        } catch (RemoteException e) {
            Log.w(TAG, "getOldConfigInterface(): " + e.getMessage());
            return null;
        }
    }

    public void addRegistrationAdapter(ImsRegistrationCompatAdapter regCompat) throws RemoteException {
        this.mRegCompatAdapter = regCompat;
    }

    /* access modifiers changed from: private */
    public MmTelFeature.MmTelCapabilities convertCapabilities(int[] enabledFeatures) {
        boolean[] featuresEnabled = new boolean[enabledFeatures.length];
        int i = 0;
        while (i <= 5 && i < enabledFeatures.length) {
            if (enabledFeatures[i] == i) {
                featuresEnabled[i] = true;
            } else if (enabledFeatures[i] == -1) {
                featuresEnabled[i] = false;
            }
            i++;
        }
        MmTelFeature.MmTelCapabilities capabilities = new MmTelFeature.MmTelCapabilities();
        if (featuresEnabled[0] || featuresEnabled[2]) {
            capabilities.addCapabilities(1);
        }
        if (featuresEnabled[1] || featuresEnabled[3]) {
            capabilities.addCapabilities(2);
        }
        if (featuresEnabled[4] || featuresEnabled[5]) {
            capabilities.addCapabilities(4);
        }
        Log.i(TAG, "convertCapabilities - capabilities: " + capabilities);
        return capabilities;
    }

    private PendingIntent createIncomingCallPendingIntent() {
        Intent intent = new Intent(ACTION_IMS_INCOMING_CALL);
        intent.setPackage("com.android.phone");
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
    }

    private int convertCapability(int capability, int radioTech) {
        if (radioTech == 0) {
            if (capability == 4) {
                return 4;
            }
            switch (capability) {
                case 1:
                    return 0;
                case 2:
                    return 1;
                default:
                    return -1;
            }
        } else if (radioTech != 1) {
            return -1;
        } else {
            if (capability == 4) {
                return 5;
            }
            switch (capability) {
                case 1:
                    return 2;
                case 2:
                    return 3;
                default:
                    return -1;
            }
        }
    }
}
