package ohos.agp.components;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Image;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.VectorElement;
import ohos.agp.image.PixelMapFactory;
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
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class Image extends Component {
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
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "Image");
    private int mClipDirection;
    private int mImageResourceId;
    private PixelMap mPixelMap;

    private native int nativeGetClipGravity(long j);

    private native float[] nativeGetCornerRadii(long j);

    private native float nativeGetCornerRadius(long j);

    private native long nativeGetImageHandle();

    private native String nativeGetImageUri(long j);

    private native int nativeGetScaleType(long j);

    private native void nativeSetClipDirection(long j, int i);

    private native void nativeSetClipGravity(long j, int i);

    private native void nativeSetCornerRadii(long j, float[] fArr);

    private native void nativeSetCornerRadius(long j, float f);

    private native void nativeSetImage(long j, PixelMap pixelMap);

    private native void nativeSetScaleType(long j, int i);

    private native void nativeSetStream(long j, InputStream inputStream);

    public enum ScaleType {
        SCALE_TO_FIT_CENTER(0),
        SCALE_TO_START(1),
        SCALE_TO_END(2),
        SCALE_TO_FULL(3),
        SCALE_TO_CENTER(4),
        SCALE_TO_CENTER_INSIDE(5);
        
        private final int nativeValue;

        private ScaleType(int i) {
            this.nativeValue = i;
        }

        /* access modifiers changed from: protected */
        public int getValue() {
            return this.nativeValue;
        }

        public static ScaleType getByInt(int i) {
            return (ScaleType) Arrays.stream(values()).filter(new Predicate(i) {
                /* class ohos.agp.components.$$Lambda$Image$ScaleType$HlngW394B5SmJ_7RmtGCQyApqM */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return Image.ScaleType.lambda$getByInt$0(this.f$0, (Image.ScaleType) obj);
                }
            }).findAny().orElse(SCALE_TO_FIT_CENTER);
        }

        static /* synthetic */ boolean lambda$getByInt$0(int i, ScaleType scaleType) {
            return scaleType.getValue() == i;
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
        this.mImageResourceId = 0;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ImageAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(ImageAttrsConstants.IMAGE_URI)) {
            setImageURI(style.getPropertyValue(ImageAttrsConstants.IMAGE_URI).asString());
        }
        if (style.hasProperty(ImageAttrsConstants.CLIP_DIRECTION)) {
            setClipDirection(style.getPropertyValue(ImageAttrsConstants.CLIP_DIRECTION).asInteger());
        }
        if (style.hasProperty(ImageAttrsConstants.IMAGE_SRC)) {
            Element asElement = style.getPropertyValue(ImageAttrsConstants.IMAGE_SRC).asElement();
            if (asElement instanceof PixelMapElement) {
                setPixelMap(((PixelMapElement) asElement).getPixelMap());
            } else if (asElement instanceof VectorElement) {
                setForeground(asElement);
            } else {
                HiLog.error(TAG, "Unsupported drawable", new Object[0]);
            }
        }
    }

    public void setImageURI(String str) {
        PixelMap pixelMap = this.mPixelMap;
        if (pixelMap != null) {
            pixelMap.release();
        }
        this.mPixelMap = PixelMapFactory.createFromPath(str);
        nativeSetImage(this.mNativeViewPtr, this.mPixelMap);
    }

    public String getImageURI() {
        return nativeGetImageUri(this.mNativeViewPtr);
    }

    public void setPixelMap(PixelMap pixelMap) {
        this.mPixelMap = pixelMap;
        nativeSetImage(this.mNativeViewPtr, pixelMap);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0069, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006a, code lost:
        if (r5 != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006c, code lost:
        $closeResource(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006f, code lost:
        throw r0;
     */
    public void setImageAndDecodeBounds(int i) {
        if (this.mContext == null) {
            HiLog.error(TAG, " mContext is null!", new Object[0]);
        } else if (this.mImageResourceId != i) {
            this.mImageResourceId = i;
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
                    if (openRawFile == null) {
                        HiLog.error(TAG, " imageView resource is null", new Object[0]);
                        if (openRawFile != null) {
                            $closeResource(null, openRawFile);
                            return;
                        }
                        return;
                    }
                    nativeSetStream(this.mNativeViewPtr, openRawFile);
                    $closeResource(null, openRawFile);
                } catch (IOException unused) {
                    HiLog.error(TAG, " Fail to create image using resource id", new Object[0]);
                }
            } catch (IOException | NotExistException | WrongTypeException unused2) {
                HiLog.error(TAG, " can't find image resource id", new Object[0]);
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a8, code lost:
        if (r6 != null) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00aa, code lost:
        $closeResource(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ad, code lost:
        throw r0;
     */
    public void setPixelMap(int i) {
        HiLog.debug(TAG, " setPixelMap using resId: %{public}d", new Object[]{Integer.valueOf(i)});
        if (this.mContext == null) {
            HiLog.error(TAG, " mContext is null!", new Object[0]);
            return;
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
                if (openRawFile == null) {
                    HiLog.error(TAG, " imageView resource is null", new Object[0]);
                    if (openRawFile != null) {
                        $closeResource(null, openRawFile);
                        return;
                    }
                    return;
                }
                ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
                sourceOptions.formatHint = "image/png";
                ImageSource create = ImageSource.create(openRawFile, sourceOptions);
                if (create == null) {
                    HiLog.error(TAG, " create ImageSource is null", new Object[0]);
                    $closeResource(null, openRawFile);
                    return;
                }
                ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
                decodingOptions.desiredSize = new Size(0, 0);
                decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
                decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
                setPixelMap(create.createPixelmap(decodingOptions));
                $closeResource(null, openRawFile);
            } catch (IOException unused) {
                HiLog.error(TAG, " Fail to create image using resource id", new Object[0]);
            }
        } catch (IOException | NotExistException | WrongTypeException unused2) {
            HiLog.error(TAG, " can't find image resource id", new Object[0]);
        }
    }

    public void setClipDirection(int i) {
        int i2 = i & 3;
        if (i2 == 0) {
            i2 = 3;
        }
        this.mClipDirection = i2;
        nativeSetClipDirection(this.mNativeViewPtr, i2);
    }

    public int getClipDirection() {
        return this.mClipDirection;
    }

    public void setClipGravity(int i) {
        nativeSetClipGravity(this.mNativeViewPtr, maskClipGravity(i));
    }

    public int getClipGravity() {
        return nativeGetClipGravity(this.mNativeViewPtr);
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType != null) {
            nativeSetScaleType(this.mNativeViewPtr, scaleType.getValue());
        }
    }

    public ScaleType getScaleType() {
        return ScaleType.getByInt(nativeGetScaleType(this.mNativeViewPtr));
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
}
