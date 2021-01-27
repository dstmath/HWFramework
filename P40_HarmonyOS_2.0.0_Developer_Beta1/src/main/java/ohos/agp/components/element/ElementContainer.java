package ohos.agp.components.element;

import java.util.ArrayList;
import ohos.agp.components.element.Element;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class ElementContainer extends Element {
    private int mCurrentDrawableIndex;
    protected final ElementState mElementState = createState();

    /* access modifiers changed from: private */
    public static native int nativeAddChild(long j, long j2);

    private native long nativeGetDrawableContainerHandle();

    private native long nativeGetElementContainerStateHandle(long j);

    private native int nativeGetFadeInEffectPeriod(long j);

    private native int nativeGetFadeOutEffectPeriod(long j);

    private native boolean nativeSelectDrawable(long j, int i);

    private native void nativeSetConstantState(long j, long j2);

    private native void nativeSetEnterFadeDuration(long j, int i);

    private native void nativeSetExitFadeDuration(long j, int i);

    private native void nativeSkipAnimation(long j);

    @Override // ohos.agp.components.element.Element
    public boolean isStateful() {
        return true;
    }

    public ElementContainer() {
        setElementState(this.mElementState);
    }

    /* access modifiers changed from: protected */
    public ElementState createState() {
        return new ElementState(this);
    }

    @Override // ohos.agp.components.element.Element
    public void createNativePtr(Object obj) {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetDrawableContainerHandle();
        }
    }

    public void setEnterFadeDuration(int i) {
        nativeSetEnterFadeDuration(this.mNativeElementPtr, i);
    }

    public void setExitFadeDuration(int i) {
        nativeSetExitFadeDuration(this.mNativeElementPtr, i);
    }

    public int getFadeInEffectPeriod() {
        return nativeGetFadeInEffectPeriod(this.mNativeElementPtr);
    }

    public int getFadeOutEffectPeriod() {
        return nativeGetFadeOutEffectPeriod(this.mNativeElementPtr);
    }

    public boolean selectElement(int i) {
        if (!nativeSelectDrawable(this.mNativeElementPtr, i)) {
            return false;
        }
        this.mCurrentDrawableIndex = i;
        return true;
    }

    @Override // ohos.agp.components.element.Element
    public Element getCurrentElement() {
        int i;
        ArrayList<Element> arrayList = this.mElementState.mElementList;
        if (this.mCurrentDrawableIndex >= arrayList.size() || (i = this.mCurrentDrawableIndex) < 0) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override // ohos.agp.components.element.Element
    public void skipAnimation() {
        nativeSkipAnimation(this.mNativeElementPtr);
    }

    public ElementState getElementState() {
        return this.mElementState;
    }

    public static class ElementState extends Element.ConstantState {
        final ArrayList<Element> mElementList;

        public static class DCSCleaner implements MemoryCleaner {
            private long mNativePtr;

            private static native void nativeRelease(long j);

            DCSCleaner(long j) {
                this.mNativePtr = j;
            }

            @Override // ohos.agp.utils.MemoryCleaner
            public void run() {
                long j = this.mNativePtr;
                if (j != 0) {
                    nativeRelease(j);
                    this.mNativePtr = 0;
                }
            }
        }

        public ElementState() {
            this(null);
        }

        public ElementState(ElementContainer elementContainer) {
            this.mElementList = new ArrayList<>();
            if (elementContainer != null) {
                this.mNativePtr = elementContainer.getElementContainerStateHandle();
                MemoryCleanerRegistry.getInstance().register(this, new DCSCleaner(this.mNativePtr));
            }
        }

        public final int addChildElement(Element element) {
            if (element == null) {
                return ElementContainer.nativeAddChild(this.mNativePtr, 0);
            }
            this.mElementList.add(element);
            return ElementContainer.nativeAddChild(this.mNativePtr, element.mNativeElementPtr);
        }

        public final Element getChildElement(int i) {
            if (i >= this.mElementList.size() || i < 0) {
                return null;
            }
            return this.mElementList.get(i);
        }

        public final int getChildElementCount() {
            return this.mElementList.size();
        }
    }

    public void setElementState(ElementState elementState) {
        if (elementState != null) {
            nativeSetConstantState(this.mNativeElementPtr, elementState.mNativePtr);
        }
    }

    public long getElementContainerStateHandle() {
        return nativeGetElementContainerStateHandle(this.mNativeElementPtr);
    }
}
