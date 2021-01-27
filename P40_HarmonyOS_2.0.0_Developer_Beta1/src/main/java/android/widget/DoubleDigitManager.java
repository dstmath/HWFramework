package android.widget;

import android.os.Handler;

class DoubleDigitManager {
    private Integer intermediateDigit;
    private final CallBack mCallBack;
    private final long timeoutInMillis;

    /* access modifiers changed from: package-private */
    public interface CallBack {
        void singleDigitFinal(int i);

        boolean singleDigitIntermediate(int i);

        boolean twoDigitsFinal(int i, int i2);
    }

    public DoubleDigitManager(long timeoutInMillis2, CallBack callBack) {
        this.timeoutInMillis = timeoutInMillis2;
        this.mCallBack = callBack;
    }

    public void reportDigit(int digit) {
        Integer num = this.intermediateDigit;
        if (num == null) {
            this.intermediateDigit = Integer.valueOf(digit);
            new Handler().postDelayed(new Runnable() {
                /* class android.widget.DoubleDigitManager.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (DoubleDigitManager.this.intermediateDigit != null) {
                        DoubleDigitManager.this.mCallBack.singleDigitFinal(DoubleDigitManager.this.intermediateDigit.intValue());
                        DoubleDigitManager.this.intermediateDigit = null;
                    }
                }
            }, this.timeoutInMillis);
            if (!this.mCallBack.singleDigitIntermediate(digit)) {
                this.intermediateDigit = null;
                this.mCallBack.singleDigitFinal(digit);
            }
        } else if (this.mCallBack.twoDigitsFinal(num.intValue(), digit)) {
            this.intermediateDigit = null;
        }
    }
}
