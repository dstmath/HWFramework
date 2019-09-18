package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class LayoutInflater {
    private static final int[] ATTRS_THEME = {16842752};
    private static final String ATTR_LAYOUT = "layout";
    private static final ClassLoader BOOT_CLASS_LOADER = LayoutInflater.class.getClassLoader();
    private static final boolean DEBUG = false;
    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];
    private static final String TAG = LayoutInflater.class.getSimpleName();
    private static final String TAG_1995 = "blink";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_MERGE = "merge";
    private static final String TAG_REQUEST_FOCUS = "requestFocus";
    private static final String TAG_TAG = "tag";
    static final Class<?>[] mConstructorSignature = {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    final Object[] mConstructorArgs = new Object[2];
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
        /* access modifiers changed from: private */
        public boolean mBlink;
        /* access modifiers changed from: private */
        public boolean mBlinkState;
        private final Handler mHandler = new Handler((Handler.Callback) new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                if (msg.what != 66) {
                    return false;
                }
                if (BlinkLayout.this.mBlink) {
                    boolean unused = BlinkLayout.this.mBlinkState = !BlinkLayout.this.mBlinkState;
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
        public void makeBlink() {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(66), 500);
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mBlink = true;
            this.mBlinkState = true;
            makeBlink();
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mBlink = false;
            this.mBlinkState = true;
            this.mHandler.removeMessages(66);
        }

        /* access modifiers changed from: protected */
        public void dispatchDraw(Canvas canvas) {
            if (this.mBlinkState) {
                super.dispatchDraw(canvas);
            }
        }
    }

    public interface Factory {
        View onCreateView(
/*
Method generation error in method: android.view.LayoutInflater.Factory.onCreateView(java.lang.String, android.content.Context, android.util.AttributeSet):android.view.View, dex: boot-framework_classes2.dex
        jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r1v0 ?
        	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:189)
        	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:157)
        	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:129)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:297)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:49)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        
*/
    }

    public interface Factory2 extends Factory {
        View onCreateView(
/*
Method generation error in method: android.view.LayoutInflater.Factory2.onCreateView(android.view.View, java.lang.String, android.content.Context, android.util.AttributeSet):android.view.View, dex: boot-framework_classes2.dex
        jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r1v0 ?
        	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:189)
        	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:157)
        	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:129)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:297)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:49)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        
*/
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
            View view;
            if (this.mF12 != null) {
                v = this.mF12.onCreateView(parent, name, context, attrs);
            } else {
                v = this.mF1.onCreateView(name, context, attrs);
            }
            if (v != null) {
                return v;
            }
            if (this.mF22 != null) {
                view = this.mF22.onCreateView(parent, name, context, attrs);
            } else {
                view = this.mF2.onCreateView(name, context, attrs);
            }
            return view;
        }
    }

    public interface Filter {
        boolean onLoadClass(
/*
Method generation error in method: android.view.LayoutInflater.Filter.onLoadClass(java.lang.Class):boolean, dex: boot-framework_classes2.dex
        jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r1v0 ?
        	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:189)
        	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:157)
        	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:129)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:297)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:49)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        
*/
    }

    public abstract LayoutInflater cloneInContext(
/*
Method generation error in method: android.view.LayoutInflater.cloneInContext(android.content.Context):android.view.LayoutInflater, dex: boot-framework_classes2.dex
    jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r1v0 ?
    	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:189)
    	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:157)
    	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:129)
    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:297)
    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:49)
    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
    
*/

    protected LayoutInflater(Context context) {
        this.mContext = context;
    }

    protected LayoutInflater(LayoutInflater original, Context newContext) {
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
        } else if (factory != null) {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, null, this.mFactory, this.mFactory2);
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
            if (this.mFactory == null) {
                this.mFactory2 = factory;
                this.mFactory = factory;
                return;
            }
            FactoryMerger factoryMerger = new FactoryMerger(factory, factory, this.mFactory, this.mFactory2);
            this.mFactory2 = factoryMerger;
            this.mFactory = factoryMerger;
        } else {
            throw new NullPointerException("Given factory can not be null");
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
            this.mFilterMap = new HashMap<>();
        }
    }

    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    public View inflate(XmlPullParser parser, ViewGroup root) {
        return inflate(parser, root, root != null);
    }

    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        XmlResourceParser parser = getContext().getResources().getLayout(resource);
        try {
            return inflate((XmlPullParser) parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    /* JADX WARNING: type inference failed for: r10v13 */
    /*  JADX ERROR: IF instruction can be used only in fallback mode
        jadx.core.utils.exceptions.CodegenException: IF instruction can be used only in fallback mode
        	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:568)
        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:474)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:205)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:66)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeSynchronizedRegion(RegionGen.java:260)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:70)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0088, code lost:
        if (r24 == false) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008a, code lost:
        r16 = r1;
        r10 = r10;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0037 A[Catch:{ XmlPullParserException -> 0x00fd, Exception -> 0x00d2, all -> 0x00cd, all -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a9  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:64:0x0111=Splitter:B:64:0x0111, B:36:0x008c=Splitter:B:36:0x008c} */
    public android.view.View inflate(org.xmlpull.v1.XmlPullParser r22, android.view.ViewGroup r23, boolean r24) {
        /*
            r21 = this;
            r7 = r21
            r8 = r23
            java.lang.Object[] r9 = r7.mConstructorArgs
            monitor-enter(r9)
            java.lang.String r0 = "inflate"
            r10 = 8
            android.os.Trace.traceBegin(r10, r0)     // Catch:{ all -> 0x011f }
            android.content.Context r0 = r7.mContext     // Catch:{ all -> 0x011f }
            r12 = r0
            android.util.AttributeSet r0 = android.util.Xml.asAttributeSet(r22)     // Catch:{ all -> 0x011f }
            r13 = r0
            java.lang.Object[] r0 = r7.mConstructorArgs     // Catch:{ all -> 0x011f }
            r14 = 0
            r0 = r0[r14]     // Catch:{ all -> 0x011f }
            android.content.Context r0 = (android.content.Context) r0     // Catch:{ all -> 0x011f }
            r15 = r0
            java.lang.Object[] r0 = r7.mConstructorArgs     // Catch:{ all -> 0x011f }
            r0[r14] = r12     // Catch:{ all -> 0x011f }
            r0 = r8
        L_0x0023:
            r16 = r0
            r17 = 0
            r6 = 1
            int r0 = r22.next()     // Catch:{ XmlPullParserException -> 0x00fd, Exception -> 0x00d2, all -> 0x00cd }
            r5 = r0
            r1 = 2
            if (r0 == r1) goto L_0x0035
            if (r5 == r6) goto L_0x0035
            r0 = r16
            goto L_0x0023
        L_0x0035:
            if (r5 != r1) goto L_0x00a9
            java.lang.String r0 = r22.getName()     // Catch:{ XmlPullParserException -> 0x00fd, Exception -> 0x00d2, all -> 0x00cd }
            java.lang.String r1 = "merge"
            boolean r1 = r1.equals(r0)     // Catch:{ XmlPullParserException -> 0x00fd, Exception -> 0x00d2, all -> 0x00cd }
            if (r1 == 0) goto L_0x0066
            if (r8 == 0) goto L_0x005b
            if (r24 == 0) goto L_0x005b
            r18 = 0
            r1 = r7
            r2 = r22
            r3 = r8
            r4 = r12
            r19 = r5
            r5 = r13
            r10 = r6
            r6 = r18
            r1.rInflate(r2, r3, r4, r5, r6)     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
            r3 = r22
            goto L_0x008c
        L_0x005b:
            r19 = r5
            r10 = r6
            android.view.InflateException r1 = new android.view.InflateException     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
            java.lang.String r2 = "<merge /> can be used only with a valid ViewGroup root and attachToRoot=true"
            r1.<init>((java.lang.String) r2)     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
            throw r1     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
        L_0x0066:
            r19 = r5
            r10 = r6
            android.view.View r1 = r7.createViewFromTag(r8, r0, r12, r13)     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
            r2 = 0
            if (r8 == 0) goto L_0x007a
            android.view.ViewGroup$LayoutParams r3 = r8.generateLayoutParams((android.util.AttributeSet) r13)     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
            r2 = r3
            if (r24 != 0) goto L_0x007a
            r1.setLayoutParams(r2)     // Catch:{ XmlPullParserException -> 0x00a5, Exception -> 0x00a1, all -> 0x009c }
        L_0x007a:
            r3 = r22
            r7.rInflateChildren(r3, r1, r13, r10)     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            if (r8 == 0) goto L_0x0086
            if (r24 == 0) goto L_0x0086
            r8.addView((android.view.View) r1, (android.view.ViewGroup.LayoutParams) r2)     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
        L_0x0086:
            if (r8 == 0) goto L_0x008a
            if (r24 != 0) goto L_0x008c
        L_0x008a:
            r16 = r1
        L_0x008c:
            java.lang.Object[] r0 = r7.mConstructorArgs     // Catch:{ all -> 0x0124 }
            r0[r14] = r15     // Catch:{ all -> 0x0124 }
            java.lang.Object[] r0 = r7.mConstructorArgs     // Catch:{ all -> 0x0124 }
            r0[r10] = r17     // Catch:{ all -> 0x0124 }
            r1 = 8
            android.os.Trace.traceEnd(r1)     // Catch:{ all -> 0x0124 }
            monitor-exit(r9)     // Catch:{ all -> 0x0124 }
            return r16
        L_0x009c:
            r0 = move-exception
            r3 = r22
            goto L_0x0111
        L_0x00a1:
            r0 = move-exception
            r3 = r22
            goto L_0x00d6
        L_0x00a5:
            r0 = move-exception
            r3 = r22
            goto L_0x0101
        L_0x00a9:
            r3 = r22
            r19 = r5
            r10 = r6
            android.view.InflateException r0 = new android.view.InflateException     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            r1.<init>()     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            java.lang.String r2 = r22.getPositionDescription()     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            r1.append(r2)     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            java.lang.String r2 = ": No start tag found!"
            r1.append(r2)     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            java.lang.String r1 = r1.toString()     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            r0.<init>((java.lang.String) r1)     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
            throw r0     // Catch:{ XmlPullParserException -> 0x00cb, Exception -> 0x00c9 }
        L_0x00c9:
            r0 = move-exception
            goto L_0x00d6
        L_0x00cb:
            r0 = move-exception
            goto L_0x0101
        L_0x00cd:
            r0 = move-exception
            r3 = r22
            r10 = r6
            goto L_0x0111
        L_0x00d2:
            r0 = move-exception
            r3 = r22
            r10 = r6
        L_0x00d6:
            android.view.InflateException r1 = new android.view.InflateException     // Catch:{ all -> 0x0110 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0110 }
            r2.<init>()     // Catch:{ all -> 0x0110 }
            java.lang.String r4 = r22.getPositionDescription()     // Catch:{ all -> 0x0110 }
            r2.append(r4)     // Catch:{ all -> 0x0110 }
            java.lang.String r4 = ": "
            r2.append(r4)     // Catch:{ all -> 0x0110 }
            java.lang.String r4 = r0.getMessage()     // Catch:{ all -> 0x0110 }
            r2.append(r4)     // Catch:{ all -> 0x0110 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0110 }
            r1.<init>(r2, r0)     // Catch:{ all -> 0x0110 }
            java.lang.StackTraceElement[] r2 = EMPTY_STACK_TRACE     // Catch:{ all -> 0x0110 }
            r1.setStackTrace(r2)     // Catch:{ all -> 0x0110 }
            throw r1     // Catch:{ all -> 0x0110 }
        L_0x00fd:
            r0 = move-exception
            r3 = r22
            r10 = r6
        L_0x0101:
            android.view.InflateException r1 = new android.view.InflateException     // Catch:{ all -> 0x0110 }
            java.lang.String r2 = r0.getMessage()     // Catch:{ all -> 0x0110 }
            r1.<init>(r2, r0)     // Catch:{ all -> 0x0110 }
            java.lang.StackTraceElement[] r2 = EMPTY_STACK_TRACE     // Catch:{ all -> 0x0110 }
            r1.setStackTrace(r2)     // Catch:{ all -> 0x0110 }
            throw r1     // Catch:{ all -> 0x0110 }
        L_0x0110:
            r0 = move-exception
        L_0x0111:
            java.lang.Object[] r1 = r7.mConstructorArgs     // Catch:{ all -> 0x0124 }
            r1[r14] = r15     // Catch:{ all -> 0x0124 }
            java.lang.Object[] r1 = r7.mConstructorArgs     // Catch:{ all -> 0x0124 }
            r1[r10] = r17     // Catch:{ all -> 0x0124 }
            r1 = 8
            android.os.Trace.traceEnd(r1)     // Catch:{ all -> 0x0124 }
            throw r0     // Catch:{ all -> 0x0124 }
        L_0x011f:
            r0 = move-exception
            r3 = r22
        L_0x0122:
            monitor-exit(r9)     // Catch:{ all -> 0x0124 }
            throw r0
        L_0x0124:
            r0 = move-exception
            goto L_0x0122
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View");
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
        Constructor<? extends U> constructor;
        String str;
        String str2;
        String str3;
        String str4;
        synchronized (sConstructorMap) {
            constructor = sConstructorMap.get(name);
        }
        if (constructor != null && !verifyClassLoader(constructor)) {
            synchronized (sConstructorMap) {
                sConstructorMap.remove(name);
            }
            constructor = null;
        }
        Class<? extends U> cls = null;
        try {
            Trace.traceBegin(8, name);
            if (constructor == null) {
                ClassLoader classLoader = this.mContext.getClassLoader();
                if (prefix != null) {
                    str4 = prefix + name;
                } else {
                    str4 = name;
                }
                cls = classLoader.loadClass(str4).asSubclass(View.class);
                if (!(this.mFilter == null || cls == null || this.mFilter.onLoadClass(cls))) {
                    failNotAllowed(name, prefix, attrs);
                }
                constructor = cls.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                synchronized (sConstructorMap) {
                    sConstructorMap.put(name, constructor);
                }
            } else if (this.mFilter != null) {
                Boolean allowedState = this.mFilterMap.get(name);
                if (allowedState == null) {
                    ClassLoader classLoader2 = this.mContext.getClassLoader();
                    if (prefix != null) {
                        str3 = prefix + name;
                    } else {
                        str3 = name;
                    }
                    Class<? extends U> asSubclass = classLoader2.loadClass(str3).asSubclass(View.class);
                    boolean allowed = asSubclass != null && this.mFilter.onLoadClass(asSubclass);
                    this.mFilterMap.put(name, Boolean.valueOf(allowed));
                    if (!allowed) {
                        failNotAllowed(name, prefix, attrs);
                    }
                } else if (allowedState.equals(Boolean.FALSE)) {
                    failNotAllowed(name, prefix, attrs);
                }
            }
            Object lastContext = this.mConstructorArgs[0];
            if (this.mConstructorArgs[0] == null) {
                this.mConstructorArgs[0] = this.mContext;
            }
            Object[] args = this.mConstructorArgs;
            args[1] = attrs;
            View view = (View) constructor.newInstance(args);
            if (view instanceof ViewStub) {
                ((ViewStub) view).setLayoutInflater(cloneInContext((Context) args[0]));
            }
            this.mConstructorArgs[0] = lastContext;
            Trace.traceEnd(8);
            return view;
        } catch (NoSuchMethodException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(attrs.getPositionDescription());
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
            sb2.append(attrs.getPositionDescription());
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
            try {
                StringBuilder sb3 = new StringBuilder();
                sb3.append(attrs.getPositionDescription());
                sb3.append(": Error inflating class ");
                sb3.append(cls == null ? MediaStore.UNKNOWN_STRING : cls.getName());
                InflateException ie3 = new InflateException(sb3.toString(), e4);
                ie3.setStackTrace(EMPTY_STACK_TRACE);
                throw ie3;
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }
    }

    private void failNotAllowed(String name, String prefix, AttributeSet attrs) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(attrs.getPositionDescription());
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

    private View createViewFromTag(View parent, String name, Context context, AttributeSet attrs) {
        return createViewFromTag(parent, name, context, attrs, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005c A[Catch:{ all -> 0x007d, InflateException -> 0x00cc, ClassNotFoundException -> 0x00a8, Exception -> 0x0084 }] */
    public View createViewFromTag(View parent, String name, Context context, AttributeSet attrs, boolean ignoreThemeAttr) {
        View view;
        Object lastContext;
        View view2;
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
        try {
            if (this.mFactory2 != null) {
                view = this.mFactory2.onCreateView(parent, name, context, attrs);
            } else if (this.mFactory != null) {
                view = this.mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
                if (view == null && this.mPrivateFactory != null) {
                    view = this.mPrivateFactory.onCreateView(parent, name, context, attrs);
                }
                if (view == null) {
                    lastContext = this.mConstructorArgs[0];
                    this.mConstructorArgs[0] = context;
                    if (-1 == name.indexOf(46)) {
                        view2 = onCreateView(parent, name, attrs);
                    } else {
                        view2 = createView(name, null, attrs);
                    }
                    view = view2;
                    this.mConstructorArgs[0] = lastContext;
                }
                return view;
            }
            view = this.mPrivateFactory.onCreateView(parent, name, context, attrs);
            if (view == null) {
            }
            return view;
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e2);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (Exception e3) {
            InflateException ie2 = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e3);
            ie2.setStackTrace(EMPTY_STACK_TRACE);
            throw ie2;
        } catch (Throwable th) {
            this.mConstructorArgs[0] = lastContext;
            throw th;
        }
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
            int next = parser.next();
            int type = next;
            if ((next != 3 || parser.getDepth() > depth) && type != 1) {
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

    /* JADX WARNING: Removed duplicated region for block: B:35:0x009a A[Catch:{ all -> 0x0163 }] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0141  */
    private void parseInclude(XmlPullParser parser, Context context, View parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        int type;
        int type2;
        View view;
        ViewGroup group;
        int id;
        int visibility;
        AttributeSet childAttrs;
        ViewGroup.LayoutParams params;
        ContextThemeWrapper contextThemeWrapper = context;
        View view2 = parent;
        AttributeSet attributeSet = attrs;
        if (view2 instanceof ViewGroup) {
            TypedArray ta = contextThemeWrapper.obtainStyledAttributes(attributeSet, ATTRS_THEME);
            int themeResId = ta.getResourceId(0, 0);
            boolean hasThemeOverride = themeResId != 0;
            if (hasThemeOverride) {
                contextThemeWrapper = new ContextThemeWrapper(contextThemeWrapper, themeResId);
            }
            Context context2 = contextThemeWrapper;
            ta.recycle();
            int layout = attributeSet.getAttributeResourceValue(null, ATTR_LAYOUT, 0);
            if (layout == 0) {
                String value = attributeSet.getAttributeValue(null, ATTR_LAYOUT);
                if (value == null || value.length() <= 0) {
                    throw new InflateException("You must specify a layout in the include tag: <include layout=\"@layout/layoutID\" />");
                }
                layout = context2.getResources().getIdentifier(value.substring(1), "attr", context2.getPackageName());
            }
            if (this.mTempValue == null) {
                this.mTempValue = new TypedValue();
            }
            if (layout != 0 && context2.getTheme().resolveAttribute(layout, this.mTempValue, true)) {
                layout = this.mTempValue.resourceId;
            }
            int layout2 = layout;
            if (layout2 != 0) {
                XmlResourceParser childParser = context2.getResources().getLayout(layout2);
                try {
                    AttributeSet childAttrs2 = Xml.asAttributeSet(childParser);
                    while (true) {
                        AttributeSet childAttrs3 = childAttrs2;
                        int next = childParser.next();
                        type = next;
                        if (next != 2 && type != 1) {
                            childAttrs2 = childAttrs3;
                        } else if (type != 2) {
                            String childName = childParser.getName();
                            if (TAG_MERGE.equals(childName)) {
                                String str = childName;
                                type2 = type;
                                XmlResourceParser childParser2 = childParser;
                                int i = layout2;
                                try {
                                    rInflate(childParser, view2, context2, childAttrs3, false);
                                    childParser = childParser2;
                                } catch (Throwable th) {
                                    th = th;
                                    childParser = childParser2;
                                    childParser.close();
                                    throw th;
                                }
                            } else {
                                type2 = type;
                                int i2 = layout2;
                                AttributeSet childAttrs4 = childAttrs3;
                                XmlResourceParser childParser3 = childParser;
                                try {
                                    view = createViewFromTag(view2, childName, context2, childAttrs4, hasThemeOverride);
                                    group = (ViewGroup) view2;
                                    TypedArray a = context2.obtainStyledAttributes(attributeSet, R.styleable.Include);
                                    id = a.getResourceId(0, -1);
                                    visibility = a.getInt(1, -1);
                                    a.recycle();
                                    ViewGroup.LayoutParams params2 = null;
                                    try {
                                        params2 = group.generateLayoutParams(attributeSet);
                                    } catch (RuntimeException e) {
                                    }
                                    if (params2 == null) {
                                        childAttrs = childAttrs4;
                                        try {
                                            params = group.generateLayoutParams(childAttrs);
                                        } catch (Throwable th2) {
                                            th = th2;
                                            childParser = childParser3;
                                            childParser.close();
                                            throw th;
                                        }
                                    } else {
                                        childAttrs = childAttrs4;
                                        params = params2;
                                    }
                                    view.setLayoutParams(params);
                                    childParser = childParser3;
                                } catch (Throwable th3) {
                                    th = th3;
                                    childParser = childParser3;
                                    childParser.close();
                                    throw th;
                                }
                                try {
                                    rInflateChildren(childParser, view, childAttrs, true);
                                    if (id != -1) {
                                        view.setId(id);
                                    }
                                    switch (visibility) {
                                        case 0:
                                            view.setVisibility(0);
                                            break;
                                        case 1:
                                            view.setVisibility(4);
                                            break;
                                        case 2:
                                            view.setVisibility(8);
                                            break;
                                    }
                                    group.addView(view);
                                } catch (Throwable th4) {
                                    th = th4;
                                    childParser.close();
                                    throw th;
                                }
                            }
                            childParser.close();
                            int i3 = type2;
                            consumeChildElements(parser);
                            return;
                        } else {
                            int i4 = type;
                            AttributeSet attributeSet2 = childAttrs3;
                            int i5 = layout2;
                            throw new InflateException(childParser.getPositionDescription() + ": No start tag found!");
                        }
                    }
                    if (type != 2) {
                    }
                } catch (Throwable th5) {
                    th = th5;
                    int i6 = layout2;
                    childParser.close();
                    throw th;
                }
            } else {
                String value2 = attributeSet.getAttributeValue(null, ATTR_LAYOUT);
                throw new InflateException("You must specify a valid layout reference. The layout ID " + value2 + " is not valid.");
            }
        } else {
            throw new InflateException("<include /> can only be used inside of a ViewGroup");
        }
    }

    static final void consumeChildElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        int currentDepth = parser.getDepth();
        do {
            int next = parser.next();
            type = next;
            if (next == 3 && parser.getDepth() <= currentDepth) {
                return;
            }
        } while (type != 1);
    }
}
