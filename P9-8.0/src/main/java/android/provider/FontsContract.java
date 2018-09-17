package android.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.Typeface.Builder;
import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass10;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass11;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass12;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass13;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass2;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass3;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass4;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass5;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass6;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass7;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass8;
import android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass9;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.util.LruCache;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private static final Comparator<byte[]> sByteArrayComparator = new -$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c();
    private static volatile Context sContext;
    @GuardedBy("sLock")
    private static Handler sHandler;
    @GuardedBy("sLock")
    private static Set<String> sInQueueSet;
    private static final Object sLock = new Object();
    private static final Runnable sReplaceDispatcherThreadRunnable = new Runnable() {
        public void run() {
            synchronized (FontsContract.sLock) {
                if (FontsContract.sThread != null) {
                    FontsContract.sThread.quitSafely();
                    FontsContract.sThread = null;
                    FontsContract.sHandler = null;
                }
            }
        }
    };
    @GuardedBy("sLock")
    private static HandlerThread sThread;
    private static final LruCache<String, Typeface> sTypefaceCache = new LruCache(16);

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

        /* renamed from: onTypefaceRetrieved */
        public void lambda$-android_provider_FontsContract_24540(Typeface typeface) {
        }

        /* renamed from: onTypefaceRequestFailed */
        public void lambda$-android_provider_FontsContract_23796(int reason) {
        }
    }

    private FontsContract() {
    }

    public static void setApplicationContextForResources(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Typeface getFontSync(FontRequest request) {
        String id = request.getIdentifier();
        Typeface cachedTypeface = (Typeface) sTypefaceCache.get(id);
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
            AtomicReference<Typeface> holder = new AtomicReference();
            AtomicBoolean waiting = new AtomicBoolean(true);
            AtomicBoolean timeout = new AtomicBoolean(false);
            sHandler.post(new AnonymousClass12(request, id, holder, lock, timeout, waiting, cond));
            sHandler.removeCallbacks(sReplaceDispatcherThreadRunnable);
            sHandler.postDelayed(sReplaceDispatcherThreadRunnable, 10000);
            long remaining = TimeUnit.MILLISECONDS.toNanos(SYNC_FONT_FETCH_TIMEOUT_MS);
            lock.lock();
            try {
                Typeface typeface;
                if (waiting.get()) {
                    do {
                        try {
                            remaining = cond.awaitNanos(remaining);
                        } catch (InterruptedException e) {
                        }
                        if (!waiting.get()) {
                            typeface = (Typeface) holder.get();
                            lock.unlock();
                            return typeface;
                        }
                    } while (remaining > 0);
                    timeout.set(true);
                    Log.w(TAG, "Remote font fetch timed out: " + request.getProviderAuthority() + "/" + request.getQuery());
                    lock.unlock();
                    return null;
                }
                typeface = (Typeface) holder.get();
                lock.unlock();
                return typeface;
            } catch (Throwable th) {
                lock.unlock();
            }
        }
    }

    static /* synthetic */ void lambda$-android_provider_FontsContract_13824(FontRequest request, String id, AtomicReference holder, Lock lock, AtomicBoolean timeout, AtomicBoolean waiting, Condition cond) {
        try {
            FontFamilyResult result = fetchFonts(sContext, null, request);
            if (result.getStatusCode() == 0) {
                Typeface typeface = buildTypeface(sContext, null, result.getFonts());
                if (typeface != null) {
                    sTypefaceCache.put(id, typeface);
                }
                holder.set(typeface);
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "find NameNotFoundException.");
        }
        lock.lock();
        try {
            if (!timeout.get()) {
                waiting.set(false);
                cond.signal();
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public static void requestFonts(Context context, FontRequest request, Handler handler, CancellationSignal cancellationSignal, FontRequestCallback callback) {
        Handler callerThreadHandler = new Handler();
        Typeface cachedTypeface = (Typeface) sTypefaceCache.get(request.getIdentifier());
        if (cachedTypeface != null) {
            callerThreadHandler.post(new AnonymousClass10(callback, cachedTypeface));
        } else {
            handler.post(new AnonymousClass11(context, cancellationSignal, request, callerThreadHandler, callback));
        }
    }

    static /* synthetic */ void lambda$-android_provider_FontsContract_20996(Context context, CancellationSignal cancellationSignal, FontRequest request, Handler callerThreadHandler, FontRequestCallback callback) {
        try {
            FontFamilyResult result = fetchFonts(context, cancellationSignal, request);
            Typeface anotherCachedTypeface = (Typeface) sTypefaceCache.get(request.getIdentifier());
            if (anotherCachedTypeface != null) {
                callerThreadHandler.post(new AnonymousClass8(callback, anotherCachedTypeface));
            } else if (result.getStatusCode() != 0) {
                switch (result.getStatusCode()) {
                    case 1:
                        callerThreadHandler.post(new AnonymousClass2(callback));
                        return;
                    case 2:
                        callerThreadHandler.post(new AnonymousClass3(callback));
                        return;
                    default:
                        callerThreadHandler.post(new AnonymousClass4(callback));
                        return;
                }
            } else {
                FontInfo[] fonts = result.getFonts();
                if (fonts == null || fonts.length == 0) {
                    callerThreadHandler.post(new AnonymousClass5(callback));
                    return;
                }
                for (FontInfo font : fonts) {
                    if (font.getResultCode() != 0) {
                        int resultCode = font.getResultCode();
                        if (resultCode < 0) {
                            callerThreadHandler.post(new AnonymousClass6(callback));
                        } else {
                            callerThreadHandler.post(new AnonymousClass13(resultCode, callback));
                        }
                        return;
                    }
                }
                Typeface typeface = buildTypeface(context, cancellationSignal, fonts);
                if (typeface == null) {
                    callerThreadHandler.post(new AnonymousClass7(callback));
                    return;
                }
                sTypefaceCache.put(request.getIdentifier(), typeface);
                callerThreadHandler.post(new AnonymousClass9(callback, typeface));
            }
        } catch (NameNotFoundException e) {
            callerThreadHandler.post(new android.provider.-$Lambda$a7Jyr6j_Mb70hHJ2ssL1AAhKh4c.AnonymousClass1(callback));
        }
    }

    public static FontFamilyResult fetchFonts(Context context, CancellationSignal cancellationSignal, FontRequest request) throws NameNotFoundException {
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
        return new Builder(fonts, uriBuffer).build();
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x00af A:{Splitter: B:9:0x0034, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008b A:{SYNTHETIC, Splitter: B:41:0x008b} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00ae A:{SYNTHETIC, Splitter: B:63:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0090 A:{SYNTHETIC, Splitter: B:44:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00af A:{Splitter: B:9:0x0034, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:34:0x007f, code:
            r17 = th;
     */
    /* JADX WARNING: Missing block: B:55:0x00a0, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:56:0x00a1, code:
            r21 = r4;
            r4 = r3;
            r3 = r21;
     */
    /* JADX WARNING: Missing block: B:65:0x00af, code:
            r3 = th;
     */
    /* JADX WARNING: Missing block: B:66:0x00b0, code:
            r4 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<Uri, ByteBuffer> prepareFontData(Context context, FontInfo[] fonts, CancellationSignal cancellationSignal) {
        ParcelFileDescriptor pfd;
        Throwable th;
        Throwable th2;
        HashMap<Uri, ByteBuffer> out = new HashMap();
        ContentResolver resolver = context.getContentResolver();
        int i = 0;
        int length = fonts.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return Collections.unmodifiableMap(out);
            }
            FontInfo font = fonts[i2];
            if (font.getResultCode() == 0) {
                Uri uri = font.getUri();
                if (out.containsKey(uri)) {
                    continue;
                } else {
                    ByteBuffer buffer = null;
                    Throwable th3 = null;
                    pfd = null;
                    try {
                        pfd = resolver.openFileDescriptor(uri, "r", cancellationSignal);
                        if (pfd != null) {
                            Throwable th4 = null;
                            FileInputStream fis = null;
                            try {
                                FileInputStream fis2 = new FileInputStream(pfd.getFileDescriptor());
                                try {
                                    FileChannel fileChannel = fis2.getChannel();
                                    buffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                                    if (fis2 != null) {
                                        try {
                                            fis2.close();
                                        } catch (IOException e) {
                                            fis = fis2;
                                        }
                                    }
                                    if (th4 != null) {
                                        throw th4;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    fis = fis2;
                                    th2 = null;
                                    if (fis != null) {
                                        try {
                                            fis.close();
                                        } catch (IOException e2) {
                                        }
                                    }
                                    if (th2 == null) {
                                        throw th2;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                th2 = null;
                                if (fis != null) {
                                }
                                if (th2 == null) {
                                }
                            }
                        }
                    } catch (IOException e22) {
                    } catch (Throwable th7) {
                    }
                    if (pfd != null) {
                        try {
                            pfd.close();
                        } catch (Throwable th8) {
                            th3 = th8;
                        }
                    }
                    if (th3 != null) {
                        try {
                            throw th3;
                        } catch (IOException e3) {
                            Log.w(TAG, "find IOException.");
                        }
                    }
                    out.put(uri, buffer);
                }
            }
            i = i2 + 1;
        }
        if (pfd != null) {
            try {
                pfd.close();
            } catch (Throwable th9) {
                if (th2 == null) {
                    th2 = th9;
                } else if (th2 != th9) {
                    th2.addSuppressed(th9);
                }
            }
        }
        if (th2 != null) {
            throw th2;
        } else {
            throw th;
        }
    }

    public static ProviderInfo getProvider(PackageManager packageManager, FontRequest request) throws NameNotFoundException {
        String providerAuthority = request.getProviderAuthority();
        ProviderInfo info = packageManager.resolveContentProvider(providerAuthority, 0);
        if (info == null) {
            throw new NameNotFoundException("No package found for authority: " + providerAuthority);
        } else if (!info.packageName.equals(request.getProviderPackage())) {
            throw new NameNotFoundException("Found content provider " + providerAuthority + ", but package was not " + request.getProviderPackage());
        } else if (info.applicationInfo.isSystemApp()) {
            return info;
        } else {
            List<byte[]> signatures = convertToByteArrayList(packageManager.getPackageInfo(info.packageName, 64).signatures);
            Collections.sort(signatures, sByteArrayComparator);
            List<List<byte[]>> requestCertificatesList = request.getCertificates();
            for (int i = 0; i < requestCertificatesList.size(); i++) {
                List<byte[]> requestSignatures = new ArrayList((Collection) requestCertificatesList.get(i));
                Collections.sort(requestSignatures, sByteArrayComparator);
                if (equalsByteArrayList(signatures, requestSignatures)) {
                    return info;
                }
            }
            return null;
        }
    }

    static /* synthetic */ int lambda$-android_provider_FontsContract_31284(byte[] l, byte[] r) {
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
            if (!Arrays.equals((byte[]) signatures.get(i), (byte[]) requestSignatures.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<byte[]> convertToByteArrayList(Signature[] signatures) {
        List<byte[]> shas = new ArrayList();
        for (Signature toByteArray : signatures) {
            shas.add(toByteArray.toByteArray());
        }
        return shas;
    }

    public static FontInfo[] getFontFromProvider(Context context, FontRequest request, String authority, CancellationSignal cancellationSignal) {
        Throwable th;
        Throwable th2;
        ArrayList<FontInfo> result = new ArrayList();
        Uri uri = new Uri.Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).build();
        Uri fileBaseUri = new Uri.Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath("file").build();
        Throwable th3 = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_id", Columns.FILE_ID, Columns.TTC_INDEX, Columns.VARIATION_SETTINGS, Columns.WEIGHT, Columns.ITALIC, Columns.RESULT_CODE}, "query = ?", new String[]{request.getQuery()}, null, cancellationSignal);
            if (cursor != null && cursor.getCount() > 0) {
                int resultCodeColumnIndex = cursor.getColumnIndex(Columns.RESULT_CODE);
                ArrayList<FontInfo> result2 = new ArrayList();
                try {
                    int idColumnIndex = cursor.getColumnIndexOrThrow("_id");
                    int fileIdColumnIndex = cursor.getColumnIndex(Columns.FILE_ID);
                    int ttcIndexColumnIndex = cursor.getColumnIndex(Columns.TTC_INDEX);
                    int vsColumnIndex = cursor.getColumnIndex(Columns.VARIATION_SETTINGS);
                    int weightColumnIndex = cursor.getColumnIndex(Columns.WEIGHT);
                    int italicColumnIndex = cursor.getColumnIndex(Columns.ITALIC);
                    while (cursor.moveToNext()) {
                        Uri fileUri;
                        int weight;
                        boolean italic;
                        int resultCode = resultCodeColumnIndex != -1 ? cursor.getInt(resultCodeColumnIndex) : 0;
                        int ttcIndex = ttcIndexColumnIndex != -1 ? cursor.getInt(ttcIndexColumnIndex) : 0;
                        String variationSettings = vsColumnIndex != -1 ? cursor.getString(vsColumnIndex) : null;
                        if (fileIdColumnIndex == -1) {
                            fileUri = ContentUris.withAppendedId(uri, cursor.getLong(idColumnIndex));
                        } else {
                            fileUri = ContentUris.withAppendedId(fileBaseUri, cursor.getLong(fileIdColumnIndex));
                        }
                        if (weightColumnIndex == -1 || italicColumnIndex == -1) {
                            weight = 400;
                            italic = false;
                        } else {
                            weight = cursor.getInt(weightColumnIndex);
                            italic = cursor.getInt(italicColumnIndex) == 1;
                        }
                        result2.add(new FontInfo(fileUri, ttcIndex, FontVariationAxis.fromFontVariationSettings(variationSettings), weight, italic, resultCode));
                    }
                    result = result2;
                } catch (Throwable th4) {
                    th = th4;
                    th2 = null;
                    result = result2;
                }
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th5) {
                    th3 = th5;
                }
            }
            if (th3 != null) {
                throw th3;
            }
            return (FontInfo[]) result.toArray(new FontInfo[0]);
        } catch (Throwable th6) {
            th = th6;
            th2 = null;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th32) {
                    if (th2 == null) {
                        th2 = th32;
                    } else if (th2 != th32) {
                        th2.addSuppressed(th32);
                    }
                }
            }
            if (th2 != null) {
                throw th2;
            }
            throw th;
        }
    }
}
