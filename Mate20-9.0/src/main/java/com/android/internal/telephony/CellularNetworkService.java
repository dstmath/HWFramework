package com.android.internal.telephony;

import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityNr;
import android.telephony.NetworkRegistrationState;
import android.telephony.NetworkService;
import android.telephony.NetworkServiceCallback;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import java.util.concurrent.ConcurrentHashMap;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellIdentityNr;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellIdentity_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwDataRegStateResult_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwVoiceRegStateResult_1_1;

public class CellularNetworkService extends NetworkService {
    private static final boolean DBG = false;
    private static final int GET_CS_REGISTRATION_STATE_DONE = 1;
    private static final int GET_PS_REGISTRATION_STATE_DONE = 2;
    private static final int NETWORK_REGISTRATION_STATE_CHANGED = 3;
    private static final String TAG = CellularNetworkService.class.getSimpleName();

    private class CellularNetworkServiceProvider extends NetworkService.NetworkServiceProvider {
        /* access modifiers changed from: private */
        public final ConcurrentHashMap<Message, NetworkServiceCallback> mCallbackMap = new ConcurrentHashMap<>();
        private final Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread(CellularNetworkService.class.getSimpleName());
        private final Looper mLooper;
        private final Phone mPhone = PhoneFactory.getPhone(getSlotId());

        CellularNetworkServiceProvider(int slotId) {
            super(CellularNetworkService.this, slotId);
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, CellularNetworkService.this) {
                public void handleMessage(Message message) {
                    int resultCode;
                    NetworkServiceCallback callback = (NetworkServiceCallback) CellularNetworkServiceProvider.this.mCallbackMap.remove(message);
                    switch (message.what) {
                        case 1:
                        case 2:
                            if (callback != null) {
                                AsyncResult ar = (AsyncResult) message.obj;
                                int domain = 1;
                                if (message.what != 1) {
                                    domain = 2;
                                }
                                NetworkRegistrationState netState = CellularNetworkServiceProvider.this.getRegistrationStateFromResult(ar.result, domain);
                                if (ar.exception != null || netState == null) {
                                    resultCode = 5;
                                } else {
                                    resultCode = 0;
                                }
                                try {
                                    callback.onGetNetworkRegistrationStateComplete(resultCode, netState);
                                    break;
                                } catch (Exception e) {
                                    CellularNetworkService cellularNetworkService = CellularNetworkService.this;
                                    cellularNetworkService.loge("Exception: " + e);
                                    break;
                                }
                            } else {
                                return;
                            }
                        case 3:
                            CellularNetworkServiceProvider.this.notifyNetworkRegistrationStateChanged();
                            break;
                        default:
                            return;
                    }
                }
            };
            this.mPhone.mCi.registerForNetworkStateChanged(this.mHandler, 3, null);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
            return 3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
            return 2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
            return 4;
         */
        private int getRegStateFromHalRegState(int halRegState) {
            if (halRegState != 10) {
                switch (halRegState) {
                    case 0:
                        break;
                    case 1:
                        return 1;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        return 5;
                    default:
                        switch (halRegState) {
                            case 12:
                                break;
                            case 13:
                                break;
                            case 14:
                                break;
                            default:
                                return 0;
                        }
                }
            }
            return 0;
        }

        private boolean isEmergencyOnly(int halRegState) {
            switch (halRegState) {
                case 10:
                case 12:
                case 13:
                case 14:
                    return true;
                default:
                    return false;
            }
        }

        private int[] getAvailableServices(int regState, int domain, boolean emergencyOnly) {
            if (emergencyOnly) {
                return new int[]{5};
            } else if (regState != 5 && regState != 1) {
                return null;
            } else {
                if (domain == 2) {
                    return new int[]{2};
                } else if (domain == 1) {
                    return new int[]{1, 3, 4};
                } else {
                    return null;
                }
            }
        }

        private int getAccessNetworkTechnologyFromRat(int rilRat) {
            return ServiceState.rilRadioTechnologyToNetworkType(rilRat);
        }

        /* access modifiers changed from: private */
        public NetworkRegistrationState getRegistrationStateFromResult(Object result, int domain) {
            if (result == null) {
                return null;
            }
            if (domain == 1) {
                return createRegistrationStateFromVoiceRegState(result);
            }
            if (domain == 2) {
                return createRegistrationStateFromDataRegState(result);
            }
            return null;
        }

