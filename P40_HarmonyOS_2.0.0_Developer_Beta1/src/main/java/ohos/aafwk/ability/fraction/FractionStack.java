package ohos.aafwk.ability.fraction;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.fraction.FractionManager;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

/* access modifiers changed from: package-private */
public final class FractionStack extends FractionScheduler implements FractionManager.OpBuilder {
    private static final LogLabel LABEL = LogLabel.create();
    private static final int OP_ADD = 1;
    private static final int OP_HIDE = 4;
    private static final int OP_NULL = 0;
    private static final int OP_REMOVE = 3;
    private static final int OP_REPLACE = 2;
    private static final int OP_SHOW = 5;
    boolean mAddToBackStack;
    private boolean mCommitted;
    int mIndex = -1;
    private FractionManager mManager;
    String mName;
    List<Op> mOps = new ArrayList();

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler add(int i, Fraction fraction) {
        doAddOp(i, fraction, null, 1);
        return this;
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler add(int i, Fraction fraction, String str) {
        doAddOp(i, fraction, str, 1);
        return this;
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler replace(int i, Fraction fraction) {
        if (i != 0) {
            doAddOp(i, fraction, null, 2);
            return this;
        }
        throw new IllegalArgumentException("Must use non-zero containerComponentId");
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler remove(Fraction fraction) {
        addOp(new Op(3, fraction));
        return this;
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler hide(Fraction fraction) {
        addOp(new Op(4, fraction));
        return this;
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler show(Fraction fraction) {
        addOp(new Op(5, fraction));
        return this;
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public int submit() {
        return commitInternal();
    }

    @Override // ohos.aafwk.ability.fraction.FractionScheduler
    public FractionScheduler pushIntoStack(String str) {
        this.mAddToBackStack = true;
        this.mName = str;
        return this;
    }

    private int commitInternal() {
        if (!this.mCommitted) {
            Log.debug(LABEL, "Commit %{public}s", getClass());
            this.mCommitted = true;
            if (this.mAddToBackStack) {
                this.mIndex = this.mManager.allocBackStackIndex(this);
            } else {
                this.mIndex = -1;
            }
            this.mManager.enqueueOperation(this);
            return this.mIndex;
        }
        throw new IllegalStateException("commit already called");
    }

    @Override // ohos.aafwk.ability.fraction.FractionManager.OpBuilder
    public boolean makeOps(List<FractionStack> list, List<Boolean> list2) {
        list.add(this);
        list2.add(false);
        if (!this.mAddToBackStack) {
            return true;
        }
        this.mManager.handleStackState(this);
        return true;
    }

    /* access modifiers changed from: package-private */
    public static final class Op {
        int cmd;
        Fraction fraction;

        Op() {
            this(0, null);
        }

        Op(int i, Fraction fraction2) {
            this.cmd = i;
            this.fraction = fraction2;
        }
    }

    FractionStack(FractionManager fractionManager) {
        this.mManager = fractionManager;
    }

    private void doAddOp(int i, Fraction fraction, String str, int i2) {
        Class<?> cls = fraction.getClass();
        int modifiers = cls.getModifiers();
        if (cls.isAnonymousClass() || !Modifier.isPublic(modifiers) || (cls.isMemberClass() && !Modifier.isStatic(modifiers))) {
            throw new IllegalStateException("fraction " + cls.getCanonicalName() + " must be a public static class to be  properly recreated from instance state.");
        }
        fraction.mManager = this.mManager;
        if (str != null) {
            if (fraction.mTag == null || str.equals(fraction.mTag)) {
                fraction.mTag = str;
            } else {
                throw new IllegalStateException("Can't change tag of fraction " + fraction + ": was " + fraction.mTag + " now " + str);
            }
        }
        if (i != 0) {
            if (i == -1) {
                throw new IllegalArgumentException("Can't add fraction " + fraction + " with tag to container component with no id");
            } else if (fraction.mFractionId == 0 || fraction.mFractionId == i) {
                fraction.mFractionId = i;
                fraction.mContainerId = i;
            } else {
                throw new IllegalStateException("Can't change container ID of fraction " + fraction + ": was " + fraction.mFractionId + " now " + i);
            }
        }
        addOp(new Op(i2, fraction));
    }

    /* access modifiers changed from: package-private */
    public void addOp(Op op) {
        this.mOps.add(op);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return this.mName;
    }

    /* access modifiers changed from: package-private */
    public void executeOps() {
        int size = this.mOps.size();
        for (int i = 0; i < size; i++) {
            Op op = this.mOps.get(i);
            Fraction fraction = op.fraction;
            int i2 = op.cmd;
            if (i2 == 1) {
                this.mManager.addFraction(fraction, false);
            } else if (i2 == 3) {
                this.mManager.removeFraction(fraction);
            } else if (i2 == 4) {
                this.mManager.hideFraction(fraction);
            } else if (i2 == 5) {
                this.mManager.showFraction(fraction);
            } else {
                throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            }
            if (!(op.cmd == 1 || fraction == null)) {
                this.mManager.moveFractionToExpectedState(fraction);
            }
        }
        FractionManager fractionManager = this.mManager;
        fractionManager.moveToCurState(fractionManager.mCurState, true);
    }

    /* access modifiers changed from: package-private */
    public void executePopOps(boolean z) {
        for (int size = this.mOps.size() - 1; size >= 0; size--) {
            Op op = this.mOps.get(size);
            Fraction fraction = op.fraction;
            int i = op.cmd;
            if (i == 1) {
                this.mManager.removeFraction(fraction);
            } else if (i == 3) {
                this.mManager.addFraction(fraction, false);
            } else if (i == 4) {
                this.mManager.showFraction(fraction);
            } else if (i == 5) {
                this.mManager.hideFraction(fraction);
            } else {
                throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            }
            if (!(op.cmd == 3 || fraction == null)) {
                this.mManager.moveFractionToExpectedState(fraction);
            }
        }
        if (z) {
            FractionManager fractionManager = this.mManager;
            fractionManager.moveToCurState(fractionManager.mCurState, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void optimizeOps(List<Fraction> list) {
        int i = 0;
        while (i < this.mOps.size()) {
            Op op = this.mOps.get(i);
            int i2 = op.cmd;
            if (i2 == 1) {
                list.add(op.fraction);
            } else if (i2 == 2) {
                Fraction fraction = op.fraction;
                int i3 = fraction.mContainerId;
                int i4 = i;
                boolean z = false;
                for (int size = list.size() - 1; size >= 0; size--) {
                    Fraction fraction2 = list.get(size);
                    if (fraction2.mContainerId == i3) {
                        if (fraction2.equals(fraction)) {
                            z = true;
                        } else {
                            this.mOps.add(i4, new Op(3, fraction2));
                            list.remove(fraction2);
                            i4++;
                        }
                    }
                }
                if (z) {
                    this.mOps.remove(i4);
                    i = i4 - 1;
                } else {
                    op.cmd = 1;
                    list.add(fraction);
                    i = i4;
                }
            } else if (i2 == 3) {
                list.remove(op.fraction);
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public void trackAddedFractionsInPop(List<Fraction> list) {
        for (int i = 0; i < this.mOps.size(); i++) {
            Op op = this.mOps.get(i);
            int i2 = op.cmd;
            if (i2 == 1) {
                list.remove(op.fraction);
            } else if (i2 == 3) {
                list.add(op.fraction);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void bumpBackStackNesting(int i) {
        if (this.mAddToBackStack) {
            Log.debug(LABEL, "Bump nesting in %{public}s by %{public}d", getClass(), Integer.valueOf(i));
            int size = this.mOps.size();
            for (int i2 = 0; i2 < size; i2++) {
                Op op = this.mOps.get(i2);
                if (op.fraction != null) {
                    op.fraction.mBackStackNesting += i;
                    Log.debug(LABEL, "Bump nesting of %{public}s to %{public}s", op.fraction, Integer.valueOf(op.fraction.mBackStackNesting));
                }
            }
        }
    }
}
