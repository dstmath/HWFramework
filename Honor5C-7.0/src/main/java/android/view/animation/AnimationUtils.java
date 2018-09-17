package android.view.animation;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationUtils {
    private static final int SEQUENTIALLY = 1;
    private static final int TOGETHER = 0;

    public static long currentAnimationTimeMillis() {
        return SystemClock.uptimeMillis();
    }

    public static Animation loadAnimation(Context context, int id) throws NotFoundException {
        NotFoundException rnf;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getAnimation(id);
            Animation createAnimationFromXml = createAnimationFromXml(context, xmlResourceParser);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return createAnimationFromXml;
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

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Animation createAnimationFromXml(Context c, XmlPullParser parser, AnimationSet parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        Animation anim = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != SEQUENTIALLY) {
                if (type == 2) {
                    String name = parser.getName();
                    if (!name.equals("set")) {
                        if (!name.equals("alpha")) {
                            if (!name.equals("scale")) {
                                if (!name.equals("rotate")) {
                                    if (!name.equals("translate")) {
                                        break;
                                    }
                                    anim = new TranslateAnimation(c, attrs);
                                } else {
                                    anim = new RotateAnimation(c, attrs);
                                }
                            } else {
                                anim = new ScaleAnimation(c, attrs);
                            }
                        } else {
                            anim = new AlphaAnimation(c, attrs);
                        }
                    } else {
                        anim = new AnimationSet(c, attrs);
                        createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
                    }
                    if (parent != null) {
                        parent.addAnimation(anim);
                    }
                }
            }
        }
        throw new RuntimeException("Unknown animation name: " + parser.getName());
    }

    public static LayoutAnimationController loadLayoutAnimation(Context context, int id) throws NotFoundException {
        NotFoundException rnf;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getAnimation(id);
            LayoutAnimationController createLayoutAnimationFromXml = createLayoutAnimationFromXml(context, xmlResourceParser);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return createLayoutAnimationFromXml;
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

    private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createLayoutAnimationFromXml(c, parser, Xml.asAttributeSet(parser));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        LayoutAnimationController controller = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != SEQUENTIALLY) {
                if (type == 2) {
                    String name = parser.getName();
                    if (!"layoutAnimation".equals(name)) {
                        if (!"gridLayoutAnimation".equals(name)) {
                            break;
                        }
                        controller = new GridLayoutAnimationController(c, attrs);
                    } else {
                        controller = new LayoutAnimationController(c, attrs);
                    }
                }
            }
        }
        return controller;
    }

    public static Animation makeInAnimation(Context c, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = loadAnimation(c, R.anim.slide_in_left);
        } else {
            a = loadAnimation(c, R.anim.slide_in_right);
        }
        a.setInterpolator(new DecelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Animation makeOutAnimation(Context c, boolean toRight) {
        Animation a;
        if (toRight) {
            a = loadAnimation(c, R.anim.slide_out_right);
        } else {
            a = loadAnimation(c, R.anim.slide_out_left);
        }
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Animation makeInChildBottomAnimation(Context c) {
        Animation a = loadAnimation(c, R.anim.slide_in_child_bottom);
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Interpolator loadInterpolator(Context context, int id) throws NotFoundException {
        NotFoundException rnf;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getAnimation(id);
            Interpolator createInterpolatorFromXml = createInterpolatorFromXml(context.getResources(), context.getTheme(), xmlResourceParser);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return createInterpolatorFromXml;
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

    public static Interpolator loadInterpolator(Resources res, Theme theme, int id) throws NotFoundException {
        NotFoundException rnf;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = res.getAnimation(id);
            Interpolator createInterpolatorFromXml = createInterpolatorFromXml(res, theme, xmlResourceParser);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return createInterpolatorFromXml;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Interpolator createInterpolatorFromXml(Resources res, Theme theme, XmlPullParser parser) throws XmlPullParserException, IOException {
        Interpolator interpolator = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != SEQUENTIALLY) {
                if (type == 2) {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    String name = parser.getName();
                    if (!name.equals("linearInterpolator")) {
                        if (!name.equals("accelerateInterpolator")) {
                            if (!name.equals("decelerateInterpolator")) {
                                if (!name.equals("accelerateDecelerateInterpolator")) {
                                    if (!name.equals("cycleInterpolator")) {
                                        if (!name.equals("anticipateInterpolator")) {
                                            if (!name.equals("overshootInterpolator")) {
                                                if (!name.equals("anticipateOvershootInterpolator")) {
                                                    if (!name.equals("bounceInterpolator")) {
                                                        if (!name.equals("pathInterpolator")) {
                                                            if (!name.equals("cubicBezierInterpolator") && !name.equals("cubicBezierReverseInterpolator")) {
                                                                break;
                                                            }
                                                            interpolator = (BaseInterpolator) HwFrameworkFactory.createHwInterpolator(name, res, theme, attrs);
                                                        } else {
                                                            interpolator = new PathInterpolator(res, theme, attrs);
                                                        }
                                                    } else {
                                                        interpolator = new BounceInterpolator();
                                                    }
                                                } else {
                                                    interpolator = new AnticipateOvershootInterpolator(res, theme, attrs);
                                                }
                                            } else {
                                                interpolator = new OvershootInterpolator(res, theme, attrs);
                                            }
                                        } else {
                                            interpolator = new AnticipateInterpolator(res, theme, attrs);
                                        }
                                    } else {
                                        interpolator = new CycleInterpolator(res, theme, attrs);
                                    }
                                } else {
                                    interpolator = new AccelerateDecelerateInterpolator();
                                }
                            } else {
                                interpolator = new DecelerateInterpolator(res, theme, attrs);
                            }
                        } else {
                            interpolator = new AccelerateInterpolator(res, theme, attrs);
                        }
                    } else {
                        interpolator = new LinearInterpolator();
                    }
                }
            }
        }
        throw new RuntimeException("Unknown interpolator name: " + parser.getName());
    }
}
