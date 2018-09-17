package android.graphics.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.InflateException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class DrawableInflater {
    private static final HashMap<String, Constructor<? extends Drawable>> CONSTRUCTOR_MAP = new HashMap();
    private final ClassLoader mClassLoader;
    private final Resources mRes;

    public static Drawable loadDrawable(Context context, int id) {
        return loadDrawable(context.getResources(), context.getTheme(), id);
    }

    public static Drawable loadDrawable(Resources resources, Theme theme, int id) {
        return resources.getDrawable(id, theme);
    }

    public DrawableInflater(Resources res, ClassLoader classLoader) {
        this.mRes = res;
        this.mClassLoader = classLoader;
    }

    public Drawable inflateFromXml(String name, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        return inflateFromXmlForDensity(name, parser, attrs, 0, theme);
    }

    Drawable inflateFromXmlForDensity(String name, XmlPullParser parser, AttributeSet attrs, int density, Theme theme) throws XmlPullParserException, IOException {
        if (name.equals("drawable")) {
            name = attrs.getAttributeValue(null, "class");
            if (name == null) {
                throw new InflateException("<drawable> tag must specify class attribute");
            }
        }
        Drawable drawable = inflateFromTag(name);
        if (drawable == null) {
            drawable = inflateFromClass(name);
        }
        drawable.setSrcDensityOverride(density);
        drawable.inflate(this.mRes, parser, attrs, theme);
        return drawable;
    }

    private Drawable inflateFromTag(String name) {
        if (name.equals("selector")) {
            return new StateListDrawable();
        }
        if (name.equals("animated-selector")) {
            return new AnimatedStateListDrawable();
        }
        if (name.equals("level-list")) {
            return new LevelListDrawable();
        }
        if (name.equals("hwcolorful")) {
            return new HwColorfulDrawable();
        }
        if (name.equals("layer-list")) {
            return new LayerDrawable();
        }
        if (name.equals("transition")) {
            return new TransitionDrawable();
        }
        if (name.equals("ripple")) {
            return new RippleDrawable();
        }
        if (name.equals("adaptive-icon")) {
            return new AdaptiveIconDrawable();
        }
        if (name.equals("color")) {
            return new ColorDrawable();
        }
        if (name.equals("shape")) {
            return new GradientDrawable();
        }
        if (name.equals("vector")) {
            return new VectorDrawable();
        }
        if (name.equals("animated-vector")) {
            return new AnimatedVectorDrawable();
        }
        if (name.equals(BatteryManager.EXTRA_SCALE)) {
            return new ScaleDrawable();
        }
        if (name.equals("clip")) {
            return new ClipDrawable();
        }
        if (name.equals("rotate")) {
            return new RotateDrawable();
        }
        if (name.equals("animated-rotate")) {
            return new AnimatedRotateDrawable();
        }
        if (name.equals("animation-list")) {
            return new AnimationDrawable();
        }
        if (name.equals("inset")) {
            return new InsetDrawable();
        }
        if (name.equals("bitmap")) {
            return new BitmapDrawable();
        }
        if (name.equals("nine-patch")) {
            return new NinePatchDrawable();
        }
        return null;
    }

    private Drawable inflateFromClass(String className) {
        InflateException ie;
        try {
            Constructor<? extends Drawable> constructor;
            synchronized (CONSTRUCTOR_MAP) {
                constructor = (Constructor) CONSTRUCTOR_MAP.get(className);
                if (constructor == null) {
                    constructor = this.mClassLoader.loadClass(className).asSubclass(Drawable.class).getConstructor(new Class[0]);
                    CONSTRUCTOR_MAP.put(className, constructor);
                }
            }
            return (Drawable) constructor.newInstance(new Object[0]);
        } catch (NoSuchMethodException e) {
            ie = new InflateException("Error inflating class " + className);
            ie.initCause(e);
            throw ie;
        } catch (ClassCastException e2) {
            ie = new InflateException("Class is not a Drawable " + className);
            ie.initCause(e2);
            throw ie;
        } catch (ClassNotFoundException e3) {
            ie = new InflateException("Class not found " + className);
            ie.initCause(e3);
            throw ie;
        } catch (Exception e4) {
            ie = new InflateException("Error inflating class " + className);
            ie.initCause(e4);
            throw ie;
        }
    }
}
