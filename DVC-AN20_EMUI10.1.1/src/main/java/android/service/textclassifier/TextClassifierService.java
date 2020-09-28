package android.service.textclassifier;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.textclassifier.ITextClassifierService;
import android.service.textclassifier.TextClassifierService;
import android.text.TextUtils;
import android.util.Slog;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.SelectionEvent;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassificationSessionId;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextClassifierEvent;
import android.view.textclassifier.TextLanguage;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SystemApi
public abstract class TextClassifierService extends Service {
    private static final String KEY_RESULT = "key_result";
    private static final String LOG_TAG = "TextClassifierService";
    public static final String SERVICE_INTERFACE = "android.service.textclassifier.TextClassifierService";
    private final ITextClassifierService.Stub mBinder = new ITextClassifierService.Stub() {
        /* class android.service.textclassifier.TextClassifierService.AnonymousClass1 */
        private final CancellationSignal mCancellationSignal = new CancellationSignal();

        @Override // android.service.textclassifier.ITextClassifierService
        public void onSuggestSelection(TextClassificationSessionId sessionId, TextSelection.Request request, ITextClassifierCallback callback) {
            Preconditions.checkNotNull(request);
            Preconditions.checkNotNull(callback);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, request, callback) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$mKOXH9oGuUFyRzOo15GnAPhABs */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ TextSelection.Request f$2;
                private final /* synthetic */ ITextClassifierCallback f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onSuggestSelection$0$TextClassifierService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onSuggestSelection$0$TextClassifierService$1(TextClassificationSessionId sessionId, TextSelection.Request request, ITextClassifierCallback callback) {
            TextClassifierService.this.onSuggestSelection(sessionId, request, this.mCancellationSignal, new ProxyCallback(callback));
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onClassifyText(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassifierCallback callback) {
            Preconditions.checkNotNull(request);
            Preconditions.checkNotNull(callback);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, request, callback) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$LziW7ahHkWlZlAFekrEQR96QofM */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ TextClassification.Request f$2;
                private final /* synthetic */ ITextClassifierCallback f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onClassifyText$1$TextClassifierService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onClassifyText$1$TextClassifierService$1(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassifierCallback callback) {
            TextClassifierService.this.onClassifyText(sessionId, request, this.mCancellationSignal, new ProxyCallback(callback));
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onGenerateLinks(TextClassificationSessionId sessionId, TextLinks.Request request, ITextClassifierCallback callback) {
            Preconditions.checkNotNull(request);
            Preconditions.checkNotNull(callback);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, request, callback) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$suS99xMAl9SLES4WhRmaub16wIc */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ TextLinks.Request f$2;
                private final /* synthetic */ ITextClassifierCallback f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onGenerateLinks$2$TextClassifierService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onGenerateLinks$2$TextClassifierService$1(TextClassificationSessionId sessionId, TextLinks.Request request, ITextClassifierCallback callback) {
            TextClassifierService.this.onGenerateLinks(sessionId, request, this.mCancellationSignal, new ProxyCallback(callback));
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onSelectionEvent(TextClassificationSessionId sessionId, SelectionEvent event) {
            Preconditions.checkNotNull(event);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, event) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$Nsl56ysLPoVPJ4Gu0VUwYCh4wE */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ SelectionEvent f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onSelectionEvent$3$TextClassifierService$1(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onSelectionEvent$3$TextClassifierService$1(TextClassificationSessionId sessionId, SelectionEvent event) {
            TextClassifierService.this.onSelectionEvent(sessionId, event);
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onTextClassifierEvent(TextClassificationSessionId sessionId, TextClassifierEvent event) {
            Preconditions.checkNotNull(event);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, event) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$bqy_LY0V0g3pGHWd_N7ARYwQWLY */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ TextClassifierEvent f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onTextClassifierEvent$4$TextClassifierService$1(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onTextClassifierEvent$4$TextClassifierService$1(TextClassificationSessionId sessionId, TextClassifierEvent event) {
            TextClassifierService.this.onTextClassifierEvent(sessionId, event);
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onDetectLanguage(TextClassificationSessionId sessionId, TextLanguage.Request request, ITextClassifierCallback callback) {
            Preconditions.checkNotNull(request);
            Preconditions.checkNotNull(callback);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, request, callback) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$lcpBFMoy_hRkYQ42cWViBMbNnMk */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ TextLanguage.Request f$2;
                private final /* synthetic */ ITextClassifierCallback f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onDetectLanguage$5$TextClassifierService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onDetectLanguage$5$TextClassifierService$1(TextClassificationSessionId sessionId, TextLanguage.Request request, ITextClassifierCallback callback) {
            TextClassifierService.this.onDetectLanguage(sessionId, request, this.mCancellationSignal, new ProxyCallback(callback));
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onSuggestConversationActions(TextClassificationSessionId sessionId, ConversationActions.Request request, ITextClassifierCallback callback) {
            Preconditions.checkNotNull(request);
            Preconditions.checkNotNull(callback);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId, request, callback) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$Xkudza2Bh6W4NodH1DOFiRgfuM */
                private final /* synthetic */ TextClassificationSessionId f$1;
                private final /* synthetic */ ConversationActions.Request f$2;
                private final /* synthetic */ ITextClassifierCallback f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onSuggestConversationActions$6$TextClassifierService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onSuggestConversationActions$6$TextClassifierService$1(TextClassificationSessionId sessionId, ConversationActions.Request request, ITextClassifierCallback callback) {
            TextClassifierService.this.onSuggestConversationActions(sessionId, request, this.mCancellationSignal, new ProxyCallback(callback));
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onCreateTextClassificationSession(TextClassificationContext context, TextClassificationSessionId sessionId) {
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(sessionId);
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(context, sessionId) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$oecuM3n2XJWuEPg_O0hSZtoF0ls */
                private final /* synthetic */ TextClassificationContext f$1;
                private final /* synthetic */ TextClassificationSessionId f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onCreateTextClassificationSession$7$TextClassifierService$1(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onCreateTextClassificationSession$7$TextClassifierService$1(TextClassificationContext context, TextClassificationSessionId sessionId) {
            TextClassifierService.this.onCreateTextClassificationSession(context, sessionId);
        }

        @Override // android.service.textclassifier.ITextClassifierService
        public void onDestroyTextClassificationSession(TextClassificationSessionId sessionId) {
            TextClassifierService.this.mMainThreadHandler.post(new Runnable(sessionId) {
                /* class android.service.textclassifier.$$Lambda$TextClassifierService$1$fhIvecFpMXNthJWnvXRvpNrPFA */
                private final /* synthetic */ TextClassificationSessionId f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TextClassifierService.AnonymousClass1.this.lambda$onDestroyTextClassificationSession$8$TextClassifierService$1(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onDestroyTextClassificationSession$8$TextClassifierService$1(TextClassificationSessionId sessionId) {
            TextClassifierService.this.onDestroyTextClassificationSession(sessionId);
        }
    };
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper(), null, true);
    private final ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public interface Callback<T> {
        void onFailure(CharSequence charSequence);

        void onSuccess(T t);
    }

    public abstract void onClassifyText(TextClassificationSessionId textClassificationSessionId, TextClassification.Request request, CancellationSignal cancellationSignal, Callback<TextClassification> callback);

    public abstract void onGenerateLinks(TextClassificationSessionId textClassificationSessionId, TextLinks.Request request, CancellationSignal cancellationSignal, Callback<TextLinks> callback);

    public abstract void onSuggestSelection(TextClassificationSessionId textClassificationSessionId, TextSelection.Request request, CancellationSignal cancellationSignal, Callback<TextSelection> callback);

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    public void onDetectLanguage(TextClassificationSessionId sessionId, TextLanguage.Request request, CancellationSignal cancellationSignal, Callback<TextLanguage> callback) {
        this.mSingleThreadExecutor.submit(new Runnable(callback, request) {
            /* class android.service.textclassifier.$$Lambda$TextClassifierService$9kfVuo6FJ1uQiU277n9JgliEEc */
            private final /* synthetic */ TextClassifierService.Callback f$1;
            private final /* synthetic */ TextLanguage.Request f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TextClassifierService.this.lambda$onDetectLanguage$0$TextClassifierService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$onDetectLanguage$0$TextClassifierService(Callback callback, TextLanguage.Request request) {
        callback.onSuccess(getLocalTextClassifier().detectLanguage(request));
    }

    public void onSuggestConversationActions(TextClassificationSessionId sessionId, ConversationActions.Request request, CancellationSignal cancellationSignal, Callback<ConversationActions> callback) {
        this.mSingleThreadExecutor.submit(new Runnable(callback, request) {
            /* class android.service.textclassifier.$$Lambda$TextClassifierService$OMrgO9sL3mlBJfpfxbmg7ieGoWk */
            private final /* synthetic */ TextClassifierService.Callback f$1;
            private final /* synthetic */ ConversationActions.Request f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TextClassifierService.this.lambda$onSuggestConversationActions$1$TextClassifierService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$onSuggestConversationActions$1$TextClassifierService(Callback callback, ConversationActions.Request request) {
        callback.onSuccess(getLocalTextClassifier().suggestConversationActions(request));
    }

    @Deprecated
    public void onSelectionEvent(TextClassificationSessionId sessionId, SelectionEvent event) {
    }

    public void onTextClassifierEvent(TextClassificationSessionId sessionId, TextClassifierEvent event) {
    }

    public void onCreateTextClassificationSession(TextClassificationContext context, TextClassificationSessionId sessionId) {
    }

    public void onDestroyTextClassificationSession(TextClassificationSessionId sessionId) {
    }

    @Deprecated
    public final TextClassifier getLocalTextClassifier() {
        return getDefaultTextClassifierImplementation(this);
    }

    public static TextClassifier getDefaultTextClassifierImplementation(Context context) {
        TextClassificationManager tcm = (TextClassificationManager) context.getSystemService(TextClassificationManager.class);
        if (tcm != null) {
            return tcm.getTextClassifier(0);
        }
        return TextClassifier.NO_OP;
    }

    public static <T extends Parcelable> T getResponse(Bundle bundle) {
        return (T) bundle.getParcelable(KEY_RESULT);
    }

    public static ComponentName getServiceComponentName(Context context) {
        String packageName = context.getPackageManager().getSystemTextClassifierPackageName();
        if (TextUtils.isEmpty(packageName)) {
            Slog.d(LOG_TAG, "No configured system TextClassifierService");
            return null;
        }
        ResolveInfo ri = context.getPackageManager().resolveService(new Intent(SERVICE_INTERFACE).setPackage(packageName), 1048576);
        if (ri == null || ri.serviceInfo == null) {
            Slog.w(LOG_TAG, String.format("Package or service not found in package %s for user %d", packageName, Integer.valueOf(context.getUserId())));
            return null;
        }
        ServiceInfo si = ri.serviceInfo;
        if (Manifest.permission.BIND_TEXTCLASSIFIER_SERVICE.equals(si.permission)) {
            return si.getComponentName();
        }
        Slog.w(LOG_TAG, String.format("Service %s should require %s permission. Found %s permission", si.getComponentName(), Manifest.permission.BIND_TEXTCLASSIFIER_SERVICE, si.permission));
        return null;
    }

    /* access modifiers changed from: private */
    public static final class ProxyCallback<T extends Parcelable> implements Callback<T> {
        private WeakReference<ITextClassifierCallback> mTextClassifierCallback;

        private ProxyCallback(ITextClassifierCallback textClassifierCallback) {
            this.mTextClassifierCallback = new WeakReference<>((ITextClassifierCallback) Preconditions.checkNotNull(textClassifierCallback));
        }

        public void onSuccess(T result) {
            ITextClassifierCallback callback = this.mTextClassifierCallback.get();
            if (callback != null) {
                try {
                    Bundle bundle = new Bundle(1);
                    bundle.putParcelable(TextClassifierService.KEY_RESULT, result);
                    callback.onSuccess(bundle);
                } catch (RemoteException e) {
                    Slog.d(TextClassifierService.LOG_TAG, "Error calling callback");
                }
            }
        }

        @Override // android.service.textclassifier.TextClassifierService.Callback
        public void onFailure(CharSequence error) {
            ITextClassifierCallback callback = this.mTextClassifierCallback.get();
            if (callback != null) {
                try {
                    callback.onFailure();
                } catch (RemoteException e) {
                    Slog.d(TextClassifierService.LOG_TAG, "Error calling callback");
                }
            }
        }
    }
}
