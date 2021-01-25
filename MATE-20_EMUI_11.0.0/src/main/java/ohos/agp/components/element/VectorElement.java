package ohos.agp.components.element;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.AttrHelper;
import ohos.agp.render.LinearShader;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.render.RadialShader;
import ohos.agp.render.Shader;
import ohos.agp.render.SweepShader;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class VectorElement extends Element {
    private static final String CLIP_PATH = "clip-path";
    private static final String GROUP = "group";
    private static final String HEIGHT = "height";
    private static final int MAX_DEPTH = 200;
    private static final int MAX_ELEMENT_COUNT = 5000;
    private static final String PATH = "path";
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "VectorDrawable");
    private static final String VECTOR = "vector";
    private static final String VIEWPORT_HEIGHT = "viewportHeight";
    private static final String VIEWPORT_WIDTH = "viewportWidth";
    private static final String WIDTH = "width";
    private Context mContext;
    private int mParseDepth = 0;
    private int mParseElementCount = 0;
    private ResourceManager mResourceManager;
    private VGroup mRootGroup;

    /* access modifiers changed from: private */
    public enum GradientType {
        LINEAR,
        RADIAL,
        SWEEP
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeAddChild(long j, long j2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native long nativeCreateClipPath();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native long nativeCreateFullPath();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native long nativeCreateGroup();

    private native long nativeCreateVectorElement(long j);

    private native void nativeSetAntiAlias(long j, boolean z);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeSetPathString(long j, String str, int i);

    private native void nativeSetViewportSize(long j, int i, int i2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUpdateFullPathFillGradient(long j, long j2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUpdateFullPathProperties(long j, float f, int i, float f2, int i2, float f3, float f4, float f5, float f6, float f7, int i3, int i4, int i5);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUpdateFullPathStrokeGradient(long j, long j2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeUpdateGroupProperties(long j, float f, float f2, float f3, float f4, float f5, float f6, float f7);

    public VectorElement() {
    }

    public VectorElement(Context context, int i) {
        if (context != null) {
            this.mResourceManager = context.getResourceManager();
            this.mContext = context;
            parseXMLNode(context, ElementScatter.getInstance(context).getRootNodeFromXmlId(i));
        }
    }

    public void setAntiAlias(boolean z) {
        nativeSetAntiAlias(this.mNativeElementPtr, z);
    }

    @Override // ohos.agp.components.element.Element
    public void createNativePtr() {
        if (this.mNativeElementPtr == 0) {
            this.mRootGroup = new VGroup();
            this.mNativeElementPtr = nativeCreateVectorElement(this.mRootGroup.getNativePtr());
        }
    }

    /* access modifiers changed from: private */
    public class VGroup extends VObject {
        private static final String PIVOT_X = "pivotX";
        private static final String PIVOT_Y = "pivotY";
        private static final String ROTATION = "rotation";
        private static final String SCALE_X = "scaleX";
        private static final String SCALE_Y = "scaleY";
        private static final String TRANSLATE_X = "translateX";
        private static final String TRANSLATE_Y = "translateY";

        VGroup() {
            super(VectorElement.this, null);
            this.mNativePtr = VectorElement.this.nativeCreateGroup();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.agp.components.element.VectorElement.VObject
        public long getNativePtr() {
            return this.mNativePtr;
        }

        /* access modifiers changed from: package-private */
        public void addChild(VObject vObject) {
            VectorElement.this.nativeAddChild(this.mNativePtr, vObject.getNativePtr());
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: package-private */
        public void parseProperties(List<TypedAttribute> list) {
            char c;
            HiLog.debug(VectorElement.TAG, "parseProperty:attr size: %{public}d", new Object[]{Integer.valueOf(list.size())});
            float f = 1.0f;
            float f2 = 1.0f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            float f6 = 0.0f;
            float f7 = 0.0f;
            for (TypedAttribute typedAttribute : list) {
                String name = typedAttribute.getName();
                if (name == null) {
                    HiLog.error(VectorElement.TAG, " attribute name is null", new Object[0]);
                } else {
                    try {
                        float convertValueToFloat = AttrHelper.convertValueToFloat(typedAttribute.getStringValue(), 0.0f);
                        switch (name.hashCode()) {
                            case -1721943862:
                                if (name.equals(TRANSLATE_X)) {
                                    c = 5;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -1721943861:
                                if (name.equals(TRANSLATE_Y)) {
                                    c = 6;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -987906986:
                                if (name.equals(PIVOT_X)) {
                                    c = 1;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -987906985:
                                if (name.equals(PIVOT_Y)) {
                                    c = 2;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -908189618:
                                if (name.equals(SCALE_X)) {
                                    c = 3;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -908189617:
                                if (name.equals(SCALE_Y)) {
                                    c = 4;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -40300674:
                                if (name.equals("rotation")) {
                                    c = 0;
                                    break;
                                }
                                c = 65535;
                                break;
                            default:
                                c = 65535;
                                break;
                        }
                        switch (c) {
                            case 0:
                                f3 = convertValueToFloat;
                                continue;
                            case 1:
                                f4 = convertValueToFloat;
                                continue;
                            case 2:
                                f5 = convertValueToFloat;
                                continue;
                            case 3:
                                f = convertValueToFloat;
                                continue;
                            case 4:
                                f2 = convertValueToFloat;
                                continue;
                            case 5:
                                f6 = convertValueToFloat;
                                continue;
                            case 6:
                                f7 = convertValueToFloat;
                                continue;
                        }
                    } catch (IOException | NotExistException | WrongTypeException unused) {
                        HiLog.error(VectorElement.TAG, "parse property %{public}s typedAttribute failed", new Object[]{name});
                    }
                }
            }
            VectorElement.this.nativeUpdateGroupProperties(this.mNativePtr, f3, f4, f5, f, f2, f6, f7);
        }
    }

    private abstract class VPath extends VObject {
        static final String PATH_DATA = "pathData";

        private VPath() {
            super(VectorElement.this, null);
        }

        /* synthetic */ VPath(VectorElement vectorElement, AnonymousClass1 r2) {
            this();
        }
    }

    /* access modifiers changed from: private */
    public class VClipPath extends VPath {
        VClipPath() {
            super(VectorElement.this, null);
            this.mNativePtr = VectorElement.this.nativeCreateClipPath();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.agp.components.element.VectorElement.VObject
        public long getNativePtr() {
            return this.mNativePtr;
        }

        /* access modifiers changed from: package-private */
        public void parseProperties(List<TypedAttribute> list) {
            String str;
            HiLog.debug(VectorElement.TAG, "parseProperty:attr size: %{public}d", new Object[]{Integer.valueOf(list.size())});
            Iterator<TypedAttribute> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    str = null;
                    break;
                }
                TypedAttribute next = it.next();
                if ("pathData".equals(next.getName())) {
                    try {
                        str = next.getStringValue();
                        HiLog.debug(VectorElement.TAG, "parse clip-pathData: %{public}s", new Object[]{str});
                        break;
                    } catch (IOException | NotExistException | WrongTypeException unused) {
                        HiLog.error(VectorElement.TAG, "parse property pathData typedAttribute failed", new Object[0]);
                    }
                }
            }
            if (str != null) {
                VectorElement.this.nativeSetPathString(this.mNativePtr, str, str.length());
            }
        }
    }

    /* access modifiers changed from: private */
    public class VFullPath extends VPath {
        private static final String ATTR = "attr";
        private static final String BUTT_CAP = "butt";
        private static final String CENTER_X = "centerX";
        private static final String CENTER_Y = "centerY";
        private static final String CLAMP = "clamp";
        private static final String COLOR = "color";
        private static final String END_X = "endX";
        private static final String END_Y = "endY";
        private static final String EVEN_ODD = "evenOdd";
        private static final String FILL_ALPHA = "fillAlpha";
        private static final String FILL_COLOR = "fillColor";
        private static final String FILL_TYPE = "filltype";
        private static final String GRADIENT = "gradient";
        private static final String GRADIENT_RADIUS = "gradientRadius";
        private static final String ITEM = "item";
        private static final String LINEAR_GRADIENT = "linear";
        private static final String MIRROR = "mirror";
        private static final String MITER_JOIN = "miter";
        private static final String NAME = "name";
        private static final String OFFSET = "offset";
        private static final String RADIAL_GRADIENT = "radial";
        private static final String REPEAT = "repeat";
        private static final String ROUND_CAP = "round";
        private static final String ROUND_JOIN = "round";
        private static final String START_X = "startX";
        private static final String START_Y = "startY";
        private static final String STROKE_ALPHA = "strokeAlpha";
        private static final String STROKE_COLOR = "strokeColor";
        private static final String STROKE_LINE_CAP = "strokeLineCap";
        private static final String STROKE_LINE_JOIN = "strokeLineJoin";
        private static final String STROKE_MITER_LIMIT = "strokeMiterLimit";
        private static final String STROKE_WIDTH = "strokeWidth";
        private static final String SWEEP_GRADIENT = "sweep";
        private static final String TILE_MODE = "tileMode";
        private static final String TRIM_PATH_END = "trimPathEnd";
        private static final String TRIM_PATH_OFFSET = "trimPathOffset";
        private static final String TRIM_PATH_START = "trimPathStart";
        private static final String TYPE = "type";
        private Shader mFillGradient = null;
        private GradientProperty mGradientProperty = new GradientProperty();
        private FullPathProperty mProperty = new FullPathProperty();
        private Shader mStrokeGradient = null;

        VFullPath() {
            super(VectorElement.this, null);
            this.mNativePtr = VectorElement.this.nativeCreateFullPath();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.agp.components.element.VectorElement.VObject
        public long getNativePtr() {
            return this.mNativePtr;
        }

        /* access modifiers changed from: package-private */
        public void parseProperties(Node node) {
            List<TypedAttribute> typedAttribute = node.getTypedAttribute(VectorElement.this.mResourceManager);
            HiLog.debug(VectorElement.TAG, "parseProperty:attr size: %{public}d", new Object[]{Integer.valueOf(typedAttribute.size())});
            for (TypedAttribute typedAttribute2 : typedAttribute) {
                VectorElement.this.mParseElementCount++;
                parseProperty(typedAttribute2);
            }
            VectorElement.this.nativeUpdateFullPathProperties(this.mNativePtr, this.mProperty.mStrokeWidth, this.mProperty.mStrokeColor, this.mProperty.mStrokeAlpha, this.mProperty.mFillColor, this.mProperty.mFillAlpha, this.mProperty.mTrimPathStart, this.mProperty.mTrimPathEnd, this.mProperty.mTrimPathOffset, this.mProperty.mStrokeMiterLimit, this.mProperty.mStrokeLineCap, this.mProperty.mStrokeLineJoin, this.mProperty.mFillType);
            String str = this.mProperty.mPathDataString;
            if (str != null) {
                VectorElement.this.nativeSetPathString(this.mNativePtr, str, str.length());
            }
            Node child = node.getChild();
            if (child != null) {
                HiLog.debug(VectorElement.TAG, "parse gradient property: %{public}s", new Object[]{child.getName()});
                VectorElement.this.mParseDepth++;
                parseFullPathAttribute(child);
            }
        }

        /* access modifiers changed from: private */
        public class FullPathProperty {
            private float mFillAlpha;
            private int mFillColor;
            private int mFillType;
            private String mPathDataString;
            private float mStrokeAlpha;
            private int mStrokeColor;
            private int mStrokeLineCap;
            private int mStrokeLineJoin;
            private float mStrokeMiterLimit;
            private float mStrokeWidth;
            private float mTrimPathEnd;
            private float mTrimPathOffset;
            private float mTrimPathStart;

            FullPathProperty() {
                reset();
            }

            private void reset() {
                this.mPathDataString = null;
                this.mStrokeWidth = 0.0f;
                this.mStrokeColor = 0;
                this.mStrokeAlpha = 1.0f;
                this.mFillColor = 0;
                this.mFillAlpha = 1.0f;
                this.mTrimPathStart = 0.0f;
                this.mTrimPathEnd = 1.0f;
                this.mTrimPathOffset = 0.0f;
                this.mStrokeLineCap = 0;
                this.mStrokeLineJoin = 0;
                this.mStrokeMiterLimit = 0.0f;
                this.mFillType = 0;
            }
        }

        /* access modifiers changed from: private */
        public class GradientProperty {
            private float mCenterX;
            private float mCenterY;
            private float mEndX;
            private float mEndY;
            private float mGradientRadius;
            private GradientType mGradientType;
            private Vector<Integer> mShaderColors = new Vector<>();
            private Vector<Float> mShaderStops = new Vector<>();
            private float mStartX;
            private float mStartY;
            private Shader.TileMode mTileMode;

            GradientProperty() {
                reset();
            }

            /* access modifiers changed from: package-private */
            public void reset() {
                this.mGradientType = GradientType.LINEAR;
                this.mTileMode = Shader.TileMode.CLAMP;
                this.mStartX = 0.0f;
                this.mStartY = 0.0f;
                this.mEndX = 0.0f;
                this.mEndY = 0.0f;
                this.mCenterX = 0.0f;
                this.mCenterY = 0.0f;
                this.mGradientRadius = 0.0f;
                this.mShaderColors.clear();
                this.mShaderStops.clear();
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private void parseProperty(TypedAttribute typedAttribute) {
            char c;
            String name = typedAttribute.getName();
            try {
                String stringValue = typedAttribute.getStringValue();
                if (name != null && stringValue != null) {
                    switch (name.hashCode()) {
                        case -1143814757:
                            if (name.equals(FILL_ALPHA)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1141881952:
                            if (name.equals(FILL_COLOR)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1121758502:
                            if (name.equals(TRIM_PATH_OFFSET)) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case -728102083:
                            if (name.equals(FILL_TYPE)) {
                                c = 11;
                                break;
                            }
                            c = 65535;
                            break;
                        case -170626757:
                            if (name.equals(TRIM_PATH_START)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 49292814:
                            if (name.equals(STROKE_MITER_LIMIT)) {
                                c = '\n';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1027544550:
                            if (name.equals(STROKE_LINE_CAP)) {
                                c = '\b';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1233923439:
                            if (name.equals("pathData")) {
                                c = '\f';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1789331862:
                            if (name.equals(STROKE_LINE_JOIN)) {
                                c = '\t';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1903848966:
                            if (name.equals(STROKE_ALPHA)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1905781771:
                            if (name.equals(STROKE_COLOR)) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1924065902:
                            if (name.equals(STROKE_WIDTH)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2136119284:
                            if (name.equals(TRIM_PATH_END)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            this.mProperty.mStrokeWidth = AttrHelper.convertValueToFloat(stringValue, 0.0f);
                            return;
                        case 1:
                            this.mProperty.mStrokeColor = AttrHelper.convertValueToColor(stringValue).getValue();
                            return;
                        case 2:
                            this.mProperty.mStrokeAlpha = AttrHelper.convertValueToFloat(stringValue, 1.0f);
                            return;
                        case 3:
                            this.mProperty.mFillColor = AttrHelper.convertValueToColor(stringValue).getValue();
                            return;
                        case 4:
                            this.mProperty.mFillAlpha = AttrHelper.convertValueToFloat(stringValue, 1.0f);
                            return;
                        case 5:
                            this.mProperty.mTrimPathStart = AttrHelper.convertValueToFloat(stringValue, 0.0f);
                            return;
                        case 6:
                            this.mProperty.mTrimPathEnd = AttrHelper.convertValueToFloat(stringValue, 1.0f);
                            return;
                        case 7:
                            this.mProperty.mTrimPathOffset = AttrHelper.convertValueToFloat(stringValue, 0.0f);
                            return;
                        case '\b':
                            this.mProperty.mStrokeLineCap = getStrokeCapType(stringValue).value();
                            return;
                        case '\t':
                            this.mProperty.mStrokeLineJoin = getStrokeJoinType(stringValue).value();
                            return;
                        case '\n':
                            this.mProperty.mStrokeMiterLimit = (float) AttrHelper.convertValueToInt(stringValue, 0);
                            return;
                        case 11:
                            this.mProperty.mFillType = getFillType(stringValue).value();
                            return;
                        case '\f':
                            this.mProperty.mPathDataString = stringValue;
                            HiLog.debug(VectorElement.TAG, "parse pathData: %{public}s", new Object[]{this.mProperty.mPathDataString});
                            return;
                        default:
                            return;
                    }
                }
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(VectorElement.TAG, "parse property failed", new Object[0]);
            }
        }

        private Path.FillType getFillType(String str) {
            if (str.equals(EVEN_ODD)) {
                return Path.FillType.EVEN_ODD;
            }
            return Path.FillType.WINDING_ORDER;
        }

        private Paint.StrokeCap getStrokeCapType(String str) {
            if (BUTT_CAP.equals(str)) {
                return Paint.StrokeCap.BUTT;
            }
            if (Keywords.FUNC_ROUND_STRING.equals(str)) {
                return Paint.StrokeCap.ROUND;
            }
            return Paint.StrokeCap.SQUARE;
        }

        private Paint.Join getStrokeJoinType(String str) {
            if (MITER_JOIN.equals(str)) {
                return Paint.Join.MITER;
            }
            if (Keywords.FUNC_ROUND_STRING.equals(str)) {
                return Paint.Join.ROUND;
            }
            return Paint.Join.BEVEL;
        }

        private void parseFullPathAttribute(Node node) {
            VectorElement.this.checkXMLDepthAndElementCount();
            VectorElement.this.mParseElementCount++;
            String name = node.getName();
            if (!(name == null || name.length() == 0 || !name.equals(ATTR))) {
                for (TypedAttribute typedAttribute : node.getTypedAttribute(VectorElement.this.mResourceManager)) {
                    String name2 = typedAttribute.getName();
                    try {
                        String stringValue = typedAttribute.getStringValue();
                        if (!(name2 == null || stringValue == null || !"name".equals(name2))) {
                            this.mGradientProperty.reset();
                            VectorElement.this.mParseDepth++;
                            parseGradient(node.getChild());
                            if (this.mGradientProperty.mShaderColors.size() > 1) {
                                makeShader(stringValue);
                            }
                        }
                    } catch (IOException | NotExistException | WrongTypeException unused) {
                        HiLog.error(VectorElement.TAG, "get path typedAttribute failed", new Object[0]);
                    }
                }
            }
        }

        private void makeShader(String str) {
            Vector vector = this.mGradientProperty.mShaderColors;
            Color[] colorArr = new Color[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                colorArr[i] = new Color(((Integer) vector.get(i)).intValue());
            }
            Vector vector2 = this.mGradientProperty.mShaderStops;
            float[] fArr = new float[vector2.size()];
            for (int i2 = 0; i2 < vector2.size(); i2++) {
                fArr[i2] = ((Float) vector2.get(i2)).floatValue();
            }
            Shader shader = null;
            int i3 = AnonymousClass1.$SwitchMap$ohos$agp$components$element$VectorElement$GradientType[this.mGradientProperty.mGradientType.ordinal()];
            if (i3 == 1) {
                shader = new LinearShader(new Point[]{new Point(this.mGradientProperty.mStartX, this.mGradientProperty.mStartY), new Point(this.mGradientProperty.mEndX, this.mGradientProperty.mEndY)}, fArr, colorArr, this.mGradientProperty.mTileMode);
            } else if (i3 == 2) {
                shader = new SweepShader(this.mGradientProperty.mCenterX, this.mGradientProperty.mCenterY, colorArr, fArr);
            } else if (i3 == 3) {
                shader = new RadialShader(new Point(this.mGradientProperty.mCenterX, this.mGradientProperty.mCenterY), this.mGradientProperty.mGradientRadius, fArr, colorArr, this.mGradientProperty.mTileMode);
            }
            long j = 0;
            if (str.equals(FILL_COLOR)) {
                this.mFillGradient = shader;
                VectorElement vectorElement = VectorElement.this;
                long j2 = this.mNativePtr;
                Shader shader2 = this.mFillGradient;
                if (shader2 != null) {
                    j = shader2.getNativeHandle();
                }
                vectorElement.nativeUpdateFullPathFillGradient(j2, j);
            } else if (str.equals(STROKE_COLOR)) {
                this.mStrokeGradient = shader;
                VectorElement vectorElement2 = VectorElement.this;
                long j3 = this.mNativePtr;
                Shader shader3 = this.mStrokeGradient;
                if (shader3 != null) {
                    j = shader3.getNativeHandle();
                }
                vectorElement2.nativeUpdateFullPathStrokeGradient(j3, j);
            } else {
                HiLog.debug(VectorElement.TAG, "unknown attr, ignore it", new Object[0]);
            }
        }

        private void parseGradient(Node node) {
            VectorElement.this.checkXMLDepthAndElementCount();
            if (node != null) {
                String name = node.getName();
                if (name == null || name.length() == 0 || !name.equals(GRADIENT)) {
                    parseGradient(node.getSibling());
                    return;
                }
                VectorElement.this.mParseElementCount++;
                for (TypedAttribute typedAttribute : node.getTypedAttribute(VectorElement.this.mResourceManager)) {
                    parseGradientProperty(typedAttribute);
                }
                Node child = node.getChild();
                if (child != null) {
                    VectorElement.this.mParseDepth++;
                    parseGradientColors(child);
                }
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
            if (r0.equals("type") != false) goto L_0x0087;
         */
        private void parseGradientProperty(TypedAttribute typedAttribute) {
            String str;
            String name = typedAttribute.getName();
            char c = 0;
            try {
                str = typedAttribute.getStringValue();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(VectorElement.TAG, "get gradient property %{public}s typedAttribute failed", new Object[]{name});
                str = null;
            }
            if (name != null && str != null) {
                switch (name.hashCode()) {
                    case -2106197647:
                        if (name.equals(TILE_MODE)) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case -892483530:
                        if (name.equals(START_X)) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -892483529:
                        if (name.equals(START_Y)) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3117789:
                        if (name.equals(END_X)) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3117790:
                        if (name.equals(END_Y)) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3575610:
                        break;
                    case 132197346:
                        if (name.equals(GRADIENT_RADIUS)) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case 665239203:
                        if (name.equals(CENTER_X)) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case 665239204:
                        if (name.equals(CENTER_Y)) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        if (str.equals(RADIAL_GRADIENT)) {
                            this.mGradientProperty.mGradientType = GradientType.RADIAL;
                            return;
                        } else if (str.equals(SWEEP_GRADIENT)) {
                            this.mGradientProperty.mGradientType = GradientType.SWEEP;
                            return;
                        } else {
                            this.mGradientProperty.mGradientType = GradientType.LINEAR;
                            return;
                        }
                    case 1:
                        if (str.equals(REPEAT)) {
                            this.mGradientProperty.mTileMode = Shader.TileMode.REPEAT;
                            return;
                        } else if (str.equals(MIRROR)) {
                            this.mGradientProperty.mTileMode = Shader.TileMode.MIRROR;
                            return;
                        } else {
                            this.mGradientProperty.mTileMode = Shader.TileMode.CLAMP;
                            return;
                        }
                    case 2:
                        this.mGradientProperty.mStartX = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case 3:
                        this.mGradientProperty.mStartY = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case 4:
                        this.mGradientProperty.mEndX = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case 5:
                        this.mGradientProperty.mEndY = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case 6:
                        this.mGradientProperty.mCenterX = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case 7:
                        this.mGradientProperty.mCenterY = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    case '\b':
                        this.mGradientProperty.mGradientRadius = AttrHelper.convertValueToFloat(str, 0.0f);
                        return;
                    default:
                        return;
                }
            }
        }

        private void parseGradientColors(Node node) {
            VectorElement.this.checkXMLDepthAndElementCount();
            VectorElement.this.mParseElementCount++;
            String name = node.getName();
            if (name != null && name.length() != 0 && name.equals(ITEM)) {
                for (TypedAttribute typedAttribute : node.getTypedAttribute(VectorElement.this.mResourceManager)) {
                    String name2 = typedAttribute.getName();
                    String str = null;
                    try {
                        str = typedAttribute.getStringValue();
                    } catch (IOException | NotExistException | WrongTypeException unused) {
                        HiLog.error(VectorElement.TAG, "get gradient colors %{public}s typedAttribute failed", new Object[]{name2});
                    }
                    if (!(name2 == null || str == null)) {
                        if (name2.equals(COLOR)) {
                            Vector vector = this.mGradientProperty.mShaderColors;
                            vector.add(Integer.valueOf(AttrHelper.convertValueToColor(str).getValue()));
                            this.mGradientProperty.mShaderColors = vector;
                        } else if (name2.equals(OFFSET)) {
                            Vector vector2 = this.mGradientProperty.mShaderStops;
                            vector2.add(Float.valueOf(AttrHelper.convertValueToFloat(str, 0.0f)));
                            this.mGradientProperty.mShaderStops = vector2;
                        } else {
                            HiLog.debug(VectorElement.TAG, "unknown attr in shader, ignore it", new Object[0]);
                        }
                    }
                }
                if (node.getSibling() != null) {
                    parseGradientColors(node.getSibling());
                }
            } else if (node.getSibling() != null) {
                parseGradientColors(node.getSibling());
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.agp.components.element.VectorElement$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$components$element$VectorElement$GradientType = new int[GradientType.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$components$element$VectorElement$GradientType[GradientType.LINEAR.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$components$element$VectorElement$GradientType[GradientType.SWEEP.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$components$element$VectorElement$GradientType[GradientType.RADIAL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /* access modifiers changed from: private */
    public abstract class VObject {
        long mNativePtr;

        /* access modifiers changed from: package-private */
        public abstract long getNativePtr();

        private VObject() {
            this.mNativePtr = 0;
        }

        /* synthetic */ VObject(VectorElement vectorElement, AnonymousClass1 r2) {
            this();
        }
    }

    @Override // ohos.agp.components.element.Element
    public void parseXMLNode(Context context, Node node) {
        super.parseXMLNode(context, node);
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            if (this.mResourceManager == null) {
                throw new ElementScatterException("mResourceManager is null");
            } else if (node != null) {
                String name = node.getName();
                if (name == null || name.length() == 0) {
                    throw new ElementScatterException("Solid XML root node has no name!");
                } else if (name.equals(VECTOR)) {
                    HiLog.debug(TAG, "rootNode: %{public}s", new Object[]{name});
                    parseProperties(node.getTypedAttribute(this.mResourceManager));
                    this.mParseDepth = 1;
                    this.mParseElementCount = 1;
                    Node child = node.getChild();
                    if (child != null) {
                        parseSolidXmlNode(this.mRootGroup, child);
                    }
                } else {
                    throw new ElementScatterException("vector XML is not valid!");
                }
            } else {
                throw new ElementScatterException("Solid XML has no root node!");
            }
        } else {
            throw new ElementScatterException("context is null");
        }
    }

    private void parseProperties(List<TypedAttribute> list) {
        HiLog.debug(TAG, "parseProperty:attr size: %{public}d", new Object[]{Integer.valueOf(list.size())});
        float density = AttrHelper.getDensity(this.mContext);
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (TypedAttribute typedAttribute : list) {
            String name = typedAttribute.getName();
            String str = null;
            try {
                str = typedAttribute.getStringValue();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "parse property %{public}s typedAttribute failed", new Object[]{name});
            }
            if (!(name == null || str == null)) {
                char c = 65535;
                switch (name.hashCode()) {
                    case -1499022144:
                        if (name.equals(VIEWPORT_WIDTH)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -1221029593:
                        if (name.equals("height")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 113126854:
                        if (name.equals("width")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 341959021:
                        if (name.equals(VIEWPORT_HEIGHT)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    i = AttrHelper.convertDimensionToPix(str, density, 0);
                } else if (c == 1) {
                    i2 = AttrHelper.convertDimensionToPix(str, density, 0);
                } else if (c == 2) {
                    i3 = AttrHelper.convertDimensionToPix(str, density, 0);
                } else if (c == 3) {
                    i4 = AttrHelper.convertDimensionToPix(str, density, 0);
                }
            }
        }
        setBounds(new Rect(0, 0, i, i2));
        nativeSetViewportSize(this.mNativeElementPtr, i3, i4);
        HiLog.debug(TAG, "parseProperty:width: %{public}d height: %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
    }

    private void parseSolidXmlNode(VGroup vGroup, Node node) {
        checkXMLDepthAndElementCount();
        String name = node.getName();
        if (name == null || name.length() == 0) {
            HiLog.error(TAG, "null node", new Object[0]);
            return;
        }
        HiLog.debug(TAG, "parseSolidXmlNode: %{public}s: %{public}s", new Object[]{vGroup, name});
        List<TypedAttribute> typedAttribute = node.getTypedAttribute(this.mResourceManager);
        this.mParseElementCount++;
        if (PATH.equals(name)) {
            VFullPath vFullPath = new VFullPath();
            vFullPath.parseProperties(node);
            vGroup.addChild(vFullPath);
        } else if (CLIP_PATH.equals(name)) {
            VClipPath vClipPath = new VClipPath();
            vClipPath.parseProperties(typedAttribute);
            vGroup.addChild(vClipPath);
        } else if (GROUP.equals(name)) {
            VGroup vGroup2 = new VGroup();
            vGroup2.parseProperties(typedAttribute);
            vGroup.addChild(vGroup2);
            Node child = node.getChild();
            if (child != null) {
                HiLog.debug(TAG, "parseSolidXmlNode: child: %{public}s: %{public}s", new Object[]{vGroup2, child.getName()});
                this.mParseDepth++;
                parseSolidXmlNode(vGroup2, child);
            }
        } else {
            HiLog.debug(TAG, "unsupported attribute:%{public}s, ignore it", new Object[]{name});
        }
        HiLog.debug(TAG, "get sibling", new Object[0]);
        Node sibling = node.getSibling();
        if (sibling != null) {
            HiLog.debug(TAG, "parseSolidXmlNode: sibling: %{public}s: %{public}s", new Object[]{vGroup, sibling.getName()});
            parseSolidXmlNode(vGroup, sibling);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkXMLDepthAndElementCount() {
        int i = this.mParseDepth;
        if (i > 200 || this.mParseElementCount > MAX_ELEMENT_COUNT) {
            throw new ElementScatterException(String.format(Locale.ROOT, "Exceeded the depth limit: %d Or count limit: %d", 200, Integer.valueOf((int) MAX_ELEMENT_COUNT)));
        }
        HiLog.debug(TAG, "parseSolidXmlNode current depth: %{public}d", new Object[]{Integer.valueOf(i)});
    }
}
