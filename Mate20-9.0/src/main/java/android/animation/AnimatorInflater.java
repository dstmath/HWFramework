package android.animation;

import android.content.Context;
import android.content.res.ConfigurationBoundResourceCache;
import android.content.res.ConstantState;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.PathParser;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.InflateException;
import android.view.animation.AnimationUtils;
import android.view.animation.BaseInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

    private static class PathDataEvaluator implements TypeEvaluator<PathParser.PathData> {
        private final PathParser.PathData mPathData;

        private PathDataEvaluator() {
            this.mPathData = new PathParser.PathData();
        }

        public PathParser.PathData evaluate(float fraction, PathParser.PathData startPathData, PathParser.PathData endPathData) {
            if (PathParser.interpolatePathData(this.mPathData, startPathData, endPathData, fraction)) {
                return this.mPathData;
            }
            throw new IllegalArgumentException("Can't interpolate between two incompatible pathData");
        }
    }

    public static Animator loadAnimator(Context context, int id) throws Resources.NotFoundException {
        return loadAnimator(context.getResources(), context.getTheme(), id);
    }

    public static Animator loadAnimator(Resources resources, Resources.Theme theme, int id) throws Resources.NotFoundException {
        return loadAnimator(resources, theme, id, 1.0f);
    }

    public static Animator loadAnimator(Resources resources, Resources.Theme theme, int id, float pathErrorScale) throws Resources.NotFoundException {
        ConfigurationBoundResourceCache<Animator> animatorCache = resources.getAnimatorCache();
        Animator animator = animatorCache.getInstance((long) id, resources, theme);
        if (animator != null) {
            return animator;
        }
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = resources.getAnimation(id);
            Animator animator2 = createAnimatorFromXml(resources, theme, parser2, pathErrorScale);
            if (animator2 != null) {
                animator2.appendChangingConfigurations(getChangingConfigs(resources, id));
                ConstantState<Animator> constantState = animator2.createConstantState();
                if (constantState != null) {
                    animatorCache.put((long) id, theme, constantState);
                    animator2 = constantState.newInstance(resources, theme);
                }
            }
            if (parser2 != null) {
                parser2.close();
            }
            return animator2;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    public static StateListAnimator loadStateListAnimator(Context context, int id) throws Resources.NotFoundException {
        Resources resources = context.getResources();
        ConfigurationBoundResourceCache<StateListAnimator> cache = resources.getStateListAnimatorCache();
        Resources.Theme theme = context.getTheme();
        StateListAnimator animator = cache.getInstance((long) id, resources, theme);
        if (animator != null) {
            return animator;
        }
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = resources.getAnimation(id);
            StateListAnimator animator2 = createStateListAnimatorFromXml(context, parser2, Xml.asAttributeSet(parser2));
            if (animator2 != null) {
                animator2.appendChangingConfigurations(getChangingConfigs(resources, id));
                ConstantState<StateListAnimator> constantState = animator2.createConstantState();
                if (constantState != null) {
                    cache.put((long) id, theme, constantState);
                    animator2 = constantState.newInstance(resources, theme);
                }
            }
            if (parser2 != null) {
                parser2.close();
            }
            return animator2;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load state list animator resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private static StateListAnimator createStateListAnimatorFromXml(Context context, XmlPullParser parser, AttributeSet attributeSet) throws IOException, XmlPullParserException {
        int i;
        StateListAnimator stateListAnimator = new StateListAnimator();
        while (true) {
            switch (parser.next()) {
                case 1:
                case 3:
                    return stateListAnimator;
                case 2:
                    if (HwThemeManager.TAG_ITEM.equals(parser.getName())) {
                        int attributeCount = parser.getAttributeCount();
                        int[] states = new int[attributeCount];
                        int stateIndex = 0;
                        Animator animator = null;
                        for (int i2 = 0; i2 < attributeCount; i2++) {
                            int attrName = attributeSet.getAttributeNameResource(i2);
                            if (attrName == 16843213) {
                                animator = loadAnimator(context, attributeSet.getAttributeResourceValue(i2, 0));
                            } else {
                                int stateIndex2 = stateIndex + 1;
                                if (attributeSet.getAttributeBooleanValue(i2, false)) {
                                    i = attrName;
                                } else {
                                    i = -attrName;
                                }
                                states[stateIndex] = i;
                                stateIndex = stateIndex2;
                            }
                        }
                        if (animator == null) {
                            animator = createAnimatorFromXml(context.getResources(), context.getTheme(), parser, 1.0f);
                        }
                        if (animator != null) {
                            stateListAnimator.addState(StateSet.trimStateSet(states, stateIndex), animator);
                            break;
                        } else {
                            throw new Resources.NotFoundException("animation state item must have a valid animation");
                        }
                    } else {
                        continue;
                    }
            }
        }
    }

    private static PropertyValuesHolder getPVH(TypedArray styledAttributes, int valueType, int valueFromId, int valueToId, String propertyName) {
        int valueType2;
        PropertyValuesHolder returnValue;
        PropertyValuesHolder returnValue2;
        int valueTo;
        int valueTo2;
        int valueFrom;
        int valueTo3;
        int valueTo4;
        PropertyValuesHolder propertyValuesHolder;
        float valueTo5;
        float valueFrom2;
        float valueTo6;
        TypedValue tvFrom;
        int toType;
        PropertyValuesHolder propertyValuesHolder2;
        TypedArray typedArray = styledAttributes;
        int i = valueFromId;
        int i2 = valueToId;
        String str = propertyName;
        TypedValue tvFrom2 = typedArray.peekValue(i);
        boolean hasFrom = tvFrom2 != null;
        int fromType = hasFrom ? tvFrom2.type : 0;
        TypedValue tvTo = typedArray.peekValue(i2);
        boolean hasTo = tvTo != null;
        int toType2 = hasTo ? tvTo.type : 0;
        int i3 = valueType;
        if (i3 != 4) {
            valueType2 = i3;
        } else if ((!hasFrom || !isColorType(fromType)) && (!hasTo || !isColorType(toType2))) {
            valueType2 = 0;
        } else {
            valueType2 = 3;
        }
        boolean getFloats = valueType2 == 0;
        if (valueType2 == 2) {
            String fromString = typedArray.getString(i);
            String toString = typedArray.getString(i2);
            PathParser.PathData nodesFrom = fromString == null ? null : new PathParser.PathData(fromString);
            if (toString == null) {
                TypedValue typedValue = tvFrom2;
                tvFrom = null;
            } else {
                TypedValue typedValue2 = tvFrom2;
                tvFrom = new PathParser.PathData(toString);
            }
            if (nodesFrom == null && tvFrom == null) {
                TypedValue typedValue3 = tvTo;
                toType = toType2;
                propertyValuesHolder2 = null;
            } else {
                if (nodesFrom != null) {
                    TypedValue typedValue4 = tvTo;
                    TypeEvaluator evaluator = new PathDataEvaluator();
                    if (tvFrom == null) {
                        toType = toType2;
                        returnValue = PropertyValuesHolder.ofObject(str, evaluator, nodesFrom);
                    } else if (PathParser.canMorph(nodesFrom, tvFrom)) {
                        returnValue = PropertyValuesHolder.ofObject(str, evaluator, nodesFrom, tvFrom);
                        toType = toType2;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        int i4 = toType2;
                        sb.append(" Can't morph from ");
                        sb.append(fromString);
                        sb.append(" to ");
                        sb.append(toString);
                        throw new InflateException(sb.toString());
                    }
                } else {
                    toType = toType2;
                    propertyValuesHolder2 = null;
                    if (tvFrom != null) {
                        returnValue = PropertyValuesHolder.ofObject(str, new PathDataEvaluator(), tvFrom);
                    }
                }
                int i5 = toType;
                int i6 = valueToId;
            }
            returnValue = propertyValuesHolder2;
            int i52 = toType;
            int i62 = valueToId;
        } else {
            TypedValue typedValue5 = tvTo;
            int toType3 = toType2;
            TypeEvaluator evaluator2 = null;
            if (valueType2 == 3) {
                evaluator2 = ArgbEvaluator.getInstance();
            }
            if (getFloats) {
                if (hasFrom) {
                    if (fromType == 5) {
                        valueFrom2 = typedArray.getDimension(i, 0.0f);
                    } else {
                        valueFrom2 = typedArray.getFloat(i, 0.0f);
                    }
                    if (hasTo) {
                        if (toType3 == 5) {
                            valueTo6 = typedArray.getDimension(valueToId, 0.0f);
                        } else {
                            valueTo6 = typedArray.getFloat(valueToId, 0.0f);
                        }
                        returnValue2 = PropertyValuesHolder.ofFloat(str, valueFrom2, valueTo6);
                    } else {
                        int i7 = valueToId;
                        propertyValuesHolder = PropertyValuesHolder.ofFloat(str, valueFrom2);
                    }
                } else {
                    int i8 = valueToId;
                    if (toType3 == 5) {
                        valueTo5 = typedArray.getDimension(i8, 0.0f);
                    } else {
                        valueTo5 = typedArray.getFloat(i8, 0.0f);
                    }
                    propertyValuesHolder = PropertyValuesHolder.ofFloat(str, valueTo5);
                }
                returnValue2 = propertyValuesHolder;
            } else {
                int toType4 = toType3;
                int i9 = valueToId;
                if (hasFrom) {
                    if (fromType == 5) {
                        valueFrom = (int) typedArray.getDimension(i, 0.0f);
                    } else if (isColorType(fromType) != 0) {
                        valueFrom = typedArray.getColor(i, 0);
                    } else {
                        valueFrom = typedArray.getInt(i, 0);
                    }
                    int valueFrom3 = valueFrom;
                    if (hasTo) {
                        if (toType4 == 5) {
                            valueTo3 = (int) typedArray.getDimension(i9, 0.0f);
                            valueTo4 = 0;
                        } else if (isColorType(toType4) != 0) {
                            valueTo4 = 0;
                            valueTo3 = typedArray.getColor(i9, 0);
                        } else {
                            valueTo4 = 0;
                            valueTo3 = typedArray.getInt(i9, 0);
                        }
                        int[] iArr = new int[2];
                        iArr[valueTo4] = valueFrom3;
                        iArr[1] = valueTo3;
                        returnValue2 = PropertyValuesHolder.ofInt(str, iArr);
                    } else {
                        returnValue2 = PropertyValuesHolder.ofInt(str, valueFrom3);
                    }
                } else if (hasTo) {
                    if (toType4 == 5) {
                        valueTo = (int) typedArray.getDimension(i9, 0.0f);
                        valueTo2 = 0;
                    } else if (isColorType(toType4) != 0) {
                        valueTo2 = 0;
                        valueTo = typedArray.getColor(i9, 0);
                    } else {
                        valueTo2 = 0;
                        valueTo = typedArray.getInt(i9, 0);
                    }
                    int[] iArr2 = new int[1];
                    iArr2[valueTo2] = valueTo;
                    returnValue2 = PropertyValuesHolder.ofInt(str, iArr2);
                } else {
                    returnValue2 = null;
                }
            }
            if (!(returnValue == null || evaluator2 == null)) {
                returnValue.setEvaluator(evaluator2);
            }
        }
        return returnValue;
    }

    private static void parseAnimatorFromTypeArray(ValueAnimator anim, TypedArray arrayAnimator, TypedArray arrayObjectAnimator, float pixelSize) {
        long duration = (long) arrayAnimator.getInt(1, 300);
        long startDelay = (long) arrayAnimator.getInt(2, 0);
        int valueType = arrayAnimator.getInt(7, 4);
        if (valueType == 4) {
            valueType = inferValueTypeFromValues(arrayAnimator, 5, 6);
        }
        PropertyValuesHolder pvh = getPVH(arrayAnimator, valueType, 5, 6, "");
        if (pvh != null) {
            anim.setValues(pvh);
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
        PathParser.PathData pathDataFrom = fromString == null ? null : new PathParser.PathData(fromString);
        PathParser.PathData pathDataTo = toString == null ? null : new PathParser.PathData(toString);
        if (pathDataFrom != null) {
            if (pathDataTo != null) {
                anim.setObjectValues(pathDataFrom, pathDataTo);
                if (!PathParser.canMorph(pathDataFrom, pathDataTo)) {
                    throw new InflateException(arrayAnimator.getPositionDescription() + " Can't morph from " + fromString + " to " + toString);
                }
            } else {
                anim.setObjectValues(pathDataFrom);
            }
            return new PathDataEvaluator();
        } else if (pathDataTo == null) {
            return null;
        } else {
            anim.setObjectValues(pathDataTo);
            return new PathDataEvaluator();
        }
    }

    private static void setupObjectAnimator(ValueAnimator anim, TypedArray arrayObjectAnimator, int valueType, float pixelSize) {
        Keyframes yKeyframes;
        Keyframes xKeyframes;
        TypedArray typedArray = arrayObjectAnimator;
        int valueType2 = valueType;
        ObjectAnimator oa = (ObjectAnimator) anim;
        String pathData = typedArray.getString(1);
        if (pathData != null) {
            String propertyXName = typedArray.getString(2);
            String propertyYName = typedArray.getString(3);
            if (valueType2 == 2 || valueType2 == 4) {
                valueType2 = 0;
            }
            if (propertyXName == null && propertyYName == null) {
                throw new InflateException(arrayObjectAnimator.getPositionDescription() + " propertyXName or propertyYName is needed for PathData");
            }
            PathKeyframes keyframeSet = KeyframeSet.ofPath(PathParser.createPathFromPathData(pathData), 0.5f * pixelSize);
            if (valueType2 == 0) {
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
            } else if (y == null) {
                oa.setValues(x);
            } else {
                oa.setValues(x, y);
            }
        } else {
            oa.setPropertyName(typedArray.getString(0));
        }
    }

    private static void setupValues(ValueAnimator anim, TypedArray arrayAnimator, boolean getFloats, boolean hasFrom, int fromType, boolean hasTo, int toType) {
        int valueTo;
        int valueFrom;
        int valueTo2;
        float valueTo3;
        float valueFrom2;
        float valueTo4;
        if (getFloats) {
            if (hasFrom) {
                if (fromType == 5) {
                    valueFrom2 = arrayAnimator.getDimension(5, 0.0f);
                } else {
                    valueFrom2 = arrayAnimator.getFloat(5, 0.0f);
                }
                if (hasTo) {
                    if (toType == 5) {
                        valueTo4 = arrayAnimator.getDimension(6, 0.0f);
                    } else {
                        valueTo4 = arrayAnimator.getFloat(6, 0.0f);
                    }
                    anim.setFloatValues(valueFrom2, valueTo4);
                    return;
                }
                anim.setFloatValues(valueFrom2);
                return;
            }
            if (toType == 5) {
                valueTo3 = arrayAnimator.getDimension(6, 0.0f);
            } else {
                valueTo3 = arrayAnimator.getFloat(6, 0.0f);
            }
            anim.setFloatValues(valueTo3);
        } else if (hasFrom) {
            if (fromType == 5) {
                valueFrom = (int) arrayAnimator.getDimension(5, 0.0f);
            } else if (isColorType(fromType) != 0) {
                valueFrom = arrayAnimator.getColor(5, 0);
            } else {
                valueFrom = arrayAnimator.getInt(5, 0);
            }
            if (hasTo) {
                if (toType == 5) {
                    valueTo2 = (int) arrayAnimator.getDimension(6, 0.0f);
                } else if (isColorType(toType) != 0) {
                    valueTo2 = arrayAnimator.getColor(6, 0);
                } else {
                    valueTo2 = arrayAnimator.getInt(6, 0);
                }
                anim.setIntValues(valueFrom, valueTo2);
                return;
            }
            anim.setIntValues(valueFrom);
        } else if (hasTo) {
            if (toType == 5) {
                valueTo = (int) arrayAnimator.getDimension(6, 0.0f);
            } else if (isColorType(toType) != 0) {
                valueTo = arrayAnimator.getColor(6, 0);
            } else {
                valueTo = arrayAnimator.getInt(6, 0);
            }
            anim.setIntValues(valueTo);
        }
    }

    private static Animator createAnimatorFromXml(Resources res, Resources.Theme theme, XmlPullParser parser, float pixelSize) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(res, theme, parser, Xml.asAttributeSet(parser), null, 0, pixelSize);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c1  */
    private static Animator createAnimatorFromXml(Resources res, Resources.Theme theme, XmlPullParser parser, AttributeSet attrs, AnimatorSet parent, int sequenceOrdering, float pixelSize) throws XmlPullParserException, IOException {
        TypedArray a;
        Resources resources = res;
        Resources.Theme theme2 = theme;
        AttributeSet attributeSet = attrs;
        AnimatorSet animatorSet = parent;
        float f = pixelSize;
        Animator anim = null;
        int depth = parser.getDepth();
        ArrayList<Animator> childAnims = null;
        while (true) {
            int depth2 = depth;
            int next = parser.next();
            int type = next;
            if ((next != 3 || parser.getDepth() > depth2) && type != 1) {
                if (type != 2) {
                    depth = depth2;
                } else {
                    String name = parser.getName();
                    boolean gotValues = false;
                    if (name.equals("objectAnimator")) {
                        anim = loadObjectAnimator(resources, theme2, attributeSet, f);
                    } else if (name.equals("animator")) {
                        anim = loadAnimator(resources, theme2, attributeSet, null, f);
                    } else {
                        if (name.equals("set")) {
                            Animator anim2 = new AnimatorSet();
                            if (theme2 != null) {
                                a = theme2.obtainStyledAttributes(attributeSet, R.styleable.AnimatorSet, 0, 0);
                            } else {
                                a = resources.obtainAttributes(attributeSet, R.styleable.AnimatorSet);
                            }
                            TypedArray a2 = a;
                            anim2.appendChangingConfigurations(a2.getChangingConfigurations());
                            createAnimatorFromXml(resources, theme2, parser, attributeSet, (AnimatorSet) anim2, a2.getInt(0, 0), f);
                            a2.recycle();
                            anim = anim2;
                        } else if (name.equals("propertyValuesHolder")) {
                            PropertyValuesHolder[] values = loadValues(resources, theme2, parser, Xml.asAttributeSet(parser));
                            if (!(values == null || anim == null || !(anim instanceof ValueAnimator))) {
                                ((ValueAnimator) anim).setValues(values);
                            }
                            gotValues = true;
                        } else {
                            XmlPullParser xmlPullParser = parser;
                            throw new RuntimeException("Unknown animator name: " + parser.getName());
                        }
                        if (animatorSet != null && !gotValues) {
                            if (childAnims == null) {
                                childAnims = new ArrayList<>();
                            }
                            childAnims.add(anim);
                        }
                        depth = depth2;
                        attributeSet = attrs;
                    }
                    XmlPullParser xmlPullParser2 = parser;
                    if (childAnims == null) {
                    }
                    childAnims.add(anim);
                    depth = depth2;
                    attributeSet = attrs;
                }
            }
        }
        XmlPullParser xmlPullParser3 = parser;
        if (!(animatorSet == null || childAnims == null)) {
            Animator[] animsArray = new Animator[childAnims.size()];
            int index = 0;
            Iterator<Animator> it = childAnims.iterator();
            while (it.hasNext()) {
                animsArray[index] = it.next();
                index++;
            }
            if (sequenceOrdering == 0) {
                animatorSet.playTogether(animsArray);
            } else {
                animatorSet.playSequentially(animsArray);
            }
        }
        return anim;
    }

    private static PropertyValuesHolder[] loadValues(Resources res, Resources.Theme theme, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        int i;
        TypedArray a;
        ArrayList<PropertyValuesHolder> values = null;
        while (true) {
            int eventType = parser.getEventType();
            int type = eventType;
            if (eventType == 3 || type == 1) {
                PropertyValuesHolder[] valuesArray = null;
            } else if (type != 2) {
                parser.next();
            } else {
                if (parser.getName().equals("propertyValuesHolder")) {
                    if (theme != null) {
                        a = theme.obtainStyledAttributes(attrs, R.styleable.PropertyValuesHolder, 0, 0);
                    } else {
                        a = res.obtainAttributes(attrs, R.styleable.PropertyValuesHolder);
                    }
                    String propertyName = a.getString(3);
                    int valueType = a.getInt(2, 4);
                    PropertyValuesHolder pvh = loadPvh(res, theme, parser, propertyName, valueType);
                    if (pvh == null) {
                        pvh = getPVH(a, valueType, 0, 1, propertyName);
                    }
                    if (pvh != null) {
                        if (values == null) {
                            values = new ArrayList<>();
                        }
                        values.add(pvh);
                    }
                    a.recycle();
                }
                parser.next();
            }
        }
        PropertyValuesHolder[] valuesArray2 = null;
        if (values != null) {
            int count = values.size();
            valuesArray2 = new PropertyValuesHolder[count];
            for (i = 0; i < count; i++) {
                valuesArray2[i] = values.get(i);
            }
        }
        return valuesArray2;
    }

    private static int inferValueTypeOfKeyframe(Resources res, Resources.Theme theme, AttributeSet attrs) {
        TypedArray a;
        int valueType = 0;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.Keyframe, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.Keyframe);
        }
        TypedValue keyframeValue = a.peekValue(0);
        if ((keyframeValue != null) && isColorType(keyframeValue.type)) {
            valueType = 3;
        }
        a.recycle();
        return valueType;
    }

    private static int inferValueTypeFromValues(TypedArray styledAttributes, int valueFromId, int valueToId) {
        TypedValue tvFrom = styledAttributes.peekValue(valueFromId);
        boolean hasTo = true;
        boolean hasFrom = tvFrom != null;
        int fromType = hasFrom ? tvFrom.type : 0;
        TypedValue tvTo = styledAttributes.peekValue(valueToId);
        if (tvTo == null) {
            hasTo = false;
        }
        int toType = hasTo ? tvTo.type : 0;
        if ((!hasFrom || !isColorType(fromType)) && (!hasTo || !isColorType(toType))) {
            return 0;
        }
        return 3;
    }

    private static void dumpKeyframes(Object[] keyframes, String header) {
        if (keyframes != null && keyframes.length != 0) {
            Log.d(TAG, header);
            int count = keyframes.length;
            for (int i = 0; i < count; i++) {
                Keyframe keyframe = keyframes[i];
                StringBuilder sb = new StringBuilder();
                sb.append("Keyframe ");
                sb.append(i);
                sb.append(": fraction ");
                sb.append(keyframe.getFraction() < 0.0f ? "null" : Float.valueOf(keyframe.getFraction()));
                sb.append(", , value : ");
                sb.append(keyframe.hasValue() ? keyframe.getValue() : "null");
                Log.d(TAG, sb.toString());
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0046  */
    private static PropertyValuesHolder loadPvh(Resources res, Resources.Theme theme, XmlPullParser parser, String propertyName, int valueType) throws XmlPullParserException, IOException {
        float f;
        Resources resources = res;
        Resources.Theme theme2 = theme;
        PropertyValuesHolder value = null;
        ArrayList<Keyframe> keyframes = null;
        int valueType2 = valueType;
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 3 || type == 1) {
                if (keyframes != null) {
                    int size = keyframes.size();
                    int count = size;
                    if (size > 0) {
                        int i = 0;
                        Keyframe firstKeyframe = keyframes.get(0);
                        Keyframe lastKeyframe = keyframes.get(count - 1);
                        float endFraction = lastKeyframe.getFraction();
                        float f2 = 1.0f;
                        float f3 = 0.0f;
                        if (endFraction < 1.0f) {
                            if (endFraction < 0.0f) {
                                lastKeyframe.setFraction(1.0f);
                            } else {
                                keyframes.add(keyframes.size(), createNewKeyframe(lastKeyframe, 1.0f));
                                count++;
                            }
                        }
                        float startFraction = firstKeyframe.getFraction();
                        if (startFraction != 0.0f) {
                            if (startFraction < 0.0f) {
                                firstKeyframe.setFraction(0.0f);
                            } else {
                                keyframes.add(0, createNewKeyframe(firstKeyframe, 0.0f));
                                count++;
                            }
                        }
                        Keyframe[] keyframeArray = new Keyframe[count];
                        keyframes.toArray(keyframeArray);
                        while (i < count) {
                            Keyframe keyframe = keyframeArray[i];
                            if (keyframe.getFraction() < f3) {
                                if (i == 0) {
                                    keyframe.setFraction(f3);
                                } else {
                                    if (i == count - 1) {
                                        keyframe.setFraction(f2);
                                        f = 0.0f;
                                    } else {
                                        int startIndex = i;
                                        int j = startIndex + 1;
                                        int endIndex = i;
                                        while (true) {
                                            int j2 = j;
                                            if (j2 >= count - 1) {
                                                f = 0.0f;
                                                break;
                                            }
                                            f = 0.0f;
                                            if (keyframeArray[j2].getFraction() >= 0.0f) {
                                                break;
                                            }
                                            endIndex = j2;
                                            j = j2 + 1;
                                            Resources resources2 = res;
                                            Resources.Theme theme3 = theme;
                                        }
                                        distributeKeyframes(keyframeArray, keyframeArray[endIndex + 1].getFraction() - keyframeArray[startIndex - 1].getFraction(), startIndex, endIndex);
                                    }
                                    i++;
                                    f3 = f;
                                    Resources resources3 = res;
                                    Resources.Theme theme4 = theme;
                                    f2 = 1.0f;
                                }
                            }
                            f = f3;
                            i++;
                            f3 = f;
                            Resources resources32 = res;
                            Resources.Theme theme42 = theme;
                            f2 = 1.0f;
                        }
                        value = PropertyValuesHolder.ofKeyframe(propertyName, keyframeArray);
                        if (valueType2 == 3) {
                            value.setEvaluator(ArgbEvaluator.getInstance());
                        }
                        return value;
                    }
                }
            } else if (parser.getName().equals("keyframe")) {
                if (valueType2 == 4) {
                    valueType2 = inferValueTypeOfKeyframe(resources, theme2, Xml.asAttributeSet(parser));
                }
                Keyframe keyframe2 = loadKeyframe(resources, theme2, Xml.asAttributeSet(parser), valueType2);
                if (keyframe2 != null) {
                    if (keyframes == null) {
                        keyframes = new ArrayList<>();
                    }
                    keyframes.add(keyframe2);
                }
                parser.next();
            }
        }
        if (keyframes != null) {
        }
        String str = propertyName;
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

    private static Keyframe loadKeyframe(Resources res, Resources.Theme theme, AttributeSet attrs, int valueType) throws XmlPullParserException, IOException {
        TypedArray a;
        Keyframe keyframe;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.Keyframe, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.Keyframe);
        }
        Keyframe keyframe2 = null;
        float fraction = a.getFloat(3, -1.0f);
        TypedValue keyframeValue = a.peekValue(0);
        boolean hasValue = keyframeValue != null;
        if (valueType == 4) {
            if (!hasValue || !isColorType(keyframeValue.type)) {
                valueType = 0;
            } else {
                valueType = 3;
            }
        }
        if (hasValue) {
            if (valueType != 3) {
                switch (valueType) {
                    case 0:
                        keyframe2 = Keyframe.ofFloat(fraction, a.getFloat(0, 0.0f));
                        break;
                    case 1:
                        break;
                }
            }
            keyframe2 = Keyframe.ofInt(fraction, a.getInt(0, 0));
        } else {
            if (valueType == 0) {
                keyframe = Keyframe.ofFloat(fraction);
            } else {
                keyframe = Keyframe.ofInt(fraction);
            }
            keyframe2 = keyframe;
        }
        int resID = a.getResourceId(1, 0);
        if (resID > 0) {
            keyframe2.setInterpolator(AnimationUtils.loadInterpolator(res, theme, resID));
        }
        a.recycle();
        return keyframe2;
    }

    private static ObjectAnimator loadObjectAnimator(Resources res, Resources.Theme theme, AttributeSet attrs, float pathErrorScale) throws Resources.NotFoundException {
        ObjectAnimator anim = new ObjectAnimator();
        loadAnimator(res, theme, attrs, anim, pathErrorScale);
        return anim;
    }

    private static ValueAnimator loadAnimator(Resources res, Resources.Theme theme, AttributeSet attrs, ValueAnimator anim, float pathErrorScale) throws Resources.NotFoundException {
        TypedArray arrayAnimator;
        TypedArray arrayObjectAnimator = null;
        if (theme != null) {
            arrayAnimator = theme.obtainStyledAttributes(attrs, R.styleable.Animator, 0, 0);
        } else {
            arrayAnimator = res.obtainAttributes(attrs, R.styleable.Animator);
        }
        if (anim != null) {
            if (theme != null) {
                arrayObjectAnimator = theme.obtainStyledAttributes(attrs, R.styleable.PropertyAnimator, 0, 0);
            } else {
                arrayObjectAnimator = res.obtainAttributes(attrs, R.styleable.PropertyAnimator);
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
}
