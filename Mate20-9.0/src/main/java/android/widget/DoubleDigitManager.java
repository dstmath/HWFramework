package android.widget;

import android.os.Handler;

class DoubleDigitManager {
    /* access modifiers changed from: private */
    public Integer intermediateDigit;
    /* access modifiers changed from: private */
    public final CallBack mCallBack;
    private final long timeoutInMillis;

    interface CallBack {
        void singleDigitFinal(int i);

        boolean singleDigitIntermediate(int i);

        boolean twoDigitsFinal(int i, int i2);
    }

    public DoubleDigitManager(long timeoutInMillis2, CallBack callBack) {
        this.timeoutInMillis = timeoutInMillis2;
        this.mCallBack = callBack;
    }

    public void reportDigit(int digit) {
        if (this.intermediateDigit == null) {
            this.intermediateDigit = Integer.valueOf(digit);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (DoubleDigitManager.this.intermediateDigit != null) {
                        DoubleDigitManager.this.mCallBack.singleDigitFinal(DoubleDigitManager.this.intermediateDigit.intValue());
                        Integer unused = DoubleDigitManager.this.intermediateDigit = null;
                    }
                }
            }, this.timeoutInMillis);
            if (!this.mCallBack.singleDigitIntermediate(digit)) {
                this.intermediateDigit = null;
                this.mCallBack.singleDigitFinal(digit);
            }
        } else if (this.mCallBack.twoDigitsFinal(this.intermediateDigit.intValue(), digit)) {
            this.intermediateDigit = null;
        }
    }
}
