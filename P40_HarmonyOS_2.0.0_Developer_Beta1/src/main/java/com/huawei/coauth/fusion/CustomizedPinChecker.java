package com.huawei.coauth.fusion;

import android.os.Bundle;
import com.huawei.coauth.utils.LogUtils;

public class CustomizedPinChecker {
    private static final String PHASE_COUNT1 = "PHASE_COUNT1";
    private static final String PHASE_COUNT2 = "PHASE_COUNT2";
    private static final String PHASE_SALT1 = "PHASE_SALT1";
    private static final String PHASE_SALT2 = "PHASE_SALT2";
    private static final String PIN_TYPE = "PIN_TYPE";
    private static final int SALT1_KEY = 1;
    private static final int SALT2_KEY = 2;
    Checker checker;
    int phaseIterationCount1;
    int phaseIterationCount2;
    byte[] phaseSalt1;
    byte[] phaseSalt2;
    PinType pinType;

    public interface Checker {
        boolean check(byte[] bArr);
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setPinType(PinType pinType2) {
        this.pinType = pinType2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setPhaseSalt1(byte[] phaseSalt12) {
        if (phaseSalt12 == null) {
            return this;
        }
        this.phaseSalt1 = (byte[]) phaseSalt12.clone();
        return this;
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setPhaseSalt2(byte[] phaseSalt22) {
        if (phaseSalt22 == null) {
            return this;
        }
        this.phaseSalt2 = (byte[]) phaseSalt22.clone();
        return this;
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setPhaseIterationCount1(int phaseIterationCount12) {
        this.phaseIterationCount1 = phaseIterationCount12;
        return this;
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setPhaseIterationCount2(int phaseIterationCount22) {
        this.phaseIterationCount2 = phaseIterationCount22;
        return this;
    }

    /* access modifiers changed from: package-private */
    public CustomizedPinChecker setChecker(Checker checker2) {
        this.checker = checker2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(PIN_TYPE, this.pinType.getValue());
        safePutByteArray(bundle, PHASE_SALT1, this.phaseSalt1);
        safePutByteArray(bundle, PHASE_SALT2, this.phaseSalt2);
        bundle.putInt(PHASE_COUNT1, this.phaseIterationCount1);
        bundle.putInt(PHASE_COUNT2, this.phaseIterationCount2);
        return bundle;
    }

    private void safePutByteArray(Bundle bundle, String key, byte[] value) {
        if (key != null && value != null) {
            bundle.putByteArray(key, value);
        }
    }

    public enum PinType {
        PIN_NONE(1),
        PIN_ALPHANUMERIC(2),
        PIN_NUMERIC(3),
        PIN_FOUR(4),
        PIN_SIX(5),
        PIN_PATTERN(6);
        
        private final int value;

        private PinType(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }

    private CustomizedPinChecker() {
        this.phaseIterationCount1 = 1;
        this.phaseIterationCount2 = 1;
    }

    public static class Builder {
        private static final String TAG = "CustomizedPinChecker_Builder";
        private Checker checker;
        private int phaseIterationCount1;
        private int phaseIterationCount2;
        private byte[] phaseSalt1;
        private byte[] phaseSalt2;
        private PinType pinType;

        public Builder setPinType(PinType pinType2) {
            this.pinType = pinType2;
            return this;
        }

        public Builder setPhaseSalt(int phase, byte[] salt) {
            if (salt == null) {
                return this;
            }
            if (phase == 1) {
                this.phaseSalt1 = (byte[]) salt.clone();
            } else if (phase == 2) {
                this.phaseSalt2 = (byte[]) salt.clone();
            } else {
                LogUtils.error(TAG, "invalid phase.");
            }
            return this;
        }

        public Builder setPhaseIterationCount(int phase, int count) {
            if (phase == 1) {
                this.phaseIterationCount1 = count;
            } else if (phase == 2) {
                this.phaseIterationCount2 = count;
            } else {
                LogUtils.error(TAG, "invalid phase.");
            }
            return this;
        }

        public Builder setChecker(Checker checker2) {
            this.checker = checker2;
            return this;
        }

        public CustomizedPinChecker build() {
            return new CustomizedPinChecker().setChecker(this.checker).setPhaseSalt1(this.phaseSalt1).setPhaseIterationCount1(this.phaseIterationCount1).setPhaseSalt2(this.phaseSalt2).setPhaseIterationCount2(this.phaseIterationCount2).setPinType(this.pinType).setChecker(this.checker);
        }
    }
}
