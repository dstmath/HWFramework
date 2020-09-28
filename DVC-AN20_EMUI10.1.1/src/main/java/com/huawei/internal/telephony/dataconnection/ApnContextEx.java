package com.huawei.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;

public class ApnContextEx {
    private ApnContext mApnContext;

    public enum StateEx {
        IDLE(DctConstants.State.IDLE),
        CONNECTING(DctConstants.State.CONNECTING),
        RETRYING(DctConstants.State.RETRYING),
        CONNECTED(DctConstants.State.CONNECTED),
        DISCONNECTING(DctConstants.State.DISCONNECTING),
        FAILED(DctConstants.State.FAILED);
        
        private final DctConstants.State value;

        private StateEx(DctConstants.State value2) {
            this.value = value2;
        }
    }

    public ApnContext getApnContext() {
        return this.mApnContext;
    }

    public void setApnContext(ApnContext apnContext) {
        this.mApnContext = apnContext;
    }

    public static StateEx getStateExFromState(DctConstants.State state) {
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$DctConstants$State[state.ordinal()]) {
            case 1:
                return StateEx.IDLE;
            case 2:
                return StateEx.FAILED;
            case 3:
                return StateEx.RETRYING;
            case 4:
                return StateEx.CONNECTED;
            case 5:
                return StateEx.CONNECTING;
            case 6:
                return StateEx.DISCONNECTING;
            default:
                return null;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.internal.telephony.dataconnection.ApnContextEx$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx = new int[StateEx.values().length];
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.RETRYING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.CONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.CONNECTING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$dataconnection$ApnContextEx$StateEx[StateEx.DISCONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 3;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 5;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    public static DctConstants.State getDctStateFromStateEx(StateEx stateEx) {
        switch (stateEx) {
            case IDLE:
                return DctConstants.State.IDLE;
            case FAILED:
                return DctConstants.State.FAILED;
            case RETRYING:
                return DctConstants.State.RETRYING;
            case CONNECTED:
                return DctConstants.State.CONNECTED;
            case CONNECTING:
                return DctConstants.State.CONNECTING;
            case DISCONNECTING:
                return DctConstants.State.DISCONNECTING;
            default:
                return null;
        }
    }

    public ApnSetting getApnSetting() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getApnSetting();
        }
        return null;
    }

    public StateEx getState() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return getStateExFromState(apnContext.getState());
        }
        return null;
    }

    public String getApnType() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getApnType();
        }
        return null;
    }

    public boolean restartOnError(int errorCode) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.restartOnError(errorCode);
        }
        return false;
    }

    public DataConnectionEx getDataConnection() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext == null) {
            return null;
        }
        DataConnection dc = apnContext.getDataConnection();
        DataConnectionEx dcEx = new DataConnectionEx();
        dcEx.setDataConnection(dc);
        return dcEx;
    }

    public PendingIntent getDisableNrIntent() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getDisableNrIntent();
        }
        return null;
    }

    public void setDisableNrIntent(PendingIntent intent) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setDisableNrIntent(intent);
        }
    }

    public PendingIntent getReenableNrIntent() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getReenableNrIntent();
        }
        return null;
    }

    public void setReenableNrIntent(PendingIntent intent) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setReenableNrIntent(intent);
        }
    }

    public void setDisableNrBundle(Bundle bundle) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setDisableNrBundle(bundle);
        }
    }

    public Bundle getDisableNrBundle() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getDisableNrBundle();
        }
        return null;
    }

    public PendingIntent getReconnectIntent() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getReconnectIntent();
        }
        return null;
    }

    public int getFailCause() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getFailCause();
        }
        return 0;
    }

    public void setFailCause(int failCause) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setFailCause(failCause);
        }
    }

    public void resetApnPermanentFailedFlag() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.resetApnPermanentFailedFlag();
        }
    }

    public void setModemSuggestedDelay(long delay) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setModemSuggestedDelay(delay);
        }
    }

    public long getModemSuggestedDelay() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getModemSuggestedDelay();
        }
        return 0;
    }

    public String getReason() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getReason();
        }
        return null;
    }

    public boolean isIpv6Connected() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext == null || apnContext.getDataConnection() == null) {
            return false;
        }
        return this.mApnContext.getDataConnection().isIpv6Connected();
    }

    public boolean isIpv4Connected() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext == null || apnContext.getDataConnection() == null) {
            return false;
        }
        return this.mApnContext.getDataConnection().isIpv4Connected();
    }

    public boolean isDisconnected() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.isDisconnected();
        }
        return true;
    }

    public String getDnn() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getDnn();
        }
        return null;
    }

    public int getPduSessionType() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getPduSessionType();
        }
        return -1;
    }

    public String getSnssai() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getSnssai();
        }
        return null;
    }

    public byte getSscMode() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getSscMode();
        }
        return 0;
    }

    public Context getContext() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getContext();
        }
        return null;
    }

    public void setSscMode(byte sscMode) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setSscMode(sscMode);
        }
    }

    public void setSnssai(String snssai) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setSnssai(snssai);
        }
    }

    public void setDnn(String dnn) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setDnn(dnn);
        }
    }

    public void setPduSessionType(int pduSessionType) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setPduSessionType(pduSessionType);
        }
    }

    public void setRouteBitmap(byte routeBitmap) {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            apnContext.setRouteBitmap(routeBitmap);
        }
    }

    public byte getRouteBitmap() {
        ApnContext apnContext = this.mApnContext;
        if (apnContext != null) {
            return apnContext.getRouteBitmap();
        }
        return 0;
    }
}
