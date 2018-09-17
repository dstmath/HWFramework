package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.ComplexColor;
import android.content.res.GradientColor;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable.ConstantState;
import android.speech.tts.TextToSpeech.Engine;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.PathParser.PathData;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.util.VirtualRefBasePtr;
import dalvik.system.VMRuntime;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VectorDrawable extends Drawable {
    private static final String LOGTAG = null;
    private static final String SHAPE_CLIP_PATH = "clip-path";
    private static final String SHAPE_GROUP = "group";
    private static final String SHAPE_PATH = "path";
    private static final String SHAPE_VECTOR = "vector";
    private ColorFilter mColorFilter;
    private boolean mDpiScaledDirty;
    private int mDpiScaledHeight;
    private Insets mDpiScaledInsets;
    private int mDpiScaledWidth;
    private boolean mMutated;
    private int mTargetDensity;
    private PorterDuffColorFilter mTintFilter;
    private final Rect mTmpBounds;
    private VectorDrawableState mVectorState;

    static abstract class VObject {
        VirtualRefBasePtr mTreePtr;

        abstract void applyTheme(Theme theme);

        abstract boolean canApplyTheme();

        abstract long getNativePtr();

        abstract int getNativeSize();

        abstract void inflate(Resources resources, AttributeSet attributeSet, Theme theme);

        abstract boolean isStateful();

        abstract boolean onStateChange(int[] iArr);

        VObject() {
            this.mTreePtr = null;
        }

        boolean isTreeValid() {
            return (this.mTreePtr == null || this.mTreePtr.get() == 0) ? false : true;
        }

        void setTree(VirtualRefBasePtr ptr) {
            this.mTreePtr = ptr;
        }
    }

    static abstract class VPath extends VObject {
        int mChangingConfigurations;
        protected PathData mPathData;
        String mPathName;

        public VPath() {
            this.mPathData = null;
        }

        public VPath(VPath copy) {
            PathData pathData = null;
            this.mPathData = null;
            this.mPathName = copy.mPathName;
            this.mChangingConfigurations = copy.mChangingConfigurations;
            if (copy.mPathData != null) {
                pathData = new PathData(copy.mPathData);
            }
            this.mPathData = pathData;
        }

        public String getPathName() {
            return this.mPathName;
        }

        public PathData getPathData() {
            return this.mPathData;
        }

        public void setPathData(PathData pathData) {
            this.mPathData.setPathData(pathData);
            if (isTreeValid()) {
                VectorDrawable.nSetPathData(getNativePtr(), this.mPathData.getNativePtr());
            }
        }
    }

    private static class VClipPath extends VPath {
        private static final int NATIVE_ALLOCATION_SIZE = 120;
        private final long mNativePtr;

        public VClipPath() {
            this.mNativePtr = VectorDrawable.nCreateClipPath();
        }

        public VClipPath(VClipPath copy) {
            super(copy);
            this.mNativePtr = VectorDrawable.nCreateClipPath(copy.mNativePtr);
        }

        public long getNativePtr() {
            return this.mNativePtr;
        }

        public void inflate(Resources r, AttributeSet attrs, Theme theme) {
            TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.VectorDrawableClipPath);
            updateStateFromTypedArray(a);
            a.recycle();
        }

        public boolean canApplyTheme() {
            return false;
        }

        public void applyTheme(Theme theme) {
        }

        public boolean onStateChange(int[] stateSet) {
            return false;
        }

        public boolean isStateful() {
            return false;
        }

        int getNativeSize() {
            return NATIVE_ALLOCATION_SIZE;
        }

        private void updateStateFromTypedArray(TypedArray a) {
            this.mChangingConfigurations |= a.getChangingConfigurations();
            String pathName = a.getString(0);
            if (pathName != null) {
                this.mPathName = pathName;
                VectorDrawable.nSetName(this.mNativePtr, this.mPathName);
            }
            String pathDataString = a.getString(1);
            if (pathDataString != null) {
                this.mPathData = new PathData(pathDataString);
                VectorDrawable.nSetPathString(this.mNativePtr, pathDataString, pathDataString.length());
            }
        }
    }

    static class VFullPath extends VPath {
        private static final int FILL_ALPHA_INDEX = 4;
        private static final int FILL_COLOR_INDEX = 3;
        private static final int FILL_TYPE_INDEX = 11;
        private static final int NATIVE_ALLOCATION_SIZE = 264;
        private static final int STROKE_ALPHA_INDEX = 2;
        private static final int STROKE_COLOR_INDEX = 1;
        private static final int STROKE_LINE_CAP_INDEX = 8;
        private static final int STROKE_LINE_JOIN_INDEX = 9;
        private static final int STROKE_MITER_LIMIT_INDEX = 10;
        private static final int STROKE_WIDTH_INDEX = 0;
        private static final int TOTAL_PROPERTY_COUNT = 12;
        private static final int TRIM_PATH_END_INDEX = 6;
        private static final int TRIM_PATH_OFFSET_INDEX = 7;
        private static final int TRIM_PATH_START_INDEX = 5;
        private static final HashMap<String, Integer> sPropertyMap = null;
        ComplexColor mFillColors;
        private final long mNativePtr;
        private byte[] mPropertyData;
        ComplexColor mStrokeColors;
        private int[] mThemeAttrs;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.VectorDrawable.VFullPath.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.VectorDrawable.VFullPath.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.VectorDrawable.VFullPath.<clinit>():void");
        }

        public VFullPath() {
            this.mStrokeColors = null;
            this.mFillColors = null;
            this.mNativePtr = VectorDrawable.nCreateFullPath();
        }

        public VFullPath(VFullPath copy) {
            super(copy);
            this.mStrokeColors = null;
            this.mFillColors = null;
            this.mNativePtr = VectorDrawable.nCreateFullPath(copy.mNativePtr);
            this.mThemeAttrs = copy.mThemeAttrs;
            this.mStrokeColors = copy.mStrokeColors;
            this.mFillColors = copy.mFillColors;
        }

        int getPropertyIndex(String propertyName) {
            if (sPropertyMap.containsKey(propertyName)) {
                return ((Integer) sPropertyMap.get(propertyName)).intValue();
            }
            return -1;
        }

        public boolean onStateChange(int[] stateSet) {
            boolean z = false;
            if (this.mStrokeColors != null && (this.mStrokeColors instanceof ColorStateList)) {
                int oldStrokeColor = getStrokeColor();
                int newStrokeColor = ((ColorStateList) this.mStrokeColors).getColorForState(stateSet, oldStrokeColor);
                if (oldStrokeColor != newStrokeColor) {
                    z = STROKE_COLOR_INDEX;
                } else {
                    z = STROKE_WIDTH_INDEX;
                }
                if (oldStrokeColor != newStrokeColor) {
                    VectorDrawable.nSetStrokeColor(this.mNativePtr, newStrokeColor);
                }
            }
            if (this.mFillColors != null && (this.mFillColors instanceof ColorStateList)) {
                int i;
                int oldFillColor = getFillColor();
                int newFillColor = ((ColorStateList) this.mFillColors).getColorForState(stateSet, oldFillColor);
                if (oldFillColor != newFillColor) {
                    i = STROKE_COLOR_INDEX;
                } else {
                    i = STROKE_WIDTH_INDEX;
                }
                z |= i;
                if (oldFillColor != newFillColor) {
                    VectorDrawable.nSetFillColor(this.mNativePtr, newFillColor);
                }
            }
            return z;
        }

        public boolean isStateful() {
            return (this.mStrokeColors == null && this.mFillColors == null) ? false : true;
        }

        int getNativeSize() {
            return NATIVE_ALLOCATION_SIZE;
        }

        public long getNativePtr() {
            return this.mNativePtr;
        }

        public void inflate(Resources r, AttributeSet attrs, Theme theme) {
            TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.VectorDrawablePath);
            updateStateFromTypedArray(a);
            a.recycle();
        }

        private void updateStateFromTypedArray(TypedArray a) {
            if (this.mPropertyData == null) {
                this.mPropertyData = new byte[48];
            }
            if (VectorDrawable.nGetFullPathProperties(this.mNativePtr, this.mPropertyData, 48)) {
                ByteBuffer properties = ByteBuffer.wrap(this.mPropertyData);
                properties.order(ByteOrder.nativeOrder());
                float strokeWidth = properties.getFloat(STROKE_WIDTH_INDEX);
                int strokeColor = properties.getInt(FILL_ALPHA_INDEX);
                float strokeAlpha = properties.getFloat(STROKE_LINE_CAP_INDEX);
                int fillColor = properties.getInt(TOTAL_PROPERTY_COUNT);
                float fillAlpha = properties.getFloat(16);
                float trimPathStart = properties.getFloat(20);
                float trimPathEnd = properties.getFloat(24);
                float trimPathOffset = properties.getFloat(28);
                int strokeLineCap = properties.getInt(32);
                int strokeLineJoin = properties.getInt(36);
                float strokeMiterLimit = properties.getFloat(40);
                int fillType = properties.getInt(44);
                Shader shader = null;
                Shader shader2 = null;
                this.mChangingConfigurations |= a.getChangingConfigurations();
                this.mThemeAttrs = a.extractThemeAttrs();
                String pathName = a.getString(STROKE_WIDTH_INDEX);
                if (pathName != null) {
                    this.mPathName = pathName;
                    VectorDrawable.nSetName(this.mNativePtr, this.mPathName);
                }
                String pathString = a.getString(STROKE_ALPHA_INDEX);
                if (pathString != null) {
                    this.mPathData = new PathData(pathString);
                    VectorDrawable.nSetPathString(this.mNativePtr, pathString, pathString.length());
                }
                ComplexColor fillColors = a.getComplexColor(STROKE_COLOR_INDEX);
                if (fillColors != null) {
                    if (fillColors instanceof GradientColor) {
                        this.mFillColors = fillColors;
                        shader = ((GradientColor) fillColors).getShader();
                    } else if (fillColors.isStateful()) {
                        this.mFillColors = fillColors;
                    } else {
                        this.mFillColors = null;
                    }
                    fillColor = fillColors.getDefaultColor();
                }
                ComplexColor strokeColors = a.getComplexColor(FILL_COLOR_INDEX);
                if (strokeColors != null) {
                    if (strokeColors instanceof GradientColor) {
                        this.mStrokeColors = strokeColors;
                        shader2 = ((GradientColor) strokeColors).getShader();
                    } else if (strokeColors.isStateful()) {
                        this.mStrokeColors = strokeColors;
                    } else {
                        this.mStrokeColors = null;
                    }
                    strokeColor = strokeColors.getDefaultColor();
                }
                VectorDrawable.nUpdateFullPathFillGradient(this.mNativePtr, shader != null ? shader.getNativeInstance() : 0);
                VectorDrawable.nUpdateFullPathStrokeGradient(this.mNativePtr, shader2 != null ? shader2.getNativeInstance() : 0);
                fillAlpha = a.getFloat(TOTAL_PROPERTY_COUNT, fillAlpha);
                strokeLineCap = a.getInt(STROKE_LINE_CAP_INDEX, strokeLineCap);
                strokeLineJoin = a.getInt(STROKE_LINE_JOIN_INDEX, strokeLineJoin);
                strokeMiterLimit = a.getFloat(STROKE_MITER_LIMIT_INDEX, strokeMiterLimit);
                VectorDrawable.nUpdateFullPathProperties(this.mNativePtr, a.getFloat(FILL_ALPHA_INDEX, strokeWidth), strokeColor, a.getFloat(FILL_TYPE_INDEX, strokeAlpha), fillColor, fillAlpha, a.getFloat(TRIM_PATH_START_INDEX, trimPathStart), a.getFloat(TRIM_PATH_END_INDEX, trimPathEnd), a.getFloat(TRIM_PATH_OFFSET_INDEX, trimPathOffset), strokeMiterLimit, strokeLineCap, strokeLineJoin, a.getInt(13, fillType));
                return;
            }
            throw new RuntimeException("Error: inconsistent property count");
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs != null) {
                return true;
            }
            boolean fillCanApplyTheme = canComplexColorApplyTheme(this.mFillColors);
            boolean strokeCanApplyTheme = canComplexColorApplyTheme(this.mStrokeColors);
            if (fillCanApplyTheme || strokeCanApplyTheme) {
                return true;
            }
            return false;
        }

        public void applyTheme(Theme t) {
            if (this.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(this.mThemeAttrs, R.styleable.VectorDrawablePath);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            boolean fillCanApplyTheme = canComplexColorApplyTheme(this.mFillColors);
            boolean strokeCanApplyTheme = canComplexColorApplyTheme(this.mStrokeColors);
            if (fillCanApplyTheme) {
                this.mFillColors = this.mFillColors.obtainForTheme(t);
                if (this.mFillColors instanceof GradientColor) {
                    VectorDrawable.nUpdateFullPathFillGradient(this.mNativePtr, ((GradientColor) this.mFillColors).getShader().getNativeInstance());
                } else if (this.mFillColors instanceof ColorStateList) {
                    VectorDrawable.nSetFillColor(this.mNativePtr, this.mFillColors.getDefaultColor());
                }
            }
            if (strokeCanApplyTheme) {
                this.mStrokeColors = this.mStrokeColors.obtainForTheme(t);
                if (this.mStrokeColors instanceof GradientColor) {
                    VectorDrawable.nUpdateFullPathStrokeGradient(this.mNativePtr, ((GradientColor) this.mStrokeColors).getShader().getNativeInstance());
                } else if (this.mStrokeColors instanceof ColorStateList) {
                    VectorDrawable.nSetStrokeColor(this.mNativePtr, this.mStrokeColors.getDefaultColor());
                }
            }
        }

        private boolean canComplexColorApplyTheme(ComplexColor complexColor) {
            return complexColor != null ? complexColor.canApplyTheme() : false;
        }

        int getStrokeColor() {
            return isTreeValid() ? VectorDrawable.nGetStrokeColor(this.mNativePtr) : STROKE_WIDTH_INDEX;
        }

        void setStrokeColor(int strokeColor) {
            this.mStrokeColors = null;
            if (isTreeValid()) {
                VectorDrawable.nSetStrokeColor(this.mNativePtr, strokeColor);
            }
        }

        float getStrokeWidth() {
            return isTreeValid() ? VectorDrawable.nGetStrokeWidth(this.mNativePtr) : 0.0f;
        }

        void setStrokeWidth(float strokeWidth) {
            if (isTreeValid()) {
                VectorDrawable.nSetStrokeWidth(this.mNativePtr, strokeWidth);
            }
        }

        float getStrokeAlpha() {
            return isTreeValid() ? VectorDrawable.nGetStrokeAlpha(this.mNativePtr) : 0.0f;
        }

        void setStrokeAlpha(float strokeAlpha) {
            if (isTreeValid()) {
                VectorDrawable.nSetStrokeAlpha(this.mNativePtr, strokeAlpha);
            }
        }

        int getFillColor() {
            return isTreeValid() ? VectorDrawable.nGetFillColor(this.mNativePtr) : STROKE_WIDTH_INDEX;
        }

        void setFillColor(int fillColor) {
            this.mFillColors = null;
            if (isTreeValid()) {
                VectorDrawable.nSetFillColor(this.mNativePtr, fillColor);
            }
        }

        float getFillAlpha() {
            return isTreeValid() ? VectorDrawable.nGetFillAlpha(this.mNativePtr) : 0.0f;
        }

        void setFillAlpha(float fillAlpha) {
            if (isTreeValid()) {
                VectorDrawable.nSetFillAlpha(this.mNativePtr, fillAlpha);
            }
        }

        float getTrimPathStart() {
            return isTreeValid() ? VectorDrawable.nGetTrimPathStart(this.mNativePtr) : 0.0f;
        }

        void setTrimPathStart(float trimPathStart) {
            if (isTreeValid()) {
                VectorDrawable.nSetTrimPathStart(this.mNativePtr, trimPathStart);
            }
        }

        float getTrimPathEnd() {
            return isTreeValid() ? VectorDrawable.nGetTrimPathEnd(this.mNativePtr) : 0.0f;
        }

        void setTrimPathEnd(float trimPathEnd) {
            if (isTreeValid()) {
                VectorDrawable.nSetTrimPathEnd(this.mNativePtr, trimPathEnd);
            }
        }

        float getTrimPathOffset() {
            return isTreeValid() ? VectorDrawable.nGetTrimPathOffset(this.mNativePtr) : 0.0f;
        }

        void setTrimPathOffset(float trimPathOffset) {
            if (isTreeValid()) {
                VectorDrawable.nSetTrimPathOffset(this.mNativePtr, trimPathOffset);
            }
        }
    }

    static class VGroup extends VObject {
        private static final int NATIVE_ALLOCATION_SIZE = 100;
        private static final int PIVOT_X_INDEX = 1;
        private static final int PIVOT_Y_INDEX = 2;
        private static final int ROTATE_INDEX = 0;
        private static final int SCALE_X_INDEX = 3;
        private static final int SCALE_Y_INDEX = 4;
        private static final int TRANSFORM_PROPERTY_COUNT = 7;
        private static final int TRANSLATE_X_INDEX = 5;
        private static final int TRANSLATE_Y_INDEX = 6;
        private static final HashMap<String, Integer> sPropertyMap = null;
        private int mChangingConfigurations;
        private final ArrayList<VObject> mChildren;
        private String mGroupName;
        private boolean mIsStateful;
        private final long mNativePtr;
        private int[] mThemeAttrs;
        private float[] mTransform;

        /* renamed from: android.graphics.drawable.VectorDrawable.VGroup.1 */
        static class AnonymousClass1 extends HashMap<String, Integer> {
            AnonymousClass1() {
                put("translateX", Integer.valueOf(VGroup.TRANSLATE_X_INDEX));
                put("translateY", Integer.valueOf(VGroup.TRANSLATE_Y_INDEX));
                put("scaleX", Integer.valueOf(VGroup.SCALE_X_INDEX));
                put("scaleY", Integer.valueOf(VGroup.SCALE_Y_INDEX));
                put("pivotX", Integer.valueOf(VGroup.PIVOT_X_INDEX));
                put("pivotY", Integer.valueOf(VGroup.PIVOT_Y_INDEX));
                put("rotation", Integer.valueOf(VGroup.ROTATE_INDEX));
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.VectorDrawable.VGroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.VectorDrawable.VGroup.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.VectorDrawable.VGroup.<clinit>():void");
        }

        static int getPropertyIndex(String propertyName) {
            if (sPropertyMap.containsKey(propertyName)) {
                return ((Integer) sPropertyMap.get(propertyName)).intValue();
            }
            return -1;
        }

        public VGroup(VGroup copy, ArrayMap<String, Object> targetsMap) {
            this.mChildren = new ArrayList();
            this.mGroupName = null;
            this.mIsStateful = copy.mIsStateful;
            this.mThemeAttrs = copy.mThemeAttrs;
            this.mGroupName = copy.mGroupName;
            this.mChangingConfigurations = copy.mChangingConfigurations;
            if (this.mGroupName != null) {
                targetsMap.put(this.mGroupName, this);
            }
            this.mNativePtr = VectorDrawable.nCreateGroup(copy.mNativePtr);
            ArrayList<VObject> children = copy.mChildren;
            for (int i = ROTATE_INDEX; i < children.size(); i += PIVOT_X_INDEX) {
                VObject copyChild = (VObject) children.get(i);
                if (copyChild instanceof VGroup) {
                    addChild(new VGroup((VGroup) copyChild, targetsMap));
                } else {
                    VPath newPath;
                    if (copyChild instanceof VFullPath) {
                        newPath = new VFullPath((VFullPath) copyChild);
                    } else if (copyChild instanceof VClipPath) {
                        newPath = new VClipPath((VClipPath) copyChild);
                    } else {
                        throw new IllegalStateException("Unknown object in the tree!");
                    }
                    addChild(newPath);
                    if (newPath.mPathName != null) {
                        targetsMap.put(newPath.mPathName, newPath);
                    }
                }
            }
        }

        public VGroup() {
            this.mChildren = new ArrayList();
            this.mGroupName = null;
            this.mNativePtr = VectorDrawable.nCreateGroup();
        }

        public String getGroupName() {
            return this.mGroupName;
        }

        public void addChild(VObject child) {
            VectorDrawable.nAddChild(this.mNativePtr, child.getNativePtr());
            this.mChildren.add(child);
            this.mIsStateful |= child.isStateful();
        }

        public void setTree(VirtualRefBasePtr treeRoot) {
            super.setTree(treeRoot);
            for (int i = ROTATE_INDEX; i < this.mChildren.size(); i += PIVOT_X_INDEX) {
                ((VObject) this.mChildren.get(i)).setTree(treeRoot);
            }
        }

        public long getNativePtr() {
            return this.mNativePtr;
        }

        public void inflate(Resources res, AttributeSet attrs, Theme theme) {
            TypedArray a = Drawable.obtainAttributes(res, theme, attrs, R.styleable.VectorDrawableGroup);
            updateStateFromTypedArray(a);
            a.recycle();
        }

        void updateStateFromTypedArray(TypedArray a) {
            this.mChangingConfigurations |= a.getChangingConfigurations();
            this.mThemeAttrs = a.extractThemeAttrs();
            if (this.mTransform == null) {
                this.mTransform = new float[TRANSFORM_PROPERTY_COUNT];
            }
            if (VectorDrawable.nGetGroupProperties(this.mNativePtr, this.mTransform, TRANSFORM_PROPERTY_COUNT)) {
                float rotate = a.getFloat(TRANSLATE_X_INDEX, this.mTransform[ROTATE_INDEX]);
                float pivotX = a.getFloat(PIVOT_X_INDEX, this.mTransform[PIVOT_X_INDEX]);
                float pivotY = a.getFloat(PIVOT_Y_INDEX, this.mTransform[PIVOT_Y_INDEX]);
                float scaleX = a.getFloat(SCALE_X_INDEX, this.mTransform[SCALE_X_INDEX]);
                float scaleY = a.getFloat(SCALE_Y_INDEX, this.mTransform[SCALE_Y_INDEX]);
                float translateX = a.getFloat(TRANSLATE_Y_INDEX, this.mTransform[TRANSLATE_X_INDEX]);
                float translateY = a.getFloat(TRANSFORM_PROPERTY_COUNT, this.mTransform[TRANSLATE_Y_INDEX]);
                String groupName = a.getString(ROTATE_INDEX);
                if (groupName != null) {
                    this.mGroupName = groupName;
                    VectorDrawable.nSetName(this.mNativePtr, this.mGroupName);
                }
                VectorDrawable.nUpdateGroupProperties(this.mNativePtr, rotate, pivotX, pivotY, scaleX, scaleY, translateX, translateY);
                return;
            }
            throw new RuntimeException("Error: inconsistent property count");
        }

        public boolean onStateChange(int[] stateSet) {
            boolean changed = false;
            ArrayList<VObject> children = this.mChildren;
            int count = children.size();
            for (int i = ROTATE_INDEX; i < count; i += PIVOT_X_INDEX) {
                VObject child = (VObject) children.get(i);
                if (child.isStateful()) {
                    changed |= child.onStateChange(stateSet);
                }
            }
            return changed;
        }

        public boolean isStateful() {
            return this.mIsStateful;
        }

        int getNativeSize() {
            int size = NATIVE_ALLOCATION_SIZE;
            for (int i = ROTATE_INDEX; i < this.mChildren.size(); i += PIVOT_X_INDEX) {
                size += ((VObject) this.mChildren.get(i)).getNativeSize();
            }
            return size;
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs != null) {
                return true;
            }
            ArrayList<VObject> children = this.mChildren;
            int count = children.size();
            for (int i = ROTATE_INDEX; i < count; i += PIVOT_X_INDEX) {
                if (((VObject) children.get(i)).canApplyTheme()) {
                    return true;
                }
            }
            return false;
        }

        public void applyTheme(Theme t) {
            if (this.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(this.mThemeAttrs, R.styleable.VectorDrawableGroup);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            ArrayList<VObject> children = this.mChildren;
            int count = children.size();
            for (int i = ROTATE_INDEX; i < count; i += PIVOT_X_INDEX) {
                VObject child = (VObject) children.get(i);
                if (child.canApplyTheme()) {
                    child.applyTheme(t);
                    this.mIsStateful |= child.isStateful();
                }
            }
        }

        public float getRotation() {
            return isTreeValid() ? VectorDrawable.nGetRotation(this.mNativePtr) : 0.0f;
        }

        public void setRotation(float rotation) {
            if (isTreeValid()) {
                VectorDrawable.nSetRotation(this.mNativePtr, rotation);
            }
        }

        public float getPivotX() {
            return isTreeValid() ? VectorDrawable.nGetPivotX(this.mNativePtr) : 0.0f;
        }

        public void setPivotX(float pivotX) {
            if (isTreeValid()) {
                VectorDrawable.nSetPivotX(this.mNativePtr, pivotX);
            }
        }

        public float getPivotY() {
            return isTreeValid() ? VectorDrawable.nGetPivotY(this.mNativePtr) : 0.0f;
        }

        public void setPivotY(float pivotY) {
            if (isTreeValid()) {
                VectorDrawable.nSetPivotY(this.mNativePtr, pivotY);
            }
        }

        public float getScaleX() {
            return isTreeValid() ? VectorDrawable.nGetScaleX(this.mNativePtr) : 0.0f;
        }

        public void setScaleX(float scaleX) {
            if (isTreeValid()) {
                VectorDrawable.nSetScaleX(this.mNativePtr, scaleX);
            }
        }

        public float getScaleY() {
            return isTreeValid() ? VectorDrawable.nGetScaleY(this.mNativePtr) : 0.0f;
        }

        public void setScaleY(float scaleY) {
            if (isTreeValid()) {
                VectorDrawable.nSetScaleY(this.mNativePtr, scaleY);
            }
        }

        public float getTranslateX() {
            return isTreeValid() ? VectorDrawable.nGetTranslateX(this.mNativePtr) : 0.0f;
        }

        public void setTranslateX(float translateX) {
            if (isTreeValid()) {
                VectorDrawable.nSetTranslateX(this.mNativePtr, translateX);
            }
        }

        public float getTranslateY() {
            return isTreeValid() ? VectorDrawable.nGetTranslateY(this.mNativePtr) : 0.0f;
        }

        public void setTranslateY(float translateY) {
            if (isTreeValid()) {
                VectorDrawable.nSetTranslateY(this.mNativePtr, translateY);
            }
        }
    }

    static class VectorDrawableState extends ConstantState {
        private static final int NATIVE_ALLOCATION_SIZE = 316;
        private int mAllocationOfAllNodes;
        boolean mAutoMirrored;
        float mBaseHeight;
        float mBaseWidth;
        boolean mCacheDirty;
        boolean mCachedAutoMirrored;
        int[] mCachedThemeAttrs;
        ColorStateList mCachedTint;
        Mode mCachedTintMode;
        int mChangingConfigurations;
        int mDensity;
        int mLastHWCachePixelCount;
        int mLastSWCachePixelCount;
        VirtualRefBasePtr mNativeTree;
        Insets mOpticalInsets;
        VGroup mRootGroup;
        String mRootName;
        int[] mThemeAttrs;
        ColorStateList mTint;
        Mode mTintMode;
        final ArrayMap<String, Object> mVGTargetsMap;
        float mViewportHeight;
        float mViewportWidth;

        public VectorDrawableState(VectorDrawableState copy) {
            this.mTint = null;
            this.mTintMode = VectorDrawable.DEFAULT_TINT_MODE;
            this.mBaseWidth = 0.0f;
            this.mBaseHeight = 0.0f;
            this.mViewportWidth = 0.0f;
            this.mViewportHeight = 0.0f;
            this.mOpticalInsets = Insets.NONE;
            this.mRootName = null;
            this.mNativeTree = null;
            this.mDensity = Const.CODE_G3_RANGE_START;
            this.mVGTargetsMap = new ArrayMap();
            this.mLastSWCachePixelCount = 0;
            this.mLastHWCachePixelCount = 0;
            this.mAllocationOfAllNodes = 0;
            if (copy != null) {
                this.mThemeAttrs = copy.mThemeAttrs;
                this.mChangingConfigurations = copy.mChangingConfigurations;
                this.mTint = copy.mTint;
                this.mTintMode = copy.mTintMode;
                this.mAutoMirrored = copy.mAutoMirrored;
                this.mRootGroup = new VGroup(copy.mRootGroup, this.mVGTargetsMap);
                createNativeTreeFromCopy(copy, this.mRootGroup);
                this.mBaseWidth = copy.mBaseWidth;
                this.mBaseHeight = copy.mBaseHeight;
                setViewportSize(copy.mViewportWidth, copy.mViewportHeight);
                this.mOpticalInsets = copy.mOpticalInsets;
                this.mRootName = copy.mRootName;
                this.mDensity = copy.mDensity;
                if (copy.mRootName != null) {
                    this.mVGTargetsMap.put(copy.mRootName, this);
                }
                onTreeConstructionFinished();
            }
        }

        private void createNativeTree(VGroup rootGroup) {
            this.mNativeTree = new VirtualRefBasePtr(VectorDrawable.nCreateTree(rootGroup.mNativePtr));
            VMRuntime.getRuntime().registerNativeAllocation(NATIVE_ALLOCATION_SIZE);
        }

        private void createNativeTreeFromCopy(VectorDrawableState copy, VGroup rootGroup) {
            this.mNativeTree = new VirtualRefBasePtr(VectorDrawable.nCreateTreeFromCopy(copy.mNativeTree.get(), rootGroup.mNativePtr));
            VMRuntime.getRuntime().registerNativeAllocation(NATIVE_ALLOCATION_SIZE);
        }

        void onTreeConstructionFinished() {
            this.mRootGroup.setTree(this.mNativeTree);
            this.mAllocationOfAllNodes = this.mRootGroup.getNativeSize();
            VMRuntime.getRuntime().registerNativeAllocation(this.mAllocationOfAllNodes);
        }

        long getNativeRenderer() {
            if (this.mNativeTree == null) {
                return 0;
            }
            return this.mNativeTree.get();
        }

        public boolean canReuseCache() {
            if (!this.mCacheDirty && this.mCachedThemeAttrs == this.mThemeAttrs && this.mCachedTint == this.mTint && this.mCachedTintMode == this.mTintMode && this.mCachedAutoMirrored == this.mAutoMirrored) {
                return true;
            }
            updateCacheStates();
            return false;
        }

        public void updateCacheStates() {
            this.mCachedThemeAttrs = this.mThemeAttrs;
            this.mCachedTint = this.mTint;
            this.mCachedTintMode = this.mTintMode;
            this.mCachedAutoMirrored = this.mAutoMirrored;
            this.mCacheDirty = false;
        }

        public void applyTheme(Theme t) {
            this.mRootGroup.applyTheme(t);
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs != null || ((this.mRootGroup != null && this.mRootGroup.canApplyTheme()) || (this.mTint != null && this.mTint.canApplyTheme()))) {
                return true;
            }
            return super.canApplyTheme();
        }

        public VectorDrawableState() {
            this.mTint = null;
            this.mTintMode = VectorDrawable.DEFAULT_TINT_MODE;
            this.mBaseWidth = 0.0f;
            this.mBaseHeight = 0.0f;
            this.mViewportWidth = 0.0f;
            this.mViewportHeight = 0.0f;
            this.mOpticalInsets = Insets.NONE;
            this.mRootName = null;
            this.mNativeTree = null;
            this.mDensity = Const.CODE_G3_RANGE_START;
            this.mVGTargetsMap = new ArrayMap();
            this.mLastSWCachePixelCount = 0;
            this.mLastHWCachePixelCount = 0;
            this.mAllocationOfAllNodes = 0;
            this.mRootGroup = new VGroup();
            createNativeTree(this.mRootGroup);
        }

        public Drawable newDrawable() {
            return new VectorDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new VectorDrawable(this, res, null);
        }

        public int getChangingConfigurations() {
            return (this.mTint != null ? this.mTint.getChangingConfigurations() : 0) | this.mChangingConfigurations;
        }

        public boolean isStateful() {
            if (this.mTint == null || !this.mTint.isStateful()) {
                return this.mRootGroup != null ? this.mRootGroup.isStateful() : false;
            } else {
                return true;
            }
        }

        void setViewportSize(float viewportWidth, float viewportHeight) {
            this.mViewportWidth = viewportWidth;
            this.mViewportHeight = viewportHeight;
            VectorDrawable.nSetRendererViewportSize(getNativeRenderer(), viewportWidth, viewportHeight);
        }

        public final boolean setDensity(int targetDensity) {
            if (this.mDensity == targetDensity) {
                return false;
            }
            int sourceDensity = this.mDensity;
            this.mDensity = targetDensity;
            applyDensityScaling(sourceDensity, targetDensity);
            return true;
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            this.mBaseWidth = Drawable.scaleFromDensity(this.mBaseWidth, sourceDensity, targetDensity);
            this.mBaseHeight = Drawable.scaleFromDensity(this.mBaseHeight, sourceDensity, targetDensity);
            this.mOpticalInsets = Insets.of(Drawable.scaleFromDensity(this.mOpticalInsets.left, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(this.mOpticalInsets.top, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(this.mOpticalInsets.right, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(this.mOpticalInsets.bottom, sourceDensity, targetDensity, false));
        }

        public boolean onStateChange(int[] stateSet) {
            return this.mRootGroup.onStateChange(stateSet);
        }

        public void finalize() throws Throwable {
            super.finalize();
            VMRuntime.getRuntime().registerNativeFree((this.mAllocationOfAllNodes + NATIVE_ALLOCATION_SIZE) + ((this.mLastHWCachePixelCount * 4) + (this.mLastSWCachePixelCount * 4)));
        }

        public boolean setAlpha(float alpha) {
            return VectorDrawable.nSetRootAlpha(this.mNativeTree.get(), alpha);
        }

        public float getAlpha() {
            return VectorDrawable.nGetRootAlpha(this.mNativeTree.get());
        }

        public int getAllocationOfAllNodes() {
            return this.mAllocationOfAllNodes;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.VectorDrawable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.VectorDrawable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.VectorDrawable.<clinit>():void");
    }

    /* synthetic */ VectorDrawable(VectorDrawableState state, Resources res, VectorDrawable vectorDrawable) {
        this(state, res);
    }

    private static native void nAddChild(long j, long j2);

    private static native long nCreateClipPath();

    private static native long nCreateClipPath(long j);

    private static native long nCreateFullPath();

    private static native long nCreateFullPath(long j);

    private static native long nCreateGroup();

    private static native long nCreateGroup(long j);

    private static native long nCreateTree(long j);

    private static native long nCreateTreeFromCopy(long j, long j2);

    private static native int nDraw(long j, long j2, long j3, Rect rect, boolean z, boolean z2);

    private static native float nGetFillAlpha(long j);

    private static native int nGetFillColor(long j);

    private static native boolean nGetFullPathProperties(long j, byte[] bArr, int i);

    private static native boolean nGetGroupProperties(long j, float[] fArr, int i);

    private static native float nGetPivotX(long j);

    private static native float nGetPivotY(long j);

    private static native float nGetRootAlpha(long j);

    private static native float nGetRotation(long j);

    private static native float nGetScaleX(long j);

    private static native float nGetScaleY(long j);

    private static native float nGetStrokeAlpha(long j);

    private static native int nGetStrokeColor(long j);

    private static native float nGetStrokeWidth(long j);

    private static native float nGetTranslateX(long j);

    private static native float nGetTranslateY(long j);

    private static native float nGetTrimPathEnd(long j);

    private static native float nGetTrimPathOffset(long j);

    private static native float nGetTrimPathStart(long j);

    private static native void nSetAllowCaching(long j, boolean z);

    private static native void nSetFillAlpha(long j, float f);

    private static native void nSetFillColor(long j, int i);

    private static native void nSetName(long j, String str);

    private static native void nSetPathData(long j, long j2);

    private static native void nSetPathString(long j, String str, int i);

    private static native void nSetPivotX(long j, float f);

    private static native void nSetPivotY(long j, float f);

    private static native void nSetRendererViewportSize(long j, float f, float f2);

    private static native boolean nSetRootAlpha(long j, float f);

    private static native void nSetRotation(long j, float f);

    private static native void nSetScaleX(long j, float f);

    private static native void nSetScaleY(long j, float f);

    private static native void nSetStrokeAlpha(long j, float f);

    private static native void nSetStrokeColor(long j, int i);

    private static native void nSetStrokeWidth(long j, float f);

    private static native void nSetTranslateX(long j, float f);

    private static native void nSetTranslateY(long j, float f);

    private static native void nSetTrimPathEnd(long j, float f);

    private static native void nSetTrimPathOffset(long j, float f);

    private static native void nSetTrimPathStart(long j, float f);

    private static native void nUpdateFullPathFillGradient(long j, long j2);

    private static native void nUpdateFullPathProperties(long j, float f, int i, float f2, int i2, float f3, float f4, float f5, float f6, float f7, int i3, int i4, int i5);

    private static native void nUpdateFullPathStrokeGradient(long j, long j2);

    private static native void nUpdateGroupProperties(long j, float f, float f2, float f3, float f4, float f5, float f6, float f7);

    public VectorDrawable() {
        this(new VectorDrawableState(), null);
    }

    private VectorDrawable(VectorDrawableState state, Resources res) {
        this.mDpiScaledWidth = 0;
        this.mDpiScaledHeight = 0;
        this.mDpiScaledInsets = Insets.NONE;
        this.mDpiScaledDirty = true;
        this.mTmpBounds = new Rect();
        this.mVectorState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        int density = Drawable.resolveDensity(res, this.mVectorState.mDensity);
        if (this.mTargetDensity != density) {
            this.mTargetDensity = density;
            this.mDpiScaledDirty = true;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mVectorState.mTint, this.mVectorState.mTintMode);
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mVectorState = new VectorDrawableState(this.mVectorState);
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    Object getTargetByName(String name) {
        return this.mVectorState.mVGTargetsMap.get(name);
    }

    public ConstantState getConstantState() {
        this.mVectorState.mChangingConfigurations = getChangingConfigurations();
        return this.mVectorState;
    }

    public void draw(Canvas canvas) {
        copyBounds(this.mTmpBounds);
        if (this.mTmpBounds.width() > 0 && this.mTmpBounds.height() > 0) {
            long colorFilterNativeInstance;
            ColorFilter colorFilter = this.mColorFilter == null ? this.mTintFilter : this.mColorFilter;
            if (colorFilter == null) {
                colorFilterNativeInstance = 0;
            } else {
                colorFilterNativeInstance = colorFilter.native_instance;
            }
            int pixelCount = nDraw(this.mVectorState.getNativeRenderer(), canvas.getNativeCanvasWrapper(), colorFilterNativeInstance, this.mTmpBounds, needMirroring(), this.mVectorState.canReuseCache());
            if (pixelCount != 0) {
                int deltaInBytes;
                if (canvas.isHardwareAccelerated()) {
                    deltaInBytes = (pixelCount - this.mVectorState.mLastHWCachePixelCount) * 4;
                    this.mVectorState.mLastHWCachePixelCount = pixelCount;
                } else {
                    deltaInBytes = (pixelCount - this.mVectorState.mLastSWCachePixelCount) * 4;
                    this.mVectorState.mLastSWCachePixelCount = pixelCount;
                }
                if (deltaInBytes > 0) {
                    VMRuntime.getRuntime().registerNativeAllocation(deltaInBytes);
                } else if (deltaInBytes < 0) {
                    VMRuntime.getRuntime().registerNativeFree(-deltaInBytes);
                }
            }
        }
    }

    public int getAlpha() {
        return (int) (this.mVectorState.getAlpha() * 255.0f);
    }

    public void setAlpha(int alpha) {
        if (this.mVectorState.setAlpha(((float) alpha) / 255.0f)) {
            invalidateSelf();
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mColorFilter = colorFilter;
        invalidateSelf();
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public void setTintList(ColorStateList tint) {
        VectorDrawableState state = this.mVectorState;
        if (state.mTint != tint) {
            state.mTint = tint;
            this.mTintFilter = updateTintFilter(this.mTintFilter, tint, state.mTintMode);
            invalidateSelf();
        }
    }

    public void setTintMode(Mode tintMode) {
        VectorDrawableState state = this.mVectorState;
        if (state.mTintMode != tintMode) {
            state.mTintMode = tintMode;
            this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, tintMode);
            invalidateSelf();
        }
    }

    public boolean isStateful() {
        if (super.isStateful()) {
            return true;
        }
        return this.mVectorState != null ? this.mVectorState.isStateful() : false;
    }

    protected boolean onStateChange(int[] stateSet) {
        boolean changed = false;
        if (isStateful()) {
            mutate();
        }
        VectorDrawableState state = this.mVectorState;
        if (state.onStateChange(stateSet)) {
            changed = true;
            state.mCacheDirty = true;
        }
        if (state.mTint == null || state.mTintMode == null) {
            return changed;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        return true;
    }

    public int getOpacity() {
        return getAlpha() == 0 ? -2 : -3;
    }

    public int getIntrinsicWidth() {
        if (this.mDpiScaledDirty) {
            computeVectorSize();
        }
        return this.mDpiScaledWidth;
    }

    public int getIntrinsicHeight() {
        if (this.mDpiScaledDirty) {
            computeVectorSize();
        }
        return this.mDpiScaledHeight;
    }

    public Insets getOpticalInsets() {
        if (this.mDpiScaledDirty) {
            computeVectorSize();
        }
        return this.mDpiScaledInsets;
    }

    void computeVectorSize() {
        Insets opticalInsets = this.mVectorState.mOpticalInsets;
        int sourceDensity = this.mVectorState.mDensity;
        int targetDensity = this.mTargetDensity;
        if (targetDensity != sourceDensity) {
            this.mDpiScaledWidth = Drawable.scaleFromDensity((int) this.mVectorState.mBaseWidth, sourceDensity, targetDensity, true);
            this.mDpiScaledHeight = Drawable.scaleFromDensity((int) this.mVectorState.mBaseHeight, sourceDensity, targetDensity, true);
            this.mDpiScaledInsets = Insets.of(Drawable.scaleFromDensity(opticalInsets.left, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(opticalInsets.top, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(opticalInsets.right, sourceDensity, targetDensity, false), Drawable.scaleFromDensity(opticalInsets.bottom, sourceDensity, targetDensity, false));
        } else {
            this.mDpiScaledWidth = (int) this.mVectorState.mBaseWidth;
            this.mDpiScaledHeight = (int) this.mVectorState.mBaseHeight;
            this.mDpiScaledInsets = opticalInsets;
        }
        this.mDpiScaledDirty = false;
    }

    public boolean canApplyTheme() {
        return (this.mVectorState == null || !this.mVectorState.canApplyTheme()) ? super.canApplyTheme() : true;
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        VectorDrawableState state = this.mVectorState;
        if (state != null) {
            this.mDpiScaledDirty |= this.mVectorState.setDensity(Drawable.resolveDensity(t.getResources(), 0));
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.VectorDrawable);
                try {
                    state.mCacheDirty = true;
                    updateStateFromTypedArray(a);
                    a.recycle();
                    this.mDpiScaledDirty = true;
                } catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (Throwable th) {
                    a.recycle();
                }
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            if (this.mVectorState != null && this.mVectorState.canApplyTheme()) {
                this.mVectorState.applyTheme(t);
            }
            updateLocalState(t.getResources());
        }
    }

    public float getPixelSize() {
        if (this.mVectorState == null || this.mVectorState.mBaseWidth == 0.0f || this.mVectorState.mBaseHeight == 0.0f || this.mVectorState.mViewportHeight == 0.0f || this.mVectorState.mViewportWidth == 0.0f) {
            return Engine.DEFAULT_VOLUME;
        }
        float intrinsicWidth = this.mVectorState.mBaseWidth;
        float intrinsicHeight = this.mVectorState.mBaseHeight;
        return Math.min(this.mVectorState.mViewportWidth / intrinsicWidth, this.mVectorState.mViewportHeight / intrinsicHeight);
    }

    public static VectorDrawable create(Resources resources, int rid) {
        try {
            int type;
            XmlPullParser parser = resources.getXml(rid);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                throw new XmlPullParserException("No start tag found");
            }
            VectorDrawable drawable = new VectorDrawable();
            drawable.inflate(resources, parser, attrs);
            return drawable;
        } catch (XmlPullParserException e) {
            Log.e(LOGTAG, "parser error", e);
            return null;
        } catch (IOException e2) {
            Log.e(LOGTAG, "parser error", e2);
            return null;
        }
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        if (!(this.mVectorState.mRootGroup == null && this.mVectorState.mNativeTree == null)) {
            if (this.mVectorState.mRootGroup != null) {
                VMRuntime.getRuntime().registerNativeFree(this.mVectorState.getAllocationOfAllNodes());
                this.mVectorState.mRootGroup.setTree(null);
            }
            this.mVectorState.mRootGroup = new VGroup();
            if (this.mVectorState.mNativeTree != null) {
                VMRuntime.getRuntime().registerNativeFree(316);
                this.mVectorState.mNativeTree.release();
            }
            this.mVectorState.createNativeTree(this.mVectorState.mRootGroup);
        }
        VectorDrawableState state = this.mVectorState;
        state.setDensity(Drawable.resolveDensity(r, 0));
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.VectorDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        this.mDpiScaledDirty = true;
        state.mCacheDirty = true;
        inflateChildElements(r, parser, attrs, theme);
        state.onTreeConstructionFinished();
        updateLocalState(r);
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        VectorDrawableState state = this.mVectorState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        int tintMode = a.getInt(6, -1);
        if (tintMode != -1) {
            state.mTintMode = Drawable.parseTintMode(tintMode, Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(1);
        if (tint != null) {
            state.mTint = tint;
        }
        state.mAutoMirrored = a.getBoolean(5, state.mAutoMirrored);
        state.setViewportSize(a.getFloat(7, state.mViewportWidth), a.getFloat(8, state.mViewportHeight));
        if (state.mViewportWidth <= 0.0f) {
            throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires viewportWidth > 0");
        } else if (state.mViewportHeight <= 0.0f) {
            throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires viewportHeight > 0");
        } else {
            state.mBaseWidth = a.getDimension(3, state.mBaseWidth);
            state.mBaseHeight = a.getDimension(2, state.mBaseHeight);
            if (state.mBaseWidth <= 0.0f) {
                throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires width > 0");
            } else if (state.mBaseHeight <= 0.0f) {
                throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires height > 0");
            } else {
                state.mOpticalInsets = Insets.of(a.getDimensionPixelOffset(9, state.mOpticalInsets.left), a.getDimensionPixelOffset(10, state.mOpticalInsets.top), a.getDimensionPixelOffset(11, state.mOpticalInsets.right), a.getDimensionPixelOffset(12, state.mOpticalInsets.bottom));
                state.setAlpha(a.getFloat(4, state.getAlpha()));
                String name = a.getString(0);
                if (name != null) {
                    state.mRootName = name;
                    state.mVGTargetsMap.put(name, state);
                }
            }
        }
    }

    private void inflateChildElements(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        VectorDrawableState state = this.mVectorState;
        boolean noPathTag = true;
        Stack<VGroup> groupStack = new Stack();
        groupStack.push(state.mRootGroup);
        int eventType = parser.getEventType();
        while (eventType != 1) {
            if (eventType == 2) {
                String tagName = parser.getName();
                VGroup currentGroup = (VGroup) groupStack.peek();
                if (SHAPE_PATH.equals(tagName)) {
                    VFullPath path = new VFullPath();
                    path.inflate(res, attrs, theme);
                    currentGroup.addChild(path);
                    if (path.getPathName() != null) {
                        state.mVGTargetsMap.put(path.getPathName(), path);
                    }
                    noPathTag = false;
                    state.mChangingConfigurations |= path.mChangingConfigurations;
                } else if (SHAPE_CLIP_PATH.equals(tagName)) {
                    VClipPath path2 = new VClipPath();
                    path2.inflate(res, attrs, theme);
                    currentGroup.addChild(path2);
                    if (path2.getPathName() != null) {
                        state.mVGTargetsMap.put(path2.getPathName(), path2);
                    }
                    state.mChangingConfigurations |= path2.mChangingConfigurations;
                } else if (SHAPE_GROUP.equals(tagName)) {
                    VGroup newChildGroup = new VGroup();
                    newChildGroup.inflate(res, attrs, theme);
                    currentGroup.addChild(newChildGroup);
                    groupStack.push(newChildGroup);
                    if (newChildGroup.getGroupName() != null) {
                        state.mVGTargetsMap.put(newChildGroup.getGroupName(), newChildGroup);
                    }
                    state.mChangingConfigurations |= newChildGroup.mChangingConfigurations;
                }
            } else if (eventType == 3) {
                if (SHAPE_GROUP.equals(parser.getName())) {
                    groupStack.pop();
                }
            }
            eventType = parser.next();
        }
        if (noPathTag) {
            StringBuffer tag = new StringBuffer();
            if (tag.length() > 0) {
                tag.append(" or ");
            }
            tag.append(SHAPE_PATH);
            throw new XmlPullParserException("no " + tag + " defined");
        }
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mVectorState.getChangingConfigurations();
    }

    void setAllowCaching(boolean allowCaching) {
        nSetAllowCaching(this.mVectorState.getNativeRenderer(), allowCaching);
    }

    private boolean needMirroring() {
        return isAutoMirrored() && getLayoutDirection() == 1;
    }

    public void setAutoMirrored(boolean mirrored) {
        if (this.mVectorState.mAutoMirrored != mirrored) {
            this.mVectorState.mAutoMirrored = mirrored;
            invalidateSelf();
        }
    }

    public boolean isAutoMirrored() {
        return this.mVectorState.mAutoMirrored;
    }
}