        private NetworkRegistrationState createRegistrationStateFromVoiceRegState(Object result) {
            Object obj = result;
            if (obj instanceof VoiceRegStateResult) {
                VoiceRegStateResult voiceRegState = (VoiceRegStateResult) obj;
                int regState = getRegStateFromHalRegState(voiceRegState.regState);
                int accessNetworkTechnology = getAccessNetworkTechnologyFromRat(voiceRegState.rat);
                int reasonForDenial = voiceRegState.reasonForDenial;
                boolean emergencyOnly = isEmergencyOnly(voiceRegState.regState);
                boolean cssSupported = voiceRegState.cssSupported;
                int roamingIndicator = voiceRegState.roamingIndicator;
                int systemIsInPrl = voiceRegState.systemIsInPrl;
                int defaultRoamingIndicator = voiceRegState.defaultRoamingIndicator;
                int systemIsInPrl2 = systemIsInPrl;
                int roamingIndicator2 = roamingIndicator;
                boolean cssSupported2 = cssSupported;
                boolean z = emergencyOnly;
                int i = reasonForDenial;
                int i2 = regState;
                VoiceRegStateResult voiceRegStateResult = voiceRegState;
                NetworkRegistrationState networkRegistrationState = new NetworkRegistrationState(1, 1, regState, accessNetworkTechnology, reasonForDenial, emergencyOnly, getAvailableServices(regState, 1, emergencyOnly), convertHalCellIdentityToCellIdentity(voiceRegState.cellIdentity), cssSupported2, roamingIndicator2, systemIsInPrl2, defaultRoamingIndicator);
                return networkRegistrationState;
            } else if (obj instanceof android.hardware.radio.V1_2.VoiceRegStateResult) {
                android.hardware.radio.V1_2.VoiceRegStateResult voiceRegState2 = (android.hardware.radio.V1_2.VoiceRegStateResult) obj;
                int regState2 = getRegStateFromHalRegState(voiceRegState2.regState);
                int accessNetworkTechnology2 = getAccessNetworkTechnologyFromRat(voiceRegState2.rat);
                int reasonForDenial2 = voiceRegState2.reasonForDenial;
                boolean emergencyOnly2 = isEmergencyOnly(voiceRegState2.regState);
                boolean cssSupported3 = voiceRegState2.cssSupported;
                int roamingIndicator3 = voiceRegState2.roamingIndicator;
                int systemIsInPrl3 = voiceRegState2.systemIsInPrl;
                int defaultRoamingIndicator2 = voiceRegState2.defaultRoamingIndicator;
                int systemIsInPrl4 = systemIsInPrl3;
                int roamingIndicator4 = roamingIndicator3;
                boolean cssSupported4 = cssSupported3;
                boolean z2 = emergencyOnly2;
                int i3 = reasonForDenial2;
                int i4 = regState2;
                android.hardware.radio.V1_2.VoiceRegStateResult voiceRegStateResult2 = voiceRegState2;
                NetworkRegistrationState networkRegistrationState2 = new NetworkRegistrationState(1, 1, regState2, accessNetworkTechnology2, reasonForDenial2, emergencyOnly2, getAvailableServices(regState2, 1, emergencyOnly2), convertHalCellIdentityToCellIdentity(voiceRegState2.cellIdentity), cssSupported4, roamingIndicator4, systemIsInPrl4, defaultRoamingIndicator2);
                return networkRegistrationState2;
            } else if (!(obj instanceof HwVoiceRegStateResult_1_1)) {
                return null;
            } else {
                HwVoiceRegStateResult_1_1 voiceRegState3 = (HwVoiceRegStateResult_1_1) obj;
                int regState3 = getRegStateFromHalRegState(voiceRegState3.regState);
                int accessNetworkTechnology3 = getAccessNetworkTechnologyFromRat(voiceRegState3.rat);
                int reasonForDenial3 = voiceRegState3.reasonForDenial;
                boolean emergencyOnly3 = isEmergencyOnly(voiceRegState3.regState);
                boolean cssSupported5 = voiceRegState3.cssSupported;
                int roamingIndicator5 = voiceRegState3.roamingIndicator;
                int systemIsInPrl5 = voiceRegState3.systemIsInPrl;
                int defaultRoamingIndicator3 = voiceRegState3.defaultRoamingIndicator;
                int[] availableServices = getAvailableServices(regState3, 1, emergencyOnly3);
                CellIdentity cellIdentity = convertHalCellIdentityToCellIdentity(voiceRegState3.cellIdentity);
                int nsaState = voiceRegState3.nsaState;
                int systemIsInPrl6 = systemIsInPrl5;
                int roamingIndicator6 = roamingIndicator5;
                boolean cssSupported6 = cssSupported5;
                boolean z3 = emergencyOnly3;
                int i5 = reasonForDenial3;
                int i6 = regState3;
                HwVoiceRegStateResult_1_1 hwVoiceRegStateResult_1_1 = voiceRegState3;
                NetworkRegistrationState networkRegistrationState3 = new NetworkRegistrationState(1, 1, regState3, accessNetworkTechnology3, reasonForDenial3, emergencyOnly3, availableServices, cellIdentity, cssSupported6, roamingIndicator6, systemIsInPrl6, defaultRoamingIndicator3);
                networkRegistrationState3.setNsaState(nsaState);
                return networkRegistrationState3;
            }
        }

