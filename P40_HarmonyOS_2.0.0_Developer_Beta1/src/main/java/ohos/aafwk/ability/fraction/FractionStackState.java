package ohos.aafwk.ability.fraction;

import java.util.Arrays;
import ohos.aafwk.ability.fraction.FractionStack;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

public class FractionStackState {
    private static final LogLabel LABEL = LogLabel.create();
    private int mIndex;
    private String mName;
    private int[] mOps;

    FractionStackState(FractionStack fractionStack) {
        this(new int[(fractionStack.mOps.size() * 2)], fractionStack.mName, fractionStack.mIndex);
        if (fractionStack.mAddToBackStack) {
            int i = 0;
            int i2 = 0;
            while (i < fractionStack.mOps.size()) {
                FractionStack.Op op = fractionStack.mOps.get(i);
                int i3 = i2 + 1;
                this.mOps[i2] = op.cmd;
                int i4 = i3 + 1;
                this.mOps[i3] = op.fraction != null ? op.fraction.mIndex : -1;
                i++;
                i2 = i4;
            }
            return;
        }
        throw new IllegalArgumentException("Not on back stack");
    }

    FractionStackState(int[] iArr, String str, int i) {
        this.mOps = Arrays.copyOf(iArr, iArr.length);
        this.mName = str;
        this.mIndex = i;
    }

    /* access modifiers changed from: package-private */
    public FractionStack init(FractionManager fractionManager) {
        FractionStack fractionStack = new FractionStack(fractionManager);
        int i = 0;
        int i2 = 0;
        while (i < this.mOps.length) {
            FractionStack.Op op = new FractionStack.Op();
            int i3 = i + 1;
            op.cmd = this.mOps[i];
            Log.debug(LABEL, "init op #%{public}s base fraction #%{public}s", String.valueOf(i2), String.valueOf(this.mOps[i3]));
            int i4 = i3 + 1;
            int i5 = this.mOps[i3];
            if (i5 >= 0) {
                op.fraction = fractionManager.mActive.get(Integer.valueOf(i5));
            } else {
                op.fraction = null;
            }
            fractionStack.addOp(op);
            i2++;
            i = i4;
        }
        fractionStack.mName = this.mName;
        fractionStack.mIndex = this.mIndex;
        fractionStack.mAddToBackStack = true;
        fractionStack.bumpBackStackNesting(1);
        return fractionStack;
    }
}
