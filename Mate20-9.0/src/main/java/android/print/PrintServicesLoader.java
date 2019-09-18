package android.print;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;
import android.os.Message;
import android.print.PrintManager;
import android.printservice.PrintServiceInfo;
import com.android.internal.util.Preconditions;
import java.util.List;

public class PrintServicesLoader extends Loader<List<PrintServiceInfo>> {
    private final Handler mHandler = new MyHandler();
    private PrintManager.PrintServicesChangeListener mListener;
    private final PrintManager mPrintManager;
    private final int mSelectionFlags;

    private class MyHandler extends Handler {
        public MyHandler() {
            super(PrintServicesLoader.this.getContext().getMainLooper());
        }

        public void handleMessage(Message msg) {
            if (PrintServicesLoader.this.isStarted()) {
                PrintServicesLoader.this.deliverResult((List) msg.obj);
            }
        }
    }

    public PrintServicesLoader(PrintManager printManager, Context context, int selectionFlags) {
        super((Context) Preconditions.checkNotNull(context));
        this.mPrintManager = (PrintManager) Preconditions.checkNotNull(printManager);
        this.mSelectionFlags = Preconditions.checkFlagsArgument(selectionFlags, 3);
    }

    /* access modifiers changed from: protected */
    public void onForceLoad() {
        queueNewResult();
    }

    /* access modifiers changed from: private */
    public void queueNewResult() {
        if (this.mHandler != null) {
            Message m = this.mHandler.obtainMessage(0);
            m.obj = this.mPrintManager.getPrintServices(this.mSelectionFlags);
            this.mHandler.sendMessage(m);
        }
    }

    /* access modifiers changed from: protected */
    public void onStartLoading() {
        this.mListener = new PrintManager.PrintServicesChangeListener() {
            public void onPrintServicesChanged() {
                PrintServicesLoader.this.queueNewResult();
            }
        };
        this.mPrintManager.addPrintServicesChangeListener(this.mListener, null);
        deliverResult(this.mPrintManager.getPrintServices(this.mSelectionFlags));
    }

    /* access modifiers changed from: protected */
    public void onStopLoading() {
        if (this.mListener != null) {
            this.mPrintManager.removePrintServicesChangeListener(this.mListener);
            this.mListener = null;
        }
        this.mHandler.removeMessages(0);
    }

    /* access modifiers changed from: protected */
    public void onReset() {
        onStopLoading();
    }
}
