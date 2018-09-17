package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;

public final class PointerIcon implements Parcelable {
    public static final Creator<PointerIcon> CREATOR = new Creator<PointerIcon>() {
        public PointerIcon createFromParcel(Parcel in) {
            int type = in.readInt();
            if (type == 0) {
                return PointerIcon.getNullIcon();
            }
            int systemIconResourceId = in.readInt();
            if (systemIconResourceId == 0) {
                return PointerIcon.create((Bitmap) Bitmap.CREATOR.createFromParcel(in), in.readFloat(), in.readFloat());
            }
            PointerIcon icon = new PointerIcon(type, null);
            icon.mSystemIconResourceId = systemIconResourceId;
            return icon;
        }

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
    private static final SparseArray<PointerIcon> gSystemIcons = new SparseArray();
    private static boolean sUseLargeIcons = false;
    private Bitmap mBitmap;
    private Bitmap[] mBitmapFrames;
    private int mDurationPerFrame;
    private float mHotSpotX;
    private float mHotSpotY;
    private int mSystemIconResourceId;
    private final int mType;

    /* synthetic */ PointerIcon(int type, PointerIcon -this1) {
        this(type);
    }

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
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (type == 0) {
            return gNullIcon;
        } else {
            PointerIcon icon = (PointerIcon) gSystemIcons.get(type);
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
                PointerIcon pointerIcon;
                Log.w(TAG, "Missing theme resources for pointer icon type " + type);
                if (type == 1000) {
                    pointerIcon = gNullIcon;
                } else {
                    pointerIcon = getSystemIcon(context, 1000);
                }
                return pointerIcon;
            }
            icon = new PointerIcon(type);
            if ((-16777216 & resourceId) == 16777216) {
                icon.mSystemIconResourceId = resourceId;
            } else {
                icon.loadResource(context, context.getResources(), resourceId);
            }
            gSystemIcons.append(type, icon);
            return icon;
        }
    }

    public static void setUseLargeIcons(boolean use) {
        sUseLargeIcons = use;
        gSystemIcons.clear();
    }

    public static PointerIcon create(Bitmap bitmap, float hotSpotX, float hotSpotY) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap must not be null");
        }
        validateHotSpot(bitmap, hotSpotX, hotSpotY);
        PointerIcon icon = new PointerIcon(-1);
        icon.mBitmap = bitmap;
        icon.mHotSpotX = hotSpotX;
        icon.mHotSpotY = hotSpotY;
        return icon;
    }

    public static PointerIcon load(Resources resources, int resourceId) {
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null");
        }
        PointerIcon icon = new PointerIcon(-1);
        icon.loadResource(null, resources, resourceId);
        return icon;
    }

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

    public int describeContents() {
        return 0;
    }

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
        if (this == other) {
            return true;
        }
        if (other == null || ((other instanceof PointerIcon) ^ 1) != 0) {
            return false;
        }
        PointerIcon otherIcon = (PointerIcon) other;
        if (this.mType == otherIcon.mType && this.mSystemIconResourceId == otherIcon.mSystemIconResourceId) {
            return this.mSystemIconResourceId != 0 || (this.mBitmap == otherIcon.mBitmap && this.mHotSpotX == otherIcon.mHotSpotX && this.mHotSpotY == otherIcon.mHotSpotY);
        } else {
            return false;
        }
    }

    private void loadResource(Context context, Resources resources, int resourceId) {
        XmlResourceParser parser = resources.getXml(resourceId);
        try {
            XmlUtils.beginDocument(parser, "pointer-icon");
            TypedArray a = resources.obtainAttributes(parser, R.styleable.PointerIcon);
            int bitmapRes = a.getResourceId(0, 0);
            float hotSpotX = a.getDimension(1, 0.0f);
            float hotSpotY = a.getDimension(2, 0.0f);
            a.recycle();
            parser.close();
            if (bitmapRes == 0) {
                throw new IllegalArgumentException("<pointer-icon> is missing bitmap attribute.");
            }
            Drawable drawable;
            if (context == null) {
                drawable = resources.getDrawable(bitmapRes);
            } else {
                drawable = context.getDrawable(bitmapRes);
            }
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                int frames = animationDrawable.getNumberOfFrames();
                drawable = animationDrawable.getFrame(0);
                if (frames == 1) {
                    Log.w(TAG, "Animation icon with single frame -- simply treating the first frame as a normal bitmap icon.");
                } else {
                    this.mDurationPerFrame = animationDrawable.getDuration(0);
                    this.mBitmapFrames = new Bitmap[(frames - 1)];
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    int i = 1;
                    while (i < frames) {
                        Drawable drawableFrame = animationDrawable.getFrame(i);
                        if (!(drawableFrame instanceof BitmapDrawable)) {
                            throw new IllegalArgumentException("Frame of an animated pointer icon must refer to a bitmap drawable.");
                        } else if (drawableFrame.getIntrinsicWidth() == width && drawableFrame.getIntrinsicHeight() == height) {
                            this.mBitmapFrames[i - 1] = ((BitmapDrawable) drawableFrame).getBitmap();
                            i++;
                        } else {
                            throw new IllegalArgumentException("The bitmap size of " + i + "-th frame " + "is different. All frames should have the exact same size and " + "share the same hotspot.");
                        }
                    }
                }
            }
            if (drawable instanceof BitmapDrawable) {
                this.mBitmap = ((BitmapDrawable) drawable).getBitmap();
                this.mHotSpotX = hotSpotX;
                this.mHotSpotY = hotSpotY;
                return;
            }
            throw new IllegalArgumentException("<pointer-icon> bitmap attribute must refer to a bitmap drawable.");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Exception parsing pointer icon resource.", ex);
        } catch (Throwable th) {
            parser.close();
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
