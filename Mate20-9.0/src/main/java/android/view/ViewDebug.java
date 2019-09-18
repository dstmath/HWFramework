package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewOverlay;
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

public class ViewDebug {
    private static final int CAPTURE_TIMEOUT = 4000;
    public static final boolean DEBUG_DRAG = false;
    public static final boolean DEBUG_POSITIONING = false;
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

    public static class HardwareCanvasProvider implements CanvasProvider {
        private Picture mPicture;

        public Canvas getCanvas(View view, int width, int height) {
            this.mPicture = new Picture();
            return this.mPicture.beginRecording(width, height);
        }

        public Bitmap createBitmap() {
            this.mPicture.endRecording();
            return Bitmap.createBitmap(this.mPicture);
        }
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

    public static class SoftwareCanvasProvider implements CanvasProvider {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private boolean mEnabledHwBitmapsInSwMode;

        public Canvas getCanvas(View view, int width, int height) {
            this.mBitmap = Bitmap.createBitmap(view.getResources().getDisplayMetrics(), width, height, Bitmap.Config.ARGB_8888);
            if (this.mBitmap != null) {
                this.mBitmap.setDensity(view.getResources().getDisplayMetrics().densityDpi);
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

        public Bitmap createBitmap() {
            this.mCanvas.setBitmap(null);
            this.mCanvas.setHwBitmapsInSwModeEnabled(this.mEnabledHwBitmapsInSwMode);
            return this.mBitmap;
        }
    }

    interface ViewOperation {
        void run();

        void pre() {
        }
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
            if (out == null) {
                return;
            }
        } catch (Throwable th) {
            if (out != null) {
                out.close();
            }
            throw th;
        }
        out.close();
    }

    public static void profileViewAndChildren(View view, BufferedWriter out) throws IOException {
        RenderNode node = RenderNode.create("ViewDebug", null);
        profileViewAndChildren(view, node, out, true);
        node.destroy();
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

            public void run() {
                View.this.measure(View.this.mOldWidthMeasureSpec, View.this.mOldHeightMeasureSpec);
            }
        });
    }

    private static long profileViewLayout(View view) {
        return profileViewOperation(view, new ViewOperation() {
            public final void run() {
                View.this.layout(View.this.mLeft, View.this.mTop, View.this.mRight, View.this.mBottom);
            }
        });
    }

