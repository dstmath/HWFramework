package ohos.agp.components.element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatedStateElement extends StateElement {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AnimatedStateElement");
    private Map<Integer, Integer> elementMap = new HashMap();
    private Set<Node> itemSet = new HashSet();
    private Context mContext;
    private final ArrayList<int[]> mFromStateList = new ArrayList<>();
    private ResourceManager mResourceManager;
    private final ArrayList<int[]> mToStateList = new ArrayList<>();
    private Set<Node> transitionSet = new HashSet();

    private native void nativeAddStateTransition(long j, int[] iArr, int[] iArr2, long j2);

    private native int nativeGetCurrentIndex(long j);

    private native long nativeGetDrawableContainerStateHandle(long j);

    private native long nativeGetStateListElementHandle();

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public void createNativePtr(Object obj) {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetStateListElementHandle();
        }
    }

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
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

    public void addStateTransition(int[] iArr, int[] iArr2, Element element) {
        long j;
        this.mFromStateList.add(iArr);
        this.mToStateList.add(iArr2);
        getElementState().mElementList.add(element);
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
        return getElementState().mElementList.get(i);
    }

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.Element
    public void parseXMLNode(Context context, Node node) {
        HiLog.debug(TAG, "enter animated-state-container, new AnimatedStateElement", new Object[0]);
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            if (this.mResourceManager == null) {
                throw new ElementScatterException("mResourceManager is null");
            } else if (node.getName() != null) {
                if (node.getChild() != null) {
                    parseAnimatedNodeInternal(node);
                    return;
                }
                throw new ElementScatterException("no items in AnimatedStateElement!");
            }
        } else {
            throw new ElementScatterException("context is null");
        }
    }

    private void parseAnimatedNodeInternal(Node node) {
        if (node != null) {
            for (Node child = node.getChild(); child != null; child = child.getSibling()) {
                if ("item".equals(child.getName())) {
                    this.itemSet.add(child);
                }
                if ("transition".equals(child.getName())) {
                    this.transitionSet.add(child);
                }
            }
            for (Node node2 : this.itemSet) {
                parseAnimatedItem(node2.getTypedAttributes(this.mResourceManager));
            }
            for (Node node3 : this.transitionSet) {
                parseTransition(node3);
            }
        }
    }

    private void parseTransition(Node node) {
        List<TypedAttribute> typedAttributes = node.getTypedAttributes(this.mResourceManager);
        if (typedAttributes != null && !typedAttributes.isEmpty()) {
            try {
                int i = -1;
                int i2 = -1;
                int i3 = 0;
                int i4 = 0;
                for (TypedAttribute typedAttribute : typedAttributes) {
                    if ("start".equals(typedAttribute.getName())) {
                        i3 = typedAttribute.getIntegerValue();
                        i = this.elementMap.getOrDefault(Integer.valueOf(i3), -1).intValue();
                    }
                    if ("end".equals(typedAttribute.getName())) {
                        i4 = typedAttribute.getIntegerValue();
                        i2 = this.elementMap.getOrDefault(Integer.valueOf(i4), -1).intValue();
                    }
                }
                Node child = node.getChild();
                if (child == null) {
                    HiLog.error(TAG, "animation-list should be set in transition", new Object[0]);
                } else if (!"animation-list".equals(child.getName())) {
                    HiLog.error(TAG, "transition child tag should be set as animation-list", new Object[0]);
                } else {
                    FrameAnimationElement frameAnimationElement = new FrameAnimationElement();
                    frameAnimationElement.parseXMLNode(this.mContext, child);
                    if (i3 == 0 || i4 == 0) {
                        HiLog.error(TAG, "get startId or endId wrong", new Object[0]);
                    } else if (i == -1 || i2 == -1) {
                        HiLog.error(TAG, "set startState or end State wrong", new Object[0]);
                    } else {
                        addStateTransition(new int[]{i}, new int[]{i2}, frameAnimationElement);
                    }
                }
            } catch (IOException | IllegalArgumentException | NotExistException | WrongTypeException unused) {
                throw new ElementScatterException("set transition failed.");
            }
        }
    }

    private void parseAnimatedItem(List<TypedAttribute> list) {
        if (list != null && !list.isEmpty()) {
            try {
                int i = 0;
                int i2 = 0;
                for (TypedAttribute typedAttribute : list) {
                    if ("id".equals(typedAttribute.getName())) {
                        i = typedAttribute.getIntegerValue();
                    }
                    if ("state".equals(typedAttribute.getName())) {
                        i2 = typedAttribute.getIntegerValue();
                    }
                }
                if (i != 0) {
                    this.elementMap.put(Integer.valueOf(i), Integer.valueOf(i2));
                }
            } catch (IOException | IllegalArgumentException | NotExistException | WrongTypeException unused) {
                throw new ElementScatterException("set item failed.");
            }
        }
    }

    @Override // ohos.agp.components.element.StateElement, ohos.agp.components.element.ElementContainer
    public long getElementContainerStateHandle() {
        return nativeGetDrawableContainerStateHandle(this.mNativeElementPtr);
    }
}
