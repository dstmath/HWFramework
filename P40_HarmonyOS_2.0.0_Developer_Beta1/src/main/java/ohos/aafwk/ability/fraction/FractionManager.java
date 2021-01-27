package ohos.aafwk.ability.fraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ohos.aafwk.ability.fraction.FractionStack;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.utils.PacMap;
import ohos.utils.Sequenceable;

public class FractionManager {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String COMPONENT_STATE_TAG = "harmony:component_state";
    private static final LogLabel LABEL = LogLabel.create();
    private static final int POP_FROM_STACK_INCLUSIVE = 1;
    private FractionAbility mAbility;
    Map<Integer, Fraction> mActive = new HashMap();
    private final List<Fraction> mAdded = new ArrayList();
    private List<Fraction> mAddedFractionSnap;
    private List<Integer> mAvailBackStackIndices;
    private List<FractionStack> mBackStackIndices;
    int mCurState = 0;
    private boolean mDataSaved;
    private List<FractionStack> mEvents;
    private boolean mExecutingOperations;
    private ArrayList<FractionStack> mFractionStack;
    private List<Boolean> mIsEventPop;
    private int mNextFractionIndex = 0;
    private List<OpBuilder> mPendingOperations;
    private ArrayList<Sequenceable> mStateArray = null;
    private PacMap mStatePac = null;
    private boolean mStopped;

    /* access modifiers changed from: package-private */
    public interface OpBuilder {
        boolean makeOps(List<FractionStack> list, List<Boolean> list2);
    }

    FractionManager() {
    }

    public FractionScheduler startFractionScheduler() {
        return new FractionStack(this);
    }

    /* access modifiers changed from: package-private */
    public boolean popStack() {
        execPendingOperations();
        boolean popStackState = popStackState(this.mEvents, this.mIsEventPop, null, -1, 0);
        Log.debug(LABEL, "popFromStack result state is %{public}s", Boolean.valueOf(popStackState));
        if (popStackState) {
            this.mExecutingOperations = true;
            try {
                optimizeOperationsAndExecute(this.mEvents, this.mIsEventPop);
            } finally {
                execClear();
            }
        }
        burpActive();
        return popStackState;
    }

    public void popFromStack() {
        popStack();
    }

    public void popFromStack(String str, int i) {
        enqueueOperation(new PopStackState(str, -1, i));
    }

    /* access modifiers changed from: package-private */
    public void handleStackState(FractionStack fractionStack) {
        if (this.mFractionStack == null) {
            this.mFractionStack = new ArrayList<>();
        }
        this.mFractionStack.add(fractionStack);
    }

