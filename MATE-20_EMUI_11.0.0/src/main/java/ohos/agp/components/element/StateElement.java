package ohos.agp.components.element;

import java.util.ArrayList;
import java.util.Arrays;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class StateElement extends ElementContainer {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "Drawable");
    public ArrayList<int[]> mStateList = new ArrayList<>();

    private native void nativeAddState(long j, int[] iArr, long j2);

    private native int nativeGetCurrentIndex(long j);

    private native long nativeGetDrawableContainerStateHandle(long j);

    private native long nativeGetStateListDrawableHandle();

    @Override // ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public void createNativePtr() {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetStateListDrawableHandle();
        }
    }

    @Override // ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public Element getCurrentElement() {
        if (getConstantState().mElementList.size() == 0) {
            return null;
        }
        int nativeGetCurrentIndex = nativeGetCurrentIndex(this.mNativeElementPtr);
        if (nativeGetCurrentIndex >= 0 && nativeGetCurrentIndex < getConstantState().mElementList.size()) {
            return getConstantState().mElementList.get(nativeGetCurrentIndex);
        }
        HiLog.error(TAG, "get current index fail.", new Object[0]);
        return null;
    }

    public void addState(int[] iArr, Element element) {
        if (element == null) {
            getConstantState().mElementList.add(null);
            this.mStateList.add(iArr);
            nativeAddState(this.mNativeElementPtr, iArr, 0);
        } else if (!element.isStateful()) {
            getConstantState().mElementList.add(element);
            this.mStateList.add(iArr);
            nativeAddState(this.mNativeElementPtr, iArr, element.getNativeElementPtr());
        }
    }

    public int findStateElementIndex(int[] iArr) {
        int stateCount = getStateCount();
        for (int i = 0; i < stateCount; i++) {
            if (Arrays.equals(iArr, this.mStateList.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public int[] getStateSet(int i) {
        if (i >= 0 && i < this.mStateList.size()) {
            return this.mStateList.get(i);
        }
        HiLog.error(TAG, "get state set fail.", new Object[0]);
        return new int[0];
    }

    public Element getStateElement(int i) {
        return getConstantState().mElementList.get(i);
    }

    public int getStateCount() {
        return this.mStateList.size();
    }

    @Override // ohos.agp.components.element.ElementContainer
    public long getElementContainerStateHandle() {
        return nativeGetDrawableContainerStateHandle(this.mNativeElementPtr);
    }
}
