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
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.provider.SettingsStringUtil;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LogException;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
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
    private static final String DUMPC_CUST_FIELDSLIST = "dumpc_cust_fields";
    private static final String DUMPC_CUST_METHODSLIST = "dumpc_cust_methods";
    private static final String REMOTE_COMMAND_CAPTURE = "CAPTURE";
    private static final String REMOTE_COMMAND_CAPTURE_LAYERS = "CAPTURE_LAYERS";
    private static final String REMOTE_COMMAND_DUMP = "DUMP";
    private static String REMOTE_COMMAND_DUMP_CUST = SystemProperties.get("ro.config.autotestdump", "DUMPC");
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
    private static boolean mCustomizedDump = false;
    private static String[] mFieldsList = new String[]{"mText", "absolute_x", "absolute_y", "mID", "mLeft", "mTop", "mScrollX", "mScrollY", "x", "y"};
    private static boolean mInited = false;
    private static String[] mMethodsList = new String[]{"isSelected", "isClickable", "isEnabled", "getWidth", "getHeight", "getTag", "isChecked", "isActivated", "getVisibility", "getLayoutParams"};
    private static HashMap<AccessibleObject, ExportedProperty> sAnnotations;
    private static HashMap<Class<?>, Field[]> sCustFieldsForClasses = new HashMap();
    private static HashMap<Class<?>, Method[]> sCustMethodsForClasses = new HashMap();
    private static HashMap<Class<?>, Field[]> sFieldsForClasses;
    private static HashMap<Class<?>, Method[]> sMethodsForClasses;

    interface ViewOperation<T> {
        void post(T... tArr);

        T[] pre();

        void run(T... tArr);
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
            dump(view, false, true, clientStream);
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
        final View view = findView(root, parameter);
        if (view != null) {
            root.post(new Runnable() {
                public void run() {
                    view.requestLayout();
                }
            });
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0045  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void profile(View root, OutputStream clientStream, String parameter) throws IOException {
        Exception e;
        Throwable th;
        View view = findView(root, parameter);
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(clientStream), 32768);
            if (view != null) {
                try {
                    profileViewAndChildren(view, out2);
                } catch (Exception e2) {
                    e = e2;
                    out = out2;
                } catch (Throwable th2) {
                    th = th2;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
            out2.write("-1 -1 -1");
            out2.newLine();
            out2.write("DONE.");
            out2.newLine();
            if (out2 != null) {
                out2.close();
            }
            out = out2;
        } catch (Exception e3) {
            e = e3;
            try {
                Log.w("View", "Problem profiling the view:", e);
                if (out != null) {
                    out.close();
                }
            } catch (Throwable th3) {
                th = th3;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        }
    }

    public static void profileViewAndChildren(View view, BufferedWriter out) throws IOException {
        profileViewAndChildren(view, out, true);
    }

    private static void profileViewAndChildren(final View view, BufferedWriter out, boolean root) throws IOException {
        long durationMeasure;
        long durationLayout;
        long durationDraw;
        if (root || (view.mPrivateFlags & 2048) != 0) {
            durationMeasure = profileViewOperation(view, new ViewOperation<Void>() {
                public Void[] pre() {
                    forceLayout(view);
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
                    view.measure(view.mOldWidthMeasureSpec, view.mOldHeightMeasureSpec);
                }

                public void post(Void... data) {
                }
            });
        } else {
            durationMeasure = 0;
        }
        if (root || (view.mPrivateFlags & 8192) != 0) {
            durationLayout = profileViewOperation(view, new ViewOperation<Void>() {
                public Void[] pre() {
                    return null;
                }

                public void run(Void... data) {
                    view.layout(view.mLeft, view.mTop, view.mRight, view.mBottom);
                }

                public void post(Void... data) {
                }
            });
        } else {
            durationLayout = 0;
        }
        if (!root && (view.willNotDraw() ^ 1) == 0 && (view.mPrivateFlags & 32) == 0) {
            durationDraw = 0;
        } else {
            durationDraw = profileViewOperation(view, new ViewOperation<Object>() {
                public Object[] pre() {
                    Bitmap bitmap;
                    DisplayMetrics metrics = (view == null || view.getResources() == null) ? null : view.getResources().getDisplayMetrics();
                    if (metrics != null) {
                        bitmap = Bitmap.createBitmap(metrics, metrics.widthPixels, metrics.heightPixels, Config.RGB_565);
                    } else {
                        bitmap = null;
                    }
                    Canvas canvas = bitmap != null ? new Canvas(bitmap) : null;
                    return new Object[]{bitmap, canvas};
                }

                public void run(Object... data) {
                    if (data[1] != null) {
                        view.draw((Canvas) data[1]);
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
            });
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
                profileViewAndChildren(group.getChildAt(i), out, false);
            }
        }
    }

    private static <T> long profileViewOperation(View view, final ViewOperation<T> operation) {
        final CountDownLatch latch = new CountDownLatch(1);
        final long[] duration = new long[1];
        view.post(new Runnable() {
            public void run() {
                try {
                    T[] data = operation.pre();
                    long start = Debug.threadCpuTimeNanos();
                    operation.run(data);
                    duration[0] = Debug.threadCpuTimeNanos() - start;
                    operation.post(data);
                } finally {
                    latch.countDown();
                }
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
        boolean localVisible = view.getVisibility() == 0 ? visible : false;
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

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void capture(View root, OutputStream clientStream, View captureView) throws IOException {
        Throwable th;
        Bitmap b = performViewCapture(captureView, false);
        if (b == null) {
            Log.w("View", "Failed to create capture bitmap!");
            b = Bitmap.createBitmap(root.getResources().getDisplayMetrics(), 1, 1, Config.ARGB_8888);
        }
        BufferedOutputStream out = null;
        try {
            BufferedOutputStream out2 = new BufferedOutputStream(clientStream, 32768);
            try {
                b.compress(CompressFormat.PNG, 100, out2);
                out2.flush();
                if (out2 != null) {
                    out2.close();
                }
                b.recycle();
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                    out.close();
                }
                b.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
            }
            b.recycle();
            throw th;
        }
    }

    private static Bitmap performViewCapture(final View captureView, final boolean skipChildren) {
        if (captureView != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            final Bitmap[] cache = new Bitmap[1];
            captureView.post(new Runnable() {
                public void run() {
                    try {
                        cache[0] = captureView.createSnapshot(Config.ARGB_8888, 0, skipChildren);
                    } catch (OutOfMemoryError e) {
                        Log.w("View", "Out of memory for bitmap");
                    } finally {
                        latch.countDown();
                    }
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

    /* JADX WARNING: Removed duplicated region for block: B:25:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x004b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public static void dump(View root, boolean skipChildren, boolean includeProperties, OutputStream clientStream) throws IOException {
        Exception e;
        Throwable th;
        BufferedWriter out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), 32768);
            try {
                View view = root.getRootView();
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    dumpViewHierarchy(group.getContext(), group, out, 0, skipChildren, includeProperties);
                }
                out.write("DONE.");
                out.newLine();
                if (out != null) {
                    out.close();
                }
            } catch (Exception e2) {
                e = e2;
                try {
                    Log.w("View", "Problem dumping the view:", e);
                    if (out == null) {
                        out.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
        } catch (Exception e3) {
            e = e3;
            out = null;
            Log.w("View", "Problem dumping the view:", e);
            if (out == null) {
            }
        } catch (Throwable th3) {
            th = th3;
            out = null;
            if (out != null) {
                out.close();
            }
            throw th;
        }
    }

    public static void dumpv2(final View view, ByteArrayOutputStream out) throws InterruptedException {
        final ViewHierarchyEncoder encoder = new ViewHierarchyEncoder(out);
        final CountDownLatch latch = new CountDownLatch(1);
        view.post(new Runnable() {
            public void run() {
                encoder.addProperty("window:left", view.mAttachInfo.mWindowLeft);
                encoder.addProperty("window:top", view.mAttachInfo.mWindowTop);
                view.encode(encoder);
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);
        encoder.endStream();
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:34:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0082  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void dumpTheme(View view, OutputStream clientStream) throws IOException {
        Exception e;
        Throwable th;
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), 32768);
            try {
                String[] attributes = getStyleAttributesDump(view.getContext().getResources(), view.getContext().getTheme());
                if (attributes != null) {
                    for (int i = 0; i < attributes.length; i += 2) {
                        if (attributes[i] != null) {
                            out2.write(attributes[i] + "\n");
                            out2.write(attributes[i + 1] + "\n");
                        }
                    }
                }
                out2.write("DONE.");
                out2.newLine();
                if (out2 != null) {
                    out2.close();
                }
                out = out2;
            } catch (Exception e2) {
                e = e2;
                out = out2;
                try {
                    Log.w("View", "Problem dumping View Theme:", e);
                    if (out == null) {
                        out.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        out.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.w("View", "Problem dumping View Theme:", e);
            if (out == null) {
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
            sFieldsForClasses = new HashMap();
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap(512);
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
            sAnnotations = new HashMap(512);
        }
        HashMap<Class<?>, Method[]> map = getMethodMap();
        Method[] methods = (Method[]) map.get(klass);
        if (methods != null) {
            return methods;
        }
        methods = klass.getDeclaredMethodsUnchecked(false);
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
        dumpViewProperties(context, view, out, LogException.NO_VALUE);
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
            return method.invoke(object, (Object[]) null);
        }
        final View view = (View) object;
        FutureTask<Object> future = new FutureTask(new Callable<Object>() {
            public Object call() throws IllegalAccessException, InvocationTargetException {
                return method.invoke(view, (Object[]) null);
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
        for (Method method : getExportedPropertyMethods(klass)) {
            try {
                Object methodValue = callMethodOnAppropriateTheadBlocking(method, view);
                Class<?> returnType = method.getReturnType();
                ExportedProperty property = (ExportedProperty) sAnnotations.get(method);
                String categoryPrefix = property.category().length() != 0 ? property.category() + SettingsStringUtil.DELIMITER : LogException.NO_VALUE;
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
                        boolean mapped = false;
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
            Object fieldValue = null;
            try {
                Class<?> type = field.getType();
                ExportedProperty property = (ExportedProperty) sAnnotations.get(field);
                String categoryPrefix = property.category().length() != 0 ? property.category() + SettingsStringUtil.DELIMITER : LogException.NO_VALUE;
                int j;
                if (type != Integer.TYPE && type != Byte.TYPE) {
                    if (type == int[].class) {
                        int[] array = (int[]) field.get(view);
                        String valuePrefix = categoryPrefix + prefix + field.getName() + '_';
                        String suffix = LogException.NO_VALUE;
                        exportUnrolledArray(context, out, property, array, valuePrefix, LogException.NO_VALUE);
                    } else if (type == String[].class) {
                        String[] array2 = (String[]) field.get(view);
                        if (property.hasAdjacentMapping() && array2 != null) {
                            for (j = 0; j < array2.length; j += 2) {
                                if (array2[j] != null) {
                                    writeEntry(out, categoryPrefix + prefix, array2[j], LogException.NO_VALUE, array2[j + 1] == null ? "null" : array2[j + 1]);
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
                                fieldValue = mapped.to();
                                break;
                            }
                        }
                        if (fieldValue == null) {
                            fieldValue = Integer.valueOf(intValue);
                        }
                    }
                    if (property.formatToHexString()) {
                        fieldValue = field.get(view);
                        if (type == Integer.TYPE) {
                            fieldValue = formatIntToHexString(((Integer) fieldValue).intValue());
                        } else if (type == Byte.TYPE) {
                            fieldValue = "0x" + Byte.toHexString(((Byte) fieldValue).byteValue(), true);
                        }
                    }
                } else {
                    fieldValue = resolveId(context, field.getInt(view));
                }
                if (fieldValue == null) {
                    fieldValue = field.get(view);
                }
                writeEntry(out, categoryPrefix + prefix, field.getName(), LogException.NO_VALUE, fieldValue);
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
            int maskResult = intValue & flagMapping.mask();
            boolean test = maskResult == flagMapping.equals();
            if ((test && ifTrue) || !(test || (ifTrue ^ 1) == 0)) {
                writeEntry(out, prefix, flagMapping.name(), LogException.NO_VALUE, formatIntToHexString(maskResult));
            }
        }
    }

    private static void exportUnrolledArray(Context context, BufferedWriter out, ExportedProperty property, int[] array, String prefix, String suffix) throws IOException {
        IntToString[] indexMapping = property.indexMapping();
        boolean hasIndexMapping = indexMapping.length > 0;
        IntToString[] mapping = property.mapping();
        boolean hasMapping = mapping.length > 0;
        boolean resolveId = property.resolveId() && context != null;
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
                out.write(",");
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
        sb.append(capturedViewExportFields(view, klass, LogException.NO_VALUE));
        sb.append(capturedViewExportMethods(view, klass, LogException.NO_VALUE));
        Log.d(tag, sb.toString());
    }

    public static Object invokeViewMethod(View view, Method method, Object[] args) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Object> result = new AtomicReference();
        final AtomicReference<Throwable> exception = new AtomicReference();
        final Method method2 = method;
        final View view2 = view;
        final Object[] objArr = args;
        view.post(new Runnable() {
            public void run() {
                try {
                    result.set(method2.invoke(view2, objArr));
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
            throw new RuntimeException((Throwable) exception.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLayoutParameter(final View view, String param, int value) throws NoSuchFieldException, IllegalAccessException {
        final LayoutParams p = view.getLayoutParams();
        Field f = p.getClass().getField(param);
        if (f.getType() != Integer.TYPE) {
            throw new RuntimeException("Only integer layout parameters can be set. Field " + param + " is of type " + f.getType().getSimpleName());
        }
        f.set(p, Integer.valueOf(value));
        view.post(new Runnable() {
            public void run() {
                view.-wrap18(p);
            }
        });
    }

    private static void initCustomizedList(Context context) {
        if (!mInited && context != null) {
            mInited = true;
            try {
                String fields = Systemex.getString(context.getContentResolver(), DUMPC_CUST_FIELDSLIST);
                String methods = Systemex.getString(context.getContentResolver(), DUMPC_CUST_METHODSLIST);
                Log.i("ViewServer", "fields:" + fields + ", methods:" + methods + ", REMOTE_COMMAND_DUMP_CUST:" + REMOTE_COMMAND_DUMP_CUST);
                mFieldsList = fields != null ? fields.split(",") : mFieldsList;
                mMethodsList = methods != null ? methods.split(",") : mMethodsList;
            } catch (Exception e) {
                Log.e("ViewDebug", "Could not load fields or methods from database.", e);
            }
        }
    }

    private static void customizedDump(View root, OutputStream clientStream) throws IOException {
        mCustomizedDump = true;
        initCustomizedList(root.getContext());
        try {
            dump(root, false, true, clientStream);
            mCustomizedDump = false;
        } catch (IOException e) {
            throw e;
        } catch (Throwable th) {
            mCustomizedDump = false;
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
        return false;
    }

    private static HashMap<Class<?>, Field[]> getFieldsMap() {
        return mCustomizedDump ? sCustFieldsForClasses : sFieldsForClasses;
    }

    private static HashMap<Class<?>, Method[]> getMethodMap() {
        return mCustomizedDump ? sCustMethodsForClasses : sMethodsForClasses;
    }

    private static Field[] getFields(Class<?> klass) {
        Field[] fields = klass.getDeclaredFieldsUnchecked(false);
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
