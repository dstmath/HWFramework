package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.InflateException;
import java.io.IOException;
import java.lang.annotation.RCUnownedRef;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class DrawableInflater {
    private static final HashMap<String, Constructor<? extends Drawable>> CONSTRUCTOR_MAP = new HashMap<>();
    @UnsupportedAppUsage
    private final ClassLoader mClassLoader;
    @RCUnownedRef
    private final Resources mRes;

    public static Drawable loadDrawable(Context context, int id) {
        return loadDrawable(context.getResources(), context.getTheme(), id);
    }

    public static Drawable loadDrawable(Resources resources, Resources.Theme theme, int id) {
        return resources.getDrawable(id, theme);
    }

    public DrawableInflater(Resources res, ClassLoader classLoader) {
        this.mRes = res;
        this.mClassLoader = classLoader;
    }

    public Drawable inflateFromXml(String name, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        return inflateFromXmlForDensity(name, parser, attrs, 0, theme);
    }

    /* access modifiers changed from: package-private */
    public Drawable inflateFromXmlForDensity(String name, XmlPullParser parser, AttributeSet attrs, int density, Resources.Theme theme) throws XmlPullParserException, IOException {
        if (!name.equals("drawable") || (name = attrs.getAttributeValue(null, "class")) != null) {
            Drawable drawable = inflateFromTag(name);
            if (drawable == null) {
                drawable = inflateFromClass(name);
            }
            drawable.setSrcDensityOverride(density);
            drawable.inflate(this.mRes, parser, attrs, theme);
            return drawable;
        }
        throw new InflateException("<drawable> tag must specify class attribute");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private Drawable inflateFromTag(String name) {
        char c;
        switch (name.hashCode()) {
            case -2024464016:
                if (name.equals("adaptive-icon")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1724158635:
                if (name.equals("transition")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1671889043:
                if (name.equals("nine-patch")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1493546681:
                if (name.equals("animation-list")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1388777169:
                if (name.equals("bitmap")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -930826704:
                if (name.equals("ripple")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -925180581:
                if (name.equals("rotate")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -820387517:
                if (name.equals("vector")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -510364471:
                if (name.equals("animated-selector")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -94197862:
                if (name.equals("layer-list")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3056464:
                if (name.equals("clip")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 94842723:
                if (name.equals("color")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 100360477:
                if (name.equals("inset")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 109250890:
                if (name.equals(BatteryManager.EXTRA_SCALE)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 109399969:
                if (name.equals("shape")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 160680263:
                if (name.equals("level-list")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1191572447:
                if (name.equals("selector")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1442046129:
                if (name.equals("animated-image")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 2013827269:
                if (name.equals("animated-rotate")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 2118620333:
                if (name.equals("animated-vector")) {
                    c = '\n';
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
                return new StateListDrawable();
            case 1:
                return new AnimatedStateListDrawable();
            case 2:
                return new LevelListDrawable();
            case 3:
                return new LayerDrawable();
            case 4:
                return new TransitionDrawable();
            case 5:
                return new RippleDrawable();
            case 6:
                return new AdaptiveIconDrawable();
            case 7:
                return new ColorDrawable();
            case '\b':
                return new GradientDrawable();
            case '\t':
                return new VectorDrawable();
            case '\n':
                return new AnimatedVectorDrawable();
            case 11:
                return new ScaleDrawable();
            case '\f':
                return new ClipDrawable();
            case '\r':
                return new RotateDrawable();
            case 14:
                return new AnimatedRotateDrawable();
            case 15:
                return new AnimationDrawable();
            case 16:
                return new InsetDrawable();
            case 17:
                return new BitmapDrawable();
            case 18:
                return new NinePatchDrawable();
            case 19:
                return new AnimatedImageDrawable();
            default:
                return null;
        }
    }

    private Drawable inflateFromClass(String className) {
        Constructor<? extends Drawable> constructor;
        try {
            synchronized (CONSTRUCTOR_MAP) {
                constructor = CONSTRUCTOR_MAP.get(className);
                if (constructor == null) {
                    constructor = this.mClassLoader.loadClass(className).asSubclass(Drawable.class).getConstructor(new Class[0]);
                    CONSTRUCTOR_MAP.put(className, constructor);
                }
            }
            return (Drawable) constructor.newInstance(new Object[0]);
        } catch (NoSuchMethodException e) {
            InflateException ie = new InflateException("Error inflating class " + className);
            ie.initCause(e);
            throw ie;
        } catch (ClassCastException e2) {
            InflateException ie2 = new InflateException("Class is not a Drawable " + className);
            ie2.initCause(e2);
            throw ie2;
        } catch (ClassNotFoundException e3) {
            InflateException ie3 = new InflateException("Class not found " + className);
            ie3.initCause(e3);
            throw ie3;
        } catch (Exception e4) {
            InflateException ie4 = new InflateException("Error inflating class " + className);
            ie4.initCause(e4);
            throw ie4;
        }
    }
}
