package android.app.slice;

import android.app.PendingIntent;
import android.app.slice.Slice;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Process;
import android.os.StrictMode;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class SliceProvider extends ContentProvider {
    private static final boolean DEBUG = false;
    public static final String EXTRA_BIND_URI = "slice_uri";
    public static final String EXTRA_INTENT = "slice_intent";
    public static final String EXTRA_PKG = "pkg";
    public static final String EXTRA_PROVIDER_PKG = "provider_pkg";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_SLICE = "slice";
    public static final String EXTRA_SLICE_DESCENDANTS = "slice_descendants";
    public static final String EXTRA_SUPPORTED_SPECS = "supported_specs";
    public static final String METHOD_GET_DESCENDANTS = "get_descendants";
    public static final String METHOD_GET_PERMISSIONS = "get_permissions";
    public static final String METHOD_MAP_INTENT = "map_slice";
    public static final String METHOD_MAP_ONLY_INTENT = "map_only";
    public static final String METHOD_PIN = "pin";
    public static final String METHOD_SLICE = "bind_slice";
    public static final String METHOD_UNPIN = "unpin";
    private static final long SLICE_BIND_ANR = 2000;
    public static final String SLICE_TYPE = "vnd.android.slice";
    private static final String TAG = "SliceProvider";
    private final Runnable mAnr;
    private final String[] mAutoGrantPermissions;
    private String mCallback;
    private SliceManager mSliceManager;

    public SliceProvider(String... autoGrantPermissions) {
        this.mAnr = new Runnable() {
            public final void run() {
                SliceProvider.lambda$new$0(SliceProvider.this);
            }
        };
        this.mAutoGrantPermissions = autoGrantPermissions;
    }

    public SliceProvider() {
        this.mAnr = new Runnable() {
            public final void run() {
                SliceProvider.lambda$new$0(SliceProvider.this);
            }
        };
        this.mAutoGrantPermissions = new String[0];
    }

    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        this.mSliceManager = (SliceManager) context.getSystemService(SliceManager.class);
    }

    public Slice onBindSlice(Uri sliceUri, Set<SliceSpec> supportedSpecs) {
        return onBindSlice(sliceUri, (List<SliceSpec>) new ArrayList(supportedSpecs));
    }

    @Deprecated
    public Slice onBindSlice(Uri sliceUri, List<SliceSpec> list) {
        return null;
    }

    public void onSlicePinned(Uri sliceUri) {
    }

    public void onSliceUnpinned(Uri sliceUri) {
    }

    public Collection<Uri> onGetSliceDescendants(Uri uri) {
        return Collections.emptyList();
    }

    public Uri onMapIntentToUri(Intent intent) {
        throw new UnsupportedOperationException("This provider has not implemented intent to uri mapping");
    }

    public PendingIntent onCreatePermissionRequest(Uri sliceUri) {
        return createPermissionIntent(getContext(), sliceUri, getCallingPackage());
    }

    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        return null;
    }

    public final Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        return null;
    }

    public final Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public final String getType(Uri uri) {
        return SLICE_TYPE;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals(METHOD_SLICE)) {
            Slice s = handleBindSlice(getUriWithoutUserId((Uri) extras.getParcelable(EXTRA_BIND_URI)), extras.getParcelableArrayList(EXTRA_SUPPORTED_SPECS), getCallingPackage(), Binder.getCallingUid(), Binder.getCallingPid());
            Bundle b = new Bundle();
            b.putParcelable("slice", s);
            return b;
        } else if (method.equals(METHOD_MAP_INTENT)) {
            Intent intent = (Intent) extras.getParcelable(EXTRA_INTENT);
            if (intent == null) {
                return null;
            }
            Uri uri = onMapIntentToUri(intent);
            ArrayList parcelableArrayList = extras.getParcelableArrayList(EXTRA_SUPPORTED_SPECS);
            Bundle b2 = new Bundle();
            if (uri != null) {
                b2.putParcelable("slice", handleBindSlice(uri, parcelableArrayList, getCallingPackage(), Binder.getCallingUid(), Binder.getCallingPid()));
            } else {
                b2.putParcelable("slice", null);
            }
            return b2;
        } else if (method.equals(METHOD_MAP_ONLY_INTENT)) {
            Intent intent2 = (Intent) extras.getParcelable(EXTRA_INTENT);
            if (intent2 == null) {
                return null;
            }
            Uri uri2 = onMapIntentToUri(intent2);
            Bundle b3 = new Bundle();
            b3.putParcelable("slice", uri2);
            return b3;
        } else {
            if (method.equals(METHOD_PIN)) {
                Uri uri3 = getUriWithoutUserId((Uri) extras.getParcelable(EXTRA_BIND_URI));
                if (Binder.getCallingUid() == 1000) {
                    handlePinSlice(uri3);
                } else {
                    throw new SecurityException("Only the system can pin/unpin slices");
                }
            } else if (method.equals(METHOD_UNPIN)) {
                Uri uri4 = getUriWithoutUserId((Uri) extras.getParcelable(EXTRA_BIND_URI));
                if (Binder.getCallingUid() == 1000) {
                    handleUnpinSlice(uri4);
                } else {
                    throw new SecurityException("Only the system can pin/unpin slices");
                }
            } else if (method.equals(METHOD_GET_DESCENDANTS)) {
                Uri uri5 = getUriWithoutUserId((Uri) extras.getParcelable(EXTRA_BIND_URI));
                Bundle b4 = new Bundle();
                b4.putParcelableArrayList(EXTRA_SLICE_DESCENDANTS, new ArrayList(handleGetDescendants(uri5)));
                return b4;
            } else if (method.equals(METHOD_GET_PERMISSIONS)) {
                if (Binder.getCallingUid() == 1000) {
                    Bundle b5 = new Bundle();
                    b5.putStringArray(EXTRA_RESULT, this.mAutoGrantPermissions);
                    return b5;
                }
                throw new SecurityException("Only the system can get permissions");
            }
            return super.call(method, arg, extras);
        }
    }

    private Collection<Uri> handleGetDescendants(Uri uri) {
        this.mCallback = "onGetSliceDescendants";
        return onGetSliceDescendants(uri);
    }

    private void handlePinSlice(Uri sliceUri) {
        this.mCallback = "onSlicePinned";
        Handler.getMain().postDelayed(this.mAnr, SLICE_BIND_ANR);
        try {
            onSlicePinned(sliceUri);
        } finally {
            Handler.getMain().removeCallbacks(this.mAnr);
        }
    }

    private void handleUnpinSlice(Uri sliceUri) {
        this.mCallback = "onSliceUnpinned";
        Handler.getMain().postDelayed(this.mAnr, SLICE_BIND_ANR);
        try {
            onSliceUnpinned(sliceUri);
        } finally {
            Handler.getMain().removeCallbacks(this.mAnr);
        }
    }

    private Slice handleBindSlice(Uri sliceUri, List<SliceSpec> supportedSpecs, String callingPkg, int callingUid, int callingPid) {
        String pkg = callingPkg != null ? callingPkg : getContext().getPackageManager().getNameForUid(callingUid);
        try {
            this.mSliceManager.enforceSlicePermission(sliceUri, pkg, callingPid, callingUid, this.mAutoGrantPermissions);
            this.mCallback = "onBindSlice";
            Handler.getMain().postDelayed(this.mAnr, SLICE_BIND_ANR);
            try {
                return onBindSliceStrict(sliceUri, supportedSpecs);
            } finally {
                Handler.getMain().removeCallbacks(this.mAnr);
            }
        } catch (SecurityException e) {
            return createPermissionSlice(getContext(), sliceUri, pkg);
        }
    }

    /* JADX INFO: finally extract failed */
    public Slice createPermissionSlice(Context context, Uri sliceUri, String callingPackage) {
        this.mCallback = "onCreatePermissionRequest";
        Handler.getMain().postDelayed(this.mAnr, SLICE_BIND_ANR);
        try {
            PendingIntent action = onCreatePermissionRequest(sliceUri);
            Handler.getMain().removeCallbacks(this.mAnr);
            Slice.Builder parent = new Slice.Builder(sliceUri);
            Slice.Builder childAction = new Slice.Builder(parent).addIcon(Icon.createWithResource(context, (int) R.drawable.ic_permission), null, Collections.emptyList()).addHints(Arrays.asList(new String[]{"title", "shortcut"})).addAction(action, new Slice.Builder(parent).build(), null);
            TypedValue tv = new TypedValue();
            new ContextThemeWrapper(context, 16974123).getTheme().resolveAttribute(16843829, tv, true);
            parent.addSubSlice(new Slice.Builder(sliceUri.buildUpon().appendPath(UsbManager.EXTRA_PERMISSION_GRANTED).build()).addIcon(Icon.createWithResource(context, (int) R.drawable.ic_arrow_forward), null, Collections.emptyList()).addText(getPermissionString(context, callingPackage), null, Collections.emptyList()).addInt(tv.data, Slice.SUBTYPE_COLOR, Collections.emptyList()).addSubSlice(childAction.build(), null).build(), null);
            return parent.addHints(Arrays.asList(new String[]{Slice.HINT_PERMISSION_REQUEST})).build();
        } catch (Throwable th) {
            Handler.getMain().removeCallbacks(this.mAnr);
            throw th;
        }
    }

    public static PendingIntent createPermissionIntent(Context context, Uri sliceUri, String callingPackage) {
        Intent intent = new Intent(SliceManager.ACTION_REQUEST_SLICE_PERMISSION);
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SlicePermissionActivity"));
        intent.putExtra(EXTRA_BIND_URI, (Parcelable) sliceUri);
        intent.putExtra(EXTRA_PKG, callingPackage);
        intent.putExtra(EXTRA_PROVIDER_PKG, context.getPackageName());
        intent.setData(sliceUri.buildUpon().appendQueryParameter("package", callingPackage).build());
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public static CharSequence getPermissionString(Context context, String callingPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            return context.getString(R.string.slices_permission_request, pm.getApplicationInfo(callingPackage, 0).loadLabel(pm), context.getApplicationInfo().loadLabel(pm));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Unknown calling app", e);
        }
    }

    /* JADX INFO: finally extract failed */
    private Slice onBindSliceStrict(Uri sliceUri, List<SliceSpec> supportedSpecs) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().build());
            Slice onBindSlice = onBindSlice(sliceUri, (Set<SliceSpec>) new ArraySet(supportedSpecs));
            StrictMode.setThreadPolicy(oldPolicy);
            return onBindSlice;
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(oldPolicy);
            throw th;
        }
    }

    public static /* synthetic */ void lambda$new$0(SliceProvider sliceProvider) {
        Process.sendSignal(Process.myPid(), 3);
        Log.wtf(TAG, "Timed out while handling slice callback " + sliceProvider.mCallback);
    }
}
