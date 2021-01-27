package ohos.agp.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentObserverHandler;
import ohos.agp.components.Image;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.FlexElement;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.element.StateElement;
import ohos.agp.components.element.VectorElement;
import ohos.agp.image.PixelMapFactory;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ImageAttrsConstants;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

public class Image extends Component {
    private static final int CLIP_DIRECTION_MASK = 3;
    public static final int CLIP_DIRECTION_NOT_SET = 0;
    public static final int CLIP_GRAVITY_NOT_SET = 0;
    public static final int CLIP_HORIZONTAL = 1;
    public static final int CLIP_VERTICAL = 2;
    private static final int CORNER_ARRAY_LENGTH = 8;
    public static final int GRAVITY_BOTTOM = 2048;
    public static final int GRAVITY_CENTER = 4096;
    private static final int GRAVITY_CENTER_MASK = 4096;
    public static final int GRAVITY_LEFT = 256;
    private static final int GRAVITY_LEFT_RIGHT_MASK = 768;
    private static final int GRAVITY_MASK = 3840;
    public static final int GRAVITY_RIGHT = 512;
    public static final int GRAVITY_TOP = 1024;
    private static final int GRAVITY_TOP_BOTTOM_MASK = 3072;
    private static final int IMAGE_CALLBACK_PARAMS_NUM = 0;
    private static final List<Class<?>> SUPPORTED_ELEMENT_TYPES = Arrays.asList(ShapeElement.class, VectorElement.class, PixelMapElement.class, StateElement.class, FlexElement.class);
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "Image");
    private int mClipDirection;
    private int mClipGravity;
    private Element mElement;
    private final ImageObserverHandler mImageObserverHandler;
    private int mImageResourceId;
    private PixelMapHolder mPixelMapHolder;

    public interface ImageObserver extends ComponentObserverHandler.Observer {
        void onImageChanged();
    }

    private native void nativeAddByteData(long j, byte[] bArr, int i);

    private native long nativeCreateBuffer();

    private native int nativeGetClipDirection(long j);

    private native int nativeGetClipGravity(long j);

    private native float[] nativeGetCornerRadii(long j);

    private native float nativeGetCornerRadius(long j);

    private native long nativeGetImageHandle();

    private native int nativeGetScaleMode(long j);

    private native void nativeReleaseBufferToImage(long j, long j2);

    private native void nativeSetClipDirection(long j, int i);

    private native void nativeSetClipGravity(long j, int i);

    private native void nativeSetCornerRadii(long j, float[] fArr);

    private native void nativeSetCornerRadius(long j, float f);

    private native void nativeSetImageElement(long j, long j2);

    private native void nativeSetImageHolder(long j, long j2);

    private native void nativeSetScaleMode(long j, int i);

    public enum ScaleMode {
        ZOOM_CENTER(0),
        ZOOM_START(1),
        ZOOM_END(2),
        STRETCH(3),
        CENTER(4),
        INSIDE(5),
        CLIP_CENTER(6);
        
        private final int nativeValue;

        private ScaleMode(int i) {
            this.nativeValue = i;
        }

        /* access modifiers changed from: protected */
        public int getValue() {
            return this.nativeValue;
        }

        public static ScaleMode getByInt(int i) {
            return (ScaleMode) Arrays.stream(values()).filter(new Predicate(i) {
                /* class ohos.agp.components.$$Lambda$Image$ScaleMode$j9EGlu66tnY96_GZq1RQag35cw */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return Image.ScaleMode.lambda$getByInt$0(this.f$0, (Image.ScaleMode) obj);
                }
            }).findAny().orElse(ZOOM_CENTER);
        }

        static /* synthetic */ boolean lambda$getByInt$0(int i, ScaleMode scaleMode) {
            return scaleMode.getValue() == i;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetImageHandle();
        }
    }

    public Image(Context context) {
        this(context, null);
    }

    public Image(Context context, AttrSet attrSet) {
        this(context, attrSet, "ImageDefaultStyle");
    }

    public Image(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mClipDirection = 3;
        this.mImageResourceId = 0;
        this.mImageObserverHandler = new ImageObserverHandler();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getImageAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(ImageAttrsConstants.IMAGE_SRC)) {
            Element asElement = style.getPropertyValue(ImageAttrsConstants.IMAGE_SRC).asElement();
            if (asElement instanceof PixelMapElement) {
                setPixelMap(((PixelMapElement) asElement).getPixelMap());
            } else if ((asElement instanceof VectorElement) || (asElement instanceof ShapeElement)) {
                setImageElement(asElement);
            } else {
                HiLog.error(TAG, "Unsupported drawable", new Object[0]);
            }
        }
    }

    public void setPixelMapHolder(PixelMapHolder pixelMapHolder) {
        this.mImageResourceId = 0;
        long j = 0;
        if (this.mElement != null) {
            this.mElement = null;
            nativeSetImageElement(this.mNativeViewPtr, 0);
        }
        this.mPixelMapHolder = pixelMapHolder;
        long j2 = this.mNativeViewPtr;
        if (pixelMapHolder != null) {
            j = pixelMapHolder.getNativeHolder();
        }
        nativeSetImageHolder(j2, j);
    }

    public PixelMapHolder getPixelMapHolder() {
        return this.mPixelMapHolder;
    }

    public void setPixelMap(PixelMap pixelMap) {
        if (pixelMap == null) {
            setPixelMapHolder(null);
            return;
        }
        PixelMapHolder pixelMapHolder = this.mPixelMapHolder;
        if (pixelMapHolder != null) {
            pixelMapHolder.resetPixelMap(pixelMap);
            postLayout();
            invalidate();
            return;
        }
        setPixelMapHolder(new PixelMapHolder(pixelMap));
    }

    public PixelMap getPixelMap() {
        if (this.mPixelMapHolder == null) {
            Element element = this.mElement;
            if (element instanceof PixelMapElement) {
                return ((PixelMapElement) element).getPixelMap();
            }
        }
        PixelMapHolder pixelMapHolder = this.mPixelMapHolder;
        if (pixelMapHolder == null) {
            return null;
        }
        return pixelMapHolder.getPixelMap();
    }

    public void setImageElement(Element element) {
        if (SUPPORTED_ELEMENT_TYPES.stream().anyMatch(new Predicate() {
            /* class ohos.agp.components.$$Lambda$Image$pr8i5yV7NK06cF2DKKUcUheOD4 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((Class) obj).isInstance(Element.this);
            }
        })) {
            this.mElement = element;
            nativeSetImageElement(this.mNativeViewPtr, element.getNativeElementPtr());
            if (this.mPixelMapHolder != null) {
                this.mPixelMapHolder = null;
                nativeSetImageHolder(this.mNativeViewPtr, 0);
            }
            this.mImageResourceId = 0;
        }
    }

    public Element getImageElement() {
        return this.mElement;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0081, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0086, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0087, code lost:
        r6.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008a, code lost:
        throw r0;
     */
    public void setImageAndDecodeBounds(int i) {
        if (this.mContext == null) {
            HiLog.error(TAG, " mContext is null!", new Object[0]);
        } else if (this.mImageResourceId != i) {
            this.mImageResourceId = i;
            this.mPixelMapHolder = null;
            if (this.mElement != null) {
                this.mElement = null;
                nativeSetImageElement(this.mNativeViewPtr, 0);
            }
            ResourceManager resourceManager = this.mContext.getResourceManager();
            if (resourceManager == null) {
                HiLog.error(TAG, " Fail to get resource manager!", new Object[0]);
                return;
            }
            try {
                String mediaPath = resourceManager.getMediaPath(i);
                if (mediaPath == null) {
                    HiLog.error(TAG, " resourcePath is null", new Object[0]);
                    return;
                }
                RawFileEntry rawFileEntry = resourceManager.getRawFileEntry(mediaPath);
                if (rawFileEntry == null) {
                    HiLog.error(TAG, " imageView rawFileEntry is null", new Object[0]);
                    return;
                }
                try {
                    Resource openRawFile = rawFileEntry.openRawFile();
                    if (openRawFile != null) {
                        long nativeCreateBuffer = nativeCreateBuffer();
                        byte[] bArr = new byte[4096];
                        while (true) {
                            int read = openRawFile.read(bArr);
                            if (read > 0) {
                                nativeAddByteData(nativeCreateBuffer, bArr, read);
                            } else {
                                nativeReleaseBufferToImage(this.mNativeViewPtr, nativeCreateBuffer);
                                openRawFile.close();
                                return;
                            }
                        }
                    } else if (openRawFile != null) {
                        openRawFile.close();
                    }
                } catch (IOException unused) {
                    HiLog.error(TAG, " Fail to create image using resource id", new Object[0]);
                }
            } catch (IOException | NotExistException | WrongTypeException unused2) {
                HiLog.error(TAG, " can't find image resource id", new Object[0]);
            }
        }
    }

    public void setPixelMap(int i) {
        setPixelMap(PixelMapFactory.createFromResourceId(this.mContext, i).orElse(null));
    }

    public void setClipDirection(int i) {
        int i2 = i & 3;
        if (i2 == 0) {
            i2 = 3;
        }
        this.mClipDirection = i2;
        nativeSetClipDirection(this.mNativeViewPtr, this.mClipDirection);
        setClipGravity(this.mClipGravity);
    }

    public int getClipDirection() {
        return nativeGetClipDirection(this.mNativeViewPtr);
    }

    public void setClipGravity(int i) {
        this.mClipGravity = i;
        nativeSetClipGravity(this.mNativeViewPtr, maskClipGravity(i));
    }

    public int getClipGravity() {
        return nativeGetClipGravity(this.mNativeViewPtr);
    }

    public void setScaleMode(ScaleMode scaleMode) {
        if (scaleMode != null) {
            nativeSetScaleMode(this.mNativeViewPtr, scaleMode.getValue());
        }
    }

    public ScaleMode getScaleMode() {
        return ScaleMode.getByInt(nativeGetScaleMode(this.mNativeViewPtr));
    }

    public void setCornerRadius(float f) {
        if (f < 0.0f) {
            HiLog.error(TAG, "radius is invalid.", new Object[0]);
        } else {
            nativeSetCornerRadius(this.mNativeViewPtr, f);
        }
    }

    public float getCornerRadius() {
        return nativeGetCornerRadius(this.mNativeViewPtr);
    }

    public void setCornerRadii(float[] fArr) {
        if (fArr.length != 8) {
            HiLog.error(TAG, "radii is invalid.", new Object[0]);
        } else {
            nativeSetCornerRadii(this.mNativeViewPtr, fArr);
        }
    }

    public float[] getCornerRadii() {
        return nativeGetCornerRadii(this.mNativeViewPtr);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002d, code lost:
        if (r0 != false) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        if (r0 != false) goto L_0x0045;
     */
    private int maskClipGravity(int i) {
        int i2;
        int i3 = i & 4096;
        if (i3 != 0) {
            return i3;
        }
        int i4 = i & 3840;
        int i5 = this.mClipDirection;
        boolean z = false;
        if (i5 == 1) {
            i2 = i4 & 768;
            if (i2 == 0) {
                i2 = 4096;
            }
            if (i2 == 768) {
                z = true;
            }
        } else {
            if (i5 == 2) {
                i2 = i4 & GRAVITY_TOP_BOTTOM_MASK;
                if (i2 == 0) {
                    i2 = 4096;
                }
                if (i2 == GRAVITY_TOP_BOTTOM_MASK) {
                    z = true;
                }
            } else if (i5 == 3) {
                boolean z2 = (i4 & GRAVITY_TOP_BOTTOM_MASK) == GRAVITY_TOP_BOTTOM_MASK;
                if ((i4 & 768) == 768) {
                    z = true;
                }
                if (!z2 && !z) {
                    return i4;
                }
            }
            return 4096;
        }
        return i2;
    }

    public void addImageObserver(ImageObserver imageObserver) {
        if (this.mImageObserverHandler.getObserversCount() == 0) {
            addObserverHandler(this.mImageObserverHandler);
        }
        this.mImageObserverHandler.addObserver(imageObserver);
    }

    public void removeImageObserver(ImageObserver imageObserver) {
        this.mImageObserverHandler.removeObserver(imageObserver);
        if (this.mImageObserverHandler.getObserversCount() == 0) {
            removeObserverHandler(this.mImageObserverHandler);
        }
    }

    private static class ImageObserverHandler extends ComponentObserverHandler<ImageObserver> {
        private ImageObserverHandler() {
        }

        @Override // ohos.agp.components.ComponentObserverHandler
        public void onChange(int[] iArr) {
            super.onChange(iArr);
            if (iArr.length > 0) {
                HiLog.error(Image.TAG, "Illegal return params, should be size %{public}d", new Object[]{0});
                return;
            }
            for (ImageObserver imageObserver : this.mObservers) {
                imageObserver.onImageChanged();
            }
        }
    }
}
