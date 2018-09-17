package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.android.internal.R;
import huawei.cust.HwCfgFilePolicy;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class LayoutInflater {
    private static final int[] ATTRS_THEME = null;
    private static final String ATTR_LAYOUT = "layout";
    private static final ClassLoader BOOT_CLASS_LOADER = null;
    private static final boolean DEBUG = false;
    private static final StackTraceElement[] EMPTY_STACK_TRACE = null;
    private static final String TAG = null;
    private static final String TAG_1995 = "blink";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_MERGE = "merge";
    private static final String TAG_REQUEST_FOCUS = "requestFocus";
    private static final String TAG_TAG = "tag";
    static final Class<?>[] mConstructorSignature = null;
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = null;
    final Object[] mConstructorArgs;
    protected final Context mContext;
    private Factory mFactory;
    private Factory2 mFactory2;
    private boolean mFactorySet;
    private Filter mFilter;
    private HashMap<String, Boolean> mFilterMap;
    private Factory2 mPrivateFactory;
    private TypedValue mTempValue;

    private static class BlinkLayout extends FrameLayout {
        private static final int BLINK_DELAY = 500;
        private static final int MESSAGE_BLINK = 66;
        private boolean mBlink;
        private boolean mBlinkState;
        private final Handler mHandler;

        public BlinkLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mHandler = new Handler(new Callback() {
                public boolean handleMessage(Message msg) {
                    boolean z = LayoutInflater.DEBUG;
                    if (msg.what != BlinkLayout.MESSAGE_BLINK) {
                        return LayoutInflater.DEBUG;
                    }
                    if (BlinkLayout.this.mBlink) {
                        BlinkLayout blinkLayout = BlinkLayout.this;
                        if (!BlinkLayout.this.mBlinkState) {
                            z = true;
                        }
                        blinkLayout.mBlinkState = z;
                        BlinkLayout.this.makeBlink();
                    }
                    BlinkLayout.this.invalidate();
                    return true;
                }
            });
        }

        private void makeBlink() {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_BLINK), 500);
        }

        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mBlink = true;
            this.mBlinkState = true;
            makeBlink();
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mBlink = LayoutInflater.DEBUG;
            this.mBlinkState = true;
            this.mHandler.removeMessages(MESSAGE_BLINK);
        }

        protected void dispatchDraw(Canvas canvas) {
            if (this.mBlinkState) {
                super.dispatchDraw(canvas);
            }
        }
    }

    public interface Factory {
        View onCreateView(String str, Context context, AttributeSet attributeSet);
    }

    public interface Factory2 extends Factory {
        View onCreateView(View view, String str, Context context, AttributeSet attributeSet);
    }

    private static class FactoryMerger implements Factory2 {
        private final Factory mF1;
        private final Factory2 mF12;
        private final Factory mF2;
        private final Factory2 mF22;

        FactoryMerger(Factory f1, Factory2 f12, Factory f2, Factory2 f22) {
            this.mF1 = f1;
            this.mF2 = f2;
            this.mF12 = f12;
            this.mF22 = f22;
        }

        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View v = this.mF1.onCreateView(name, context, attrs);
            if (v != null) {
                return v;
            }
            return this.mF2.onCreateView(name, context, attrs);
        }

        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            View v;
            if (this.mF12 != null) {
                v = this.mF12.onCreateView(parent, name, context, attrs);
            } else {
                v = this.mF1.onCreateView(name, context, attrs);
            }
            if (v != null) {
                return v;
            }
            View onCreateView;
            if (this.mF22 != null) {
                onCreateView = this.mF22.onCreateView(parent, name, context, attrs);
            } else {
                onCreateView = this.mF2.onCreateView(name, context, attrs);
            }
            return onCreateView;
        }
    }

    public interface Filter {
        boolean onLoadClass(Class cls);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.LayoutInflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.LayoutInflater.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.LayoutInflater.<clinit>():void");
    }

    public abstract LayoutInflater cloneInContext(Context context);

    protected LayoutInflater(Context context) {
        this.mConstructorArgs = new Object[2];
        this.mContext = context;
    }

    protected LayoutInflater(LayoutInflater original, Context newContext) {
        this.mConstructorArgs = new Object[2];
        this.mContext = newContext;
        this.mFactory = original.mFactory;
        this.mFactory2 = original.mFactory2;
        this.mPrivateFactory = original.mPrivateFactory;
        setFilter(original.mFilter);
    }

    public static LayoutInflater from(Context context) {
        LayoutInflater LayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        if (LayoutInflater != null) {
            return LayoutInflater;
        }
        throw new AssertionError("LayoutInflater not found.");
    }

    public Context getContext() {
        return this.mContext;
    }

    public final Factory getFactory() {
        return this.mFactory;
    }

    public final Factory2 getFactory2() {
        return this.mFactory2;
    }

    public void setFactory(Factory factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        } else if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        } else {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, null, this.mFactory, this.mFactory2);
            }
        }
    }

    public void setFactory2(Factory2 factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        } else if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        } else {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory2 = factory;
                this.mFactory = factory;
                return;
            }
            Factory factoryMerger = new FactoryMerger(factory, factory, this.mFactory, this.mFactory2);
            this.mFactory2 = factoryMerger;
            this.mFactory = factoryMerger;
        }
    }

    public void setPrivateFactory(Factory2 factory) {
        if (this.mPrivateFactory == null) {
            this.mPrivateFactory = factory;
        } else {
            this.mPrivateFactory = new FactoryMerger(factory, factory, this.mPrivateFactory, this.mPrivateFactory);
        }
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
        if (filter != null) {
            this.mFilterMap = new HashMap();
        }
    }

    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null ? true : DEBUG);
    }

    public View inflate(XmlPullParser parser, ViewGroup root) {
        return inflate(parser, root, root != null ? true : DEBUG);
    }

    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        XmlPullParser parser = getContext().getResources().getLayout(resource);
        try {
            View inflate = inflate(parser, root, attachToRoot);
            return inflate;
        } finally {
            parser.close();
        }
    }

    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        View result;
        InflateException ie;
        synchronized (this.mConstructorArgs) {
            Trace.traceBegin(8, "inflate");
            Context inflaterContext = this.mContext;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = this.mConstructorArgs[0];
            this.mConstructorArgs[0] = inflaterContext;
            result = root;
            int type;
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (XmlPullParserException e) {
                    ie = new InflateException(e.getMessage(), e);
                    ie.setStackTrace(EMPTY_STACK_TRACE);
                    throw ie;
                } catch (Exception e2) {
                    ie = new InflateException(parser.getPositionDescription() + ": " + e2.getMessage(), e2);
                    ie.setStackTrace(EMPTY_STACK_TRACE);
                    throw ie;
                } catch (Throwable th) {
                    this.mConstructorArgs[0] = lastContext;
                    this.mConstructorArgs[1] = null;
                    Trace.traceEnd(8);
                }
            } while (type != 1);
            if (type != 2) {
                throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
            }
            String name = parser.getName();
            if (!TAG_MERGE.equals(name)) {
                View temp = createViewFromTag(root, name, inflaterContext, attrs);
                LayoutParams layoutParams = null;
                if (root != null) {
                    layoutParams = root.generateLayoutParams(attrs);
                    if (!attachToRoot) {
                        temp.setLayoutParams(layoutParams);
                    }
                }
                rInflateChildren(parser, temp, attrs, true);
                if (root != null && attachToRoot) {
                    root.addView(temp, layoutParams);
                }
                if (root == null || !attachToRoot) {
                    result = temp;
                }
            } else if (root == null || !attachToRoot) {
                throw new InflateException("<merge /> can be used only with a valid ViewGroup root and attachToRoot=true");
            } else {
                rInflate(parser, root, inflaterContext, attrs, DEBUG);
            }
            this.mConstructorArgs[0] = lastContext;
            this.mConstructorArgs[1] = null;
            Trace.traceEnd(8);
        }
        return result;
    }

    private final boolean verifyClassLoader(Constructor<? extends View> constructor) {
        ClassLoader constructorLoader = constructor.getDeclaringClass().getClassLoader();
        if (constructorLoader == BOOT_CLASS_LOADER) {
            return true;
        }
        ClassLoader cl = this.mContext.getClassLoader();
        while (constructorLoader != cl) {
            cl = cl.getParent();
            if (cl == null) {
                return DEBUG;
            }
        }
        return true;
    }

    public final View createView(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        StringBuilder append;
        InflateException ie;
        synchronized (sConstructorMap) {
            Constructor constructor = (Constructor) sConstructorMap.get(name);
        }
        if (!(constructor == null || verifyClassLoader(constructor))) {
            constructor = null;
            synchronized (sConstructorMap) {
                sConstructorMap.remove(name);
            }
        }
        Class cls = null;
        try {
            Trace.traceBegin(8, name);
            ClassLoader classLoader;
            String str;
            if (constructor == null) {
                classLoader = this.mContext.getClassLoader();
                if (prefix != null) {
                    str = prefix + name;
                } else {
                    str = name;
                }
                cls = classLoader.loadClass(str).asSubclass(View.class);
                if (!(this.mFilter == null || cls == null || this.mFilter.onLoadClass(cls))) {
                    failNotAllowed(name, prefix, attrs);
                }
                constructor = cls.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                synchronized (sConstructorMap) {
                    sConstructorMap.put(name, constructor);
                }
            } else if (this.mFilter != null) {
                Boolean allowedState = (Boolean) this.mFilterMap.get(name);
                if (allowedState == null) {
                    classLoader = this.mContext.getClassLoader();
                    if (prefix != null) {
                        str = prefix + name;
                    } else {
                        str = name;
                    }
                    Class<? extends View> clazz = classLoader.loadClass(str).asSubclass(View.class);
                    boolean onLoadClass = clazz != null ? this.mFilter.onLoadClass(clazz) : DEBUG;
                    this.mFilterMap.put(name, Boolean.valueOf(onLoadClass));
                    if (!onLoadClass) {
                        failNotAllowed(name, prefix, attrs);
                    }
                } else if (allowedState.equals(Boolean.FALSE)) {
                    failNotAllowed(name, prefix, attrs);
                }
            }
            Object[] args = this.mConstructorArgs;
            args[1] = attrs;
            View view = (View) constructor.newInstance(args);
            if (view instanceof ViewStub) {
                ((ViewStub) view).setLayoutInflater(cloneInContext((Context) args[0]));
            }
            Trace.traceEnd(8);
            return view;
        } catch (NoSuchMethodException e) {
            try {
                append = new StringBuilder().append(attrs.getPositionDescription()).append(": Error inflating class ");
                if (prefix != null) {
                    name = prefix + name;
                }
                ie = new InflateException(append.append(name).toString(), e);
                ie.setStackTrace(EMPTY_STACK_TRACE);
                throw ie;
            } catch (Throwable th) {
                Trace.traceEnd(8);
            }
        } catch (ClassCastException e2) {
            append = new StringBuilder().append(attrs.getPositionDescription()).append(": Class is not a View ");
            if (prefix != null) {
                name = prefix + name;
            }
            ie = new InflateException(append.append(name).toString(), e2);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (ClassNotFoundException e3) {
            throw e3;
        } catch (Exception e4) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + (cls == null ? "<unknown>" : cls.getName()), e4);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        }
    }

    private void failNotAllowed(String name, String prefix, AttributeSet attrs) {
        StringBuilder append = new StringBuilder().append(attrs.getPositionDescription()).append(": Class not allowed to be inflated ");
        if (prefix != null) {
            name = prefix + name;
        }
        throw new InflateException(append.append(name).toString());
    }

    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createView(name, "android.view.", attrs);
    }

    protected View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        return onCreateView(name, attrs);
    }

    private View createViewFromTag(View parent, String name, Context context, AttributeSet attrs) {
        return createViewFromTag(parent, name, context, attrs, DEBUG);
    }

    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs, boolean ignoreThemeAttr) {
        InflateException ie;
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }
        if (!ignoreThemeAttr) {
            TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            int themeResId = ta.getResourceId(0, 0);
            if (themeResId != 0) {
                context = new ContextThemeWrapper(context, themeResId);
            }
            ta.recycle();
        }
        if (name.equals(TAG_1995)) {
            return new BlinkLayout(context, attrs);
        }
        Object lastContext;
        try {
            View onCreateView;
            if (this.mFactory2 != null) {
                onCreateView = this.mFactory2.onCreateView(parent, name, context, attrs);
            } else if (this.mFactory != null) {
                onCreateView = this.mFactory.onCreateView(name, context, attrs);
            } else {
                onCreateView = null;
            }
            if (onCreateView == null && this.mPrivateFactory != null) {
                onCreateView = this.mPrivateFactory.onCreateView(parent, name, context, attrs);
            }
            if (onCreateView == null) {
                lastContext = this.mConstructorArgs[0];
                this.mConstructorArgs[0] = context;
                if (-1 == name.indexOf(46)) {
                    onCreateView = onCreateView(parent, name, attrs);
                } else {
                    onCreateView = createView(name, null, attrs);
                }
                this.mConstructorArgs[0] = lastContext;
            }
            return onCreateView;
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e2);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (Exception e3) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e3);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (Throwable th) {
            this.mConstructorArgs[0] = lastContext;
        }
    }

    final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
        rInflate(parser, parent, parent.getContext(), attrs, finishInflate);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void rInflate(XmlPullParser parser, View parent, Context context, AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (TAG_REQUEST_FOCUS.equals(name)) {
                        parseRequestFocus(parser, parent);
                    } else if (TAG_TAG.equals(name)) {
                        parseViewTag(parser, parent, attrs);
                    } else if (TAG_INCLUDE.equals(name)) {
                        if (parser.getDepth() == 0) {
                            break;
                        }
                        parseInclude(parser, context, parent, attrs);
                    } else if (TAG_MERGE.equals(name)) {
                        break;
                    } else {
                        View view = createViewFromTag(parent, name, context, attrs);
                        ViewGroup viewGroup = (ViewGroup) parent;
                        LayoutParams params = viewGroup.generateLayoutParams(attrs);
                        rInflateChildren(parser, view, attrs, true);
                        viewGroup.addView(view, params);
                    }
                }
            }
        }
        throw new InflateException("<merge /> must be the root element");
    }

    private void parseRequestFocus(XmlPullParser parser, View view) throws XmlPullParserException, IOException {
        view.requestFocus();
        consumeChildElements(parser);
    }

    private void parseViewTag(XmlPullParser parser, View view, AttributeSet attrs) throws XmlPullParserException, IOException {
        TypedArray ta = view.getContext().obtainStyledAttributes(attrs, R.styleable.ViewTag);
        view.setTag(ta.getResourceId(1, 0), ta.getText(0));
        ta.recycle();
        consumeChildElements(parser);
    }

    private void parseInclude(XmlPullParser parser, Context context, View parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        if (parent instanceof ViewGroup) {
            TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            int themeResId = ta.getResourceId(0, 0);
            boolean hasThemeOverride = themeResId != 0 ? true : DEBUG;
            if (hasThemeOverride) {
                context = new ContextThemeWrapper(context, themeResId);
            }
            ta.recycle();
            int layout = attrs.getAttributeResourceValue(null, ATTR_LAYOUT, 0);
            if (layout == 0) {
                String value = attrs.getAttributeValue(null, ATTR_LAYOUT);
                if (value == null || value.length() <= 0) {
                    throw new InflateException("You must specify a layout in the include tag: <include layout=\"@layout/layoutID\" />");
                }
                layout = context.getResources().getIdentifier(value.substring(1), null, null);
            }
            if (this.mTempValue == null) {
                this.mTempValue = new TypedValue();
            }
            if (layout != 0) {
                if (context.getTheme().resolveAttribute(layout, this.mTempValue, true)) {
                    layout = this.mTempValue.resourceId;
                }
            }
            if (layout == 0) {
                throw new InflateException("You must specify a valid layout reference. The layout ID " + attrs.getAttributeValue(null, ATTR_LAYOUT) + " is not valid.");
            }
            XmlResourceParser childParser = context.getResources().getLayout(layout);
            try {
                int type;
                AttributeSet childAttrs = Xml.asAttributeSet(childParser);
                do {
                    type = childParser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new InflateException(childParser.getPositionDescription() + ": No start tag found!");
                }
                String childName = childParser.getName();
                if (TAG_MERGE.equals(childName)) {
                    rInflate(childParser, parent, context, childAttrs, DEBUG);
                } else {
                    View view = createViewFromTag(parent, childName, context, childAttrs, hasThemeOverride);
                    ViewGroup group = (ViewGroup) parent;
                    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Include);
                    int id = a.getResourceId(0, -1);
                    int visibility = a.getInt(1, -1);
                    a.recycle();
                    LayoutParams params = null;
                    params = group.generateLayoutParams(attrs);
                    if (params == null) {
                        params = group.generateLayoutParams(childAttrs);
                    }
                    view.setLayoutParams(params);
                    rInflateChildren(childParser, view, childAttrs, true);
                    if (id != -1) {
                        view.setId(id);
                    }
                    switch (visibility) {
                        case HwCfgFilePolicy.GLOBAL /*0*/:
                            view.setVisibility(0);
                            break;
                        case HwCfgFilePolicy.EMUI /*1*/:
                            view.setVisibility(4);
                            break;
                        case HwCfgFilePolicy.PC /*2*/:
                            view.setVisibility(8);
                            break;
                    }
                    group.addView(view);
                }
                childParser.close();
                consumeChildElements(parser);
            } catch (RuntimeException e) {
            } catch (Throwable th) {
                childParser.close();
            }
        } else {
            throw new InflateException("<include /> can only be used inside of a ViewGroup");
        }
    }

    static final void consumeChildElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        int currentDepth = parser.getDepth();
        int type;
        do {
            type = parser.next();
            if (type == 3 && parser.getDepth() <= currentDepth) {
                return;
            }
        } while (type != 1);
    }
}
