package android.view;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.R;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class LayoutInflater {
    @UnsupportedAppUsage
    private static final int[] ATTRS_THEME = {16842752};
    private static final String ATTR_LAYOUT = "layout";
    private static final ClassLoader BOOT_CLASS_LOADER = LayoutInflater.class.getClassLoader();
    private static final String COMPILED_VIEW_DEX_FILE_NAME = "/compiled_view.dex";
    private static final boolean DEBUG = false;
    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];
    private static final String TAG = LayoutInflater.class.getSimpleName();
    private static final String TAG_1995 = "blink";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_MERGE = "merge";
    private static final String TAG_REQUEST_FOCUS = "requestFocus";
    private static final String TAG_TAG = "tag";
    private static final String USE_PRECOMPILED_LAYOUT = "view.precompiled_layout_enabled";
    @UnsupportedAppUsage
    static final Class<?>[] mConstructorSignature = {Context.class, AttributeSet.class};
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769490)
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    @UnsupportedAppUsage(maxTargetSdk = 28)
    final Object[] mConstructorArgs;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    protected final Context mContext;
    @UnsupportedAppUsage
    private Factory mFactory;
    @UnsupportedAppUsage
    private Factory2 mFactory2;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private boolean mFactorySet;
    private Filter mFilter;
    private HashMap<String, Boolean> mFilterMap;
    private ClassLoader mPrecompiledClassLoader;
    @UnsupportedAppUsage
    private Factory2 mPrivateFactory;
    private TypedValue mTempValue;
    private boolean mUseCompiledView;

    public interface Factory {
        View onCreateView(String str, Context context, AttributeSet attributeSet);
    }

    public interface Factory2 extends Factory {
        View onCreateView(View view, String str, Context context, AttributeSet attributeSet);
    }

    public interface Filter {
        boolean onLoadClass(Class cls);
    }

    public abstract LayoutInflater cloneInContext(Context context);

    /* access modifiers changed from: private */
    public static class FactoryMerger implements Factory2 {
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

        @Override // android.view.LayoutInflater.Factory
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View v = this.mF1.onCreateView(name, context, attrs);
            if (v != null) {
                return v;
            }
            return this.mF2.onCreateView(name, context, attrs);
        }

        @Override // android.view.LayoutInflater.Factory2
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            View v;
            Factory2 factory2 = this.mF12;
            if (factory2 != null) {
                v = factory2.onCreateView(parent, name, context, attrs);
            } else {
                v = this.mF1.onCreateView(name, context, attrs);
            }
            if (v != null) {
                return v;
            }
            Factory2 factory22 = this.mF22;
            if (factory22 != null) {
                return factory22.onCreateView(parent, name, context, attrs);
            }
            return this.mF2.onCreateView(name, context, attrs);
        }
    }

    protected LayoutInflater(Context context) {
        this.mConstructorArgs = new Object[2];
        this.mContext = context;
        initPrecompiledViews();
    }

    protected LayoutInflater(LayoutInflater original, Context newContext) {
        this.mConstructorArgs = new Object[2];
        this.mContext = newContext;
        this.mFactory = original.mFactory;
        this.mFactory2 = original.mFactory2;
        this.mPrivateFactory = original.mPrivateFactory;
        setFilter(original.mFilter);
        initPrecompiledViews();
    }

    public static LayoutInflater from(Context context) {
        LayoutInflater LayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        } else if (factory != null) {
            this.mFactorySet = true;
            Factory factory2 = this.mFactory;
            if (factory2 == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, null, factory2, this.mFactory2);
            }
        } else {
            throw new NullPointerException("Given factory can not be null");
        }
    }

    public void setFactory2(Factory2 factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        } else if (factory != null) {
            this.mFactorySet = true;
            Factory factory2 = this.mFactory;
            if (factory2 == null) {
                this.mFactory2 = factory;
                this.mFactory = factory;
                return;
            }
            FactoryMerger factoryMerger = new FactoryMerger(factory, factory, factory2, this.mFactory2);
            this.mFactory2 = factoryMerger;
            this.mFactory = factoryMerger;
        } else {
            throw new NullPointerException("Given factory can not be null");
        }
    }

    @UnsupportedAppUsage
    public void setPrivateFactory(Factory2 factory) {
        Factory2 factory2 = this.mPrivateFactory;
        if (factory2 == null) {
            this.mPrivateFactory = factory;
        } else {
            this.mPrivateFactory = new FactoryMerger(factory, factory, factory2, factory2);
        }
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
        if (filter != null) {
            this.mFilterMap = new HashMap<>();
        }
    }

    private void initPrecompiledViews() {
        initPrecompiledViews(false);
    }

    private void initPrecompiledViews(boolean enablePrecompiledViews) {
        this.mUseCompiledView = enablePrecompiledViews;
        if (!this.mUseCompiledView) {
            this.mPrecompiledClassLoader = null;
            return;
        }
        ApplicationInfo appInfo = this.mContext.getApplicationInfo();
        if (appInfo.isEmbeddedDexUsed() || appInfo.isPrivilegedApp()) {
            this.mUseCompiledView = false;
            return;
        }
        try {
            this.mPrecompiledClassLoader = this.mContext.getClassLoader();
            String dexFile = this.mContext.getCodeCacheDir() + COMPILED_VIEW_DEX_FILE_NAME;
            if (new File(dexFile).exists()) {
                this.mPrecompiledClassLoader = new PathClassLoader(dexFile, this.mPrecompiledClassLoader);
            } else {
                this.mUseCompiledView = false;
            }
        } catch (Throwable th) {
            this.mUseCompiledView = false;
        }
        if (!this.mUseCompiledView) {
            this.mPrecompiledClassLoader = null;
        }
    }

    public void setPrecompiledLayoutsEnabledForTesting(boolean enablePrecompiledLayouts) {
        initPrecompiledViews(enablePrecompiledLayouts);
    }

    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    public View inflate(XmlPullParser parser, ViewGroup root) {
        return inflate(parser, root, root != null);
    }

    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        Resources res = getContext().getResources();
        View view = tryInflatePrecompiled(resource, res, root, attachToRoot);
        if (view != null) {
            return view;
        }
        XmlResourceParser parser = res.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    private View tryInflatePrecompiled(int resource, Resources res, ViewGroup root, boolean attachToRoot) {
        if (!this.mUseCompiledView) {
            return null;
        }
        Trace.traceBegin(8, "inflate (precompiled)");
        String pkg = res.getResourcePackageName(resource);
        String layout = res.getResourceEntryName(resource);
        try {
            View view = (View) Class.forName("" + pkg + ".CompiledView", false, this.mPrecompiledClassLoader).getMethod(layout, Context.class, Integer.TYPE).invoke(null, this.mContext, Integer.valueOf(resource));
            if (!(view == null || root == null)) {
                XmlResourceParser parser = res.getLayout(resource);
                try {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    advanceToRootNode(parser);
                    ViewGroup.LayoutParams params = root.generateLayoutParams(attrs);
                    if (attachToRoot) {
                        root.addView(view, params);
                    } else {
                        view.setLayoutParams(params);
                    }
                } finally {
                    parser.close();
                }
            }
            Trace.traceEnd(8);
            return view;
        } catch (Throwable th) {
            Trace.traceEnd(8);
            return null;
        }
    }

    private void advanceToRootNode(XmlPullParser parser) throws InflateException, IOException, XmlPullParserException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:31:0x008b */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:9:0x003b */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v0 */
    /* JADX WARN: Type inference failed for: r10v2 */
    /* JADX WARN: Type inference failed for: r10v4 */
    /* JADX WARN: Type inference failed for: r10v5 */
    /* JADX WARN: Type inference failed for: r10v15 */
    /* JADX WARN: Type inference failed for: r10v16 */
    /* JADX WARNING: Unknown variable types count: 1 */
    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        Throwable th;
        boolean z;
        XmlPullParserException e;
        XmlPullParserException e2;
        Exception e3;
        boolean z2;
        synchronized (this.mConstructorArgs) {
            ?? r10 = 8;
            try {
                Trace.traceBegin(8, "inflate");
                IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
                if (hwRtgSchedImpl != null) {
                    Trace.traceBegin(8, "Inflate Message Send");
                    hwRtgSchedImpl.doInflate();
                    Trace.traceEnd(8);
                }
                Context inflaterContext = this.mContext;
                AttributeSet attrs = Xml.asAttributeSet(parser);
                Context lastContext = (Context) this.mConstructorArgs[0];
                this.mConstructorArgs[0] = inflaterContext;
                View result = root;
                try {
                    advanceToRootNode(parser);
                    String name = parser.getName();
                    if (!TAG_MERGE.equals(name)) {
                        z2 = true;
                        View temp = createViewFromTag(root, name, inflaterContext, attrs);
                        ViewGroup.LayoutParams params = null;
                        if (root != null) {
                            params = root.generateLayoutParams(attrs);
                            if (!attachToRoot) {
                                temp.setLayoutParams(params);
                            }
                        }
                        try {
                            rInflateChildren(parser, temp, attrs, true);
                            if (root != null && attachToRoot) {
                                root.addView(temp, params);
                            }
                            if (root == null || !attachToRoot) {
                                result = temp;
                            }
                        } catch (XmlPullParserException e4) {
                            e2 = e4;
                            InflateException ie = new InflateException(e2.getMessage(), e2);
                            ie.setStackTrace(EMPTY_STACK_TRACE);
                            throw ie;
                        } catch (Exception e5) {
                            e3 = e5;
                            r10 = z2;
                            InflateException ie2 = new InflateException(getParserStateDescription(inflaterContext, attrs) + ": " + e3.getMessage(), e3);
                            ie2.setStackTrace(EMPTY_STACK_TRACE);
                            throw ie2;
                        }
                    } else if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid ViewGroup root and attachToRoot=true");
                    } else {
                        z2 = true;
                        try {
                            rInflate(parser, root, inflaterContext, attrs, false);
                        } catch (XmlPullParserException e6) {
                            e2 = e6;
                            InflateException ie3 = new InflateException(e2.getMessage(), e2);
                            ie3.setStackTrace(EMPTY_STACK_TRACE);
                            throw ie3;
                        } catch (Exception e7) {
                            e3 = e7;
                            r10 = z2;
                            InflateException ie22 = new InflateException(getParserStateDescription(inflaterContext, attrs) + ": " + e3.getMessage(), e3);
                            ie22.setStackTrace(EMPTY_STACK_TRACE);
                            throw ie22;
                        } catch (Throwable th2) {
                            e = th2;
                            z = z2;
                            this.mConstructorArgs[0] = lastContext;
                            Object[] objArr = this.mConstructorArgs;
                            char c = z ? 1 : 0;
                            char c2 = z ? 1 : 0;
                            char c3 = z ? 1 : 0;
                            objArr[c] = null;
                            Trace.traceEnd(8);
                            throw e;
                        }
                    }
                    try {
                        this.mConstructorArgs[0] = lastContext;
                        this.mConstructorArgs[z2 ? 1 : 0] = null;
                        Trace.traceEnd(8);
                        return result;
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } catch (XmlPullParserException e8) {
                    e2 = e8;
                    InflateException ie32 = new InflateException(e2.getMessage(), e2);
                    ie32.setStackTrace(EMPTY_STACK_TRACE);
                    throw ie32;
                } catch (Exception e9) {
                    e3 = e9;
                    r10 = 1;
                    InflateException ie222 = new InflateException(getParserStateDescription(inflaterContext, attrs) + ": " + e3.getMessage(), e3);
                    ie222.setStackTrace(EMPTY_STACK_TRACE);
                    throw ie222;
                } catch (Throwable th4) {
                    e = th4;
                    z = r10;
                    this.mConstructorArgs[0] = lastContext;
                    Object[] objArr2 = this.mConstructorArgs;
                    char c4 = z ? 1 : 0;
                    char c22 = z ? 1 : 0;
                    char c32 = z ? 1 : 0;
                    objArr2[c4] = null;
                    Trace.traceEnd(8);
                    throw e;
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }

    private static String getParserStateDescription(Context context, AttributeSet attrs) {
        int sourceResId = Resources.getAttributeSetSourceResId(attrs);
        if (sourceResId == 0) {
            return attrs.getPositionDescription();
        }
        return attrs.getPositionDescription() + " in " + context.getResources().getResourceName(sourceResId);
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
                return false;
            }
        }
        return true;
    }

    public final View createView(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        Context context = (Context) this.mConstructorArgs[0];
        if (context == null) {
            context = this.mContext;
        }
        return createView(context, name, prefix, attrs);
    }

    /* JADX INFO: finally extract failed */
    public final View createView(Context viewContext, String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        String str;
        String str2;
        String str3;
        String str4;
        Objects.requireNonNull(viewContext);
        Objects.requireNonNull(name);
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if (constructor != null && !verifyClassLoader(constructor)) {
            constructor = null;
            sConstructorMap.remove(name);
        }
        Class<? extends View> clazz = null;
        try {
            Trace.traceBegin(8, name);
            if (constructor == null) {
                if (prefix != null) {
                    str4 = prefix + name;
                } else {
                    str4 = name;
                }
                Class<? extends U> asSubclass = Class.forName(str4, false, this.mContext.getClassLoader()).asSubclass(View.class);
                if (!(this.mFilter == null || asSubclass == null || this.mFilter.onLoadClass(asSubclass))) {
                    failNotAllowed(name, prefix, viewContext, attrs);
                }
                constructor = asSubclass.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } else if (this.mFilter != null) {
                Boolean allowedState = this.mFilterMap.get(name);
                if (allowedState == null) {
                    if (prefix != null) {
                        str3 = prefix + name;
                    } else {
                        str3 = name;
                    }
                    Class<? extends View> clazz2 = Class.forName(str3, false, this.mContext.getClassLoader()).asSubclass(View.class);
                    boolean allowed = clazz2 != null && this.mFilter.onLoadClass(clazz2);
                    this.mFilterMap.put(name, Boolean.valueOf(allowed));
                    if (!allowed) {
                        failNotAllowed(name, prefix, viewContext, attrs);
                    }
                } else if (allowedState.equals(Boolean.FALSE)) {
                    failNotAllowed(name, prefix, viewContext, attrs);
                }
            }
            Object lastContext = this.mConstructorArgs[0];
            this.mConstructorArgs[0] = viewContext;
            Object[] args = this.mConstructorArgs;
            args[1] = attrs;
            try {
                View view = (View) constructor.newInstance(args);
                if (view instanceof ViewStub) {
                    ((ViewStub) view).setLayoutInflater(cloneInContext((Context) args[0]));
                }
                this.mConstructorArgs[0] = lastContext;
                Trace.traceEnd(8);
                return view;
            } catch (Throwable th) {
                this.mConstructorArgs[0] = lastContext;
                throw th;
            }
        } catch (NoSuchMethodException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(getParserStateDescription(viewContext, attrs));
            sb.append(": Error inflating class ");
            if (prefix != null) {
                str = prefix + name;
            } else {
                str = name;
            }
            sb.append(str);
            InflateException ie = new InflateException(sb.toString(), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (ClassCastException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(getParserStateDescription(viewContext, attrs));
            sb2.append(": Class is not a View ");
            if (prefix != null) {
                str2 = prefix + name;
            } else {
                str2 = name;
            }
            sb2.append(str2);
            InflateException ie2 = new InflateException(sb2.toString(), e2);
            ie2.setStackTrace(EMPTY_STACK_TRACE);
            throw ie2;
        } catch (ClassNotFoundException e3) {
            throw e3;
        } catch (Exception e4) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(getParserStateDescription(viewContext, attrs));
            sb3.append(": Error inflating class ");
            sb3.append(0 == 0 ? MediaStore.UNKNOWN_STRING : clazz.getName());
            InflateException ie3 = new InflateException(sb3.toString(), e4);
            ie3.setStackTrace(EMPTY_STACK_TRACE);
            throw ie3;
        } catch (Throwable th2) {
            Trace.traceEnd(8);
            throw th2;
        }
    }

    private void failNotAllowed(String name, String prefix, Context context, AttributeSet attrs) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(getParserStateDescription(context, attrs));
        sb.append(": Class not allowed to be inflated ");
        if (prefix != null) {
            str = prefix + name;
        } else {
            str = name;
        }
        sb.append(str);
        throw new InflateException(sb.toString());
    }

    /* access modifiers changed from: protected */
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createView(name, "android.view.", attrs);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        return onCreateView(name, attrs);
    }

    public View onCreateView(Context viewContext, View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        return onCreateView(parent, name, attrs);
    }

    @UnsupportedAppUsage
    private View createViewFromTag(View parent, String name, Context context, AttributeSet attrs) {
        return createViewFromTag(parent, name, context, attrs, false);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public View createViewFromTag(View parent, String name, Context context, AttributeSet attrs, boolean ignoreThemeAttr) {
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
        try {
            View view = tryCreateView(parent, name, context, attrs);
            if (view == null) {
                Object lastContext = this.mConstructorArgs[0];
                this.mConstructorArgs[0] = context;
                try {
                    if (-1 == name.indexOf(46)) {
                        view = onCreateView(context, parent, name, attrs);
                    } else {
                        view = createView(context, name, null, attrs);
                    }
                } finally {
                    this.mConstructorArgs[0] = lastContext;
                }
            }
            return view;
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            InflateException ie = new InflateException(getParserStateDescription(context, attrs) + ": Error inflating class " + name, e2);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (Exception e3) {
            InflateException ie2 = new InflateException(getParserStateDescription(context, attrs) + ": Error inflating class " + name, e3);
            ie2.setStackTrace(EMPTY_STACK_TRACE);
            throw ie2;
        }
    }

    @UnsupportedAppUsage(trackingBug = 122360734)
    public final View tryCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view;
        Factory2 factory2;
        if (name.equals(TAG_1995)) {
            return new BlinkLayout(context, attrs);
        }
        Factory2 factory22 = this.mFactory2;
        if (factory22 != null) {
            view = factory22.onCreateView(parent, name, context, attrs);
        } else {
            Factory factory = this.mFactory;
            if (factory != null) {
                view = factory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }
        }
        if (view != null || (factory2 = this.mPrivateFactory) == null) {
            return view;
        }
        return factory2.onCreateView(parent, name, context, attrs);
    }

    /* access modifiers changed from: package-private */
    public final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
        rInflate(parser, parent, parent.getContext(), attrs, finishInflate);
    }

    /* access modifiers changed from: package-private */
    public void rInflate(XmlPullParser parser, View parent, Context context, AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        boolean pendingRequestFocus = false;
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (TAG_REQUEST_FOCUS.equals(name)) {
                        pendingRequestFocus = true;
                        consumeChildElements(parser);
                    } else if ("tag".equals(name)) {
                        parseViewTag(parser, parent, attrs);
                    } else if (TAG_INCLUDE.equals(name)) {
                        if (parser.getDepth() != 0) {
                            parseInclude(parser, context, parent, attrs);
                        } else {
                            throw new InflateException("<include /> cannot be the root element");
                        }
                    } else if (!TAG_MERGE.equals(name)) {
                        View view = createViewFromTag(parent, name, context, attrs);
                        ViewGroup viewGroup = (ViewGroup) parent;
                        ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                        rInflateChildren(parser, view, attrs, true);
                        viewGroup.addView(view, params);
                    } else {
                        throw new InflateException("<merge /> must be the root element");
                    }
                }
            }
        }
        if (pendingRequestFocus) {
            parent.restoreDefaultFocus();
        }
        if (finishInflate) {
            parent.onFinishInflate();
        }
    }

    private void parseViewTag(XmlPullParser parser, View view, AttributeSet attrs) throws XmlPullParserException, IOException {
        TypedArray ta = view.getContext().obtainStyledAttributes(attrs, R.styleable.ViewTag);
        view.setTag(ta.getResourceId(1, 0), ta.getText(0));
        ta.recycle();
        consumeChildElements(parser);
    }

    @UnsupportedAppUsage
    private void parseInclude(XmlPullParser parser, Context context, View parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        Context context2;
        int layout;
        XmlResourceParser childParser;
        Throwable th;
        int type;
        AttributeSet childAttrs;
        ViewGroup.LayoutParams params;
        if (parent instanceof ViewGroup) {
            TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            int themeResId = ta.getResourceId(0, 0);
            boolean hasThemeOverride = themeResId != 0;
            if (hasThemeOverride) {
                context2 = new ContextThemeWrapper(context, themeResId);
            } else {
                context2 = context;
            }
            ta.recycle();
            int layout2 = attrs.getAttributeResourceValue(null, "layout", 0);
            if (layout2 == 0) {
                String value = attrs.getAttributeValue(null, "layout");
                if (value == null || value.length() <= 0) {
                    throw new InflateException("You must specify a layout in the include tag: <include layout=\"@layout/layoutID\" />");
                }
                layout2 = context2.getResources().getIdentifier(value.substring(1), "attr", context2.getPackageName());
            }
            if (this.mTempValue == null) {
                this.mTempValue = new TypedValue();
            }
            if (layout2 == 0 || !context2.getTheme().resolveAttribute(layout2, this.mTempValue, true)) {
                layout = layout2;
            } else {
                layout = this.mTempValue.resourceId;
            }
            if (layout != 0) {
                if (tryInflatePrecompiled(layout, context2.getResources(), (ViewGroup) parent, true) == null) {
                    XmlResourceParser childParser2 = context2.getResources().getLayout(layout);
                    try {
                        AttributeSet childAttrs2 = Xml.asAttributeSet(childParser2);
                        if (type == 2) {
                            String childName = childParser2.getName();
                            if (TAG_MERGE.equals(childName)) {
                                try {
                                    rInflate(childParser2, parent, context2, childAttrs2, false);
                                    childParser = childParser2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    childParser = childParser2;
                                    childParser.close();
                                    throw th;
                                }
                            } else {
                                try {
                                    View view = createViewFromTag(parent, childName, context2, childAttrs2, hasThemeOverride);
                                    ViewGroup group = (ViewGroup) parent;
                                    TypedArray a = context2.obtainStyledAttributes(attrs, R.styleable.Include);
                                    int id = a.getResourceId(0, -1);
                                    int visibility = a.getInt(1, -1);
                                    a.recycle();
                                    ViewGroup.LayoutParams params2 = null;
                                    try {
                                        params2 = group.generateLayoutParams(attrs);
                                    } catch (RuntimeException e) {
                                    }
                                    if (params2 == null) {
                                        childAttrs = childAttrs2;
                                        try {
                                            params = group.generateLayoutParams(childAttrs);
                                        } catch (Throwable th3) {
                                            th = th3;
                                            childParser = childParser2;
                                            childParser.close();
                                            throw th;
                                        }
                                    } else {
                                        childAttrs = childAttrs2;
                                        params = params2;
                                    }
                                    view.setLayoutParams(params);
                                    childParser = childParser2;
                                    try {
                                        rInflateChildren(childParser, view, childAttrs, true);
                                        if (id != -1) {
                                            view.setId(id);
                                        }
                                        if (visibility == 0) {
                                            view.setVisibility(0);
                                        } else if (visibility == 1) {
                                            view.setVisibility(4);
                                        } else if (visibility == 2) {
                                            view.setVisibility(8);
                                        }
                                        group.addView(view);
                                    } catch (Throwable th4) {
                                        th = th4;
                                        childParser.close();
                                        throw th;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    childParser = childParser2;
                                    childParser.close();
                                    throw th;
                                }
                            }
                            childParser.close();
                        } else {
                            throw new InflateException(getParserStateDescription(context2, childAttrs2) + ": No start tag found!");
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        childParser = childParser2;
                        childParser.close();
                        throw th;
                    }
                    while (true) {
                        type = childParser2.next();
                        if (type == 2 || type == 1) {
                            break;
                        }
                    }
                }
                consumeChildElements(parser);
                return;
            }
            String value2 = attrs.getAttributeValue(null, "layout");
            throw new InflateException("You must specify a valid layout reference. The layout ID " + value2 + " is not valid.");
        }
        throw new InflateException("<include /> can only be used inside of a ViewGroup");
    }

    static final void consumeChildElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        int currentDepth = parser.getDepth();
        do {
            type = parser.next();
            if (type == 3 && parser.getDepth() <= currentDepth) {
                return;
            }
        } while (type != 1);
    }

    /* access modifiers changed from: private */
    public static class BlinkLayout extends FrameLayout {
        private static final int BLINK_DELAY = 500;
        private static final int MESSAGE_BLINK = 66;
        private boolean mBlink;
        private boolean mBlinkState;
        private final Handler mHandler = new Handler(new Handler.Callback() {
            /* class android.view.LayoutInflater.BlinkLayout.AnonymousClass1 */

            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                if (msg.what != 66) {
                    return false;
                }
                if (BlinkLayout.this.mBlink) {
                    BlinkLayout blinkLayout = BlinkLayout.this;
                    blinkLayout.mBlinkState = !blinkLayout.mBlinkState;
                    BlinkLayout.this.makeBlink();
                }
                BlinkLayout.this.invalidate();
                return true;
            }
        });

        public BlinkLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void makeBlink() {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(66), 500);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewGroup, android.view.View
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mBlink = true;
            this.mBlinkState = true;
            makeBlink();
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewGroup, android.view.View
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mBlink = false;
            this.mBlinkState = true;
            this.mHandler.removeMessages(66);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewGroup, android.view.View
        public void dispatchDraw(Canvas canvas) {
            if (this.mBlinkState) {
                super.dispatchDraw(canvas);
            }
        }
    }
}
