package android.content;

import android.accounts.Account;
import android.annotation.RequiresPermission.Read;
import android.annotation.RequiresPermission.Write;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.backup.FullBackup;
import android.content.ISyncStatusObserver.Stub;
import android.content.SyncRequest.Builder;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.CrossProcessCursorWrapper;
import android.database.Cursor;
import android.database.IContentObserver;
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.DeadObjectException;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Intents;
import android.security.KeyChain;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ContentResolver {
    public static final Intent ACTION_SYNC_CONN_STATUS_CHANGED = null;
    public static final String ANY_CURSOR_ITEM_TYPE = "vnd.android.cursor.item/*";
    public static final String CONTENT_SERVICE_NAME = "content";
    public static final String CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir";
    public static final String CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item";
    private static final boolean ENABLE_CONTENT_SAMPLE = false;
    public static final String EXTRA_SIZE = "android.content.extra.SIZE";
    public static final int NOTIFY_SKIP_NOTIFY_FOR_DESCENDANTS = 2;
    public static final int NOTIFY_SYNC_TO_NETWORK = 1;
    public static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_FILE = "file";
    private static final int SLOW_THRESHOLD_MILLIS = 500;
    public static final int SYNC_ERROR_AUTHENTICATION = 2;
    public static final int SYNC_ERROR_CONFLICT = 5;
    public static final int SYNC_ERROR_INTERNAL = 8;
    public static final int SYNC_ERROR_IO = 3;
    private static final String[] SYNC_ERROR_NAMES = null;
    public static final int SYNC_ERROR_PARSE = 4;
    public static final int SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS = 1;
    public static final int SYNC_ERROR_TOO_MANY_DELETIONS = 6;
    public static final int SYNC_ERROR_TOO_MANY_RETRIES = 7;
    @Deprecated
    public static final String SYNC_EXTRAS_ACCOUNT = "account";
    public static final String SYNC_EXTRAS_DISALLOW_METERED = "allow_metered";
    public static final String SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS = "discard_deletions";
    public static final String SYNC_EXTRAS_DO_NOT_RETRY = "do_not_retry";
    public static final String SYNC_EXTRAS_EXPECTED_DOWNLOAD = "expected_download";
    public static final String SYNC_EXTRAS_EXPECTED_UPLOAD = "expected_upload";
    public static final String SYNC_EXTRAS_EXPEDITED = "expedited";
    @Deprecated
    public static final String SYNC_EXTRAS_FORCE = "force";
    public static final String SYNC_EXTRAS_IGNORE_BACKOFF = "ignore_backoff";
    public static final String SYNC_EXTRAS_IGNORE_SETTINGS = "ignore_settings";
    public static final String SYNC_EXTRAS_INITIALIZE = "initialize";
    public static final String SYNC_EXTRAS_MANUAL = "force";
    public static final String SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS = "deletions_override";
    public static final String SYNC_EXTRAS_PRIORITY = "sync_priority";
    public static final String SYNC_EXTRAS_REQUIRE_CHARGING = "require_charging";
    public static final String SYNC_EXTRAS_UPLOAD = "upload";
    public static final int SYNC_OBSERVER_TYPE_ACTIVE = 4;
    public static final int SYNC_OBSERVER_TYPE_ALL = Integer.MAX_VALUE;
    public static final int SYNC_OBSERVER_TYPE_PENDING = 2;
    public static final int SYNC_OBSERVER_TYPE_SETTINGS = 1;
    public static final int SYNC_OBSERVER_TYPE_STATUS = 8;
    private static final String TAG = "ContentResolver";
    private static IContentService sContentService;
    private final Context mContext;
    final String mPackageName;
    private final Random mRandom;

    /* renamed from: android.content.ContentResolver.1 */
    static class AnonymousClass1 extends Stub {
        final /* synthetic */ SyncStatusObserver val$callback;

        AnonymousClass1(SyncStatusObserver val$callback) {
            this.val$callback = val$callback;
        }

        public void onStatusChanged(int which) throws RemoteException {
            this.val$callback.onStatusChanged(which);
        }
    }

    private final class CursorWrapperInner extends CrossProcessCursorWrapper {
        private final CloseGuard mCloseGuard;
        private final IContentProvider mContentProvider;
        private final AtomicBoolean mProviderReleased;

        CursorWrapperInner(Cursor cursor, IContentProvider contentProvider) {
            super(cursor);
            this.mProviderReleased = new AtomicBoolean();
            this.mCloseGuard = CloseGuard.get();
            this.mContentProvider = contentProvider;
            this.mCloseGuard.open("close");
        }

        public void close() {
            this.mCloseGuard.close();
            super.close();
            if (this.mProviderReleased.compareAndSet(ContentResolver.ENABLE_CONTENT_SAMPLE, true)) {
                ContentResolver.this.releaseProvider(this.mContentProvider);
            }
        }

        protected void finalize() throws Throwable {
            try {
                this.mCloseGuard.warnIfOpen();
                close();
            } finally {
                super.finalize();
            }
        }
    }

    public class OpenResourceIdResult {
        public int id;
        public Resources r;
    }

    private final class ParcelFileDescriptorInner extends ParcelFileDescriptor {
        private final IContentProvider mContentProvider;
        private final AtomicBoolean mProviderReleased;

        ParcelFileDescriptorInner(ParcelFileDescriptor pfd, IContentProvider icp) {
            super(pfd);
            this.mProviderReleased = new AtomicBoolean();
            this.mContentProvider = icp;
        }

        public void releaseResources() {
            if (this.mProviderReleased.compareAndSet(ContentResolver.ENABLE_CONTENT_SAMPLE, true)) {
                ContentResolver.this.releaseProvider(this.mContentProvider);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.ContentResolver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.ContentResolver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentResolver.<clinit>():void");
    }

    protected abstract IContentProvider acquireProvider(Context context, String str);

    protected abstract IContentProvider acquireUnstableProvider(Context context, String str);

    public final android.database.Cursor query(@android.annotation.RequiresPermission.Read android.net.Uri r28, java.lang.String[] r29, java.lang.String r30, java.lang.String[] r31, java.lang.String r32, android.os.CancellationSignal r33) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00e4 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r27 = this;
        r5 = "uri";
        r0 = r28;
        com.android.internal.util.Preconditions.checkNotNull(r0, r5);
        r5 = 1;
        r0 = r28;
        r5 = android.hsm.HwSystemManager.allowOp(r0, r5);
        if (r5 != 0) goto L_0x0016;
    L_0x0011:
        r5 = android.hsm.HwSystemManager.getDummyCursor(r27, r28, r29, r30, r31, r32);
        return r5;
    L_0x0016:
        r4 = r27.acquireUnstableProvider(r28);
        if (r4 != 0) goto L_0x001e;
    L_0x001c:
        r5 = 0;
        return r5;
    L_0x001e:
        r12 = 0;
        r23 = 0;
        r24 = android.os.SystemClock.uptimeMillis();	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r11 = 0;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        if (r33 == 0) goto L_0x0034;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
    L_0x0028:
        r33.throwIfCanceled();	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r11 = r4.createCancellationSignal();	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r0 = r33;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r0.setRemote(r11);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
    L_0x0034:
        r0 = r27;	 Catch:{ DeadObjectException -> 0x0065 }
        r5 = r0.mPackageName;	 Catch:{ DeadObjectException -> 0x0065 }
        r6 = r28;	 Catch:{ DeadObjectException -> 0x0065 }
        r7 = r29;	 Catch:{ DeadObjectException -> 0x0065 }
        r8 = r30;	 Catch:{ DeadObjectException -> 0x0065 }
        r9 = r31;	 Catch:{ DeadObjectException -> 0x0065 }
        r10 = r32;	 Catch:{ DeadObjectException -> 0x0065 }
        r23 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ DeadObjectException -> 0x0065 }
    L_0x0046:
        if (r23 != 0) goto L_0x009e;
    L_0x0048:
        r5 = 0;
        if (r23 == 0) goto L_0x004e;
    L_0x004b:
        r23.close();
    L_0x004e:
        if (r33 == 0) goto L_0x0056;
    L_0x0050:
        r6 = 0;
        r0 = r33;
        r0.setRemote(r6);
    L_0x0056:
        if (r4 == 0) goto L_0x005d;
    L_0x0058:
        r0 = r27;
        r0.releaseUnstableProvider(r4);
    L_0x005d:
        if (r12 == 0) goto L_0x0064;
    L_0x005f:
        r0 = r27;
        r0.releaseProvider(r12);
    L_0x0064:
        return r5;
    L_0x0065:
        r20 = move-exception;
        r0 = r27;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r0.unstableProviderDied(r4);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r12 = r27.acquireProvider(r28);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        if (r12 != 0) goto L_0x0089;
    L_0x0071:
        r5 = 0;
        if (r33 == 0) goto L_0x007a;
    L_0x0074:
        r6 = 0;
        r0 = r33;
        r0.setRemote(r6);
    L_0x007a:
        if (r4 == 0) goto L_0x0081;
    L_0x007c:
        r0 = r27;
        r0.releaseUnstableProvider(r4);
    L_0x0081:
        if (r12 == 0) goto L_0x0088;
    L_0x0083:
        r0 = r27;
        r0.releaseProvider(r12);
    L_0x0088:
        return r5;
    L_0x0089:
        r0 = r27;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r13 = r0.mPackageName;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r14 = r28;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r15 = r29;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r16 = r30;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r17 = r31;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r18 = r32;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r19 = r11;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r23 = r12.query(r13, r14, r15, r16, r17, r18, r19);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        goto L_0x0046;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
    L_0x009e:
        r23.getCount();	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r6 = android.os.SystemClock.uptimeMillis();	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r14 = r6 - r24;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r13 = r27;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r16 = r28;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r17 = r29;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r18 = r30;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r19 = r32;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r13.maybeLogQueryToEventLog(r14, r16, r17, r18, r19);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        if (r12 == 0) goto L_0x00d8;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
    L_0x00b6:
        r22 = r12;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
    L_0x00b8:
        r26 = new android.content.ContentResolver$CursorWrapperInner;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r0 = r26;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r1 = r27;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r2 = r23;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r3 = r22;	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r0.<init>(r2, r3);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        r12 = 0;
        r23 = 0;
        if (r33 == 0) goto L_0x00d0;
    L_0x00ca:
        r5 = 0;
        r0 = r33;
        r0.setRemote(r5);
    L_0x00d0:
        if (r4 == 0) goto L_0x00d7;
    L_0x00d2:
        r0 = r27;
        r0.releaseUnstableProvider(r4);
    L_0x00d7:
        return r26;
    L_0x00d8:
        r22 = r27.acquireProvider(r28);	 Catch:{ RemoteException -> 0x00dd, all -> 0x00fb }
        goto L_0x00b8;
    L_0x00dd:
        r21 = move-exception;
        r5 = 0;
        if (r23 == 0) goto L_0x00e4;
    L_0x00e1:
        r23.close();
    L_0x00e4:
        if (r33 == 0) goto L_0x00ec;
    L_0x00e6:
        r6 = 0;
        r0 = r33;
        r0.setRemote(r6);
    L_0x00ec:
        if (r4 == 0) goto L_0x00f3;
    L_0x00ee:
        r0 = r27;
        r0.releaseUnstableProvider(r4);
    L_0x00f3:
        if (r12 == 0) goto L_0x00fa;
    L_0x00f5:
        r0 = r27;
        r0.releaseProvider(r12);
    L_0x00fa:
        return r5;
    L_0x00fb:
        r5 = move-exception;
        if (r23 == 0) goto L_0x0101;
    L_0x00fe:
        r23.close();
    L_0x0101:
        if (r33 == 0) goto L_0x0109;
    L_0x0103:
        r6 = 0;
        r0 = r33;
        r0.setRemote(r6);
    L_0x0109:
        if (r4 == 0) goto L_0x0110;
    L_0x010b:
        r0 = r27;
        r0.releaseUnstableProvider(r4);
    L_0x0110:
        if (r12 == 0) goto L_0x0117;
    L_0x0112:
        r0 = r27;
        r0.releaseProvider(r12);
    L_0x0117:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentResolver.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, android.os.CancellationSignal):android.database.Cursor");
    }

    public abstract boolean releaseProvider(IContentProvider iContentProvider);

    public abstract boolean releaseUnstableProvider(IContentProvider iContentProvider);

    public abstract void unstableProviderDied(IContentProvider iContentProvider);

    public static String syncErrorToString(int error) {
        if (error < SYNC_OBSERVER_TYPE_SETTINGS || error > SYNC_ERROR_NAMES.length) {
            return String.valueOf(error);
        }
        return SYNC_ERROR_NAMES[error - 1];
    }

    public static int syncErrorStringToInt(String error) {
        int n = SYNC_ERROR_NAMES.length;
        for (int i = 0; i < n; i += SYNC_OBSERVER_TYPE_SETTINGS) {
            if (SYNC_ERROR_NAMES[i].equals(error)) {
                return i + SYNC_OBSERVER_TYPE_SETTINGS;
            }
        }
        if (error != null) {
            try {
                return Integer.parseInt(error);
            } catch (NumberFormatException e) {
                Log.d(TAG, "error parsing sync error: " + error);
            }
        }
        return 0;
    }

    public ContentResolver(Context context) {
        this.mRandom = new Random();
        if (context == null) {
            context = ActivityThread.currentApplication();
        }
        this.mContext = context;
        this.mPackageName = this.mContext.getOpPackageName();
    }

    protected IContentProvider acquireExistingProvider(Context c, String name) {
        return acquireProvider(c, name);
    }

    public void appNotRespondingViaProvider(IContentProvider icp) {
        throw new UnsupportedOperationException("appNotRespondingViaProvider");
    }

    public final String getType(Uri url) {
        String str = ImageMappingColumns.URL;
        Preconditions.checkNotNull(url, str);
        IContentProvider provider = acquireExistingProvider(url);
        if (provider != null) {
            try {
                str = provider.getType(url);
                return str;
            } catch (RemoteException e) {
                return null;
            } catch (Exception e2) {
                str = TAG;
                Log.w(str, "Failed to get type for: " + url + " (" + e2.getMessage() + ")");
                return null;
            } finally {
                releaseProvider(provider);
            }
        } else if (!SCHEME_CONTENT.equals(url.getScheme())) {
            return null;
        } else {
            try {
                return ActivityManagerNative.getDefault().getProviderMimeType(ContentProvider.getUriWithoutUserId(url), resolveUserId(url));
            } catch (RemoteException e3) {
                return null;
            } catch (Exception e22) {
                Log.w(TAG, "Failed to get type for: " + url + " (" + e22.getMessage() + ")");
                return null;
            }
        }
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) {
        Preconditions.checkNotNull(url, ImageMappingColumns.URL);
        String[] strArr = "mimeTypeFilter";
        Preconditions.checkNotNull(mimeTypeFilter, strArr);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            strArr = provider.getStreamTypes(url, mimeTypeFilter);
            return strArr;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Cursor query(@Read Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder, null);
    }

    public final Uri canonicalize(Uri url) {
        Uri uri = ImageMappingColumns.URL;
        Preconditions.checkNotNull(url, uri);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            uri = provider.canonicalize(this.mPackageName, url);
            return uri;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Uri uncanonicalize(Uri url) {
        Uri uri = ImageMappingColumns.URL;
        Preconditions.checkNotNull(url, uri);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            uri = provider.uncanonicalize(this.mPackageName, url);
            return uri;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final InputStream openInputStream(Uri uri) throws FileNotFoundException {
        InputStream inputStream = null;
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            OpenResourceIdResult r = getResourceId(uri);
            try {
                return r.r.openRawResource(r.id);
            } catch (NotFoundException e) {
                throw new FileNotFoundException("Resource does not exist: " + uri);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            return new FileInputStream(uri.getPath());
        } else {
            AssetFileDescriptor fd = openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN, null);
            if (fd != null) {
                try {
                    inputStream = fd.createInputStream();
                } catch (IOException e2) {
                    throw new FileNotFoundException("Unable to create stream");
                }
            }
            return inputStream;
        }
    }

    public final OutputStream openOutputStream(Uri uri) throws FileNotFoundException {
        return openOutputStream(uri, "w");
    }

    public final OutputStream openOutputStream(Uri uri, String mode) throws FileNotFoundException {
        OutputStream outputStream = null;
        AssetFileDescriptor fd = openAssetFileDescriptor(uri, mode, null);
        if (fd != null) {
            try {
                outputStream = fd.createOutputStream();
            } catch (IOException e) {
                throw new FileNotFoundException("Unable to create stream");
            }
        }
        return outputStream;
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri, String mode) throws FileNotFoundException {
        return openFileDescriptor(uri, mode, null);
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
        AssetFileDescriptor afd = openAssetFileDescriptor(uri, mode, cancellationSignal);
        if (afd == null) {
            return null;
        }
        if (afd.getDeclaredLength() < 0) {
            return afd.getParcelFileDescriptor();
        }
        try {
            afd.close();
        } catch (IOException e) {
        }
        throw new FileNotFoundException("Not a whole file");
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode) throws FileNotFoundException {
        return openAssetFileDescriptor(uri, mode, null);
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        Preconditions.checkNotNull(mode, Intents.EXTRA_MODE);
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            if (FullBackup.ROOT_TREE_TOKEN.equals(mode)) {
                OpenResourceIdResult r = getResourceId(uri);
                try {
                    return r.r.openRawResourceFd(r.id);
                } catch (NotFoundException e) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            }
            throw new FileNotFoundException("Can't write resources: " + uri);
        } else if (SCHEME_FILE.equals(scheme)) {
            return new AssetFileDescriptor(ParcelFileDescriptor.open(new File(uri.getPath()), ParcelFileDescriptor.parseMode(mode)), 0, -1);
        } else {
            if (FullBackup.ROOT_TREE_TOKEN.equals(mode)) {
                return openTypedAssetFileDescriptor(uri, "*/*", null, cancellationSignal);
            }
            IContentProvider unstableProvider = acquireUnstableProvider(uri);
            if (unstableProvider == null) {
                throw new FileNotFoundException("No content provider: " + uri);
            }
            AssetFileDescriptor fd;
            IContentProvider iContentProvider = null;
            ICancellationSignal iCancellationSignal = null;
            if (cancellationSignal != null) {
                try {
                    cancellationSignal.throwIfCanceled();
                    iCancellationSignal = unstableProvider.createCancellationSignal();
                    cancellationSignal.setRemote(iCancellationSignal);
                } catch (DeadObjectException e2) {
                    unstableProviderDied(unstableProvider);
                    iContentProvider = acquireProvider(uri);
                    if (iContentProvider == null) {
                        throw new FileNotFoundException("No content provider: " + uri);
                    }
                    fd = iContentProvider.openAssetFile(this.mPackageName, uri, mode, iCancellationSignal);
                    if (fd == null) {
                        if (cancellationSignal != null) {
                            cancellationSignal.setRemote(null);
                        }
                        if (iContentProvider != null) {
                            releaseProvider(iContentProvider);
                        }
                        if (unstableProvider != null) {
                            releaseUnstableProvider(unstableProvider);
                        }
                        return null;
                    }
                } catch (RemoteException e3) {
                    try {
                        throw new FileNotFoundException("Failed opening content provider: " + uri);
                    } catch (Throwable th) {
                        if (cancellationSignal != null) {
                            cancellationSignal.setRemote(null);
                        }
                        if (iContentProvider != null) {
                            releaseProvider(iContentProvider);
                        }
                        if (unstableProvider != null) {
                            releaseUnstableProvider(unstableProvider);
                        }
                    }
                } catch (FileNotFoundException e4) {
                    throw e4;
                }
            }
            fd = unstableProvider.openAssetFile(this.mPackageName, uri, mode, iCancellationSignal);
            if (fd == null) {
                if (cancellationSignal != null) {
                    cancellationSignal.setRemote(null);
                }
                if (unstableProvider != null) {
                    releaseUnstableProvider(unstableProvider);
                }
                return null;
            }
            if (iContentProvider == null) {
                iContentProvider = acquireProvider(uri);
            }
            releaseUnstableProvider(unstableProvider);
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), iContentProvider), fd.getStartOffset(), fd.getDeclaredLength());
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            return assetFileDescriptor;
        }
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) throws FileNotFoundException {
        return openTypedAssetFileDescriptor(uri, mimeType, opts, null);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal cancellationSignal) throws FileNotFoundException {
        AssetFileDescriptor fd;
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        Preconditions.checkNotNull(mimeType, "mimeType");
        IContentProvider unstableProvider = acquireUnstableProvider(uri);
        if (unstableProvider == null) {
            throw new FileNotFoundException("No content provider: " + uri);
        }
        IContentProvider iContentProvider = null;
        ICancellationSignal iCancellationSignal = null;
        if (cancellationSignal != null) {
            try {
                cancellationSignal.throwIfCanceled();
                iCancellationSignal = unstableProvider.createCancellationSignal();
                cancellationSignal.setRemote(iCancellationSignal);
            } catch (DeadObjectException e) {
                unstableProviderDied(unstableProvider);
                iContentProvider = acquireProvider(uri);
                if (iContentProvider == null) {
                    throw new FileNotFoundException("No content provider: " + uri);
                }
                fd = iContentProvider.openTypedAssetFile(this.mPackageName, uri, mimeType, opts, iCancellationSignal);
                if (fd == null) {
                    if (cancellationSignal != null) {
                        cancellationSignal.setRemote(null);
                    }
                    if (iContentProvider != null) {
                        releaseProvider(iContentProvider);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    return null;
                }
            } catch (RemoteException e2) {
                try {
                    throw new FileNotFoundException("Failed opening content provider: " + uri);
                } catch (Throwable th) {
                    if (cancellationSignal != null) {
                        cancellationSignal.setRemote(null);
                    }
                    if (iContentProvider != null) {
                        releaseProvider(iContentProvider);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                }
            } catch (FileNotFoundException e3) {
                throw e3;
            }
        }
        fd = unstableProvider.openTypedAssetFile(this.mPackageName, uri, mimeType, opts, iCancellationSignal);
        if (fd == null) {
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            return null;
        }
        if (iContentProvider == null) {
            iContentProvider = acquireProvider(uri);
        }
        releaseUnstableProvider(unstableProvider);
        AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), iContentProvider), fd.getStartOffset(), fd.getDeclaredLength());
        if (cancellationSignal != null) {
            cancellationSignal.setRemote(null);
        }
        return assetFileDescriptor;
    }

    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        }
        try {
            Resources r = this.mContext.getPackageManager().getResourcesForApplication(authority);
            List<String> path = uri.getPathSegments();
            if (path == null) {
                throw new FileNotFoundException("No path: " + uri);
            }
            int id;
            int len = path.size();
            if (len == SYNC_OBSERVER_TYPE_SETTINGS) {
                try {
                    id = Integer.parseInt((String) path.get(0));
                } catch (NumberFormatException e) {
                    throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
                }
            } else if (len == SYNC_OBSERVER_TYPE_PENDING) {
                id = r.getIdentifier((String) path.get(SYNC_OBSERVER_TYPE_SETTINGS), (String) path.get(0), authority);
            } else {
                throw new FileNotFoundException("More than two path segments: " + uri);
            }
            if (id == 0) {
                throw new FileNotFoundException("No resource found for: " + uri);
            }
            OpenResourceIdResult res = new OpenResourceIdResult();
            res.r = r;
            res.id = id;
            return res;
        } catch (NameNotFoundException e2) {
            throw new FileNotFoundException("No package found for authority: " + uri);
        }
    }

    public final Uri insert(@Write Uri url, ContentValues values) {
        Preconditions.checkNotNull(url, ImageMappingColumns.URL);
        if (!HwSystemManager.allowOp(url, (int) SYNC_OBSERVER_TYPE_PENDING)) {
            return null;
        }
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        try {
            long startTime = SystemClock.uptimeMillis();
            Uri createdRow = provider.insert(this.mPackageName, url, values);
            maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "insert", null);
            return createdRow;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Preconditions.checkNotNull(authority, Directory.DIRECTORY_AUTHORITY);
        Preconditions.checkNotNull(operations, "operations");
        ContentProviderClient provider = acquireContentProviderClient(authority);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown authority " + authority);
        }
        try {
            ContentProviderResult[] applyBatch = provider.applyBatch(HwSystemManager.getAllowedApplyBatchOp(authority, operations));
            return applyBatch;
        } finally {
            provider.release();
        }
    }

    public final int bulkInsert(@Write Uri url, ContentValues[] values) {
        Preconditions.checkNotNull(url, ImageMappingColumns.URL);
        Preconditions.checkNotNull(values, "values");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        try {
            if (!HwSystemManager.allowOp(url, (int) SYNC_OBSERVER_TYPE_PENDING)) {
                return 0;
            }
            long startTime = SystemClock.uptimeMillis();
            int rowsCreated = provider.bulkInsert(this.mPackageName, url, values);
            maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "bulkinsert", null);
            releaseProvider(provider);
            return rowsCreated;
        } catch (RemoteException e) {
            return 0;
        } finally {
            releaseProvider(provider);
        }
    }

    public final int delete(@Write Uri url, String where, String[] selectionArgs) {
        Preconditions.checkNotNull(url, ImageMappingColumns.URL);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        int i;
        try {
            if (HwSystemManager.allowOp(url, (int) SYNC_ERROR_IO)) {
                long startTime = SystemClock.uptimeMillis();
                int rowsDeleted = provider.delete(this.mPackageName, url, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "delete", where);
                releaseProvider(provider);
                return rowsDeleted;
            }
            i = 0;
            return i;
        } catch (RemoteException e) {
            i = -1;
            return i;
        } finally {
            releaseProvider(provider);
        }
    }

    public final int update(@Write Uri uri, ContentValues values, String where, String[] selectionArgs) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        IContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int i;
        try {
            if (HwSystemManager.allowOp(uri, (int) SYNC_OBSERVER_TYPE_PENDING)) {
                long startTime = SystemClock.uptimeMillis();
                int rowsUpdated = provider.update(this.mPackageName, uri, values, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, uri, "update", where);
                releaseProvider(provider);
                return rowsUpdated;
            }
            i = 0;
            return i;
        } catch (RemoteException e) {
            i = -1;
            return i;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Bundle call(Uri uri, String method, String arg, Bundle extras) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        Preconditions.checkNotNull(method, RemindersColumns.METHOD);
        IContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle res = provider.call(this.mPackageName, method, arg, extras);
            Bundle.setDefusable(res, true);
            return res;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final IContentProvider acquireProvider(Uri uri) {
        if (!SCHEME_CONTENT.equals(uri.getScheme())) {
            return null;
        }
        String auth = uri.getAuthority();
        if (auth != null) {
            return acquireProvider(this.mContext, auth);
        }
        return null;
    }

    public final IContentProvider acquireExistingProvider(Uri uri) {
        if (!SCHEME_CONTENT.equals(uri.getScheme())) {
            return null;
        }
        String auth = uri.getAuthority();
        if (auth != null) {
            return acquireExistingProvider(this.mContext, auth);
        }
        return null;
    }

    public final IContentProvider acquireProvider(String name) {
        if (name == null) {
            return null;
        }
        return acquireProvider(this.mContext, name);
    }

    public final IContentProvider acquireUnstableProvider(Uri uri) {
        if (SCHEME_CONTENT.equals(uri.getScheme()) && uri.getAuthority() != null) {
            return acquireUnstableProvider(this.mContext, uri.getAuthority());
        }
        return null;
    }

    public final IContentProvider acquireUnstableProvider(String name) {
        if (name == null) {
            return null;
        }
        return acquireUnstableProvider(this.mContext, name);
    }

    public final ContentProviderClient acquireContentProviderClient(Uri uri) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        IContentProvider provider = acquireProvider(uri);
        if (provider != null) {
            return new ContentProviderClient(this, provider, true);
        }
        return null;
    }

    public final ContentProviderClient acquireContentProviderClient(String name) {
        Preconditions.checkNotNull(name, KeyChain.EXTRA_NAME);
        IContentProvider provider = acquireProvider(name);
        if (provider != null) {
            return new ContentProviderClient(this, provider, true);
        }
        return null;
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(Uri uri) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        IContentProvider provider = acquireUnstableProvider(uri);
        if (provider != null) {
            return new ContentProviderClient(this, provider, ENABLE_CONTENT_SAMPLE);
        }
        return null;
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(String name) {
        Preconditions.checkNotNull(name, KeyChain.EXTRA_NAME);
        IContentProvider provider = acquireUnstableProvider(name);
        if (provider != null) {
            return new ContentProviderClient(this, provider, ENABLE_CONTENT_SAMPLE);
        }
        return null;
    }

    public final void registerContentObserver(Uri uri, boolean notifyForDescendants, ContentObserver observer) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        Preconditions.checkNotNull(observer, "observer");
        registerContentObserver(ContentProvider.getUriWithoutUserId(uri), notifyForDescendants, observer, ContentProvider.getUserIdFromUri(uri, UserHandle.myUserId()));
    }

    public final void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
        try {
            getContentService().registerContentObserver(uri, notifyForDescendents, observer.getContentObserver(), userHandle);
        } catch (RemoteException e) {
        }
    }

    public final void unregisterContentObserver(ContentObserver observer) {
        Preconditions.checkNotNull(observer, "observer");
        try {
            IContentObserver contentObserver = observer.releaseContentObserver();
            if (contentObserver != null) {
                getContentService().unregisterContentObserver(contentObserver);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyChange(Uri uri, ContentObserver observer) {
        notifyChange(uri, observer, true);
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        notifyChange(ContentProvider.getUriWithoutUserId(uri), observer, syncToNetwork, ContentProvider.getUserIdFromUri(uri, UserHandle.myUserId()));
    }

    public void notifyChange(Uri uri, ContentObserver observer, int flags) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        notifyChange(ContentProvider.getUriWithoutUserId(uri), observer, flags, ContentProvider.getUserIdFromUri(uri, UserHandle.myUserId()));
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork, int userHandle) {
        int i = 0;
        IContentObserver iContentObserver = null;
        try {
            boolean deliverSelfNotifications;
            IContentService contentService = getContentService();
            if (observer != null) {
                iContentObserver = observer.getContentObserver();
            }
            if (observer != null) {
                deliverSelfNotifications = observer.deliverSelfNotifications();
            } else {
                deliverSelfNotifications = ENABLE_CONTENT_SAMPLE;
            }
            if (syncToNetwork) {
                i = SYNC_OBSERVER_TYPE_SETTINGS;
            }
            contentService.notifyChange(uri, iContentObserver, deliverSelfNotifications, i, userHandle);
        } catch (RemoteException e) {
        }
    }

    public void notifyChange(Uri uri, ContentObserver observer, int flags, int userHandle) {
        IContentObserver iContentObserver = null;
        try {
            IContentService contentService = getContentService();
            if (observer != null) {
                iContentObserver = observer.getContentObserver();
            }
            contentService.notifyChange(uri, iContentObserver, observer != null ? observer.deliverSelfNotifications() : ENABLE_CONTENT_SAMPLE, flags, userHandle);
        } catch (RemoteException e) {
        }
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        try {
            ActivityManagerNative.getDefault().takePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
        }
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, KeyChain.EXTRA_URI);
        try {
            ActivityManagerNative.getDefault().releasePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
        }
    }

    public List<UriPermission> getPersistedUriPermissions() {
        try {
            return ActivityManagerNative.getDefault().getPersistedUriPermissions(this.mPackageName, true).getList();
        } catch (RemoteException e) {
            throw new RuntimeException("Activity manager has died", e);
        }
    }

    public List<UriPermission> getOutgoingPersistedUriPermissions() {
        try {
            return ActivityManagerNative.getDefault().getPersistedUriPermissions(this.mPackageName, ENABLE_CONTENT_SAMPLE).getList();
        } catch (RemoteException e) {
            throw new RuntimeException("Activity manager has died", e);
        }
    }

    @Deprecated
    public void startSync(Uri uri, Bundle extras) {
        String str = null;
        Account account = null;
        if (extras != null) {
            String accountName = extras.getString(SYNC_EXTRAS_ACCOUNT);
            if (!TextUtils.isEmpty(accountName)) {
                account = new Account(accountName, "com.google");
            }
            extras.remove(SYNC_EXTRAS_ACCOUNT);
        }
        if (uri != null) {
            str = uri.getAuthority();
        }
        requestSync(account, str, extras);
    }

    public static void requestSync(Account account, String authority, Bundle extras) {
        requestSyncAsUser(account, authority, UserHandle.myUserId(), extras);
    }

    public static void requestSyncAsUser(Account account, String authority, int userId, Bundle extras) {
        if (extras == null) {
            throw new IllegalArgumentException("Must specify extras.");
        }
        try {
            getContentService().syncAsUser(new Builder().setSyncAdapter(account, authority).setExtras(extras).syncOnce().build(), userId);
        } catch (RemoteException e) {
        }
    }

    public static void requestSync(SyncRequest request) {
        try {
            getContentService().sync(request);
        } catch (RemoteException e) {
        }
    }

    public static void validateSyncExtrasBundle(Bundle extras) {
        try {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value != null && !(value instanceof Long) && !(value instanceof Integer) && !(value instanceof Boolean) && !(value instanceof Float) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Account)) {
                    throw new IllegalArgumentException("unexpected value type: " + value.getClass().getName());
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException exc) {
            throw new IllegalArgumentException("error unparceling Bundle", exc);
        }
    }

    @Deprecated
    public void cancelSync(Uri uri) {
        String authority;
        if (uri != null) {
            authority = uri.getAuthority();
        } else {
            authority = null;
        }
        cancelSync(null, authority);
    }

    public static void cancelSync(Account account, String authority) {
        try {
            getContentService().cancelSync(account, authority, null);
        } catch (RemoteException e) {
        }
    }

    public static void cancelSyncAsUser(Account account, String authority, int userId) {
        try {
            getContentService().cancelSyncAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypes() {
        try {
            return getContentService().getSyncAdapterTypes();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) {
        try {
            return getContentService().getSyncAdapterTypesAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        try {
            return getContentService().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
        } catch (RemoteException e) {
            return (String[]) ArrayUtils.emptyArray(String.class);
        }
    }

    public static boolean getSyncAutomatically(Account account, String authority) {
        try {
            return getContentService().getSyncAutomatically(account, authority);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean getSyncAutomaticallyAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncAutomaticallyAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setSyncAutomatically(Account account, String authority, boolean sync) {
        setSyncAutomaticallyAsUser(account, authority, sync, UserHandle.myUserId());
    }

    public static void setSyncAutomaticallyAsUser(Account account, String authority, boolean sync, int userId) {
        try {
            getContentService().setSyncAutomaticallyAsUser(account, authority, sync, userId);
        } catch (RemoteException e) {
        }
    }

    public static void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        validateSyncExtrasBundle(extras);
        if (extras.getBoolean(SYNC_EXTRAS_MANUAL, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_DO_NOT_RETRY, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_IGNORE_BACKOFF, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_IGNORE_SETTINGS, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_INITIALIZE, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_MANUAL, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_EXPEDITED, ENABLE_CONTENT_SAMPLE)) {
            throw new IllegalArgumentException("illegal extras were set");
        }
        try {
            getContentService().addPeriodicSync(account, authority, extras, pollFrequency);
        } catch (RemoteException e) {
        }
    }

    public static boolean invalidPeriodicExtras(Bundle extras) {
        if (extras.getBoolean(SYNC_EXTRAS_MANUAL, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_DO_NOT_RETRY, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_IGNORE_BACKOFF, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_IGNORE_SETTINGS, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_INITIALIZE, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_MANUAL, ENABLE_CONTENT_SAMPLE) || extras.getBoolean(SYNC_EXTRAS_EXPEDITED, ENABLE_CONTENT_SAMPLE)) {
            return true;
        }
        return ENABLE_CONTENT_SAMPLE;
    }

    public static void removePeriodicSync(Account account, String authority, Bundle extras) {
        validateSyncExtrasBundle(extras);
        try {
            getContentService().removePeriodicSync(account, authority, extras);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void cancelSync(SyncRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        try {
            getContentService().cancelRequest(request);
        } catch (RemoteException e) {
        }
    }

    public static List<PeriodicSync> getPeriodicSyncs(Account account, String authority) {
        try {
            return getContentService().getPeriodicSyncs(account, authority, null);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static int getIsSyncable(Account account, String authority) {
        try {
            return getContentService().getIsSyncable(account, authority);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static int getIsSyncableAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getIsSyncableAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setIsSyncable(Account account, String authority, int syncable) {
        try {
            getContentService().setIsSyncable(account, authority, syncable);
        } catch (RemoteException e) {
        }
    }

    public static boolean getMasterSyncAutomatically() {
        try {
            return getContentService().getMasterSyncAutomatically();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean getMasterSyncAutomaticallyAsUser(int userId) {
        try {
            return getContentService().getMasterSyncAutomaticallyAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setMasterSyncAutomatically(boolean sync) {
        setMasterSyncAutomaticallyAsUser(sync, UserHandle.myUserId());
    }

    public static void setMasterSyncAutomaticallyAsUser(boolean sync, int userId) {
        try {
            getContentService().setMasterSyncAutomaticallyAsUser(sync, userId);
        } catch (RemoteException e) {
        }
    }

    public static boolean isSyncActive(Account account, String authority) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        } else if (authority == null) {
            throw new IllegalArgumentException("authority must not be null");
        } else {
            try {
                return getContentService().isSyncActive(account, authority, null);
            } catch (RemoteException e) {
                throw new RuntimeException("the ContentService should always be reachable", e);
            }
        }
    }

    @Deprecated
    public static SyncInfo getCurrentSync() {
        try {
            List<SyncInfo> syncs = getContentService().getCurrentSyncs();
            if (syncs.isEmpty()) {
                return null;
            }
            return (SyncInfo) syncs.get(0);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static List<SyncInfo> getCurrentSyncs() {
        try {
            return getContentService().getCurrentSyncs();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static List<SyncInfo> getCurrentSyncsAsUser(int userId) {
        try {
            return getContentService().getCurrentSyncsAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncStatusInfo getSyncStatus(Account account, String authority) {
        try {
            return getContentService().getSyncStatus(account, authority, null);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncStatusInfo getSyncStatusAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncStatusAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean isSyncPending(Account account, String authority) {
        return isSyncPendingAsUser(account, authority, UserHandle.myUserId());
    }

    public static boolean isSyncPendingAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().isSyncPendingAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static Object addStatusChangeListener(int mask, SyncStatusObserver callback) {
        if (callback == null) {
            throw new IllegalArgumentException("you passed in a null callback");
        }
        try {
            Stub observer = new AnonymousClass1(callback);
            getContentService().addStatusChangeListener(mask, observer);
            return observer;
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void removeStatusChangeListener(Object handle) {
        if (handle == null) {
            throw new IllegalArgumentException("you passed in a null handle");
        }
        try {
            getContentService().removeStatusChangeListener((Stub) handle);
        } catch (RemoteException e) {
        }
    }

    public void putCache(Uri key, Bundle value) {
        try {
            getContentService().putCache(this.mContext.getPackageName(), key, value, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Bundle getCache(Uri key) {
        try {
            Bundle bundle = getContentService().getCache(this.mContext.getPackageName(), key, this.mContext.getUserId());
            if (bundle != null) {
                bundle.setClassLoader(this.mContext.getClassLoader());
            }
            return bundle;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private int samplePercentForDuration(long durationMillis) {
        if (durationMillis >= 500) {
            return 100;
        }
        return ((int) ((100 * durationMillis) / 500)) + SYNC_OBSERVER_TYPE_SETTINGS;
    }

    private void maybeLogQueryToEventLog(long durationMillis, Uri uri, String[] projection, String selection, String sortOrder) {
    }

    private void maybeLogUpdateToEventLog(long durationMillis, Uri uri, String operation, String selection) {
    }

    public static IContentService getContentService() {
        if (sContentService != null) {
            return sContentService;
        }
        sContentService = IContentService.Stub.asInterface(ServiceManager.getService(SCHEME_CONTENT));
        return sContentService;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int resolveUserId(Uri uri) {
        return ContentProvider.getUserIdFromUri(uri, this.mContext.getUserId());
    }
}
