package android.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.provider.FontsContract;
import android.util.Log;
import android.util.LruCache;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FontsContract {
    private static final long SYNC_FONT_FETCH_TIMEOUT_MS = 500;
    private static final String TAG = "FontsContract";
    private static final int THREAD_RENEWAL_THRESHOLD_MS = 10000;
    private static final Comparator<byte[]> sByteArrayComparator = $$Lambda$FontsContract$3FDNQdWsglsyDhifaHVbzkfrA.INSTANCE;
    private static volatile Context sContext;
    /* access modifiers changed from: private */
    @GuardedBy("sLock")
    public static Handler sHandler;
    @GuardedBy("sLock")
    private static Set<String> sInQueueSet;
    /* access modifiers changed from: private */
    public static final Object sLock = new Object();
    private static final Runnable sReplaceDispatcherThreadRunnable = new Runnable() {
        public void run() {
            synchronized (FontsContract.sLock) {
                if (FontsContract.sThread != null) {
                    FontsContract.sThread.quitSafely();
                    HandlerThread unused = FontsContract.sThread = null;
                    Handler unused2 = FontsContract.sHandler = null;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy("sLock")
    public static HandlerThread sThread;
    private static final LruCache<String, Typeface> sTypefaceCache = new LruCache<>(16);

    public static final class Columns implements BaseColumns {
        public static final String FILE_ID = "file_id";
        public static final String ITALIC = "font_italic";
        public static final String RESULT_CODE = "result_code";
        public static final int RESULT_CODE_FONT_NOT_FOUND = 1;
        public static final int RESULT_CODE_FONT_UNAVAILABLE = 2;
        public static final int RESULT_CODE_MALFORMED_QUERY = 3;
        public static final int RESULT_CODE_OK = 0;
        public static final String TTC_INDEX = "font_ttc_index";
        public static final String VARIATION_SETTINGS = "font_variation_settings";
        public static final String WEIGHT = "font_weight";

        private Columns() {
        }
    }

    public static class FontFamilyResult {
        public static final int STATUS_OK = 0;
        public static final int STATUS_REJECTED = 3;
        public static final int STATUS_UNEXPECTED_DATA_PROVIDED = 2;
        public static final int STATUS_WRONG_CERTIFICATES = 1;
        private final FontInfo[] mFonts;
        private final int mStatusCode;

        @Retention(RetentionPolicy.SOURCE)
        @interface FontResultStatus {
        }

        public FontFamilyResult(int statusCode, FontInfo[] fonts) {
            this.mStatusCode = statusCode;
            this.mFonts = fonts;
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public FontInfo[] getFonts() {
            return this.mFonts;
        }
    }

    public static class FontInfo {
        private final FontVariationAxis[] mAxes;
        private final boolean mItalic;
        private final int mResultCode;
        private final int mTtcIndex;
        private final Uri mUri;
        private final int mWeight;

        public FontInfo(Uri uri, int ttcIndex, FontVariationAxis[] axes, int weight, boolean italic, int resultCode) {
            this.mUri = (Uri) Preconditions.checkNotNull(uri);
            this.mTtcIndex = ttcIndex;
            this.mAxes = axes;
            this.mWeight = weight;
            this.mItalic = italic;
            this.mResultCode = resultCode;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public int getTtcIndex() {
            return this.mTtcIndex;
        }

        public FontVariationAxis[] getAxes() {
            return this.mAxes;
        }

        public int getWeight() {
            return this.mWeight;
        }

        public boolean isItalic() {
            return this.mItalic;
        }

        public int getResultCode() {
            return this.mResultCode;
        }
    }

    public static class FontRequestCallback {
        public static final int FAIL_REASON_FONT_LOAD_ERROR = -3;
        public static final int FAIL_REASON_FONT_NOT_FOUND = 1;
        public static final int FAIL_REASON_FONT_UNAVAILABLE = 2;
        public static final int FAIL_REASON_MALFORMED_QUERY = 3;
        public static final int FAIL_REASON_PROVIDER_NOT_FOUND = -1;
        public static final int FAIL_REASON_WRONG_CERTIFICATES = -2;

        @Retention(RetentionPolicy.SOURCE)
        @interface FontRequestFailReason {
        }

        public void onTypefaceRetrieved(Typeface typeface) {
        }

        public void onTypefaceRequestFailed(int reason) {
        }
    }

    private FontsContract() {
    }

    public static void setApplicationContextForResources(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Typeface getFontSync(FontRequest request) {
        String id = request.getIdentifier();
        Typeface cachedTypeface = sTypefaceCache.get(id);
        if (cachedTypeface != null) {
            return cachedTypeface;
        }
        synchronized (sLock) {
            try {
                if (sHandler == null) {
                    try {
                        sThread = new HandlerThread("fonts", 10);
                        sThread.start();
                        sHandler = new Handler(sThread.getLooper());
                    } catch (Throwable th) {
                        th = th;
                        String str = id;
                        Typeface typeface = cachedTypeface;
                    }
                }
                Lock lock = new ReentrantLock();
                Condition cond = lock.newCondition();
                AtomicReference<Typeface> holder = new AtomicReference<>();
                AtomicBoolean waiting = new AtomicBoolean(true);
                AtomicBoolean timeout = new AtomicBoolean(false);
                Handler handler = sHandler;
                r1 = r1;
                String str2 = id;
                $$Lambda$FontsContract$rqfIZKvP1frnI9vP1hVA8jQN_RE r15 = r1;
                String str3 = id;
                AtomicBoolean timeout2 = timeout;
                Typeface typeface2 = cachedTypeface;
                AtomicBoolean waiting2 = waiting;
                try {
                    $$Lambda$FontsContract$rqfIZKvP1frnI9vP1hVA8jQN_RE r1 = new Runnable(str2, holder, lock, timeout, waiting, cond) {
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ AtomicReference f$2;
                        private final /* synthetic */ Lock f$3;
                        private final /* synthetic */ AtomicBoolean f$4;
                        private final /* synthetic */ AtomicBoolean f$5;
                        private final /* synthetic */ Condition f$6;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                            this.f$6 = r7;
                        }

                        public final void run() {
                            FontsContract.lambda$getFontSync$0(FontRequest.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                        }
                    };
                    handler.post(r15);
                    sHandler.removeCallbacks(sReplaceDispatcherThreadRunnable);
                    sHandler.postDelayed(sReplaceDispatcherThreadRunnable, 10000);
                    long remaining = TimeUnit.MILLISECONDS.toNanos(500);
                    lock.lock();
                    if (!waiting2.get()) {
                        Typeface typeface3 = holder.get();
                        lock.unlock();
                        return typeface3;
                    }
                    do {
                        try {
                            remaining = cond.awaitNanos(remaining);
                        } catch (InterruptedException e) {
                        }
                        if (!waiting2.get()) {
                            Typeface typeface4 = holder.get();
                            lock.unlock();
                            return typeface4;
                        }
                    } while (remaining > 0);
                    timeout2.set(true);
                    Log.w(TAG, "Remote font fetch timed out: " + request.getProviderAuthority() + "/" + request.getQuery());
                    lock.unlock();
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                String str4 = id;
                Typeface typeface5 = cachedTypeface;
                throw th;
            }
        }
    }

    static /* synthetic */ void lambda$getFontSync$0(FontRequest request, String id, AtomicReference holder, Lock lock, AtomicBoolean timeout, AtomicBoolean waiting, Condition cond) {
        try {
            FontFamilyResult result = fetchFonts(sContext, null, request);
            if (result.getStatusCode() == 0) {
                Typeface typeface = buildTypeface(sContext, null, result.getFonts());
                if (typeface != null) {
                    sTypefaceCache.put(id, typeface);
                }
                holder.set(typeface);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "find NameNotFoundException.");
        }
        lock.lock();
        try {
            if (!timeout.get()) {
                waiting.set(false);
                cond.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void requestFonts(Context context, FontRequest request, Handler handler, CancellationSignal cancellationSignal, FontRequestCallback callback) {
        Handler callerThreadHandler = new Handler();
        Typeface cachedTypeface = sTypefaceCache.get(request.getIdentifier());
        if (cachedTypeface != null) {
            callerThreadHandler.post(new Runnable(cachedTypeface) {
                private final /* synthetic */ Typeface f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FontsContract.FontRequestCallback.this.onTypefaceRetrieved(this.f$1);
                }
            });
            return;
        }
        $$Lambda$FontsContract$dFs2m4XF5xdir4W3TncUQAVX8k r1 = new Runnable(context, cancellationSignal, request, callerThreadHandler, callback) {
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ CancellationSignal f$1;
            private final /* synthetic */ FontRequest f$2;
            private final /* synthetic */ Handler f$3;
            private final /* synthetic */ FontsContract.FontRequestCallback f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                FontsContract.lambda$requestFonts$12(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        };
        handler.post(r1);
    }

    static /* synthetic */ void lambda$requestFonts$12(Context context, CancellationSignal cancellationSignal, FontRequest request, Handler callerThreadHandler, FontRequestCallback callback) {
        try {
            FontFamilyResult result = fetchFonts(context, cancellationSignal, request);
            Typeface anotherCachedTypeface = sTypefaceCache.get(request.getIdentifier());
            if (anotherCachedTypeface != null) {
                callerThreadHandler.post(new Runnable(anotherCachedTypeface) {
                    private final /* synthetic */ Typeface f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        FontsContract.FontRequestCallback.this.onTypefaceRetrieved(this.f$1);
                    }
                });
            } else if (result.getStatusCode() != 0) {
                switch (result.getStatusCode()) {
                    case 1:
                        callerThreadHandler.post(new Runnable() {
                            public final void run() {
                                FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-2);
                            }
                        });
                        return;
                    case 2:
                        callerThreadHandler.post(new Runnable() {
                            public final void run() {
                                FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                            }
                        });
                        return;
                    default:
                        callerThreadHandler.post(new Runnable() {
                            public final void run() {
                                FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                            }
                        });
                        return;
                }
            } else {
                FontInfo[] fonts = result.getFonts();
                if (fonts == null || fonts.length == 0) {
                    callerThreadHandler.post(new Runnable() {
                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(1);
                        }
                    });
                    return;
                }
                for (FontInfo font : fonts) {
                    if (font.getResultCode() != 0) {
                        int resultCode = font.getResultCode();
                        if (resultCode < 0) {
                            callerThreadHandler.post(new Runnable() {
                                public final void run() {
                                    FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                                }
                            });
                        } else {
                            callerThreadHandler.post(new Runnable(resultCode) {
                                private final /* synthetic */ int f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(this.f$1);
                                }
                            });
                        }
                        return;
                    }
                }
                Typeface typeface = buildTypeface(context, cancellationSignal, fonts);
                if (typeface == null) {
                    callerThreadHandler.post(new Runnable() {
                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                        }
                    });
                    return;
                }
                sTypefaceCache.put(request.getIdentifier(), typeface);
                callerThreadHandler.post(new Runnable(typeface) {
                    private final /* synthetic */ Typeface f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        FontsContract.FontRequestCallback.this.onTypefaceRetrieved(this.f$1);
                    }
                });
            }
        } catch (PackageManager.NameNotFoundException e) {
            callerThreadHandler.post(new Runnable() {
                public final void run() {
                    FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-1);
                }
            });
        }
    }

    public static FontFamilyResult fetchFonts(Context context, CancellationSignal cancellationSignal, FontRequest request) throws PackageManager.NameNotFoundException {
        if (context.isRestricted()) {
            return new FontFamilyResult(3, null);
        }
        ProviderInfo providerInfo = getProvider(context.getPackageManager(), request);
        if (providerInfo == null) {
            return new FontFamilyResult(1, null);
        }
        try {
            return new FontFamilyResult(0, getFontFromProvider(context, request, providerInfo.authority, cancellationSignal));
        } catch (IllegalArgumentException e) {
            return new FontFamilyResult(2, null);
        }
    }

    public static Typeface buildTypeface(Context context, CancellationSignal cancellationSignal, FontInfo[] fonts) {
        if (context.isRestricted()) {
            return null;
        }
        Map<Uri, ByteBuffer> uriBuffer = prepareFontData(context, fonts, cancellationSignal);
        if (uriBuffer.isEmpty()) {
            return null;
        }
        return new Typeface.Builder(fonts, uriBuffer).build();
    }

    private static Map<Uri, ByteBuffer> prepareFontData(Context context, FontInfo[] fonts, CancellationSignal cancellationSignal) {
        ParcelFileDescriptor pfd;
        Throwable th;
        FileInputStream fis;
        Throwable th2;
        HashMap hashMap = new HashMap();
        ContentResolver resolver = context.getContentResolver();
        for (FontInfo font : fonts) {
            if (font.getResultCode() == 0) {
                Uri uri = font.getUri();
                if (!hashMap.containsKey(uri)) {
                    ByteBuffer buffer = null;
                    try {
                        pfd = resolver.openFileDescriptor(uri, "r", cancellationSignal);
                        if (pfd != null) {
                            fis = new FileInputStream(pfd.getFileDescriptor());
                            try {
                                FileChannel fileChannel = fis.getChannel();
                                buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                                $closeResource(null, fis);
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                    } catch (IOException e) {
                    } catch (IOException e2) {
                        Log.w(TAG, "find IOException.");
                    } catch (Throwable th4) {
                        if (pfd != null) {
                            $closeResource(th, pfd);
                        }
                        throw th4;
                    }
                    if (pfd != null) {
                        $closeResource(null, pfd);
                    }
                    hashMap.put(uri, buffer);
                }
            }
            CancellationSignal cancellationSignal2 = cancellationSignal;
        }
        CancellationSignal cancellationSignal3 = cancellationSignal;
        return Collections.unmodifiableMap(hashMap);
        $closeResource(th2, fis);
        throw th;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @VisibleForTesting
    public static ProviderInfo getProvider(PackageManager packageManager, FontRequest request) throws PackageManager.NameNotFoundException {
        String providerAuthority = request.getProviderAuthority();
        ProviderInfo info = packageManager.resolveContentProvider(providerAuthority, 0);
        if (info == null) {
            throw new PackageManager.NameNotFoundException("No package found for authority: " + providerAuthority);
        } else if (!info.packageName.equals(request.getProviderPackage())) {
            throw new PackageManager.NameNotFoundException("Found content provider " + providerAuthority + ", but package was not " + request.getProviderPackage());
        } else if (info.applicationInfo.isSystemApp()) {
            return info;
        } else {
            List<byte[]> signatures = convertToByteArrayList(packageManager.getPackageInfo(info.packageName, 64).signatures);
            Collections.sort(signatures, sByteArrayComparator);
            List<List<byte[]>> requestCertificatesList = request.getCertificates();
            for (int i = 0; i < requestCertificatesList.size(); i++) {
                List<byte[]> requestSignatures = new ArrayList<>(requestCertificatesList.get(i));
                Collections.sort(requestSignatures, sByteArrayComparator);
                if (equalsByteArrayList(signatures, requestSignatures)) {
                    return info;
                }
            }
            return null;
        }
    }

    static /* synthetic */ int lambda$static$13(byte[] l, byte[] r) {
        if (l.length != r.length) {
            return l.length - r.length;
        }
        for (int i = 0; i < l.length; i++) {
            if (l[i] != r[i]) {
                return l[i] - r[i];
            }
        }
        return 0;
    }

    private static boolean equalsByteArrayList(List<byte[]> signatures, List<byte[]> requestSignatures) {
        if (signatures.size() != requestSignatures.size()) {
            return false;
        }
        for (int i = 0; i < signatures.size(); i++) {
            if (!Arrays.equals(signatures.get(i), requestSignatures.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<byte[]> convertToByteArrayList(Signature[] signatures) {
        List<byte[]> shas = new ArrayList<>();
        for (Signature byteArray : signatures) {
            shas.add(byteArray.toByteArray());
        }
        return shas;
    }

    @VisibleForTesting
    public static FontInfo[] getFontFromProvider(Context context, FontRequest request, String authority, CancellationSignal cancellationSignal) {
        Throwable th;
        int resultCodeColumnIndex;
        Uri fileUri;
        boolean italic;
        int weight;
        String str = authority;
        ArrayList<FontInfo> result = new ArrayList<>();
        Uri uri = new Uri.Builder().scheme("content").authority(str).build();
        Uri fileBaseUri = new Uri.Builder().scheme("content").authority(str).appendPath("file").build();
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id", Columns.FILE_ID, Columns.TTC_INDEX, Columns.VARIATION_SETTINGS, Columns.WEIGHT, Columns.ITALIC, Columns.RESULT_CODE}, "query = ?", new String[]{request.getQuery()}, null, cancellationSignal);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    int resultCodeColumnIndex2 = cursor.getColumnIndex(Columns.RESULT_CODE);
                    result = new ArrayList<>();
                    int idColumnIndex = cursor.getColumnIndexOrThrow("_id");
                    int fileIdColumnIndex = cursor.getColumnIndex(Columns.FILE_ID);
                    int ttcIndexColumnIndex = cursor.getColumnIndex(Columns.TTC_INDEX);
                    int vsColumnIndex = cursor.getColumnIndex(Columns.VARIATION_SETTINGS);
                    int weightColumnIndex = cursor.getColumnIndex(Columns.WEIGHT);
                    int italicColumnIndex = cursor.getColumnIndex(Columns.ITALIC);
                    while (cursor.moveToNext()) {
                        int resultCode = resultCodeColumnIndex2 != -1 ? cursor.getInt(resultCodeColumnIndex2) : 0;
                        int ttcIndex = ttcIndexColumnIndex != -1 ? cursor.getInt(ttcIndexColumnIndex) : 0;
                        String variationSettings = vsColumnIndex != -1 ? cursor.getString(vsColumnIndex) : null;
                        if (fileIdColumnIndex == -1) {
                            resultCodeColumnIndex = resultCodeColumnIndex2;
                            fileUri = ContentUris.withAppendedId(uri, cursor.getLong(idColumnIndex));
                        } else {
                            resultCodeColumnIndex = resultCodeColumnIndex2;
                            fileUri = ContentUris.withAppendedId(fileBaseUri, cursor.getLong(fileIdColumnIndex));
                        }
                        if (weightColumnIndex == -1 || italicColumnIndex == -1) {
                            weight = 400;
                            italic = false;
                        } else {
                            weight = cursor.getInt(weightColumnIndex);
                            italic = cursor.getInt(italicColumnIndex) == 1;
                        }
                        FontInfo fontInfo = new FontInfo(fileUri, ttcIndex, FontVariationAxis.fromFontVariationSettings(variationSettings), weight, italic, resultCode);
                        result.add(fontInfo);
                        resultCodeColumnIndex2 = resultCodeColumnIndex;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        return (FontInfo[]) result.toArray(new FontInfo[0]);
        if (cursor != null) {
            $closeResource(th, cursor);
        }
        throw th;
    }
}
