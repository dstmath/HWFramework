package ohos.agp.components.element;

import java.util.ArrayList;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatedStateElement extends StateElement {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "Drawable");
    public ArrayList<int[]> mFromStateList = new ArrayList<>();
    public ArrayList<int[]> mToStateList = new ArrayList<>();

    private native void nativeAddStateTransition(long j, int[] iArr, int[] iArr2, long j2);

    private native int nativeGetCurrentIndex(long j);

    private native long nativeGetDrawableContainerStateHandle(long j);

    private native long nativeGetStateListElementHandle();

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public void createNativePtr() {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetStateListElementHandle();
        }
    }

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
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

    public void addStateTransition(int[] iArr, int[] iArr2, Element element) {
        long j;
        this.mFromStateList.add(iArr);
        this.mToStateList.add(iArr2);
        getConstantState().mElementList.add(element);
        long j2 = this.mNativeElementPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeAddStateTransition(j2, iArr, iArr2, j);
    }

    @Override // ohos.agp.components.element.StateElement
    public Element getStateElement(int i) {
        return getConstantState().mElementList.get(i);
    }

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer
    public long getElementContainerStateHandle() {
        return nativeGetDrawableContainerStateHandle(this.mNativeElementPtr);
    }
}
