package android.print;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;
import android.os.Message;
import android.print.PrintManager;
import android.printservice.recommendation.RecommendationInfo;
import com.android.internal.util.Preconditions;
import java.util.List;

public class PrintServiceRecommendationsLoader extends Loader<List<RecommendationInfo>> {
    private final Handler mHandler = new MyHandler();
    private PrintManager.PrintServiceRecommendationsChangeListener mListener;
    private final PrintManager mPrintManager;

    public PrintServiceRecommendationsLoader(PrintManager printManager, Context context) {
        super((Context) Preconditions.checkNotNull(context));
        this.mPrintManager = (PrintManager) Preconditions.checkNotNull(printManager);
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onForceLoad() {
        queueNewResult();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queueNewResult() {
        Message m = this.mHandler.obtainMessage(0);
        m.obj = this.mPrintManager.getPrintServiceRecommendations();
        this.mHandler.sendMessage(m);
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onStartLoading() {
        this.mListener = new PrintManager.PrintServiceRecommendationsChangeListener() {
            /* class android.print.PrintServiceRecommendationsLoader.AnonymousClass1 */

            @Override // android.print.PrintManager.PrintServiceRecommendationsChangeListener
            public void onPrintServiceRecommendationsChanged() {
                PrintServiceRecommendationsLoader.this.queueNewResult();
            }
        };
        this.mPrintManager.addPrintServiceRecommendationsChangeListener(this.mListener, null);
        deliverResult(this.mPrintManager.getPrintServiceRecommendations());
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onStopLoading() {
        PrintManager.PrintServiceRecommendationsChangeListener printServiceRecommendationsChangeListener = this.mListener;
        if (printServiceRecommendationsChangeListener != null) {
            this.mPrintManager.removePrintServiceRecommendationsChangeListener(printServiceRecommendationsChangeListener);
            this.mListener = null;
        }
        this.mHandler.removeMessages(0);
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onReset() {
        onStopLoading();
    }

    private class MyHandler extends Handler {
        public MyHandler() {
            super(PrintServiceRecommendationsLoader.this.getContext().getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (PrintServiceRecommendationsLoader.this.isStarted()) {
                PrintServiceRecommendationsLoader.this.deliverResult((List) msg.obj);
            }
        }
    }
}
