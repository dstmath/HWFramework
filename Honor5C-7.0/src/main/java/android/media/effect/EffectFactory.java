package android.media.effect;

public class EffectFactory {
    public static final String EFFECT_AUTOFIX = "android.media.effect.effects.AutoFixEffect";
    public static final String EFFECT_BACKDROPPER = "android.media.effect.effects.BackDropperEffect";
    public static final String EFFECT_BITMAPOVERLAY = "android.media.effect.effects.BitmapOverlayEffect";
    public static final String EFFECT_BLACKWHITE = "android.media.effect.effects.BlackWhiteEffect";
    public static final String EFFECT_BRIGHTNESS = "android.media.effect.effects.BrightnessEffect";
    public static final String EFFECT_CONTRAST = "android.media.effect.effects.ContrastEffect";
    public static final String EFFECT_CROP = "android.media.effect.effects.CropEffect";
    public static final String EFFECT_CROSSPROCESS = "android.media.effect.effects.CrossProcessEffect";
    public static final String EFFECT_DOCUMENTARY = "android.media.effect.effects.DocumentaryEffect";
    public static final String EFFECT_DUOTONE = "android.media.effect.effects.DuotoneEffect";
    public static final String EFFECT_FILLLIGHT = "android.media.effect.effects.FillLightEffect";
    public static final String EFFECT_FISHEYE = "android.media.effect.effects.FisheyeEffect";
    public static final String EFFECT_FLIP = "android.media.effect.effects.FlipEffect";
    public static final String EFFECT_GRAIN = "android.media.effect.effects.GrainEffect";
    public static final String EFFECT_GRAYSCALE = "android.media.effect.effects.GrayscaleEffect";
    public static final String EFFECT_IDENTITY = "IdentityEffect";
    public static final String EFFECT_LOMOISH = "android.media.effect.effects.LomoishEffect";
    public static final String EFFECT_NEGATIVE = "android.media.effect.effects.NegativeEffect";
    private static final String[] EFFECT_PACKAGES = null;
    public static final String EFFECT_POSTERIZE = "android.media.effect.effects.PosterizeEffect";
    public static final String EFFECT_REDEYE = "android.media.effect.effects.RedEyeEffect";
    public static final String EFFECT_ROTATE = "android.media.effect.effects.RotateEffect";
    public static final String EFFECT_SATURATE = "android.media.effect.effects.SaturateEffect";
    public static final String EFFECT_SEPIA = "android.media.effect.effects.SepiaEffect";
    public static final String EFFECT_SHARPEN = "android.media.effect.effects.SharpenEffect";
    public static final String EFFECT_STRAIGHTEN = "android.media.effect.effects.StraightenEffect";
    public static final String EFFECT_TEMPERATURE = "android.media.effect.effects.ColorTemperatureEffect";
    public static final String EFFECT_TINT = "android.media.effect.effects.TintEffect";
    public static final String EFFECT_VIGNETTE = "android.media.effect.effects.VignetteEffect";
    private EffectContext mEffectContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.effect.EffectFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.effect.EffectFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.effect.EffectFactory.<clinit>():void");
    }

    EffectFactory(EffectContext effectContext) {
        this.mEffectContext = effectContext;
    }

    public Effect createEffect(String effectName) {
        Class effectClass = getEffectClassByName(effectName);
        if (effectClass != null) {
            return instantiateEffect(effectClass, effectName);
        }
        throw new IllegalArgumentException("Cannot instantiate unknown effect '" + effectName + "'!");
    }

    public static boolean isEffectSupported(String effectName) {
        return getEffectClassByName(effectName) != null;
    }

    private static Class getEffectClassByName(String className) {
        Class effectClass = null;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        String[] strArr = EFFECT_PACKAGES;
        int i = 0;
        int length = strArr.length;
        while (i < length) {
            try {
                effectClass = contextClassLoader.loadClass(strArr[i] + className);
                if (effectClass != null) {
                    break;
                }
                i++;
            } catch (ClassNotFoundException e) {
            }
        }
        return effectClass;
    }

    private Effect instantiateEffect(Class effectClass, String name) {
        try {
            effectClass.asSubclass(Effect.class);
            try {
                try {
                    return (Effect) effectClass.getConstructor(new Class[]{EffectContext.class, String.class}).newInstance(new Object[]{this.mEffectContext, name});
                } catch (Throwable t) {
                    RuntimeException runtimeException = new RuntimeException("There was an error constructing the effect '" + effectClass + "'!", t);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("The effect class '" + effectClass + "' does not have " + "the required constructor.", e);
            }
        } catch (ClassCastException e2) {
            throw new IllegalArgumentException("Attempting to allocate effect '" + effectClass + "' which is not a subclass of Effect!", e2);
        }
    }
}
