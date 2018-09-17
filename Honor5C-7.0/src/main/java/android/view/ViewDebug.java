package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.SettingsEx.Systemex;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.PtmLog;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import com.huawei.pgmng.log.LogPower;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.microedition.khronos.opengles.GL10;

public class ViewDebug {
    private static final int CAPTURE_TIMEOUT = 4000;
    public static final boolean DEBUG_DRAG = false;
    public static final boolean DEBUG_POSITIONING = false;
    private static final String DUMPC_CUST_FIELDSLIST = "dumpc_cust_fields";
    private static final String DUMPC_CUST_METHODSLIST = "dumpc_cust_methods";
    private static final String REMOTE_COMMAND_CAPTURE = "CAPTURE";
    private static final String REMOTE_COMMAND_CAPTURE_LAYERS = "CAPTURE_LAYERS";
    private static final String REMOTE_COMMAND_DUMP = "DUMP";
    private static String REMOTE_COMMAND_DUMP_CUST = null;
    private static final String REMOTE_COMMAND_DUMP_THEME = "DUMP_THEME";
    private static final String REMOTE_COMMAND_INVALIDATE = "INVALIDATE";
    private static final String REMOTE_COMMAND_OUTPUT_DISPLAYLIST = "OUTPUT_DISPLAYLIST";
    private static final String REMOTE_COMMAND_REQUEST_LAYOUT = "REQUEST_LAYOUT";
    private static final String REMOTE_PROFILE = "PROFILE";
    @Deprecated
    public static final boolean TRACE_HIERARCHY = false;
    @Deprecated
    public static final boolean TRACE_RECYCLER = false;
    private static HashMap<Class<?>, Field[]> mCapturedViewFieldsForClasses;
    private static HashMap<Class<?>, Method[]> mCapturedViewMethodsForClasses;
    private static boolean mCustomizedDump;
    private static String[] mFieldsList;
    private static boolean mInited;
    private static String[] mMethodsList;
    private static HashMap<AccessibleObject, ExportedProperty> sAnnotations;
    private static HashMap<Class<?>, Field[]> sCustFieldsForClasses;
    private static HashMap<Class<?>, Method[]> sCustMethodsForClasses;
    private static HashMap<Class<?>, Field[]> sFieldsForClasses;
    private static HashMap<Class<?>, Method[]> sMethodsForClasses;

    /* renamed from: android.view.ViewDebug.10 */
    static class AnonymousClass10 implements Runnable {
        final /* synthetic */ LayoutParams val$p;
        final /* synthetic */ View val$view;

        AnonymousClass10(View val$view, LayoutParams val$p) {
            this.val$view = val$view;
            this.val$p = val$p;
        }

        public void run() {
            this.val$view.setLayoutParams(this.val$p);
        }
    }

    /* renamed from: android.view.ViewDebug.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ View val$view;

        AnonymousClass1(View val$view) {
            this.val$view = val$view;
        }

        public void run() {
            this.val$view.requestLayout();
        }
    }

    interface ViewOperation<T> {
        void post(T... tArr);

        T[] pre();

        void run(T... tArr);
    }

    /* renamed from: android.view.ViewDebug.2 */
    static class AnonymousClass2 implements ViewOperation<Void> {
        final /* synthetic */ View val$view;

        AnonymousClass2(View val$view) {
            this.val$view = val$view;
        }

        public Void[] pre() {
            forceLayout(this.val$view);
            return null;
        }

        private void forceLayout(View view) {
            view.forceLayout();
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                int count = group.getChildCount();
                for (int i = 0; i < count; i++) {
                    forceLayout(group.getChildAt(i));
                }
            }
        }

        public void run(Void... data) {
            this.val$view.measure(this.val$view.mOldWidthMeasureSpec, this.val$view.mOldHeightMeasureSpec);
        }

