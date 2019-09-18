package com.android.internal.telephony.dataconnection;

import android.hardware.radio.V1_0.SetupDataCallResult;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.telephony.data.DataService;
import android.telephony.data.DataServiceCallback;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellularDataService extends DataService {
    private static final int DATA_CALL_LIST_CHANGED = 6;
    private static final boolean DBG = false;
    private static final int DEACTIVATE_DATA_ALL_COMPLETE = 2;
    private static final int GET_DATA_CALL_LIST_COMPLETE = 5;
    private static final int SETUP_DATA_CALL_COMPLETE = 1;
    private static final int SET_DATA_PROFILE_COMPLETE = 4;
    private static final int SET_INITIAL_ATTACH_APN_COMPLETE = 3;
    private static final String TAG = CellularDataService.class.getSimpleName();

    private class CellularDataServiceProvider extends DataService.DataServiceProvider {
        /* access modifiers changed from: private */
        public final Map<Message, DataServiceCallback> mCallbackMap;
        private final Handler mHandler;
        private final Looper mLooper;
        private final Phone mPhone;

        private CellularDataServiceProvider(int slotId) {
            super(CellularDataService.this, slotId);
            this.mCallbackMap = new HashMap();
            this.mPhone = PhoneFactory.getPhone(getSlotId());
            HandlerThread thread = new HandlerThread(CellularDataService.class.getSimpleName());
            thread.start();
            this.mLooper = thread.getLooper();
            this.mHandler = new Handler(this.mLooper, CellularDataService.this) {
                public void handleMessage(Message message) {
                    List list;
                    DataServiceCallback callback = (DataServiceCallback) CellularDataServiceProvider.this.mCallbackMap.remove(message);
                    AsyncResult ar = (AsyncResult) message.obj;
                    int i = 0;
                    switch (message.what) {
                        case 1:
                            SetupDataCallResult result = (SetupDataCallResult) ar.result;
                            int resultCode = 0;
                            if (ar.exception != null) {
                                if (!(ar.exception instanceof CommandException) || ((CommandException) ar.exception).getCommandError() != CommandException.Error.RADIO_NOT_AVAILABLE) {
                                    resultCode = 1;
                                } else {
                                    resultCode = 4;
                                }
                            }
                            callback.onSetupDataCallComplete(resultCode, CellularDataService.this.convertDataCallResult(result));
                            break;
                        case 2:
                            if (ar.exception != null) {
                                i = 4;
                            }
                            callback.onDeactivateDataCallComplete(i);
                            break;
                        case 3:
                            if (ar.exception != null) {
                                i = 4;
                            }
                            callback.onSetInitialAttachApnComplete(i);
                            break;
                        case 4:
                            if (ar.exception != null) {
                                i = 4;
                            }
                            callback.onSetDataProfileComplete(i);
                            break;
                        case 5:
                            if (ar.exception != null) {
                                i = 4;
                            }
                            if (ar.exception != null) {
                                list = null;
                            } else {
                                list = CellularDataServiceProvider.this.getDataCallList((List<SetupDataCallResult>) (List) ar.result);
                            }
                            callback.onGetDataCallListComplete(i, list);
                            break;
                        case 6:
                            CellularDataServiceProvider.this.notifyDataCallListChanged(CellularDataServiceProvider.this.getDataCallList((List<SetupDataCallResult>) (List) ar.result));
                            break;
                        default:
                            CellularDataService.this.loge("Unexpected event: " + message.what);
                            return;
                    }
                }
            };
            this.mPhone.mCi.registerForDataCallListChanged(this.mHandler, 6, null);
        }

        /* access modifiers changed from: private */
        public List<DataCallResponse> getDataCallList(List<SetupDataCallResult> dcList) {
            List<DataCallResponse> dcResponseList = new ArrayList<>();
            for (SetupDataCallResult dcResult : dcList) {
                dcResponseList.add(CellularDataService.this.convertDataCallResult(dcResult));
            }
            return dcResponseList;
        }

        public void setupDataCall(int radioTechnology, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, DataServiceCallback callback) {
            DataServiceCallback dataServiceCallback = callback;
            Message message = null;
            if (dataServiceCallback != null) {
                message = Message.obtain(this.mHandler, 1);
                this.mCallbackMap.put(message, dataServiceCallback);
            }
            int i = reason;
            if (i == 15) {
                this.mPhone.mCi.setupEIMEDataCall(message);
            } else {
                this.mPhone.mCi.setupDataCall(radioTechnology, dataProfile, isRoaming, allowRoaming, i, linkProperties, message);
            }
        }

        public void deactivateDataCall(int cid, int reason, DataServiceCallback callback) {
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 2);
                this.mCallbackMap.put(message, callback);
            }
            if (reason == 15) {
                this.mPhone.mCi.deactivateEIMEDataCall(message);
            } else {
                this.mPhone.mCi.deactivateDataCall(cid, reason, message);
            }
        }

        public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, DataServiceCallback callback) {
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 3);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.setInitialAttachApn(dataProfile, isRoaming, message);
        }

        public void setDataProfile(List<DataProfile> dps, boolean isRoaming, DataServiceCallback callback) {
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 4);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[dps.size()]), isRoaming, message);
        }

        public void getDataCallList(DataServiceCallback callback) {
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 5);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.getDataCallList(message);
        }
    }

    public DataService.DataServiceProvider createDataServiceProvider(int slotId) {
        log("Cellular data service created for slot " + slotId);
        if (SubscriptionManager.isValidSlotIndex(slotId) || slotId == 2) {
            return new CellularDataServiceProvider(slotId);
        }
        loge("Tried to cellular data service with invalid slotId " + slotId);
        return null;
    }

    @VisibleForTesting
    public DataCallResponse convertDataCallResult(SetupDataCallResult dcResult) {
        LinkAddress la;
        SetupDataCallResult setupDataCallResult = dcResult;
        if (setupDataCallResult == null) {
            return null;
        }
        String[] addresses = null;
        if (!TextUtils.isEmpty(setupDataCallResult.addresses)) {
            addresses = setupDataCallResult.addresses.split("\\s+");
        }
        String[] addresses2 = addresses;
        List<InetAddress> gatewayList = new ArrayList<>();
        if (addresses2 != null) {
            for (String address : addresses2) {
                String address2 = address.trim();
                if (!address2.isEmpty()) {
                    try {
                        if (address2.split("/").length == 2) {
                            la = new LinkAddress(address2);
                        } else {
                            InetAddress ia = NetworkUtils.numericToInetAddress(address2);
                            la = new LinkAddress(ia, ia instanceof Inet4Address ? 32 : 64);
                        }
                        gatewayList.add(la);
                    } catch (IllegalArgumentException e) {
                        loge("Unknown address: " + address2 + ", exception = " + e);
                    }
                }
            }
        }
        String[] dnses = null;
        if (!TextUtils.isEmpty(setupDataCallResult.dnses)) {
            dnses = setupDataCallResult.dnses.split("\\s+");
        }
        String[] dnses2 = dnses;
        ArrayList arrayList = new ArrayList();
        if (dnses2 != null) {
            for (String dns : dnses2) {
                try {
                    arrayList.add(NetworkUtils.numericToInetAddress(dns.trim()));
                } catch (IllegalArgumentException e2) {
                    loge("Unknown dns: " + dns + ", exception = " + e2);
                }
            }
        }
        String[] gateways = null;
        if (!TextUtils.isEmpty(setupDataCallResult.gateways)) {
            gateways = setupDataCallResult.gateways.split("\\s+");
        }
        String[] gateways2 = gateways;
        List<InetAddress> gatewayList2 = new ArrayList<>();
        if (gateways2 != null) {
            for (String gateway : gateways2) {
                try {
                    gatewayList2.add(NetworkUtils.numericToInetAddress(gateway.trim()));
                } catch (IllegalArgumentException e3) {
                    loge("Unknown gateway: " + gateway + ", exception = " + e3);
                }
            }
        }
        String[] strArr = addresses2;
        String[] strArr2 = gateways2;
        ArrayList arrayList2 = arrayList;
        String[] strArr3 = dnses2;
        List<InetAddress> list = gatewayList;
        DataCallResponse dataCallResponse = new DataCallResponse(setupDataCallResult.status, setupDataCallResult.suggestedRetryTime, setupDataCallResult.cid, setupDataCallResult.active, setupDataCallResult.type, setupDataCallResult.ifname, gatewayList, arrayList, gatewayList2, new ArrayList(Arrays.asList(setupDataCallResult.pcscf.trim().split("\\s+"))), setupDataCallResult.mtu);
        return dataCallResponse;
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