    private static long profileViewDraw(View view, RenderNode node) {
        DisplayMetrics dm = view.getResources().getDisplayMetrics();
        if (dm == null) {
            return 0;
        }
        if (view.isHardwareAccelerated()) {
            DisplayListCanvas canvas = node.start(dm.widthPixels, dm.heightPixels);
            try {
                return profileViewOperation(view, new ViewOperation(canvas) {
                    private final /* synthetic */ DisplayListCanvas f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        View.this.draw(this.f$1);
                    }
                });
            } finally {
                node.end(canvas);
            }
        } else {
            Bitmap bitmap = Bitmap.createBitmap(dm, dm.widthPixels, dm.heightPixels, Bitmap.Config.RGB_565);
            Canvas canvas2 = new Canvas(bitmap);
            try {
                return profileViewOperation(view, new ViewOperation(canvas2) {
                    private final /* synthetic */ Canvas f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        View.this.draw(this.f$1);
                    }
                });
            } finally {
                canvas2.setBitmap(null);
                bitmap.recycle();
            }
        }
    }

    private static long profileViewOperation(View view, ViewOperation operation) {
        CountDownLatch latch = new CountDownLatch(1);
        long[] duration = new long[1];
        view.post(new Runnable(duration, latch) {
            private final /* synthetic */ long[] f$1;
            private final /* synthetic */ CountDownLatch f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

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
        if (captureView != null) {
            CountDownLatch latch = new CountDownLatch(1);
            Bitmap[] cache = new Bitmap[1];
            captureView.post(new Runnable(cache, skipChildren, latch) {
                private final /* synthetic */ Bitmap[] f$1;
                private final /* synthetic */ boolean f$2;
                private final /* synthetic */ CountDownLatch f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    ViewDebug.lambda$performViewCapture$4(View.this, this.f$1, this.f$2, this.f$3);
                }
            });
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

    static /* synthetic */ void lambda$performViewCapture$4(View captureView, Bitmap[] cache, boolean skipChildren, CountDownLatch latch) {
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
            if (out == null) {
                return;
            }
        } catch (Throwable th) {
            if (out != null) {
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
            if (out == null) {
                return;
            }
        } catch (Throwable th) {
            if (out != null) {
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
        if (isRequestedView(group, className, hashCode)) {
            return group;
        }
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                View found = findView((ViewGroup) view, className, hashCode);
                if (found != null) {
                    return found;
                }
            } else if (isRequestedView(view, className, hashCode)) {
                return view;
            }
            if (view.mOverlay != null) {
                View found2 = findView(view.mOverlay.mOverlayViewGroup, className, hashCode);
                if (found2 != null) {
                    return found2;
                }
            }
            if (view instanceof HierarchyHandler) {
                View found3 = ((HierarchyHandler) view).findHierarchyView(className, hashCode);
                if (found3 != null) {
                    return found3;
                }
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
        Context context2 = context;
        ViewGroup viewGroup = group;
        BufferedWriter bufferedWriter = out;
        int i = level;
        boolean z = includeProperties;
        if (dumpView(context2, viewGroup, bufferedWriter, i, z) && !skipChildren) {
            int count = group.getChildCount();
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= count) {
                    break;
                }
                View view = viewGroup.getChildAt(i3);
                if (view instanceof ViewGroup) {
                    dumpViewHierarchy(context2, (ViewGroup) view, bufferedWriter, i + 1, skipChildren, z);
                } else {
                    dumpView(context2, view, bufferedWriter, i + 1, z);
                }
                if (view.mOverlay != null) {
                    ViewOverlay.OverlayViewGroup overlayViewGroup = view.getOverlay().mOverlayViewGroup;
                    ViewOverlay.OverlayViewGroup overlayViewGroup2 = overlayViewGroup;
                    dumpViewHierarchy(context2, overlayViewGroup, bufferedWriter, i + 2, skipChildren, z);
                }
                i2 = i3 + 1;
            }
            if ((viewGroup instanceof HierarchyHandler) != 0) {
                ((HierarchyHandler) viewGroup).dumpViewHierarchyWithProperties(bufferedWriter, i + 1);
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
        Class cls = view.getClass();
        do {
            exportFields(context, view, out, cls, prefix);
            exportMethods(context, view, out, cls, prefix);
            cls = cls.getSuperclass();
        } while (cls != Object.class);
    }

    private static Object callMethodOnAppropriateTheadBlocking(final Method method, Object object) throws IllegalAccessException, InvocationTargetException, TimeoutException {
        if (!(object instanceof View)) {
            return method.invoke(object, null);
        }
        final View view = (View) object;
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
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

    private static void exportMethods(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        int count;
        Method[] methods;
        String str;
        String categoryPrefix;
        ExportedProperty property;
        Context context2 = context;
        BufferedWriter bufferedWriter = out;
        String str2 = prefix;
        Method[] methods2 = getExportedPropertyMethods(klass);
        int count2 = methods2.length;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < count2) {
                Method method = methods2[i2];
                try {
                    Object methodValue = callMethodOnAppropriateTheadBlocking(method, view);
                    Class<?> returnType = method.getReturnType();
                    ExportedProperty property2 = sAnnotations.get(method);
                    if (property2.category().length() != 0) {
                        try {
                            str = property2.category() + SettingsStringUtil.DELIMITER;
                        } catch (IllegalAccessException e) {
                            methods = methods2;
                            count = count2;
                        } catch (InvocationTargetException e2) {
                            methods = methods2;
                            count = count2;
                        } catch (TimeoutException e3) {
                            methods = methods2;
                            count = count2;
                        }
                    } else {
                        str = "";
                    }
                    String categoryPrefix2 = str;
                    if (returnType != Integer.TYPE) {
                        ExportedProperty property3 = property2;
                        if (returnType == int[].class) {
                            Object obj = "()";
                            methods = methods2;
                            String str3 = categoryPrefix2;
                            count = count2;
                            Class<?> cls = returnType;
                            try {
                                exportUnrolledArray(context2, bufferedWriter, property3, (int[]) methodValue, categoryPrefix2 + str2 + method.getName() + '_', "()");
                            } catch (IllegalAccessException | InvocationTargetException | TimeoutException e4) {
                            }
                        } else {
                            methods = methods2;
                            count = count2;
                            ExportedProperty property4 = property3;
                            categoryPrefix = categoryPrefix2;
                            Class<?> returnType2 = returnType;
                            if (returnType2 == String[].class) {
                                String[] array = (String[]) methodValue;
                                if (property4.hasAdjacentMapping() && array != null) {
                                    for (int j = 0; j < array.length; j += 2) {
                                        if (array[j] != null) {
                                            writeEntry(bufferedWriter, categoryPrefix + str2, array[j], "()", array[j + 1] == null ? "null" : array[j + 1]);
                                        }
                                    }
                                }
                            } else {
                                ExportedProperty property5 = property4;
                                if (!returnType2.isPrimitive() && property5.deepExport()) {
                                    dumpViewProperties(context2, methodValue, bufferedWriter, str2 + property5.prefix());
                                }
                            }
                        }
                        i = i2 + 1;
                        methods2 = methods;
                        count2 = count;
                    } else if (!property2.resolveId() || context2 == null) {
                        FlagToString[] flagsMapping = property2.flagMapping();
                        if (flagsMapping.length > 0) {
                            exportUnrolledFlags(bufferedWriter, flagsMapping, ((Integer) methodValue).intValue(), categoryPrefix2 + str2 + method.getName() + '_');
                        }
                        IntToString[] mapping = property2.mapping();
                        if (mapping.length > 0) {
                            int intValue = ((Integer) methodValue).intValue();
                            boolean mapped = false;
                            FlagToString[] flagToStringArr = flagsMapping;
                            int mappingCount = mapping.length;
                            int j2 = 0;
                            while (true) {
                                property = property2;
                                int j3 = j2;
                                if (j3 >= mappingCount) {
                                    IntToString[] intToStringArr = mapping;
                                    break;
                                }
                                int mappingCount2 = mappingCount;
                                IntToString[] mapping2 = mapping;
                                IntToString mapper = mapping[j3];
                                if (mapper.from() == intValue) {
                                    methodValue = mapper.to();
                                    mapped = true;
                                    break;
                                }
                                j2 = j3 + 1;
                                property2 = property;
                                mappingCount = mappingCount2;
                                mapping = mapping2;
                            }
                            if (!mapped) {
                                methodValue = Integer.valueOf(intValue);
                            }
                        } else {
                            property = property2;
                        }
                        methods = methods2;
                        count = count2;
                        categoryPrefix = categoryPrefix2;
                        Class<?> cls2 = returnType;
                        ExportedProperty exportedProperty = property;
                    } else {
                        methodValue = resolveId(context2, ((Integer) methodValue).intValue());
                        methods = methods2;
                        count = count2;
                        categoryPrefix = categoryPrefix2;
                        ExportedProperty exportedProperty2 = property2;
                        Class<?> cls3 = returnType;
                    }
                    writeEntry(bufferedWriter, categoryPrefix + str2, method.getName(), "()", methodValue);
                } catch (IllegalAccessException e5) {
                    methods = methods2;
                    count = count2;
                } catch (InvocationTargetException e6) {
                    methods = methods2;
                    count = count2;
                } catch (TimeoutException e7) {
                    methods = methods2;
                    count = count2;
                }
                i = i2 + 1;
                methods2 = methods;
                count2 = count;
            } else {
                Object obj2 = view;
                Method[] methodArr = methods2;
                int i3 = count2;
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x01c8 A[Catch:{ IllegalAccessException -> 0x01e6 }] */
    private static void exportFields(Context context, Object view, BufferedWriter out, Class<?> klass, String prefix) throws IOException {
        Field[] fields;
        String str;
        Object fieldValue;
        String categoryPrefix;
        Object fieldValue2;
        Class<?> type;
        ExportedProperty property;
        Context context2 = context;
        Object obj = view;
        BufferedWriter bufferedWriter = out;
        String str2 = prefix;
        Field[] fields2 = getExportedPropertyFields(klass);
        int count = fields2.length;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < count) {
                Field field = fields2[i2];
                try {
                    Class<?> type2 = field.getType();
                    ExportedProperty property2 = sAnnotations.get(field);
                    if (property2.category().length() != 0) {
                        try {
                            str = property2.category() + SettingsStringUtil.DELIMITER;
                        } catch (IllegalAccessException e) {
                            fields = fields2;
                        }
                    } else {
                        str = "";
                    }
                    String categoryPrefix2 = str;
                    if (type2 == Integer.TYPE) {
                        fieldValue2 = null;
                        categoryPrefix = categoryPrefix2;
                        property = property2;
                        fields = fields2;
                        type = type2;
                    } else if (type2 == Byte.TYPE) {
                        fieldValue2 = null;
                        categoryPrefix = categoryPrefix2;
                        property = property2;
                        fields = fields2;
                        type = type2;
                    } else {
                        if (type2 == int[].class) {
                            Object obj2 = "";
                            Object fieldValue3 = categoryPrefix2;
                            ExportedProperty exportedProperty = property2;
                            fields = fields2;
                            Class<?> cls = type2;
                            try {
                                exportUnrolledArray(context2, bufferedWriter, property2, (int[]) field.get(obj), categoryPrefix2 + str2 + field.getName() + '_', "");
                            } catch (IllegalAccessException e2) {
                            }
                        } else {
                            categoryPrefix = categoryPrefix2;
                            ExportedProperty property3 = property2;
                            fields = fields2;
                            Class<?> type3 = type2;
                            if (type3 == String[].class) {
                                String[] array = (String[]) field.get(obj);
                                if (property3.hasAdjacentMapping() && array != null) {
                                    for (int j = 0; j < array.length; j += 2) {
                                        if (array[j] != null) {
                                            writeEntry(bufferedWriter, categoryPrefix + str2, array[j], "", array[j + 1] == null ? "null" : array[j + 1]);
                                        }
                                    }
                                }
                            } else {
                                ExportedProperty property4 = property3;
                                if (type3.isPrimitive() || !property4.deepExport()) {
                                    fieldValue = null;
                                    if (fieldValue == null) {
                                        fieldValue = field.get(obj);
                                    }
                                    writeEntry(bufferedWriter, categoryPrefix + str2, field.getName(), "", fieldValue);
                                } else {
                                    dumpViewProperties(context2, field.get(obj), bufferedWriter, str2 + property4.prefix());
                                }
                            }
                        }
                        i = i2 + 1;
                        fields2 = fields;
                    }
                    if (!property.resolveId() || context2 == null) {
                        FlagToString[] flagsMapping = property.flagMapping();
                        if (flagsMapping.length > 0) {
                            exportUnrolledFlags(bufferedWriter, flagsMapping, field.getInt(obj), categoryPrefix + str2 + field.getName() + '_');
                        }
                        IntToString[] mapping = property.mapping();
                        if (mapping.length > 0) {
                            int intValue = field.getInt(obj);
                            int mappingCount = mapping.length;
                            int j2 = 0;
                            while (true) {
                                if (j2 >= mappingCount) {
                                    fieldValue = fieldValue2;
                                    break;
                                }
                                IntToString mapped = mapping[j2];
                                FlagToString[] flagsMapping2 = flagsMapping;
                                if (mapped.from() == intValue) {
                                    fieldValue = mapped.to();
                                    break;
                                } else {
                                    j2++;
                                    flagsMapping = flagsMapping2;
                                }
                            }
                            if (fieldValue == null) {
                                fieldValue = Integer.valueOf(intValue);
                            }
                        } else {
                            fieldValue = fieldValue2;
                        }
                        if (property.formatToHexString()) {
                            fieldValue = field.get(obj);
                            if (type == Integer.TYPE) {
                                fieldValue = formatIntToHexString(((Integer) fieldValue).intValue());
                            } else if (type == Byte.TYPE) {
                                fieldValue = "0x" + Byte.toHexString(((Byte) fieldValue).byteValue(), true);
                            }
                        }
                        if (fieldValue == null) {
                        }
                        writeEntry(bufferedWriter, categoryPrefix + str2, field.getName(), "", fieldValue);
                        i = i2 + 1;
                        fields2 = fields;
                    } else {
                        fieldValue = resolveId(context2, field.getInt(obj));
                        if (fieldValue == null) {
                        }
                        writeEntry(bufferedWriter, categoryPrefix + str2, field.getName(), "", fieldValue);
                        i = i2 + 1;
                        fields2 = fields;
                    }
                } catch (IllegalAccessException e3) {
                    fields = fields2;
                }
            } else {
                return;
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v4, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private static void exportUnrolledArray(Context context, BufferedWriter out, ExportedProperty property, int[] array, String prefix, String suffix) throws IOException {
        Context context2 = context;
        int[] iArr = array;
        IntToString[] indexMapping = property.indexMapping();
        boolean resolveId = true;
        boolean hasIndexMapping = indexMapping.length > 0;
        IntToString[] mapping = property.mapping();
        boolean hasMapping = mapping.length > 0;
        if (!property.resolveId() || context2 == null) {
            resolveId = false;
        }
        int valuesCount = iArr.length;
        for (int j = 0; j < valuesCount; j++) {
            String value = null;
            int intValue = iArr[j];
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
                value = resolveId(context2, intValue);
            }
            writeEntry(out, prefix, name, suffix, value);
        }
        BufferedWriter bufferedWriter = out;
        String str = prefix;
        String str2 = suffix;
    }

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
        String output;
        String str;
        if (value != null) {
            output = "[EXCEPTION]";
            try {
                output = value.toString().replace("\n", "\\n");
            } finally {
                out.write(String.valueOf(output.length()));
                str = ",";
                out.write(str);
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
        for (Field field : klass.getFields()) {
            if (field.isAnnotationPresent(CapturedViewProperty.class)) {
                field.setAccessible(true);
                foundFields.add(field);
            }
        }
        Field[] fields2 = (Field[]) foundFields.toArray(new Field[foundFields.size()]);
        map.put(klass, fields2);
        return fields2;
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
        for (Method method : klass.getMethods()) {
            if (method.getParameterTypes().length == 0 && method.isAnnotationPresent(CapturedViewProperty.class) && method.getReturnType() != Void.class) {
                method.setAccessible(true);
                foundMethods.add(method);
            }
        }
        Method[] methods2 = (Method[]) foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods2);
        return methods2;
    }

    private static String capturedViewExportMethods(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (Method method : capturedViewGetPropertyMethods(klass)) {
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
        for (Field field : capturedViewGetPropertyFields(klass)) {
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

    public static Object invokeViewMethod(View view, Method method, Object[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> result = new AtomicReference<>();
        AtomicReference<Throwable> exception = new AtomicReference<>();
        final AtomicReference<Object> atomicReference = result;
        final Method method2 = method;
        final View view2 = view;
        final Object[] objArr = args;
        final AtomicReference<Throwable> atomicReference2 = exception;
        final CountDownLatch countDownLatch = latch;
        AnonymousClass5 r2 = new Runnable() {
            public void run() {
                try {
                    atomicReference.set(method2.invoke(view2, objArr));
                } catch (InvocationTargetException e) {
                    atomicReference2.set(e.getCause());
                } catch (Exception e2) {
                    atomicReference2.set(e2);
                }
                countDownLatch.countDown();
            }
        };
        view.post(r2);
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
                public void run() {
                    View.this.setLayoutParams(p);
                }
            });
            return;
        }
        throw new RuntimeException("Only integer layout parameters can be set. Field " + param + " is of type " + f.getType().getSimpleName());
    }
}
