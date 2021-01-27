package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.Picture;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ViewDebug {
    private static final int CAPTURE_TIMEOUT = 4000;
    public static final boolean DEBUG_DRAG = false;
    public static final boolean DEBUG_POSITIONING = false;
    public static final boolean IS_SUPPORT_DRAG_OPTIMIZATION = false;
    private static final String REMOTE_COMMAND_CAPTURE = "CAPTURE";
    private static final String REMOTE_COMMAND_CAPTURE_LAYERS = "CAPTURE_LAYERS";
    private static final String REMOTE_COMMAND_DUMP = "DUMP";
    private static final String REMOTE_COMMAND_DUMP_THEME = "DUMP_THEME";
    private static final String REMOTE_COMMAND_INVALIDATE = "INVALIDATE";
    private static final String REMOTE_COMMAND_OUTPUT_DISPLAYLIST = "OUTPUT_DISPLAYLIST";
    private static final String REMOTE_COMMAND_REQUEST_LAYOUT = "REQUEST_LAYOUT";
    private static final String REMOTE_PROFILE = "PROFILE";
    @Deprecated
    public static final boolean TRACE_HIERARCHY = false;
    @Deprecated
    public static final boolean TRACE_RECYCLER = false;
    private static HashMap<Class<?>, Field[]> mCapturedViewFieldsForClasses = null;
    private static HashMap<Class<?>, Method[]> mCapturedViewMethodsForClasses = null;
    private static HashMap<AccessibleObject, ExportedProperty> sAnnotations;
    private static HashMap<Class<?>, Field[]> sFieldsForClasses;
    private static HashMap<Class<?>, Method[]> sMethodsForClasses;

    public interface CanvasProvider {
        Bitmap createBitmap();

        Canvas getCanvas(View view, int i, int i2);
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
        INVALIDATE,
        INVALIDATE_CHILD,
        INVALIDATE_CHILD_IN_PARENT,
        REQUEST_LAYOUT,
        ON_LAYOUT,
        ON_MEASURE,
        DRAW,
        BUILD_CACHE
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IntToString {
        int from();

        String to();
    }

    @Deprecated
    public enum RecyclerTraceType {
        NEW_VIEW,
        BIND_VIEW,
        RECYCLE_FROM_ACTIVE_HEAP,
        RECYCLE_FROM_SCRAP_HEAP,
        MOVE_TO_SCRAP_HEAP,
        MOVE_FROM_ACTIVE_TO_SCRAP_HEAP
    }

    @UnsupportedAppUsage
    public static long getViewInstanceCount() {
        return Debug.countInstancesOfClass(View.class);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    static void dispatchCommand(View view, String command, String parameters, OutputStream clientStream) throws IOException {
        View view2 = view.getRootView();
        if (REMOTE_COMMAND_DUMP.equalsIgnoreCase(command)) {
            dump(view2, false, true, clientStream);
        } else if (REMOTE_COMMAND_DUMP_THEME.equalsIgnoreCase(command)) {
            dumpTheme(view2, clientStream);
        } else if (REMOTE_COMMAND_CAPTURE_LAYERS.equalsIgnoreCase(command)) {
            captureLayers(view2, new DataOutputStream(clientStream));
        } else {
            String[] params = parameters.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            if (REMOTE_COMMAND_CAPTURE.equalsIgnoreCase(command)) {
                capture(view2, clientStream, params[0]);
            } else if (REMOTE_COMMAND_OUTPUT_DISPLAYLIST.equalsIgnoreCase(command)) {
                outputDisplayList(view2, params[0]);
            } else if (REMOTE_COMMAND_INVALIDATE.equalsIgnoreCase(command)) {
                invalidate(view2, params[0]);
            } else if (REMOTE_COMMAND_REQUEST_LAYOUT.equalsIgnoreCase(command)) {
                requestLayout(view2, params[0]);
            } else if (REMOTE_PROFILE.equalsIgnoreCase(command)) {
                profile(view2, clientStream, params[0]);
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
        final View view = findView(root, parameter);
        if (view != null) {
            root.post(new Runnable() {
                /* class android.view.ViewDebug.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    View.this.requestLayout();
                }
            });
        }
    }

    private static void profile(View root, OutputStream clientStream, String parameter) throws IOException {
        View view = findView(root, parameter);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 32768);
            if (view != null) {
                profileViewAndChildren(view, out);
            } else {
                out.write("-1 -1 -1");
                out.newLine();
            }
            out.write("DONE.");
            out.newLine();
        } catch (Exception e) {
            Log.w("View", "Problem profiling the view:", e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                out.close();
            }
            throw th;
        }
        out.close();
    }

    public static void profileViewAndChildren(View view, BufferedWriter out) throws IOException {
        profileViewAndChildren(view, RenderNode.create("ViewDebug", null), out, true);
    }

    private static void profileViewAndChildren(View view, RenderNode node, BufferedWriter out, boolean root) throws IOException {
        long durationDraw = 0;
        long durationMeasure = (root || (view.mPrivateFlags & 2048) != 0) ? profileViewMeasure(view) : 0;
        long durationLayout = (root || (view.mPrivateFlags & 8192) != 0) ? profileViewLayout(view) : 0;
        if (root || !view.willNotDraw() || (view.mPrivateFlags & 32) != 0) {
            durationDraw = profileViewDraw(view, node);
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
                profileViewAndChildren(group.getChildAt(i), node, out, false);
            }
        }
    }

    private static long profileViewMeasure(final View view) {
        return profileViewOperation(view, new ViewOperation() {
            /* class android.view.ViewDebug.AnonymousClass2 */

            @Override // android.view.ViewDebug.ViewOperation
            public void pre() {
                forceLayout(View.this);
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

            @Override // android.view.ViewDebug.ViewOperation
            public void run() {
                View view = View.this;
                view.measure(view.mOldWidthMeasureSpec, View.this.mOldHeightMeasureSpec);
            }
        });
    }

    private static long profileViewLayout(View view) {
        return profileViewOperation(view, new ViewOperation() {
            /* class android.view.$$Lambda$ViewDebug$inOytI2zZEgp1DJv8Cu4GjQVNiE */

            @Override // android.view.ViewDebug.ViewOperation
            public final void run() {
                View view;
                view.layout(view.mLeft, view.mTop, view.mRight, View.this.mBottom);
            }
        });
    }

    private static long profileViewDraw(View view, RenderNode node) {
        DisplayMetrics dm = view.getResources().getDisplayMetrics();
        if (dm == null) {
            return 0;
        }
        if (view.isHardwareAccelerated()) {
            try {
                return profileViewOperation(view, new ViewOperation(node.beginRecording(dm.widthPixels, dm.heightPixels)) {
                    /* class android.view.$$Lambda$ViewDebug$flFXZc7_CjFXx7_tYT59WSbUNjI */
                    private final /* synthetic */ RecordingCanvas f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.view.ViewDebug.ViewOperation
                    public final void run() {
                        View.this.draw(this.f$1);
                    }
                });
            } finally {
                node.endRecording();
            }
        } else {
            Bitmap bitmap = Bitmap.createBitmap(dm, dm.widthPixels, dm.heightPixels, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            try {
                return profileViewOperation(view, new ViewOperation(canvas) {
                    /* class android.view.$$Lambda$ViewDebug$w986pBwzwNi77yEgLa3IWusjPNw */
                    private final /* synthetic */ Canvas f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.view.ViewDebug.ViewOperation
                    public final void run() {
                        View.this.draw(this.f$1);
                    }
                });
            } finally {
                canvas.setBitmap(null);
                bitmap.recycle();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public interface ViewOperation {
        void run();

        default void pre() {
        }
    }

    private static long profileViewOperation(View view, ViewOperation operation) {
        CountDownLatch latch = new CountDownLatch(1);
        long[] duration = new long[1];
        view.post(new Runnable(duration, latch) {
            /* class android.view.$$Lambda$ViewDebug$5rTN0pemwbr3I3IL2ExDBeDTDg */
            private final /* synthetic */ long[] f$1;
            private final /* synthetic */ CountDownLatch f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ViewDebug.lambda$profileViewOperation$3(ViewDebug.ViewOperation.this, this.f$1, this.f$2);
            }
        });
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

    static /* synthetic */ void lambda$profileViewOperation$3(ViewOperation operation, long[] duration, CountDownLatch latch) {
        try {
            operation.pre();
            long start = Debug.threadCpuTimeNanos();
            operation.run();
            duration[0] = Debug.threadCpuTimeNanos() - start;
        } finally {
            latch.countDown();
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
        boolean localVisible = view.getVisibility() == 0 && visible;
        if ((view.mPrivateFlags & 128) != 128) {
            int id = view.getId();
            String name = view.getClass().getSimpleName();
            if (id != -1) {
                name = resolveId(view.getContext(), id).toString();
            }
            clientStream.write(1);
            clientStream.writeUTF(name);
            clientStream.writeByte(localVisible ? 1 : 0);
            int[] position = new int[2];
            view.getLocationInWindow(position);
            clientStream.writeInt(position[0]);
            clientStream.writeInt(position[1]);
            clientStream.flush();
            Bitmap b = performViewCapture(view, true);
            if (b != null) {
                ByteArrayOutputStream arrayOut = new ByteArrayOutputStream(b.getWidth() * b.getHeight() * 2);
                b.compress(Bitmap.CompressFormat.PNG, 100, arrayOut);
                clientStream.writeInt(arrayOut.size());
                arrayOut.writeTo(clientStream);
            }
            clientStream.flush();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                captureViewLayer(group.getChildAt(i), clientStream, localVisible);
            }
        }
        if (view.mOverlay != null) {
            captureViewLayer(view.getOverlay().mOverlayViewGroup, clientStream, localVisible);
        }
    }

    private static void outputDisplayList(View root, String parameter) throws IOException {
        View view = findView(root, parameter);
        view.getViewRootImpl().outputDisplayList(view);
    }

    public static void outputDisplayList(View root, View target) {
        root.getViewRootImpl().outputDisplayList(target);
    }

    private static class PictureCallbackHandler implements AutoCloseable, HardwareRenderer.PictureCapturedCallback, Runnable {
        private final Function<Picture, Boolean> mCallback;
        private final Executor mExecutor;
        private final ReentrantLock mLock;
        private final ArrayDeque<Picture> mQueue;
        private Thread mRenderThread;
        private final HardwareRenderer mRenderer;
        private boolean mStopListening;

        private PictureCallbackHandler(HardwareRenderer renderer, Function<Picture, Boolean> callback, Executor executor) {
            this.mLock = new ReentrantLock(false);
            this.mQueue = new ArrayDeque<>(3);
            this.mRenderer = renderer;
            this.mCallback = callback;
            this.mExecutor = executor;
            this.mRenderer.setPictureCaptureCallback(this);
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            this.mLock.lock();
            this.mStopListening = true;
            this.mLock.unlock();
            this.mRenderer.setPictureCaptureCallback(null);
        }

        @Override // android.graphics.HardwareRenderer.PictureCapturedCallback
        public void onPictureCaptured(Picture picture) {
            this.mLock.lock();
            if (this.mStopListening) {
                this.mLock.unlock();
                this.mRenderer.setPictureCaptureCallback(null);
                return;
            }
            if (this.mRenderThread == null) {
                this.mRenderThread = Thread.currentThread();
            }
            Picture toDestroy = null;
            if (this.mQueue.size() == 3) {
                toDestroy = this.mQueue.removeLast();
            }
            this.mQueue.add(picture);
            this.mLock.unlock();
            if (toDestroy == null) {
                this.mExecutor.execute(this);
            } else {
                toDestroy.close();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mLock.lock();
            Picture picture = this.mQueue.poll();
            boolean isStopped = this.mStopListening;
            this.mLock.unlock();
            if (Thread.currentThread() == this.mRenderThread) {
                close();
                throw new IllegalStateException("ViewDebug#startRenderingCommandsCapture must be given an executor that invokes asynchronously");
            } else if (isStopped) {
                picture.close();
            } else if (!this.mCallback.apply(picture).booleanValue()) {
                close();
            }
        }
    }

    @Deprecated
    public static AutoCloseable startRenderingCommandsCapture(View tree, Executor executor, Function<Picture, Boolean> callback) {
        View.AttachInfo attachInfo = tree.mAttachInfo;
        if (attachInfo == null) {
            throw new IllegalArgumentException("Given view isn't attached");
        } else if (attachInfo.mHandler.getLooper() == Looper.myLooper()) {
            HardwareRenderer renderer = attachInfo.mThreadedRenderer;
            if (renderer != null) {
                return new PictureCallbackHandler(renderer, callback, executor);
            }
            return null;
        } else {
            throw new IllegalStateException("Called on the wrong thread. Must be called on the thread that owns the given View");
        }
    }

    public static AutoCloseable startRenderingCommandsCapture(View tree, Executor executor, Callable<OutputStream> callback) {
        View.AttachInfo attachInfo = tree.mAttachInfo;
        if (attachInfo == null) {
            throw new IllegalArgumentException("Given view isn't attached");
        } else if (attachInfo.mHandler.getLooper() == Looper.myLooper()) {
            HardwareRenderer renderer = attachInfo.mThreadedRenderer;
            if (renderer != null) {
                return new PictureCallbackHandler(renderer, new Function(callback) {
                    /* class android.view.$$Lambda$ViewDebug$hyDSYptlxuUTTyRIONqWzWWVDB0 */
                    private final /* synthetic */ Callable f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return ViewDebug.lambda$startRenderingCommandsCapture$4(this.f$0, (Picture) obj);
                    }
                }, executor);
            }
            return null;
        } else {
            throw new IllegalStateException("Called on the wrong thread. Must be called on the thread that owns the given View");
        }
    }

    static /* synthetic */ Boolean lambda$startRenderingCommandsCapture$4(Callable callback, Picture picture) {
        try {
            OutputStream stream = (OutputStream) callback.call();
            if (stream != null) {
                picture.writeToStream(stream);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static void capture(View root, OutputStream clientStream, String parameter) throws IOException {
        capture(root, clientStream, findView(root, parameter));
    }

    public static void capture(View root, OutputStream clientStream, View captureView) throws IOException {
        Bitmap b = performViewCapture(captureView, false);
        if (b == null) {
            Log.w("View", "Failed to create capture bitmap!");
            b = Bitmap.createBitmap(root.getResources().getDisplayMetrics(), 1, 1, Bitmap.Config.ARGB_8888);
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(clientStream, 32768);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            b.recycle();
        } catch (Throwable th) {
            if (out != null) {
                out.close();
            }
            b.recycle();
            throw th;
        }
    }

    private static Bitmap performViewCapture(View captureView, boolean skipChildren) {
        if (captureView == null) {
            return null;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Bitmap[] cache = new Bitmap[1];
        captureView.post(new Runnable(cache, skipChildren, latch) {
            /* class android.view.$$Lambda$ViewDebug$1iDmmthcZt_8LsYI6VndkxasPEs */
            private final /* synthetic */ Bitmap[] f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ CountDownLatch f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ViewDebug.lambda$performViewCapture$5(View.this, this.f$1, this.f$2, this.f$3);
            }
        });
        try {
            latch.await(4000, TimeUnit.MILLISECONDS);
            return cache[0];
        } catch (InterruptedException e) {
            Log.w("View", "Could not complete the capture of the view " + captureView);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    static /* synthetic */ void lambda$performViewCapture$5(View captureView, Bitmap[] cache, boolean skipChildren, CountDownLatch latch) {
        try {
            cache[0] = captureView.createSnapshot(captureView.isHardwareAccelerated() ? new HardwareCanvasProvider() : new SoftwareCanvasProvider(), skipChildren);
        } catch (OutOfMemoryError e) {
            Log.w("View", "Out of memory for bitmap");
        } catch (Throwable th) {
            latch.countDown();
            throw th;
        }
        latch.countDown();
    }

    @UnsupportedAppUsage
    @Deprecated
    public static void dump(View root, boolean skipChildren, boolean includeProperties, OutputStream clientStream) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), 32768);
            View view = root.getRootView();
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                dumpViewHierarchy(group.getContext(), group, out, 0, skipChildren, includeProperties);
            }
            out.write("DONE.");
            out.newLine();
        } catch (Exception e) {
            Log.w("View", "Problem dumping the view:", e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                out.close();
            }
            throw th;
        }
        out.close();
    }

    public static void dumpv2(final View view, ByteArrayOutputStream out) throws InterruptedException {
        final ViewHierarchyEncoder encoder = new ViewHierarchyEncoder(out);
        final CountDownLatch latch = new CountDownLatch(1);
        view.post(new Runnable() {
            /* class android.view.ViewDebug.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                ViewHierarchyEncoder.this.addProperty("window:left", view.mAttachInfo.mWindowLeft);
                ViewHierarchyEncoder.this.addProperty("window:top", view.mAttachInfo.mWindowTop);
                view.encode(ViewHierarchyEncoder.this);
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);
        encoder.endStream();
    }

    public static void dumpTheme(View view, OutputStream clientStream) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), 32768);
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
        } catch (Exception e) {
            Log.w("View", "Problem dumping View Theme:", e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                out.close();
            }
            throw th;
        }
        out.close();
    }

    private static String[] getStyleAttributesDump(Resources resources, Resources.Theme theme) {
        String str;
        TypedValue outValue = new TypedValue();
        int i = 0;
        int[] attributes = theme.getAllAttributes();
        String[] data = new String[(attributes.length * 2)];
        for (int attributeId : attributes) {
            try {
                data[i] = resources.getResourceName(attributeId);
                int i2 = i + 1;
                if (theme.resolveAttribute(attributeId, outValue, true)) {
                    str = outValue.coerceToString().toString();
                } else {
                    str = "null";
                }
                data[i2] = str;
                i += 2;
                if (outValue.type == 1) {
                    data[i - 1] = resources.getResourceName(outValue.resourceId);
                }
            } catch (Resources.NotFoundException e) {
            }
        }
        return data;
    }

    private static View findView(ViewGroup group, String className, int hashCode) {
        View found;
        View found2;
        if (isRequestedView(group, className, hashCode)) {
            return group;
        }
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                View found3 = findView((ViewGroup) view, className, hashCode);
                if (found3 != null) {
                    return found3;
                }
            } else if (isRequestedView(view, className, hashCode)) {
                return view;
            }
            if (!(view.mOverlay == null || (found2 = findView(view.mOverlay.mOverlayViewGroup, className, hashCode)) == null)) {
                return found2;
            }
            if ((view instanceof HierarchyHandler) && (found = ((HierarchyHandler) view).findHierarchyView(className, hashCode)) != null) {
                return found;
            }
        }
        return null;
    }

    private static boolean isRequestedView(View view, String className, int hashCode) {
        if (view.hashCode() != hashCode) {
            return false;
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
        for (int i = 0; i < level; i++) {
            try {
                out.write(32);
            } catch (IOException e) {
                Log.w("View", "Error while dumping hierarchy tree");
                return false;
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
            sFieldsForClasses = new HashMap<>();
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap<>(512);
        }
        HashMap<Class<?>, Field[]> map = sFieldsForClasses;
        Field[] fields = map.get(klass);
        if (fields != null) {
            return fields;
        }
        try {
            Field[] declaredFields = klass.getDeclaredFieldsUnchecked(false);
            ArrayList<Field> foundFields = new ArrayList<>();
            for (Field field : declaredFields) {
                if (field.getType() != null && field.isAnnotationPresent(ExportedProperty.class)) {
                    field.setAccessible(true);
                    foundFields.add(field);
                    sAnnotations.put(field, (ExportedProperty) field.getAnnotation(ExportedProperty.class));
                }
            }
            Field[] fields2 = (Field[]) foundFields.toArray(new Field[foundFields.size()]);
            map.put(klass, fields2);
            return fields2;
        } catch (NoClassDefFoundError e) {
            throw new AssertionError(e);
        }
    }

    private static Method[] getExportedPropertyMethods(Class<?> klass) {
        if (sMethodsForClasses == null) {
            sMethodsForClasses = new HashMap<>(100);
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap<>(512);
        }
        HashMap<Class<?>, Method[]> map = sMethodsForClasses;
        Method[] methods = map.get(klass);
        if (methods != null) {
            return methods;
        }
        Method[] methods2 = klass.getDeclaredMethodsUnchecked(false);
        ArrayList<Method> foundMethods = new ArrayList<>();
        for (Method method : methods2) {
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
        Method[] methods3 = (Method[]) foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods3);
        return methods3;
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

    private static Object callMethodOnAppropriateTheadBlocking(final Method method, Object object) throws IllegalAccessException, InvocationTargetException, TimeoutException {
        if (!(object instanceof View)) {
            return method.invoke(object, null);
        }
        final View view = (View) object;
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
            /* class android.view.ViewDebug.AnonymousClass4 */

            @Override // java.util.concurrent.Callable
            public Object call() throws IllegalAccessException, InvocationTargetException {
                return method.invoke(view, null);
            }
        });
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

    /* JADX INFO: Multiple debug info for r10v4 'categoryPrefix'  java.lang.String: [D('methods' java.lang.reflect.Method[]), D('categoryPrefix' java.lang.String)] */
    private static void exportMethods(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        Method[] methods;
        String categoryPrefix;
        String categoryPrefix2;
        String str;
        String[] array;
        String str2;
        boolean mapped;
        Method[] methods2 = getExportedPropertyMethods(klass);
        int count = methods2.length;
        int i = 0;
        while (i < count) {
            Method method = methods2[i];
            try {
                Object methodValue = callMethodOnAppropriateTheadBlocking(method, view);
                Class<?> returnType = method.getReturnType();
                ExportedProperty property = sAnnotations.get(method);
                if (property.category().length() != 0) {
                    try {
                        categoryPrefix = property.category() + SettingsStringUtil.DELIMITER;
                    } catch (IllegalAccessException e) {
                        methods = methods2;
                    } catch (InvocationTargetException e2) {
                        methods = methods2;
                    } catch (TimeoutException e3) {
                        methods = methods2;
                    }
                } else {
                    categoryPrefix = "";
                }
                if (returnType != Integer.TYPE) {
                    if (returnType == int[].class) {
                        methods = methods2;
                        try {
                            exportUnrolledArray(context, out, property, (int[]) methodValue, categoryPrefix + prefix + method.getName() + '_', "()");
                        } catch (IllegalAccessException | InvocationTargetException | TimeoutException e4) {
                        }
                    } else {
                        methods = methods2;
                        str = "()";
                        categoryPrefix2 = categoryPrefix;
                        if (returnType == String[].class) {
                            String[] array2 = (String[]) methodValue;
                            if (property.hasAdjacentMapping() && array2 != null) {
                                int j = 0;
                                while (j < array2.length) {
                                    if (array2[j] != null) {
                                        array = array2;
                                        writeEntry(out, categoryPrefix2 + prefix, array2[j], str, array2[j + 1] == null ? "null" : array2[j + 1]);
                                    } else {
                                        array = array2;
                                    }
                                    j += 2;
                                    array2 = array;
                                }
                            }
                        } else if (!returnType.isPrimitive() && property.deepExport()) {
                            dumpViewProperties(context, methodValue, out, prefix + property.prefix());
                        }
                    }
                    i++;
                    methods2 = methods;
                } else if (!property.resolveId() || context == null) {
                    FlagToString[] flagsMapping = property.flagMapping();
                    if (flagsMapping.length > 0) {
                        int intValue = ((Integer) methodValue).intValue();
                        StringBuilder sb = new StringBuilder();
                        sb.append(categoryPrefix);
                        sb.append(prefix);
                        str2 = "()";
                        sb.append(method.getName());
                        sb.append('_');
                        exportUnrolledFlags(out, flagsMapping, intValue, sb.toString());
                    } else {
                        str2 = "()";
                    }
                    IntToString[] mapping = property.mapping();
                    if (mapping.length > 0) {
                        int intValue2 = ((Integer) methodValue).intValue();
                        int mappingCount = mapping.length;
                        int j2 = 0;
                        while (true) {
                            if (j2 >= mappingCount) {
                                mapped = false;
                                break;
                            }
                            IntToString mapper = mapping[j2];
                            if (mapper.from() == intValue2) {
                                methodValue = mapper.to();
                                mapped = true;
                                break;
                            }
                            j2++;
                            mappingCount = mappingCount;
                        }
                        if (!mapped) {
                            methodValue = Integer.valueOf(intValue2);
                        }
                    }
                    methods = methods2;
                    str = str2;
                    categoryPrefix2 = categoryPrefix;
                } else {
                    methodValue = resolveId(context, ((Integer) methodValue).intValue());
                    str = "()";
                    methods = methods2;
                    categoryPrefix2 = categoryPrefix;
                }
                writeEntry(out, categoryPrefix2 + prefix, method.getName(), str, methodValue);
            } catch (IllegalAccessException e5) {
                methods = methods2;
            } catch (InvocationTargetException e6) {
                methods = methods2;
            } catch (TimeoutException e7) {
                methods = methods2;
            }
            i++;
            methods2 = methods;
        }
    }

    /* JADX INFO: Multiple debug info for r0v6 'categoryPrefix'  java.lang.String: [D('fieldValue' java.lang.Object), D('categoryPrefix' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01c4  */
    private static void exportFields(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        Object fieldValue;
        String categoryPrefix;
        Object fieldValue2;
        String[] array;
        Field[] fields = getExportedPropertyFields(klass);
        for (Field field : fields) {
            try {
                Class<?> type = field.getType();
                ExportedProperty property = sAnnotations.get(field);
                String categoryPrefix2 = property.category().length() != 0 ? property.category() + SettingsStringUtil.DELIMITER : "";
                if (type == Integer.TYPE) {
                    fieldValue2 = null;
                    categoryPrefix = categoryPrefix2;
                } else if (type == Byte.TYPE) {
                    fieldValue2 = null;
                    categoryPrefix = categoryPrefix2;
                } else {
                    if (type == int[].class) {
                        exportUnrolledArray(context, out, property, (int[]) field.get(view), categoryPrefix2 + prefix + field.getName() + '_', "");
                    } else {
                        categoryPrefix = categoryPrefix2;
                        if (type == String[].class) {
                            String[] array2 = (String[]) field.get(view);
                            if (property.hasAdjacentMapping() && array2 != null) {
                                int j = 0;
                                while (j < array2.length) {
                                    if (array2[j] != null) {
                                        array = array2;
                                        writeEntry(out, categoryPrefix + prefix, array2[j], "", array2[j + 1] == null ? "null" : array2[j + 1]);
                                    } else {
                                        array = array2;
                                    }
                                    j += 2;
                                    array2 = array;
                                }
                            }
                        } else if (type.isPrimitive() || !property.deepExport()) {
                            fieldValue = null;
                            if (fieldValue == null) {
                                fieldValue = field.get(view);
                            }
                            writeEntry(out, categoryPrefix + prefix, field.getName(), "", fieldValue);
                        } else {
                            dumpViewProperties(context, field.get(view), out, prefix + property.prefix());
                        }
                    }
                }
                if (!property.resolveId() || context == null) {
                    FlagToString[] flagsMapping = property.flagMapping();
                    if (flagsMapping.length > 0) {
                        exportUnrolledFlags(out, flagsMapping, field.getInt(view), categoryPrefix + prefix + field.getName() + '_');
                    }
                    IntToString[] mapping = property.mapping();
                    if (mapping.length > 0) {
                        int intValue = field.getInt(view);
                        int mappingCount = mapping.length;
                        int j2 = 0;
                        while (true) {
                            if (j2 >= mappingCount) {
                                break;
                            }
                            IntToString mapped = mapping[j2];
                            if (mapped.from() == intValue) {
                                fieldValue2 = mapped.to();
                                break;
                            } else {
                                j2++;
                                mapping = mapping;
                            }
                        }
                        if (fieldValue2 == null) {
                            fieldValue2 = Integer.valueOf(intValue);
                        }
                    }
                    if (property.formatToHexString()) {
                        fieldValue = field.get(view);
                        if (type == Integer.TYPE) {
                            fieldValue = formatIntToHexString(((Integer) fieldValue).intValue());
                        } else if (type == Byte.TYPE) {
                            fieldValue = "0x" + Byte.toHexString(((Byte) fieldValue).byteValue(), true);
                        }
                    } else {
                        fieldValue = fieldValue2;
                    }
                    if (fieldValue == null) {
                    }
                    writeEntry(out, categoryPrefix + prefix, field.getName(), "", fieldValue);
                } else {
                    fieldValue = resolveId(context, field.getInt(view));
                    if (fieldValue == null) {
                    }
                    writeEntry(out, categoryPrefix + prefix, field.getName(), "", fieldValue);
                }
            } catch (IllegalAccessException e) {
            }
        }
    }

    private static void writeEntry(BufferedWriter out, String prefix, String name, String suffix, Object value) throws IOException {
        out.write(prefix);
        out.write(name);
        out.write(suffix);
        out.write("=");
        writeValue(out, value);
        out.write(32);
    }

    private static void exportUnrolledFlags(BufferedWriter out, FlagToString[] mapping, int intValue, String prefix) throws IOException {
        for (FlagToString flagMapping : mapping) {
            boolean ifTrue = flagMapping.outputIf();
            int maskResult = flagMapping.mask() & intValue;
            boolean test = maskResult == flagMapping.equals();
            if ((test && ifTrue) || (!test && !ifTrue)) {
                writeEntry(out, prefix, flagMapping.name(), "", formatIntToHexString(maskResult));
            }
        }
    }

    public static String intToString(Class<?> clazz, String field, int integer) {
        IntToString[] mapping = getMapping(clazz, field);
        if (mapping == null) {
            return Integer.toString(integer);
        }
        for (IntToString map : mapping) {
            if (map.from() == integer) {
                return map.to();
            }
        }
        return Integer.toString(integer);
    }

    public static String flagsToString(Class<?> clazz, String field, int flags) {
        FlagToString[] mapping = getFlagMapping(clazz, field);
        if (mapping == null) {
            return Integer.toHexString(flags);
        }
        StringBuilder result = new StringBuilder();
        int count = mapping.length;
        int j = 0;
        while (true) {
            boolean test = true;
            if (j >= count) {
                break;
            }
            FlagToString flagMapping = mapping[j];
            boolean ifTrue = flagMapping.outputIf();
            if ((flagMapping.mask() & flags) != flagMapping.equals()) {
                test = false;
            }
            if (test && ifTrue) {
                result.append(flagMapping.name());
                result.append(' ');
            }
            j++;
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static FlagToString[] getFlagMapping(Class<?> clazz, String field) {
        try {
            return ((ExportedProperty) clazz.getDeclaredField(field).getAnnotation(ExportedProperty.class)).flagMapping();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static IntToString[] getMapping(Class<?> clazz, String field) {
        try {
            return ((ExportedProperty) clazz.getDeclaredField(field).getAnnotation(ExportedProperty.class)).mapping();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static void exportUnrolledArray(Context context, BufferedWriter out, ExportedProperty property, int[] array, String prefix, String suffix) throws IOException {
        IntToString[] indexMapping = property.indexMapping();
        boolean resolveId = false;
        boolean hasIndexMapping = indexMapping.length > 0;
        IntToString[] mapping = property.mapping();
        boolean hasMapping = mapping.length > 0;
        if (property.resolveId() && context != null) {
            resolveId = true;
        }
        int valuesCount = array.length;
        for (int j = 0; j < valuesCount; j++) {
            String value = null;
            int intValue = array[j];
            String name = String.valueOf(j);
            if (hasIndexMapping) {
                int mappingCount = indexMapping.length;
                int k = 0;
                while (true) {
                    if (k >= mappingCount) {
                        break;
                    }
                    IntToString mapped = indexMapping[k];
                    if (mapped.from() == j) {
                        name = mapped.to();
                        break;
                    }
                    k++;
                }
            }
            if (hasMapping) {
                int mappingCount2 = mapping.length;
                int k2 = 0;
                while (true) {
                    if (k2 >= mappingCount2) {
                        break;
                    }
                    IntToString mapped2 = mapping[k2];
                    if (mapped2.from() == intValue) {
                        value = mapped2.to();
                        break;
                    }
                    k2++;
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

    /* JADX INFO: Multiple debug info for r1v2 java.lang.String: [D('fieldValue' java.lang.Object), D('e' android.content.res.Resources$NotFoundException)] */
    static Object resolveId(Context context, int id) {
        Resources resources = context.getResources();
        if (id < 0) {
            return "NO_ID";
        }
        try {
            return resources.getResourceTypeName(id) + '/' + resources.getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
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
                out.write(SmsManager.REGEX_PREFIX_DELIMITER);
                out.write(output);
            }
        } else {
            out.write("4,null");
        }
    }

    private static Field[] capturedViewGetPropertyFields(Class<?> klass) {
        if (mCapturedViewFieldsForClasses == null) {
            mCapturedViewFieldsForClasses = new HashMap<>();
        }
        HashMap<Class<?>, Field[]> map = mCapturedViewFieldsForClasses;
        Field[] fields = map.get(klass);
        if (fields != null) {
            return fields;
        }
        ArrayList<Field> foundFields = new ArrayList<>();
        Field[] fields2 = klass.getFields();
        for (Field field : fields2) {
            if (field.isAnnotationPresent(CapturedViewProperty.class)) {
                field.setAccessible(true);
                foundFields.add(field);
            }
        }
        Field[] fields3 = (Field[]) foundFields.toArray(new Field[foundFields.size()]);
        map.put(klass, fields3);
        return fields3;
    }

    private static Method[] capturedViewGetPropertyMethods(Class<?> klass) {
        if (mCapturedViewMethodsForClasses == null) {
            mCapturedViewMethodsForClasses = new HashMap<>();
        }
        HashMap<Class<?>, Method[]> map = mCapturedViewMethodsForClasses;
        Method[] methods = map.get(klass);
        if (methods != null) {
            return methods;
        }
        ArrayList<Method> foundMethods = new ArrayList<>();
        Method[] methods2 = klass.getMethods();
        for (Method method : methods2) {
            if (method.getParameterTypes().length == 0 && method.isAnnotationPresent(CapturedViewProperty.class) && method.getReturnType() != Void.class) {
                method.setAccessible(true);
                foundMethods.add(method);
            }
        }
        Method[] methods3 = (Method[]) foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods3);
        return methods3;
    }

    private static String capturedViewExportMethods(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        Method[] methods = capturedViewGetPropertyMethods(klass);
        for (Method method : methods) {
            try {
                Object methodValue = method.invoke(obj, null);
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
            } catch (IllegalAccessException | InvocationTargetException e) {
            }
        }
        return sb.toString();
    }

    private static String capturedViewExportFields(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        Field[] fields = capturedViewGetPropertyFields(klass);
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(obj);
                sb.append(prefix);
                sb.append(field.getName());
                sb.append("=");
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

    public static Object invokeViewMethod(final View view, final Method method, final Object[] args) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Object> result = new AtomicReference<>();
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        view.post(new Runnable() {
            /* class android.view.ViewDebug.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    result.set(method.invoke(view, args));
                } catch (InvocationTargetException e) {
                    exception.set(e.getCause());
                } catch (Exception e2) {
                    exception.set(e2);
                }
                latch.countDown();
            }
        });
        try {
            latch.await();
            if (exception.get() == null) {
                return result.get();
            }
            throw new RuntimeException(exception.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLayoutParameter(final View view, String param, int value) throws NoSuchFieldException, IllegalAccessException {
        final ViewGroup.LayoutParams p = view.getLayoutParams();
        Field f = p.getClass().getField(param);
        if (f.getType() == Integer.TYPE) {
            f.set(p, Integer.valueOf(value));
            view.post(new Runnable() {
                /* class android.view.ViewDebug.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    View.this.setLayoutParams(p);
                }
            });
            return;
        }
        throw new RuntimeException("Only integer layout parameters can be set. Field " + param + " is of type " + f.getType().getSimpleName());
    }

    public static class SoftwareCanvasProvider implements CanvasProvider {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private boolean mEnabledHwBitmapsInSwMode;

        @Override // android.view.ViewDebug.CanvasProvider
        public Canvas getCanvas(View view, int width, int height) {
            this.mBitmap = Bitmap.createBitmap(view.getResources().getDisplayMetrics(), width, height, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = this.mBitmap;
            if (bitmap != null) {
                bitmap.setDensity(view.getResources().getDisplayMetrics().densityDpi);
                if (view.mAttachInfo != null) {
                    this.mCanvas = view.mAttachInfo.mCanvas;
                }
                if (this.mCanvas == null) {
                    this.mCanvas = new Canvas();
                }
                this.mEnabledHwBitmapsInSwMode = this.mCanvas.isHwBitmapsInSwModeEnabled();
                this.mCanvas.setBitmap(this.mBitmap);
                return this.mCanvas;
            }
            throw new OutOfMemoryError();
        }

        @Override // android.view.ViewDebug.CanvasProvider
        public Bitmap createBitmap() {
            this.mCanvas.setBitmap(null);
            this.mCanvas.setHwBitmapsInSwModeEnabled(this.mEnabledHwBitmapsInSwMode);
            return this.mBitmap;
        }
    }

    public static class HardwareCanvasProvider implements CanvasProvider {
        private Picture mPicture;

        @Override // android.view.ViewDebug.CanvasProvider
        public Canvas getCanvas(View view, int width, int height) {
            this.mPicture = new Picture();
            return this.mPicture.beginRecording(width, height);
        }

        @Override // android.view.ViewDebug.CanvasProvider
        public Bitmap createBitmap() {
            this.mPicture.endRecording();
            return Bitmap.createBitmap(this.mPicture);
        }
    }
}
