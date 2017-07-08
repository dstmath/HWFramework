package android.animation;

import android.R;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.ConfigurationBoundResourceCache;
import android.content.res.ConstantState;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.hwtheme.HwThemeManager;
import android.net.ProxyInfo;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.Voice;
import android.util.AttributeSet;
import android.util.Log;
import android.util.PathParser;
import android.util.PathParser.PathData;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.InflateException;
import android.view.animation.AnimationUtils;
import android.view.animation.BaseInterpolator;
import android.view.animation.Interpolator;
import huawei.android.animation.HwStateListAnimator;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimatorInflater {
    private static final boolean DBG_ANIMATOR_INFLATER = false;
    private static final int SEQUENTIALLY = 1;
    private static final String TAG = "AnimatorInflater";
    private static final int TOGETHER = 0;
    private static final int VALUE_TYPE_COLOR = 3;
    private static final int VALUE_TYPE_FLOAT = 0;
    private static final int VALUE_TYPE_INT = 1;
    private static final int VALUE_TYPE_PATH = 2;
    private static final int VALUE_TYPE_UNDEFINED = 4;
    private static final TypedValue sTmpTypedValue = null;

    private static class PathDataEvaluator implements TypeEvaluator<PathData> {
        private final PathData mPathData;

        private PathDataEvaluator() {
            this.mPathData = new PathData();
        }

        public PathData evaluate(float fraction, PathData startPathData, PathData endPathData) {
            if (PathParser.interpolatePathData(this.mPathData, startPathData, endPathData, fraction)) {
                return this.mPathData;
            }
            throw new IllegalArgumentException("Can't interpolate between two incompatible pathData");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.AnimatorInflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.AnimatorInflater.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.AnimatorInflater.<clinit>():void");
    }

    public static Animator loadAnimator(Context context, int id) throws NotFoundException {
        return loadAnimator(context.getResources(), context.getTheme(), id);
    }

    public static Animator loadAnimator(Resources resources, Theme theme, int id) throws NotFoundException {
        return loadAnimator(resources, theme, id, Engine.DEFAULT_VOLUME);
    }

    public static Animator loadAnimator(Resources resources, Theme theme, int id, float pathErrorScale) throws NotFoundException {
        NotFoundException rnf;
        ConfigurationBoundResourceCache<Animator> animatorCache = resources.getAnimatorCache();
        Animator animator = (Animator) animatorCache.getInstance((long) id, resources, theme);
        if (animator != null) {
            return animator;
        }
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = resources.getAnimation(id);
            animator = createAnimatorFromXml(resources, theme, xmlResourceParser, pathErrorScale);
            if (animator != null) {
                animator.appendChangingConfigurations(getChangingConfigs(resources, id));
                ConstantState<Animator> constantState = animator.createConstantState();
                if (constantState != null) {
                    animatorCache.put((long) id, theme, constantState);
                    animator = (Animator) constantState.newInstance(resources, theme);
                }
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return animator;
        } catch (XmlPullParserException ex) {
            rnf = new NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            rnf = new NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex2);
            throw rnf;
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    public static StateListAnimator loadStateListAnimator(Context context, int id) throws NotFoundException {
        NotFoundException rnf;
        Resources resources = context.getResources();
        ConfigurationBoundResourceCache<StateListAnimator> cache = resources.getStateListAnimatorCache();
        Theme theme = context.getTheme();
        StateListAnimator animator = (StateListAnimator) cache.getInstance((long) id, resources, theme);
        if (animator != null) {
            return animator;
        }
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = resources.getAnimation(id);
            animator = createStateListAnimatorFromXml(context, xmlResourceParser, Xml.asAttributeSet(xmlResourceParser));
            if (animator != null) {
                animator.appendChangingConfigurations(getChangingConfigs(resources, id));
                ConstantState<StateListAnimator> constantState = animator.createConstantState();
                if (constantState != null) {
                    cache.put((long) id, theme, constantState);
                    animator = (StateListAnimator) constantState.newInstance(resources, theme);
                }
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return animator;
        } catch (XmlPullParserException ex) {
            rnf = new NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            rnf = new NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex2);
            throw rnf;
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private static StateListAnimator createStateListAnimatorFromXml(Context context, XmlPullParser parser, AttributeSet attributeSet) throws IOException, XmlPullParserException {
        StateListAnimator stateListAnimator = new StateListAnimator();
        while (true) {
            switch (parser.next()) {
                case VALUE_TYPE_INT /*1*/:
                case VALUE_TYPE_COLOR /*3*/:
                    return stateListAnimator;
                case VALUE_TYPE_PATH /*2*/:
                    Animator animator = null;
                    if (HwThemeManager.TAG_ITEM.equals(parser.getName())) {
                        int attributeCount = parser.getAttributeCount();
                        int[] states = new int[attributeCount];
                        int i = VALUE_TYPE_FLOAT;
                        int stateIndex = VALUE_TYPE_FLOAT;
                        while (i < attributeCount) {
                            int stateIndex2;
                            int attrName = attributeSet.getAttributeNameResource(i);
                            if (attrName == R.attr.animation) {
                                animator = loadAnimator(context, attributeSet.getAttributeResourceValue(i, VALUE_TYPE_FLOAT));
                                stateIndex2 = stateIndex;
                            } else {
                                stateIndex2 = stateIndex + VALUE_TYPE_INT;
                                if (!attributeSet.getAttributeBooleanValue(i, DBG_ANIMATOR_INFLATER)) {
                                    attrName = -attrName;
                                }
                                states[stateIndex] = attrName;
                            }
                            i += VALUE_TYPE_INT;
                            stateIndex = stateIndex2;
                        }
                        if (animator == null) {
                            animator = createAnimatorFromXml(context.getResources(), context.getTheme(), parser, Engine.DEFAULT_VOLUME);
                        }
                        if (animator != null) {
                            stateListAnimator.addState(StateSet.trimStateSet(states, stateIndex), animator);
                            break;
                        }
                        throw new NotFoundException("animation state item must have a valid animation");
                    }
                    continue;
                default:
                    break;
            }
        }
    }

    private static PropertyValuesHolder getPVH(TypedArray styledAttributes, int valueType, int valueFromId, int valueToId, String propertyName) {
        TypedValue tvFrom = styledAttributes.peekValue(valueFromId);
        boolean hasFrom = tvFrom != null ? true : DBG_ANIMATOR_INFLATER;
        int fromType = hasFrom ? tvFrom.type : VALUE_TYPE_FLOAT;
        TypedValue tvTo = styledAttributes.peekValue(valueToId);
        boolean hasTo = tvTo != null ? true : DBG_ANIMATOR_INFLATER;
        int toType = hasTo ? tvTo.type : VALUE_TYPE_FLOAT;
        if (valueType == VALUE_TYPE_UNDEFINED) {
            if ((hasFrom && isColorType(fromType)) || (hasTo && isColorType(toType))) {
                valueType = VALUE_TYPE_COLOR;
            } else {
                valueType = VALUE_TYPE_FLOAT;
            }
        }
        boolean getFloats = valueType == 0 ? true : DBG_ANIMATOR_INFLATER;
        PropertyValuesHolder returnValue = null;
        TypeEvaluator evaluator;
        if (valueType == VALUE_TYPE_PATH) {
            String fromString = styledAttributes.getString(valueFromId);
            String toString = styledAttributes.getString(valueToId);
            PathData pathData = fromString == null ? null : new PathData(fromString);
            PathData pathData2 = toString == null ? null : new PathData(toString);
            if (pathData == null && pathData2 == null) {
                return null;
            }
            PathData[] pathDataArr;
            if (pathData != null) {
                evaluator = new PathDataEvaluator();
                if (pathData2 == null) {
                    pathDataArr = new Object[VALUE_TYPE_INT];
                    pathDataArr[VALUE_TYPE_FLOAT] = pathData;
                    return PropertyValuesHolder.ofObject(propertyName, evaluator, (Object[]) pathDataArr);
                } else if (PathParser.canMorph(pathData, pathData2)) {
                    pathDataArr = new Object[VALUE_TYPE_PATH];
                    pathDataArr[VALUE_TYPE_FLOAT] = pathData;
                    pathDataArr[VALUE_TYPE_INT] = pathData2;
                    return PropertyValuesHolder.ofObject(propertyName, evaluator, (Object[]) pathDataArr);
                } else {
                    throw new InflateException(" Can't morph from " + fromString + " to " + toString);
                }
            } else if (pathData2 == null) {
                return null;
            } else {
                evaluator = new PathDataEvaluator();
                pathDataArr = new Object[VALUE_TYPE_INT];
                pathDataArr[VALUE_TYPE_FLOAT] = pathData2;
                return PropertyValuesHolder.ofObject(propertyName, evaluator, (Object[]) pathDataArr);
            }
        }
        evaluator = null;
        if (valueType == VALUE_TYPE_COLOR) {
            evaluator = ArgbEvaluator.getInstance();
        }
        if (getFloats) {
            float valueTo;
            float[] fArr;
            if (hasFrom) {
                float valueFrom;
                if (fromType == 5) {
                    valueFrom = styledAttributes.getDimension(valueFromId, 0.0f);
                } else {
                    valueFrom = styledAttributes.getFloat(valueFromId, 0.0f);
                }
                if (hasTo) {
                    if (toType == 5) {
                        valueTo = styledAttributes.getDimension(valueToId, 0.0f);
                    } else {
                        valueTo = styledAttributes.getFloat(valueToId, 0.0f);
                    }
                    fArr = new float[VALUE_TYPE_PATH];
                    fArr[VALUE_TYPE_FLOAT] = valueFrom;
                    fArr[VALUE_TYPE_INT] = valueTo;
                    returnValue = PropertyValuesHolder.ofFloat(propertyName, fArr);
                } else {
                    fArr = new float[VALUE_TYPE_INT];
                    fArr[VALUE_TYPE_FLOAT] = valueFrom;
                    returnValue = PropertyValuesHolder.ofFloat(propertyName, fArr);
                }
            } else {
                if (toType == 5) {
                    valueTo = styledAttributes.getDimension(valueToId, 0.0f);
                } else {
                    valueTo = styledAttributes.getFloat(valueToId, 0.0f);
                }
                fArr = new float[VALUE_TYPE_INT];
                fArr[VALUE_TYPE_FLOAT] = valueTo;
                returnValue = PropertyValuesHolder.ofFloat(propertyName, fArr);
            }
        } else if (hasFrom) {
            int valueFrom2;
            if (fromType == 5) {
                valueFrom2 = (int) styledAttributes.getDimension(valueFromId, 0.0f);
            } else if (isColorType(fromType)) {
                valueFrom2 = styledAttributes.getColor(valueFromId, VALUE_TYPE_FLOAT);
            } else {
                valueFrom2 = styledAttributes.getInt(valueFromId, VALUE_TYPE_FLOAT);
            }
            if (hasTo) {
                if (toType == 5) {
                    valueTo = (int) styledAttributes.getDimension(valueToId, 0.0f);
                } else if (isColorType(toType)) {
                    valueTo = styledAttributes.getColor(valueToId, VALUE_TYPE_FLOAT);
                } else {
                    valueTo = styledAttributes.getInt(valueToId, VALUE_TYPE_FLOAT);
                }
                r20 = new int[VALUE_TYPE_PATH];
                r20[VALUE_TYPE_FLOAT] = valueFrom2;
                r20[VALUE_TYPE_INT] = valueTo;
                returnValue = PropertyValuesHolder.ofInt(propertyName, r20);
            } else {
                r20 = new int[VALUE_TYPE_INT];
                r20[VALUE_TYPE_FLOAT] = valueFrom2;
                returnValue = PropertyValuesHolder.ofInt(propertyName, r20);
            }
        } else if (hasTo) {
            if (toType == 5) {
                valueTo = (int) styledAttributes.getDimension(valueToId, 0.0f);
            } else if (isColorType(toType)) {
                valueTo = styledAttributes.getColor(valueToId, VALUE_TYPE_FLOAT);
            } else {
                valueTo = styledAttributes.getInt(valueToId, VALUE_TYPE_FLOAT);
            }
            r20 = new int[VALUE_TYPE_INT];
            r20[VALUE_TYPE_FLOAT] = valueTo;
            returnValue = PropertyValuesHolder.ofInt(propertyName, r20);
        }
        if (returnValue == null || evaluator == null) {
            return returnValue;
        }
        returnValue.setEvaluator(evaluator);
        return returnValue;
    }

    private static void parseAnimatorFromTypeArray(ValueAnimator anim, TypedArray arrayAnimator, TypedArray arrayObjectAnimator, float pixelSize) {
        long duration = (long) arrayAnimator.getInt(VALUE_TYPE_INT, Voice.QUALITY_NORMAL);
        long startDelay = (long) arrayAnimator.getInt(VALUE_TYPE_PATH, VALUE_TYPE_FLOAT);
        int valueType = arrayAnimator.getInt(7, VALUE_TYPE_UNDEFINED);
        if (valueType == VALUE_TYPE_UNDEFINED) {
            valueType = inferValueTypeFromValues(arrayAnimator, 5, 6);
        }
        PropertyValuesHolder pvh = getPVH(arrayAnimator, valueType, 5, 6, ProxyInfo.LOCAL_EXCL_LIST);
        if (pvh != null) {
            PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[VALUE_TYPE_INT];
            propertyValuesHolderArr[VALUE_TYPE_FLOAT] = pvh;
            anim.setValues(propertyValuesHolderArr);
        }
        anim.setDuration(duration);
        anim.setStartDelay(startDelay);
        if (arrayAnimator.hasValue(VALUE_TYPE_COLOR)) {
            anim.setRepeatCount(arrayAnimator.getInt(VALUE_TYPE_COLOR, VALUE_TYPE_FLOAT));
        }
        if (arrayAnimator.hasValue(VALUE_TYPE_UNDEFINED)) {
            anim.setRepeatMode(arrayAnimator.getInt(VALUE_TYPE_UNDEFINED, VALUE_TYPE_INT));
        }
        if (arrayObjectAnimator != null) {
            boolean z;
            if (valueType == 0) {
                z = true;
            } else {
                z = DBG_ANIMATOR_INFLATER;
            }
            setupObjectAnimator(anim, arrayObjectAnimator, z, pixelSize);
        }
    }

    private static TypeEvaluator setupAnimatorForPath(ValueAnimator anim, TypedArray arrayAnimator) {
        String fromString = arrayAnimator.getString(5);
        String toString = arrayAnimator.getString(6);
        PathData pathData = fromString == null ? null : new PathData(fromString);
        PathData pathData2 = toString == null ? null : new PathData(toString);
        Object[] objArr;
        if (pathData != null) {
            if (pathData2 != null) {
                objArr = new Object[VALUE_TYPE_PATH];
                objArr[VALUE_TYPE_FLOAT] = pathData;
                objArr[VALUE_TYPE_INT] = pathData2;
                anim.setObjectValues(objArr);
                if (!PathParser.canMorph(pathData, pathData2)) {
                    throw new InflateException(arrayAnimator.getPositionDescription() + " Can't morph from " + fromString + " to " + toString);
                }
            }
            objArr = new Object[VALUE_TYPE_INT];
            objArr[VALUE_TYPE_FLOAT] = pathData;
            anim.setObjectValues(objArr);
            return new PathDataEvaluator();
        } else if (pathData2 == null) {
            return null;
        } else {
            objArr = new Object[VALUE_TYPE_INT];
            objArr[VALUE_TYPE_FLOAT] = pathData2;
            anim.setObjectValues(objArr);
            return new PathDataEvaluator();
        }
    }

    private static void setupObjectAnimator(ValueAnimator anim, TypedArray arrayObjectAnimator, boolean getFloats, float pixelSize) {
        ObjectAnimator oa = (ObjectAnimator) anim;
        String pathData = arrayObjectAnimator.getString(VALUE_TYPE_INT);
        if (pathData != null) {
            String propertyXName = arrayObjectAnimator.getString(VALUE_TYPE_PATH);
            String propertyYName = arrayObjectAnimator.getString(VALUE_TYPE_COLOR);
            if (propertyXName == null && propertyYName == null) {
                throw new InflateException(arrayObjectAnimator.getPositionDescription() + " propertyXName or propertyYName is needed for PathData");
            }
            Keyframes xKeyframes;
            Keyframes yKeyframes;
            PathKeyframes keyframeSet = KeyframeSet.ofPath(PathParser.createPathFromPathData(pathData), NetworkHistoryUtils.RECOVERY_PERCENTAGE * pixelSize);
            if (getFloats) {
                xKeyframes = keyframeSet.createXFloatKeyframes();
                yKeyframes = keyframeSet.createYFloatKeyframes();
            } else {
                xKeyframes = keyframeSet.createXIntKeyframes();
                yKeyframes = keyframeSet.createYIntKeyframes();
            }
            PropertyValuesHolder x = null;
            PropertyValuesHolder y = null;
            if (propertyXName != null) {
                x = PropertyValuesHolder.ofKeyframes(propertyXName, xKeyframes);
            }
            if (propertyYName != null) {
                y = PropertyValuesHolder.ofKeyframes(propertyYName, yKeyframes);
            }
            PropertyValuesHolder[] propertyValuesHolderArr;
            if (x == null) {
                propertyValuesHolderArr = new PropertyValuesHolder[VALUE_TYPE_INT];
                propertyValuesHolderArr[VALUE_TYPE_FLOAT] = y;
                oa.setValues(propertyValuesHolderArr);
                return;
            } else if (y == null) {
                propertyValuesHolderArr = new PropertyValuesHolder[VALUE_TYPE_INT];
                propertyValuesHolderArr[VALUE_TYPE_FLOAT] = x;
                oa.setValues(propertyValuesHolderArr);
                return;
            } else {
                propertyValuesHolderArr = new PropertyValuesHolder[VALUE_TYPE_PATH];
                propertyValuesHolderArr[VALUE_TYPE_FLOAT] = x;
                propertyValuesHolderArr[VALUE_TYPE_INT] = y;
                oa.setValues(propertyValuesHolderArr);
                return;
            }
        }
        oa.setPropertyName(arrayObjectAnimator.getString(VALUE_TYPE_FLOAT));
    }

    private static void setupValues(ValueAnimator anim, TypedArray arrayAnimator, boolean getFloats, boolean hasFrom, int fromType, boolean hasTo, int toType) {
        if (getFloats) {
            float valueTo;
            float[] fArr;
            if (hasFrom) {
                float valueFrom;
                if (fromType == 5) {
                    valueFrom = arrayAnimator.getDimension(5, 0.0f);
                } else {
                    valueFrom = arrayAnimator.getFloat(5, 0.0f);
                }
                if (hasTo) {
                    if (toType == 5) {
                        valueTo = arrayAnimator.getDimension(6, 0.0f);
                    } else {
                        valueTo = arrayAnimator.getFloat(6, 0.0f);
                    }
                    fArr = new float[VALUE_TYPE_PATH];
                    fArr[VALUE_TYPE_FLOAT] = valueFrom;
                    fArr[VALUE_TYPE_INT] = valueTo;
                    anim.setFloatValues(fArr);
                    return;
                }
                fArr = new float[VALUE_TYPE_INT];
                fArr[VALUE_TYPE_FLOAT] = valueFrom;
                anim.setFloatValues(fArr);
                return;
            }
            if (toType == 5) {
                valueTo = arrayAnimator.getDimension(6, 0.0f);
            } else {
                valueTo = arrayAnimator.getFloat(6, 0.0f);
            }
            fArr = new float[VALUE_TYPE_INT];
            fArr[VALUE_TYPE_FLOAT] = valueTo;
            anim.setFloatValues(fArr);
        } else if (hasFrom) {
            int valueFrom2;
            if (fromType == 5) {
                valueFrom2 = (int) arrayAnimator.getDimension(5, 0.0f);
            } else if (isColorType(fromType)) {
                valueFrom2 = arrayAnimator.getColor(5, VALUE_TYPE_FLOAT);
            } else {
                valueFrom2 = arrayAnimator.getInt(5, VALUE_TYPE_FLOAT);
            }
            if (hasTo) {
                if (toType == 5) {
                    valueTo = (int) arrayAnimator.getDimension(6, 0.0f);
                } else if (isColorType(toType)) {
                    valueTo = arrayAnimator.getColor(6, VALUE_TYPE_FLOAT);
                } else {
                    valueTo = arrayAnimator.getInt(6, VALUE_TYPE_FLOAT);
                }
                r6 = new int[VALUE_TYPE_PATH];
                r6[VALUE_TYPE_FLOAT] = valueFrom2;
                r6[VALUE_TYPE_INT] = valueTo;
                anim.setIntValues(r6);
                return;
            }
            r6 = new int[VALUE_TYPE_INT];
            r6[VALUE_TYPE_FLOAT] = valueFrom2;
            anim.setIntValues(r6);
        } else if (hasTo) {
            if (toType == 5) {
                valueTo = (int) arrayAnimator.getDimension(6, 0.0f);
            } else if (isColorType(toType)) {
                valueTo = arrayAnimator.getColor(6, VALUE_TYPE_FLOAT);
            } else {
                valueTo = arrayAnimator.getInt(6, VALUE_TYPE_FLOAT);
            }
            r6 = new int[VALUE_TYPE_INT];
            r6[VALUE_TYPE_FLOAT] = valueTo;
            anim.setIntValues(r6);
        }
    }

    private static Animator createAnimatorFromXml(Resources res, Theme theme, XmlPullParser parser, float pixelSize) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(res, theme, parser, Xml.asAttributeSet(parser), null, VALUE_TYPE_FLOAT, pixelSize);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Animator createAnimatorFromXml(Resources res, Theme theme, XmlPullParser parser, AttributeSet attrs, AnimatorSet parent, int sequenceOrdering, float pixelSize) throws XmlPullParserException, IOException {
        Animator anim = null;
        Iterable childAnims = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != VALUE_TYPE_COLOR || parser.getDepth() > depth) && type != VALUE_TYPE_INT) {
                if (type == VALUE_TYPE_PATH) {
                    String name = parser.getName();
                    boolean gotValues = DBG_ANIMATOR_INFLATER;
                    if (!name.equals("objectAnimator")) {
                        if (!name.equals("animator")) {
                            if (!name.equals("set")) {
                                if (!name.equals("propertyValuesHolder")) {
                                    break;
                                }
                                PropertyValuesHolder[] values = loadValues(res, theme, parser, Xml.asAttributeSet(parser));
                                if (!(values == null || anim == null || !(anim instanceof ValueAnimator))) {
                                    ((ValueAnimator) anim).setValues(values);
                                }
                                gotValues = true;
                            } else {
                                TypedArray a;
                                anim = new AnimatorSet();
                                if (theme != null) {
                                    a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.AnimatorSet, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
                                } else {
                                    a = res.obtainAttributes(attrs, com.android.internal.R.styleable.AnimatorSet);
                                }
                                anim.appendChangingConfigurations(a.getChangingConfigurations());
                                Resources resources = res;
                                Theme theme2 = theme;
                                XmlPullParser xmlPullParser = parser;
                                AttributeSet attributeSet = attrs;
                                createAnimatorFromXml(resources, theme2, xmlPullParser, attributeSet, (AnimatorSet) anim, a.getInt(VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT), pixelSize);
                                a.recycle();
                            }
                        } else {
                            anim = loadAnimator(res, theme, attrs, null, pixelSize);
                        }
                    } else {
                        anim = loadObjectAnimator(res, theme, attrs, pixelSize);
                    }
                    if (!(parent == null || gotValues)) {
                        if (childAnims == null) {
                            childAnims = new ArrayList();
                        }
                        childAnims.add(anim);
                    }
                }
            }
        }
        throw new RuntimeException("Unknown animator name: " + parser.getName());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PropertyValuesHolder[] loadValues(Resources res, Theme theme, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        PropertyValuesHolder[] valuesArray;
        ArrayList arrayList = null;
        while (true) {
            int type = parser.getEventType();
            if (type == VALUE_TYPE_COLOR || type == VALUE_TYPE_INT) {
                valuesArray = null;
            } else if (type != VALUE_TYPE_PATH) {
                parser.next();
            } else {
                if (parser.getName().equals("propertyValuesHolder")) {
                    TypedArray a;
                    if (theme != null) {
                        a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.PropertyValuesHolder, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
                    } else {
                        a = res.obtainAttributes(attrs, com.android.internal.R.styleable.PropertyValuesHolder);
                    }
                    String propertyName = a.getString(VALUE_TYPE_COLOR);
                    int valueType = a.getInt(VALUE_TYPE_PATH, VALUE_TYPE_UNDEFINED);
                    PropertyValuesHolder pvh = loadPvh(res, theme, parser, propertyName, valueType);
                    if (pvh == null) {
                        pvh = getPVH(a, valueType, VALUE_TYPE_FLOAT, VALUE_TYPE_INT, propertyName);
                    }
                    if (pvh != null) {
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                        }
                        arrayList.add(pvh);
                    }
                    a.recycle();
                }
                parser.next();
            }
        }
        valuesArray = null;
        if (arrayList != null) {
            int count = arrayList.size();
            valuesArray = new PropertyValuesHolder[count];
            for (int i = VALUE_TYPE_FLOAT; i < count; i += VALUE_TYPE_INT) {
                valuesArray[i] = (PropertyValuesHolder) arrayList.get(i);
            }
        }
        return valuesArray;
    }

    private static int inferValueTypeOfKeyframe(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        int valueType;
        boolean hasValue = DBG_ANIMATOR_INFLATER;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Keyframe, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
        } else {
            a = res.obtainAttributes(attrs, com.android.internal.R.styleable.Keyframe);
        }
        TypedValue keyframeValue = a.peekValue(VALUE_TYPE_FLOAT);
        if (keyframeValue != null) {
            hasValue = true;
        }
        if (hasValue && isColorType(keyframeValue.type)) {
            valueType = VALUE_TYPE_COLOR;
        } else {
            valueType = VALUE_TYPE_FLOAT;
        }
        a.recycle();
        return valueType;
    }

    private static int inferValueTypeFromValues(TypedArray styledAttributes, int valueFromId, int valueToId) {
        boolean hasFrom;
        boolean hasTo = true;
        TypedValue tvFrom = styledAttributes.peekValue(valueFromId);
        if (tvFrom != null) {
            hasFrom = true;
        } else {
            hasFrom = DBG_ANIMATOR_INFLATER;
        }
        int fromType = hasFrom ? tvFrom.type : VALUE_TYPE_FLOAT;
        TypedValue tvTo = styledAttributes.peekValue(valueToId);
        if (tvTo == null) {
            hasTo = DBG_ANIMATOR_INFLATER;
        }
        int toType = hasTo ? tvTo.type : VALUE_TYPE_FLOAT;
        if ((hasFrom && isColorType(fromType)) || (hasTo && isColorType(toType))) {
            return VALUE_TYPE_COLOR;
        }
        return VALUE_TYPE_FLOAT;
    }

    private static void dumpKeyframes(Object[] keyframes, String header) {
        if (keyframes != null && keyframes.length != 0) {
            Log.d(TAG, header);
            int count = keyframes.length;
            for (int i = VALUE_TYPE_FLOAT; i < count; i += VALUE_TYPE_INT) {
                Keyframe keyframe = keyframes[i];
                Log.d(TAG, "Keyframe " + i + ": fraction " + (keyframe.getFraction() < 0.0f ? "null" : Float.valueOf(keyframe.getFraction())) + ", " + ", value : " + (keyframe.hasValue() ? keyframe.getValue() : "null"));
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PropertyValuesHolder loadPvh(Resources res, Theme theme, XmlPullParser parser, String propertyName, int valueType) throws XmlPullParserException, IOException {
        Keyframe keyframe;
        Keyframe firstKeyframe;
        Keyframe lastKeyframe;
        Keyframe[] keyframeArray;
        int i;
        PropertyValuesHolder value = null;
        ArrayList arrayList = null;
        while (true) {
            int count;
            float endFraction;
            float startFraction;
            int endIndex;
            int j;
            int type = parser.next();
            if (type != VALUE_TYPE_COLOR && type != VALUE_TYPE_INT) {
                if (parser.getName().equals("keyframe")) {
                    if (valueType == VALUE_TYPE_UNDEFINED) {
                        valueType = inferValueTypeOfKeyframe(res, theme, Xml.asAttributeSet(parser));
                    }
                    keyframe = loadKeyframe(res, theme, Xml.asAttributeSet(parser), valueType);
                    if (keyframe != null) {
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                        }
                        arrayList.add(keyframe);
                    }
                    parser.next();
                }
            } else if (arrayList != null) {
                count = arrayList.size();
                if (count > 0) {
                    firstKeyframe = (Keyframe) arrayList.get(VALUE_TYPE_FLOAT);
                    lastKeyframe = (Keyframe) arrayList.get(count - 1);
                    endFraction = lastKeyframe.getFraction();
                    if (endFraction < Engine.DEFAULT_VOLUME) {
                        if (endFraction >= 0.0f) {
                            lastKeyframe.setFraction(Engine.DEFAULT_VOLUME);
                        } else {
                            arrayList.add(arrayList.size(), createNewKeyframe(lastKeyframe, Engine.DEFAULT_VOLUME));
                            count += VALUE_TYPE_INT;
                        }
                    }
                    startFraction = firstKeyframe.getFraction();
                    if (startFraction != 0.0f) {
                        if (startFraction >= 0.0f) {
                            firstKeyframe.setFraction(0.0f);
                        } else {
                            arrayList.add(VALUE_TYPE_FLOAT, createNewKeyframe(firstKeyframe, 0.0f));
                            count += VALUE_TYPE_INT;
                        }
                    }
                    keyframeArray = new Keyframe[count];
                    arrayList.toArray(keyframeArray);
                    for (i = VALUE_TYPE_FLOAT; i < count; i += VALUE_TYPE_INT) {
                        keyframe = keyframeArray[i];
                        if (keyframe.getFraction() >= 0.0f) {
                            if (i == 0) {
                                keyframe.setFraction(0.0f);
                            } else if (i != count - 1) {
                                keyframe.setFraction(Engine.DEFAULT_VOLUME);
                            } else {
                                int startIndex = i;
                                endIndex = i;
                                j = i + VALUE_TYPE_INT;
                                while (j < count - 1 && keyframeArray[j].getFraction() < 0.0f) {
                                    endIndex = j;
                                    j += VALUE_TYPE_INT;
                                }
                                distributeKeyframes(keyframeArray, keyframeArray[endIndex + VALUE_TYPE_INT].getFraction() - keyframeArray[startIndex - 1].getFraction(), startIndex, endIndex);
                            }
                        }
                    }
                    value = PropertyValuesHolder.ofKeyframe(propertyName, keyframeArray);
                    if (valueType == VALUE_TYPE_COLOR) {
                        value.setEvaluator(ArgbEvaluator.getInstance());
                    }
                }
            }
        }
        if (arrayList != null) {
            count = arrayList.size();
            if (count > 0) {
                firstKeyframe = (Keyframe) arrayList.get(VALUE_TYPE_FLOAT);
                lastKeyframe = (Keyframe) arrayList.get(count - 1);
                endFraction = lastKeyframe.getFraction();
                if (endFraction < Engine.DEFAULT_VOLUME) {
                    if (endFraction >= 0.0f) {
                        arrayList.add(arrayList.size(), createNewKeyframe(lastKeyframe, Engine.DEFAULT_VOLUME));
                        count += VALUE_TYPE_INT;
                    } else {
                        lastKeyframe.setFraction(Engine.DEFAULT_VOLUME);
                    }
                }
                startFraction = firstKeyframe.getFraction();
                if (startFraction != 0.0f) {
                    if (startFraction >= 0.0f) {
                        arrayList.add(VALUE_TYPE_FLOAT, createNewKeyframe(firstKeyframe, 0.0f));
                        count += VALUE_TYPE_INT;
                    } else {
                        firstKeyframe.setFraction(0.0f);
                    }
                }
                keyframeArray = new Keyframe[count];
                arrayList.toArray(keyframeArray);
                for (i = VALUE_TYPE_FLOAT; i < count; i += VALUE_TYPE_INT) {
                    keyframe = keyframeArray[i];
                    if (keyframe.getFraction() >= 0.0f) {
                        if (i == 0) {
                            keyframe.setFraction(0.0f);
                        } else if (i != count - 1) {
                            int startIndex2 = i;
                            endIndex = i;
                            j = i + VALUE_TYPE_INT;
                            while (j < count - 1) {
                                endIndex = j;
                                j += VALUE_TYPE_INT;
                            }
                            distributeKeyframes(keyframeArray, keyframeArray[endIndex + VALUE_TYPE_INT].getFraction() - keyframeArray[startIndex2 - 1].getFraction(), startIndex2, endIndex);
                        } else {
                            keyframe.setFraction(Engine.DEFAULT_VOLUME);
                        }
                    }
                }
                value = PropertyValuesHolder.ofKeyframe(propertyName, keyframeArray);
                if (valueType == VALUE_TYPE_COLOR) {
                    value.setEvaluator(ArgbEvaluator.getInstance());
                }
            }
        }
        return value;
    }

    private static Keyframe createNewKeyframe(Keyframe sampleKeyframe, float fraction) {
        if (sampleKeyframe.getType() == Float.TYPE) {
            return Keyframe.ofFloat(fraction);
        }
        if (sampleKeyframe.getType() == Integer.TYPE) {
            return Keyframe.ofInt(fraction);
        }
        return Keyframe.ofObject(fraction);
    }

    private static void distributeKeyframes(Keyframe[] keyframes, float gap, int startIndex, int endIndex) {
        float increment = gap / ((float) ((endIndex - startIndex) + VALUE_TYPE_PATH));
        for (int i = startIndex; i <= endIndex; i += VALUE_TYPE_INT) {
            keyframes[i].setFraction(keyframes[i - 1].getFraction() + increment);
        }
    }

    private static Keyframe loadKeyframe(Resources res, Theme theme, AttributeSet attrs, int valueType) throws XmlPullParserException, IOException {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Keyframe, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
        } else {
            a = res.obtainAttributes(attrs, com.android.internal.R.styleable.Keyframe);
        }
        Keyframe keyframe = null;
        float fraction = a.getFloat(VALUE_TYPE_COLOR, ScaledLayoutParams.SCALE_UNSPECIFIED);
        TypedValue keyframeValue = a.peekValue(VALUE_TYPE_FLOAT);
        boolean hasValue = keyframeValue != null ? true : DBG_ANIMATOR_INFLATER;
        if (valueType == VALUE_TYPE_UNDEFINED) {
            if (hasValue && isColorType(keyframeValue.type)) {
                valueType = VALUE_TYPE_COLOR;
            } else {
                valueType = VALUE_TYPE_FLOAT;
            }
        }
        if (hasValue) {
            switch (valueType) {
                case VALUE_TYPE_FLOAT /*0*/:
                    keyframe = Keyframe.ofFloat(fraction, a.getFloat(VALUE_TYPE_FLOAT, 0.0f));
                    break;
                case VALUE_TYPE_INT /*1*/:
                case VALUE_TYPE_COLOR /*3*/:
                    keyframe = Keyframe.ofInt(fraction, a.getInt(VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT));
                    break;
            }
        } else if (valueType == 0) {
            keyframe = Keyframe.ofFloat(fraction);
        } else {
            keyframe = Keyframe.ofInt(fraction);
        }
        int resID = a.getResourceId(VALUE_TYPE_INT, VALUE_TYPE_FLOAT);
        if (resID > 0) {
            keyframe.setInterpolator(AnimationUtils.loadInterpolator(res, theme, resID));
        }
        a.recycle();
        return keyframe;
    }

    private static ObjectAnimator loadObjectAnimator(Resources res, Theme theme, AttributeSet attrs, float pathErrorScale) throws NotFoundException {
        ObjectAnimator anim = new ObjectAnimator();
        loadAnimator(res, theme, attrs, anim, pathErrorScale);
        return anim;
    }

    private static ValueAnimator loadAnimator(Resources res, Theme theme, AttributeSet attrs, ValueAnimator anim, float pathErrorScale) throws NotFoundException {
        TypedArray arrayAnimator;
        TypedArray arrayObjectAnimator = null;
        if (theme != null) {
            arrayAnimator = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Animator, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
        } else {
            arrayAnimator = res.obtainAttributes(attrs, com.android.internal.R.styleable.Animator);
        }
        if (anim != null) {
            if (theme != null) {
                arrayObjectAnimator = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.PropertyAnimator, VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
            } else {
                arrayObjectAnimator = res.obtainAttributes(attrs, com.android.internal.R.styleable.PropertyAnimator);
            }
            anim.appendChangingConfigurations(arrayObjectAnimator.getChangingConfigurations());
        }
        if (anim == null) {
            anim = new ValueAnimator();
        }
        anim.appendChangingConfigurations(arrayAnimator.getChangingConfigurations());
        parseAnimatorFromTypeArray(anim, arrayAnimator, arrayObjectAnimator, pathErrorScale);
        int resID = arrayAnimator.getResourceId(VALUE_TYPE_FLOAT, VALUE_TYPE_FLOAT);
        if (resID > 0) {
            Interpolator interpolator = AnimationUtils.loadInterpolator(res, theme, resID);
            if (interpolator instanceof BaseInterpolator) {
                anim.appendChangingConfigurations(((BaseInterpolator) interpolator).getChangingConfiguration());
            }
            anim.setInterpolator(interpolator);
        }
        arrayAnimator.recycle();
        if (arrayObjectAnimator != null) {
            arrayObjectAnimator.recycle();
        }
        return anim;
    }

    private static int getChangingConfigs(Resources resources, int id) {
        int i;
        synchronized (sTmpTypedValue) {
            resources.getValue(id, sTmpTypedValue, true);
            i = sTmpTypedValue.changingConfigurations;
        }
        return i;
    }

    private static boolean isColorType(int type) {
        return (type < 28 || type > 31) ? DBG_ANIMATOR_INFLATER : true;
    }

    public static HwStateListAnimator loadHwStateListAnimator(Context context, int id) throws NotFoundException {
        NotFoundException rnf;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getAnimation(id);
            HwStateListAnimator animator = createHwStateListAnimatorFromXml(context, xmlResourceParser, Xml.asAttributeSet(xmlResourceParser));
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return animator;
        } catch (XmlPullParserException ex) {
            rnf = new NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            rnf = new NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex2);
            throw rnf;
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private static HwStateListAnimator createHwStateListAnimatorFromXml(Context context, XmlPullParser parser, AttributeSet attributeSet) throws IOException, XmlPullParserException {
        HwStateListAnimator stateListAnimator = HwFrameworkFactory.getHwStateListAnimator();
        while (true) {
            switch (parser.next()) {
                case VALUE_TYPE_INT /*1*/:
                case VALUE_TYPE_COLOR /*3*/:
                    return stateListAnimator;
                case VALUE_TYPE_PATH /*2*/:
                    Animator animator = null;
                    if (HwThemeManager.TAG_ITEM.equals(parser.getName())) {
                        int attributeCount = parser.getAttributeCount();
                        int[] states = new int[attributeCount];
                        int i = VALUE_TYPE_FLOAT;
                        int stateIndex = VALUE_TYPE_FLOAT;
                        while (i < attributeCount) {
                            int stateIndex2;
                            int attrName = attributeSet.getAttributeNameResource(i);
                            if (attrName == R.attr.animation) {
                                animator = loadAnimator(context, attributeSet.getAttributeResourceValue(i, VALUE_TYPE_FLOAT));
                                stateIndex2 = stateIndex;
                            } else {
                                stateIndex2 = stateIndex + VALUE_TYPE_INT;
                                if (!attributeSet.getAttributeBooleanValue(i, DBG_ANIMATOR_INFLATER)) {
                                    attrName = -attrName;
                                }
                                states[stateIndex] = attrName;
                            }
                            i += VALUE_TYPE_INT;
                            stateIndex = stateIndex2;
                        }
                        if (animator == null) {
                            animator = createAnimatorFromXml(context.getResources(), context.getTheme(), parser, Engine.DEFAULT_VOLUME);
                        }
                        if (animator != null) {
                            stateListAnimator.addState(StateSet.trimStateSet(states, stateIndex), animator);
                            break;
                        }
                        throw new NotFoundException("animation state item must have a valid animation");
                    }
                    continue;
                default:
                    break;
            }
        }
    }
}
