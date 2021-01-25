package ohos.agp.components.element;

import java.io.IOException;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.element.ElementContainer;
import ohos.app.Context;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class FrameAnimationElement extends ElementContainer {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AnimationDrawable");
    private AnimationState mAnimationState;
    private Context mContext;
    private ResourceManager mResourceManager;

    /* access modifiers changed from: private */
    public static native void nativeAddFrame(long j, long j2, int i);

    private native long nativeGetAnimationDrawableHandle();

    /* access modifiers changed from: private */
    public static native long nativeGetAnimationDrawableStateHandle(long j);

    private native int nativeGetFrameDuration(long j, int i);

    private native void nativeSetOneShot(long j, boolean z);

    private native void nativeStart(long j);

    private native void nativeStop(long j);

    public FrameAnimationElement() {
        this.mAnimationState = new AnimationState(this);
        setConstantState(this.mAnimationState);
    }

    public FrameAnimationElement(Context context, int i) {
        if (context != null) {
            this.mAnimationState = new AnimationState(this);
            setConstantState(this.mAnimationState);
            parseXMLNode(context, ElementScatter.getInstance(context).getRootNodeFromXmlId(i));
            return;
        }
        throw new ElementScatterException("context is null");
    }

    @Override // ohos.agp.components.element.ElementContainer, ohos.agp.components.element.Element
    public void createNativePtr() {
        if (this.mNativeElementPtr == 0) {
            this.mNativeElementPtr = nativeGetAnimationDrawableHandle();
        }
    }

    public void start() {
        nativeStart(this.mNativeElementPtr);
    }

    public void stop() {
        nativeStop(this.mNativeElementPtr);
    }

    public int getNumberOfFrames() {
        return this.mAnimationState.getChildElementCount();
    }

    public Element getFrame(int i) {
        return this.mAnimationState.getChildElement(i);
    }

    public int getDuration(int i) {
        return nativeGetFrameDuration(this.mNativeElementPtr, i);
    }

    public boolean isOneShot() {
        return this.mAnimationState.mOneShot;
    }

    public void setOneShot(boolean z) {
        this.mAnimationState.mOneShot = z;
        nativeSetOneShot(this.mNativeElementPtr, z);
    }

    public void addFrame(Element element, int i) {
        this.mAnimationState.addFrame(element, i);
    }

    /* access modifiers changed from: private */
    public static class AnimationState extends ElementContainer.ElementState {
        private boolean mOneShot = false;

        AnimationState(FrameAnimationElement frameAnimationElement) {
            this.mNativePtr = FrameAnimationElement.nativeGetAnimationDrawableStateHandle(frameAnimationElement.mNativeElementPtr);
        }

        /* access modifiers changed from: package-private */
        public void addFrame(Element element, int i) {
            this.mElementList.add(element);
            FrameAnimationElement.nativeAddFrame(this.mNativePtr, element.mNativeElementPtr, i);
        }
    }

    @Override // ohos.agp.components.element.ElementContainer
    public void setConstantState(ElementContainer.ElementState elementState) {
        super.setConstantState(elementState);
        if (elementState instanceof AnimationState) {
            this.mAnimationState = (AnimationState) elementState;
        }
    }

    @Override // ohos.agp.components.element.Element
    public void parseXMLNode(Context context, Node node) {
        super.parseXMLNode(context, node);
        HiLog.debug(TAG, "enter animation-list, new AnimationDrawable", new Object[0]);
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            ResourceManager resourceManager = this.mResourceManager;
            if (resourceManager != null) {
                for (TypedAttribute typedAttribute : node.getTypedAttribute(resourceManager)) {
                    if (typedAttribute == null) {
                        HiLog.error(TAG, "typedAttribute is null", new Object[0]);
                    } else if ("oneshot".equals(typedAttribute.getName())) {
                        try {
                            boolean z = true;
                            HiLog.debug(TAG, "animationDrawable setOneShot: %{public}s", new Object[]{typedAttribute.getStringValue()});
                            if (typedAttribute.getStringValue().equalsIgnoreCase("false")) {
                                z = false;
                            }
                            setOneShot(z);
                        } catch (IOException | NotExistException | WrongTypeException unused) {
                            HiLog.error(TAG, "get oneShot value failed.", new Object[0]);
                        }
                    }
                }
                Node child = node.getChild();
                if (child != null) {
                    parseAddFrame(child);
                    return;
                }
                throw new ElementScatterException("no items in AnimationDrawable!");
            }
            throw new ElementScatterException("mResourceManager is null");
        }
        throw new ElementScatterException("context is null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x009d  */
    private void parseAddFrame(Node node) {
        boolean z;
        if (!"item".equals(node.getName())) {
            throw new ElementScatterException("tag is not item !");
        } else if (node.getChild() == null) {
            HiLog.debug(TAG, "parsing drawable item ", new Object[0]);
            Element element = null;
            int i = -1;
            for (TypedAttribute typedAttribute : node.getTypedAttribute(this.mResourceManager)) {
                if (typedAttribute == null) {
                    HiLog.error(TAG, "typedAttribute is null", new Object[0]);
                } else {
                    String name = typedAttribute.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != -1992012396) {
                        if (hashCode == -1662836996 && name.equals("element")) {
                            z = false;
                            if (!z) {
                                element = parseDrawable(typedAttribute);
                            } else if (!z) {
                                HiLog.error(TAG, "do not support this tag: %{public}s", new Object[]{typedAttribute.getName()});
                            } else {
                                try {
                                    i = typedAttribute.getIntegerValue();
                                    HiLog.debug(TAG, "duration is %{public}d", new Object[]{Integer.valueOf(i)});
                                } catch (IOException | NotExistException | WrongTypeException unused) {
                                    throw new ElementScatterException("get duration failed");
                                }
                            }
                            if (!(i == -1 || element == null)) {
                                HiLog.debug(TAG, "add a Frame!", new Object[0]);
                                addFrame(element, i);
                            }
                        }
                    } else if (name.equals(SchemaSymbols.ATTVAL_DURATION)) {
                        z = true;
                        if (!z) {
                        }
                        HiLog.debug(TAG, "add a Frame!", new Object[0]);
                        addFrame(element, i);
                    }
                    z = true;
                    if (!z) {
                    }
                    HiLog.debug(TAG, "add a Frame!", new Object[0]);
                    addFrame(element, i);
                }
            }
            Node sibling = node.getSibling();
            if (sibling == null) {
                HiLog.debug(TAG, "no sibling node, reach the end of frame item.", new Object[0]);
                return;
            }
            HiLog.debug(TAG, "continue to get frame Drawable from sibling", new Object[0]);
            parseAddFrame(sibling);
        } else {
            throw new ElementScatterException("just need one item layer.");
        }
    }

    private Element parseDrawable(TypedAttribute typedAttribute) {
        try {
            String mediaValue = typedAttribute.getMediaValue();
            HiLog.debug(TAG, "drawable mediaPath is %{public}s", new Object[]{mediaValue});
            Element drawableFromPath = getDrawableFromPath(mediaValue, this.mResourceManager);
            if (drawableFromPath != null) {
                return drawableFromPath;
            }
            HiLog.error(TAG, "AnimationDrawable is null", new Object[0]);
            return null;
        } catch (IOException | NotExistException | WrongTypeException unused) {
            throw new ElementScatterException("get animation media item path failed!");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0069, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006a, code lost:
        if (r4 != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0071, code lost:
        r3.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0074, code lost:
        throw r0;
     */
    private Element getDrawableFromPath(String str, ResourceManager resourceManager) {
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        RawFileEntry rawFileEntry = resourceManager.getRawFileEntry(str);
        if (rawFileEntry == null) {
            HiLog.error(TAG, " element rawFileEntry is null", new Object[0]);
            return null;
        }
        try {
            Resource openRawFile = rawFileEntry.openRawFile();
            if (openRawFile == null) {
                HiLog.error(TAG, " element resource is null", new Object[0]);
                if (openRawFile != null) {
                    openRawFile.close();
                }
                return null;
            }
            ImageSource create = ImageSource.create(openRawFile, sourceOptions);
            if (create == null) {
                HiLog.error(TAG, " create image source failed! ", new Object[0]);
                openRawFile.close();
                return null;
            }
            PixelMapElement pixelMapElement = new PixelMapElement(create.createPixelmap(decodingOptions));
            openRawFile.close();
            return pixelMapElement;
        } catch (IOException unused) {
            HiLog.error(TAG, "create element catch error", new Object[0]);
            return null;
        }
    }
}
