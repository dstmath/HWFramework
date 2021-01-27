package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;

public final class PointerIcon implements Parcelable {
    public static final Parcelable.Creator<PointerIcon> CREATOR = new Parcelable.Creator<PointerIcon>() {
        /* class android.view.PointerIcon.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PointerIcon createFromParcel(Parcel in) {
            int type = in.readInt();
            if (type == 0) {
                return PointerIcon.getNullIcon();
            }
            int systemIconResourceId = in.readInt();
            if (systemIconResourceId == 0) {
                return PointerIcon.create(Bitmap.CREATOR.createFromParcel(in), in.readFloat(), in.readFloat());
            }
            PointerIcon icon = new PointerIcon(type);
            icon.mSystemIconResourceId = systemIconResourceId;
            return icon;
        }

        @Override // android.os.Parcelable.Creator
        public PointerIcon[] newArray(int size) {
            return new PointerIcon[size];
        }
    };
    private static final String TAG = "PointerIcon";
    public static final int TYPE_ALIAS = 1010;
    public static final int TYPE_ALL_SCROLL = 1013;
    public static final int TYPE_ARROW = 1000;
    public static final int TYPE_CELL = 1006;
    public static final int TYPE_CONTEXT_MENU = 1001;
    public static final int TYPE_COPY = 1011;
    public static final int TYPE_CROSSHAIR = 1007;
    public static final int TYPE_CUSTOM = -1;
    public static final int TYPE_DEFAULT = 1000;
    public static final int TYPE_GRAB = 1020;
    public static final int TYPE_GRABBING = 1021;
    public static final int TYPE_HAND = 1002;
    public static final int TYPE_HELP = 1003;
    public static final int TYPE_HORIZONTAL_DOUBLE_ARROW = 1014;
    public static final int TYPE_NOT_SPECIFIED = 1;
    public static final int TYPE_NO_DROP = 1012;
    public static final int TYPE_NULL = 0;
    private static final int TYPE_OEM_FIRST = 10000;
    public static final int TYPE_SPOT_ANCHOR = 2002;
    public static final int TYPE_SPOT_HOVER = 2000;
    public static final int TYPE_SPOT_TOUCH = 2001;
    public static final int TYPE_TEXT = 1008;
    public static final int TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW = 1017;
    public static final int TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW = 1016;
    public static final int TYPE_VERTICAL_DOUBLE_ARROW = 1015;
    public static final int TYPE_VERTICAL_TEXT = 1009;
    public static final int TYPE_WAIT = 1004;
    public static final int TYPE_ZOOM_IN = 1018;
    public static final int TYPE_ZOOM_OUT = 1019;
    private static final PointerIcon gNullIcon = new PointerIcon(0);
    private static final SparseArray<SparseArray<PointerIcon>> gSystemIconsByDisplay = new SparseArray<>();
    private static DisplayManager.DisplayListener sDisplayListener;
    private static boolean sUseLargeIcons = false;
    @UnsupportedAppUsage
    private Bitmap mBitmap;
    @UnsupportedAppUsage
    private Bitmap[] mBitmapFrames;
    @UnsupportedAppUsage
    private int mDurationPerFrame;
    @UnsupportedAppUsage
    private float mHotSpotX;
    @UnsupportedAppUsage
    private float mHotSpotY;
    private int mSystemIconResourceId;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final int mType;

    private PointerIcon(int type) {
        this.mType = type;
    }

    public static PointerIcon getNullIcon() {
        return gNullIcon;
    }

    public static PointerIcon getDefaultIcon(Context context) {
        return getSystemIcon(context, 1000);
    }

    public static PointerIcon getSystemIcon(Context context, int type) {
        SparseArray<PointerIcon> systemIcons;
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (type == 0) {
            return gNullIcon;
        } else {
            if (sDisplayListener == null) {
                registerDisplayListener(context);
            }
            int displayId = context.getDisplayId();
            synchronized (gSystemIconsByDisplay) {
                systemIcons = gSystemIconsByDisplay.get(displayId);
            }
            if (systemIcons == null) {
                SparseArray<PointerIcon> systemIcons2 = new SparseArray<>();
                synchronized (gSystemIconsByDisplay) {
                    gSystemIconsByDisplay.put(displayId, systemIcons2);
                }
                systemIcons = systemIcons2;
            }
            PointerIcon icon = systemIcons.get(type);
            if (icon != null) {
                return icon;
            }
            int typeIndex = getSystemIconTypeIndex(type);
            if (typeIndex == 0) {
                typeIndex = getSystemIconTypeIndex(1000);
            }
            TypedArray a = context.obtainStyledAttributes(null, R.styleable.Pointer, 0, sUseLargeIcons ? R.style.LargePointer : R.style.Pointer);
            int resourceId = a.getResourceId(typeIndex, -1);
            a.recycle();
            if (resourceId == -1) {
                Log.w(TAG, "Missing theme resources for pointer icon type " + type);
                return type == 1000 ? gNullIcon : getSystemIcon(context, 1000);
            }
            PointerIcon icon2 = new PointerIcon(type);
            if ((-16777216 & resourceId) == 16777216) {
                icon2.mSystemIconResourceId = resourceId;
            } else {
                icon2.loadResource(context, context.getResources(), resourceId);
            }
            systemIcons.append(type, icon2);
            return icon2;
        }
    }

    public static void setUseLargeIcons(boolean use) {
        sUseLargeIcons = use;
        synchronized (gSystemIconsByDisplay) {
            gSystemIconsByDisplay.clear();
        }
    }

    public static PointerIcon create(Bitmap bitmap, float hotSpotX, float hotSpotY) {
        if (bitmap != null) {
            validateHotSpot(bitmap, hotSpotX, hotSpotY);
            PointerIcon icon = new PointerIcon(-1);
            icon.mBitmap = bitmap;
            icon.mHotSpotX = hotSpotX;
            icon.mHotSpotY = hotSpotY;
            return icon;
        }
        throw new IllegalArgumentException("bitmap must not be null");
    }

    public static PointerIcon load(Resources resources, int resourceId) {
        if (resources != null) {
            PointerIcon icon = new PointerIcon(-1);
            icon.loadResource(null, resources, resourceId);
            return icon;
        }
        throw new IllegalArgumentException("resources must not be null");
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public PointerIcon load(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (this.mSystemIconResourceId == 0 || this.mBitmap != null) {
            return this;
        } else {
            PointerIcon result = new PointerIcon(this.mType);
            result.mSystemIconResourceId = this.mSystemIconResourceId;
            result.loadResource(context, context.getResources(), this.mSystemIconResourceId);
            return result;
        }
    }

    public int getType() {
        return this.mType;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mType);
        if (this.mType != 0) {
            out.writeInt(this.mSystemIconResourceId);
            if (this.mSystemIconResourceId == 0) {
                this.mBitmap.writeToParcel(out, flags);
                out.writeFloat(this.mHotSpotX);
                out.writeFloat(this.mHotSpotY);
            }
        }
    }

    public boolean equals(Object other) {
        int i;
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof PointerIcon)) {
            return false;
        }
        PointerIcon otherIcon = (PointerIcon) other;
        if (this.mType != otherIcon.mType || (i = this.mSystemIconResourceId) != otherIcon.mSystemIconResourceId) {
            return false;
        }
        if (i != 0 || (this.mBitmap == otherIcon.mBitmap && this.mHotSpotX == otherIcon.mHotSpotX && this.mHotSpotY == otherIcon.mHotSpotY)) {
            return true;
        }
        return false;
    }

    private Bitmap getBitmapFromDrawable(BitmapDrawable bitmapDrawable) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        int scaledWidth = bitmapDrawable.getIntrinsicWidth();
        int scaledHeight = bitmapDrawable.getIntrinsicHeight();
        if (scaledWidth == bitmap.getWidth() && scaledHeight == bitmap.getHeight()) {
            return bitmap;
        }
        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF dst = new RectF(0.0f, 0.0f, (float) scaledWidth, (float) scaledHeight);
        Bitmap scaled = Bitmap.createBitmap(scaledWidth, scaledHeight, bitmap.getConfig());
        Canvas canvas = new Canvas(scaled);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, src, dst, paint);
        return scaled;
    }

    private void loadResource(Context context, Resources resources, int resourceId) {
        Drawable drawable;
        Drawable drawable2;
        XmlResourceParser parser = resources.getXml(resourceId);
        try {
            XmlUtils.beginDocument(parser, "pointer-icon");
            TypedArray a = resources.obtainAttributes(parser, R.styleable.PointerIcon);
            int bitmapRes = a.getResourceId(0, 0);
            float hotSpotX = a.getDimension(1, 0.0f);
            float hotSpotY = a.getDimension(2, 0.0f);
            a.recycle();
            parser.close();
            if (bitmapRes != 0) {
                if (context == null) {
                    drawable = resources.getDrawable(bitmapRes);
                } else {
                    drawable = context.getDrawable(bitmapRes);
                }
                if (drawable instanceof AnimationDrawable) {
                    AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                    int frames = animationDrawable.getNumberOfFrames();
                    Drawable drawable3 = animationDrawable.getFrame(0);
                    if (frames == 1) {
                        Log.w(TAG, "Animation icon with single frame -- simply treating the first frame as a normal bitmap icon.");
                        drawable2 = drawable3;
                    } else {
                        this.mDurationPerFrame = animationDrawable.getDuration(0);
                        this.mBitmapFrames = new Bitmap[(frames - 1)];
                        int width = drawable3.getIntrinsicWidth();
                        int height = drawable3.getIntrinsicHeight();
                        for (int i = 1; i < frames; i++) {
                            Drawable drawableFrame = animationDrawable.getFrame(i);
                            if (!(drawableFrame instanceof BitmapDrawable)) {
                                throw new IllegalArgumentException("Frame of an animated pointer icon must refer to a bitmap drawable.");
                            } else if (drawableFrame.getIntrinsicWidth() == width && drawableFrame.getIntrinsicHeight() == height) {
                                this.mBitmapFrames[i - 1] = getBitmapFromDrawable((BitmapDrawable) drawableFrame);
                            } else {
                                throw new IllegalArgumentException("The bitmap size of " + i + "-th frame is different. All frames should have the exact same size and share the same hotspot.");
                            }
                        }
                        drawable2 = drawable3;
                    }
                    drawable = drawable2;
                }
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = getBitmapFromDrawable((BitmapDrawable) drawable);
                    validateHotSpot(bitmap, hotSpotX, hotSpotY);
                    this.mBitmap = bitmap;
                    this.mHotSpotX = hotSpotX;
                    this.mHotSpotY = hotSpotY;
                    return;
                }
                throw new IllegalArgumentException("<pointer-icon> bitmap attribute must refer to a bitmap drawable.");
            }
            throw new IllegalArgumentException("<pointer-icon> is missing bitmap attribute.");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Exception parsing pointer icon resource.", ex);
        } catch (Throwable th) {
            parser.close();
            throw th;
        }
    }

    private static void validateHotSpot(Bitmap bitmap, float hotSpotX, float hotSpotY) {
        if (hotSpotX < 0.0f || hotSpotX >= ((float) bitmap.getWidth())) {
            throw new IllegalArgumentException("x hotspot lies outside of the bitmap area");
        } else if (hotSpotY < 0.0f || hotSpotY >= ((float) bitmap.getHeight())) {
            throw new IllegalArgumentException("y hotspot lies outside of the bitmap area");
        }
    }

    private static int getSystemIconTypeIndex(int type) {
        switch (type) {
            case 1000:
                return 2;
            case 1001:
                return 4;
            case 1002:
                return 9;
            case 1003:
                return 10;
            case 1004:
                return 21;
            default:
                switch (type) {
                    case 1006:
                        return 3;
                    case 1007:
                        return 6;
                    case 1008:
                        return 16;
                    case 1009:
                        return 20;
                    case 1010:
                        return 0;
                    case 1011:
                        return 5;
                    case 1012:
                        return 12;
                    case 1013:
                        return 1;
                    case 1014:
                        return 11;
                    case 1015:
                        return 19;
                    case 1016:
                        return 18;
                    case 1017:
                        return 17;
                    case 1018:
                        return 22;
                    case 1019:
                        return 23;
                    case 1020:
                        return 7;
                    case 1021:
                        return 8;
                    default:
                        switch (type) {
                            case 2000:
                                return 14;
                            case 2001:
                                return 15;
                            case 2002:
                                return 13;
                            default:
                                return 0;
                        }
                }
        }
    }

    private static void registerDisplayListener(Context context) {
        sDisplayListener = new DisplayManager.DisplayListener() {
            /* class android.view.PointerIcon.AnonymousClass2 */

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int displayId) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int displayId) {
                synchronized (PointerIcon.gSystemIconsByDisplay) {
                    PointerIcon.gSystemIconsByDisplay.remove(displayId);
                }
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int displayId) {
                synchronized (PointerIcon.gSystemIconsByDisplay) {
                    PointerIcon.gSystemIconsByDisplay.remove(displayId);
                }
            }
        };
        ((DisplayManager) context.getSystemService(DisplayManager.class)).registerDisplayListener(sDisplayListener, null);
    }
}
