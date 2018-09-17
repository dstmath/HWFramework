package android.transition;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.ViewGroup;
import com.android.internal.R;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TransitionInflater {
    private static final Class<?>[] sConstructorSignature = null;
    private static final ArrayMap<String, Constructor> sConstructors = null;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.TransitionInflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.TransitionInflater.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.TransitionInflater.<clinit>():void");
    }

    private TransitionInflater(Context context) {
        this.mContext = context;
    }

    public static TransitionInflater from(Context context) {
        return new TransitionInflater(context);
    }

    public Transition inflateTransition(int resource) {
        InflateException ex;
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            Transition createTransitionFromXml = createTransitionFromXml(parser, Xml.asAttributeSet(parser), null);
            parser.close();
            return createTransitionFromXml;
        } catch (XmlPullParserException e) {
            ex = new InflateException(e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (IOException e2) {
            ex = new InflateException(parser.getPositionDescription() + ": " + e2.getMessage());
            ex.initCause(e2);
            throw ex;
        } catch (Throwable th) {
            parser.close();
        }
    }

    public TransitionManager inflateTransitionManager(int resource, ViewGroup sceneRoot) {
        InflateException ex;
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            TransitionManager createTransitionManagerFromXml = createTransitionManagerFromXml(parser, Xml.asAttributeSet(parser), sceneRoot);
            parser.close();
            return createTransitionManagerFromXml;
        } catch (XmlPullParserException e) {
            ex = new InflateException(e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (IOException e2) {
            ex = new InflateException(parser.getPositionDescription() + ": " + e2.getMessage());
            ex.initCause(e2);
            throw ex;
        } catch (Throwable th) {
            parser.close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Transition createTransitionFromXml(XmlPullParser parser, AttributeSet attrs, Transition parent) throws XmlPullParserException, IOException {
        Transition transition = null;
        int depth = parser.getDepth();
        TransitionSet transitionSet = parent instanceof TransitionSet ? (TransitionSet) parent : null;
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (!"fade".equals(name)) {
                        if (!"changeBounds".equals(name)) {
                            if (!"slide".equals(name)) {
                                if (!"explode".equals(name)) {
                                    if (!"changeImageTransform".equals(name)) {
                                        if (!"changeTransform".equals(name)) {
                                            if (!"changeClipBounds".equals(name)) {
                                                if (!"autoTransition".equals(name)) {
                                                    if (!"recolor".equals(name)) {
                                                        if (!"changeScroll".equals(name)) {
                                                            if (!"transitionSet".equals(name)) {
                                                                if (!"transition".equals(name)) {
                                                                    if (!"targets".equals(name)) {
                                                                        if (!"arcMotion".equals(name)) {
                                                                            if (!"pathMotion".equals(name)) {
                                                                                if (!"patternPathMotion".equals(name)) {
                                                                                    break;
                                                                                }
                                                                                parent.setPathMotion(new PatternPathMotion(this.mContext, attrs));
                                                                            } else {
                                                                                parent.setPathMotion((PathMotion) createCustom(attrs, PathMotion.class, "pathMotion"));
                                                                            }
                                                                        } else {
                                                                            parent.setPathMotion(new ArcMotion(this.mContext, attrs));
                                                                        }
                                                                    } else {
                                                                        getTargetIds(parser, attrs, parent);
                                                                    }
                                                                } else {
                                                                    transition = (Transition) createCustom(attrs, Transition.class, "transition");
                                                                }
                                                            } else {
                                                                transition = new TransitionSet(this.mContext, attrs);
                                                            }
                                                        } else {
                                                            transition = new ChangeScroll(this.mContext, attrs);
                                                        }
                                                    } else {
                                                        transition = new Recolor(this.mContext, attrs);
                                                    }
                                                } else {
                                                    transition = new AutoTransition(this.mContext, attrs);
                                                }
                                            } else {
                                                transition = new ChangeClipBounds(this.mContext, attrs);
                                            }
                                        } else {
                                            transition = new ChangeTransform(this.mContext, attrs);
                                        }
                                    } else {
                                        transition = new ChangeImageTransform(this.mContext, attrs);
                                    }
                                } else {
                                    transition = new Explode(this.mContext, attrs);
                                }
                            } else {
                                transition = new Slide(this.mContext, attrs);
                            }
                        } else {
                            transition = new ChangeBounds(this.mContext, attrs);
                        }
                    } else {
                        transition = new Fade(this.mContext, attrs);
                    }
                    if (transition == null) {
                        continue;
                    } else {
                        if (!parser.isEmptyElementTag()) {
                            createTransitionFromXml(parser, attrs, transition);
                        }
                        if (transitionSet != null) {
                            transitionSet.addTransition(transition);
                            transition = null;
                        } else if (parent != null) {
                            break;
                        }
                    }
                }
            }
        }
        throw new InflateException("Could not add transition to another transition.");
    }

    private Object createCustom(AttributeSet attrs, Class expectedType, String tag) {
        String className = attrs.getAttributeValue(null, "class");
        if (className == null) {
            throw new InflateException(tag + " tag must have a 'class' attribute");
        }
        try {
            Object newInstance;
            synchronized (sConstructors) {
                Constructor constructor = (Constructor) sConstructors.get(className);
                if (constructor == null) {
                    Class c = this.mContext.getClassLoader().loadClass(className).asSubclass(expectedType);
                    if (c != null) {
                        constructor = c.getConstructor(sConstructorSignature);
                        constructor.setAccessible(true);
                        sConstructors.put(className, constructor);
                    }
                }
                newInstance = constructor.newInstance(new Object[]{this.mContext, attrs});
            }
            return newInstance;
        } catch (InstantiationException e) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e);
        } catch (ClassNotFoundException e2) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e2);
        } catch (InvocationTargetException e3) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e3);
        } catch (NoSuchMethodException e4) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e4);
        } catch (IllegalAccessException e5) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e5);
        }
    }

    private void getTargetIds(XmlPullParser parser, AttributeSet attrs, Transition transition) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    if (!parser.getName().equals("target")) {
                        break;
                    }
                    TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TransitionTarget);
                    int id = a.getResourceId(1, 0);
                    if (id != 0) {
                        transition.addTarget(id);
                    } else {
                        id = a.getResourceId(2, 0);
                        if (id != 0) {
                            transition.excludeTarget(id, true);
                        } else {
                            String transitionName = a.getString(4);
                            if (transitionName != null) {
                                transition.addTarget(transitionName);
                            } else {
                                transitionName = a.getString(5);
                                if (transitionName != null) {
                                    transition.excludeTarget(transitionName, true);
                                } else {
                                    String className = a.getString(3);
                                    if (className != null) {
                                        try {
                                            transition.excludeTarget(Class.forName(className), true);
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException("Could not create " + className, e);
                                        }
                                    }
                                    className = a.getString(0);
                                    if (className != null) {
                                        transition.addTarget(Class.forName(className));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                return;
            }
        }
        throw new RuntimeException("Unknown scene name: " + parser.getName());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private TransitionManager createTransitionManagerFromXml(XmlPullParser parser, AttributeSet attrs, ViewGroup sceneRoot) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        TransitionManager transitionManager = null;
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (name.equals("transitionManager")) {
                        transitionManager = new TransitionManager();
                    } else if (name.equals("transition") && transitionManager != null) {
                        loadTransition(attrs, sceneRoot, transitionManager);
                    }
                }
            }
        }
        throw new RuntimeException("Unknown scene name: " + parser.getName());
    }

    private void loadTransition(AttributeSet attrs, ViewGroup sceneRoot, TransitionManager transitionManager) throws NotFoundException {
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TransitionManager);
        int transitionId = a.getResourceId(2, -1);
        int fromId = a.getResourceId(0, -1);
        Scene sceneForLayout = fromId < 0 ? null : Scene.getSceneForLayout(sceneRoot, fromId, this.mContext);
        int toId = a.getResourceId(1, -1);
        Scene sceneForLayout2 = toId < 0 ? null : Scene.getSceneForLayout(sceneRoot, toId, this.mContext);
        if (transitionId >= 0) {
            Transition transition = inflateTransition(transitionId);
            if (transition != null) {
                if (sceneForLayout2 == null) {
                    throw new RuntimeException("No toScene for transition ID " + transitionId);
                } else if (sceneForLayout == null) {
                    transitionManager.setTransition(sceneForLayout2, transition);
                } else {
                    transitionManager.setTransition(sceneForLayout, sceneForLayout2, transition);
                }
            }
        }
        a.recycle();
    }
}
