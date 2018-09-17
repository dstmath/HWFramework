package com.android.server.print;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.print.IPrintDocumentAdapter;
import android.print.IPrintJobStateChangeListener;
import android.print.IPrintServicesChangeListener;
import android.print.IPrinterDiscoveryObserver;
import android.print.IPrinterDiscoveryObserver.Stub;
import android.print.PrintAttributes;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.print.PrinterInfo.Builder;
import android.printservice.PrintServiceInfo;
import android.printservice.recommendation.IRecommendationsChangeListener;
import android.printservice.recommendation.RecommendationInfo;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.server.print.RemotePrintService.PrintServiceCallbacks;
import com.android.server.print.RemotePrintServiceRecommendationService.RemotePrintServiceRecommendationServiceCallbacks;
import com.android.server.print.RemotePrintSpooler.PrintSpoolerCallbacks;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

final class UserState implements PrintSpoolerCallbacks, PrintServiceCallbacks, RemotePrintServiceRecommendationServiceCallbacks {
    private static final char COMPONENT_NAME_SEPARATOR = ':';
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "UserState";
    private static final int SERVICE_RESTART_DELAY_MILLIS = 500;
    private final ArrayMap<ComponentName, RemotePrintService> mActiveServices = new ArrayMap();
    private final Context mContext;
    private boolean mDestroyed;
    private final Set<ComponentName> mDisabledServices = new ArraySet();
    private final Handler mHandler;
    private final List<PrintServiceInfo> mInstalledServices = new ArrayList();
    private final Object mLock;
    private final PrintJobForAppCache mPrintJobForAppCache = new PrintJobForAppCache(this, null);
    private List<PrintJobStateChangeListenerRecord> mPrintJobStateChangeListenerRecords;
    private List<RecommendationInfo> mPrintServiceRecommendations;
    private List<ListenerRecord<IRecommendationsChangeListener>> mPrintServiceRecommendationsChangeListenerRecords;
    private RemotePrintServiceRecommendationService mPrintServiceRecommendationsService;
    private List<ListenerRecord<IPrintServicesChangeListener>> mPrintServicesChangeListenerRecords;
    private PrinterDiscoverySessionMediator mPrinterDiscoverySession;
    private final Intent mQueryIntent = new Intent("android.printservice.PrintService");
    private final RemotePrintSpooler mSpooler;
    private final SimpleStringSplitter mStringColonSplitter = new SimpleStringSplitter(COMPONENT_NAME_SEPARATOR);
    private final int mUserId;

    private class PrinterDiscoverySessionMediator {
        private final RemoteCallbackList<IPrinterDiscoveryObserver> mDiscoveryObservers = new RemoteCallbackList<IPrinterDiscoveryObserver>() {
            public void onCallbackDied(IPrinterDiscoveryObserver observer) {
                synchronized (UserState.this.mLock) {
                    PrinterDiscoverySessionMediator.this.stopPrinterDiscoveryLocked(observer);
                    PrinterDiscoverySessionMediator.this.removeObserverLocked(observer);
                }
            }
        };
        private boolean mIsDestroyed;
        private final ArrayMap<PrinterId, PrinterInfo> mPrinters = new ArrayMap();
        private final Handler mSessionHandler;
        private final List<IBinder> mStartedPrinterDiscoveryTokens = new ArrayList();
        private final List<PrinterId> mStateTrackedPrinters = new ArrayList();

        private final class SessionHandler extends Handler {
            public static final int MSG_CREATE_PRINTER_DISCOVERY_SESSION = 5;
            public static final int MSG_DESTROY_PRINTER_DISCOVERY_SESSION = 6;
            public static final int MSG_DESTROY_SERVICE = 16;
            public static final int MSG_DISPATCH_CREATE_PRINTER_DISCOVERY_SESSION = 9;
            public static final int MSG_DISPATCH_DESTROY_PRINTER_DISCOVERY_SESSION = 10;
            public static final int MSG_DISPATCH_PRINTERS_ADDED = 3;
            public static final int MSG_DISPATCH_PRINTERS_REMOVED = 4;
            public static final int MSG_DISPATCH_START_PRINTER_DISCOVERY = 11;
            public static final int MSG_DISPATCH_STOP_PRINTER_DISCOVERY = 12;
            public static final int MSG_PRINTERS_ADDED = 1;
            public static final int MSG_PRINTERS_REMOVED = 2;
            public static final int MSG_START_PRINTER_DISCOVERY = 7;
            public static final int MSG_START_PRINTER_STATE_TRACKING = 14;
            public static final int MSG_STOP_PRINTER_DISCOVERY = 8;
            public static final int MSG_STOP_PRINTER_STATE_TRACKING = 15;
            public static final int MSG_VALIDATE_PRINTERS = 13;

            SessionHandler(Looper looper) {
                super(looper, null, false);
            }

            public void handleMessage(Message message) {
                SomeArgs args;
                IPrinterDiscoveryObserver observer;
                List<PrinterId> printerIds;
                RemotePrintService service;
                PrinterId printerId;
                switch (message.what) {
                    case 1:
                        args = message.obj;
                        observer = args.arg1;
                        List<PrinterInfo> addedPrinters = args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handlePrintersAdded(observer, addedPrinters);
                        return;
                    case 2:
                        args = (SomeArgs) message.obj;
                        observer = (IPrinterDiscoveryObserver) args.arg1;
                        List<PrinterId> removedPrinterIds = args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handlePrintersRemoved(observer, removedPrinterIds);
                        break;
                    case 3:
                        break;
                    case 4:
                        PrinterDiscoverySessionMediator.this.handleDispatchPrintersRemoved((List) message.obj);
                        return;
                    case 5:
                        message.obj.createPrinterDiscoverySession();
                        return;
                    case 6:
                        ((RemotePrintService) message.obj).destroyPrinterDiscoverySession();
                        return;
                    case 7:
                        ((RemotePrintService) message.obj).startPrinterDiscovery(null);
                        return;
                    case 8:
                        ((RemotePrintService) message.obj).stopPrinterDiscovery();
                        return;
                    case 9:
                        PrinterDiscoverySessionMediator.this.handleDispatchCreatePrinterDiscoverySession(message.obj);
                        return;
                    case 10:
                        PrinterDiscoverySessionMediator.this.handleDispatchDestroyPrinterDiscoverySession((List) message.obj);
                        return;
                    case 11:
                        args = (SomeArgs) message.obj;
                        List<RemotePrintService> services = (List) args.arg1;
                        printerIds = args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handleDispatchStartPrinterDiscovery(services, printerIds);
                        return;
                    case 12:
                        PrinterDiscoverySessionMediator.this.handleDispatchStopPrinterDiscovery((List) message.obj);
                        return;
                    case 13:
                        args = (SomeArgs) message.obj;
                        service = (RemotePrintService) args.arg1;
                        printerIds = (List) args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handleValidatePrinters(service, printerIds);
                        return;
                    case 14:
                        args = (SomeArgs) message.obj;
                        service = (RemotePrintService) args.arg1;
                        printerId = args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handleStartPrinterStateTracking(service, printerId);
                        return;
                    case 15:
                        args = (SomeArgs) message.obj;
                        service = (RemotePrintService) args.arg1;
                        printerId = (PrinterId) args.arg2;
                        args.recycle();
                        PrinterDiscoverySessionMediator.this.handleStopPrinterStateTracking(service, printerId);
                        return;
                    case 16:
                        ((RemotePrintService) message.obj).destroy();
                        return;
                    default:
                        return;
                }
                PrinterDiscoverySessionMediator.this.handleDispatchPrintersAdded((List) message.obj);
            }
        }