        private NetworkRegistrationState createRegistrationStateFromDataRegState(Object result) {
            Object obj = result;
            if (obj instanceof DataRegStateResult) {
                DataRegStateResult dataRegState = (DataRegStateResult) obj;
                int regState = getRegStateFromHalRegState(dataRegState.regState);
                int accessNetworkTechnology = getAccessNetworkTechnologyFromRat(dataRegState.rat);
                int reasonForDenial = dataRegState.reasonDataDenied;
                boolean emergencyOnly = isEmergencyOnly(dataRegState.regState);
                int maxDataCalls = dataRegState.maxDataCalls;
                boolean z = emergencyOnly;
                int i = reasonForDenial;
                NetworkRegistrationState networkRegistrationState = new NetworkRegistrationState(1, 2, regState, accessNetworkTechnology, reasonForDenial, emergencyOnly, getAvailableServices(regState, 2, emergencyOnly), convertHalCellIdentityToCellIdentity(dataRegState.cellIdentity), maxDataCalls);
                return networkRegistrationState;
            } else if (obj instanceof android.hardware.radio.V1_2.DataRegStateResult) {
                android.hardware.radio.V1_2.DataRegStateResult dataRegState2 = (android.hardware.radio.V1_2.DataRegStateResult) obj;
                int regState2 = getRegStateFromHalRegState(dataRegState2.regState);
                int accessNetworkTechnology2 = getAccessNetworkTechnologyFromRat(dataRegState2.rat);
                int reasonForDenial2 = dataRegState2.reasonDataDenied;
                boolean emergencyOnly2 = isEmergencyOnly(dataRegState2.regState);
                int maxDataCalls2 = dataRegState2.maxDataCalls;
                boolean z2 = emergencyOnly2;
                int i2 = reasonForDenial2;
                NetworkRegistrationState networkRegistrationState2 = new NetworkRegistrationState(1, 2, regState2, accessNetworkTechnology2, reasonForDenial2, emergencyOnly2, getAvailableServices(regState2, 2, emergencyOnly2), convertHalCellIdentityToCellIdentity(dataRegState2.cellIdentity), maxDataCalls2);
                return networkRegistrationState2;
            } else if (!(obj instanceof HwDataRegStateResult_1_1)) {
                return null;
            } else {
                HwDataRegStateResult_1_1 dataRegState3 = (HwDataRegStateResult_1_1) obj;
                int regState3 = getRegStateFromHalRegState(dataRegState3.regState);
                int accessNetworkTechnology3 = getAccessNetworkTechnologyFromRat(dataRegState3.rat);
                int reasonForDenial3 = dataRegState3.reasonDataDenied;
                boolean emergencyOnly3 = isEmergencyOnly(dataRegState3.regState);
                int maxDataCalls3 = dataRegState3.maxDataCalls;
                int[] availableServices = getAvailableServices(regState3, 2, emergencyOnly3);
                CellIdentity cellIdentity = convertHalCellIdentityToCellIdentity(dataRegState3.cellIdentity);
                int nsaState = dataRegState3.nsaState;
                boolean z3 = emergencyOnly3;
                int i3 = reasonForDenial3;
                NetworkRegistrationState networkRegistrationState3 = new NetworkRegistrationState(1, 2, regState3, accessNetworkTechnology3, reasonForDenial3, emergencyOnly3, availableServices, cellIdentity, maxDataCalls3);
                networkRegistrationState3.setNsaState(nsaState);
                return networkRegistrationState3;
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: android.telephony.CellIdentityCdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: android.telephony.CellIdentityLte} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: android.telephony.CellIdentityWcdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: android.telephony.CellIdentityTdscdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX WARNING: Multi-variable type inference failed */
        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_0.CellIdentity cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            CellIdentity result = null;
            switch (cellIdentity.cellInfoType) {
                case 1:
                    if (cellIdentity.cellIdentityGsm.size() == 1) {
                        CellIdentityGsm cellIdentityGsm = cellIdentity.cellIdentityGsm.get(0);
                        android.telephony.CellIdentityGsm cellIdentityGsm2 = new android.telephony.CellIdentityGsm(cellIdentityGsm.lac, cellIdentityGsm.cid, cellIdentityGsm.arfcn, cellIdentityGsm.bsic, cellIdentityGsm.mcc, cellIdentityGsm.mnc, null, null);
                        result = cellIdentityGsm2;
                        break;
                    }
                    break;
                case 2:
                    if (cellIdentity.cellIdentityCdma.size() == 1) {
                        CellIdentityCdma cellIdentityCdma = cellIdentity.cellIdentityCdma.get(0);
                        android.telephony.CellIdentityCdma cellIdentityCdma2 = new android.telephony.CellIdentityCdma(cellIdentityCdma.networkId, cellIdentityCdma.systemId, cellIdentityCdma.baseStationId, cellIdentityCdma.longitude, cellIdentityCdma.latitude);
                        result = cellIdentityCdma2;
                        break;
                    }
                    break;
                case 3:
                    if (cellIdentity.cellIdentityLte.size() == 1) {
                        CellIdentityLte cellIdentityLte = cellIdentity.cellIdentityLte.get(0);
                        android.telephony.CellIdentityLte cellIdentityLte2 = new android.telephony.CellIdentityLte(cellIdentityLte.ci, cellIdentityLte.pci, cellIdentityLte.tac, cellIdentityLte.earfcn, KeepaliveStatus.INVALID_HANDLE, cellIdentityLte.mcc, cellIdentityLte.mnc, null, null);
                        result = cellIdentityLte2;
                        break;
                    }
                    break;
                case 4:
                    if (cellIdentity.cellIdentityWcdma.size() == 1) {
                        CellIdentityWcdma cellIdentityWcdma = cellIdentity.cellIdentityWcdma.get(0);
                        android.telephony.CellIdentityWcdma cellIdentityWcdma2 = new android.telephony.CellIdentityWcdma(cellIdentityWcdma.lac, cellIdentityWcdma.cid, cellIdentityWcdma.psc, cellIdentityWcdma.uarfcn, cellIdentityWcdma.mcc, cellIdentityWcdma.mnc, null, null);
                        result = cellIdentityWcdma2;
                        break;
                    }
                    break;
                case 5:
                    if (cellIdentity.cellIdentityTdscdma.size() == 1) {
                        CellIdentityTdscdma cellIdentityTdscdma = cellIdentity.cellIdentityTdscdma.get(0);
                        android.telephony.CellIdentityTdscdma cellIdentityTdscdma2 = new android.telephony.CellIdentityTdscdma(cellIdentityTdscdma.mcc, cellIdentityTdscdma.mnc, cellIdentityTdscdma.lac, cellIdentityTdscdma.cid, cellIdentityTdscdma.cpid);
                        result = cellIdentityTdscdma2;
                        break;
                    }
                    break;
            }
            return result;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v17, resolved type: android.telephony.CellIdentityCdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v26, resolved type: android.telephony.CellIdentityLte} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v35, resolved type: android.telephony.CellIdentityWcdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v43, resolved type: android.telephony.CellIdentityTdscdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX WARNING: Multi-variable type inference failed */
        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_2.CellIdentity cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            CellIdentity result = null;
            switch (cellIdentity.cellInfoType) {
                case 1:
                    if (cellIdentity.cellIdentityGsm.size() == 1) {
                        android.hardware.radio.V1_2.CellIdentityGsm cellIdentityGsm = cellIdentity.cellIdentityGsm.get(0);
                        android.telephony.CellIdentityGsm cellIdentityGsm2 = new android.telephony.CellIdentityGsm(cellIdentityGsm.base.lac, cellIdentityGsm.base.cid, cellIdentityGsm.base.arfcn, cellIdentityGsm.base.bsic, cellIdentityGsm.base.mcc, cellIdentityGsm.base.mnc, cellIdentityGsm.operatorNames.alphaLong, cellIdentityGsm.operatorNames.alphaShort);
                        result = cellIdentityGsm2;
                        break;
                    }
                    break;
                case 2:
                    if (cellIdentity.cellIdentityCdma.size() == 1) {
                        android.hardware.radio.V1_2.CellIdentityCdma cellIdentityCdma = cellIdentity.cellIdentityCdma.get(0);
                        android.telephony.CellIdentityCdma cellIdentityCdma2 = new android.telephony.CellIdentityCdma(cellIdentityCdma.base.networkId, cellIdentityCdma.base.systemId, cellIdentityCdma.base.baseStationId, cellIdentityCdma.base.longitude, cellIdentityCdma.base.latitude, cellIdentityCdma.operatorNames.alphaLong, cellIdentityCdma.operatorNames.alphaShort);
                        result = cellIdentityCdma2;
                        break;
                    }
                    break;
                case 3:
                    if (cellIdentity.cellIdentityLte.size() == 1) {
                        android.hardware.radio.V1_2.CellIdentityLte cellIdentityLte = cellIdentity.cellIdentityLte.get(0);
                        android.telephony.CellIdentityLte cellIdentityLte2 = new android.telephony.CellIdentityLte(cellIdentityLte.base.ci, cellIdentityLte.base.pci, cellIdentityLte.base.tac, cellIdentityLte.base.earfcn, cellIdentityLte.bandwidth, cellIdentityLte.base.mcc, cellIdentityLte.base.mnc, cellIdentityLte.operatorNames.alphaLong, cellIdentityLte.operatorNames.alphaShort);
                        result = cellIdentityLte2;
                        break;
                    }
                    break;
                case 4:
                    if (cellIdentity.cellIdentityWcdma.size() == 1) {
                        android.hardware.radio.V1_2.CellIdentityWcdma cellIdentityWcdma = cellIdentity.cellIdentityWcdma.get(0);
                        android.telephony.CellIdentityWcdma cellIdentityWcdma2 = new android.telephony.CellIdentityWcdma(cellIdentityWcdma.base.lac, cellIdentityWcdma.base.cid, cellIdentityWcdma.base.psc, cellIdentityWcdma.base.uarfcn, cellIdentityWcdma.base.mcc, cellIdentityWcdma.base.mnc, cellIdentityWcdma.operatorNames.alphaLong, cellIdentityWcdma.operatorNames.alphaShort);
                        result = cellIdentityWcdma2;
                        break;
                    }
                    break;
                case 5:
                    if (cellIdentity.cellIdentityTdscdma.size() == 1) {
                        android.hardware.radio.V1_2.CellIdentityTdscdma cellIdentityTdscdma = cellIdentity.cellIdentityTdscdma.get(0);
                        android.telephony.CellIdentityTdscdma cellIdentityTdscdma2 = new android.telephony.CellIdentityTdscdma(cellIdentityTdscdma.base.mcc, cellIdentityTdscdma.base.mnc, cellIdentityTdscdma.base.lac, cellIdentityTdscdma.base.cid, cellIdentityTdscdma.base.cpid, cellIdentityTdscdma.operatorNames.alphaLong, cellIdentityTdscdma.operatorNames.alphaShort);
                        result = cellIdentityTdscdma2;
                        break;
                    }
                    break;
            }
            return result;
        }

