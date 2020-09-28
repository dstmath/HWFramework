package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NattKeepalivePacketData;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.os.Message;
import android.telephony.AccessNetworkConstants;
import android.telephony.Rlog;
import android.util.LocalLog;
import android.util.SparseArray;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class DcNetworkAgent extends NetworkAgent {
    private static AtomicInteger sSerialNumber = new AtomicInteger(0);
    public final DcKeepaliveTracker keepaliveTracker = new DcKeepaliveTracker();
    private DataConnection mDataConnection;
    private final LocalLog mNetCapsLocalLog = new LocalLog(10);
    private NetworkCapabilities mNetworkCapabilities;
    private Phone mPhone;
    private String mTag;
    private int mTransportType;

    private DcNetworkAgent(DataConnection dc, String tag, Phone phone, NetworkInfo ni, int score, NetworkMisc misc, int factorySerialNumber, int transportType) {
        super(dc.getHandler().getLooper(), phone.getContext(), tag, ni, dc.getNetworkCapabilities(), dc.getLinkProperties(), score, misc, factorySerialNumber);
        this.mTag = tag;
        this.mPhone = phone;
        this.mNetworkCapabilities = dc.getNetworkCapabilities();
        this.mTransportType = transportType;
        this.mDataConnection = dc;
        logd(tag + " created for data connection " + dc.getName());
    }

    public static DcNetworkAgent createDcNetworkAgent(DataConnection dc, Phone phone, NetworkInfo ni, int score, NetworkMisc misc, int factorySerialNumber, int transportType) {
        return new DcNetworkAgent(dc, "DcNetworkAgent-" + sSerialNumber.incrementAndGet(), phone, ni, score, misc, factorySerialNumber, transportType);
    }

    public synchronized void acquireOwnership(DataConnection dc, int transportType) {
        this.mDataConnection = dc;
        this.mTransportType = transportType;
        logd(dc.getName() + " acquired the ownership of this agent.");
    }

    public synchronized void releaseOwnership(DataConnection dc) {
        if (this.mDataConnection == null) {
            loge("releaseOwnership called on no-owner DcNetworkAgent!");
        } else if (this.mDataConnection != dc) {
            log("releaseOwnership: This agent belongs to " + this.mDataConnection.getName() + ", ignored the request from " + dc.getName());
        } else {
            logd("Data connection " + this.mDataConnection.getName() + " released the ownership.");
            this.mDataConnection = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void unwanted() {
        if (this.mDataConnection == null) {
            loge("Unwanted found called on no-owner DcNetworkAgent!");
            return;
        }
        logd("unwanted called. Now tear down the data connection " + this.mDataConnection.getName());
        if (!this.mPhone.getDcTracker(this.mTransportType).getHwDcTrackerEx().isDataNeededWithWifiAndBt()) {
            log("DcNetworkAgent: [unwanted]: disconnect and no retry it after disconnected");
            this.mDataConnection.tearDownAll(AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT, 2, null);
        } else {
            this.mDataConnection.tearDownAll(PhoneInternalInterface.REASON_RELEASED_BY_CONNECTIVITY_SERVICE, 2, null);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void pollLceData() {
        if (this.mDataConnection == null) {
            loge("pollLceData called on no-owner DcNetworkAgent!");
            return;
        }
        if (this.mPhone.getLceStatus() == 1 && this.mTransportType == 1) {
            this.mPhone.mCi.pullLceData(this.mDataConnection.obtainMessage(262158));
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void networkStatus(int status, String redirectUrl) {
        if (this.mDataConnection == null) {
            loge("networkStatus called on no-owner DcNetworkAgent!");
            return;
        }
        logd("validation status: " + status + " with redirection URL: " + redirectUrl);
        DcTracker dct = this.mPhone.getDcTracker(this.mTransportType);
        if (dct != null) {
            dct.obtainMessage(270380, status, 0, redirectUrl).sendToTarget();
        }
    }

    public synchronized void sendNetworkCapabilities(NetworkCapabilities networkCapabilities, DataConnection dc) {
        if (this.mDataConnection == null) {
            loge("sendNetworkCapabilities called on no-owner DcNetworkAgent!");
        } else if (this.mDataConnection != dc) {
            loge("sendNetworkCapabilities: This agent belongs to " + this.mDataConnection.getName() + ", ignored the request from " + dc.getName());
        } else {
            if (!networkCapabilities.equals(this.mNetworkCapabilities)) {
                String logStr = "Changed from " + this.mNetworkCapabilities + " to " + networkCapabilities + ", Data RAT=" + this.mPhone.getServiceState().getRilDataRadioTechnology() + ", dc=" + this.mDataConnection.getName();
                logd(logStr);
                this.mNetCapsLocalLog.log(logStr);
                this.mNetworkCapabilities = networkCapabilities;
            }
            sendNetworkCapabilities(networkCapabilities);
        }
    }

    public synchronized void sendLinkProperties(LinkProperties linkProperties, DataConnection dc) {
        if (this.mDataConnection == null) {
            loge("sendLinkProperties called on no-owner DcNetworkAgent!");
        } else if (this.mDataConnection != dc) {
            loge("sendLinkProperties: This agent belongs to " + this.mDataConnection.getName() + ", ignored the request from " + dc.getName());
        } else {
            sendLinkProperties(linkProperties);
        }
    }

    public synchronized void sendNetworkScore(int score, DataConnection dc) {
        if (this.mDataConnection == null) {
            loge("sendNetworkScore called on no-owner DcNetworkAgent!");
        } else if (this.mDataConnection != dc) {
            loge("sendNetworkScore: This agent belongs to " + this.mDataConnection.getName() + ", ignored the request from " + dc.getName());
        } else {
            sendNetworkScore(score);
        }
    }

    public synchronized void sendNetworkInfo(NetworkInfo networkInfo, DataConnection dc) {
        if (this.mDataConnection == null) {
            loge("sendNetworkInfo called on no-owner DcNetworkAgent!");
        } else if (this.mDataConnection != dc) {
            loge("sendNetworkInfo: This agent belongs to " + this.mDataConnection.getName() + ", ignored the request from " + dc.getName());
        } else {
            sendNetworkInfo(networkInfo);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void startSocketKeepalive(Message msg) {
        if (this.mDataConnection == null) {
            loge("startSocketKeepalive called on no-owner DcNetworkAgent!");
            return;
        }
        if (msg.obj instanceof NattKeepalivePacketData) {
            this.mDataConnection.obtainMessage(262165, msg.arg1, msg.arg2, msg.obj).sendToTarget();
        } else {
            onSocketKeepaliveEvent(msg.arg1, -30);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void stopSocketKeepalive(Message msg) {
        if (this.mDataConnection == null) {
            loge("stopSocketKeepalive called on no-owner DcNetworkAgent!");
        } else {
            this.mDataConnection.obtainMessage(262166, msg.arg1, msg.arg2, msg.obj).sendToTarget();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DcNetworkAgent: mDataConnection=");
        DataConnection dataConnection = this.mDataConnection;
        sb.append(dataConnection != null ? dataConnection.getName() : null);
        sb.append(" mTransportType=");
        sb.append(AccessNetworkConstants.transportTypeToString(this.mTransportType));
        sb.append(" mNetworkCapabilities=");
        sb.append(this.mNetworkCapabilities);
        return sb.toString();
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
        pw.println(toString());
        pw.increaseIndent();
        pw.println("Net caps logs:");
        this.mNetCapsLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String s) {
        Rlog.i(this.mTag, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(this.mTag, s);
    }

    class DcKeepaliveTracker {
        private final SparseArray<KeepaliveRecord> mKeepalives = new SparseArray<>();

        DcKeepaliveTracker() {
        }

        /* access modifiers changed from: private */
        public class KeepaliveRecord {
            public int currentStatus;
            public int slotId;

            KeepaliveRecord(int slotId2, int status) {
                this.slotId = slotId2;
                this.currentStatus = status;
            }
        }

        /* access modifiers changed from: package-private */
        public int getHandleForSlot(int slotId) {
            for (int i = 0; i < this.mKeepalives.size(); i++) {
                if (this.mKeepalives.valueAt(i).slotId == slotId) {
                    return this.mKeepalives.keyAt(i);
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int keepaliveStatusErrorToPacketKeepaliveError(int error) {
            if (error == 0) {
                return 0;
            }
            if (error == 1) {
                return -30;
            }
            if (error != 2) {
                return -31;
            }
            return -32;
        }

        /* access modifiers changed from: package-private */
        public void handleKeepaliveStarted(int slot, KeepaliveStatus ks) {
            int i = ks.statusCode;
            if (i == 0) {
                DcNetworkAgent.this.onSocketKeepaliveEvent(slot, 0);
            } else if (i == 1) {
                DcNetworkAgent.this.onSocketKeepaliveEvent(slot, keepaliveStatusErrorToPacketKeepaliveError(ks.errorCode));
                return;
            } else if (i != 2) {
                DcNetworkAgent dcNetworkAgent = DcNetworkAgent.this;
                dcNetworkAgent.logd("Invalid KeepaliveStatus Code: " + ks.statusCode);
                return;
            }
            DcNetworkAgent dcNetworkAgent2 = DcNetworkAgent.this;
            dcNetworkAgent2.logd("Adding keepalive handle=" + ks.sessionHandle + " slot = " + slot);
            this.mKeepalives.put(ks.sessionHandle, new KeepaliveRecord(slot, ks.statusCode));
        }

        /* access modifiers changed from: package-private */
        public void handleKeepaliveStatus(KeepaliveStatus ks) {
            KeepaliveRecord kr = this.mKeepalives.get(ks.sessionHandle);
            if (kr == null) {
                DcNetworkAgent dcNetworkAgent = DcNetworkAgent.this;
                dcNetworkAgent.loge("Discarding keepalive event for different data connection:" + ks);
                return;
            }
            int i = kr.currentStatus;
            if (i == 0) {
                int i2 = ks.statusCode;
                if (i2 != 0) {
                    if (i2 == 1) {
                        DcNetworkAgent.this.logd("Keepalive received stopped status!");
                        DcNetworkAgent.this.onSocketKeepaliveEvent(kr.slotId, 0);
                        kr.currentStatus = 1;
                        this.mKeepalives.remove(ks.sessionHandle);
                        return;
                    } else if (i2 != 2) {
                        DcNetworkAgent dcNetworkAgent2 = DcNetworkAgent.this;
                        dcNetworkAgent2.loge("Invalid Keepalive Status received, " + ks.statusCode);
                        return;
                    }
                }
                DcNetworkAgent.this.loge("Active Keepalive received invalid status!");
            } else if (i == 1) {
                DcNetworkAgent.this.logd("Inactive Keepalive received status!");
                DcNetworkAgent.this.onSocketKeepaliveEvent(kr.slotId, -31);
            } else if (i != 2) {
                DcNetworkAgent dcNetworkAgent3 = DcNetworkAgent.this;
                dcNetworkAgent3.loge("Invalid Keepalive Status received, " + kr.currentStatus);
            } else {
                int i3 = ks.statusCode;
                if (i3 == 0) {
                    DcNetworkAgent.this.logd("Pending Keepalive received active status!");
                    kr.currentStatus = 0;
                    DcNetworkAgent.this.onSocketKeepaliveEvent(kr.slotId, 0);
                } else if (i3 == 1) {
                    DcNetworkAgent.this.onSocketKeepaliveEvent(kr.slotId, keepaliveStatusErrorToPacketKeepaliveError(ks.errorCode));
                    kr.currentStatus = 1;
                    this.mKeepalives.remove(ks.sessionHandle);
                } else if (i3 != 2) {
                    DcNetworkAgent dcNetworkAgent4 = DcNetworkAgent.this;
                    dcNetworkAgent4.loge("Invalid Keepalive Status received, " + ks.statusCode);
                } else {
                    DcNetworkAgent.this.loge("Invalid unsolicied Keepalive Pending Status!");
                }
            }
        }
    }
}
