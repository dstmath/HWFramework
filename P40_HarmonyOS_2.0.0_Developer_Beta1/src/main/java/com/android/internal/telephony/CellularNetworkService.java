package com.android.internal.telephony;

import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.hardware.radio.V1_4.CellIdentityNr;
import android.hardware.radio.V1_4.LteVopsInfo;
import android.hardware.radio.V1_4.NrIndicators;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.LteVopsSupportInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.NetworkService;
import android.telephony.NetworkServiceCallback;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import vendor.huawei.hardware.hisiradio.V1_2.HwDataRegStateResult13;
import vendor.huawei.hardware.radio.V2_1.HwDataRegStateResult_2_1;

public class CellularNetworkService extends NetworkService {
    private static final boolean DBG = false;
    private static final int GET_CS_REGISTRATION_STATE_DONE = 1;
    private static final int GET_PS_REGISTRATION_STATE_DONE = 2;
    private static final int NETWORK_REGISTRATION_STATE_CHANGED = 3;
    private static final String TAG = CellularNetworkService.class.getSimpleName();

    private class CellularNetworkServiceProvider extends NetworkService.NetworkServiceProvider {
        private final ConcurrentHashMap<Message, NetworkServiceCallback> mCallbackMap = new ConcurrentHashMap<>();
        private final Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread(CellularNetworkService.class.getSimpleName());
        private final Looper mLooper;
        private final Phone mPhone = PhoneFactory.getPhone(getSlotIndex());

