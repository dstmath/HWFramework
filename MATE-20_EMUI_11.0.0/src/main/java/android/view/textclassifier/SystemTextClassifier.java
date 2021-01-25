package android.view.textclassifier;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.textclassifier.ITextClassifierCallback;
import android.service.textclassifier.ITextClassifierService;
import android.service.textclassifier.TextClassifierService;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLanguage;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class SystemTextClassifier implements TextClassifier {
    private static final String LOG_TAG = "SystemTextClassifier";
    private final TextClassifier mFallback;
    private final ITextClassifierService mManagerService = ITextClassifierService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.TEXT_CLASSIFICATION_SERVICE));
    private final String mPackageName;
    private TextClassificationSessionId mSessionId;
    private final TextClassificationConstants mSettings;
    private final int mUserId;

    public SystemTextClassifier(Context context, TextClassificationConstants settings) throws ServiceManager.ServiceNotFoundException {
        this.mSettings = (TextClassificationConstants) Preconditions.checkNotNull(settings);
        this.mFallback = ((TextClassificationManager) context.getSystemService(TextClassificationManager.class)).getTextClassifier(0);
        this.mPackageName = (String) Preconditions.checkNotNull(context.getOpPackageName());
        this.mUserId = context.getUserId();
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextSelection suggestSelection(TextSelection.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            request.setCallingPackageName(this.mPackageName);
            request.setUserId(this.mUserId);
            BlockingCallback<TextSelection> callback = new BlockingCallback<>("textselection");
            this.mManagerService.onSuggestSelection(this.mSessionId, request, callback);
            TextSelection selection = callback.get();
            if (selection != null) {
                return selection;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error suggesting selection for text. Using fallback.", e);
        }
        return this.mFallback.suggestSelection(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextClassification classifyText(TextClassification.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            request.setCallingPackageName(this.mPackageName);
            request.setUserId(this.mUserId);
            BlockingCallback<TextClassification> callback = new BlockingCallback<>(Context.TEXT_CLASSIFICATION_SERVICE);
            this.mManagerService.onClassifyText(this.mSessionId, request, callback);
            TextClassification classification = callback.get();
            if (classification != null) {
                return classification;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error classifying text. Using fallback.", e);
        }
        return this.mFallback.classifyText(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextLinks generateLinks(TextLinks.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        if (!this.mSettings.isSmartLinkifyEnabled() && request.isLegacyFallback()) {
            return TextClassifier.Utils.generateLegacyLinks(request);
        }
        try {
            request.setCallingPackageName(this.mPackageName);
            request.setUserId(this.mUserId);
            BlockingCallback<TextLinks> callback = new BlockingCallback<>("textlinks");
            this.mManagerService.onGenerateLinks(this.mSessionId, request, callback);
            TextLinks links = callback.get();
            if (links != null) {
                return links;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error generating links. Using fallback.", e);
        }
        return this.mFallback.generateLinks(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onSelectionEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        TextClassifier.Utils.checkMainThread();
        try {
            event.setUserId(this.mUserId);
            this.mManagerService.onSelectionEvent(this.mSessionId, event);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reporting selection event.", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onTextClassifierEvent(TextClassifierEvent event) {
        TextClassificationContext tcContext;
        Preconditions.checkNotNull(event);
        TextClassifier.Utils.checkMainThread();
        try {
            if (event.getEventContext() == null) {
                tcContext = new TextClassificationContext.Builder(this.mPackageName, "unknown").build();
            } else {
                tcContext = event.getEventContext();
            }
            tcContext.setUserId(this.mUserId);
            event.setEventContext(tcContext);
            this.mManagerService.onTextClassifierEvent(this.mSessionId, event);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reporting textclassifier event.", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextLanguage detectLanguage(TextLanguage.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            request.setCallingPackageName(this.mPackageName);
            request.setUserId(this.mUserId);
            BlockingCallback<TextLanguage> callback = new BlockingCallback<>("textlanguage");
            this.mManagerService.onDetectLanguage(this.mSessionId, request, callback);
            TextLanguage textLanguage = callback.get();
            if (textLanguage != null) {
                return textLanguage;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error detecting language.", e);
        }
        return this.mFallback.detectLanguage(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public ConversationActions suggestConversationActions(ConversationActions.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            request.setCallingPackageName(this.mPackageName);
            request.setUserId(this.mUserId);
            BlockingCallback<ConversationActions> callback = new BlockingCallback<>("conversation-actions");
            this.mManagerService.onSuggestConversationActions(this.mSessionId, request, callback);
            ConversationActions conversationActions = callback.get();
            if (conversationActions != null) {
                return conversationActions;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reporting selection event.", e);
        }
        return this.mFallback.suggestConversationActions(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public int getMaxGenerateLinksTextLength() {
        return this.mFallback.getMaxGenerateLinksTextLength();
    }

    @Override // android.view.textclassifier.TextClassifier
    public void destroy() {
        try {
            if (this.mSessionId != null) {
                this.mManagerService.onDestroyTextClassificationSession(this.mSessionId);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error destroying classification session.", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public void dump(IndentingPrintWriter printWriter) {
        printWriter.println("SystemTextClassifier:");
        printWriter.increaseIndent();
        printWriter.printPair("mFallback", this.mFallback);
        printWriter.printPair("mPackageName", this.mPackageName);
        printWriter.printPair("mSessionId", this.mSessionId);
        printWriter.printPair("mUserId", Integer.valueOf(this.mUserId));
        printWriter.decreaseIndent();
        printWriter.println();
    }

    /* access modifiers changed from: package-private */
    public void initializeRemoteSession(TextClassificationContext classificationContext, TextClassificationSessionId sessionId) {
        this.mSessionId = (TextClassificationSessionId) Preconditions.checkNotNull(sessionId);
        try {
            classificationContext.setUserId(this.mUserId);
            this.mManagerService.onCreateTextClassificationSession(classificationContext, this.mSessionId);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error starting a new classification session.", e);
        }
    }

    private static final class BlockingCallback<T extends Parcelable> extends ITextClassifierCallback.Stub {
        private final ResponseReceiver<T> mReceiver;

        BlockingCallback(String name) {
            this.mReceiver = new ResponseReceiver<>(name);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.view.textclassifier.SystemTextClassifier$ResponseReceiver<T extends android.os.Parcelable> */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.service.textclassifier.ITextClassifierCallback
        public void onSuccess(Bundle result) {
            this.mReceiver.onSuccess(TextClassifierService.getResponse(result));
        }

        @Override // android.service.textclassifier.ITextClassifierCallback
        public void onFailure() {
            this.mReceiver.onFailure();
        }

        public T get() {
            return this.mReceiver.get();
        }
    }

    /* access modifiers changed from: private */
    public static final class ResponseReceiver<T> {
        private final CountDownLatch mLatch;
        private final String mName;
        private T mResponse;

        private ResponseReceiver(String name) {
            this.mLatch = new CountDownLatch(1);
            this.mName = name;
        }

        public void onSuccess(T response) {
            this.mResponse = response;
            this.mLatch.countDown();
        }

        public void onFailure() {
            Log.e(SystemTextClassifier.LOG_TAG, "Request failed.", null);
            this.mLatch.countDown();
        }

        public T get() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                try {
                    if (!this.mLatch.await(2, TimeUnit.SECONDS)) {
                        Log.w(SystemTextClassifier.LOG_TAG, "Timeout in ResponseReceiver.get(): " + this.mName);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(SystemTextClassifier.LOG_TAG, "Interrupted during ResponseReceiver.get(): " + this.mName, e);
                }
            }
            return this.mResponse;
        }
    }
}