    public Optional<Fraction> getFractionByTag(String str) {
        Log.debug(LABEL, "mAdded size is %{public}s, mActive size is %{public}s", Integer.valueOf(this.mAdded.size()), Integer.valueOf(this.mActive.size()));
        if (str != null) {
            for (int size = this.mAdded.size() - 1; size >= 0; size--) {
                Fraction fraction = this.mAdded.get(size);
                if (fraction != null && str.equals(fraction.mTag)) {
                    return Optional.of(fraction);
                }
            }
        }
        Map<Integer, Fraction> map = this.mActive;
        if (!(map == null || str == null)) {
            for (Map.Entry<Integer, Fraction> entry : map.entrySet()) {
                Fraction value = entry.getValue();
                if (value != null && str.equals(value.mTag)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Object> getFractionById(int i) {
        Log.debug(LABEL, "mAdded size is %{public}s, mActive size is %{public}s", Integer.valueOf(this.mAdded.size()), Integer.valueOf(this.mActive.size()));
        for (int size = this.mAdded.size() - 1; size >= 0; size--) {
            Fraction fraction = this.mAdded.get(size);
            if (fraction != null && fraction.mFractionId == i) {
                return Optional.of(fraction);
            }
        }
        Map<Integer, Fraction> map = this.mActive;
        if (map != null) {
            for (Map.Entry<Integer, Fraction> entry : map.entrySet()) {
                Fraction value = entry.getValue();
                if (value != null && value.mFractionId == i) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    /* access modifiers changed from: package-private */
    public void attachAbility(FractionAbility fractionAbility) {
        this.mAbility = fractionAbility;
    }

    /* access modifiers changed from: package-private */
    public synchronized void enqueueOperation(OpBuilder opBuilder) {
        if (this.mPendingOperations == null) {
            this.mPendingOperations = new ArrayList();
        }
        this.mPendingOperations.add(opBuilder);
        if (this.mPendingOperations.size() == 1) {
            execPendingOperations();
        }
    }

    private void execPendingOperations() {
        if (!this.mExecutingOperations) {
            if (this.mEvents == null) {
                this.mEvents = new ArrayList();
                this.mIsEventPop = new ArrayList();
            }
            while (generateOpsForPendingOperations(this.mEvents, this.mIsEventPop)) {
                this.mExecutingOperations = true;
                try {
                    optimizeOperationsAndExecute(this.mEvents, this.mIsEventPop);
                } finally {
                    execClear();
                }
            }
            burpActive();
            return;
        }
        throw new IllegalStateException("FractionManager is already executing scheduler");
    }

    private void execClear() {
        this.mExecutingOperations = false;
        this.mIsEventPop.clear();
        this.mEvents.clear();
    }

    private void burpActive() {
        Map<Integer, Fraction> map = this.mActive;
        if (map != null) {
            Iterator<Map.Entry<Integer, Fraction>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                if (this.mActive.get(Integer.valueOf(it.next().getKey().intValue())) == null) {
                    it.remove();
                }
            }
        }
    }

    private synchronized boolean generateOpsForPendingOperations(List<FractionStack> list, List<Boolean> list2) {
        if (this.mPendingOperations != null) {
            if (this.mPendingOperations.size() != 0) {
                int size = this.mPendingOperations.size();
                boolean z = false;
                for (int i = 0; i < size; i++) {
                    z |= this.mPendingOperations.get(i).makeOps(list, list2);
                }
                this.mPendingOperations.clear();
                return z;
            }
        }
        return false;
    }

    private void optimizeOperationsAndExecute(List<FractionStack> list, List<Boolean> list2) {
        if (list != null && !list.isEmpty()) {
            if (list2 == null || list.size() != list2.size()) {
                throw new IllegalStateException("Internal error with the back stack records");
            }
            executeOps(list, list2);
        }
    }

    private void executeOps(List<FractionStack> list, List<Boolean> list2) {
        int size = list.size();
        List<Fraction> list3 = this.mAddedFractionSnap;
        if (list3 == null) {
            this.mAddedFractionSnap = new ArrayList();
        } else {
            list3.clear();
        }
        this.mAddedFractionSnap.addAll(this.mAdded);
        boolean z = false;
        for (int i = 0; i < size; i++) {
            FractionStack fractionStack = list.get(i);
            if (!list2.get(i).booleanValue()) {
                fractionStack.optimizeOps(this.mAddedFractionSnap);
            } else {
                fractionStack.trackAddedFractionsInPop(this.mAddedFractionSnap);
            }
            z = z || fractionStack.mAddToBackStack;
        }
        this.mAddedFractionSnap.clear();
        int i2 = 0;
        while (i2 < size) {
            FractionStack fractionStack2 = list.get(i2);
            boolean booleanValue = list2.get(i2).booleanValue();
            Log.debug(LABEL, "isPop value is %{public}s", Boolean.valueOf(booleanValue));
            if (booleanValue) {
                fractionStack2.bumpBackStackNesting(-1);
                fractionStack2.executePopOps(i2 == size + -1);
            } else {
                fractionStack2.bumpBackStackNesting(1);
                fractionStack2.executeOps();
            }
            i2++;
        }
        for (int i3 = 0; i3 < size; i3++) {
            FractionStack fractionStack3 = list.get(i3);
            if (list2.get(i3).booleanValue() && fractionStack3.mIndex >= 0) {
                freeBackStackIndex(fractionStack3.mIndex);
                fractionStack3.mIndex = -1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized int allocBackStackIndex(FractionStack fractionStack) {
        if (this.mAvailBackStackIndices != null) {
            if (this.mAvailBackStackIndices.size() > 0) {
                int intValue = this.mAvailBackStackIndices.remove(this.mAvailBackStackIndices.size() - 1).intValue();
                Log.debug(LABEL, "Adding back stack index %{public}d to %{public}s", Integer.valueOf(intValue), fractionStack.mName);
                this.mBackStackIndices.set(intValue, fractionStack);
                return intValue;
            }
        }
        if (this.mBackStackIndices == null) {
            this.mBackStackIndices = new ArrayList();
        }
        int size = this.mBackStackIndices.size();
        Log.debug(LABEL, "Setting back stack index %{public}d to %{public}s", Integer.valueOf(size), fractionStack.mName);
        this.mBackStackIndices.add(fractionStack);
        return size;
    }

    private synchronized void setBackStackIndex(int i, FractionStack fractionStack) {
        if (this.mBackStackIndices == null) {
            this.mBackStackIndices = new ArrayList();
        }
        int size = this.mBackStackIndices.size();
        if (i < size) {
            this.mBackStackIndices.set(i, fractionStack);
        } else {
            while (size < i) {
                this.mBackStackIndices.add(null);
                if (this.mAvailBackStackIndices == null) {
                    this.mAvailBackStackIndices = new ArrayList();
                }
                this.mAvailBackStackIndices.add(Integer.valueOf(size));
                size++;
            }
            this.mBackStackIndices.add(fractionStack);
        }
    }

    private synchronized void freeBackStackIndex(int i) {
        this.mBackStackIndices.set(i, null);
        if (this.mAvailBackStackIndices == null) {
            this.mAvailBackStackIndices = new ArrayList();
        }
        Log.debug(LABEL, "Freeing back stack index %{public}d", Integer.valueOf(i));
        this.mAvailBackStackIndices.add(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void moveToCurState(int i, boolean z) {
        if (z || i != this.mCurState) {
            this.mCurState = i;
            if (this.mActive != null) {
                for (int i2 = 0; i2 < this.mAdded.size(); i2++) {
                    moveFractionToExpectedState(this.mAdded.get(i2));
                }
                for (Map.Entry<Integer, Fraction> entry : this.mActive.entrySet()) {
                    Fraction value = entry.getValue();
                    if (value != null && value.mRemoving) {
                        moveFractionToExpectedState(value);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveFractionToExpectedState(Fraction fraction) {
        Fraction findFractionUnder;
        ComponentContainer componentContainer;
        int childIndex;
        int childIndex2;
        if (fraction != null) {
            int i = this.mCurState;
            if (fraction.mRemoving) {
                if (fraction.isInBackStack()) {
                    i = Math.min(i, 1);
                } else {
                    i = Math.min(i, 0);
                }
            }
            moveToState(fraction, i);
            if (!(fraction.mComponent == null || (findFractionUnder = findFractionUnder(fraction)) == null || (childIndex2 = componentContainer.getChildIndex(fraction.mComponent)) >= (childIndex = (componentContainer = fraction.mContainer).getChildIndex(findFractionUnder.mComponent)))) {
                componentContainer.removeComponentAt(childIndex2);
                componentContainer.addComponent(fraction.mComponent, childIndex);
            }
            if (fraction.mHiddenChanged) {
                if (fraction.mComponent != null) {
                    fraction.mComponent.setVisibility(fraction.mHidden ? 2 : 0);
                }
                fraction.mHiddenChanged = false;
            }
        }
    }

    private Fraction findFractionUnder(Fraction fraction) {
        ComponentContainer componentContainer = fraction.mContainer;
        Component component = fraction.mComponent;
        if (!(componentContainer == null || component == null)) {
            for (int indexOf = this.mAdded.indexOf(fraction) - 1; indexOf >= 0; indexOf--) {
                Fraction fraction2 = this.mAdded.get(indexOf);
                if (fraction2.mContainer == componentContainer && fraction2.mComponent != null) {
                    return fraction2;
                }
            }
        }
        return null;
    }

    private void moveToState(Fraction fraction, int i) {
        if (!fraction.mAdded && i > 1) {
            i = 1;
        }
        if (fraction.mRemoving && i > fraction.mState) {
            if (fraction.mState != 0 || !fraction.isInBackStack()) {
                i = fraction.mState;
            } else {
                i = 2;
            }
        }
        Log.debug(LABEL, "state of fraction is: %{public}d, and tmpState is: %{public}d", Integer.valueOf(fraction.mState), Integer.valueOf(i));
        if (fraction.mState <= i) {
            upgradeState(fraction, i);
        } else {
            degradeState(fraction, i);
        }
        if (fraction.mState != i) {
            Log.warn(LABEL, "moveToState: Fraction %{public}s expected state is %{public}s found %{public}s", fraction, Integer.valueOf(i), Integer.valueOf(fraction.mState));
            fraction.mState = i;
        }
    }

    private void degradeState(Fraction fraction, int i) {
        int i2 = fraction.mState;
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 == 3) {
                    if (i < 3) {
                        Log.debug(LABEL, "move from ACTIVE: %{public}s", fraction);
                        fraction.executeInActive(fraction.mIntent);
                    }
                } else {
                    return;
                }
            }
            if (i < 2) {
                Log.debug(LABEL, "move from STARTED: %{public}s", fraction);
                fraction.executeBackground(fraction.mIntent);
                if (fraction.mRemoving) {
                    removeFractionComponent(fraction);
                }
                fraction.mUpgrade = false;
            }
        }
        if (i < 1) {
            Log.debug(LABEL, "move from ATTACH_COMPONENT: %{public}s", fraction);
            fraction.executeStop(fraction.mIntent);
            fraction.mState = 0;
            fraction.executeComponentDetach();
            makeInactive(fraction);
            removeFractionComponent(fraction);
            fraction.mAbility = null;
            fraction.mManager = null;
            fraction.mUpgrade = true;
        }
    }

    private void removeFractionComponent(Fraction fraction) {
        if (!(fraction.mComponent == null || fraction.mContainer == null)) {
            fraction.mContainer.removeComponent(fraction.mComponent);
        }
        fraction.mContainer = null;
        fraction.mComponent = null;
        fraction.mRealComponent = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:14:? A[RETURN, SYNTHETIC] */
    private void upgradeState(Fraction fraction, int i) {
        int i2 = fraction.mState;
        if (i2 != 0) {
            if (i2 != 1) {
                if (i2 != 2) {
                    return;
                }
                if (i <= 2) {
                    Log.debug(LABEL, "move to ACTIVE: %{public}s", fraction);
                    fraction.executeActive(fraction.mIntent);
                    return;
                }
                return;
            }
        } else if (i > 0) {
            Log.debug(LABEL, "move to ATTACH_COMPONENT: %{public}s", fraction);
            moveStateToAttachComponent(fraction);
        }
        if (i > 1) {
            Log.debug(LABEL, "move to STARTED: %{public}s", fraction);
            if (fraction.mUpgrade) {
                fraction.executeStart(fraction.mIntent);
            } else {
                fraction.executeForeground(fraction.mIntent);
            }
        }
        if (i <= 2) {
        }
    }

    private void moveStateToAttachComponent(Fraction fraction) {
        ComponentContainer componentContainer;
        fraction.mAbility = this.mAbility;
        fraction.mManager = this;
        if (!fraction.mFromLayout) {
            if (fraction.mContainerId == 0) {
                componentContainer = null;
            } else if (fraction.mContainerId != -1) {
                componentContainer = this.mAbility.getAbilityComponentById(fraction.mContainerId) instanceof ComponentContainer ? (ComponentContainer) this.mAbility.getAbilityComponentById(fraction.mContainerId) : null;
                if (componentContainer != null) {
                    fraction.mContainer = componentContainer;
                } else {
                    throw new IllegalArgumentException("There is no component for id " + Integer.toHexString(fraction.mContainerId) + " for fraction " + fraction);
                }
            } else {
                throw new IllegalArgumentException("Cannot create fraction " + fraction + " for container component with no id");
            }
            fraction.executeAttachComponent(LayoutScatter.getInstance(this.mAbility.getContext()), componentContainer, fraction.mIntent);
            if (fraction.mComponent != null) {
                fraction.mRealComponent = fraction.mComponent;
                if (componentContainer != null) {
                    componentContainer.addComponent(fraction.mComponent);
                }
                if (fraction.mHidden) {
                    fraction.mComponent.setVisibility(2);
                    return;
                }
                return;
            }
            fraction.mRealComponent = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void addFraction(Fraction fraction, boolean z) {
        makeActive(fraction);
        if (!this.mAdded.contains(fraction)) {
            synchronized (this.mAdded) {
                this.mAdded.add(fraction);
            }
            fraction.mAdded = true;
            fraction.mRemoving = false;
            if (fraction.mComponent == null) {
                fraction.mHiddenChanged = false;
            }
            if (z) {
                moveToState(fraction, this.mCurState);
                return;
            }
            return;
        }
        throw new IllegalStateException("fraction already added");
    }

    /* access modifiers changed from: package-private */
    public void removeFraction(Fraction fraction) {
        Log.debug(LABEL, "remove fraction by pop here.", new Object[0]);
        synchronized (this.mAdded) {
            this.mAdded.remove(fraction);
        }
        fraction.mAdded = false;
        fraction.mRemoving = true;
    }

    /* access modifiers changed from: package-private */
    public void hideFraction(Fraction fraction) {
        if (!fraction.mHidden) {
            fraction.mHidden = true;
            fraction.mHiddenChanged = true ^ fraction.mHiddenChanged;
        }
    }

    /* access modifiers changed from: package-private */
    public void showFraction(Fraction fraction) {
        if (fraction.mHidden) {
            fraction.mHidden = false;
            fraction.mHiddenChanged = !fraction.mHiddenChanged;
        }
    }

    private void makeActive(Fraction fraction) {
        if (fraction.mIndex >= 0) {
            fraction.mState = 0;
            return;
        }
        Log.debug(LABEL, "makeActive: before mIndex value is %{public}s", Integer.valueOf(fraction.mIndex));
        int i = this.mNextFractionIndex;
        this.mNextFractionIndex = i + 1;
        fraction.setIndex(i);
        Log.debug(LABEL, "makeActive: mActive is %{public}s", this.mActive);
        if (this.mActive == null) {
            this.mActive = new HashMap();
        }
        Log.debug(LABEL, "makeActive: after mIndex value is %{public}s", Integer.valueOf(fraction.mIndex));
        this.mActive.put(Integer.valueOf(fraction.mIndex), fraction);
    }

    private void makeInactive(Fraction fraction) {
        if (fraction.mIndex >= 0) {
            this.mActive.put(Integer.valueOf(fraction.mIndex), null);
            fraction.initState();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleStateChange(int i) {
        moveToCurState(i, false);
        execPendingOperations();
    }

    /* access modifiers changed from: package-private */
    public void handleComponentAttach() {
        this.mDataSaved = false;
        this.mStopped = false;
        moveToCurState(1, false);
    }

    /* access modifiers changed from: package-private */
    public void handleStart() {
        this.mDataSaved = false;
        this.mStopped = false;
        handleStateChange(2);
    }

    /* access modifiers changed from: package-private */
    public void handleForeground() {
        handleStateChange(2);
    }

    /* access modifiers changed from: package-private */
    public void handleActive() {
        this.mDataSaved = false;
        this.mStopped = false;
        handleStateChange(3);
    }

    /* access modifiers changed from: package-private */
    public void handleInActive() {
        handleStateChange(2);
    }

    /* access modifiers changed from: package-private */
    public void handleBackground() {
        handleStateChange(1);
    }

    /* access modifiers changed from: package-private */
    public void handleStop() {
        this.mStopped = true;
        execPendingOperations();
        handleStateChange(0);
        this.mActive = null;
        this.mAdded.clear();
    }

    /* access modifiers changed from: package-private */
    public boolean isStateSaved() {
        return this.mDataSaved || this.mStopped;
    }

    private void saveFractionComponentState(Fraction fraction) {
        if (fraction.mRealComponent != null) {
            ArrayList<Sequenceable> arrayList = this.mStateArray;
            if (arrayList == null) {
                this.mStateArray = new ArrayList<>();
            } else {
                arrayList.clear();
            }
            if (this.mStateArray.size() > 0) {
                ArrayList<Sequenceable> arrayList2 = this.mStateArray;
                fraction.mSavedComponentState = (Sequenceable[]) arrayList2.toArray(new Sequenceable[arrayList2.size()]);
                this.mStateArray = null;
            }
        }
    }

    private PacMap saveFractionBasicState(Fraction fraction) {
        PacMap pacMap;
        if (this.mStatePac == null) {
            this.mStatePac = new PacMap();
        }
        fraction.onSaveFractionState(this.mStatePac);
        if (!this.mStatePac.isEmpty()) {
            pacMap = this.mStatePac;
            this.mStatePac = null;
        } else {
            pacMap = null;
        }
        if (fraction.mComponent != null) {
            saveFractionComponentState(fraction);
        }
        if (fraction.mSavedComponentState != null) {
            if (pacMap == null) {
                pacMap = new PacMap();
            }
            pacMap.putSequenceableObjectArray(COMPONENT_STATE_TAG, fraction.mSavedComponentState);
        }
        return pacMap;
    }

    /* access modifiers changed from: package-private */
    public HashMap<String, Object> saveAllData() {
        ArrayList<Map<String, Object>> saveActiveData;
        int[] iArr;
        int size;
        execPendingOperations();
        this.mDataSaved = true;
        Map<Integer, Fraction> map = this.mActive;
        ArrayList arrayList = null;
        if (map == null || map.size() <= 0 || (saveActiveData = saveActiveData()) == null) {
            return null;
        }
        int size2 = this.mAdded.size();
        if (size2 > 0) {
            iArr = new int[size2];
            for (int i = 0; i < size2; i++) {
                iArr[i] = this.mAdded.get(i).mIndex;
                if (iArr[i] >= 0) {
                    Log.debug(LABEL, "saveAllState: adding fraction #%{public}d: %{public}s", Integer.valueOf(iArr[i]), this.mAdded.get(i));
                } else {
                    throw new IllegalArgumentException("Failed to save state: active " + this.mAdded.get(i) + "has cleared index: " + iArr[i]);
                }
            }
        } else {
            iArr = null;
        }
        ArrayList<FractionStack> arrayList2 = this.mFractionStack;
        if (arrayList2 != null && (size = arrayList2.size()) > 0) {
            arrayList = new ArrayList(this.mActive.size());
            for (int i2 = 0; i2 < size; i2++) {
                HashMap hashMap = new HashMap();
                FractionStack fractionStack = this.mFractionStack.get(i2);
                hashMap.put("mOps", getSavedOps(fractionStack));
                hashMap.put("mName", fractionStack.mName);
                hashMap.put("mIndex", Integer.valueOf(fractionStack.mIndex));
                arrayList.add(hashMap);
                Log.debug(LABEL, "saveAllState: adding back stack #%{public}s: %{public}s", Integer.valueOf(i2), this.mFractionStack.get(i2));
            }
        }
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("mActive", saveActiveData);
        hashMap2.put("mAdded", iArr);
        hashMap2.put("stackState", arrayList);
        hashMap2.put("mNextFractionIndex", Integer.valueOf(this.mNextFractionIndex));
        return hashMap2;
    }

    private int[] getSavedOps(FractionStack fractionStack) {
        int size = fractionStack.mOps.size();
        int[] iArr = new int[(size * 2)];
        if (fractionStack.mAddToBackStack) {
            int i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                FractionStack.Op op = fractionStack.mOps.get(i2);
                int i3 = i + 1;
                iArr[i] = op.cmd;
                i = i3 + 1;
                iArr[i3] = op.fraction != null ? op.fraction.mIndex : -1;
            }
            return iArr;
        }
        throw new IllegalArgumentException("Not on back stack");
    }

    private ArrayList<Map<String, Object>> saveActiveData() {
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>(this.mActive.size());
        Iterator<Map.Entry<Integer, Fraction>> it = this.mActive.entrySet().iterator();
        boolean z = false;
        while (true) {
            HashMap hashMap = null;
            if (it.hasNext()) {
                Fraction value = it.next().getValue();
                if (value != null) {
                    if (value.mIndex >= 0) {
                        HashMap hashMap2 = new HashMap();
                        hashMap2.put("mClassName", value.getClass().getName());
                        hashMap2.put("mFractionId", Integer.valueOf(value.mFractionId));
                        hashMap2.put("mContainerId", Integer.valueOf(value.mContainerId));
                        hashMap2.put("mFromLayout", Boolean.valueOf(value.mFromLayout));
                        hashMap2.put("mIndex", Integer.valueOf(value.mIndex));
                        hashMap2.put("mTag", value.mTag);
                        if (value.mArguments != null) {
                            hashMap2.put("mArguments", new HashMap(value.mArguments.getAll()));
                        } else {
                            hashMap2.put("mArguments", null);
                        }
                        hashMap2.put("mHidden", Boolean.valueOf(value.mHidden));
                        if (value.mState <= 0 || hashMap2.get("mSavedFractionData") != null) {
                            hashMap = new HashMap(value.mSavedFractionData.getAll());
                        } else {
                            PacMap saveFractionBasicState = saveFractionBasicState(value);
                            if (saveFractionBasicState != null) {
                                hashMap = new HashMap(saveFractionBasicState.getAll());
                            }
                        }
                        hashMap2.put("mSavedFractionData", hashMap);
                        arrayList.add(hashMap2);
                        Log.debug(LABEL, "Saved state of %{public}s: %{public}s", value, hashMap2.get("mSavedFractionData"));
                        z = true;
                    } else {
                        throw new IllegalStateException("False to save data: active " + value + " has cleared index: " + value.mIndex);
                    }
                }
            } else if (z) {
                return arrayList;
            } else {
                Log.debug(LABEL, "saveAllState: no fragments!", new Object[0]);
                return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreAllState(HashMap<?, ?> hashMap) {
        if (!(hashMap == null || hashMap.get("mActive") == null)) {
            if (hashMap.get("mActive") instanceof ArrayList) {
                restoreActiveData((ArrayList) hashMap.get("mActive"));
            }
            this.mAdded.clear();
            if (hashMap.get("mAdded") != null && (hashMap.get("mAdded") instanceof int[])) {
                int[] iArr = (int[]) hashMap.get("mAdded");
                for (int i = 0; i < iArr.length; i++) {
                    Fraction fraction = this.mActive.get(Integer.valueOf(iArr[i]));
                    if (fraction != null) {
                        fraction.mAdded = true;
                        if (!this.mAdded.contains(fraction)) {
                            synchronized (this.mAdded) {
                                this.mAdded.add(fraction);
                            }
                        } else {
                            throw new IllegalStateException("Already added");
                        }
                    } else {
                        throw new IllegalStateException("No fraction for index #" + iArr[i]);
                    }
                }
            }
            if (hashMap.get("stackState") == null || !(hashMap.get("stackState") instanceof ArrayList)) {
                this.mFractionStack = null;
            } else {
                restoreStackState((ArrayList) hashMap.get("stackState"));
            }
            if (hashMap.get("mNextFractionIndex") != null && (hashMap.get("mNextFractionIndex") instanceof Integer)) {
                this.mNextFractionIndex = ((Integer) hashMap.get("mNextFractionIndex")).intValue();
            }
        }
    }

    private void restoreStackState(ArrayList<?> arrayList) {
        this.mFractionStack = new ArrayList<>(arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i) instanceof Map) {
                Map map = (Map) arrayList.get(i);
                String str = null;
                int[] iArr = map.get("mOps") instanceof int[] ? (int[]) map.get("mOps") : null;
                if (map.get("mName") instanceof String) {
                    str = (String) map.get("mName");
                }
                int intValue = map.get("mIndex") instanceof Integer ? ((Integer) map.get("mIndex")).intValue() : -1;
                if (Objects.isNull(iArr) || Objects.isNull(str) || intValue == -1) {
                    Log.debug(LABEL, "restoreAllState: invalid parameters.", new Object[0]);
                } else {
                    FractionStack init = new FractionStackState(iArr, str, intValue).init(this);
                    Log.debug(LABEL, "restoreAllState: back stack #%{public}s (index %{public}s): %{public}s", Integer.valueOf(i), Integer.valueOf(init.mIndex), init);
                    this.mFractionStack.add(init);
                    if (init.mIndex >= 0) {
                        setBackStackIndex(init.mIndex, init);
                    }
                }
            }
        }
    }

    private void restoreActiveData(ArrayList<?> arrayList) {
        this.mActive = new HashMap();
        for (int i = 0; i < arrayList.size(); i++) {
            FractionState executeActiveMap = executeActiveMap(arrayList, i);
            Fraction init = executeActiveMap.init(this.mAbility, this);
            Log.debug(LABEL, "restoreAllState: active#%{public}s: %{public}s", Integer.valueOf(i), init);
            this.mActive.put(Integer.valueOf(init.mIndex), init);
            executeActiveMap.mInstance = null;
        }
    }

    private FractionState executeActiveMap(ArrayList<?> arrayList, int i) {
        PacMap pacMap;
        PacMap pacMap2 = null;
        Map map = arrayList.get(i) instanceof Map ? (Map) arrayList.get(i) : null;
        String str = map.get("mClassName") instanceof String ? (String) map.get("mClassName") : null;
        int intValue = map.get("mFractionId") instanceof Integer ? ((Integer) map.get("mFractionId")).intValue() : -1;
        int intValue2 = map.get("mContainerId") instanceof Integer ? ((Integer) map.get("mContainerId")).intValue() : -1;
        boolean booleanValue = map.get("mFromLayout") instanceof Boolean ? ((Boolean) map.get("mFromLayout")).booleanValue() : false;
        int intValue3 = map.get("mIndex") instanceof Integer ? ((Integer) map.get("mIndex")).intValue() : -1;
        String str2 = map.get("mTag") instanceof String ? (String) map.get("mTag") : null;
        if (map.get("mArguments") instanceof Map) {
            PacMap pacMap3 = new PacMap();
            Map map2 = (Map) map.get("mArguments");
            if (map2 != null) {
                for (Map.Entry entry : map2.entrySet()) {
                    if (entry.getValue() instanceof PacMap) {
                        pacMap3.putAll((PacMap) entry.getValue());
                    }
                }
            }
            pacMap = pacMap3;
        } else {
            pacMap = null;
        }
        boolean booleanValue2 = map.get("mHidden") instanceof Boolean ? ((Boolean) map.get("mHidden")).booleanValue() : false;
        if (map.get("mSavedFractionData") instanceof Map) {
            pacMap2 = new PacMap();
            Map map3 = (Map) map.get("mSavedFractionData");
            if (map3 != null) {
                for (Map.Entry entry2 : map3.entrySet()) {
                    if (entry2.getValue() instanceof PacMap) {
                        pacMap2.putAll((PacMap) entry2.getValue());
                    }
                }
            }
        }
        return new FractionState(str, intValue, intValue2, booleanValue, intValue3, str2, pacMap, booleanValue2, pacMap2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean popStackState(List<FractionStack> list, List<Boolean> list2, String str, int i, int i2) {
        int i3;
        ArrayList<FractionStack> arrayList = this.mFractionStack;
        if (arrayList == null) {
            return false;
        }
        if (str == null && (i2 & 1) == 0) {
            int size = arrayList.size() - 1;
            if (size < 0) {
                return false;
            }
            list.add(this.mFractionStack.remove(size));
            list2.add(true);
        } else {
            if (str != null) {
                i3 = this.mFractionStack.size() - 1;
                while (i3 >= 0 && !str.equals(this.mFractionStack.get(i3).getName())) {
                    i3--;
                }
                if (i3 < 0) {
                    return false;
                }
                if ((i2 & 1) != 0) {
                    do {
                        i3--;
                        if (i3 < 0) {
                            break;
                        }
                    } while (str.equals(this.mFractionStack.get(i3).getName()));
                }
            } else {
                i3 = -1;
            }
            if (i3 == this.mFractionStack.size() - 1) {
                return false;
            }
            for (int size2 = this.mFractionStack.size() - 1; size2 > i3; size2--) {
                list.add(this.mFractionStack.remove(size2));
                list2.add(true);
            }
        }
        return true;
    }

    private class PopStackState implements OpBuilder {
        int mFlags;
        int mId;
        String mName;

        PopStackState(String str, int i, int i2) {
            this.mName = str;
            this.mId = i;
            this.mFlags = i2;
        }

        @Override // ohos.aafwk.ability.fraction.FractionManager.OpBuilder
        public boolean makeOps(List<FractionStack> list, List<Boolean> list2) {
            return FractionManager.this.popStackState(list, list2, this.mName, this.mId, this.mFlags);
        }
    }
}
