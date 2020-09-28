package android.provider;

import android.app.job.JobInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.FontStyle;
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
    @GuardedBy({"sLock"})
    private static Handler sHandler;
    @GuardedBy({"sLock"})
    private static Set<String> sInQueueSet;
    private static final Object sLock = new Object();
    private static final Runnable sReplaceDispatcherThreadRunnable = new Runnable() {
        /* class android.provider.FontsContract.AnonymousClass1 */

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
    @GuardedBy({"sLock"})
    private static HandlerThread sThread;
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

    private FontsContract() {
    }

    public static void setApplicationContextForResources(Context context) {
        sContext = context.getApplicationContext();
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

    public static Typeface getFontSync(FontRequest request) {
        String id = request.getIdentifier();
        Typeface cachedTypeface = sTypefaceCache.get(id);
        if (cachedTypeface != null) {
            return cachedTypeface;
        }
        synchronized (sLock) {
            if (sHandler == null) {
                sThread = new HandlerThread("fonts", 10);
                sThread.start();
                sHandler = new Handler(sThread.getLooper());
            }
            Lock lock = new ReentrantLock();
            Condition cond = lock.newCondition();
            AtomicReference<Typeface> holder = new AtomicReference<>();
            AtomicBoolean waiting = new AtomicBoolean(true);
            AtomicBoolean timeout = new AtomicBoolean(false);
            sHandler.post(new Runnable(id, holder, lock, timeout, waiting, cond) {
                /* class android.provider.$$Lambda$FontsContract$rqfIZKvP1frnI9vP1hVA8jQN_RE */
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
            });
            sHandler.removeCallbacks(sReplaceDispatcherThreadRunnable);
            sHandler.postDelayed(sReplaceDispatcherThreadRunnable, JobInfo.MIN_BACKOFF_MILLIS);
            long remaining = TimeUnit.MILLISECONDS.toNanos(SYNC_FONT_FETCH_TIMEOUT_MS);
            lock.lock();
            try {
                if (!waiting.get()) {
                    return holder.get();
                }
                do {
                    try {
                        remaining = cond.awaitNanos(remaining);
                    } catch (InterruptedException e) {
                    }
                    if (!waiting.get()) {
                        Typeface typeface = holder.get();
                        lock.unlock();
                        return typeface;
                    }
                } while (remaining > 0);
                timeout.set(true);
                Log.w(TAG, "Remote font fetch timed out: " + request.getProviderAuthority() + "/" + request.getQuery());
                lock.unlock();
                return null;
            } finally {
                lock.unlock();
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

    public static void requestFonts(Context context, FontRequest request, Handler handler, CancellationSignal cancellationSignal, FontRequestCallback callback) {
        Handler callerThreadHandler = new Handler();
        Typeface cachedTypeface = sTypefaceCache.get(request.getIdentifier());
        if (cachedTypeface != null) {
            callerThreadHandler.post(new Runnable(cachedTypeface) {
                /* class android.provider.$$Lambda$FontsContract$p_tsXYYYpEH0EJSp2uPrJ33dkU */
                private final /* synthetic */ Typeface f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FontsContract.FontRequestCallback.this.onTypefaceRetrieved(this.f$1);
                }
            });
        } else {
            handler.post(new Runnable(cancellationSignal, request, callerThreadHandler, callback) {
                /* class android.provider.$$Lambda$FontsContract$dFs2m4XF5xdir4W3TncUQAVX8k */
                private final /* synthetic */ CancellationSignal f$1;
                private final /* synthetic */ FontRequest f$2;
                private final /* synthetic */ Handler f$3;
                private final /* synthetic */ FontsContract.FontRequestCallback f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    FontsContract.lambda$requestFonts$12(Context.this, this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }
    }

    static /* synthetic */ void lambda$requestFonts$12(Context context, CancellationSignal cancellationSignal, FontRequest request, Handler callerThreadHandler, FontRequestCallback callback) {
        try {
            FontFamilyResult result = fetchFonts(context, cancellationSignal, request);
            Typeface anotherCachedTypeface = sTypefaceCache.get(request.getIdentifier());
            if (anotherCachedTypeface != null) {
                callerThreadHandler.post(new Runnable(anotherCachedTypeface) {
                    /* class android.provider.$$Lambda$FontsContract$xDMhIK5JxjXFDIXBeQbZ_hdXTBc */
                    private final /* synthetic */ Typeface f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        FontsContract.FontRequestCallback.this.onTypefaceRetrieved(this.f$1);
                    }
                });
            } else if (result.getStatusCode() != 0) {
                int statusCode = result.getStatusCode();
                if (statusCode == 1) {
                    callerThreadHandler.post(new Runnable() {
                        /* class android.provider.$$Lambda$FontsContract$YhiTIVckhFBdgNR2V1bGY3Q1Nqg */

                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-2);
                        }
                    });
                } else if (statusCode != 2) {
                    callerThreadHandler.post(new Runnable() {
                        /* class android.provider.$$Lambda$FontsContract$DV4gvjPxJzdQvcfoIJqGrzFtTQs */

                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                        }
                    });
                } else {
                    callerThreadHandler.post(new Runnable() {
                        /* class android.provider.$$Lambda$FontsContract$FCawscMFN_8Qxcb2EdA5gdEO2k */

                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                        }
                    });
                }
            } else {
                FontInfo[] fonts = result.getFonts();
                if (fonts == null || fonts.length == 0) {
                    callerThreadHandler.post(new Runnable() {
                        /* class android.provider.$$Lambda$FontsContract$LJ3jfZobcxq5xTMmb88GlM1r9Jk */

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
                                /* class android.provider.$$Lambda$FontsContract$Qvl9aVA7txTF3tFcFbbKD_nWpuM */

                                public final void run() {
                                    FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                                }
                            });
                            return;
                        } else {
                            callerThreadHandler.post(new Runnable(resultCode) {
                                /* class android.provider.$$Lambda$FontsContract$rvEOORTXb3mMYTLkoH9nlHQr9Iw */
                                private final /* synthetic */ int f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(this.f$1);
                                }
                            });
                            return;
                        }
                    }
                }
                Typeface typeface = buildTypeface(context, cancellationSignal, fonts);
                if (typeface == null) {
                    callerThreadHandler.post(new Runnable() {
                        /* class android.provider.$$Lambda$FontsContract$rqmVfWYeZ5NL5MtBx5LOdhNAOP4 */

                        public final void run() {
                            FontsContract.FontRequestCallback.this.onTypefaceRequestFailed(-3);
                        }
                    });
                    return;
                }
                sTypefaceCache.put(request.getIdentifier(), typeface);
                callerThreadHandler.post(new Runnable(typeface) {
                    /* class android.provider.$$Lambda$FontsContract$gJeQYFM3pOmNcWmWnWDAEk3vlM */
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
                /* class android.provider.$$Lambda$FontsContract$bLFahJqnd9gkPbDqBOCiChzm_E */

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
        FontFamily.Builder familyBuilder = null;
        for (FontInfo fontInfo : fonts) {
            ByteBuffer buffer = uriBuffer.get(fontInfo.getUri());
            if (buffer != null) {
                try {
                    Font font = new Font.Builder(buffer).setWeight(fontInfo.getWeight()).setSlant(fontInfo.isItalic() ? 1 : 0).setTtcIndex(fontInfo.getTtcIndex()).setFontVariationSettings(fontInfo.getAxes()).build();
                    if (familyBuilder == null) {
                        familyBuilder = new FontFamily.Builder(font);
                    } else {
                        familyBuilder.addFont(font);
                    }
                } catch (IllegalArgumentException e) {
                    return null;
                } catch (IOException e2) {
                }
            }
        }
        if (familyBuilder == null) {
            return null;
        }
        FontFamily family = familyBuilder.build();
        FontStyle normal = new FontStyle(400, 0);
        Font bestFont = family.getFont(0);
        int bestScore = normal.getMatchScore(bestFont.getStyle());
        for (int i = 1; i < family.getSize(); i++) {
            Font candidate = family.getFont(i);
            int score = normal.getMatchScore(candidate.getStyle());
            if (score < bestScore) {
                bestFont = candidate;
                bestScore = score;
            }
        }
        return new Typeface.CustomFallbackBuilder(family).setStyle(bestFont.getStyle()).build();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005d, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006b, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006d, code lost:
        r8 = null;
     */
    private static Map<Uri, ByteBuffer> prepareFontData(Context context, FontInfo[] fonts, CancellationSignal cancellationSignal) {
        HashMap<Uri, ByteBuffer> out = new HashMap<>();
        ContentResolver resolver = context.getContentResolver();
        for (FontInfo font : fonts) {
            if (font.getResultCode() == 0) {
                Uri uri = font.getUri();
                if (!out.containsKey(uri)) {
                    ByteBuffer buffer = null;
                    try {
                        ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r", cancellationSignal);
                        if (pfd != null) {
                            FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
                            FileChannel fileChannel = fis.getChannel();
                            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                            $closeResource(null, fis);
                        }
                        if (pfd != null) {
                            $closeResource(null, pfd);
                        }
                    } catch (IOException e) {
                    }
                    out.put(uri, buffer);
                }
            }
        }
        return Collections.unmodifiableMap(out);
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
        for (Signature signature : signatures) {
            shas.add(signature.toByteArray());
        }
        return shas;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0111, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0112, code lost:
        $closeResource(r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0116, code lost:
        throw r0;
     */
    @VisibleForTesting
    public static FontInfo[] getFontFromProvider(Context context, FontRequest request, String authority, CancellationSignal cancellationSignal) {
        int resultCodeColumnIndex;
        Uri fileUri;
        boolean italic;
        int weight;
        ArrayList<FontInfo> result = new ArrayList<>();
        Uri uri = new Uri.Builder().scheme("content").authority(authority).build();
        Uri fileBaseUri = new Uri.Builder().scheme("content").authority(authority).appendPath(ContentResolver.SCHEME_FILE).build();
        boolean z = true;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id", Columns.FILE_ID, Columns.TTC_INDEX, Columns.VARIATION_SETTINGS, Columns.WEIGHT, Columns.ITALIC, Columns.RESULT_CODE}, "query = ?", new String[]{request.getQuery()}, null, cancellationSignal);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int weight2 = cursor.getColumnIndex(Columns.RESULT_CODE);
                result = new ArrayList<>();
                int idColumnIndex = cursor.getColumnIndexOrThrow("_id");
                int fileIdColumnIndex = cursor.getColumnIndex(Columns.FILE_ID);
                int ttcIndexColumnIndex = cursor.getColumnIndex(Columns.TTC_INDEX);
                int vsColumnIndex = cursor.getColumnIndex(Columns.VARIATION_SETTINGS);
                int weightColumnIndex = cursor.getColumnIndex(Columns.WEIGHT);
                int italicColumnIndex = cursor.getColumnIndex(Columns.ITALIC);
                while (cursor.moveToNext()) {
                    int resultCode = weight2 != -1 ? cursor.getInt(weight2) : 0;
                    int ttcIndex = ttcIndexColumnIndex != -1 ? cursor.getInt(ttcIndexColumnIndex) : 0;
                    String variationSettings = vsColumnIndex != -1 ? cursor.getString(vsColumnIndex) : null;
                    if (fileIdColumnIndex == -1) {
                        resultCodeColumnIndex = weight2;
                        fileUri = ContentUris.withAppendedId(uri, cursor.getLong(idColumnIndex));
                    } else {
                        resultCodeColumnIndex = weight2;
                        fileUri = ContentUris.withAppendedId(fileBaseUri, cursor.getLong(fileIdColumnIndex));
                    }
                    if (weightColumnIndex == -1 || italicColumnIndex == -1) {
                        weight = 400;
                        italic = false;
                    } else {
                        weight = cursor.getInt(weightColumnIndex);
                        italic = cursor.getInt(italicColumnIndex) == z ? z : false;
                    }
                    result.add(new FontInfo(fileUri, ttcIndex, FontVariationAxis.fromFontVariationSettings(variationSettings), weight, italic, resultCode));
                    weight2 = resultCodeColumnIndex;
                    z = true;
                }
            }
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        return (FontInfo[]) result.toArray(new FontInfo[0]);
    }
}
