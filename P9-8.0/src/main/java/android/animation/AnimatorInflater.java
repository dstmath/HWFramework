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
import android.hardware.camera2.params.TonemapCurve;
import android.hwtheme.HwThemeManager;
import android.net.ProxyInfo;
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
    private static final TypedValue sTmpTypedValue = new TypedValue();

    private static class PathDataEvaluator implements TypeEvaluator<PathData> {
        private final PathData mPathData;

        /* synthetic */ PathDataEvaluator(PathDataEvaluator -this0) {
            this();
        }

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

    public static Animator loadAnimator(Context context, int id) throws NotFoundException {
        return loadAnimator(context.getResources(), context.getTheme(), id);
    }

    public static Animator loadAnimator(Resources resources, Theme theme, int id) throws NotFoundException {
        return loadAnimator(resources, theme, id, 1.0f);
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
                case 1:
                case 3:
                    return stateListAnimator;
                case 2:
                    Animator animator = null;
                    if (HwThemeManager.TAG_ITEM.equals(parser.getName())) {
                        int attributeCount = parser.getAttributeCount();
                        int[] states = new int[attributeCount];
                        int i = 0;
                        int stateIndex = 0;
                        while (i < attributeCount) {
                            int stateIndex2;
                            int attrName = attributeSet.getAttributeNameResource(i);
                            if (attrName == R.attr.animation) {
                                animator = loadAnimator(context, attributeSet.getAttributeResourceValue(i, 0));
                                stateIndex2 = stateIndex;
                            } else {
                                stateIndex2 = stateIndex + 1;
                                if (!attributeSet.getAttributeBooleanValue(i, false)) {
                                    attrName = -attrName;
                                }
                                states[stateIndex] = attrName;
                            }
                            i++;
                            stateIndex = stateIndex2;
                        }
                        if (animator == null) {
                            animator = createAnimatorFromXml(context.getResources(), context.getTheme(), parser, 1.0f);
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
        boolean hasFrom = tvFrom != null;
        int fromType = hasFrom ? tvFrom.type : 0;
        TypedValue tvTo = styledAttributes.peekValue(valueToId);
        boolean hasTo = tvTo != null;
        int toType = hasTo ? tvTo.type : 0;
        if (valueType == 4) {
            if ((hasFrom && isColorType(fromType)) || (hasTo && isColorType(toType))) {
                valueType = 3;
            } else {
                valueType = 0;
            }
        }
        boolean getFloats = valueType == 0;
        PropertyValuesHolder returnValue = null;
        TypeEvaluator evaluator;
        if (valueType == 2) {
            String fromString = styledAttributes.getString(valueFromId);
            String toString = styledAttributes.getString(valueToId);
            PathData nodesFrom = fromString == null ? null : new PathData(fromString);
            PathData nodesTo = toString == null ? null : new PathData(toString);
            if (nodesFrom == null && nodesTo == null) {
                return null;
            }
            if (nodesFrom != null) {
                evaluator = new PathDataEvaluator(null);
                if (nodesTo == null) {
                    return PropertyValuesHolder.ofObject(propertyName, evaluator, (Object[]) new Object[]{nodesFrom});
                } else if (PathParser.canMorph(nodesFrom, nodesTo)) {
                    return PropertyValuesHolder.ofObject(propertyName, evaluator, (Object[]) new Object[]{nodesFrom, nodesTo});
                } else {
                    throw new InflateException(" Can't morph from " + fromString + " to " + toString);
                }
            } else if (nodesTo == null) {
                return null;
            } else {
                return PropertyValuesHolder.ofObject(propertyName, new PathDataEvaluator(null), (Object[]) new Object[]{nodesTo});
            }
        }
        evaluator = null;
        if (valueType == 3) {
            evaluator = ArgbEvaluator.getInstance();
        }
        int valueTo;
        if (getFloats) {
            float valueTo2;
            if (hasFrom) {
                float valueFrom;
                if (fromType == 5) {
                    valueFrom = styledAttributes.getDimension(valueFromId, TonemapCurve.LEVEL_BLACK);
                } else {
                    valueFrom = styledAttributes.getFloat(valueFromId, TonemapCurve.LEVEL_BLACK);
                }
                if (hasTo) {
                    if (toType == 5) {
                        valueTo2 = styledAttributes.getDimension(valueToId, TonemapCurve.LEVEL_BLACK);
                    } else {
                        valueTo2 = styledAttributes.getFloat(valueToId, TonemapCurve.LEVEL_BLACK);
                    }
                    returnValue = PropertyValuesHolder.ofFloat(propertyName, valueFrom, valueTo2);
                } else {
                    returnValue = PropertyValuesHolder.ofFloat(propertyName, valueFrom);
                }
            } else {
                if (toType == 5) {
                    valueTo2 = styledAttributes.getDimension(valueToId, TonemapCurve.LEVEL_BLACK);
                } else {
                    valueTo2 = styledAttributes.getFloat(valueToId, TonemapCurve.LEVEL_BLACK);
                }
                returnValue = PropertyValuesHolder.ofFloat(propertyName, valueTo2);
            }
        } else if (hasFrom) {
            int valueFrom2;
            if (fromType == 5) {
                valueFrom2 = (int) styledAttributes.getDimension(valueFromId, TonemapCurve.LEVEL_BLACK);
            } else if (isColorType(fromType)) {
                valueFrom2 = styledAttributes.getColor(valueFromId, 0);
            } else {
                valueFrom2 = styledAttributes.getInt(valueFromId, 0);
            }
            if (hasTo) {
                if (toType == 5) {
                    valueTo = (int) styledAttributes.getDimension(valueToId, TonemapCurve.LEVEL_BLACK);
                } else if (isColorType(toType)) {
                    valueTo = styledAttributes.getColor(valueToId, 0);
                } else {
                    valueTo = styledAttributes.getInt(valueToId, 0);
                }
                returnValue = PropertyValuesHolder.ofInt(propertyName, valueFrom2, valueTo);
            } else {
                returnValue = PropertyValuesHolder.ofInt(propertyName, valueFrom2);
            }
        } else if (hasTo) {
            if (toType == 5) {
                valueTo = (int) styledAttributes.getDimension(valueToId, TonemapCurve.LEVEL_BLACK);
            } else if (isColorType(toType)) {
                valueTo = styledAttributes.getColor(valueToId, 0);
            } else {
                valueTo = styledAttributes.getInt(valueToId, 0);
            }
            returnValue = PropertyValuesHolder.ofInt(propertyName, valueTo);
        }
        if (returnValue == null || evaluator == null) {
            return returnValue;
        }
        returnValue.setEvaluator(evaluator);
        return returnValue;
    }

    private static void parseAnimatorFromTypeArray(ValueAnimator anim, TypedArray arrayAnimator, TypedArray arrayObjectAnimator, float pixelSize) {
        long duration = (long) arrayAnimator.getInt(1, 300);
        long startDelay = (long) arrayAnimator.getInt(2, 0);
        int valueType = arrayAnimator.getInt(7, 4);
        if (valueType == 4) {
            valueType = inferValueTypeFromValues(arrayAnimator, 5, 6);
        }
        if (getPVH(arrayAnimator, valueType, 5, 6, ProxyInfo.LOCAL_EXCL_LIST) != null) {
            anim.setValues(getPVH(arrayAnimator, valueType, 5, 6, ProxyInfo.LOCAL_EXCL_LIST));
        }
        anim.setDuration(duration);
        anim.setStartDelay(startDelay);
        if (arrayAnimator.hasValue(3)) {
            anim.setRepeatCount(arrayAnimator.getInt(3, 0));
        }
        if (arrayAnimator.hasValue(4)) {
            anim.setRepeatMode(arrayAnimator.getInt(4, 1));
        }
        if (arrayObjectAnimator != null) {
            setupObjectAnimator(anim, arrayObjectAnimator, valueType, pixelSize);
        }
    }

    private static TypeEvaluator setupAnimatorForPath(ValueAnimator anim, TypedArray arrayAnimator) {
        String fromString = arrayAnimator.getString(5);
        String toString = arrayAnimator.getString(6);
        PathData pathDataFrom = fromString == null ? null : new PathData(fromString);
        PathData pathDataTo = toString == null ? null : new PathData(toString);
        if (pathDataFrom != null) {
            if (pathDataTo != null) {
                anim.setObjectValues(pathDataFrom, pathDataTo);
                if (!PathParser.canMorph(pathDataFrom, pathDataTo)) {
                    throw new InflateException(arrayAnimator.getPositionDescription() + " Can't morph from " + fromString + " to " + toString);
                }
            }
            anim.setObjectValues(pathDataFrom);
            return new PathDataEvaluator();
        } else if (pathDataTo == null) {
            return null;
        } else {
            anim.setObjectValues(pathDataTo);
            return new PathDataEvaluator();
        }
    }

    private static void setupObjectAnimator(ValueAnimator anim, TypedArray arrayObjectAnimator, int valueType, float pixelSize) {
        ObjectAnimator oa = (ObjectAnimator) anim;
        String pathData = arrayObjectAnimator.getString(1);
        if (pathData != null) {
            String propertyXName = arrayObjectAnimator.getString(2);
            String propertyYName = arrayObjectAnimator.getString(3);
            if (valueType == 2 || valueType == 4) {
                valueType = 0;
            }
            if (propertyXName == null && propertyYName == null) {
                throw new InflateException(arrayObjectAnimator.getPositionDescription() + " propertyXName or propertyYName is needed for PathData");
            }
            Keyframes xKeyframes;
            Keyframes yKeyframes;
            PathKeyframes keyframeSet = KeyframeSet.ofPath(PathParser.createPathFromPathData(pathData), 0.5f * pixelSize);
            if (valueType == 0) {
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
            if (x == null) {
                oa.setValues(y);
                return;
            } else if (y == null) {
                oa.setValues(x);
                return;
            } else {
                oa.setValues(x, y);
                return;
            }
        }
        oa.setPropertyName(arrayObjectAnimator.getString(0));
    }

    private static void setupValues(ValueAnimator anim, TypedArray arrayAnimator, boolean getFloats, boolean hasFrom, int fromType, boolean hasTo, int toType) {
        int valueTo;
        if (getFloats) {
            float valueTo2;
            if (hasFrom) {
                float valueFrom;
                if (fromType == 5) {
                    valueFrom = arrayAnimator.getDimension(5, TonemapCurve.LEVEL_BLACK);
                } else {
                    valueFrom = arrayAnimator.getFloat(5, TonemapCurve.LEVEL_BLACK);
                }
                if (hasTo) {
                    if (toType == 5) {
                        valueTo2 = arrayAnimator.getDimension(6, TonemapCurve.LEVEL_BLACK);
                    } else {
                        valueTo2 = arrayAnimator.getFloat(6, TonemapCurve.LEVEL_BLACK);
                    }
                    anim.setFloatValues(valueFrom, valueTo2);
                    return;
                }
                anim.setFloatValues(valueFrom);
                return;
            }
            if (toType == 5) {
                valueTo2 = arrayAnimator.getDimension(6, TonemapCurve.LEVEL_BLACK);
            } else {
                valueTo2 = arrayAnimator.getFloat(6, TonemapCurve.LEVEL_BLACK);
            }
            anim.setFloatValues(valueTo2);
        } else if (hasFrom) {
            int valueFrom2;
            if (fromType == 5) {
                valueFrom2 = (int) arrayAnimator.getDimension(5, TonemapCurve.LEVEL_BLACK);
            } else if (isColorType(fromType)) {
                valueFrom2 = arrayAnimator.getColor(5, 0);
            } else {
                valueFrom2 = arrayAnimator.getInt(5, 0);
            }
            if (hasTo) {
                if (toType == 5) {
                    valueTo = (int) arrayAnimator.getDimension(6, TonemapCurve.LEVEL_BLACK);
                } else if (isColorType(toType)) {
                    valueTo = arrayAnimator.getColor(6, 0);
                } else {
                    valueTo = arrayAnimator.getInt(6, 0);
                }
                anim.setIntValues(valueFrom2, valueTo);
                return;
            }
            anim.setIntValues(valueFrom2);
        } else if (hasTo) {
            if (toType == 5) {
                valueTo = (int) arrayAnimator.getDimension(6, TonemapCurve.LEVEL_BLACK);
            } else if (isColorType(toType)) {
                valueTo = arrayAnimator.getColor(6, 0);
            } else {
                valueTo = arrayAnimator.getInt(6, 0);
            }
            anim.setIntValues(valueTo);
        }
    }

    private static Animator createAnimatorFromXml(Resources res, Theme theme, XmlPullParser parser, float pixelSize) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(res, theme, parser, Xml.asAttributeSet(parser), null, 0, pixelSize);
    }

    private static Animator createAnimatorFromXml(Resources res, Theme theme, XmlPullParser parser, AttributeSet attrs, AnimatorSet parent, int sequenceOrdering, float pixelSize) throws XmlPullParserException, IOException {
        Animator anim = null;
        Iterable childAnims = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    boolean gotValues = false;
                    if (name.equals("objectAnimator")) {
                        anim = loadObjectAnimator(res, theme, attrs, pixelSize);
                    } else {
                        if (name.equals("animator")) {
                            anim = loadAnimator(res, theme, attrs, null, pixelSize);
                        } else {
                            if (name.equals("set")) {
                                TypedArray a;
                                anim = new AnimatorSet();
                                if (theme != null) {
                                    a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.AnimatorSet, 0, 0);
                                } else {
                                    a = res.obtainAttributes(attrs, com.android.internal.R.styleable.AnimatorSet);
                                }
                                anim.appendChangingConfigurations(a.getChangingConfigurations());
                                Resources resources = res;
                                Theme theme2 = theme;
                                XmlPullParser xmlPullParser = parser;
                                AttributeSet attributeSet = attrs;
                                createAnimatorFromXml(resources, theme2, xmlPullParser, attributeSet, (AnimatorSet) anim, a.getInt(0, 0), pixelSize);
                                a.recycle();
                            } else {
                                if (name.equals("propertyValuesHolder")) {
                                    PropertyValuesHolder[] values = loadValues(res, theme, parser, Xml.asAttributeSet(parser));
                                    if (!(values == null || anim == null || !(anim instanceof ValueAnimator))) {
                                        ((ValueAnimator) anim).setValues(values);
                                    }
                                    gotValues = true;
                                } else {
                                    throw new RuntimeException("Unknown animator name: " + parser.getName());
                                }
                            }
                        }
                    }
                    if (!(parent == null || (gotValues ^ 1) == 0)) {
                        if (childAnims == null) {
                            childAnims = new ArrayList();
                        }
                        childAnims.add(anim);
                    }
                }
            }
        }
        if (!(parent == null || childAnims == null)) {
            Animator[] animsArray = new Animator[childAnims.size()];
            int index = 0;
            for (Animator a2 : childAnims) {
                int index2 = index + 1;
                animsArray[index] = a2;
                index = index2;
            }
            if (sequenceOrdering == 0) {
                parent.playTogether(animsArray);
            } else {
                parent.playSequentially(animsArray);
            }
        }
        return anim;
    }

    private static PropertyValuesHolder[] loadValues(Resources res, Theme theme, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        PropertyValuesHolder[] valuesArray;
        ArrayList arrayList = null;
        while (true) {
            int type = parser.getEventType();
            if (type == 3 || type == 1) {
                valuesArray = null;
            } else if (type != 2) {
                parser.next();
            } else {
                if (parser.getName().equals("propertyValuesHolder")) {
                    TypedArray a;
                    if (theme != null) {
                        a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.PropertyValuesHolder, 0, 0);
                    } else {
                        a = res.obtainAttributes(attrs, com.android.internal.R.styleable.PropertyValuesHolder);
                    }
                    String propertyName = a.getString(3);
                    int valueType = a.getInt(2, 4);
                    PropertyValuesHolder pvh = loadPvh(res, theme, parser, propertyName, valueType);
                    if (pvh == null) {
                        pvh = getPVH(a, valueType, 0, 1, propertyName);
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
            for (int i = 0; i < count; i++) {
                valuesArray[i] = (PropertyValuesHolder) arrayList.get(i);
            }
        }
        return valuesArray;
    }

    private static int inferValueTypeOfKeyframe(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        int valueType;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Keyframe, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, com.android.internal.R.styleable.Keyframe);
        }
        TypedValue keyframeValue = a.peekValue(0);
        if ((keyframeValue != null) && isColorType(keyframeValue.type)) {
            valueType = 3;
        } else {
            valueType = 0;
        }
        a.recycle();
        return valueType;
    }

    private static int inferValueTypeFromValues(TypedArray styledAttributes, int valueFromId, int valueToId) {
        TypedValue tvFrom = styledAttributes.peekValue(valueFromId);
        boolean hasFrom = tvFrom != null;
        int fromType = hasFrom ? tvFrom.type : 0;
        TypedValue tvTo = styledAttributes.peekValue(valueToId);
        boolean hasTo = tvTo != null;
        int toType = hasTo ? tvTo.type : 0;
        if ((hasFrom && isColorType(fromType)) || (hasTo && isColorType(toType))) {
            return 3;
        }
        return 0;
    }

    private static void dumpKeyframes(Object[] keyframes, String header) {
        if (keyframes != null && keyframes.length != 0) {
            Log.d(TAG, header);
            int count = keyframes.length;
            for (int i = 0; i < count; i++) {
                Keyframe keyframe = keyframes[i];
                Log.d(TAG, "Keyframe " + i + ": fraction " + (keyframe.getFraction() < TonemapCurve.LEVEL_BLACK ? "null" : Float.valueOf(keyframe.getFraction())) + ", " + ", value : " + (keyframe.hasValue() ? keyframe.getValue() : "null"));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x005e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PropertyValuesHolder loadPvh(Resources res, Theme theme, XmlPullParser parser, String propertyName, int valueType) throws XmlPullParserException, IOException {
        PropertyValuesHolder value = null;
        ArrayList arrayList = null;
        while (true) {
            int type = parser.next();
            Keyframe keyframe;
            if (type == 3 || type == 1) {
                if (arrayList != null) {
                    int count = arrayList.size();
                    if (count > 0) {
                        Keyframe firstKeyframe = (Keyframe) arrayList.get(0);
                        Keyframe lastKeyframe = (Keyframe) arrayList.get(count - 1);
                        float endFraction = lastKeyframe.getFraction();
                        if (endFraction < 1.0f) {
                            if (endFraction < TonemapCurve.LEVEL_BLACK) {
                                lastKeyframe.setFraction(1.0f);
                            } else {
                                arrayList.add(arrayList.size(), createNewKeyframe(lastKeyframe, 1.0f));
                                count++;
                            }
                        }
                        float startFraction = firstKeyframe.getFraction();
                        if (startFraction != TonemapCurve.LEVEL_BLACK) {
                            if (startFraction < TonemapCurve.LEVEL_BLACK) {
                                firstKeyframe.setFraction(TonemapCurve.LEVEL_BLACK);
                            } else {
                                arrayList.add(0, createNewKeyframe(firstKeyframe, TonemapCurve.LEVEL_BLACK));
                                count++;
                            }
                        }
                        Keyframe[] keyframeArray = new Keyframe[count];
                        arrayList.toArray(keyframeArray);
                        for (int i = 0; i < count; i++) {
                            keyframe = keyframeArray[i];
                            if (keyframe.getFraction() < TonemapCurve.LEVEL_BLACK) {
                                if (i == 0) {
                                    keyframe.setFraction(TonemapCurve.LEVEL_BLACK);
                                } else if (i == count - 1) {
                                    keyframe.setFraction(1.0f);
                                } else {
                                    int startIndex = i;
                                    int endIndex = i;
                                    int j = i + 1;
                                    while (j < count - 1 && keyframeArray[j].getFraction() < TonemapCurve.LEVEL_BLACK) {
                                        endIndex = j;
                                        j++;
                                    }
                                    distributeKeyframes(keyframeArray, keyframeArray[endIndex + 1].getFraction() - keyframeArray[startIndex - 1].getFraction(), startIndex, endIndex);
                                }
                            }
                        }
                        value = PropertyValuesHolder.ofKeyframe(propertyName, keyframeArray);
                        if (valueType == 3) {
                            value.setEvaluator(ArgbEvaluator.getInstance());
                        }
                    }
                }
            } else if (parser.getName().equals("keyframe")) {
                if (valueType == 4) {
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
        }
        if (arrayList != null) {
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
        float increment = gap / ((float) ((endIndex - startIndex) + 2));
        for (int i = startIndex; i <= endIndex; i++) {
            keyframes[i].setFraction(keyframes[i - 1].getFraction() + increment);
        }
    }

    private static Keyframe loadKeyframe(Resources res, Theme theme, AttributeSet attrs, int valueType) throws XmlPullParserException, IOException {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Keyframe, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, com.android.internal.R.styleable.Keyframe);
        }
        Keyframe keyframe = null;
        float fraction = a.getFloat(3, -1.0f);
        TypedValue keyframeValue = a.peekValue(0);
        boolean hasValue = keyframeValue != null;
        if (valueType == 4) {
            if (hasValue && isColorType(keyframeValue.type)) {
                valueType = 3;
            } else {
                valueType = 0;
            }
        }
        if (hasValue) {
            switch (valueType) {
                case 0:
                    keyframe = Keyframe.ofFloat(fraction, a.getFloat(0, TonemapCurve.LEVEL_BLACK));
                    break;
                case 1:
                case 3:
                    keyframe = Keyframe.ofInt(fraction, a.getInt(0, 0));
                    break;
            }
        } else if (valueType == 0) {
            keyframe = Keyframe.ofFloat(fraction);
        } else {
            keyframe = Keyframe.ofInt(fraction);
        }
        int resID = a.getResourceId(1, 0);
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
            arrayAnimator = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Animator, 0, 0);
        } else {
            arrayAnimator = res.obtainAttributes(attrs, com.android.internal.R.styleable.Animator);
        }
        if (anim != null) {
            if (theme != null) {
                arrayObjectAnimator = theme.obtainStyledAttributes(attrs, com.android.internal.R.styleable.PropertyAnimator, 0, 0);
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
        int resID = arrayAnimator.getResourceId(0, 0);
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
        return type >= 28 && type <= 31;
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
                case 1:
                case 3:
                    return stateListAnimator;
                case 2:
                    Animator animator = null;
                    if (HwThemeManager.TAG_ITEM.equals(parser.getName())) {
                        int attributeCount = parser.getAttributeCount();
                        int[] states = new int[attributeCount];
                        int i = 0;
                        int stateIndex = 0;
                        while (i < attributeCount) {
                            int stateIndex2;
                            int attrName = attributeSet.getAttributeNameResource(i);
                            if (attrName == R.attr.animation) {
                                animator = loadAnimator(context, attributeSet.getAttributeResourceValue(i, 0));
                                stateIndex2 = stateIndex;
                            } else {
                                stateIndex2 = stateIndex + 1;
                                if (!attributeSet.getAttributeBooleanValue(i, false)) {
                                    attrName = -attrName;
                                }
                                states[stateIndex] = attrName;
                            }
                            i++;
                            stateIndex = stateIndex2;
                        }
                        if (animator == null) {
                            animator = createAnimatorFromXml(context.getResources(), context.getTheme(), parser, 1.0f);
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
