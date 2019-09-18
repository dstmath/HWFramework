package android.printservice;

import android.content.pm.ParceledListSlice;
import android.os.CancellationSignal;
import android.os.RemoteException;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PrinterDiscoverySession {
    private static final String LOG_TAG = "PrinterDiscoverySession";
    private static int sIdCounter = 0;
    private final int mId;
    private boolean mIsDestroyed;
    private boolean mIsDiscoveryStarted;
    private ArrayMap<PrinterId, PrinterInfo> mLastSentPrinters;
    private IPrintServiceClient mObserver;
    private final ArrayMap<PrinterId, PrinterInfo> mPrinters = new ArrayMap<>();
    private final List<PrinterId> mTrackedPrinters = new ArrayList();

    public abstract void onDestroy();

    public abstract void onStartPrinterDiscovery(List<PrinterId> list);

    public abstract void onStartPrinterStateTracking(PrinterId printerId);

    public abstract void onStopPrinterDiscovery();

    public abstract void onStopPrinterStateTracking(PrinterId printerId);

    public abstract void onValidatePrinters(List<PrinterId> list);

    public PrinterDiscoverySession() {
        int i = sIdCounter;
        sIdCounter = i + 1;
        this.mId = i;
    }

    /* access modifiers changed from: package-private */
    public void setObserver(IPrintServiceClient observer) {
        this.mObserver = observer;
        if (!this.mPrinters.isEmpty()) {
            try {
                this.mObserver.onPrintersAdded(new ParceledListSlice(getPrinters()));
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error sending added printers", re);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getId() {
        return this.mId;
    }

    public final List<PrinterInfo> getPrinters() {
        PrintService.throwIfNotCalledOnMainThread();
        if (this.mIsDestroyed) {
            return Collections.emptyList();
        }
        return new ArrayList(this.mPrinters.values());
    }

    public final void addPrinters(List<PrinterInfo> printers) {
        PrintService.throwIfNotCalledOnMainThread();
        if (this.mIsDestroyed) {
            Log.w(LOG_TAG, "Not adding printers - session destroyed.");
            return;
        }
        int i = 0;
        if (this.mIsDiscoveryStarted) {
            List<PrinterInfo> addedPrinters = null;
            int addedPrinterCount = printers.size();
            while (i < addedPrinterCount) {
                PrinterInfo addedPrinter = printers.get(i);
                PrinterInfo oldPrinter = this.mPrinters.put(addedPrinter.getId(), addedPrinter);
                if (oldPrinter == null || !oldPrinter.equals(addedPrinter)) {
                    if (addedPrinters == null) {
                        addedPrinters = new ArrayList<>();
                    }
                    addedPrinters.add(addedPrinter);
                }
                i++;
            }
            if (addedPrinters != null) {
                try {
                    this.mObserver.onPrintersAdded(new ParceledListSlice(addedPrinters));
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error sending added printers", re);
                }
            }
        } else {
            if (this.mLastSentPrinters == null) {
                this.mLastSentPrinters = new ArrayMap<>(this.mPrinters);
            }
            int addedPrinterCount2 = printers.size();
            while (i < addedPrinterCount2) {
                PrinterInfo addedPrinter2 = printers.get(i);
                if (this.mPrinters.get(addedPrinter2.getId()) == null) {
                    this.mPrinters.put(addedPrinter2.getId(), addedPrinter2);
                }
                i++;
            }
        }
    }

    public final void removePrinters(List<PrinterId> printerIds) {
        PrintService.throwIfNotCalledOnMainThread();
        if (this.mIsDestroyed) {
            Log.w(LOG_TAG, "Not removing printers - session destroyed.");
            return;
        }
        int i = 0;
        if (this.mIsDiscoveryStarted) {
            List<PrinterId> removedPrinterIds = new ArrayList<>();
            int removedPrinterIdCount = printerIds.size();
            while (i < removedPrinterIdCount) {
                PrinterId removedPrinterId = printerIds.get(i);
                if (this.mPrinters.remove(removedPrinterId) != null) {
                    removedPrinterIds.add(removedPrinterId);
                }
                i++;
            }
            if (removedPrinterIds.isEmpty() == 0) {
                try {
                    this.mObserver.onPrintersRemoved(new ParceledListSlice(removedPrinterIds));
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error sending removed printers", re);
                }
            }
        } else {
            if (this.mLastSentPrinters == null) {
                this.mLastSentPrinters = new ArrayMap<>(this.mPrinters);
            }
            int removedPrinterIdCount2 = printerIds.size();
            while (i < removedPrinterIdCount2) {
                this.mPrinters.remove(printerIds.get(i));
                i++;
            }
        }
    }

    private void sendOutOfDiscoveryPeriodPrinterChanges() {
        if (this.mLastSentPrinters == null || this.mLastSentPrinters.isEmpty()) {
            this.mLastSentPrinters = null;
            return;
        }
        List<PrinterInfo> addedPrinters = null;
        for (PrinterInfo printer : this.mPrinters.values()) {
            PrinterInfo sentPrinter = this.mLastSentPrinters.get(printer.getId());
            if (sentPrinter == null || !sentPrinter.equals(printer)) {
                if (addedPrinters == null) {
                    addedPrinters = new ArrayList<>();
                }
                addedPrinters.add(printer);
            }
        }
        if (addedPrinters != null) {
            try {
                this.mObserver.onPrintersAdded(new ParceledListSlice(addedPrinters));
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error sending added printers", re);
            }
        }
        List<PrinterId> removedPrinterIds = null;
        for (PrinterInfo sentPrinter2 : this.mLastSentPrinters.values()) {
            if (!this.mPrinters.containsKey(sentPrinter2.getId())) {
                if (removedPrinterIds == null) {
                    removedPrinterIds = new ArrayList<>();
                }
                removedPrinterIds.add(sentPrinter2.getId());
            }
        }
        if (removedPrinterIds != null) {
            try {
                this.mObserver.onPrintersRemoved(new ParceledListSlice(removedPrinterIds));
            } catch (RemoteException re2) {
                Log.e(LOG_TAG, "Error sending removed printers", re2);
            }
        }
        this.mLastSentPrinters = null;
    }

    public void onRequestCustomPrinterIcon(PrinterId printerId, CancellationSignal cancellationSignal, CustomPrinterIconCallback callback) {
    }

    public final List<PrinterId> getTrackedPrinters() {
        PrintService.throwIfNotCalledOnMainThread();
        if (this.mIsDestroyed) {
            return Collections.emptyList();
        }
        return new ArrayList(this.mTrackedPrinters);
    }

    public final boolean isDestroyed() {
        PrintService.throwIfNotCalledOnMainThread();
        return this.mIsDestroyed;
    }

    public final boolean isPrinterDiscoveryStarted() {
        PrintService.throwIfNotCalledOnMainThread();
        return this.mIsDiscoveryStarted;
    }

    /* access modifiers changed from: package-private */
    public void startPrinterDiscovery(List<PrinterId> priorityList) {
        if (!this.mIsDestroyed) {
            this.mIsDiscoveryStarted = true;
            sendOutOfDiscoveryPeriodPrinterChanges();
            if (priorityList == null) {
                priorityList = Collections.emptyList();
            }
            onStartPrinterDiscovery(priorityList);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopPrinterDiscovery() {
        if (!this.mIsDestroyed) {
            this.mIsDiscoveryStarted = false;
            onStopPrinterDiscovery();
        }
    }

    /* access modifiers changed from: package-private */
    public void validatePrinters(List<PrinterId> printerIds) {
        if (!this.mIsDestroyed && this.mObserver != null) {
            onValidatePrinters(printerIds);
        }
    }

    /* access modifiers changed from: package-private */
    public void startPrinterStateTracking(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null && !this.mTrackedPrinters.contains(printerId)) {
            this.mTrackedPrinters.add(printerId);
            onStartPrinterStateTracking(printerId);
        }
    }

    /* access modifiers changed from: package-private */
    public void requestCustomPrinterIcon(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null) {
            onRequestCustomPrinterIcon(printerId, new CancellationSignal(), new CustomPrinterIconCallback(printerId, this.mObserver));
        }
    }

    /* access modifiers changed from: package-private */
    public void stopPrinterStateTracking(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null && this.mTrackedPrinters.remove(printerId)) {
            onStopPrinterStateTracking(printerId);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        if (!this.mIsDestroyed) {
            this.mIsDestroyed = true;
            this.mIsDiscoveryStarted = false;
            this.mPrinters.clear();
            this.mLastSentPrinters = null;
            this.mObserver = null;
            onDestroy();
        }
    }
}
