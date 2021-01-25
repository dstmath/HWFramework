package com.huawei.internal.telephony;

import com.android.internal.telephony.OperatorInfo;
import java.util.ArrayList;
import java.util.Iterator;

public class OperatorInfoEx {
    private OperatorInfo mOperatorInfo;

    public static OperatorInfoEx makeOperatorInfoEx(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, StateEx state) {
        OperatorInfoEx operatorInfoEx = new OperatorInfoEx();
        operatorInfoEx.setOperatorInfo(new OperatorInfo(operatorAlphaLong, operatorAlphaShort, operatorNumeric, state.getValue()));
        return operatorInfoEx;
    }

    public static OperatorInfoEx makeOperatorInfoEx(String operatorAlphaLong, String operatorAlphaShort, String operatorNumeric, StateEx state, int level) {
        OperatorInfoEx operatorInfoEx = new OperatorInfoEx();
        operatorInfoEx.setOperatorInfo(new OperatorInfo(operatorAlphaLong, operatorAlphaShort, operatorNumeric, state.getValue(), level));
        return operatorInfoEx;
    }

    public static ArrayList<OperatorInfoEx> from(Object object) {
        ArrayList<OperatorInfoEx> operatorInfoExes = new ArrayList<>();
        if ((object instanceof ArrayList) && ((ArrayList) object).size() != 0 && (((ArrayList) object).get(0) instanceof OperatorInfo)) {
            Iterator<OperatorInfo> it = ((ArrayList) object).iterator();
            while (it.hasNext()) {
                OperatorInfoEx operatorInfoEx = new OperatorInfoEx();
                operatorInfoEx.setOperatorInfo(it.next());
                operatorInfoExes.add(operatorInfoEx);
            }
        }
        return operatorInfoExes;
    }

    public static Object convertToOperatorInfo(ArrayList<OperatorInfoEx> operatorInfoExes) {
        if (operatorInfoExes == null) {
            return null;
        }
        ArrayList<OperatorInfo> operatorInfos = new ArrayList<>();
        Iterator<OperatorInfoEx> it = operatorInfoExes.iterator();
        while (it.hasNext()) {
            operatorInfos.add(it.next().getOperatorInfo());
        }
        return operatorInfos;
    }

    public OperatorInfo getOperatorInfo() {
        return this.mOperatorInfo;
    }

    public void setOperatorInfo(OperatorInfo operatorInfo) {
        this.mOperatorInfo = operatorInfo;
    }

    public String getOperatorAlphaLong() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.getOperatorAlphaLong();
        }
        return "";
    }

    public String getOperatorAlphaShort() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.getOperatorAlphaShort();
        }
        return "";
    }

    public String getOperatorNumericWithoutAct() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.getOperatorNumericWithoutAct();
        }
        return "";
    }

    public String getOperatorNumeric() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.getOperatorNumeric();
        }
        return "";
    }

    public StateEx getState() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return StateEx.valueOf(operatorInfo.getState().toString());
        }
        return StateEx.UNKNOWN;
    }

    public int getLevel() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.getLevel();
        }
        return 0;
    }

    public String toString() {
        OperatorInfo operatorInfo = this.mOperatorInfo;
        if (operatorInfo != null) {
            return operatorInfo.toString();
        }
        return "";
    }

    public enum StateEx {
        UNKNOWN(OperatorInfo.State.UNKNOWN),
        AVAILABLE(OperatorInfo.State.AVAILABLE),
        CURRENT(OperatorInfo.State.CURRENT),
        FORBIDDEN(OperatorInfo.State.FORBIDDEN);
        
        private final OperatorInfo.State value;

        private StateEx(OperatorInfo.State value2) {
            this.value = value2;
        }

        public OperatorInfo.State getValue() {
            return this.value;
        }
    }
}