        CellularNetworkServiceProvider(int slotId) {
            super(CellularNetworkService.this, slotId);
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, CellularNetworkService.this) {
                /* class com.android.internal.telephony.CellularNetworkService.CellularNetworkServiceProvider.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    int resultCode;
                    NetworkServiceCallback callback = (NetworkServiceCallback) CellularNetworkServiceProvider.this.mCallbackMap.remove(message);
                    int i = message.what;
                    int domain = 2;
                    if (i == 1 || i == 2) {
                        if (callback != null) {
                            AsyncResult ar = (AsyncResult) message.obj;
                            if (message.what == 1) {
                                domain = 1;
                            }
                            NetworkRegistrationInfo netState = CellularNetworkServiceProvider.this.getRegistrationStateFromResult(ar.result, domain);
                            if (ar.exception != null || netState == null) {
                                resultCode = 5;
                            } else {
                                resultCode = 0;
                            }
                            try {
                                callback.onRequestNetworkRegistrationInfoComplete(resultCode, netState);
                            } catch (Exception e) {
                                CellularNetworkService.this.loge("Exception: onRequestNetworkRegistrationInfoComplete error occur.");
                            }
                        }
                    } else if (i == 3) {
                        CellularNetworkServiceProvider.this.notifyNetworkRegistrationInfoChanged();
                    }
                }
            };
            this.mPhone.mCi.registerForNetworkStateChanged(this.mHandler, 3, null);
        }

        private int getRegStateFromHalRegState(int halRegState) {
            if (halRegState != 0) {
                if (halRegState == 1) {
                    return 1;
                }
                if (halRegState != 2) {
                    if (halRegState != 3) {
                        if (halRegState != 4) {
                            if (halRegState == 5) {
                                return 5;
                            }
                            if (halRegState != 10) {
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
                        return 4;
                    }
                    return 3;
                }
                return 2;
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
                case 11:
                default:
                    return false;
            }
        }

        private List<Integer> getAvailableServices(int regState, int domain, boolean emergencyOnly) {
            List<Integer> availableServices = new ArrayList<>();
            if (emergencyOnly) {
                availableServices.add(5);
            } else if (regState == 5 || regState == 1) {
                if (domain == 2) {
                    availableServices.add(2);
                } else if (domain == 1) {
                    availableServices.add(1);
                    availableServices.add(3);
                    availableServices.add(4);
                }
            }
            return availableServices;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private NetworkRegistrationInfo getRegistrationStateFromResult(Object result, int domain) {
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

        private NetworkRegistrationInfo createRegistrationStateFromVoiceRegState(Object result) {
            int networkType;
            int networkType2;
            if (result instanceof VoiceRegStateResult) {
                VoiceRegStateResult voiceRegState = (VoiceRegStateResult) result;
                int regState = getRegStateFromHalRegState(voiceRegState.regState);
                int networkType3 = ServiceState.rilRadioTechnologyToNetworkType(voiceRegState.rat);
                if (networkType3 == 19) {
                    networkType2 = 13;
                } else {
                    networkType2 = networkType3;
                }
                int reasonForDenial = voiceRegState.reasonForDenial;
                boolean emergencyOnly = isEmergencyOnly(voiceRegState.regState);
                return new NetworkRegistrationInfo(1, 1, regState, networkType2, reasonForDenial, emergencyOnly, getAvailableServices(regState, 1, emergencyOnly), convertHalCellIdentityToCellIdentity(voiceRegState.cellIdentity), voiceRegState.cssSupported, voiceRegState.roamingIndicator, voiceRegState.systemIsInPrl, voiceRegState.defaultRoamingIndicator);
            } else if (result instanceof android.hardware.radio.V1_2.VoiceRegStateResult) {
                android.hardware.radio.V1_2.VoiceRegStateResult voiceRegState2 = (android.hardware.radio.V1_2.VoiceRegStateResult) result;
                int regState2 = getRegStateFromHalRegState(voiceRegState2.regState);
                int networkType4 = ServiceState.rilRadioTechnologyToNetworkType(voiceRegState2.rat);
                if (networkType4 == 19) {
                    networkType = 13;
                } else {
                    networkType = networkType4;
                }
                int reasonForDenial2 = voiceRegState2.reasonForDenial;
                boolean emergencyOnly2 = isEmergencyOnly(voiceRegState2.regState);
                return new NetworkRegistrationInfo(1, 1, regState2, networkType, reasonForDenial2, emergencyOnly2, getAvailableServices(regState2, 1, emergencyOnly2), convertHalCellIdentityToCellIdentity(voiceRegState2.cellIdentity), voiceRegState2.cssSupported, voiceRegState2.roamingIndicator, voiceRegState2.systemIsInPrl, voiceRegState2.defaultRoamingIndicator);
            } else {
                CellularNetworkService.this.loge("Unknown type of VoiceRegStateResult " + result);
                return null;
            }
        }

        /* JADX INFO: Multiple debug info for r2v4 android.hardware.radio.V1_4.NrIndicators: [D('regState' int), D('nrIndicators' android.hardware.radio.V1_4.NrIndicators)] */
        /* JADX INFO: Multiple debug info for r2v5 boolean: [D('isDcNrRestricted' boolean), D('nrIndicators' android.hardware.radio.V1_4.NrIndicators)] */
        /* JADX INFO: Multiple debug info for r1v18 android.telephony.CellIdentity: [D('cellIdentity' android.telephony.CellIdentity), D('dataRegState' android.hardware.radio.V1_2.DataRegStateResult)] */
        /* JADX INFO: Multiple debug info for r1v21 android.telephony.CellIdentity: [D('cellIdentity' android.telephony.CellIdentity), D('dataRegState' android.hardware.radio.V1_0.DataRegStateResult)] */
        private NetworkRegistrationInfo createRegistrationStateFromDataRegState(Object result) {
            Object result2;
            CellIdentityNr cellIdentityNr;
            boolean isDcNrRestricted;
            boolean isNrAvailable;
            boolean isEndcAvailable;
            int maxDataCalls;
            int reasonForDenial;
            CellIdentity cellIdentity;
            LteVopsSupportInfo lteVopsSupportInfo;
            int regState;
            boolean emergencyOnly;
            int networkType;
            boolean isUsingCarrierAggregation;
            int networkType2;
            LteVopsSupportInfo lteVopsSupportInfo2;
            Object result3 = result;
            CellIdentityNr cellIdentityNr2 = null;
            LteVopsSupportInfo lteVopsSupportInfo3 = new LteVopsSupportInfo(1, 1);
            if (result3 instanceof HwDataRegStateResult13) {
                HwDataRegStateResult13 dataRegState = (HwDataRegStateResult13) result3;
                cellIdentityNr2 = dataRegState.cellIdentityNr;
                if (dataRegState.base != null) {
                    result3 = dataRegState.base;
                }
            }
            if (result3 instanceof HwDataRegStateResult_2_1) {
                HwDataRegStateResult_2_1 dataRegState2 = (HwDataRegStateResult_2_1) result3;
                CellIdentityNr cellIdentityNr3 = dataRegState2.cellIdentityNr;
                if (dataRegState2.base != null) {
                    result2 = dataRegState2.base;
                    cellIdentityNr = cellIdentityNr3;
                } else {
                    result2 = result3;
                    cellIdentityNr = cellIdentityNr3;
                }
            } else {
                result2 = result3;
                cellIdentityNr = cellIdentityNr2;
            }
            if (result2 instanceof DataRegStateResult) {
                DataRegStateResult dataRegState3 = (DataRegStateResult) result2;
                int regState2 = getRegStateFromHalRegState(dataRegState3.regState);
                networkType = ServiceState.rilRadioTechnologyToNetworkType(dataRegState3.rat);
                int reasonForDenial2 = dataRegState3.reasonDataDenied;
                boolean emergencyOnly2 = isEmergencyOnly(dataRegState3.regState);
                int maxDataCalls2 = dataRegState3.maxDataCalls;
                cellIdentity = convertHalCellIdentityToCellIdentity(dataRegState3.cellIdentity);
                reasonForDenial = reasonForDenial2;
                emergencyOnly = emergencyOnly2;
                maxDataCalls = maxDataCalls2;
                isEndcAvailable = false;
                isNrAvailable = false;
                isDcNrRestricted = false;
                lteVopsSupportInfo = lteVopsSupportInfo3;
                regState = regState2;
            } else if (result2 instanceof android.hardware.radio.V1_2.DataRegStateResult) {
                android.hardware.radio.V1_2.DataRegStateResult dataRegState4 = (android.hardware.radio.V1_2.DataRegStateResult) result2;
                int regState3 = getRegStateFromHalRegState(dataRegState4.regState);
                networkType = ServiceState.rilRadioTechnologyToNetworkType(dataRegState4.rat);
                int reasonForDenial3 = dataRegState4.reasonDataDenied;
                boolean emergencyOnly3 = isEmergencyOnly(dataRegState4.regState);
                int maxDataCalls3 = dataRegState4.maxDataCalls;
                cellIdentity = convertHalCellIdentityToCellIdentity(dataRegState4.cellIdentity);
                reasonForDenial = reasonForDenial3;
                emergencyOnly = emergencyOnly3;
                maxDataCalls = maxDataCalls3;
                isEndcAvailable = false;
                isNrAvailable = false;
                isDcNrRestricted = false;
                lteVopsSupportInfo = lteVopsSupportInfo3;
                regState = regState3;
            } else if (result2 instanceof android.hardware.radio.V1_4.DataRegStateResult) {
                android.hardware.radio.V1_4.DataRegStateResult dataRegState5 = (android.hardware.radio.V1_4.DataRegStateResult) result2;
                int regState4 = getRegStateFromHalRegState(dataRegState5.base.regState);
                int networkType3 = ServiceState.rilRadioTechnologyToNetworkType(dataRegState5.base.rat);
                int reasonForDenial4 = dataRegState5.base.reasonDataDenied;
                boolean emergencyOnly4 = isEmergencyOnly(dataRegState5.base.regState);
                int maxDataCalls4 = dataRegState5.base.maxDataCalls;
                CellIdentity cellIdentity2 = convertHalCellIdentityToCellIdentity(dataRegState5.base.cellIdentity, cellIdentityNr);
                NrIndicators nrIndicators = dataRegState5.nrIndicators;
                reasonForDenial = reasonForDenial4;
                if (dataRegState5.vopsInfo.getDiscriminator() == 1) {
                    if (ServiceState.rilRadioTechnologyToAccessNetworkType(dataRegState5.base.rat) == 3) {
                        LteVopsInfo vopsSupport = dataRegState5.vopsInfo.lteVopsInfo();
                        lteVopsSupportInfo2 = convertHalLteVopsSupportInfo(vopsSupport.isVopsSupported, vopsSupport.isEmcBearerSupported);
                        boolean isEndcAvailable2 = nrIndicators.isEndcAvailable;
                        boolean isNrAvailable2 = nrIndicators.isNrAvailable;
                        regState = regState4;
                        lteVopsSupportInfo = lteVopsSupportInfo2;
                        isDcNrRestricted = nrIndicators.isDcNrRestricted;
                        isEndcAvailable = isEndcAvailable2;
                        isNrAvailable = isNrAvailable2;
                        maxDataCalls = maxDataCalls4;
                        networkType = networkType3;
                        cellIdentity = cellIdentity2;
                        emergencyOnly = emergencyOnly4;
                    }
                }
                lteVopsSupportInfo2 = new LteVopsSupportInfo(1, 1);
                boolean isEndcAvailable22 = nrIndicators.isEndcAvailable;
                boolean isNrAvailable22 = nrIndicators.isNrAvailable;
                regState = regState4;
                lteVopsSupportInfo = lteVopsSupportInfo2;
                isDcNrRestricted = nrIndicators.isDcNrRestricted;
                isEndcAvailable = isEndcAvailable22;
                isNrAvailable = isNrAvailable22;
                maxDataCalls = maxDataCalls4;
                networkType = networkType3;
                cellIdentity = cellIdentity2;
                emergencyOnly = emergencyOnly4;
            } else {
                CellularNetworkService.this.loge("Unknown type of DataRegStateResult " + result2);
                return null;
            }
            List<Integer> availableServices = getAvailableServices(regState, 2, emergencyOnly);
            if (networkType == 19) {
                isUsingCarrierAggregation = true;
                networkType2 = 13;
            } else {
                networkType2 = networkType;
                isUsingCarrierAggregation = false;
            }
            return new NetworkRegistrationInfo(2, 1, regState, networkType2, reasonForDenial, emergencyOnly, availableServices, cellIdentity, maxDataCalls, isDcNrRestricted, isNrAvailable, isEndcAvailable, lteVopsSupportInfo, isUsingCarrierAggregation);
        }

        private LteVopsSupportInfo convertHalLteVopsSupportInfo(boolean vopsSupport, boolean emcBearerSupport) {
            int vops = 3;
            int emergency = 3;
            if (vopsSupport) {
                vops = 2;
            }
            if (emcBearerSupport) {
                emergency = 2;
            }
            return new LteVopsSupportInfo(vops, emergency);
        }

        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_0.CellIdentity cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            int i = cellIdentity.cellInfoType;
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i == 5 && cellIdentity.cellIdentityTdscdma.size() == 1) {
                                return new CellIdentityTdscdma((android.hardware.radio.V1_0.CellIdentityTdscdma) cellIdentity.cellIdentityTdscdma.get(0));
                            }
                            return null;
                        } else if (cellIdentity.cellIdentityWcdma.size() == 1) {
                            return new CellIdentityWcdma((android.hardware.radio.V1_0.CellIdentityWcdma) cellIdentity.cellIdentityWcdma.get(0));
                        } else {
                            return null;
                        }
                    } else if (cellIdentity.cellIdentityLte.size() == 1) {
                        return new CellIdentityLte((android.hardware.radio.V1_0.CellIdentityLte) cellIdentity.cellIdentityLte.get(0));
                    } else {
                        return null;
                    }
                } else if (cellIdentity.cellIdentityCdma.size() == 1) {
                    return new CellIdentityCdma((android.hardware.radio.V1_0.CellIdentityCdma) cellIdentity.cellIdentityCdma.get(0));
                } else {
                    return null;
                }
            } else if (cellIdentity.cellIdentityGsm.size() == 1) {
                return new CellIdentityGsm((android.hardware.radio.V1_0.CellIdentityGsm) cellIdentity.cellIdentityGsm.get(0));
            } else {
                return null;
            }
        }

        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_2.CellIdentity cellIdentity) {
            return convertHalCellIdentityToCellIdentity(cellIdentity, null);
        }

        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_2.CellIdentity cellIdentity, CellIdentityNr cellIdentityNr) {
            if (cellIdentity == null) {
                return null;
            }
            switch (cellIdentity.cellInfoType) {
                case 1:
                    if (cellIdentity.cellIdentityGsm.size() == 1) {
                        return new CellIdentityGsm((android.hardware.radio.V1_2.CellIdentityGsm) cellIdentity.cellIdentityGsm.get(0));
                    }
                    return null;
                case 2:
                    if (cellIdentity.cellIdentityCdma.size() == 1) {
                        return new CellIdentityCdma((android.hardware.radio.V1_2.CellIdentityCdma) cellIdentity.cellIdentityCdma.get(0));
                    }
                    return null;
                case 3:
                    if (cellIdentity.cellIdentityLte.size() == 1) {
                        return new CellIdentityLte((android.hardware.radio.V1_2.CellIdentityLte) cellIdentity.cellIdentityLte.get(0));
                    }
                    return null;
                case 4:
                    if (cellIdentity.cellIdentityWcdma.size() == 1) {
                        return new CellIdentityWcdma((android.hardware.radio.V1_2.CellIdentityWcdma) cellIdentity.cellIdentityWcdma.get(0));
                    }
                    return null;
                case 5:
                    if (cellIdentity.cellIdentityTdscdma.size() == 1) {
                        return new CellIdentityTdscdma((android.hardware.radio.V1_2.CellIdentityTdscdma) cellIdentity.cellIdentityTdscdma.get(0));
                    }
                    return null;
                case 6:
                    if (cellIdentityNr != null) {
                        return new android.telephony.CellIdentityNr(cellIdentityNr);
                    }
                    return null;
                default:
                    return null;
            }
        }

        public void requestNetworkRegistrationInfo(int domain, NetworkServiceCallback callback) {
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
                cellularNetworkService.loge("requestNetworkRegistrationInfo invalid domain " + domain);
                callback.onRequestNetworkRegistrationInfoComplete(2, (NetworkRegistrationInfo) null);
            }
        }

        public void close() {
            this.mCallbackMap.clear();
            this.mHandlerThread.quit();
            this.mPhone.mCi.unregisterForNetworkStateChanged(this.mHandler);
        }
    }

    public NetworkService.NetworkServiceProvider onCreateNetworkServiceProvider(int slotIndex) {
        if (SubscriptionManager.isValidSlotIndex(slotIndex) || slotIndex == 2) {
            return new CellularNetworkServiceProvider(slotIndex);
        }
        loge("Tried to Cellular network service with invalid slotId " + slotIndex);
        return null;
    }

    private void log(String s) {
        Rlog.i(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
