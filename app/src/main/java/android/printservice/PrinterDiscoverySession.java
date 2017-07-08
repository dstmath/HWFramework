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
    private static int sIdCounter;
    private final int mId;
    private boolean mIsDestroyed;
    private boolean mIsDiscoveryStarted;
    private ArrayMap<PrinterId, PrinterInfo> mLastSentPrinters;
    private IPrintServiceClient mObserver;
    private final ArrayMap<PrinterId, PrinterInfo> mPrinters;
    private final List<PrinterId> mTrackedPrinters;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.printservice.PrinterDiscoverySession.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.printservice.PrinterDiscoverySession.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.printservice.PrinterDiscoverySession.<clinit>():void");
    }

    public abstract void onDestroy();

    public abstract void onStartPrinterDiscovery(List<PrinterId> list);

    public abstract void onStartPrinterStateTracking(PrinterId printerId);

    public abstract void onStopPrinterDiscovery();

    public abstract void onStopPrinterStateTracking(PrinterId printerId);

    public abstract void onValidatePrinters(List<PrinterId> list);

    public PrinterDiscoverySession() {
        this.mPrinters = new ArrayMap();
        this.mTrackedPrinters = new ArrayList();
        int i = sIdCounter;
        sIdCounter = i + 1;
        this.mId = i;
    }

    void setObserver(IPrintServiceClient observer) {
        this.mObserver = observer;
        if (!this.mPrinters.isEmpty()) {
            try {
                this.mObserver.onPrintersAdded(new ParceledListSlice(getPrinters()));
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error sending added printers", re);
            }
        }
    }

    int getId() {
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
        int addedPrinterCount;
        int i;
        PrinterInfo addedPrinter;
        if (this.mIsDiscoveryStarted) {
            List addedPrinters = null;
            addedPrinterCount = printers.size();
            for (i = 0; i < addedPrinterCount; i++) {
                addedPrinter = (PrinterInfo) printers.get(i);
                PrinterInfo oldPrinter = (PrinterInfo) this.mPrinters.put(addedPrinter.getId(), addedPrinter);
                if (oldPrinter == null || !oldPrinter.equals(addedPrinter)) {
                    if (addedPrinters == null) {
                        addedPrinters = new ArrayList();
                    }
                    addedPrinters.add(addedPrinter);
                }
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
                this.mLastSentPrinters = new ArrayMap(this.mPrinters);
            }
            addedPrinterCount = printers.size();
            for (i = 0; i < addedPrinterCount; i++) {
                addedPrinter = (PrinterInfo) printers.get(i);
                if (this.mPrinters.get(addedPrinter.getId()) == null) {
                    this.mPrinters.put(addedPrinter.getId(), addedPrinter);
                }
            }
        }
    }

    public final void removePrinters(List<PrinterId> printerIds) {
        PrintService.throwIfNotCalledOnMainThread();
        if (this.mIsDestroyed) {
            Log.w(LOG_TAG, "Not removing printers - session destroyed.");
            return;
        }
        int removedPrinterIdCount;
        int i;
        if (this.mIsDiscoveryStarted) {
            List<PrinterId> removedPrinterIds = new ArrayList();
            removedPrinterIdCount = printerIds.size();
            for (i = 0; i < removedPrinterIdCount; i++) {
                PrinterId removedPrinterId = (PrinterId) printerIds.get(i);
                if (this.mPrinters.remove(removedPrinterId) != null) {
                    removedPrinterIds.add(removedPrinterId);
                }
            }
            if (!removedPrinterIds.isEmpty()) {
                try {
                    this.mObserver.onPrintersRemoved(new ParceledListSlice(removedPrinterIds));
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error sending removed printers", re);
                }
            }
        } else {
            if (this.mLastSentPrinters == null) {
                this.mLastSentPrinters = new ArrayMap(this.mPrinters);
            }
            removedPrinterIdCount = printerIds.size();
            for (i = 0; i < removedPrinterIdCount; i++) {
                this.mPrinters.remove((PrinterId) printerIds.get(i));
            }
        }
    }

    private void sendOutOfDiscoveryPeriodPrinterChanges() {
        if (this.mLastSentPrinters == null || this.mLastSentPrinters.isEmpty()) {
            this.mLastSentPrinters = null;
            return;
        }
        List addedPrinters = null;
        for (PrinterInfo printer : this.mPrinters.values()) {
            PrinterInfo sentPrinter = (PrinterInfo) this.mLastSentPrinters.get(printer.getId());
            if (sentPrinter == null || !sentPrinter.equals(printer)) {
                if (addedPrinters == null) {
                    addedPrinters = new ArrayList();
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
        List removedPrinterIds = null;
        for (PrinterInfo sentPrinter2 : this.mLastSentPrinters.values()) {
            if (!this.mPrinters.containsKey(sentPrinter2.getId())) {
                if (removedPrinterIds == null) {
                    removedPrinterIds = new ArrayList();
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

    void startPrinterDiscovery(List<PrinterId> priorityList) {
        if (!this.mIsDestroyed) {
            this.mIsDiscoveryStarted = true;
            sendOutOfDiscoveryPeriodPrinterChanges();
            if (priorityList == null) {
                priorityList = Collections.emptyList();
            }
            onStartPrinterDiscovery(priorityList);
        }
    }

    void stopPrinterDiscovery() {
        if (!this.mIsDestroyed) {
            this.mIsDiscoveryStarted = false;
            onStopPrinterDiscovery();
        }
    }

    void validatePrinters(List<PrinterId> printerIds) {
        if (!this.mIsDestroyed && this.mObserver != null) {
            onValidatePrinters(printerIds);
        }
    }

    void startPrinterStateTracking(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null && !this.mTrackedPrinters.contains(printerId)) {
            this.mTrackedPrinters.add(printerId);
            onStartPrinterStateTracking(printerId);
        }
    }

    void requestCustomPrinterIcon(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null) {
            onRequestCustomPrinterIcon(printerId, new CancellationSignal(), new CustomPrinterIconCallback(printerId, this.mObserver));
        }
    }

    void stopPrinterStateTracking(PrinterId printerId) {
        if (!this.mIsDestroyed && this.mObserver != null && this.mTrackedPrinters.remove(printerId)) {
            onStopPrinterStateTracking(printerId);
        }
    }

    void destroy() {
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