        public PrinterDiscoverySessionMediator(Context context) {
            this.mSessionHandler = new SessionHandler(context.getMainLooper());
            this.mSessionHandler.obtainMessage(9, new ArrayList(UserState.this.mActiveServices.values())).sendToTarget();
        }

        public void addObserverLocked(IPrinterDiscoveryObserver observer) {
            this.mDiscoveryObservers.register(observer);
            if (!this.mPrinters.isEmpty()) {
                List<PrinterInfo> printers = new ArrayList(this.mPrinters.values());
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = observer;
                args.arg2 = printers;
                this.mSessionHandler.obtainMessage(1, args).sendToTarget();
            }
        }

        public void removeObserverLocked(IPrinterDiscoveryObserver observer) {
            this.mDiscoveryObservers.unregister(observer);
            if (this.mDiscoveryObservers.getRegisteredCallbackCount() == 0) {
                destroyLocked();
            }
        }

        public final void startPrinterDiscoveryLocked(IPrinterDiscoveryObserver observer, List<PrinterId> priorityList) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not starting dicovery - session destroyed");
                return;
            }
            boolean discoveryStarted = this.mStartedPrinterDiscoveryTokens.isEmpty() ^ 1;
            this.mStartedPrinterDiscoveryTokens.add(observer.asBinder());
            if (discoveryStarted && priorityList != null && (priorityList.isEmpty() ^ 1) != 0) {
                UserState.this.validatePrinters(priorityList);
            } else if (this.mStartedPrinterDiscoveryTokens.size() <= 1) {
                List<RemotePrintService> services = new ArrayList(UserState.this.mActiveServices.values());
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = services;
                args.arg2 = priorityList;
                this.mSessionHandler.obtainMessage(11, args).sendToTarget();
            }
        }

        public final void stopPrinterDiscoveryLocked(IPrinterDiscoveryObserver observer) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not stopping dicovery - session destroyed");
            } else if (this.mStartedPrinterDiscoveryTokens.remove(observer.asBinder()) && this.mStartedPrinterDiscoveryTokens.isEmpty()) {
                this.mSessionHandler.obtainMessage(12, new ArrayList(UserState.this.mActiveServices.values())).sendToTarget();
            }
        }

        public void validatePrintersLocked(List<PrinterId> printerIds) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not validating pritners - session destroyed");
                return;
            }
            List<PrinterId> remainingList = new ArrayList(printerIds);
            while (!remainingList.isEmpty()) {
                Iterator<PrinterId> iterator = remainingList.iterator();
                List<PrinterId> updateList = new ArrayList();
                Object serviceName = null;
                while (iterator.hasNext()) {
                    PrinterId printerId = (PrinterId) iterator.next();
                    if (printerId != null) {
                        if (updateList.isEmpty()) {
                            updateList.add(printerId);
                            serviceName = printerId.getServiceName();
                            iterator.remove();
                        } else if (printerId.getServiceName().equals(serviceName)) {
                            updateList.add(printerId);
                            iterator.remove();
                        }
                    }
                }
                RemotePrintService service = (RemotePrintService) UserState.this.mActiveServices.get(serviceName);
                if (service != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = service;
                    args.arg2 = updateList;
                    this.mSessionHandler.obtainMessage(13, args).sendToTarget();
                }
            }
        }

        public final void startPrinterStateTrackingLocked(PrinterId printerId) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not starting printer state tracking - session destroyed");
            } else if (!this.mStartedPrinterDiscoveryTokens.isEmpty()) {
                boolean containedPrinterId = this.mStateTrackedPrinters.contains(printerId);
                this.mStateTrackedPrinters.add(printerId);
                if (!containedPrinterId) {
                    RemotePrintService service = (RemotePrintService) UserState.this.mActiveServices.get(printerId.getServiceName());
                    if (service != null) {
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = service;
                        args.arg2 = printerId;
                        this.mSessionHandler.obtainMessage(14, args).sendToTarget();
                    }
                }
            }
        }

        public final void stopPrinterStateTrackingLocked(PrinterId printerId) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not stopping printer state tracking - session destroyed");
            } else if (!this.mStartedPrinterDiscoveryTokens.isEmpty() && this.mStateTrackedPrinters.remove(printerId)) {
                RemotePrintService service = (RemotePrintService) UserState.this.mActiveServices.get(printerId.getServiceName());
                if (service != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = service;
                    args.arg2 = printerId;
                    this.mSessionHandler.obtainMessage(15, args).sendToTarget();
                }
            }
        }

        public void onDestroyed() {
        }

        public void destroyLocked() {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not destroying - session destroyed");
                return;
            }
            int i;
            this.mIsDestroyed = true;
            int printerCount = this.mStateTrackedPrinters.size();
            for (i = 0; i < printerCount; i++) {
                UserState.this.stopPrinterStateTracking((PrinterId) this.mStateTrackedPrinters.get(i));
            }
            int observerCount = this.mStartedPrinterDiscoveryTokens.size();
            for (i = 0; i < observerCount; i++) {
                stopPrinterDiscoveryLocked(Stub.asInterface((IBinder) this.mStartedPrinterDiscoveryTokens.get(i)));
            }
            this.mSessionHandler.obtainMessage(10, new ArrayList(UserState.this.mActiveServices.values())).sendToTarget();
        }

        public void onPrintersAddedLocked(List<PrinterInfo> printers) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not adding printers - session destroyed");
                return;
            }
            Object addedPrinters = null;
            int addedPrinterCount = printers.size();
            for (int i = 0; i < addedPrinterCount; i++) {
                PrinterInfo printer = (PrinterInfo) printers.get(i);
                PrinterInfo oldPrinter = (PrinterInfo) this.mPrinters.put(printer.getId(), printer);
                if (oldPrinter == null || (oldPrinter.equals(printer) ^ 1) != 0) {
                    if (addedPrinters == null) {
                        addedPrinters = new ArrayList();
                    }
                    addedPrinters.add(printer);
                }
            }
            if (addedPrinters != null) {
                this.mSessionHandler.obtainMessage(3, addedPrinters).sendToTarget();
            }
        }

        public void onPrintersRemovedLocked(List<PrinterId> printerIds) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not removing printers - session destroyed");
                return;
            }
            Object removedPrinterIds = null;
            int removedPrinterCount = printerIds.size();
            for (int i = 0; i < removedPrinterCount; i++) {
                PrinterId removedPrinterId = (PrinterId) printerIds.get(i);
                if (this.mPrinters.remove(removedPrinterId) != null) {
                    if (removedPrinterIds == null) {
                        removedPrinterIds = new ArrayList();
                    }
                    removedPrinterIds.add(removedPrinterId);
                }
            }
            if (removedPrinterIds != null) {
                this.mSessionHandler.obtainMessage(4, removedPrinterIds).sendToTarget();
            }
        }

        public void onServiceRemovedLocked(RemotePrintService service) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not updating removed service - session destroyed");
                return;
            }
            removePrintersForServiceLocked(service.getComponentName());
            service.destroy();
        }

        public void onCustomPrinterIconLoadedLocked(PrinterId printerId) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not updating printer - session destroyed");
                return;
            }
            PrinterInfo printer = (PrinterInfo) this.mPrinters.get(printerId);
            if (printer != null) {
                PrinterInfo newPrinter = new Builder(printer).incCustomPrinterIconGen().build();
                this.mPrinters.put(printerId, newPrinter);
                ArrayList<PrinterInfo> addedPrinters = new ArrayList(1);
                addedPrinters.add(newPrinter);
                this.mSessionHandler.obtainMessage(3, addedPrinters).sendToTarget();
            }
        }

        public void onServiceDiedLocked(RemotePrintService service) {
            UserState.this.removeServiceLocked(service);
        }

        public void onServiceAddedLocked(RemotePrintService service) {
            if (this.mIsDestroyed) {
                Log.w(UserState.LOG_TAG, "Not updating added service - session destroyed");
                return;
            }
            this.mSessionHandler.obtainMessage(5, service).sendToTarget();
            if (!this.mStartedPrinterDiscoveryTokens.isEmpty()) {
                this.mSessionHandler.obtainMessage(7, service).sendToTarget();
            }
            int trackedPrinterCount = this.mStateTrackedPrinters.size();
            for (int i = 0; i < trackedPrinterCount; i++) {
                PrinterId printerId = (PrinterId) this.mStateTrackedPrinters.get(i);
                if (printerId.getServiceName().equals(service.getComponentName())) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = service;
                    args.arg2 = printerId;
                    this.mSessionHandler.obtainMessage(14, args).sendToTarget();
                }
            }
        }

        public void dump(PrintWriter pw, String prefix) {
            int i;
            pw.append(prefix).append("destroyed=").append(String.valueOf(UserState.this.mDestroyed)).println();
            pw.append(prefix).append("printDiscoveryInProgress=").append(String.valueOf(this.mStartedPrinterDiscoveryTokens.isEmpty() ^ 1)).println();
            String tab = "  ";
            pw.append(prefix).append(tab).append("printer discovery observers:").println();
            int observerCount = this.mDiscoveryObservers.beginBroadcast();
            for (i = 0; i < observerCount; i++) {
                pw.append(prefix).append(prefix).append(((IPrinterDiscoveryObserver) this.mDiscoveryObservers.getBroadcastItem(i)).toString());
                pw.println();
            }
            this.mDiscoveryObservers.finishBroadcast();
            pw.append(prefix).append(tab).append("start discovery requests:").println();
            int tokenCount = this.mStartedPrinterDiscoveryTokens.size();
            for (i = 0; i < tokenCount; i++) {
                pw.append(prefix).append(tab).append(tab).append(((IBinder) this.mStartedPrinterDiscoveryTokens.get(i)).toString()).println();
            }
            pw.append(prefix).append(tab).append("tracked printer requests:").println();
            int trackedPrinters = this.mStateTrackedPrinters.size();
            for (i = 0; i < trackedPrinters; i++) {
                pw.append(prefix).append(tab).append(tab).append(((PrinterId) this.mStateTrackedPrinters.get(i)).toString()).println();
            }
            pw.append(prefix).append(tab).append("printers:").println();
            int pritnerCount = this.mPrinters.size();
            for (i = 0; i < pritnerCount; i++) {
                pw.append(prefix).append(tab).append(tab).append(((PrinterInfo) this.mPrinters.valueAt(i)).toString()).println();
            }
        }

        private void removePrintersForServiceLocked(ComponentName serviceName) {
            if (!this.mPrinters.isEmpty()) {
                int i;
                List removedPrinterIds = null;
                int printerCount = this.mPrinters.size();
                for (i = 0; i < printerCount; i++) {
                    PrinterId printerId = (PrinterId) this.mPrinters.keyAt(i);
                    if (printerId.getServiceName().equals(serviceName)) {
                        if (removedPrinterIds == null) {
                            removedPrinterIds = new ArrayList();
                        }
                        removedPrinterIds.add(printerId);
                    }
                }
                if (removedPrinterIds != null) {
                    int removedPrinterCount = removedPrinterIds.size();
                    for (i = 0; i < removedPrinterCount; i++) {
                        this.mPrinters.remove(removedPrinterIds.get(i));
                    }
                    this.mSessionHandler.obtainMessage(4, removedPrinterIds).sendToTarget();
                }
            }
        }

        private void handleDispatchPrintersAdded(List<PrinterInfo> addedPrinters) {
            int observerCount = this.mDiscoveryObservers.beginBroadcast();
            for (int i = 0; i < observerCount; i++) {
                handlePrintersAdded((IPrinterDiscoveryObserver) this.mDiscoveryObservers.getBroadcastItem(i), addedPrinters);
            }
            this.mDiscoveryObservers.finishBroadcast();
        }

        private void handleDispatchPrintersRemoved(List<PrinterId> removedPrinterIds) {
            int observerCount = this.mDiscoveryObservers.beginBroadcast();
            for (int i = 0; i < observerCount; i++) {
                handlePrintersRemoved((IPrinterDiscoveryObserver) this.mDiscoveryObservers.getBroadcastItem(i), removedPrinterIds);
            }
            this.mDiscoveryObservers.finishBroadcast();
        }

        private void handleDispatchCreatePrinterDiscoverySession(List<RemotePrintService> services) {
            int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                ((RemotePrintService) services.get(i)).createPrinterDiscoverySession();
            }
        }

        private void handleDispatchDestroyPrinterDiscoverySession(List<RemotePrintService> services) {
            int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                ((RemotePrintService) services.get(i)).destroyPrinterDiscoverySession();
            }
            onDestroyed();
        }

        private void handleDispatchStartPrinterDiscovery(List<RemotePrintService> services, List<PrinterId> printerIds) {
            int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                ((RemotePrintService) services.get(i)).startPrinterDiscovery(printerIds);
            }
        }

        private void handleDispatchStopPrinterDiscovery(List<RemotePrintService> services) {
            int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                ((RemotePrintService) services.get(i)).stopPrinterDiscovery();
            }
        }

        private void handleValidatePrinters(RemotePrintService service, List<PrinterId> printerIds) {
            service.validatePrinters(printerIds);
        }

        private void handleStartPrinterStateTracking(RemotePrintService service, PrinterId printerId) {
            service.startPrinterStateTracking(printerId);
        }

        private void handleStopPrinterStateTracking(RemotePrintService service, PrinterId printerId) {
            service.stopPrinterStateTracking(printerId);
        }

        private void handlePrintersAdded(IPrinterDiscoveryObserver observer, List<PrinterInfo> printers) {
            try {
                observer.onPrintersAdded(new ParceledListSlice(printers));
            } catch (RemoteException re) {
                Log.e(UserState.LOG_TAG, "Error sending added printers", re);
            }
        }

        private void handlePrintersRemoved(IPrinterDiscoveryObserver observer, List<PrinterId> printerIds) {
            try {
                observer.onPrintersRemoved(new ParceledListSlice(printerIds));
            } catch (RemoteException re) {
                Log.e(UserState.LOG_TAG, "Error sending removed printers", re);
            }
        }
    }

    private abstract class PrintJobStateChangeListenerRecord implements DeathRecipient {
        final int appId;
        final IPrintJobStateChangeListener listener;

        public abstract void onBinderDied();

        public PrintJobStateChangeListenerRecord(IPrintJobStateChangeListener listener, int appId) throws RemoteException {
            this.listener = listener;
            this.appId = appId;
            listener.asBinder().linkToDeath(this, 0);
        }

        public void binderDied() {
            this.listener.asBinder().unlinkToDeath(this, 0);
            onBinderDied();
        }
    }

    private abstract class ListenerRecord<T extends IInterface> implements DeathRecipient {
        final T listener;

        public abstract void onBinderDied();

        public ListenerRecord(T listener) throws RemoteException {
            this.listener = listener;
            listener.asBinder().linkToDeath(this, 0);
        }

        public void binderDied() {
            this.listener.asBinder().unlinkToDeath(this, 0);
            onBinderDied();
        }
    }

    private final class PrintJobForAppCache {
        private final SparseArray<List<PrintJobInfo>> mPrintJobsForRunningApp;

        /* synthetic */ PrintJobForAppCache(UserState this$0, PrintJobForAppCache -this1) {
            this();
        }

        private PrintJobForAppCache() {
            this.mPrintJobsForRunningApp = new SparseArray();
        }

        public boolean onPrintJobCreated(final IBinder creator, final int appId, PrintJobInfo printJob) {
            try {
                creator.linkToDeath(new DeathRecipient() {
                    public void binderDied() {
                        creator.unlinkToDeath(this, 0);
                        synchronized (UserState.this.mLock) {
                            PrintJobForAppCache.this.mPrintJobsForRunningApp.remove(appId);
                        }
                    }
                }, 0);
                synchronized (UserState.this.mLock) {
                    List<PrintJobInfo> printJobsForApp = (List) this.mPrintJobsForRunningApp.get(appId);
                    if (printJobsForApp == null) {
                        printJobsForApp = new ArrayList();
                        this.mPrintJobsForRunningApp.put(appId, printJobsForApp);
                    }
                    printJobsForApp.add(printJob);
                }
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }

        public void onPrintJobStateChanged(PrintJobInfo printJob) {
            synchronized (UserState.this.mLock) {
                List<PrintJobInfo> printJobsForApp = (List) this.mPrintJobsForRunningApp.get(printJob.getAppId());
                if (printJobsForApp == null) {
                    return;
                }
                int printJobCount = printJobsForApp.size();
                for (int i = 0; i < printJobCount; i++) {
                    if (((PrintJobInfo) printJobsForApp.get(i)).getId().equals(printJob.getId())) {
                        printJobsForApp.set(i, printJob);
                    }
                }
            }
        }

        public PrintJobInfo getPrintJob(PrintJobId printJobId, int appId) {
            synchronized (UserState.this.mLock) {
                List<PrintJobInfo> printJobsForApp = (List) this.mPrintJobsForRunningApp.get(appId);
                if (printJobsForApp == null) {
                    return null;
                }
                int printJobCount = printJobsForApp.size();
                for (int i = 0; i < printJobCount; i++) {
                    PrintJobInfo printJob = (PrintJobInfo) printJobsForApp.get(i);
                    if (printJob.getId().equals(printJobId)) {
                        return printJob;
                    }
                }
                return null;
            }
        }

        public List<PrintJobInfo> getPrintJobs(int appId) {
            Throwable th;
            synchronized (UserState.this.mLock) {
                List<PrintJobInfo> printJobs = null;
                List<PrintJobInfo> printJobs2;
                List<PrintJobInfo> bucket;
                if (appId == -2) {
                    try {
                        int bucketCount = this.mPrintJobsForRunningApp.size();
                        int i = 0;
                        printJobs2 = null;
                        while (i < bucketCount) {
                            try {
                                bucket = (List) this.mPrintJobsForRunningApp.valueAt(i);
                                if (printJobs2 == null) {
                                    printJobs = new ArrayList();
                                } else {
                                    printJobs = printJobs2;
                                }
                                printJobs.addAll(bucket);
                                i++;
                                printJobs2 = printJobs;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        printJobs = printJobs2;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } else {
                    bucket = (List) this.mPrintJobsForRunningApp.get(appId);
                    if (bucket != null) {
                        printJobs2 = new ArrayList();
                        printJobs2.addAll(bucket);
                        printJobs = printJobs2;
                    }
                }
                if (printJobs != null) {
                    return printJobs;
                }
                List<PrintJobInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
        }

        public void dump(PrintWriter pw, String prefix) {
            synchronized (UserState.this.mLock) {
                String tab = "  ";
                int bucketCount = this.mPrintJobsForRunningApp.size();
                for (int i = 0; i < bucketCount; i++) {
                    pw.append(prefix).append("appId=" + this.mPrintJobsForRunningApp.keyAt(i)).append(UserState.COMPONENT_NAME_SEPARATOR).println();
                    List<PrintJobInfo> bucket = (List) this.mPrintJobsForRunningApp.valueAt(i);
                    int printJobCount = bucket.size();
                    for (int j = 0; j < printJobCount; j++) {
                        pw.append(prefix).append(tab).append(((PrintJobInfo) bucket.get(j)).toString()).println();
                    }
                }
            }
        }
    }

    private final class UserStateHandler extends Handler {
        public static final int MSG_CHECK_CONFIG_CHANGED = 4;
        public static final int MSG_DISPATCH_PRINT_JOB_STATE_CHANGED = 1;
        public static final int MSG_DISPATCH_PRINT_SERVICES_CHANGED = 2;
        public static final int MSG_DISPATCH_PRINT_SERVICES_RECOMMENDATIONS_UPDATED = 3;

        public UserStateHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    UserState.this.handleDispatchPrintJobStateChanged(message.obj, message.arg1);
                    return;
                case 2:
                    UserState.this.handleDispatchPrintServicesChanged();
                    return;
                case 3:
                    UserState.this.handleDispatchPrintServiceRecommendationsUpdated((List) message.obj);
                    return;
                case 4:
                    synchronized (UserState.this.mLock) {
                        UserState.this.onConfigurationChangedLocked();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public UserState(Context context, int userId, Object lock, boolean lowPriority) {
        this.mContext = context;
        this.mUserId = userId;
        this.mLock = lock;
        this.mSpooler = new RemotePrintSpooler(context, userId, lowPriority, this);
        this.mHandler = new UserStateHandler(context.getMainLooper());
        synchronized (this.mLock) {
            readInstalledPrintServicesLocked();
            upgradePersistentStateIfNeeded();
            readDisabledPrintServicesLocked();
        }
        prunePrintServices();
        synchronized (this.mLock) {
            onConfigurationChangedLocked();
        }
    }

    public void increasePriority() {
        this.mSpooler.increasePriority();
    }

    public void onPrintJobQueued(PrintJobInfo printJob) {
        RemotePrintService service;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            service = (RemotePrintService) this.mActiveServices.get(printJob.getPrinterId().getServiceName());
        }
        if (service != null) {
            service.onPrintJobQueued(printJob);
        } else {
            this.mSpooler.setPrintJobState(printJob.getId(), 6, this.mContext.getString(17040854));
        }
    }

    public void onAllPrintJobsForServiceHandled(ComponentName printService) {
        RemotePrintService service;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            service = (RemotePrintService) this.mActiveServices.get(printService);
        }
        if (service != null) {
            service.onAllPrintJobsHandled();
        }
    }

    public void removeObsoletePrintJobs() {
        this.mSpooler.removeObsoletePrintJobs();
    }

    public Bundle print(String printJobName, IPrintDocumentAdapter adapter, PrintAttributes attributes, String packageName, int appId) {
        final PrintJobInfo printJob = new PrintJobInfo();
        printJob.setId(new PrintJobId());
        printJob.setAppId(appId);
        printJob.setLabel(printJobName);
        printJob.setAttributes(attributes);
        printJob.setState(1);
        printJob.setCopies(1);
        printJob.setCreationTime(System.currentTimeMillis());
        if (!this.mPrintJobForAppCache.onPrintJobCreated(adapter.asBinder(), appId, printJob)) {
            return null;
        }
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                UserState.this.mSpooler.createPrintJob(printJob);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        long identity = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("android.print.PRINT_DIALOG");
            intent.setData(Uri.fromParts("printjob", printJob.getId().flattenToString(), null));
            intent.putExtra("android.print.intent.extra.EXTRA_PRINT_DOCUMENT_ADAPTER", adapter.asBinder());
            intent.putExtra("android.print.intent.extra.EXTRA_PRINT_JOB", printJob);
            intent.putExtra("android.content.extra.PACKAGE_NAME", packageName);
            IntentSender intentSender = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, new UserHandle(this.mUserId)).getIntentSender();
            Bundle result = new Bundle();
            result.putParcelable("android.print.intent.extra.EXTRA_PRINT_JOB", printJob);
            result.putParcelable("android.print.intent.extra.EXTRA_PRINT_DIALOG_INTENT", intentSender);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<PrintJobInfo> getPrintJobInfos(int appId) {
        int i;
        List<PrintJobInfo> cachedPrintJobs = this.mPrintJobForAppCache.getPrintJobs(appId);
        ArrayMap<PrintJobId, PrintJobInfo> result = new ArrayMap();
        int cachedPrintJobCount = cachedPrintJobs.size();
        for (i = 0; i < cachedPrintJobCount; i++) {
            PrintJobInfo cachedPrintJob = (PrintJobInfo) cachedPrintJobs.get(i);
            result.put(cachedPrintJob.getId(), cachedPrintJob);
            cachedPrintJob.setTag(null);
            cachedPrintJob.setAdvancedOptions(null);
        }
        List<PrintJobInfo> printJobs = this.mSpooler.getPrintJobInfos(null, -1, appId);
        if (printJobs != null) {
            int printJobCount = printJobs.size();
            for (i = 0; i < printJobCount; i++) {
                PrintJobInfo printJob = (PrintJobInfo) printJobs.get(i);
                result.put(printJob.getId(), printJob);
                printJob.setTag(null);
                printJob.setAdvancedOptions(null);
            }
        }
        return new ArrayList(result.values());
    }

    public PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId) {
        PrintJobInfo printJob = this.mPrintJobForAppCache.getPrintJob(printJobId, appId);
        if (printJob == null) {
            printJob = this.mSpooler.getPrintJobInfo(printJobId, appId);
        }
        if (printJob != null) {
            printJob.setTag(null);
            printJob.setAdvancedOptions(null);
        }
        return printJob;
    }

    public Icon getCustomPrinterIcon(PrinterId printerId) {
        Icon icon = this.mSpooler.getCustomPrinterIcon(printerId);
        if (icon == null) {
            RemotePrintService service = (RemotePrintService) this.mActiveServices.get(printerId.getServiceName());
            if (service != null) {
                service.requestCustomPrinterIcon(printerId);
            }
        }
        return icon;
    }

    public void cancelPrintJob(PrintJobId printJobId, int appId) {
        PrintJobInfo printJobInfo = this.mSpooler.getPrintJobInfo(printJobId, appId);
        if (printJobInfo != null) {
            this.mSpooler.setPrintJobCancelling(printJobId, true);
            if (printJobInfo.getState() != 6) {
                PrinterId printerId = printJobInfo.getPrinterId();
                if (printerId != null) {
                    RemotePrintService printService;
                    ComponentName printServiceName = printerId.getServiceName();
                    synchronized (this.mLock) {
                        printService = (RemotePrintService) this.mActiveServices.get(printServiceName);
                    }
                    if (printService != null) {
                        printService.onRequestCancelPrintJob(printJobInfo);
                    } else {
                        return;
                    }
                }
            }
            this.mSpooler.setPrintJobState(printJobId, 7, null);
        }
    }

    public void restartPrintJob(PrintJobId printJobId, int appId) {
        PrintJobInfo printJobInfo = getPrintJobInfo(printJobId, appId);
        if (printJobInfo != null && printJobInfo.getState() == 6) {
            this.mSpooler.setPrintJobState(printJobId, 2, null);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004b A:{Catch:{ all -> 0x0059 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<PrintServiceInfo> getPrintServices(int selectionFlags) {
        Throwable th;
        synchronized (this.mLock) {
            try {
                int installedServiceCount = this.mInstalledServices.size();
                int i = 0;
                List<PrintServiceInfo> selectedServices = null;
                while (i < installedServiceCount) {
                    List<PrintServiceInfo> selectedServices2;
                    try {
                        PrintServiceInfo installedService = (PrintServiceInfo) this.mInstalledServices.get(i);
                        installedService.setIsEnabled(this.mActiveServices.containsKey(new ComponentName(installedService.getResolveInfo().serviceInfo.packageName, installedService.getResolveInfo().serviceInfo.name)));
                        if (installedService.isEnabled()) {
                            if ((selectionFlags & 1) == 0) {
                                selectedServices2 = selectedServices;
                            }
                            if (selectedServices != null) {
                                selectedServices2 = new ArrayList();
                            } else {
                                selectedServices2 = selectedServices;
                            }
                            selectedServices2.add(installedService);
                        } else {
                            if ((selectionFlags & 2) == 0) {
                                selectedServices2 = selectedServices;
                            }
                            if (selectedServices != null) {
                            }
                            selectedServices2.add(installedService);
                        }
                        i++;
                        selectedServices = selectedServices2;
                    } catch (Throwable th2) {
                        th = th2;
                        selectedServices2 = selectedServices;
                        throw th;
                    }
                }
                return selectedServices;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void setPrintServiceEnabled(ComponentName serviceName, boolean isEnabled) {
        synchronized (this.mLock) {
            boolean isChanged = false;
            if (isEnabled) {
                isChanged = this.mDisabledServices.remove(serviceName);
            } else {
                int numServices = this.mInstalledServices.size();
                for (int i = 0; i < numServices; i++) {
                    if (((PrintServiceInfo) this.mInstalledServices.get(i)).getComponentName().equals(serviceName)) {
                        this.mDisabledServices.add(serviceName);
                        isChanged = true;
                        break;
                    }
                }
            }
            if (isChanged) {
                writeDisabledPrintServicesLocked(this.mDisabledServices);
                MetricsLogger.action(this.mContext, 511, isEnabled ? 0 : 1);
                onConfigurationChangedLocked();
            }
        }
    }

    public List<RecommendationInfo> getPrintServiceRecommendations() {
        return this.mPrintServiceRecommendations;
    }

    public void createPrinterDiscoverySession(IPrinterDiscoveryObserver observer) {
        this.mSpooler.clearCustomPrinterIconCache();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrinterDiscoverySession == null) {
                this.mPrinterDiscoverySession = new PrinterDiscoverySessionMediator(this, this.mContext) {
                    public void onDestroyed() {
                        this.mPrinterDiscoverySession = null;
                    }
                };
                this.mPrinterDiscoverySession.addObserverLocked(observer);
            } else {
                this.mPrinterDiscoverySession.addObserverLocked(observer);
            }
        }
    }

    public void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver observer) {
        synchronized (this.mLock) {
            if (this.mPrinterDiscoverySession == null) {
                return;
            }
            this.mPrinterDiscoverySession.removeObserverLocked(observer);
        }
    }

    public void startPrinterDiscovery(IPrinterDiscoveryObserver observer, List<PrinterId> printerIds) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrinterDiscoverySession == null) {
                return;
            }
            this.mPrinterDiscoverySession.startPrinterDiscoveryLocked(observer, printerIds);
        }
    }

    public void stopPrinterDiscovery(IPrinterDiscoveryObserver observer) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrinterDiscoverySession == null) {
                return;
            }
            this.mPrinterDiscoverySession.stopPrinterDiscoveryLocked(observer);
        }
    }

    public void validatePrinters(List<PrinterId> printerIds) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
            } else if (this.mPrinterDiscoverySession == null) {
            } else {
                this.mPrinterDiscoverySession.validatePrintersLocked(printerIds);
            }
        }
    }

    public void startPrinterStateTracking(PrinterId printerId) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
            } else if (this.mPrinterDiscoverySession == null) {
            } else {
                this.mPrinterDiscoverySession.startPrinterStateTrackingLocked(printerId);
            }
        }
    }

    public void stopPrinterStateTracking(PrinterId printerId) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
            } else if (this.mPrinterDiscoverySession == null) {
            } else {
                this.mPrinterDiscoverySession.stopPrinterStateTrackingLocked(printerId);
            }
        }
    }

    public void addPrintJobStateChangeListener(IPrintJobStateChangeListener listener, int appId) throws RemoteException {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintJobStateChangeListenerRecords == null) {
                this.mPrintJobStateChangeListenerRecords = new ArrayList();
            }
            this.mPrintJobStateChangeListenerRecords.add(new PrintJobStateChangeListenerRecord(this, listener, appId) {
                public void onBinderDied() {
                    synchronized (this.mLock) {
                        if (this.mPrintJobStateChangeListenerRecords != null) {
                            this.mPrintJobStateChangeListenerRecords.remove(this);
                        }
                    }
                }
            });
        }
    }

    /* JADX WARNING: Missing block: B:17:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removePrintJobStateChangeListener(IPrintJobStateChangeListener listener) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintJobStateChangeListenerRecords == null) {
                return;
            }
            int recordCount = this.mPrintJobStateChangeListenerRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (((PrintJobStateChangeListenerRecord) this.mPrintJobStateChangeListenerRecords.get(i)).listener.asBinder().equals(listener.asBinder())) {
                    this.mPrintJobStateChangeListenerRecords.remove(i);
                    break;
                }
            }
            if (this.mPrintJobStateChangeListenerRecords.isEmpty()) {
                this.mPrintJobStateChangeListenerRecords = null;
            }
        }
    }

    public void addPrintServicesChangeListener(IPrintServicesChangeListener listener) throws RemoteException {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintServicesChangeListenerRecords == null) {
                this.mPrintServicesChangeListenerRecords = new ArrayList();
            }
            this.mPrintServicesChangeListenerRecords.add(new ListenerRecord<IPrintServicesChangeListener>(this, listener) {
                public void onBinderDied() {
                    synchronized (this.mLock) {
                        if (this.mPrintServicesChangeListenerRecords != null) {
                            this.mPrintServicesChangeListenerRecords.remove(this);
                        }
                    }
                }
            });
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0040, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removePrintServicesChangeListener(IPrintServicesChangeListener listener) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintServicesChangeListenerRecords == null) {
                return;
            }
            int recordCount = this.mPrintServicesChangeListenerRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (((IPrintServicesChangeListener) ((ListenerRecord) this.mPrintServicesChangeListenerRecords.get(i)).listener).asBinder().equals(listener.asBinder())) {
                    this.mPrintServicesChangeListenerRecords.remove(i);
                    break;
                }
            }
            if (this.mPrintServicesChangeListenerRecords.isEmpty()) {
                this.mPrintServicesChangeListenerRecords = null;
            }
        }
    }

    public void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener) throws RemoteException {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintServiceRecommendationsChangeListenerRecords == null) {
                this.mPrintServiceRecommendationsChangeListenerRecords = new ArrayList();
                this.mPrintServiceRecommendationsService = new RemotePrintServiceRecommendationService(this.mContext, UserHandle.getUserHandleForUid(this.mUserId), this);
            }
            this.mPrintServiceRecommendationsChangeListenerRecords.add(new ListenerRecord<IRecommendationsChangeListener>(this, listener) {
                public void onBinderDied() {
                    synchronized (this.mLock) {
                        if (this.mPrintServiceRecommendationsChangeListenerRecords != null) {
                            this.mPrintServiceRecommendationsChangeListenerRecords.remove(this);
                        }
                    }
                }
            });
        }
    }

    /* JADX WARNING: Missing block: B:17:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrintServiceRecommendationsChangeListenerRecords == null) {
                return;
            }
            int recordCount = this.mPrintServiceRecommendationsChangeListenerRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (((IRecommendationsChangeListener) ((ListenerRecord) this.mPrintServiceRecommendationsChangeListenerRecords.get(i)).listener).asBinder().equals(listener.asBinder())) {
                    this.mPrintServiceRecommendationsChangeListenerRecords.remove(i);
                    break;
                }
            }
            if (this.mPrintServiceRecommendationsChangeListenerRecords.isEmpty()) {
                this.mPrintServiceRecommendationsChangeListenerRecords = null;
                this.mPrintServiceRecommendations = null;
                this.mPrintServiceRecommendationsService.close();
                this.mPrintServiceRecommendationsService = null;
            }
        }
    }

    public void onPrintJobStateChanged(PrintJobInfo printJob) {
        this.mPrintJobForAppCache.onPrintJobStateChanged(printJob);
        this.mHandler.obtainMessage(1, printJob.getAppId(), 0, printJob.getId()).sendToTarget();
    }

    public void onPrintServicesChanged() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    public void onPrintServiceRecommendationsUpdated(List<RecommendationInfo> recommendations) {
        this.mHandler.obtainMessage(3, 0, 0, recommendations).sendToTarget();
    }

    public void onPrintersAdded(List<PrinterInfo> printers) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
            } else if (this.mPrinterDiscoverySession == null) {
            } else {
                this.mPrinterDiscoverySession.onPrintersAddedLocked(printers);
            }
        }
    }

    public void onPrintersRemoved(List<PrinterId> printerIds) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
            } else if (this.mPrinterDiscoverySession == null) {
            } else {
                this.mPrinterDiscoverySession.onPrintersRemovedLocked(printerIds);
            }
        }
    }

    public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) {
        this.mSpooler.onCustomPrinterIconLoaded(printerId, icon);
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mPrinterDiscoverySession == null) {
                return;
            }
            this.mPrinterDiscoverySession.onCustomPrinterIconLoadedLocked(printerId);
        }
    }

    public void onServiceDied(RemotePrintService service) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mActiveServices.isEmpty()) {
                return;
            }
            failActivePrintJobsForService(service.getComponentName());
            service.onAllPrintJobsHandled();
            this.mActiveServices.remove(service.getComponentName());
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 500);
            if (this.mPrinterDiscoverySession == null) {
                return;
            }
            this.mPrinterDiscoverySession.onServiceDiedLocked(service);
        }
    }

    public void updateIfNeededLocked() {
        throwIfDestroyedLocked();
        readConfigurationLocked();
        onConfigurationChangedLocked();
    }

    public void destroyLocked() {
        throwIfDestroyedLocked();
        this.mSpooler.destroy();
        for (RemotePrintService service : this.mActiveServices.values()) {
            service.destroy();
        }
        this.mActiveServices.clear();
        this.mInstalledServices.clear();
        this.mDisabledServices.clear();
        if (this.mPrinterDiscoverySession != null) {
            this.mPrinterDiscoverySession.destroyLocked();
            this.mPrinterDiscoverySession = null;
        }
        this.mDestroyed = true;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        int i;
        pw.append(prefix).append("user state ").append(String.valueOf(this.mUserId)).append(":");
        pw.println();
        String tab = "  ";
        pw.append(prefix).append(tab).append("installed services:").println();
        int installedServiceCount = this.mInstalledServices.size();
        for (i = 0; i < installedServiceCount; i++) {
            PrintServiceInfo installedService = (PrintServiceInfo) this.mInstalledServices.get(i);
            String installedServicePrefix = prefix + tab + tab;
            pw.append(installedServicePrefix).append("service:").println();
            ResolveInfo resolveInfo = installedService.getResolveInfo();
            pw.append(installedServicePrefix).append(tab).append("componentName=").append(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name).flattenToString()).println();
            pw.append(installedServicePrefix).append(tab).append("settingsActivity=").append(installedService.getSettingsActivityName()).println();
            pw.append(installedServicePrefix).append(tab).append("addPrintersActivity=").append(installedService.getAddPrintersActivityName()).println();
            pw.append(installedServicePrefix).append(tab).append("avancedOptionsActivity=").append(installedService.getAdvancedOptionsActivityName()).println();
        }
        pw.append(prefix).append(tab).append("disabled services:").println();
        for (ComponentName disabledService : this.mDisabledServices) {
            String disabledServicePrefix = prefix + tab + tab;
            pw.append(disabledServicePrefix).append("service:").println();
            pw.append(disabledServicePrefix).append(tab).append("componentName=").append(disabledService.flattenToString());
            pw.println();
        }
        pw.append(prefix).append(tab).append("active services:").println();
        int activeServiceCount = this.mActiveServices.size();
        for (i = 0; i < activeServiceCount; i++) {
            ((RemotePrintService) this.mActiveServices.valueAt(i)).dump(pw, prefix + tab + tab);
            pw.println();
        }
        pw.append(prefix).append(tab).append("cached print jobs:").println();
        this.mPrintJobForAppCache.dump(pw, prefix + tab + tab);
        pw.append(prefix).append(tab).append("discovery mediator:").println();
        if (this.mPrinterDiscoverySession != null) {
            this.mPrinterDiscoverySession.dump(pw, prefix + tab + tab);
        }
        pw.append(prefix).append(tab).append("print spooler:").println();
        this.mSpooler.dump(fd, pw, prefix + tab + tab);
        pw.println();
    }

    private void readConfigurationLocked() {
        readInstalledPrintServicesLocked();
        readDisabledPrintServicesLocked();
    }

    private void readInstalledPrintServicesLocked() {
        Set<PrintServiceInfo> tempPrintServices = new HashSet();
        List<ResolveInfo> installedServices = this.mContext.getPackageManager().queryIntentServicesAsUser(this.mQueryIntent, 268435588, this.mUserId);
        int installedCount = installedServices.size();
        int count = installedCount;
        for (int i = 0; i < installedCount; i++) {
            ResolveInfo installedService = (ResolveInfo) installedServices.get(i);
            if ("android.permission.BIND_PRINT_SERVICE".equals(installedService.serviceInfo.permission)) {
                tempPrintServices.add(PrintServiceInfo.create(this.mContext, installedService));
            } else {
                Slog.w(LOG_TAG, "Skipping print service " + new ComponentName(installedService.serviceInfo.packageName, installedService.serviceInfo.name).flattenToShortString() + " since it does not require permission " + "android.permission.BIND_PRINT_SERVICE");
            }
        }
        this.mInstalledServices.clear();
        this.mInstalledServices.addAll(tempPrintServices);
    }

    private void upgradePersistentStateIfNeeded() {
        if (Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_print_services", this.mUserId) != null) {
            Set<ComponentName> enabledServiceNameSet = new HashSet();
            readPrintServicesFromSettingLocked("enabled_print_services", enabledServiceNameSet);
            ArraySet<ComponentName> disabledServices = new ArraySet();
            int numInstalledServices = this.mInstalledServices.size();
            for (int i = 0; i < numInstalledServices; i++) {
                ComponentName serviceName = ((PrintServiceInfo) this.mInstalledServices.get(i)).getComponentName();
                if (!enabledServiceNameSet.contains(serviceName)) {
                    disabledServices.add(serviceName);
                }
            }
            writeDisabledPrintServicesLocked(disabledServices);
            Secure.putStringForUser(this.mContext.getContentResolver(), "enabled_print_services", null, this.mUserId);
        }
    }

    private void readDisabledPrintServicesLocked() {
        Set<ComponentName> tempDisabledServiceNameSet = new HashSet();
        readPrintServicesFromSettingLocked("disabled_print_services", tempDisabledServiceNameSet);
        if (!tempDisabledServiceNameSet.equals(this.mDisabledServices)) {
            this.mDisabledServices.clear();
            this.mDisabledServices.addAll(tempDisabledServiceNameSet);
        }
    }

    private void readPrintServicesFromSettingLocked(String setting, Set<ComponentName> outServiceNames) {
        String settingValue = Secure.getStringForUser(this.mContext.getContentResolver(), setting, this.mUserId);
        if (!TextUtils.isEmpty(settingValue)) {
            SimpleStringSplitter splitter = this.mStringColonSplitter;
            splitter.setString(settingValue);
            while (splitter.hasNext()) {
                String string = splitter.next();
                if (!TextUtils.isEmpty(string)) {
                    ComponentName componentName = ComponentName.unflattenFromString(string);
                    if (componentName != null) {
                        outServiceNames.add(componentName);
                    }
                }
            }
        }
    }

    private void writeDisabledPrintServicesLocked(Set<ComponentName> disabledServices) {
        StringBuilder builder = new StringBuilder();
        for (ComponentName componentName : disabledServices) {
            if (builder.length() > 0) {
                builder.append(COMPONENT_NAME_SEPARATOR);
            }
            builder.append(componentName.flattenToShortString());
        }
        Secure.putStringForUser(this.mContext.getContentResolver(), "disabled_print_services", builder.toString(), this.mUserId);
    }

    private ArrayList<ComponentName> getInstalledComponents() {
        ArrayList<ComponentName> installedComponents = new ArrayList();
        int installedCount = this.mInstalledServices.size();
        for (int i = 0; i < installedCount; i++) {
            ResolveInfo resolveInfo = ((PrintServiceInfo) this.mInstalledServices.get(i)).getResolveInfo();
            installedComponents.add(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
        }
        return installedComponents;
    }

    public void prunePrintServices() {
        ArrayList<ComponentName> installedComponents;
        this.mSpooler.getRemoteInstanceLazyFirstly();
        synchronized (this.mLock) {
            installedComponents = getInstalledComponents();
            if (this.mDisabledServices.retainAll(installedComponents)) {
                writeDisabledPrintServicesLocked(this.mDisabledServices);
            }
            if (Secure.getStringForUser(this.mContext.getContentResolver(), "disabled_print_services", this.mUserId) == null && isChineseVersion() && (installedComponents.isEmpty() ^ 1) != 0) {
                ComponentName name = (ComponentName) installedComponents.get(0);
                Log.i(LOG_TAG, "name :" + name);
                if ("com.android.bips.BuiltInPrintService".equals(name.getClassName())) {
                    this.mDisabledServices.add(name);
                    writeDisabledPrintServicesLocked(this.mDisabledServices);
                }
            }
        }
        this.mSpooler.pruneApprovedPrintServices(installedComponents);
    }

    private boolean isChineseVersion() {
        if ("zh".equals(SystemProperties.get("ro.product.locale.language"))) {
            return "CN".equals(SystemProperties.get("ro.product.locale.region"));
        }
        return false;
    }

    private void onConfigurationChangedLocked() {
        RemotePrintService service;
        ArrayList<ComponentName> installedComponents = getInstalledComponents();
        int installedCount = installedComponents.size();
        for (int i = 0; i < installedCount; i++) {
            ComponentName serviceName = (ComponentName) installedComponents.get(i);
            if (this.mDisabledServices.contains(serviceName)) {
                service = (RemotePrintService) this.mActiveServices.remove(serviceName);
                if (service != null) {
                    removeServiceLocked(service);
                }
            } else if (!this.mActiveServices.containsKey(serviceName)) {
                addServiceLocked(new RemotePrintService(this.mContext, serviceName, this.mUserId, this.mSpooler, this));
            }
        }
        Iterator<Entry<ComponentName, RemotePrintService>> iterator = this.mActiveServices.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<ComponentName, RemotePrintService> entry = (Entry) iterator.next();
            service = (RemotePrintService) entry.getValue();
            if (!installedComponents.contains((ComponentName) entry.getKey())) {
                removeServiceLocked(service);
                iterator.remove();
            }
        }
        onPrintServicesChanged();
    }

    private void addServiceLocked(RemotePrintService service) {
        this.mActiveServices.put(service.getComponentName(), service);
        if (this.mPrinterDiscoverySession != null) {
            this.mPrinterDiscoverySession.onServiceAddedLocked(service);
        }
    }

    private void removeServiceLocked(RemotePrintService service) {
        failActivePrintJobsForService(service.getComponentName());
        if (this.mPrinterDiscoverySession != null) {
            this.mPrinterDiscoverySession.onServiceRemovedLocked(service);
        } else {
            service.destroy();
        }
    }

    private void failActivePrintJobsForService(final ComponentName serviceName) {
        if (Looper.getMainLooper().isCurrentThread()) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    UserState.this.failScheduledPrintJobsForServiceInternal(serviceName);
                }
            });
        } else {
            failScheduledPrintJobsForServiceInternal(serviceName);
        }
    }

    private void failScheduledPrintJobsForServiceInternal(ComponentName serviceName) {
        List<PrintJobInfo> printJobs = this.mSpooler.getPrintJobInfos(serviceName, -4, -2);
        if (printJobs != null) {
            long identity = Binder.clearCallingIdentity();
            try {
                int printJobCount = printJobs.size();
                for (int i = 0; i < printJobCount; i++) {
                    this.mSpooler.setPrintJobState(((PrintJobInfo) printJobs.get(i)).getId(), 6, this.mContext.getString(17040854));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void throwIfDestroyedLocked() {
        if (this.mDestroyed) {
            throw new IllegalStateException("Cannot interact with a destroyed instance.");
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0011, code:
            r3 = r4.size();
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0016, code:
            if (r0 >= r3) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:12:0x0018, code:
            r2 = (com.android.server.print.UserState.PrintJobStateChangeListenerRecord) r4.get(r0);
     */
    /* JADX WARNING: Missing block: B:13:0x0021, code:
            if (r2.appId == -2) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:15:0x0025, code:
            if (r2.appId != r9) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r2.listener.onPrintJobStateChanged(r8);
     */
    /* JADX WARNING: Missing block: B:22:0x0032, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:23:0x0033, code:
            android.util.Log.e(LOG_TAG, "Error notifying for print job state change", r1);
     */
    /* JADX WARNING: Missing block: B:24:0x003d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDispatchPrintJobStateChanged(PrintJobId printJobId, int appId) {
        synchronized (this.mLock) {
            if (this.mPrintJobStateChangeListenerRecords == null) {
                return;
            }
            List<PrintJobStateChangeListenerRecord> records = new ArrayList(this.mPrintJobStateChangeListenerRecords);
        }
        int i++;
    }

    /* JADX WARNING: Missing block: B:10:0x0011, code:
            r3 = r4.size();
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0016, code:
            if (r0 >= r3) goto L_0x0036;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            ((android.print.IPrintServicesChangeListener) ((com.android.server.print.UserState.ListenerRecord) r4.get(r0)).listener).onPrintServicesChanged();
     */
    /* JADX WARNING: Missing block: B:19:0x002b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x002c, code:
            android.util.Log.e(LOG_TAG, "Error notifying for print services change", r1);
     */
    /* JADX WARNING: Missing block: B:21:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDispatchPrintServicesChanged() {
        synchronized (this.mLock) {
            if (this.mPrintServicesChangeListenerRecords == null) {
                return;
            }
            List<ListenerRecord<IPrintServicesChangeListener>> records = new ArrayList(this.mPrintServicesChangeListenerRecords);
        }
        int i++;
    }

    /* JADX WARNING: Missing block: B:10:0x0013, code:
            r3 = r4.size();
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0018, code:
            if (r0 >= r3) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            ((android.printservice.recommendation.IRecommendationsChangeListener) ((com.android.server.print.UserState.ListenerRecord) r4.get(r0)).listener).onRecommendationsChanged();
     */
    /* JADX WARNING: Missing block: B:19:0x002d, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x002e, code:
            android.util.Log.e(LOG_TAG, "Error notifying for print service recommendations change", r1);
     */
    /* JADX WARNING: Missing block: B:21:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDispatchPrintServiceRecommendationsUpdated(List<RecommendationInfo> recommendations) {
        synchronized (this.mLock) {
            if (this.mPrintServiceRecommendationsChangeListenerRecords == null) {
                return;
            } else {
                List<ListenerRecord<IRecommendationsChangeListener>> records = new ArrayList(this.mPrintServiceRecommendationsChangeListenerRecords);
                this.mPrintServiceRecommendations = recommendations;
            }
        }
        int i++;
    }
}