        public void getNetworkRegistrationState(int domain, NetworkServiceCallback callback) {
            if (domain == 1) {
                Message message = Message.obtain(this.mHandler, 1);
                this.mCallbackMap.put(message, callback);
                this.mPhone.mCi.getVoiceRegistrationState(message);
            } else if (domain == 2) {
                Message message2 = Message.obtain(this.mHandler, 2);
                this.mCallbackMap.put(message2, callback);
                this.mPhone.mCi.getDataRegistrationState(message2);
            } else {
                CellularNetworkService cellularNetworkService = CellularNetworkService.this;
                cellularNetworkService.loge("getNetworkRegistrationState invalid domain " + domain);
                callback.onGetNetworkRegistrationStateComplete(2, null);
            }
        }

        /* access modifiers changed from: protected */
        public void onDestroy() {
            CellularNetworkService.super.onDestroy();
            this.mCallbackMap.clear();
            this.mHandlerThread.quit();
            this.mPhone.mCi.unregisterForNetworkStateChanged(this.mHandler);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: android.telephony.CellIdentityCdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: android.telephony.CellIdentityLte} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: android.telephony.CellIdentityWcdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: android.telephony.CellIdentityTdscdma} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: android.telephony.CellIdentityNr} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: android.telephony.CellIdentityGsm} */
        /* JADX WARNING: Multi-variable type inference failed */
        private CellIdentity convertHalCellIdentityToCellIdentity(HwCellIdentity_1_1 cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            CellIdentity result = null;
            switch (cellIdentity.cellInfoType) {
                case 1:
                    if (cellIdentity.cellIdentityGsm.size() == 1) {
                        CellIdentityGsm cellIdentityGsm = cellIdentity.cellIdentityGsm.get(0);
                        android.telephony.CellIdentityGsm cellIdentityGsm2 = new android.telephony.CellIdentityGsm(cellIdentityGsm.lac, cellIdentityGsm.cid, cellIdentityGsm.arfcn, cellIdentityGsm.bsic, cellIdentityGsm.mcc, cellIdentityGsm.mnc, null, null);
                        result = cellIdentityGsm2;
                        break;
                    }
                    break;
                case 2:
                    if (cellIdentity.cellIdentityCdma.size() == 1) {
                        CellIdentityCdma cellIdentityCdma = cellIdentity.cellIdentityCdma.get(0);
                        android.telephony.CellIdentityCdma cellIdentityCdma2 = new android.telephony.CellIdentityCdma(cellIdentityCdma.networkId, cellIdentityCdma.systemId, cellIdentityCdma.baseStationId, cellIdentityCdma.longitude, cellIdentityCdma.latitude);
                        result = cellIdentityCdma2;
                        break;
                    }
                    break;
                case 3:
                    if (cellIdentity.cellIdentityLte.size() == 1) {
                        CellIdentityLte cellIdentityLte = cellIdentity.cellIdentityLte.get(0);
                        android.telephony.CellIdentityLte cellIdentityLte2 = new android.telephony.CellIdentityLte(cellIdentityLte.ci, cellIdentityLte.pci, cellIdentityLte.tac, cellIdentityLte.earfcn, KeepaliveStatus.INVALID_HANDLE, cellIdentityLte.mcc, cellIdentityLte.mnc, null, null);
                        result = cellIdentityLte2;
                        break;
                    }
                    break;
                case 4:
                    if (cellIdentity.cellIdentityWcdma.size() == 1) {
                        CellIdentityWcdma cellIdentityWcdma = cellIdentity.cellIdentityWcdma.get(0);
                        android.telephony.CellIdentityWcdma cellIdentityWcdma2 = new android.telephony.CellIdentityWcdma(cellIdentityWcdma.lac, cellIdentityWcdma.cid, cellIdentityWcdma.psc, cellIdentityWcdma.uarfcn, cellIdentityWcdma.mcc, cellIdentityWcdma.mnc, null, null);
                        result = cellIdentityWcdma2;
                        break;
                    }
                    break;
                case 5:
                    if (cellIdentity.cellIdentityTdscdma.size() == 1) {
                        CellIdentityTdscdma cellIdentityTdscdma = cellIdentity.cellIdentityTdscdma.get(0);
                        android.telephony.CellIdentityTdscdma cellIdentityTdscdma2 = new android.telephony.CellIdentityTdscdma(cellIdentityTdscdma.mcc, cellIdentityTdscdma.mnc, cellIdentityTdscdma.lac, cellIdentityTdscdma.cid, cellIdentityTdscdma.cpid);
                        result = cellIdentityTdscdma2;
                        break;
                    }
                    break;
                case 6:
                    if (cellIdentity.cellIdentityNr.size() == 1) {
                        HwCellIdentityNr cellIdentityNr = cellIdentity.cellIdentityNr.get(0);
                        CellIdentityNr cellIdentityNr2 = new CellIdentityNr(cellIdentityNr.ci, cellIdentityNr.pci, cellIdentityNr.tac, cellIdentityNr.earfcn, KeepaliveStatus.INVALID_HANDLE, cellIdentityNr.mcc, cellIdentityNr.mnc, null, null);
                        result = cellIdentityNr2;
                        break;
                    }
                    break;
            }
            return result;
        }
    }

    /* access modifiers changed from: protected */
    public NetworkService.NetworkServiceProvider createNetworkServiceProvider(int slotId) {
        if (SubscriptionManager.isValidSlotIndex(slotId) || slotId == 2) {
            return new CellularNetworkServiceProvider(slotId);
        }
        loge("Tried to Cellular network service with invalid slotId " + slotId);
        return null;
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
