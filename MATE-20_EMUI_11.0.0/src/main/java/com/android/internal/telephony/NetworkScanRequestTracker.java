package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.LocationAccessPolicy;
import android.telephony.NetworkScanRequest;
import android.telephony.RadioAccessSpecifier;
import android.telephony.SubscriptionInfo;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.util.FunctionalUtils;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NetworkScanRequestTracker {
    private static final int CMD_INTERRUPT_NETWORK_SCAN = 6;
    private static final int CMD_START_NETWORK_SCAN = 1;
    private static final int CMD_STOP_NETWORK_SCAN = 4;
    private static final int EVENT_INTERRUPT_NETWORK_SCAN_DONE = 7;
    private static final int EVENT_RECEIVE_NETWORK_SCAN_RESULT = 3;
    private static final int EVENT_START_NETWORK_SCAN_DONE = 2;
    private static final int EVENT_STOP_NETWORK_SCAN_DONE = 5;
    private static final String TAG = "ScanRequestTracker";
    private final Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.NetworkScanRequestTracker.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    NetworkScanRequestTracker.this.mScheduler.doStartScan((NetworkScanRequestInfo) msg.obj);
                    return;
                case 2:
                    NetworkScanRequestTracker.this.mScheduler.startScanDone((AsyncResult) msg.obj);
                    return;
                case 3:
                    NetworkScanRequestTracker.this.mScheduler.receiveResult((AsyncResult) msg.obj);
                    return;
                case 4:
                    NetworkScanRequestTracker.this.mScheduler.doStopScan(msg.arg1);
                    return;
                case 5:
                    NetworkScanRequestTracker.this.mScheduler.stopScanDone((AsyncResult) msg.obj);
                    return;
                case 6:
                    NetworkScanRequestTracker.this.mScheduler.doInterruptScan(msg.arg1);
                    return;
                case 7:
                    NetworkScanRequestTracker.this.mScheduler.interruptScanDone((AsyncResult) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private final AtomicInteger mNextNetworkScanRequestId = new AtomicInteger(1);
    private final NetworkScanRequestScheduler mScheduler = new NetworkScanRequestScheduler();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logEmptyResultOrException(AsyncResult ar) {
        if (ar.result == null) {
            Log.e(TAG, "NetworkScanResult: Empty result");
            return;
        }
        Log.e(TAG, "NetworkScanResult: Exception: " + ar.exception);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidScan(NetworkScanRequestInfo nsri) {
        if (nsri.mRequest == null || nsri.mRequest.getSpecifiers() == null || nsri.mRequest.getSpecifiers().length > 8) {
            return false;
        }
        RadioAccessSpecifier[] specifiers = nsri.mRequest.getSpecifiers();
        for (RadioAccessSpecifier ras : specifiers) {
            if (!(ras.getRadioAccessNetwork() == 1 || ras.getRadioAccessNetwork() == 2 || ras.getRadioAccessNetwork() == 3)) {
                return false;
            }
            if (ras.getBands() != null && ras.getBands().length > 8) {
                return false;
            }
            if (ras.getChannels() != null && ras.getChannels().length > 32) {
                return false;
            }
        }
        if (nsri.mRequest.getSearchPeriodicity() < 5 || nsri.mRequest.getSearchPeriodicity() > 300 || nsri.mRequest.getMaxSearchTime() < 60 || nsri.mRequest.getMaxSearchTime() > 3600 || nsri.mRequest.getIncrementalResultsPeriodicity() < 1 || nsri.mRequest.getIncrementalResultsPeriodicity() > 10 || nsri.mRequest.getSearchPeriodicity() > nsri.mRequest.getMaxSearchTime() || nsri.mRequest.getIncrementalResultsPeriodicity() > nsri.mRequest.getMaxSearchTime()) {
            return false;
        }
        if (nsri.mRequest.getPlmns() == null || nsri.mRequest.getPlmns().size() <= 20) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean doesCellInfoCorrespondToKnownMccMnc(CellInfo ci, Collection<String> knownMccMncs) {
        return knownMccMncs.contains(ci.getCellIdentity().getMccString() + ci.getCellIdentity().getMncString());
    }

    public static Set<String> getAllowedMccMncsForLocationRestrictedScan(Context context) {
        return (Set) Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingSupplier(context) {
            /* class com.android.internal.telephony.$$Lambda$NetworkScanRequestTracker$kZrcpK3Cd6BRM_xQpUxJvEcU4 */
            private final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final Object getOrThrow() {
                return NetworkScanRequestTracker.lambda$getAllowedMccMncsForLocationRestrictedScan$0(this.f$0);
            }
        });
    }

    static /* synthetic */ Set lambda$getAllowedMccMncsForLocationRestrictedScan$0(Context context) throws Exception {
        return (Set) SubscriptionController.getInstance().getAvailableSubscriptionInfoList(context.getOpPackageName()).stream().flatMap($$Lambda$NetworkScanRequestTracker$ElkGiXq_pSMxogeu8FScyf5E2jg.INSTANCE).collect(Collectors.toSet());
    }

    /* access modifiers changed from: private */
    public static Stream<String> getAllowableMccMncsFromSubscriptionInfo(SubscriptionInfo info) {
        Stream<String> plmns = Stream.of((Object[]) new List[]{info.getEhplmns(), info.getHplmns()}).flatMap($$Lambda$seyL25CSW2NInOydsTbSDrNW6pM.INSTANCE);
        if (info.getMccString() == null || info.getMncString() == null) {
            return plmns;
        }
        return Stream.concat(plmns, Stream.of(info.getMccString() + info.getMncString()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyMessenger(NetworkScanRequestInfo nsri, int what, int err, List<CellInfo> result) {
        Messenger messenger = nsri.mMessenger;
        Message message = Message.obtain();
        message.what = what;
        message.arg1 = err;
        message.arg2 = nsri.mScanId;
        if (result != null) {
            if (what == 4) {
                result = (List) result.stream().map($$Lambda$OXXtpNvVeJw7E7y9hLioSYgFy9A.INSTANCE).filter(new Predicate(getAllowedMccMncsForLocationRestrictedScan(nsri.mPhone.getContext())) {
                    /* class com.android.internal.telephony.$$Lambda$NetworkScanRequestTracker$3p0zlHLjJ9t4MD0JtdOXxknarc */
                    private final /* synthetic */ Set f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return NetworkScanRequestTracker.doesCellInfoCorrespondToKnownMccMnc((CellInfo) obj, this.f$0);
                    }
                }).collect(Collectors.toList());
            }
            Bundle b = new Bundle();
            b.putParcelableArray("scanResult", (CellInfo[]) result.toArray(new CellInfo[result.size()]));
            message.setData(b);
        } else {
            message.obj = null;
        }
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in notifyMessenger: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public class NetworkScanRequestInfo implements IBinder.DeathRecipient {
        private final IBinder mBinder;
        private final String mCallingPackage;
        private boolean mIsBinderDead = false;
        private final Messenger mMessenger;
        private final Phone mPhone;
        private final int mPid;
        private final NetworkScanRequest mRequest;
        private final int mScanId;
        private final int mUid;

        NetworkScanRequestInfo(NetworkScanRequest r, Messenger m, IBinder b, int id, Phone phone, int callingUid, int callingPid, String callingPackage) {
            this.mRequest = r;
            this.mMessenger = m;
            this.mBinder = b;
            this.mScanId = id;
            this.mPhone = phone;
            this.mUid = callingUid;
            this.mPid = callingPid;
            this.mCallingPackage = callingPackage;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void setIsBinderDead(boolean val) {
            this.mIsBinderDead = val;
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean getIsBinderDead() {
            return this.mIsBinderDead;
        }

        /* access modifiers changed from: package-private */
        public NetworkScanRequest getRequest() {
            return this.mRequest;
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeathRecipient() {
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.e(NetworkScanRequestTracker.TAG, "PhoneInterfaceManager NetworkScanRequestInfo binderDied(" + this.mRequest + ", " + this.mBinder + ")");
            setIsBinderDead(true);
            NetworkScanRequestTracker.this.interruptNetworkScan(this.mScanId);
        }
    }

    /* access modifiers changed from: private */
    public class NetworkScanRequestScheduler {
        private NetworkScanRequestInfo mLiveRequestInfo;
        private NetworkScanRequestInfo mPendingRequestInfo;

        private NetworkScanRequestScheduler() {
        }

        private int rilErrorToScanError(int rilError) {
            if (rilError == 0) {
                return 0;
            }
            if (rilError == 1) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: RADIO_NOT_AVAILABLE");
                return 1;
            } else if (rilError == 6) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: REQUEST_NOT_SUPPORTED");
                return 4;
            } else if (rilError == 40) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: MODEM_ERR");
                return 1;
            } else if (rilError == 44) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: INVALID_ARGUMENTS");
                return 2;
            } else if (rilError == 54) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: OPERATION_NOT_ALLOWED");
                return 1;
            } else if (rilError == 64) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: DEVICE_IN_USE");
                return 3;
            } else if (rilError == 37) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: NO_MEMORY");
                return 1;
            } else if (rilError != 38) {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: Unexpected RadioError " + rilError);
                return 10000;
            } else {
                Log.e(NetworkScanRequestTracker.TAG, "rilErrorToScanError: INTERNAL_ERR");
                return 1;
            }
        }

        private int commandExceptionErrorToScanError(CommandException.Error error) {
            switch (error) {
                case RADIO_NOT_AVAILABLE:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: RADIO_NOT_AVAILABLE");
                    return 1;
                case REQUEST_NOT_SUPPORTED:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: REQUEST_NOT_SUPPORTED");
                    return 4;
                case NO_MEMORY:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: NO_MEMORY");
                    return 1;
                case INTERNAL_ERR:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: INTERNAL_ERR");
                    return 1;
                case MODEM_ERR:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: MODEM_ERR");
                    return 1;
                case OPERATION_NOT_ALLOWED:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: OPERATION_NOT_ALLOWED");
                    return 1;
                case INVALID_ARGUMENTS:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: INVALID_ARGUMENTS");
                    return 2;
                case DEVICE_IN_USE:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: DEVICE_IN_USE");
                    return 3;
                default:
                    Log.e(NetworkScanRequestTracker.TAG, "commandExceptionErrorToScanError: Unexpected CommandExceptionError " + error);
                    return 10000;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void doStartScan(NetworkScanRequestInfo nsri) {
            if (nsri == null) {
                Log.e(NetworkScanRequestTracker.TAG, "CMD_START_NETWORK_SCAN: nsri is null");
            } else if (!NetworkScanRequestTracker.this.isValidScan(nsri)) {
                NetworkScanRequestTracker.this.notifyMessenger(nsri, 2, 2, null);
            } else if (nsri.getIsBinderDead()) {
                Log.e(NetworkScanRequestTracker.TAG, "CMD_START_NETWORK_SCAN: Binder has died");
            } else if (!startNewScan(nsri) && !interruptLiveScan(nsri) && !cacheScan(nsri)) {
                NetworkScanRequestTracker.this.notifyMessenger(nsri, 2, 3, null);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void startScanDone(AsyncResult ar) {
            NetworkScanRequestInfo nsri = (NetworkScanRequestInfo) ar.userObj;
            if (nsri == null) {
                Log.e(NetworkScanRequestTracker.TAG, "EVENT_START_NETWORK_SCAN_DONE: nsri is null");
            } else if (this.mLiveRequestInfo == null || nsri.mScanId != this.mLiveRequestInfo.mScanId) {
                Log.e(NetworkScanRequestTracker.TAG, "EVENT_START_NETWORK_SCAN_DONE: nsri does not match mLiveRequestInfo");
            } else {
                if (ar.exception != null || ar.result == null) {
                    NetworkScanRequestTracker.this.logEmptyResultOrException(ar);
                    if (ar.exception != null) {
                        deleteScanAndMayNotify(nsri, commandExceptionErrorToScanError(((CommandException) ar.exception).getCommandError()), true);
                    } else {
                        Log.wtf(NetworkScanRequestTracker.TAG, "EVENT_START_NETWORK_SCAN_DONE: ar.exception can not be null!");
                    }
                } else {
                    nsri.mPhone.mCi.registerForNetworkScanResult(NetworkScanRequestTracker.this.mHandler, 3, nsri);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void receiveResult(AsyncResult ar) {
            int notifyMsg;
            NetworkScanRequestInfo nsri = (NetworkScanRequestInfo) ar.userObj;
            if (nsri == null) {
                Log.e(NetworkScanRequestTracker.TAG, "EVENT_RECEIVE_NETWORK_SCAN_RESULT: nsri is null");
                return;
            }
            LocationAccessPolicy.LocationPermissionQuery locationQuery = new LocationAccessPolicy.LocationPermissionQuery.Builder().setCallingPackage(nsri.mCallingPackage).setCallingPid(nsri.mPid).setCallingUid(nsri.mUid).setMinSdkVersionForFine(29).setMethod("NetworkScanTracker#onResult").build();
            if (ar.exception != null || ar.result == null) {
                NetworkScanRequestTracker.this.logEmptyResultOrException(ar);
                deleteScanAndMayNotify(nsri, 10000, true);
                nsri.mPhone.mCi.unregisterForNetworkScanResult(NetworkScanRequestTracker.this.mHandler);
                return;
            }
            NetworkScanResult nsr = (NetworkScanResult) ar.result;
            if (LocationAccessPolicy.checkLocationPermission(nsri.mPhone.getContext(), locationQuery) == LocationAccessPolicy.LocationPermissionResult.ALLOWED) {
                notifyMsg = 1;
            } else {
                notifyMsg = 4;
            }
            if (nsr.scanError == 0) {
                if (nsri.mPhone.getServiceStateTracker() != null) {
                    nsri.mPhone.getServiceStateTracker().updateOperatorNameForCellInfo(nsr.networkInfos);
                }
                NetworkScanRequestTracker.this.notifyMessenger(nsri, notifyMsg, rilErrorToScanError(nsr.scanError), nsr.networkInfos);
                if (nsr.scanStatus == 2) {
                    deleteScanAndMayNotify(nsri, 0, true);
                    nsri.mPhone.mCi.unregisterForNetworkScanResult(NetworkScanRequestTracker.this.mHandler);
                    return;
                }
                return;
            }
            if (nsr.networkInfos != null) {
                NetworkScanRequestTracker.this.notifyMessenger(nsri, notifyMsg, rilErrorToScanError(nsr.scanError), nsr.networkInfos);
            }
            deleteScanAndMayNotify(nsri, rilErrorToScanError(nsr.scanError), true);
            nsri.mPhone.mCi.unregisterForNetworkScanResult(NetworkScanRequestTracker.this.mHandler);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void doStopScan(int scanId) {
            if (this.mLiveRequestInfo != null && scanId == this.mLiveRequestInfo.mScanId) {
                this.mLiveRequestInfo.mPhone.stopNetworkScan(NetworkScanRequestTracker.this.mHandler.obtainMessage(5, this.mLiveRequestInfo));
            } else if (this.mPendingRequestInfo == null || scanId != this.mPendingRequestInfo.mScanId) {
                Log.e(NetworkScanRequestTracker.TAG, "stopScan: scan " + scanId + " does not exist!");
            } else {
                NetworkScanRequestTracker.this.notifyMessenger(this.mPendingRequestInfo, 3, 0, null);
                this.mPendingRequestInfo = null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopScanDone(AsyncResult ar) {
            NetworkScanRequestInfo nsri = (NetworkScanRequestInfo) ar.userObj;
            if (nsri == null) {
                Log.e(NetworkScanRequestTracker.TAG, "EVENT_STOP_NETWORK_SCAN_DONE: nsri is null");
                return;
            }
            if (ar.exception != null || ar.result == null) {
                NetworkScanRequestTracker.this.logEmptyResultOrException(ar);
                if (ar.exception != null) {
                    deleteScanAndMayNotify(nsri, commandExceptionErrorToScanError(((CommandException) ar.exception).getCommandError()), true);
                } else {
                    Log.wtf(NetworkScanRequestTracker.TAG, "EVENT_STOP_NETWORK_SCAN_DONE: ar.exception can not be null!");
                }
            } else {
                deleteScanAndMayNotify(nsri, 0, true);
            }
            nsri.mPhone.mCi.unregisterForNetworkScanResult(NetworkScanRequestTracker.this.mHandler);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void doInterruptScan(int scanId) {
            if (this.mLiveRequestInfo == null || scanId != this.mLiveRequestInfo.mScanId) {
                Log.e(NetworkScanRequestTracker.TAG, "doInterruptScan: scan " + scanId + " does not exist!");
            } else {
                this.mLiveRequestInfo.mPhone.stopNetworkScan(NetworkScanRequestTracker.this.mHandler.obtainMessage(7, this.mLiveRequestInfo));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void interruptScanDone(AsyncResult ar) {
            NetworkScanRequestInfo nsri = (NetworkScanRequestInfo) ar.userObj;
            if (nsri == null) {
                Log.e(NetworkScanRequestTracker.TAG, "EVENT_INTERRUPT_NETWORK_SCAN_DONE: nsri is null");
                return;
            }
            nsri.mPhone.mCi.unregisterForNetworkScanResult(NetworkScanRequestTracker.this.mHandler);
            deleteScanAndMayNotify(nsri, 0, false);
        }

        private synchronized boolean interruptLiveScan(NetworkScanRequestInfo nsri) {
            if (this.mLiveRequestInfo == null || this.mPendingRequestInfo != null || nsri.mUid != 1001 || this.mLiveRequestInfo.mUid == 1001) {
                return false;
            }
            doInterruptScan(this.mLiveRequestInfo.mScanId);
            this.mPendingRequestInfo = nsri;
            NetworkScanRequestTracker.this.notifyMessenger(this.mLiveRequestInfo, 2, 10002, null);
            return true;
        }

        private boolean cacheScan(NetworkScanRequestInfo nsri) {
            return false;
        }

        private synchronized boolean startNewScan(NetworkScanRequestInfo nsri) {
            if (this.mLiveRequestInfo != null) {
                return false;
            }
            this.mLiveRequestInfo = nsri;
            nsri.mPhone.startNetworkScan(nsri.getRequest(), NetworkScanRequestTracker.this.mHandler.obtainMessage(2, nsri));
            return true;
        }

        private synchronized void deleteScanAndMayNotify(NetworkScanRequestInfo nsri, int error, boolean notify) {
            if (this.mLiveRequestInfo != null && nsri.mScanId == this.mLiveRequestInfo.mScanId) {
                if (notify) {
                    if (error == 0) {
                        NetworkScanRequestTracker.this.notifyMessenger(nsri, 3, error, null);
                    } else {
                        NetworkScanRequestTracker.this.notifyMessenger(nsri, 2, error, null);
                    }
                }
                this.mLiveRequestInfo = null;
                if (this.mPendingRequestInfo != null) {
                    startNewScan(this.mPendingRequestInfo);
                    this.mPendingRequestInfo = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void interruptNetworkScan(int scanId) {
        this.mHandler.obtainMessage(6, scanId, 0).sendToTarget();
    }

    public int startNetworkScan(NetworkScanRequest request, Messenger messenger, IBinder binder, Phone phone, int callingUid, int callingPid, String callingPackage) {
        int scanId = this.mNextNetworkScanRequestId.getAndIncrement();
        this.mHandler.obtainMessage(1, new NetworkScanRequestInfo(request, messenger, binder, scanId, phone, callingUid, callingPid, callingPackage)).sendToTarget();
        return scanId;
    }

    public void stopNetworkScan(int scanId, int callingUid) {
        synchronized (this.mScheduler) {
            if ((this.mScheduler.mLiveRequestInfo != null && scanId == this.mScheduler.mLiveRequestInfo.mScanId && callingUid == this.mScheduler.mLiveRequestInfo.mUid) || (this.mScheduler.mPendingRequestInfo != null && scanId == this.mScheduler.mPendingRequestInfo.mScanId && callingUid == this.mScheduler.mPendingRequestInfo.mUid)) {
                this.mHandler.obtainMessage(4, scanId, 0).sendToTarget();
            } else {
                throw new IllegalArgumentException("Scan with id: " + scanId + " does not exist!");
            }
        }
    }
}
