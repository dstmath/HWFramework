package android.print;

import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.print.IPrinterDiscoveryObserver;
import android.util.ArrayMap;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class PrinterDiscoverySession {
    private static final String LOG_TAG = "PrinterDiscoverySession";
    private static final int MSG_PRINTERS_ADDED = 1;
    private static final int MSG_PRINTERS_REMOVED = 2;
    private final Handler mHandler;
    private boolean mIsPrinterDiscoveryStarted;
    private OnPrintersChangeListener mListener;
    private IPrinterDiscoveryObserver mObserver;
    private final IPrintManager mPrintManager;
    private final LinkedHashMap<PrinterId, PrinterInfo> mPrinters = new LinkedHashMap<>();
    private final int mUserId;

    public interface OnPrintersChangeListener {
        void onPrintersChanged();
    }

    PrinterDiscoverySession(IPrintManager printManager, Context context, int userId) {
        this.mPrintManager = printManager;
        this.mUserId = userId;
        this.mHandler = new SessionHandler(context.getMainLooper());
        this.mObserver = new PrinterDiscoveryObserver(this);
        try {
            this.mPrintManager.createPrinterDiscoverySession(this.mObserver, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error creating printer discovery session", re);
        }
    }

    public final void startPrinterDiscovery(List<PrinterId> priorityList) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring start printers discovery - session destroyed");
        } else if (!this.mIsPrinterDiscoveryStarted) {
            this.mIsPrinterDiscoveryStarted = true;
            try {
                this.mPrintManager.startPrinterDiscovery(this.mObserver, priorityList, this.mUserId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error starting printer discovery", re);
            }
        }
    }

    public final void stopPrinterDiscovery() {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring stop printers discovery - session destroyed");
        } else if (this.mIsPrinterDiscoveryStarted) {
            this.mIsPrinterDiscoveryStarted = false;
            try {
                this.mPrintManager.stopPrinterDiscovery(this.mObserver, this.mUserId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error stopping printer discovery", re);
            }
        }
    }

    public final void startPrinterStateTracking(PrinterId printerId) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring start printer state tracking - session destroyed");
            return;
        }
        try {
            this.mPrintManager.startPrinterStateTracking(printerId, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error starting printer state tracking", re);
        }
    }

    public final void stopPrinterStateTracking(PrinterId printerId) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring stop printer state tracking - session destroyed");
            return;
        }
        try {
            this.mPrintManager.stopPrinterStateTracking(printerId, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error stopping printer state tracking", re);
        }
    }

    public final void validatePrinters(List<PrinterId> printerIds) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring validate printers - session destroyed");
            return;
        }
        try {
            this.mPrintManager.validatePrinters(printerIds, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error validating printers", re);
        }
    }

    public final void destroy() {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring destroy - session destroyed");
        }
        destroyNoCheck();
    }

    public final List<PrinterInfo> getPrinters() {
        if (!isDestroyed()) {
            return new ArrayList(this.mPrinters.values());
        }
        Log.w(LOG_TAG, "Ignoring get printers - session destroyed");
        return Collections.emptyList();
    }

    public final boolean isDestroyed() {
        throwIfNotCalledOnMainThread();
        return isDestroyedNoCheck();
    }

    public final boolean isPrinterDiscoveryStarted() {
        throwIfNotCalledOnMainThread();
        return this.mIsPrinterDiscoveryStarted;
    }

    public final void setOnPrintersChangeListener(OnPrintersChangeListener listener) {
        throwIfNotCalledOnMainThread();
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public final void finalize() throws Throwable {
        if (!isDestroyedNoCheck()) {
            Log.e(LOG_TAG, "Destroying leaked printer discovery session");
            destroyNoCheck();
        }
        super.finalize();
    }

    private boolean isDestroyedNoCheck() {
        return this.mObserver == null;
    }

    private void destroyNoCheck() {
        stopPrinterDiscovery();
        try {
            this.mPrintManager.destroyPrinterDiscoverySession(this.mObserver, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error destroying printer discovery session", re);
        } catch (Throwable th) {
            this.mObserver = null;
            this.mPrinters.clear();
            throw th;
        }
        this.mObserver = null;
        this.mPrinters.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrintersAdded(List<PrinterInfo> addedPrinters) {
        if (!isDestroyed()) {
            if (this.mPrinters.isEmpty()) {
                int printerCount = addedPrinters.size();
                for (int i = 0; i < printerCount; i++) {
                    PrinterInfo printer = addedPrinters.get(i);
                    this.mPrinters.put(printer.getId(), printer);
                }
                notifyOnPrintersChanged();
                return;
            }
            ArrayMap<PrinterId, PrinterInfo> addedPrintersMap = new ArrayMap<>();
            int printerCount2 = addedPrinters.size();
            for (int i2 = 0; i2 < printerCount2; i2++) {
                PrinterInfo printer2 = addedPrinters.get(i2);
                addedPrintersMap.put(printer2.getId(), printer2);
            }
            for (PrinterId oldPrinterId : this.mPrinters.keySet()) {
                PrinterInfo updatedPrinter = addedPrintersMap.remove(oldPrinterId);
                if (updatedPrinter != null) {
                    this.mPrinters.put(oldPrinterId, updatedPrinter);
                }
            }
            this.mPrinters.putAll(addedPrintersMap);
            notifyOnPrintersChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrintersRemoved(List<PrinterId> printerIds) {
        if (!isDestroyed()) {
            boolean printersChanged = false;
            int removedPrinterIdCount = printerIds.size();
            for (int i = 0; i < removedPrinterIdCount; i++) {
                if (this.mPrinters.remove(printerIds.get(i)) != null) {
                    printersChanged = true;
                }
            }
            if (printersChanged) {
                notifyOnPrintersChanged();
            }
        }
    }

    private void notifyOnPrintersChanged() {
        OnPrintersChangeListener onPrintersChangeListener = this.mListener;
        if (onPrintersChangeListener != null) {
            onPrintersChangeListener.onPrintersChanged();
        }
    }

    private static void throwIfNotCalledOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalAccessError("must be called from the main thread");
        }
    }

    private final class SessionHandler extends Handler {
        public SessionHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                PrinterDiscoverySession.this.handlePrintersAdded((List) message.obj);
            } else if (i == 2) {
                PrinterDiscoverySession.this.handlePrintersRemoved((List) message.obj);
            }
        }
    }

    public static final class PrinterDiscoveryObserver extends IPrinterDiscoveryObserver.Stub {
        private final WeakReference<PrinterDiscoverySession> mWeakSession;

        public PrinterDiscoveryObserver(PrinterDiscoverySession session) {
            this.mWeakSession = new WeakReference<>(session);
        }

        @Override // android.print.IPrinterDiscoveryObserver
        public void onPrintersAdded(ParceledListSlice printers) {
            PrinterDiscoverySession session = this.mWeakSession.get();
            if (session != null) {
                session.mHandler.obtainMessage(1, printers.getList()).sendToTarget();
            }
        }

        @Override // android.print.IPrinterDiscoveryObserver
        public void onPrintersRemoved(ParceledListSlice printerIds) {
            PrinterDiscoverySession session = this.mWeakSession.get();
            if (session != null) {
                session.mHandler.obtainMessage(2, printerIds.getList()).sendToTarget();
            }
        }
    }
}
