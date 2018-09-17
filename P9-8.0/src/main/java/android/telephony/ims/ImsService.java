package android.telephony.ims;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MMTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsServiceController.Stub;
import com.android.ims.internal.IImsUt;

public class ImsService extends Service {
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsServiceController";
    private static final String LOG_TAG = "ImsService";
    private static final int METHOD_GET_LAST_CALL_TYPE = 2002;
    public static final String SERVICE_INTERFACE = "android.telephony.ims.ImsService";
    private static final int TRANSACT_METHOD_BASE = 2001;
    private final SparseArray<SparseArray<ImsFeature>> mFeatures = new SparseArray();
    protected final IBinder mImsServiceController = new Stub() {
        public void createImsFeature(int slotId, int feature, IImsFeatureStatusCallback c) throws RemoteException {
            synchronized (ImsService.this.mFeatures) {
                ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "createImsFeature");
                ImsService.this.onCreateImsFeatureInternal(slotId, feature, c);
            }
        }

        public void removeImsFeature(int slotId, int feature, IImsFeatureStatusCallback c) throws RemoteException {
            synchronized (ImsService.this.mFeatures) {
                ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "removeImsFeature");
                ImsService.this.onRemoveImsFeatureInternal(slotId, feature, c);
            }
        }

        public int startSession(int slotId, int featureType, PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "startSession");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    int startSession = feature.startSession(incomingCallIntent, listener);
                    return startSession;
                }
                return 0;
            }
        }

        public void endSession(int slotId, int featureType, int sessionId) throws RemoteException {
            synchronized (ImsService.this.mFeatures) {
                ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "endSession");
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.endSession(sessionId);
                }
            }
        }

        public boolean isConnected(int slotId, int featureType, int callSessionType, int callType) throws RemoteException {
            ImsService.this.enforceReadPhoneStatePermission("isConnected");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    boolean isConnected = feature.isConnected(callSessionType, callType);
                    return isConnected;
                }
                return false;
            }
        }

        public boolean isOpened(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceReadPhoneStatePermission("isOpened");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    boolean isOpened = feature.isOpened();
                    return isOpened;
                }
                return false;
            }
        }

        public int getFeatureStatus(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceReadPhoneStatePermission("getFeatureStatus");
            int status = 0;
            synchronized (ImsService.this.mFeatures) {
                SparseArray<ImsFeature> featureMap = (SparseArray) ImsService.this.mFeatures.get(slotId);
                if (featureMap != null) {
                    ImsFeature feature = ImsService.this.getImsFeatureFromType(featureMap, featureType);
                    if (feature != null) {
                        status = feature.getFeatureState();
                    }
                }
            }
            return status;
        }

        public void addRegistrationListener(int slotId, int featureType, IImsRegistrationListener listener) throws RemoteException {
            ImsService.this.enforceReadPhoneStatePermission("addRegistrationListener");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.addRegistrationListener(listener);
                }
            }
        }

        public void removeRegistrationListener(int slotId, int featureType, IImsRegistrationListener listener) throws RemoteException {
            ImsService.this.enforceReadPhoneStatePermission("removeRegistrationListener");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.removeRegistrationListener(listener);
                }
            }
        }

        public ImsCallProfile createCallProfile(int slotId, int featureType, int sessionId, int callSessionType, int callType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "createCallProfile");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    ImsCallProfile createCallProfile = feature.createCallProfile(sessionId, callSessionType, callType);
                    return createCallProfile;
                }
                return null;
            }
        }

        public IImsCallSession createCallSession(int slotId, int featureType, int sessionId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "createCallSession");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsCallSession createCallSession = feature.createCallSession(sessionId, profile, listener);
                    return createCallSession;
                }
                return null;
            }
        }

        public IImsCallSession getPendingCallSession(int slotId, int featureType, int sessionId, String callId) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "getPendingCallSession");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsCallSession pendingCallSession = feature.getPendingCallSession(sessionId, callId);
                    return pendingCallSession;
                }
                return null;
            }
        }

        public IImsUt getUtInterface(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "getUtInterface");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsUt utInterface = feature.getUtInterface();
                    return utInterface;
                }
                return null;
            }
        }

        public IImsConfig getConfigInterface(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "getConfigInterface");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsConfig configInterface = feature.getConfigInterface();
                    return configInterface;
                }
                return null;
            }
        }

        public void turnOnIms(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "turnOnIms");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.turnOnIms();
                }
            }
        }

        public void turnOffIms(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "turnOffIms");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.turnOffIms();
                }
            }
        }

        public IImsEcbm getEcbmInterface(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "getEcbmInterface");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsEcbm ecbmInterface = feature.getEcbmInterface();
                    return ecbmInterface;
                }
                return null;
            }
        }

        public void setUiTTYMode(int slotId, int featureType, int uiTtyMode, Message onComplete) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "setUiTTYMode");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    feature.setUiTTYMode(uiTtyMode, onComplete);
                }
            }
        }

        public IImsMultiEndpoint getMultiEndpointInterface(int slotId, int featureType) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "getMultiEndpointInterface");
            synchronized (ImsService.this.mFeatures) {
                MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                if (feature != null) {
                    IImsMultiEndpoint multiEndpointInterface = feature.getMultiEndpointInterface();
                    return multiEndpointInterface;
                }
                return null;
            }
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsService.this.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "onTransact");
            Log.i(ImsService.LOG_TAG, "onTransact: code = " + code);
            switch (code) {
                case 2002:
                    data.enforceInterface(ImsService.DESCRIPTOR);
                    int slotId = data.readInt();
                    int featureType = data.readInt();
                    synchronized (ImsService.this.mFeatures) {
                        MMTelFeature feature = ImsService.this.resolveMMTelFeature(slotId, featureType);
                        if (feature != null) {
                            int result = feature.getLastCallType();
                            Log.i(ImsService.LOG_TAG, "onTransact: getLastCallType result = " + result + " slotId =" + slotId + " featureType = " + featureType);
                            reply.writeNoException();
                            reply.writeInt(result);
                        }
                    }
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    };

    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mImsServiceController;
        }
        return null;
    }

    private void onCreateImsFeatureInternal(int slotId, int featureType, IImsFeatureStatusCallback c) {
        SparseArray<ImsFeature> featureMap = (SparseArray) this.mFeatures.get(slotId);
        if (featureMap == null) {
            featureMap = new SparseArray();
            this.mFeatures.put(slotId, featureMap);
        }
        ImsFeature f = makeImsFeature(slotId, featureType);
        if (f != null) {
            f.setContext(this);
            f.setSlotId(slotId);
            f.addImsFeatureStatusCallback(c);
            featureMap.put(featureType, f);
        }
    }

    private void onRemoveImsFeatureInternal(int slotId, int featureType, IImsFeatureStatusCallback c) {
        SparseArray<ImsFeature> featureMap = (SparseArray) this.mFeatures.get(slotId);
        if (featureMap != null) {
            ImsFeature featureToRemove = getImsFeatureFromType(featureMap, featureType);
            if (featureToRemove != null) {
                featureMap.remove(featureType);
                featureToRemove.notifyFeatureRemoved(slotId);
                featureToRemove.removeImsFeatureStatusCallback(c);
            }
        }
    }

    private MMTelFeature resolveMMTelFeature(int slotId, int featureType) {
        SparseArray<ImsFeature> features = getImsFeatureMap(slotId);
        if (features != null) {
            return (MMTelFeature) resolveImsFeature(features, featureType, MMTelFeature.class);
        }
        return null;
    }

    private <T extends ImsFeature> T resolveImsFeature(SparseArray<ImsFeature> set, int featureType, Class<T> className) {
        ImsFeature feature = getImsFeatureFromType(set, featureType);
        if (feature == null) {
            return null;
        }
        try {
            return (ImsFeature) className.cast(feature);
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Can not cast ImsFeature! Exception: " + e.getMessage());
            return null;
        }
    }

    public SparseArray<ImsFeature> getImsFeatureMap(int slotId) {
        return (SparseArray) this.mFeatures.get(slotId);
    }

    public ImsFeature getImsFeatureFromType(SparseArray<ImsFeature> set, int featureType) {
        return (ImsFeature) set.get(featureType);
    }

    private ImsFeature makeImsFeature(int slotId, int feature) {
        switch (feature) {
            case 0:
                return onCreateEmergencyMMTelImsFeature(slotId);
            case 1:
                return onCreateMMTelImsFeature(slotId);
            case 2:
                return onCreateRcsFeature(slotId);
            default:
                return null;
        }
    }

    private void enforceReadPhoneStatePermission(String fn) {
        if (checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") != 0) {
            enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", fn);
        }
    }

    public MMTelFeature onCreateEmergencyMMTelImsFeature(int slotId) {
        return null;
    }

    public MMTelFeature onCreateMMTelImsFeature(int slotId) {
        return null;
    }

    public RcsFeature onCreateRcsFeature(int slotId) {
        return null;
    }
}
