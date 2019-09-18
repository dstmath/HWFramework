package android.view.textclassifier;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.textclassifier.ITextClassificationCallback;
import android.service.textclassifier.ITextClassifierService;
import android.service.textclassifier.ITextLinksCallback;
import android.service.textclassifier.ITextSelectionCallback;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class SystemTextClassifier implements TextClassifier {
    private static final String LOG_TAG = "SystemTextClassifier";
    private final TextClassifier mFallback;
    private final ITextClassifierService mManagerService = ITextClassifierService.Stub.asInterface(ServiceManager.getServiceOrThrow("textclassification"));
    private final String mPackageName;
    private TextClassificationSessionId mSessionId;
    private final TextClassificationConstants mSettings;

    private static final class ResponseReceiver<T> {
        private final CountDownLatch mLatch;
        private T mResponse;

        private ResponseReceiver() {
            this.mLatch = new CountDownLatch(1);
        }

        public void onSuccess(T response) {
            this.mResponse = response;
            this.mLatch.countDown();
        }

        public void onFailure() {
            Log.e(SystemTextClassifier.LOG_TAG, "Request failed.", null);
            this.mLatch.countDown();
        }

        public T get() throws InterruptedException {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                this.mLatch.await(2, TimeUnit.SECONDS);
            }
            return this.mResponse;
        }
    }

    private static final class TextClassificationCallback extends ITextClassificationCallback.Stub {
        final ResponseReceiver<TextClassification> mReceiver;

        private TextClassificationCallback() {
            this.mReceiver = new ResponseReceiver<>();
        }

        public void onSuccess(TextClassification classification) {
            this.mReceiver.onSuccess(classification);
        }

        public void onFailure() {
            this.mReceiver.onFailure();
        }
    }

    private static final class TextLinksCallback extends ITextLinksCallback.Stub {
        final ResponseReceiver<TextLinks> mReceiver;

        private TextLinksCallback() {
            this.mReceiver = new ResponseReceiver<>();
        }

        public void onSuccess(TextLinks links) {
            this.mReceiver.onSuccess(links);
        }

        public void onFailure() {
            this.mReceiver.onFailure();
        }
    }

    private static final class TextSelectionCallback extends ITextSelectionCallback.Stub {
        final ResponseReceiver<TextSelection> mReceiver;

        private TextSelectionCallback() {
            this.mReceiver = new ResponseReceiver<>();
        }

        public void onSuccess(TextSelection selection) {
            this.mReceiver.onSuccess(selection);
        }

        public void onFailure() {
            this.mReceiver.onFailure();
        }
    }

    public SystemTextClassifier(Context context, TextClassificationConstants settings) throws ServiceManager.ServiceNotFoundException {
        this.mSettings = (TextClassificationConstants) Preconditions.checkNotNull(settings);
        this.mFallback = ((TextClassificationManager) context.getSystemService(TextClassificationManager.class)).getTextClassifier(0);
        this.mPackageName = (String) Preconditions.checkNotNull(context.getPackageName());
    }

    public TextSelection suggestSelection(TextSelection.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            TextSelectionCallback callback = new TextSelectionCallback();
            this.mManagerService.onSuggestSelection(this.mSessionId, request, callback);
            TextSelection selection = callback.mReceiver.get();
            if (selection != null) {
                return selection;
            }
        } catch (RemoteException | InterruptedException e) {
            Log.e(LOG_TAG, "Error suggesting selection for text. Using fallback.", e);
        }
        return this.mFallback.suggestSelection(request);
    }

    public TextClassification classifyText(TextClassification.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            TextClassificationCallback callback = new TextClassificationCallback();
            this.mManagerService.onClassifyText(this.mSessionId, request, callback);
            TextClassification classification = callback.mReceiver.get();
            if (classification != null) {
                return classification;
            }
        } catch (RemoteException | InterruptedException e) {
            Log.e(LOG_TAG, "Error classifying text. Using fallback.", e);
        }
        return this.mFallback.classifyText(request);
    }

    public TextLinks generateLinks(TextLinks.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        if (!this.mSettings.isSmartLinkifyEnabled() && request.isLegacyFallback()) {
            return TextClassifier.Utils.generateLegacyLinks(request);
        }
        try {
            request.setCallingPackageName(this.mPackageName);
            TextLinksCallback callback = new TextLinksCallback();
            this.mManagerService.onGenerateLinks(this.mSessionId, request, callback);
            TextLinks links = callback.mReceiver.get();
            if (links != null) {
                return links;
            }
        } catch (RemoteException | InterruptedException e) {
            Log.e(LOG_TAG, "Error generating links. Using fallback.", e);
        }
        return this.mFallback.generateLinks(request);
    }

    public void onSelectionEvent(SelectionEvent event) {
        Preconditions.checkNotNull(event);
        TextClassifier.Utils.checkMainThread();
        try {
            this.mManagerService.onSelectionEvent(this.mSessionId, event);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reporting selection event.", e);
        }
    }

    public int getMaxGenerateLinksTextLength() {
        return this.mFallback.getMaxGenerateLinksTextLength();
    }

    public void destroy() {
        try {
            if (this.mSessionId != null) {
                this.mManagerService.onDestroyTextClassificationSession(this.mSessionId);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error destroying classification session.", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeRemoteSession(TextClassificationContext classificationContext, TextClassificationSessionId sessionId) {
        this.mSessionId = (TextClassificationSessionId) Preconditions.checkNotNull(sessionId);
        try {
            this.mManagerService.onCreateTextClassificationSession(classificationContext, this.mSessionId);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error starting a new classification session.", e);
        }
    }
}