        public void post(Void... data) {
        }
    }

    /* renamed from: android.view.ViewDebug.3 */
    static class AnonymousClass3 implements ViewOperation<Void> {
        final /* synthetic */ View val$view;

        AnonymousClass3(View val$view) {
            this.val$view = val$view;
        }

        public Void[] pre() {
            return null;
        }

        public void run(Void... data) {
            this.val$view.layout(this.val$view.mLeft, this.val$view.mTop, this.val$view.mRight, this.val$view.mBottom);
        }

        public void post(Void... data) {
        }
    }

    /* renamed from: android.view.ViewDebug.4 */
    static class AnonymousClass4 implements ViewOperation<Object> {
        final /* synthetic */ View val$view;

        AnonymousClass4(View val$view) {
            this.val$view = val$view;
        }

        public Object[] pre() {
            DisplayMetrics metrics;
            Bitmap bitmap = null;
            if (this.val$view == null || this.val$view.getResources() == null) {
                metrics = null;
            } else {
                metrics = this.val$view.getResources().getDisplayMetrics();
            }
            if (metrics != null) {
                bitmap = Bitmap.createBitmap(metrics, metrics.widthPixels, metrics.heightPixels, Config.RGB_565);
            }
            Canvas canvas = bitmap != null ? new Canvas(bitmap) : null;
            return new Object[]{bitmap, canvas};
        }

        public void run(Object... data) {
            if (data[1] != null) {
                this.val$view.draw((Canvas) data[1]);
            }
        }

        public void post(Object... data) {
            if (data[1] != null) {
                ((Canvas) data[1]).setBitmap(null);
            }
            if (data[0] != null) {
                ((Bitmap) data[0]).recycle();
            }
        }
    }

    /* renamed from: android.view.ViewDebug.5 */
    static class AnonymousClass5 implements Runnable {
        final /* synthetic */ long[] val$duration;
        final /* synthetic */ CountDownLatch val$latch;
        final /* synthetic */ ViewOperation val$operation;

        AnonymousClass5(CountDownLatch val$latch, ViewOperation val$operation, long[] val$duration) {
            this.val$latch = val$latch;
            this.val$operation = val$operation;
            this.val$duration = val$duration;
        }

        public void run() {
            try {
                T[] data = this.val$operation.pre();
                long start = Debug.threadCpuTimeNanos();
                this.val$operation.run(data);
                this.val$duration[0] = Debug.threadCpuTimeNanos() - start;
                this.val$operation.post(data);
            } finally {
                this.val$latch.countDown();
            }
        }
    }

    /* renamed from: android.view.ViewDebug.6 */
    static class AnonymousClass6 implements Runnable {
        final /* synthetic */ Bitmap[] val$cache;
        final /* synthetic */ View val$captureView;
        final /* synthetic */ CountDownLatch val$latch;
        final /* synthetic */ boolean val$skipChildren;

        AnonymousClass6(CountDownLatch val$latch, Bitmap[] val$cache, View val$captureView, boolean val$skipChildren) {
            this.val$latch = val$latch;
            this.val$cache = val$cache;
            this.val$captureView = val$captureView;
            this.val$skipChildren = val$skipChildren;
        }

        public void run() {
            try {
                this.val$cache[0] = this.val$captureView.createSnapshot(Config.ARGB_8888, 0, this.val$skipChildren);
            } catch (OutOfMemoryError e) {
                Log.w("View", "Out of memory for bitmap");
            } finally {
                this.val$latch.countDown();
            }
        }
    }

    /* renamed from: android.view.ViewDebug.7 */
    static class AnonymousClass7 implements Runnable {
        final /* synthetic */ ViewHierarchyEncoder val$encoder;
        final /* synthetic */ CountDownLatch val$latch;
        final /* synthetic */ View val$view;

        AnonymousClass7(View val$view, ViewHierarchyEncoder val$encoder, CountDownLatch val$latch) {
            this.val$view = val$view;
            this.val$encoder = val$encoder;
            this.val$latch = val$latch;
        }

        public void run() {
            this.val$view.encode(this.val$encoder);
            this.val$latch.countDown();
        }
    }

    /* renamed from: android.view.ViewDebug.8 */
    static class AnonymousClass8 implements Callable<Object> {
        final /* synthetic */ Method val$method;
        final /* synthetic */ View val$view;

        AnonymousClass8(Method val$method, View val$view) {
            this.val$method = val$method;
            this.val$view = val$view;
        }

        public Object call() throws IllegalAccessException, InvocationTargetException {
            return this.val$method.invoke(this.val$view, (Object[]) null);
        }
    }

    /* renamed from: android.view.ViewDebug.9 */
    static class AnonymousClass9 implements Runnable {
        final /* synthetic */ Object[] val$args;
        final /* synthetic */ AtomicReference val$exception;
        final /* synthetic */ CountDownLatch val$latch;
        final /* synthetic */ Method val$method;
        final /* synthetic */ AtomicReference val$result;
        final /* synthetic */ View val$view;

        AnonymousClass9(AtomicReference val$result, Method val$method, View val$view, Object[] val$args, AtomicReference val$exception, CountDownLatch val$latch) {
            this.val$result = val$result;
            this.val$method = val$method;
            this.val$view = val$view;
            this.val$args = val$args;
            this.val$exception = val$exception;
            this.val$latch = val$latch;
        }

        public void run() {
            try {
                this.val$result.set(this.val$method.invoke(this.val$view, this.val$args));
            } catch (InvocationTargetException e) {
                this.val$exception.set(e.getCause());
            } catch (Exception e2) {
                this.val$exception.set(e2);
            }
            this.val$latch.countDown();
        }
    }

    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CapturedViewProperty {
        boolean retrieveReturn() default false;
    }

    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExportedProperty {
        String category() default "";

        boolean deepExport() default false;

        FlagToString[] flagMapping() default {};

        boolean formatToHexString() default false;

        boolean hasAdjacentMapping() default false;

        IntToString[] indexMapping() default {};

        IntToString[] mapping() default {};

        String prefix() default "";

        boolean resolveId() default false;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FlagToString {
        int equals();

        int mask();

        String name();

        boolean outputIf() default true;
    }

    public interface HierarchyHandler {
        void dumpViewHierarchyWithProperties(BufferedWriter bufferedWriter, int i);

        View findHierarchyView(String str, int i);
    }

    @Deprecated
    public enum HierarchyTraceType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewDebug.HierarchyTraceType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewDebug.HierarchyTraceType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewDebug.HierarchyTraceType.<clinit>():void");
        }
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IntToString {
        int from();

        String to();
    }

    @Deprecated
    public enum RecyclerTraceType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewDebug.RecyclerTraceType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewDebug.RecyclerTraceType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewDebug.RecyclerTraceType.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewDebug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewDebug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewDebug.<clinit>():void");
    }

    public ViewDebug() {
    }

    public static long getViewInstanceCount() {
        return Debug.countInstancesOfClass(View.class);
    }

    public static long getViewRootImplCount() {
        return Debug.countInstancesOfClass(ViewRootImpl.class);
    }

    @Deprecated
    public static void trace(View view, RecyclerTraceType type, int... parameters) {
    }

    @Deprecated
    public static void startRecyclerTracing(String prefix, View view) {
    }

    @Deprecated
    public static void stopRecyclerTracing() {
    }

    @Deprecated
    public static void trace(View view, HierarchyTraceType type) {
    }

    @Deprecated
    public static void startHierarchyTracing(String prefix, View view) {
    }

    @Deprecated
    public static void stopHierarchyTracing() {
    }

    static void dispatchCommand(View view, String command, String parameters, OutputStream clientStream) throws IOException {
        view = view.getRootView();
        if (REMOTE_COMMAND_DUMP_CUST.equalsIgnoreCase(command)) {
            customizedDump(view, clientStream);
        } else if (REMOTE_COMMAND_DUMP.equalsIgnoreCase(command)) {
            dump(view, DEBUG_POSITIONING, true, clientStream);
        } else if (REMOTE_COMMAND_DUMP_THEME.equalsIgnoreCase(command)) {
            dumpTheme(view, clientStream);
        } else if (REMOTE_COMMAND_CAPTURE_LAYERS.equalsIgnoreCase(command)) {
            captureLayers(view, new DataOutputStream(clientStream));
        } else {
            String[] params = parameters.split(" ");
            if (REMOTE_COMMAND_CAPTURE.equalsIgnoreCase(command)) {
                capture(view, clientStream, params[0]);
            } else if (REMOTE_COMMAND_OUTPUT_DISPLAYLIST.equalsIgnoreCase(command)) {
                outputDisplayList(view, params[0]);
            } else if (REMOTE_COMMAND_INVALIDATE.equalsIgnoreCase(command)) {
                invalidate(view, params[0]);
            } else if (REMOTE_COMMAND_REQUEST_LAYOUT.equalsIgnoreCase(command)) {
                requestLayout(view, params[0]);
            } else if (REMOTE_PROFILE.equalsIgnoreCase(command)) {
                profile(view, clientStream, params[0]);
            }
        }
    }

    public static View findView(View root, String parameter) {
        if (parameter.indexOf(64) != -1) {
            String[] ids = parameter.split("@");
            String className = ids[0];
            int hashCode = (int) Long.parseLong(ids[1], 16);
            View view = root.getRootView();
            if (view instanceof ViewGroup) {
                return findView((ViewGroup) view, className, hashCode);
            }
            return null;
        }
        return root.getRootView().findViewById(root.getResources().getIdentifier(parameter, null, null));
    }

    private static void invalidate(View root, String parameter) {
        View view = findView(root, parameter);
        if (view != null) {
            view.postInvalidate();
        }
    }

    private static void requestLayout(View root, String parameter) {
        View view = findView(root, parameter);
        if (view != null) {
            root.post(new AnonymousClass1(view));
        }
    }

    private static void profile(View root, OutputStream clientStream, String parameter) throws IOException {
        Exception e;
        Throwable th;
        View view = findView(root, parameter);
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientStream), AccessibilityNodeInfo.ACTION_PASTE);
            if (view != null) {
                try {
                    profileViewAndChildren(view, out);
                } catch (Exception e2) {
                    e = e2;
                    bufferedWriter = out;
                    try {
                        Log.w("View", "Problem profiling the view:", e);
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedWriter = out;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            }
            out.write("-1 -1 -1");
            out.newLine();
            out.write("DONE.");
            out.newLine();
            if (out != null) {
                out.close();
            }
            bufferedWriter = out;
        } catch (Exception e3) {
            e = e3;
            Log.w("View", "Problem profiling the view:", e);
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

    public static void profileViewAndChildren(View view, BufferedWriter out) throws IOException {
        profileViewAndChildren(view, out, true);
    }

    private static void profileViewAndChildren(View view, BufferedWriter out, boolean root) throws IOException {
        long durationMeasure;
        long durationLayout;
        long durationDraw;
        if (root || (view.mPrivateFlags & GL10.GL_EXP) != 0) {
            durationMeasure = profileViewOperation(view, new AnonymousClass2(view));
        } else {
            durationMeasure = 0;
        }
        if (root || (view.mPrivateFlags & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != 0) {
            durationLayout = profileViewOperation(view, new AnonymousClass3(view));
        } else {
            durationLayout = 0;
        }
        if (!root && view.willNotDraw() && (view.mPrivateFlags & 32) == 0) {
            durationDraw = 0;
        } else {
            durationDraw = profileViewOperation(view, new AnonymousClass4(view));
        }
        out.write(String.valueOf(durationMeasure));
        out.write(32);
        out.write(String.valueOf(durationLayout));
        out.write(32);
        out.write(String.valueOf(durationDraw));
        out.newLine();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                profileViewAndChildren(group.getChildAt(i), out, DEBUG_POSITIONING);
            }
        }
    }

    private static <T> long profileViewOperation(View view, ViewOperation<T> operation) {
        CountDownLatch latch = new CountDownLatch(1);
        long[] duration = new long[1];
        view.post(new AnonymousClass5(latch, operation, duration));
        try {
            if (latch.await(4000, TimeUnit.MILLISECONDS)) {
                return duration[0];
            }
            Log.w("View", "Could not complete the profiling of the view " + view);
            return -1;
        } catch (InterruptedException e) {
            Log.w("View", "Could not complete the profiling of the view " + view);
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public static void captureLayers(View root, DataOutputStream clientStream) throws IOException {
        try {
            Rect outRect = new Rect();
            try {
                root.mAttachInfo.mSession.getDisplayFrame(root.mAttachInfo.mWindow, outRect);
            } catch (RemoteException e) {
            }
            clientStream.writeInt(outRect.width());
            clientStream.writeInt(outRect.height());
            captureViewLayer(root, clientStream, true);
            clientStream.write(2);
        } finally {
            clientStream.close();
        }
    }

    private static void captureViewLayer(View view, DataOutputStream clientStream, boolean visible) throws IOException {
        boolean z = view.getVisibility() == 0 ? visible : DEBUG_POSITIONING;
        if ((view.mPrivateFlags & LogPower.START_CHG_ROTATION) != LogPower.START_CHG_ROTATION) {
            int id = view.getId();
            String name = view.getClass().getSimpleName();
            if (id != -1) {
                name = resolveId(view.getContext(), id).toString();
            }
            clientStream.write(1);
            clientStream.writeUTF(name);
            clientStream.writeByte(z ? 1 : 0);
            int[] position = new int[2];
            view.getLocationInWindow(position);
            clientStream.writeInt(position[0]);
            clientStream.writeInt(position[1]);
            clientStream.flush();
            Bitmap b = performViewCapture(view, true);
            if (b != null) {
                ByteArrayOutputStream arrayOut = new ByteArrayOutputStream((b.getWidth() * b.getHeight()) * 2);
                b.compress(CompressFormat.PNG, 100, arrayOut);
                clientStream.writeInt(arrayOut.size());
                arrayOut.writeTo(clientStream);
            }
            clientStream.flush();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                captureViewLayer(group.getChildAt(i), clientStream, z);
            }
        }
        if (view.mOverlay != null) {
            captureViewLayer(view.getOverlay().mOverlayViewGroup, clientStream, z);
        }
    }

    private static void outputDisplayList(View root, String parameter) throws IOException {
        View view = findView(root, parameter);
        view.getViewRootImpl().outputDisplayList(view);
    }

    public static void outputDisplayList(View root, View target) {
        root.getViewRootImpl().outputDisplayList(target);
    }

    private static void capture(View root, OutputStream clientStream, String parameter) throws IOException {
        capture(root, clientStream, findView(root, parameter));
    }

    public static void capture(View root, OutputStream clientStream, View captureView) throws IOException {
        Throwable th;
        Bitmap b = performViewCapture(captureView, DEBUG_POSITIONING);
        if (b == null) {
            Log.w("View", "Failed to create capture bitmap!");
            b = Bitmap.createBitmap(root.getResources().getDisplayMetrics(), 1, 1, Config.ARGB_8888);
        }
        BufferedOutputStream bufferedOutputStream = null;
        try {
            BufferedOutputStream out = new BufferedOutputStream(clientStream, AccessibilityNodeInfo.ACTION_PASTE);
            try {
                b.compress(CompressFormat.PNG, 100, out);
                out.flush();
                if (out != null) {
                    out.close();
                }
                b.recycle();
            } catch (Throwable th2) {
                th = th2;
                bufferedOutputStream = out;
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                b.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            b.recycle();
            throw th;
        }
    }

    private static Bitmap performViewCapture(View captureView, boolean skipChildren) {
        if (captureView != null) {
            CountDownLatch latch = new CountDownLatch(1);
            Bitmap[] cache = new Bitmap[1];
            captureView.post(new AnonymousClass6(latch, cache, captureView, skipChildren));
            try {
                latch.await(4000, TimeUnit.MILLISECONDS);
                return cache[0];
            } catch (InterruptedException e) {
                Log.w("View", "Could not complete the capture of the view " + captureView);
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public static void dump(View root, boolean skipChildren, boolean includeProperties, OutputStream clientStream) throws IOException {
        Exception e;
        Throwable th;
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), AccessibilityNodeInfo.ACTION_PASTE);
            try {
                View view = root.getRootView();
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    dumpViewHierarchy(group.getContext(), group, bufferedWriter, 0, skipChildren, includeProperties);
                }
                bufferedWriter.write("DONE.");
                bufferedWriter.newLine();
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (Exception e2) {
                e = e2;
                try {
                    Log.w("View", "Problem dumping the view:", e);
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            }
        } catch (Exception e3) {
            e = e3;
            bufferedWriter = null;
            Log.w("View", "Problem dumping the view:", e);
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedWriter = null;
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            throw th;
        }
    }

    public static void dumpv2(View view, ByteArrayOutputStream out) throws InterruptedException {
        ViewHierarchyEncoder encoder = new ViewHierarchyEncoder(out);
        CountDownLatch latch = new CountDownLatch(1);
        view.post(new AnonymousClass7(view, encoder, latch));
        latch.await(2, TimeUnit.SECONDS);
        encoder.endStream();
    }

    public static void dumpTheme(View view, OutputStream clientStream) throws IOException {
        Exception e;
        Throwable th;
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), AccessibilityNodeInfo.ACTION_PASTE);
            try {
                String[] attributes = getStyleAttributesDump(view.getContext().getResources(), view.getContext().getTheme());
                if (attributes != null) {
                    for (int i = 0; i < attributes.length; i += 2) {
                        if (attributes[i] != null) {
                            out.write(attributes[i] + "\n");
                            out.write(attributes[i + 1] + "\n");
                        }
                    }
                }
                out.write("DONE.");
                out.newLine();
                if (out != null) {
                    out.close();
                }
                bufferedWriter = out;
            } catch (Exception e2) {
                e = e2;
                bufferedWriter = out;
                try {
                    Log.w("View", "Problem dumping View Theme:", e);
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedWriter = out;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.w("View", "Problem dumping View Theme:", e);
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

    private static String[] getStyleAttributesDump(Resources resources, Theme theme) {
        TypedValue outValue = new TypedValue();
        String nullString = "null";
        int i = 0;
        int[] attributes = theme.getAllAttributes();
        String[] data = new String[(attributes.length * 2)];
        for (int attributeId : attributes) {
            try {
                String charSequence;
                data[i] = resources.getResourceName(attributeId);
                int i2 = i + 1;
                if (theme.resolveAttribute(attributeId, outValue, true)) {
                    charSequence = outValue.coerceToString().toString();
                } else {
                    charSequence = nullString;
                }
                data[i2] = charSequence;
                i += 2;
                if (outValue.type == 1) {
                    data[i - 1] = resources.getResourceName(outValue.resourceId);
                }
            } catch (NotFoundException e) {
            }
        }
        return data;
    }

    private static View findView(ViewGroup group, String className, int hashCode) {
        if (isRequestedView(group, className, hashCode)) {
            return group;
        }
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View found;
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                found = findView((ViewGroup) view, className, hashCode);
                if (found != null) {
                    return found;
                }
            } else if (isRequestedView(view, className, hashCode)) {
                return view;
            }
            if (view.mOverlay != null) {
                found = findView(view.mOverlay.mOverlayViewGroup, className, hashCode);
                if (found != null) {
                    return found;
                }
            }
            if (view instanceof HierarchyHandler) {
                found = ((HierarchyHandler) view).findHierarchyView(className, hashCode);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean isRequestedView(View view, String className, int hashCode) {
        if (view.hashCode() != hashCode) {
            return DEBUG_POSITIONING;
        }
        String viewClassName = view.getClass().getName();
        if (className.equals("ViewOverlay")) {
            return viewClassName.equals("android.view.ViewOverlay$OverlayViewGroup");
        }
        return className.equals(viewClassName);
    }

    private static void dumpViewHierarchy(Context context, ViewGroup group, BufferedWriter out, int level, boolean skipChildren, boolean includeProperties) {
        if (dumpView(context, group, out, level, includeProperties) && !skipChildren) {
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = group.getChildAt(i);
                if (view instanceof ViewGroup) {
                    dumpViewHierarchy(context, (ViewGroup) view, out, level + 1, skipChildren, includeProperties);
                } else {
                    dumpView(context, view, out, level + 1, includeProperties);
                }
                if (view.mOverlay != null) {
                    dumpViewHierarchy(context, view.getOverlay().mOverlayViewGroup, out, level + 2, skipChildren, includeProperties);
                }
            }
            if (group instanceof HierarchyHandler) {
                ((HierarchyHandler) group).dumpViewHierarchyWithProperties(out, level + 1);
            }
        }
    }

    private static boolean dumpView(Context context, View view, BufferedWriter out, int level, boolean includeProperties) {
        int i = 0;
        while (i < level) {
            try {
                out.write(32);
                i++;
            } catch (IOException e) {
                Log.w("View", "Error while dumping hierarchy tree");
                return DEBUG_POSITIONING;
            }
        }
        String className = view.getClass().getName();
        if (className.equals("android.view.ViewOverlay$OverlayViewGroup")) {
            className = "ViewOverlay";
        }
        out.write(className);
        out.write(64);
        out.write(Integer.toHexString(view.hashCode()));
        out.write(32);
        if (includeProperties) {
            dumpViewProperties(context, view, out);
        }
        out.newLine();
        return true;
    }

    private static Field[] getExportedPropertyFields(Class<?> klass) {
        if (sFieldsForClasses == null) {
            sFieldsForClasses = new HashMap();
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap(GL10.GL_NEVER);
        }
        HashMap<Class<?>, Field[]> map = getFieldsMap();
        Field[] fields = (Field[]) map.get(klass);
        if (fields != null) {
            return fields;
        }
        try {
            Field[] declaredFields = getFields(klass);
            ArrayList<Field> foundFields = new ArrayList();
            for (Field field : declaredFields) {
                if (field.getType() != null && field.isAnnotationPresent(ExportedProperty.class)) {
                    field.setAccessible(true);
                    foundFields.add(field);
                    sAnnotations.put(field, (ExportedProperty) field.getAnnotation(ExportedProperty.class));
                }
            }
            fields = (Field[]) foundFields.toArray(new Field[foundFields.size()]);
            map.put(klass, fields);
            return fields;
        } catch (NoClassDefFoundError e) {
            throw new AssertionError(e);
        }
    }

    private static Method[] getExportedPropertyMethods(Class<?> klass) {
        if (sMethodsForClasses == null) {
            sMethodsForClasses = new HashMap(100);
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap(GL10.GL_NEVER);
        }
        HashMap<Class<?>, Method[]> map = getMethodMap();
        Method[] methods = (Method[]) map.get(klass);
        if (methods != null) {
            return methods;
        }
        methods = klass.getDeclaredMethodsUnchecked(DEBUG_POSITIONING);
        ArrayList<Method> foundMethods = new ArrayList();
        for (Method method : getMethod(klass)) {
            try {
                method.getReturnType();
                method.getParameterTypes();
                if (method.getParameterTypes().length == 0 && method.isAnnotationPresent(ExportedProperty.class) && method.getReturnType() != Void.class) {
                    method.setAccessible(true);
                    foundMethods.add(method);
                    sAnnotations.put(method, (ExportedProperty) method.getAnnotation(ExportedProperty.class));
                }
            } catch (NoClassDefFoundError e) {
            }
        }
        methods = (Method[]) foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods);
        return methods;
    }

    private static void dumpViewProperties(Context context, Object view, BufferedWriter out) throws IOException {
        dumpViewProperties(context, view, out, "");
    }

    private static void dumpViewProperties(Context context, Object view, BufferedWriter out, String prefix) throws IOException {
        if (view == null) {
            out.write(prefix + "=4,null ");
            return;
        }
        Class<?> klass = view.getClass();
        do {
            exportFields(context, view, out, klass, prefix);
            exportMethods(context, view, out, klass, prefix);
            klass = klass.getSuperclass();
        } while (klass != Object.class);
    }

    private static Object callMethodOnAppropriateTheadBlocking(Method method, Object object) throws IllegalAccessException, InvocationTargetException, TimeoutException {
        if (!(object instanceof View)) {
            return method.invoke(object, (Object[]) null);
        }
        View view = (View) object;
        FutureTask<Object> future = new FutureTask(new AnonymousClass8(method, view));
        Handler handler = view.getHandler();
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(future);
        while (true) {
            try {
                return future.get(4000, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof IllegalAccessException) {
                    throw ((IllegalAccessException) t);
                } else if (t instanceof InvocationTargetException) {
                    throw ((InvocationTargetException) t);
                } else {
                    throw new RuntimeException("Unexpected exception", t);
                }
            } catch (InterruptedException e2) {
            } catch (CancellationException e3) {
                throw new RuntimeException("Unexpected cancellation exception", e3);
            }
        }
    }

    private static String formatIntToHexString(int value) {
        return "0x" + Integer.toHexString(value).toUpperCase();
    }

    private static void exportMethods(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        for (Method method : getExportedPropertyMethods(klass)) {
            try {
                Object methodValue = callMethodOnAppropriateTheadBlocking(method, view);
                Class<?> returnType = method.getReturnType();
                ExportedProperty property = (ExportedProperty) sAnnotations.get(method);
                String categoryPrefix = property.category().length() != 0 ? property.category() + ":" : "";
                int j;
                if (returnType != Integer.TYPE) {
                    if (returnType == int[].class) {
                        String suffix = "()";
                        exportUnrolledArray(context, out, property, (int[]) methodValue, categoryPrefix + prefix + method.getName() + '_', "()");
                    } else if (returnType == String[].class) {
                        String[] array = (String[]) methodValue;
                        if (property.hasAdjacentMapping() && array != null) {
                            for (j = 0; j < array.length; j += 2) {
                                if (array[j] != null) {
                                    writeEntry(out, categoryPrefix + prefix, array[j], "()", array[j + 1] == null ? "null" : array[j + 1]);
                                }
                            }
                        }
                    } else if (!returnType.isPrimitive() && property.deepExport()) {
                        dumpViewProperties(context, methodValue, out, prefix + property.prefix());
                    }
                } else if (!property.resolveId() || context == null) {
                    FlagToString[] flagsMapping = property.flagMapping();
                    if (flagsMapping.length > 0) {
                        exportUnrolledFlags(out, flagsMapping, ((Integer) methodValue).intValue(), categoryPrefix + prefix + method.getName() + '_');
                    }
                    IntToString[] mapping = property.mapping();
                    if (mapping.length > 0) {
                        int intValue = ((Integer) methodValue).intValue();
                        boolean mapped = DEBUG_POSITIONING;
                        for (IntToString mapper : mapping) {
                            if (mapper.from() == intValue) {
                                methodValue = mapper.to();
                                mapped = true;
                                break;
                            }
                        }
                        if (!mapped) {
                            methodValue = Integer.valueOf(intValue);
                        }
                    }
                } else {
                    methodValue = resolveId(context, ((Integer) methodValue).intValue());
                }
                writeEntry(out, categoryPrefix + prefix, method.getName(), "()", methodValue);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e2) {
            } catch (TimeoutException e3) {
            }
        }
    }

    private static void exportFields(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        for (Field field : getExportedPropertyFields(klass)) {
            Object obj = null;
            try {
                Class<?> type = field.getType();
                ExportedProperty property = (ExportedProperty) sAnnotations.get(field);
                String categoryPrefix = property.category().length() != 0 ? property.category() + ":" : "";
                int j;
                if (type != Integer.TYPE && type != Byte.TYPE) {
                    if (type == int[].class) {
                        String suffix = "";
                        exportUnrolledArray(context, out, property, (int[]) field.get(view), categoryPrefix + prefix + field.getName() + '_', "");
                    } else if (type == String[].class) {
                        String[] array = (String[]) field.get(view);
                        if (property.hasAdjacentMapping() && array != null) {
                            for (j = 0; j < array.length; j += 2) {
                                if (array[j] != null) {
                                    writeEntry(out, categoryPrefix + prefix, array[j], "", array[j + 1] == null ? "null" : array[j + 1]);
                                }
                            }
                        }
                    } else if (!type.isPrimitive() && property.deepExport()) {
                        dumpViewProperties(context, field.get(view), out, prefix + property.prefix());
                    }
                } else if (!property.resolveId() || context == null) {
                    FlagToString[] flagsMapping = property.flagMapping();
                    if (flagsMapping.length > 0) {
                        exportUnrolledFlags(out, flagsMapping, field.getInt(view), categoryPrefix + prefix + field.getName() + '_');
                    }
                    IntToString[] mapping = property.mapping();
                    if (mapping.length > 0) {
                        int intValue = field.getInt(view);
                        for (IntToString mapped : mapping) {
                            if (mapped.from() == intValue) {
                                obj = mapped.to();
                                break;
                            }
                        }
                        if (obj == null) {
                            obj = Integer.valueOf(intValue);
                        }
                    }
                    if (property.formatToHexString()) {
                        obj = field.get(view);
                        if (type == Integer.TYPE) {
                            obj = formatIntToHexString(((Integer) obj).intValue());
                        } else if (type == Byte.TYPE) {
                            obj = "0x" + Byte.toHexString(((Byte) obj).byteValue(), true);
                        }
                    }
                } else {
                    obj = resolveId(context, field.getInt(view));
                }
                if (obj == null) {
                    obj = field.get(view);
                }
                writeEntry(out, categoryPrefix + prefix, field.getName(), "", obj);
            } catch (IllegalAccessException e) {
            }
        }
    }

    private static void writeEntry(BufferedWriter out, String prefix, String name, String suffix, Object value) throws IOException {
        out.write(prefix);
        out.write(name);
        out.write(suffix);
        out.write(PtmLog.KEY_VAL_SEP);
        writeValue(out, value);
        out.write(32);
    }

    private static void exportUnrolledFlags(BufferedWriter out, FlagToString[] mapping, int intValue, String prefix) throws IOException {
        for (FlagToString flagMapping : mapping) {
            boolean ifTrue = flagMapping.outputIf();
            int maskResult = intValue & flagMapping.mask();
            boolean test = maskResult == flagMapping.equals() ? true : DEBUG_POSITIONING;
            if ((test && ifTrue) || !(test || ifTrue)) {
                writeEntry(out, prefix, flagMapping.name(), "", formatIntToHexString(maskResult));
            }
        }
    }

    private static void exportUnrolledArray(Context context, BufferedWriter out, ExportedProperty property, int[] array, String prefix, String suffix) throws IOException {
        IntToString[] indexMapping = property.indexMapping();
        boolean hasIndexMapping = indexMapping.length > 0 ? true : DEBUG_POSITIONING;
        IntToString[] mapping = property.mapping();
        boolean hasMapping = mapping.length > 0 ? true : DEBUG_POSITIONING;
        boolean resolveId = (!property.resolveId() || context == null) ? DEBUG_POSITIONING : true;
        int valuesCount = array.length;
        for (int j = 0; j < valuesCount; j++) {
            Object value = null;
            int intValue = array[j];
            String name = String.valueOf(j);
            if (hasIndexMapping) {
                for (IntToString mapped : indexMapping) {
                    if (mapped.from() == j) {
                        name = mapped.to();
                        break;
                    }
                }
            }
            if (hasMapping) {
                for (IntToString mapped2 : mapping) {
                    if (mapped2.from() == intValue) {
                        value = mapped2.to();
                        break;
                    }
                }
            }
            if (!resolveId) {
                value = String.valueOf(intValue);
            } else if (value == null) {
                value = (String) resolveId(context, intValue);
            }
            writeEntry(out, prefix, name, suffix, value);
        }
    }

    static Object resolveId(Context context, int id) {
        Resources resources = context.getResources();
        if (id < 0) {
            return "NO_ID";
        }
        try {
            return resources.getResourceTypeName(id) + '/' + resources.getResourceEntryName(id);
        } catch (NotFoundException e) {
            return "id/" + formatIntToHexString(id);
        }
    }

    private static void writeValue(BufferedWriter out, Object value) throws IOException {
        if (value != null) {
            String output = "[EXCEPTION]";
            try {
                output = value.toString().replace("\n", "\\n");
            } finally {
                out.write(String.valueOf(output.length()));
                out.write(PtmLog.PAIRE_DELIMETER);
                out.write(output);
            }
        } else {
            out.write("4,null");
        }
    }

    private static Field[] capturedViewGetPropertyFields(Class<?> klass) {
        if (mCapturedViewFieldsForClasses == null) {
            mCapturedViewFieldsForClasses = new HashMap();
        }
        HashMap<Class<?>, Field[]> map = mCapturedViewFieldsForClasses;
        Field[] fields = (Field[]) map.get(klass);
        if (fields != null) {
            return fields;
        }
        ArrayList<Field> foundFields = new ArrayList();
        for (Field field : klass.getFields()) {
            if (field.isAnnotationPresent(CapturedViewProperty.class)) {
                field.setAccessible(true);
                foundFields.add(field);
            }
        }
        fields = (Field[]) foundFields.toArray(new Field[foundFields.size()]);
        map.put(klass, fields);
        return fields;
    }

    private static Method[] capturedViewGetPropertyMethods(Class<?> klass) {
        if (mCapturedViewMethodsForClasses == null) {
            mCapturedViewMethodsForClasses = new HashMap();
        }
        HashMap<Class<?>, Method[]> map = mCapturedViewMethodsForClasses;
        Method[] methods = (Method[]) map.get(klass);
        if (methods != null) {
            return methods;
        }
        ArrayList<Method> foundMethods = new ArrayList();
        for (Method method : klass.getMethods()) {
            if (method.getParameterTypes().length == 0 && method.isAnnotationPresent(CapturedViewProperty.class) && method.getReturnType() != Void.class) {
                method.setAccessible(true);
                foundMethods.add(method);
            }
        }
        methods = (Method[]) foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods);
        return methods;
    }

    private static String capturedViewExportMethods(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (Method method : capturedViewGetPropertyMethods(klass)) {
            try {
                Object methodValue = method.invoke(obj, (Object[]) null);
                Class<?> returnType = method.getReturnType();
                if (((CapturedViewProperty) method.getAnnotation(CapturedViewProperty.class)).retrieveReturn()) {
                    sb.append(capturedViewExportMethods(methodValue, returnType, method.getName() + "#"));
                } else {
                    sb.append(prefix);
                    sb.append(method.getName());
                    sb.append("()=");
                    if (methodValue != null) {
                        sb.append(methodValue.toString().replace("\n", "\\n"));
                    } else {
                        sb.append("null");
                    }
                    sb.append("; ");
                }
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e2) {
            }
        }
        return sb.toString();
    }

    private static String capturedViewExportFields(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (Field field : capturedViewGetPropertyFields(klass)) {
            try {
                Object fieldValue = field.get(obj);
                sb.append(prefix);
                sb.append(field.getName());
                sb.append(PtmLog.KEY_VAL_SEP);
                if (fieldValue != null) {
                    sb.append(fieldValue.toString().replace("\n", "\\n"));
                } else {
                    sb.append("null");
                }
                sb.append(' ');
            } catch (IllegalAccessException e) {
            }
        }
        return sb.toString();
    }

    public static void dumpCapturedView(String tag, Object view) {
        Class<?> klass = view.getClass();
        StringBuilder sb = new StringBuilder(klass.getName() + ": ");
        sb.append(capturedViewExportFields(view, klass, ""));
        sb.append(capturedViewExportMethods(view, klass, ""));
        Log.d(tag, sb.toString());
    }

    public static Object invokeViewMethod(View view, Method method, Object[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> result = new AtomicReference();
        AtomicReference<Throwable> exception = new AtomicReference();
        view.post(new AnonymousClass9(result, method, view, args, exception, latch));
        try {
            latch.await();
            if (exception.get() == null) {
                return result.get();
            }
            throw new RuntimeException((Throwable) exception.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLayoutParameter(View view, String param, int value) throws NoSuchFieldException, IllegalAccessException {
        LayoutParams p = view.getLayoutParams();
        Field f = p.getClass().getField(param);
        if (f.getType() != Integer.TYPE) {
            throw new RuntimeException("Only integer layout parameters can be set. Field " + param + " is of type " + f.getType().getSimpleName());
        }
        f.set(p, Integer.valueOf(value));
        view.post(new AnonymousClass10(view, p));
    }

    private static void initCustomizedList(Context context) {
        if (!mInited && context != null) {
            mInited = true;
            try {
                String[] split;
                String fields = Systemex.getString(context.getContentResolver(), DUMPC_CUST_FIELDSLIST);
                String methods = Systemex.getString(context.getContentResolver(), DUMPC_CUST_METHODSLIST);
                Log.i("ViewServer", "fields:" + fields + ", methods:" + methods + ", REMOTE_COMMAND_DUMP_CUST:" + REMOTE_COMMAND_DUMP_CUST);
                mFieldsList = fields != null ? fields.split(PtmLog.PAIRE_DELIMETER) : mFieldsList;
                if (methods != null) {
                    split = methods.split(PtmLog.PAIRE_DELIMETER);
                } else {
                    split = mMethodsList;
                }
                mMethodsList = split;
            } catch (Exception e) {
                Log.e("ViewDebug", "Could not load fields or methods from database.", e);
            }
        }
    }

    private static void customizedDump(View root, OutputStream clientStream) throws IOException {
        mCustomizedDump = true;
        initCustomizedList(root.getContext());
        try {
            dump(root, DEBUG_POSITIONING, true, clientStream);
            mCustomizedDump = DEBUG_POSITIONING;
        } catch (IOException e) {
            throw e;
        } catch (Throwable th) {
            mCustomizedDump = DEBUG_POSITIONING;
        }
    }

    private static Field[] filterFieldsProperties(Field[] allProperty, String[] customizedList) {
        ArrayList<Field> result = new ArrayList();
        for (Field item : allProperty) {
            if (arrayContains(customizedList, item.getName())) {
                result.add(item);
            }
        }
        return (Field[]) result.toArray(new Field[result.size()]);
    }

    private static Method[] filterMethodsProperties(Method[] allProperty, String[] customizedList) {
        ArrayList<Method> result = new ArrayList();
        for (Method item : allProperty) {
            if (arrayContains(customizedList, item.getName())) {
                result.add(item);
            }
        }
        return (Method[]) result.toArray(new Method[result.size()]);
    }

    private static boolean arrayContains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return DEBUG_POSITIONING;
    }

    private static HashMap<Class<?>, Field[]> getFieldsMap() {
        return mCustomizedDump ? sCustFieldsForClasses : sFieldsForClasses;
    }

    private static HashMap<Class<?>, Method[]> getMethodMap() {
        return mCustomizedDump ? sCustMethodsForClasses : sMethodsForClasses;
    }

    private static Field[] getFields(Class<?> klass) {
        Field[] fields = klass.getDeclaredFieldsUnchecked(DEBUG_POSITIONING);
        if (mCustomizedDump) {
            return filterFieldsProperties(fields, mFieldsList);
        }
        return fields;
    }

    private static Method[] getMethod(Class<?> klass) {
        Method[] methods = klass.getDeclaredMethods();
        if (mCustomizedDump) {
            return filterMethodsProperties(methods, mMethodsList);
        }
        return methods;
    }
}
