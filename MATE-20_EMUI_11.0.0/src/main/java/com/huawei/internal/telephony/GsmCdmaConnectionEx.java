package com.huawei.internal.telephony;

import com.android.internal.telephony.GsmCdmaConnection;
import java.util.ArrayList;
import java.util.List;

public class GsmCdmaConnectionEx {
    GsmCdmaConnection mGsmCdmaConnection;

    public static GsmCdmaConnectionEx from(Object connection) {
        if (!(connection instanceof GsmCdmaConnection)) {
            return null;
        }
        GsmCdmaConnectionEx gsmCdmaConnectionEx = new GsmCdmaConnectionEx();
        gsmCdmaConnectionEx.setGsmCdmaConnection((GsmCdmaConnection) connection);
        return gsmCdmaConnectionEx;
    }

    public static List<GsmCdmaConnectionEx> getGsmCdmaConnectionExList(Object connectionList) {
        List<GsmCdmaConnectionEx> gsmCdmaConnectionExList = new ArrayList<>();
        if (connectionList instanceof List) {
            List conList = (List) connectionList;
            for (int i = 0; i < conList.size(); i++) {
                GsmCdmaConnectionEx gsmCdmaConnectionEx = from(conList.get(i));
                if (gsmCdmaConnectionEx != null) {
                    gsmCdmaConnectionExList.add(gsmCdmaConnectionEx);
                }
            }
        }
        return gsmCdmaConnectionExList;
    }

    public GsmCdmaConnection getGsmCdmaConnection() {
        return this.mGsmCdmaConnection;
    }

    public void setGsmCdmaConnection(GsmCdmaConnection gsmCdmaConnection) {
        this.mGsmCdmaConnection = gsmCdmaConnection;
    }

    public boolean isIncoming() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.isIncoming();
        }
        return false;
    }

    public String getAddress() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.getAddress();
        }
        return null;
    }

    public String getOrigDialString() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.getOrigDialString();
        }
        return null;
    }

    public void onLineControlInfo() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            gsmCdmaConnection.getHwGsmCdmaConnectionEx().onLineControlInfo();
        }
    }

    public boolean hasRevFWIM() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.getHwGsmCdmaConnectionEx().hasRevFWIM();
        }
        return false;
    }

    public boolean isEncryptCall() {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.getHwGsmCdmaConnectionEx().isEncryptCall();
        }
        return false;
    }

    public void setEncryptCall(boolean isEncryptCall) {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            gsmCdmaConnection.getHwGsmCdmaConnectionEx().setEncryptCall(isEncryptCall);
        }
    }

    public boolean compareToNumber(String number) {
        GsmCdmaConnection gsmCdmaConnection = this.mGsmCdmaConnection;
        if (gsmCdmaConnection != null) {
            return gsmCdmaConnection.getHwGsmCdmaConnectionEx().compareToNumber(number);
        }
        return false;
    }
}
