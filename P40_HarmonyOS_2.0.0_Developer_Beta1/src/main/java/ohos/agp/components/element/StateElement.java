package ohos.agp.components.element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.AttrHelper;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class StateElement extends ElementContainer {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "StateElement");
    private Context mContext;
    private ResourceManager mResourceManager;
    private final ArrayList<int[]> mStateList = new ArrayList<>();
    private final List<Integer> mStates = new ArrayList(Arrays.asList(64, 32, 0, 2, 268435456, 16384, 4));

    private native void nativeAddState(long j, int[] iArr, long j2);

    private native int nativeGetCurrentIndex(long j);

    private native long nativeGetDrawableContainerStateHandle(long j);

    private native long nativeGetStateListDrawableHandle();

    @Override // ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public void createNativePtr(Object obj) {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetStateListDrawableHandle();
        }
    }

    @Override // ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public Element getCurrentElement() {
        if (getElementState().mElementList.size() == 0) {
            return null;
        }
        int nativeGetCurrentIndex = nativeGetCurrentIndex(this.mNativeElementPtr);
        if (nativeGetCurrentIndex >= 0 && nativeGetCurrentIndex < getElementState().mElementList.size()) {
            return getElementState().mElementList.get(nativeGetCurrentIndex);
        }
        HiLog.error(TAG, "get current index fail.", new Object[0]);
        return null;
    }

    public void addState(int[] iArr, Element element) {
        if (element == null) {
            getElementState().mElementList.add(null);
            this.mStateList.add(iArr);
            nativeAddState(this.mNativeElementPtr, iArr, 0);
        } else if (!element.isStateful()) {
            getElementState().mElementList.add(element);
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
        return getElementState().mElementList.get(i);
    }

    public int getStateCount() {
        return this.mStateList.size();
    }

    @Override // ohos.agp.components.element.Element
    public void parseXMLNode(Context context, Node node) {
        super.parseXMLNode(context, node);
        HiLog.debug(TAG, "enter state-container, new stateElement", new Object[0]);
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            if (this.mResourceManager == null) {
                throw new ElementScatterException("mResourceManager is null");
            } else if (node.getName() != null) {
                Node child = node.getChild();
                if (child != null) {
                    parseNodeInternal(child);
                    return;
                }
                throw new ElementScatterException("no items in stateElement!");
            }
        } else {
            throw new ElementScatterException("context is null");
        }
    }

    private void parseNodeInternal(Node node) {
        if (node != null) {
            if ("item".equals(node.getName())) {
                parseItem(node.getTypedAttributes(this.mResourceManager));
            }
            Node sibling = node.getSibling();
            if (sibling != null) {
                parseNodeInternal(sibling);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004b A[Catch:{ IOException | NotExistException | WrongTypeException -> 0x0077 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0062 A[Catch:{ IOException | NotExistException | WrongTypeException -> 0x0077 }] */
    private void parseItem(List<TypedAttribute> list) {
        boolean z;
        if (list != null && !list.isEmpty()) {
            Element element = null;
            int i = -1;
            for (TypedAttribute typedAttribute : list) {
                try {
                    String name = typedAttribute.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != -1662836996) {
                        if (hashCode == 109757585) {
                            if (name.equals("state")) {
                                z = false;
                                if (!z) {
                                    i = typedAttribute.getIntegerValue();
                                    HiLog.debug(TAG, "read state : %d", new Object[]{Integer.valueOf(i)});
                                } else if (!z) {
                                    HiLog.error(TAG, "do not support this attribute: %s", new Object[]{typedAttribute.getName()});
                                } else {
                                    element = getElement(typedAttribute);
                                }
                            }
                        }
                    } else if (name.equals("element")) {
                        z = true;
                        if (!z) {
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                } catch (IOException | NotExistException | WrongTypeException unused) {
                    HiLog.error(TAG, "parse item failed", new Object[0]);
                }
            }
            if (!this.mStates.contains(Integer.valueOf(i))) {
                HiLog.error(TAG, "wrong state value in : %d", new Object[]{Integer.valueOf(i)});
                return;
            }
            int[] iArr = {i};
            if (element == null) {
                HiLog.error(TAG, "parse item element is null", new Object[0]);
            } else {
                addState(iArr, element);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0076, code lost:
        ohos.hiviewdfx.HiLog.error(ohos.agp.components.element.StateElement.TAG, "parse element failed.", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0080, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:? A[ExcHandler: IOException | NotExistException | WrongTypeException (unused java.lang.Throwable), SYNTHETIC, Splitter:B:8:0x0024] */
    private Element getElement(TypedAttribute typedAttribute) {
        if (typedAttribute.getType() == 8) {
            return ElementScatter.getInstance(this.mContext).parse(typedAttribute.getResId());
        } else if (typedAttribute.getType() == 6) {
            try {
                String mediaValue = typedAttribute.getMediaValue();
                try {
                    if (this.mContext == null) {
                        HiLog.error(TAG, "mContext is null!", new Object[0]);
                        return null;
                    }
                    ResourceManager resourceManager = this.mContext.getResourceManager();
                    if (resourceManager == null) {
                        return null;
                    }
                    return AttrHelper.getElementFromPath(mediaValue, resourceManager);
                } catch (IOException | NotExistException | WrongTypeException unused) {
                }
            } catch (IOException | NotExistException | WrongTypeException unused2) {
                HiLog.error(TAG, "get media element path failed", new Object[0]);
                return null;
            }
        } else if (typedAttribute.getType() != 1) {
            return AttrHelper.convertValueToElement(typedAttribute.getStringValue());
        } else {
            ShapeElement shapeElement = new ShapeElement();
            try {
                shapeElement.setRgbColor(RgbColor.fromArgbInt(typedAttribute.getColorValue()));
            } catch (IOException | NotExistException | WrongTypeException unused3) {
                HiLog.error(TAG, "set Background failed using color reference.", new Object[0]);
            }
            return shapeElement;
        }
    }

    @Override // ohos.agp.components.element.ElementContainer
    public long getElementContainerStateHandle() {
        return nativeGetDrawableContainerStateHandle(this.mNativeElementPtr);
    }
}
