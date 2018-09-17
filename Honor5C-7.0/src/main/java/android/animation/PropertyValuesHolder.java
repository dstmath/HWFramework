package android.animation;

import android.animation.Keyframes.FloatKeyframes;
import android.animation.Keyframes.IntKeyframes;
import android.graphics.Path;
import android.graphics.PointF;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Log;
import android.util.PathParser.PathData;
import android.util.Property;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class PropertyValuesHolder implements Cloneable {
    private static Class[] DOUBLE_VARIANTS;
    private static Class[] FLOAT_VARIANTS;
    private static Class[] INTEGER_VARIANTS;
    private static final TypeEvaluator sFloatEvaluator = null;
    private static final HashMap<Class, HashMap<String, Method>> sGetterPropertyMap = null;
    private static final TypeEvaluator sIntEvaluator = null;
    private static final HashMap<Class, HashMap<String, Method>> sSetterPropertyMap = null;
    private Object mAnimatedValue;
    private TypeConverter mConverter;
    private TypeEvaluator mEvaluator;
    private Method mGetter;
    Keyframes mKeyframes;
    protected Property mProperty;
    String mPropertyName;
    Method mSetter;
    final Object[] mTmpValueArray;
    Class mValueType;

    static class FloatPropertyValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = null;
        float mFloatAnimatedValue;
        FloatKeyframes mFloatKeyframes;
        private FloatProperty mFloatProperty;
        long mJniSetter;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PropertyValuesHolder.FloatPropertyValuesHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PropertyValuesHolder.FloatPropertyValuesHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.animation.PropertyValuesHolder.FloatPropertyValuesHolder.<clinit>():void");
        }

        public FloatPropertyValuesHolder(String propertyName, FloatKeyframes keyframes) {
            super(propertyName, null);
            this.mValueType = Float.TYPE;
            this.mKeyframes = keyframes;
            this.mFloatKeyframes = keyframes;
        }

        public FloatPropertyValuesHolder(Property property, FloatKeyframes keyframes) {
            super(property, null);
            this.mValueType = Float.TYPE;
            this.mKeyframes = keyframes;
            this.mFloatKeyframes = keyframes;
            if (property instanceof FloatProperty) {
                this.mFloatProperty = (FloatProperty) this.mProperty;
            }
        }

        public FloatPropertyValuesHolder(String propertyName, float... values) {
            super(propertyName, null);
            setFloatValues(values);
        }

        public FloatPropertyValuesHolder(Property property, float... values) {
            super(property, null);
            setFloatValues(values);
            if (property instanceof FloatProperty) {
                this.mFloatProperty = (FloatProperty) this.mProperty;
            }
        }

        public void setFloatValues(float... values) {
            super.setFloatValues(values);
            this.mFloatKeyframes = (FloatKeyframes) this.mKeyframes;
        }

        void calculateValue(float fraction) {
            this.mFloatAnimatedValue = this.mFloatKeyframes.getFloatValue(fraction);
        }

        Object getAnimatedValue() {
            return Float.valueOf(this.mFloatAnimatedValue);
        }

        public FloatPropertyValuesHolder clone() {
            FloatPropertyValuesHolder newPVH = (FloatPropertyValuesHolder) super.clone();
            newPVH.mFloatKeyframes = (FloatKeyframes) newPVH.mKeyframes;
            return newPVH;
        }

        void setAnimatedValue(Object target) {
            if (this.mFloatProperty != null) {
                this.mFloatProperty.setValue(target, this.mFloatAnimatedValue);
            } else if (this.mProperty != null) {
                this.mProperty.set(target, Float.valueOf(this.mFloatAnimatedValue));
            } else if (this.mJniSetter != 0) {
                PropertyValuesHolder.nCallFloatMethod(target, this.mJniSetter, this.mFloatAnimatedValue);
            } else {
                if (this.mSetter != null) {
                    try {
                        this.mTmpValueArray[0] = Float.valueOf(this.mFloatAnimatedValue);
                        this.mSetter.invoke(target, this.mTmpValueArray);
                    } catch (InvocationTargetException e) {
                        Log.e("PropertyValuesHolder", e.toString());
                    } catch (IllegalAccessException e2) {
                        Log.e("PropertyValuesHolder", e2.toString());
                    }
                }
            }
        }

        void setupSetter(Class targetClass) {
            if (this.mProperty == null) {
                synchronized (sJNISetterPropertyMap) {
                    HashMap<String, Long> propertyMap = (HashMap) sJNISetterPropertyMap.get(targetClass);
                    boolean wasInMap = false;
                    if (propertyMap != null) {
                        wasInMap = propertyMap.containsKey(this.mPropertyName);
                        if (wasInMap) {
                            Long jniSetter = (Long) propertyMap.get(this.mPropertyName);
                            if (jniSetter != null) {
                                this.mJniSetter = jniSetter.longValue();
                            }
                        }
                    }
                    if (!wasInMap) {
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetFloatMethod(targetClass, PropertyValuesHolder.getMethodName("set", this.mPropertyName));
                        } catch (NoSuchMethodError e) {
                        }
                        if (propertyMap == null) {
                            propertyMap = new HashMap();
                            sJNISetterPropertyMap.put(targetClass, propertyMap);
                        }
                        propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                    }
                }
                if (this.mJniSetter == 0) {
                    super.setupSetter(targetClass);
                }
            }
        }
    }

    static class IntPropertyValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = null;
        int mIntAnimatedValue;
        IntKeyframes mIntKeyframes;
        private IntProperty mIntProperty;
        long mJniSetter;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PropertyValuesHolder.IntPropertyValuesHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PropertyValuesHolder.IntPropertyValuesHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.animation.PropertyValuesHolder.IntPropertyValuesHolder.<clinit>():void");
        }

        public IntPropertyValuesHolder(String propertyName, IntKeyframes keyframes) {
            super(propertyName, null);
            this.mValueType = Integer.TYPE;
            this.mKeyframes = keyframes;
            this.mIntKeyframes = keyframes;
        }

        public IntPropertyValuesHolder(Property property, IntKeyframes keyframes) {
            super(property, null);
            this.mValueType = Integer.TYPE;
            this.mKeyframes = keyframes;
            this.mIntKeyframes = keyframes;
            if (property instanceof IntProperty) {
                this.mIntProperty = (IntProperty) this.mProperty;
            }
        }

        public IntPropertyValuesHolder(String propertyName, int... values) {
            super(propertyName, null);
            setIntValues(values);
        }

        public IntPropertyValuesHolder(Property property, int... values) {
            super(property, null);
            setIntValues(values);
            if (property instanceof IntProperty) {
                this.mIntProperty = (IntProperty) this.mProperty;
            }
        }

        public void setIntValues(int... values) {
            super.setIntValues(values);
            this.mIntKeyframes = (IntKeyframes) this.mKeyframes;
        }

        void calculateValue(float fraction) {
            this.mIntAnimatedValue = this.mIntKeyframes.getIntValue(fraction);
        }

        Object getAnimatedValue() {
            return Integer.valueOf(this.mIntAnimatedValue);
        }

        public /* bridge */ /* synthetic */ PropertyValuesHolder m31clone() {
            return clone();
        }

        public IntPropertyValuesHolder clone() {
            IntPropertyValuesHolder newPVH = (IntPropertyValuesHolder) super.clone();
            newPVH.mIntKeyframes = (IntKeyframes) newPVH.mKeyframes;
            return newPVH;
        }

        void setAnimatedValue(Object target) {
            if (this.mIntProperty != null) {
                this.mIntProperty.setValue(target, this.mIntAnimatedValue);
            } else if (this.mProperty != null) {
                this.mProperty.set(target, Integer.valueOf(this.mIntAnimatedValue));
            } else if (this.mJniSetter != 0) {
                PropertyValuesHolder.nCallIntMethod(target, this.mJniSetter, this.mIntAnimatedValue);
            } else {
                if (this.mSetter != null) {
                    try {
                        this.mTmpValueArray[0] = Integer.valueOf(this.mIntAnimatedValue);
                        this.mSetter.invoke(target, this.mTmpValueArray);
                    } catch (InvocationTargetException e) {
                        Log.e("PropertyValuesHolder", e.toString());
                    } catch (IllegalAccessException e2) {
                        Log.e("PropertyValuesHolder", e2.toString());
                    }
                }
            }
        }

        void setupSetter(Class targetClass) {
            if (this.mProperty == null) {
                synchronized (sJNISetterPropertyMap) {
                    HashMap<String, Long> propertyMap = (HashMap) sJNISetterPropertyMap.get(targetClass);
                    boolean wasInMap = false;
                    if (propertyMap != null) {
                        wasInMap = propertyMap.containsKey(this.mPropertyName);
                        if (wasInMap) {
                            Long jniSetter = (Long) propertyMap.get(this.mPropertyName);
                            if (jniSetter != null) {
                                this.mJniSetter = jniSetter.longValue();
                            }
                        }
                    }
                    if (!wasInMap) {
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetIntMethod(targetClass, PropertyValuesHolder.getMethodName("set", this.mPropertyName));
                        } catch (NoSuchMethodError e) {
                        }
                        if (propertyMap == null) {
                            propertyMap = new HashMap();
                            sJNISetterPropertyMap.put(targetClass, propertyMap);
                        }
                        propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                    }
                }
                if (this.mJniSetter == 0) {
                    super.setupSetter(targetClass);
                }
            }
        }
    }

    static class MultiFloatValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = null;
        private long mJniSetter;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PropertyValuesHolder.MultiFloatValuesHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PropertyValuesHolder.MultiFloatValuesHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.animation.PropertyValuesHolder.MultiFloatValuesHolder.<clinit>():void");
        }

        public MultiFloatValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Object... values) {
            super(propertyName, null);
            setConverter(converter);
            setObjectValues(values);
            setEvaluator(evaluator);
        }

        public MultiFloatValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Keyframes keyframes) {
            super(propertyName, null);
            setConverter(converter);
            this.mKeyframes = keyframes;
            setEvaluator(evaluator);
        }

        void setAnimatedValue(Object target) {
            float[] values = (float[]) getAnimatedValue();
            int numParameters = values.length;
            if (this.mJniSetter != 0) {
                switch (numParameters) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        PropertyValuesHolder.nCallFloatMethod(target, this.mJniSetter, values[0]);
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                        PropertyValuesHolder.nCallTwoFloatMethod(target, this.mJniSetter, values[0], values[1]);
                    case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        PropertyValuesHolder.nCallFourFloatMethod(target, this.mJniSetter, values[0], values[1], values[2], values[3]);
                    default:
                        PropertyValuesHolder.nCallMultipleFloatMethod(target, this.mJniSetter, values);
                }
            }
        }

        void setupSetterAndGetter(Object target) {
            setupSetter(target.getClass());
        }

        void setupSetter(Class targetClass) {
            if (this.mJniSetter == 0) {
                synchronized (sJNISetterPropertyMap) {
                    HashMap<String, Long> propertyMap = (HashMap) sJNISetterPropertyMap.get(targetClass);
                    boolean wasInMap = false;
                    if (propertyMap != null) {
                        wasInMap = propertyMap.containsKey(this.mPropertyName);
                        if (wasInMap) {
                            Long jniSetter = (Long) propertyMap.get(this.mPropertyName);
                            if (jniSetter != null) {
                                this.mJniSetter = jniSetter.longValue();
                            }
                        }
                    }
                    if (!wasInMap) {
                        String methodName = PropertyValuesHolder.getMethodName("set", this.mPropertyName);
                        calculateValue(0.0f);
                        int numParams = ((float[]) getAnimatedValue()).length;
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleFloatMethod(targetClass, methodName, numParams);
                        } catch (NoSuchMethodError e) {
                            try {
                                this.mJniSetter = PropertyValuesHolder.nGetMultipleFloatMethod(targetClass, this.mPropertyName, numParams);
                            } catch (NoSuchMethodError e2) {
                            }
                        }
                        if (propertyMap == null) {
                            propertyMap = new HashMap();
                            sJNISetterPropertyMap.put(targetClass, propertyMap);
                        }
                        propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                    }
                }
            }
        }
    }

    static class MultiIntValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = null;
        private long mJniSetter;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PropertyValuesHolder.MultiIntValuesHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PropertyValuesHolder.MultiIntValuesHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.animation.PropertyValuesHolder.MultiIntValuesHolder.<clinit>():void");
        }

        public MultiIntValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Object... values) {
            super(propertyName, null);
            setConverter(converter);
            setObjectValues(values);
            setEvaluator(evaluator);
        }

        public MultiIntValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Keyframes keyframes) {
            super(propertyName, null);
            setConverter(converter);
            this.mKeyframes = keyframes;
            setEvaluator(evaluator);
        }

        void setAnimatedValue(Object target) {
            int[] values = (int[]) getAnimatedValue();
            int numParameters = values.length;
            if (this.mJniSetter != 0) {
                switch (numParameters) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        PropertyValuesHolder.nCallIntMethod(target, this.mJniSetter, values[0]);
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                        PropertyValuesHolder.nCallTwoIntMethod(target, this.mJniSetter, values[0], values[1]);
                    case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        PropertyValuesHolder.nCallFourIntMethod(target, this.mJniSetter, values[0], values[1], values[2], values[3]);
                    default:
                        PropertyValuesHolder.nCallMultipleIntMethod(target, this.mJniSetter, values);
                }
            }
        }

        void setupSetterAndGetter(Object target) {
            setupSetter(target.getClass());
        }

        void setupSetter(Class targetClass) {
            if (this.mJniSetter == 0) {
                synchronized (sJNISetterPropertyMap) {
                    HashMap<String, Long> propertyMap = (HashMap) sJNISetterPropertyMap.get(targetClass);
                    boolean wasInMap = false;
                    if (propertyMap != null) {
                        wasInMap = propertyMap.containsKey(this.mPropertyName);
                        if (wasInMap) {
                            Long jniSetter = (Long) propertyMap.get(this.mPropertyName);
                            if (jniSetter != null) {
                                this.mJniSetter = jniSetter.longValue();
                            }
                        }
                    }
                    if (!wasInMap) {
                        String methodName = PropertyValuesHolder.getMethodName("set", this.mPropertyName);
                        calculateValue(0.0f);
                        int numParams = ((int[]) getAnimatedValue()).length;
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleIntMethod(targetClass, methodName, numParams);
                        } catch (NoSuchMethodError e) {
                            try {
                                this.mJniSetter = PropertyValuesHolder.nGetMultipleIntMethod(targetClass, this.mPropertyName, numParams);
                            } catch (NoSuchMethodError e2) {
                            }
                        }
                        if (propertyMap == null) {
                            propertyMap = new HashMap();
                            sJNISetterPropertyMap.put(targetClass, propertyMap);
                        }
                        propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                    }
                }
            }
        }
    }

    private static class PointFToFloatArray extends TypeConverter<PointF, float[]> {
        private float[] mCoordinates;

        public PointFToFloatArray() {
            super(PointF.class, float[].class);
            this.mCoordinates = new float[2];
        }

        public /* bridge */ /* synthetic */ Object convert(Object value) {
            return convert((PointF) value);
        }

        public float[] convert(PointF value) {
            this.mCoordinates[0] = value.x;
            this.mCoordinates[1] = value.y;
            return this.mCoordinates;
        }
    }

    private static class PointFToIntArray extends TypeConverter<PointF, int[]> {
        private int[] mCoordinates;

        public PointFToIntArray() {
            super(PointF.class, int[].class);
            this.mCoordinates = new int[2];
        }

        public /* bridge */ /* synthetic */ Object convert(Object value) {
            return convert((PointF) value);
        }

        public int[] convert(PointF value) {
            this.mCoordinates[0] = Math.round(value.x);
            this.mCoordinates[1] = Math.round(value.y);
            return this.mCoordinates;
        }
    }

    public static class PropertyValues {
        public DataSource dataSource;
        public Object endValue;
        public String propertyName;
        public Object startValue;
        public Class type;

        public interface DataSource {
            Object getValueAtFraction(float f);
        }

        public PropertyValues() {
            this.dataSource = null;
        }

        public String toString() {
            return "property name: " + this.propertyName + ", type: " + this.type + ", startValue: " + this.startValue.toString() + ", endValue: " + this.endValue.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PropertyValuesHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PropertyValuesHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.PropertyValuesHolder.<clinit>():void");
    }

    /* synthetic */ PropertyValuesHolder(Property property, PropertyValuesHolder propertyValuesHolder) {
        this(property);
    }

    /* synthetic */ PropertyValuesHolder(String propertyName, PropertyValuesHolder propertyValuesHolder) {
        this(propertyName);
    }

    private static native void nCallFloatMethod(Object obj, long j, float f);

    private static native void nCallFourFloatMethod(Object obj, long j, float f, float f2, float f3, float f4);

    private static native void nCallFourIntMethod(Object obj, long j, int i, int i2, int i3, int i4);

    private static native void nCallIntMethod(Object obj, long j, int i);

    private static native void nCallMultipleFloatMethod(Object obj, long j, float[] fArr);

    private static native void nCallMultipleIntMethod(Object obj, long j, int[] iArr);

    private static native void nCallTwoFloatMethod(Object obj, long j, float f, float f2);

    private static native void nCallTwoIntMethod(Object obj, long j, int i, int i2);

    private static native long nGetFloatMethod(Class cls, String str);

    private static native long nGetIntMethod(Class cls, String str);

    private static native long nGetMultipleFloatMethod(Class cls, String str, int i);

    private static native long nGetMultipleIntMethod(Class cls, String str, int i);

    private PropertyValuesHolder(String propertyName) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mTmpValueArray = new Object[1];
        this.mPropertyName = propertyName;
    }

    private PropertyValuesHolder(Property property) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mTmpValueArray = new Object[1];
        this.mProperty = property;
        if (property != null) {
            this.mPropertyName = property.getName();
        }
    }

    public static PropertyValuesHolder ofInt(String propertyName, int... values) {
        return new IntPropertyValuesHolder(propertyName, values);
    }

    public static PropertyValuesHolder ofInt(Property<?, Integer> property, int... values) {
        return new IntPropertyValuesHolder((Property) property, values);
    }

    public static PropertyValuesHolder ofMultiInt(String propertyName, int[][] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("At least 2 values must be supplied");
        }
        int numParameters = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("values must not be null");
            }
            int length = values[i].length;
            if (i == 0) {
                numParameters = length;
            } else if (length != numParameters) {
                throw new IllegalArgumentException("Values must all have the same length");
            }
        }
        return new MultiIntValuesHolder(propertyName, null, new IntArrayEvaluator(new int[numParameters]), (Object[]) values);
    }

    public static PropertyValuesHolder ofMultiInt(String propertyName, Path path) {
        return new MultiIntValuesHolder(propertyName, new PointFToIntArray(), null, KeyframeSet.ofPath(path));
    }

    @SafeVarargs
    public static <V> PropertyValuesHolder ofMultiInt(String propertyName, TypeConverter<V, int[]> converter, TypeEvaluator<V> evaluator, V... values) {
        return new MultiIntValuesHolder(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, (Object[]) values);
    }

    public static <T> PropertyValuesHolder ofMultiInt(String propertyName, TypeConverter<T, int[]> converter, TypeEvaluator<T> evaluator, Keyframe... values) {
        return new MultiIntValuesHolder(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofFloat(String propertyName, float... values) {
        return new FloatPropertyValuesHolder(propertyName, values);
    }

    public static PropertyValuesHolder ofFloat(Property<?, Float> property, float... values) {
        return new FloatPropertyValuesHolder((Property) property, values);
    }

    public static PropertyValuesHolder ofMultiFloat(String propertyName, float[][] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("At least 2 values must be supplied");
        }
        int numParameters = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("values must not be null");
            }
            int length = values[i].length;
            if (i == 0) {
                numParameters = length;
            } else if (length != numParameters) {
                throw new IllegalArgumentException("Values must all have the same length");
            }
        }
        return new MultiFloatValuesHolder(propertyName, null, new FloatArrayEvaluator(new float[numParameters]), (Object[]) values);
    }

    public static PropertyValuesHolder ofMultiFloat(String propertyName, Path path) {
        return new MultiFloatValuesHolder(propertyName, new PointFToFloatArray(), null, KeyframeSet.ofPath(path));
    }

    @SafeVarargs
    public static <V> PropertyValuesHolder ofMultiFloat(String propertyName, TypeConverter<V, float[]> converter, TypeEvaluator<V> evaluator, V... values) {
        return new MultiFloatValuesHolder(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, (Object[]) values);
    }

    public static <T> PropertyValuesHolder ofMultiFloat(String propertyName, TypeConverter<T, float[]> converter, TypeEvaluator<T> evaluator, Keyframe... values) {
        return new MultiFloatValuesHolder(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofObject(String propertyName, TypeEvaluator evaluator, Object... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    public static PropertyValuesHolder ofObject(String propertyName, TypeConverter<PointF, ?> converter, Path path) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.mKeyframes = KeyframeSet.ofPath(path);
        pvh.mValueType = PointF.class;
        pvh.setConverter(converter);
        return pvh;
    }

    @SafeVarargs
    public static <V> PropertyValuesHolder ofObject(Property property, TypeEvaluator<V> evaluator, V... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    @SafeVarargs
    public static <T, V> PropertyValuesHolder ofObject(Property<?, V> property, TypeConverter<T, V> converter, TypeEvaluator<T> evaluator, T... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder((Property) property);
        pvh.setConverter(converter);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    public static <V> PropertyValuesHolder ofObject(Property<?, V> property, TypeConverter<PointF, V> converter, Path path) {
        PropertyValuesHolder pvh = new PropertyValuesHolder((Property) property);
        pvh.mKeyframes = KeyframeSet.ofPath(path);
        pvh.mValueType = PointF.class;
        pvh.setConverter(converter);
        return pvh;
    }

    public static PropertyValuesHolder ofKeyframe(String propertyName, Keyframe... values) {
        return ofKeyframes(propertyName, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofKeyframe(Property property, Keyframe... values) {
        return ofKeyframes(property, KeyframeSet.ofKeyframe(values));
    }

    static PropertyValuesHolder ofKeyframes(String propertyName, Keyframes keyframes) {
        if (keyframes instanceof IntKeyframes) {
            return new IntPropertyValuesHolder(propertyName, (IntKeyframes) keyframes);
        }
        if (keyframes instanceof FloatKeyframes) {
            return new FloatPropertyValuesHolder(propertyName, (FloatKeyframes) keyframes);
        }
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.mKeyframes = keyframes;
        pvh.mValueType = keyframes.getType();
        return pvh;
    }

    static PropertyValuesHolder ofKeyframes(Property property, Keyframes keyframes) {
        if (keyframes instanceof IntKeyframes) {
            return new IntPropertyValuesHolder(property, (IntKeyframes) keyframes);
        }
        if (keyframes instanceof FloatKeyframes) {
            return new FloatPropertyValuesHolder(property, (FloatKeyframes) keyframes);
        }
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.mKeyframes = keyframes;
        pvh.mValueType = keyframes.getType();
        return pvh;
    }

    public void setIntValues(int... values) {
        this.mValueType = Integer.TYPE;
        this.mKeyframes = KeyframeSet.ofInt(values);
    }

    public void setFloatValues(float... values) {
        this.mValueType = Float.TYPE;
        this.mKeyframes = KeyframeSet.ofFloat(values);
    }

    public void setKeyframes(Keyframe... values) {
        int numKeyframes = values.length;
        Keyframe[] keyframes = new Keyframe[Math.max(numKeyframes, 2)];
        this.mValueType = values[0].getType();
        for (int i = 0; i < numKeyframes; i++) {
            keyframes[i] = values[i];
        }
        this.mKeyframes = new KeyframeSet(keyframes);
    }

    public void setObjectValues(Object... values) {
        this.mValueType = values[0].getClass();
        this.mKeyframes = KeyframeSet.ofObject(values);
        if (this.mEvaluator != null) {
            this.mKeyframes.setEvaluator(this.mEvaluator);
        }
    }

    public void setConverter(TypeConverter converter) {
        this.mConverter = converter;
    }

    private Method getPropertyFunction(Class targetClass, String prefix, Class valueType) {
        Method returnVal = null;
        String methodName = getMethodName(prefix, this.mPropertyName);
        if (valueType == null) {
            try {
                returnVal = targetClass.getMethod(methodName, null);
            } catch (NoSuchMethodException e) {
            }
        } else {
            Class[] typeVariants;
            Class[] args = new Class[1];
            if (valueType.equals(Float.class)) {
                typeVariants = FLOAT_VARIANTS;
            } else if (valueType.equals(Integer.class)) {
                typeVariants = INTEGER_VARIANTS;
            } else {
                typeVariants = valueType.equals(Double.class) ? DOUBLE_VARIANTS : new Class[]{valueType};
            }
            int length = typeVariants.length;
            int i = 0;
            while (i < length) {
                Class typeVariant = typeVariants[i];
                args[0] = typeVariant;
                try {
                    returnVal = targetClass.getMethod(methodName, args);
                    if (this.mConverter == null) {
                        this.mValueType = typeVariant;
                    }
                    return returnVal;
                } catch (NoSuchMethodException e2) {
                    i++;
                }
            }
        }
        if (returnVal == null) {
            Log.w("PropertyValuesHolder", "Method " + getMethodName(prefix, this.mPropertyName) + "() with type " + valueType + " not found on target class " + targetClass);
        }
        return returnVal;
    }

    private Method setupSetterOrGetter(Class targetClass, HashMap<Class, HashMap<String, Method>> propertyMapMap, String prefix, Class valueType) {
        Method setterOrGetter = null;
        synchronized (propertyMapMap) {
            HashMap<String, Method> propertyMap = (HashMap) propertyMapMap.get(targetClass);
            boolean wasInMap = false;
            if (propertyMap != null) {
                wasInMap = propertyMap.containsKey(this.mPropertyName);
                if (wasInMap) {
                    setterOrGetter = (Method) propertyMap.get(this.mPropertyName);
                }
            }
            if (!wasInMap) {
                setterOrGetter = getPropertyFunction(targetClass, prefix, valueType);
                if (propertyMap == null) {
                    propertyMap = new HashMap();
                    propertyMapMap.put(targetClass, propertyMap);
                }
                propertyMap.put(this.mPropertyName, setterOrGetter);
            }
        }
        return setterOrGetter;
    }

    void setupSetter(Class targetClass) {
        this.mSetter = setupSetterOrGetter(targetClass, sSetterPropertyMap, "set", this.mConverter == null ? this.mValueType : this.mConverter.getTargetType());
    }

    private void setupGetter(Class targetClass) {
        this.mGetter = setupSetterOrGetter(targetClass, sGetterPropertyMap, "get", null);
    }

    void setupSetterAndGetter(Object target) {
        List<Keyframe> keyframes;
        int keyframeCount;
        int i;
        Keyframe kf;
        this.mKeyframes.invalidateCache();
        if (this.mProperty != null) {
            Object testValue = null;
            try {
                keyframes = this.mKeyframes.getKeyframes();
                keyframeCount = keyframes == null ? 0 : keyframes.size();
                for (i = 0; i < keyframeCount; i++) {
                    kf = (Keyframe) keyframes.get(i);
                    if (!kf.hasValue() || kf.valueWasSetOnStart()) {
                        if (testValue == null) {
                            testValue = convertBack(this.mProperty.get(target));
                        }
                        kf.setValue(testValue);
                        kf.setValueWasSetOnStart(true);
                    }
                }
                return;
            } catch (ClassCastException e) {
                Log.w("PropertyValuesHolder", "No such property (" + this.mProperty.getName() + ") on target object " + target + ". Trying reflection instead");
                this.mProperty = null;
            }
        }
        if (this.mProperty == null) {
            Class targetClass = target.getClass();
            if (this.mSetter == null) {
                setupSetter(targetClass);
            }
            keyframes = this.mKeyframes.getKeyframes();
            keyframeCount = keyframes == null ? 0 : keyframes.size();
            for (i = 0; i < keyframeCount; i++) {
                kf = (Keyframe) keyframes.get(i);
                if (!kf.hasValue() || kf.valueWasSetOnStart()) {
                    if (this.mGetter == null) {
                        setupGetter(targetClass);
                        if (this.mGetter == null) {
                            return;
                        }
                    }
                    try {
                        kf.setValue(convertBack(this.mGetter.invoke(target, new Object[0])));
                        kf.setValueWasSetOnStart(true);
                    } catch (InvocationTargetException e2) {
                        Log.e("PropertyValuesHolder", e2.toString());
                    } catch (IllegalAccessException e3) {
                        Log.e("PropertyValuesHolder", e3.toString());
                    }
                }
            }
        }
    }

    private Object convertBack(Object value) {
        if (this.mConverter == null) {
            return value;
        }
        if (this.mConverter instanceof BidirectionalTypeConverter) {
            return ((BidirectionalTypeConverter) this.mConverter).convertBack(value);
        }
        throw new IllegalArgumentException("Converter " + this.mConverter.getClass().getName() + " must be a BidirectionalTypeConverter");
    }

    private void setupValue(Object target, Keyframe kf) {
        if (this.mProperty != null) {
            kf.setValue(convertBack(this.mProperty.get(target)));
        } else {
            try {
                if (this.mGetter == null) {
                    setupGetter(target.getClass());
                    if (this.mGetter == null) {
                        return;
                    }
                }
                kf.setValue(convertBack(this.mGetter.invoke(target, new Object[0])));
            } catch (InvocationTargetException e) {
                Log.e("PropertyValuesHolder", e.toString());
            } catch (IllegalAccessException e2) {
                Log.e("PropertyValuesHolder", e2.toString());
            }
        }
    }

    void setupStartValue(Object target) {
        List<Keyframe> keyframes = this.mKeyframes.getKeyframes();
        if (!keyframes.isEmpty()) {
            setupValue(target, (Keyframe) keyframes.get(0));
        }
    }

    void setupEndValue(Object target) {
        List<Keyframe> keyframes = this.mKeyframes.getKeyframes();
        if (!keyframes.isEmpty()) {
            setupValue(target, (Keyframe) keyframes.get(keyframes.size() - 1));
        }
    }

    public /* bridge */ /* synthetic */ Object m30clone() throws CloneNotSupportedException {
        return clone();
    }

    public PropertyValuesHolder clone() {
        try {
            PropertyValuesHolder newPVH = (PropertyValuesHolder) super.clone();
            newPVH.mPropertyName = this.mPropertyName;
            newPVH.mProperty = this.mProperty;
            newPVH.mKeyframes = this.mKeyframes.clone();
            newPVH.mEvaluator = this.mEvaluator;
            return newPVH;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    void setAnimatedValue(Object target) {
        if (this.mProperty != null) {
            this.mProperty.set(target, getAnimatedValue());
        }
        if (this.mSetter != null) {
            try {
                this.mTmpValueArray[0] = getAnimatedValue();
                this.mSetter.invoke(target, this.mTmpValueArray);
            } catch (InvocationTargetException e) {
                Log.e("PropertyValuesHolder", e.toString());
            } catch (IllegalAccessException e2) {
                Log.e("PropertyValuesHolder", e2.toString());
            }
        }
    }

    void init() {
        TypeEvaluator typeEvaluator = null;
        if (this.mEvaluator == null) {
            if (this.mValueType == Integer.class) {
                typeEvaluator = sIntEvaluator;
            } else if (this.mValueType == Float.class) {
                typeEvaluator = sFloatEvaluator;
            }
            this.mEvaluator = typeEvaluator;
        }
        if (this.mEvaluator != null) {
            this.mKeyframes.setEvaluator(this.mEvaluator);
        }
    }

    public void setEvaluator(TypeEvaluator evaluator) {
        this.mEvaluator = evaluator;
        this.mKeyframes.setEvaluator(evaluator);
    }

    void calculateValue(float fraction) {
        Object value = this.mKeyframes.getValue(fraction);
        if (this.mConverter != null) {
            value = this.mConverter.convert(value);
        }
        this.mAnimatedValue = value;
    }

    public void setPropertyName(String propertyName) {
        this.mPropertyName = propertyName;
    }

    public void setProperty(Property property) {
        this.mProperty = property;
    }

    public String getPropertyName() {
        return this.mPropertyName;
    }

    Object getAnimatedValue() {
        return this.mAnimatedValue;
    }

    public void getPropertyValues(PropertyValues values) {
        init();
        values.propertyName = this.mPropertyName;
        values.type = this.mValueType;
        values.startValue = this.mKeyframes.getValue(0.0f);
        if (values.startValue instanceof PathData) {
            values.startValue = new PathData((PathData) values.startValue);
        }
        values.endValue = this.mKeyframes.getValue(Engine.DEFAULT_VOLUME);
        if (values.endValue instanceof PathData) {
            values.endValue = new PathData((PathData) values.endValue);
        }
        if ((this.mKeyframes instanceof FloatKeyframesBase) || (this.mKeyframes instanceof IntKeyframesBase)) {
            values.dataSource = new DataSource() {
                public Object getValueAtFraction(float fraction) {
                    return PropertyValuesHolder.this.mKeyframes.getValue(fraction);
                }
            };
        } else {
            values.dataSource = null;
        }
    }

    public String toString() {
        return this.mPropertyName + ": " + this.mKeyframes.toString();
    }

    static String getMethodName(String prefix, String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            return prefix;
        }
        char firstLetter = Character.toUpperCase(propertyName.charAt(0));
        return prefix + firstLetter + propertyName.substring(1);
    }
}
