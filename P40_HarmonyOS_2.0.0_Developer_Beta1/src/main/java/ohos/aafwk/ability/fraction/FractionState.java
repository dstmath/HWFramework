package ohos.aafwk.ability.fraction;

import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.app.Context;
import ohos.utils.PacMap;

/* access modifiers changed from: package-private */
public final class FractionState {
    private static final LogLabel LABEL = LogLabel.create();
    private PacMap mArguments;
    private String mClassName;
    private int mContainerId;
    private int mFractionId;
    private boolean mFromLayout;
    private boolean mHidden;
    private int mIndex;
    Fraction mInstance;
    PacMap mSavedFractionData;
    private String mTag;

    FractionState(Fraction fraction) {
        this(fraction.getClass().getName(), fraction.mFractionId, fraction.mContainerId, fraction.mFromLayout, fraction.mIndex, fraction.mTag, fraction.mArguments, fraction.mHidden, null);
    }

    FractionState(String str, int i, int i2, boolean z, int i3, String str2, PacMap pacMap, boolean z2, PacMap pacMap2) {
        this.mClassName = str;
        this.mFractionId = i;
        this.mContainerId = i2;
        this.mFromLayout = z;
        this.mIndex = i3;
        this.mTag = str2;
        this.mArguments = pacMap;
        this.mHidden = z2;
        this.mSavedFractionData = pacMap2;
    }

    /* access modifiers changed from: package-private */
    public Fraction init(FractionAbility fractionAbility, FractionManager fractionManager) {
        if (this.mInstance == null) {
            if (fractionAbility != null) {
                Context context = fractionAbility.getContext();
                PacMap pacMap = this.mArguments;
                if (pacMap != null) {
                    pacMap.setClassLoader(context.getClassloader());
                    this.mInstance = fractionAbility.init(context, this.mClassName, this.mArguments);
                }
                PacMap pacMap2 = this.mSavedFractionData;
                if (pacMap2 != null) {
                    pacMap2.setClassLoader(context.getClassloader());
                    this.mInstance.mSavedFractionData = this.mSavedFractionData;
                }
                Fraction fraction = this.mInstance;
                if (fraction != null) {
                    fraction.setIndex(this.mIndex);
                    Fraction fraction2 = this.mInstance;
                    fraction2.mFromLayout = this.mFromLayout;
                    fraction2.mFractionId = this.mFractionId;
                    fraction2.mContainerId = this.mContainerId;
                    fraction2.mTag = this.mTag;
                    fraction2.mHidden = this.mHidden;
                    fraction2.mManager = fractionManager;
                    Log.debug(LABEL, "init fraction %{public}s", fraction2);
                }
            } else {
                throw new IllegalStateException("ability is null, nothing can be done.");
            }
        }
        return this.mInstance;
    }
}
