package com.android.server.accessibility;

import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.view.InputEventConsistencyVerifier;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KeyEventDispatcher {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "KeyEventDispatcher";
    private static final int MAX_POOL_SIZE = 10;
    private static final int MSG_ON_KEY_EVENT_TIMEOUT = 1;
    private static final long ON_KEY_EVENT_TIMEOUT_MILLIS = 500;
    private final Handler mHandlerToSendKeyEventsToInputFilter;
    private final Handler mKeyEventTimeoutHandler;
    private final Object mLock;
    private final int mMessageTypeForSendKeyEvent;
    private final Pool<PendingKeyEvent> mPendingEventPool;
    private final Map<Service, ArrayList<PendingKeyEvent>> mPendingEventsMap;
    private final PowerManager mPowerManager;
    private final InputEventConsistencyVerifier mSentEventsVerifier;

    private class Callback implements android.os.Handler.Callback {
        private Callback() {
        }

        public boolean handleMessage(Message message) {
            if (message.what != KeyEventDispatcher.MSG_ON_KEY_EVENT_TIMEOUT) {
                throw new IllegalArgumentException("Unknown message: " + message.what);
            }
            PendingKeyEvent pendingKeyEvent = message.obj;
            synchronized (KeyEventDispatcher.this.mLock) {
                Iterator listForService$iterator = KeyEventDispatcher.this.mPendingEventsMap.values().iterator();
                while (listForService$iterator.hasNext() && (!((ArrayList) listForService$iterator.next()).remove(pendingKeyEvent) || !KeyEventDispatcher.this.removeReferenceToPendingEventLocked(pendingKeyEvent))) {
                }
            }
            return true;
        }
    }

    private static final class PendingKeyEvent {
        KeyEvent event;
        boolean handled;
        int policyFlags;
        int referenceCount;

        private PendingKeyEvent() {
        }
    }

    public KeyEventDispatcher(Handler handlerToSendKeyEventsToInputFilter, int messageTypeForSendKeyEvent, Object lock, PowerManager powerManager) {
        this.mPendingEventPool = new SimplePool(MAX_POOL_SIZE);
        this.mPendingEventsMap = new ArrayMap();
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            this.mSentEventsVerifier = new InputEventConsistencyVerifier(this, 0, KeyEventDispatcher.class.getSimpleName());
        } else {
            this.mSentEventsVerifier = null;
        }
        this.mHandlerToSendKeyEventsToInputFilter = handlerToSendKeyEventsToInputFilter;
        this.mMessageTypeForSendKeyEvent = messageTypeForSendKeyEvent;
        this.mKeyEventTimeoutHandler = new Handler(this.mHandlerToSendKeyEventsToInputFilter.getLooper(), new Callback());
        this.mLock = lock;
        this.mPowerManager = powerManager;
    }

    public boolean notifyKeyEventLocked(KeyEvent event, int policyFlags, List<Service> boundServices) {
        Object pendingKeyEvent = null;
        KeyEvent localClone = KeyEvent.obtain(event);
        for (int i = 0; i < boundServices.size(); i += MSG_ON_KEY_EVENT_TIMEOUT) {
            Service service = (Service) boundServices.get(i);
            if (service.mRequestFilterKeyEvents && (service.mAccessibilityServiceInfo.getCapabilities() & 8) != 0) {
                try {
                    service.mServiceInterface.onKeyEvent(localClone, localClone.getSequenceNumber());
                    if (pendingKeyEvent == null) {
                        pendingKeyEvent = obtainPendingEventLocked(localClone, policyFlags);
                    }
                    ArrayList<PendingKeyEvent> pendingEventList = (ArrayList) this.mPendingEventsMap.get(service);
                    if (pendingEventList == null) {
                        pendingEventList = new ArrayList();
                        this.mPendingEventsMap.put(service, pendingEventList);
                    }
                    pendingEventList.add(pendingKeyEvent);
                    pendingKeyEvent.referenceCount += MSG_ON_KEY_EVENT_TIMEOUT;
                } catch (RemoteException e) {
                }
            }
        }
        if (pendingKeyEvent == null) {
            localClone.recycle();
            return DEBUG;
        }
        this.mKeyEventTimeoutHandler.sendMessageDelayed(this.mKeyEventTimeoutHandler.obtainMessage(MSG_ON_KEY_EVENT_TIMEOUT, pendingKeyEvent), ON_KEY_EVENT_TIMEOUT_MILLIS);
        return true;
    }

    public void setOnKeyEventResult(Service service, boolean handled, int sequence) {
        synchronized (this.mLock) {
            PendingKeyEvent pendingEvent = removeEventFromListLocked((List) this.mPendingEventsMap.get(service), sequence);
            if (pendingEvent != null) {
                if (handled && !pendingEvent.handled) {
                    pendingEvent.handled = handled;
                    long identity = Binder.clearCallingIdentity();
                    try {
                        this.mPowerManager.userActivity(pendingEvent.event.getEventTime(), 3, 0);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
                removeReferenceToPendingEventLocked(pendingEvent);
            }
        }
    }

    public void flush(Service service) {
        synchronized (this.mLock) {
            List<PendingKeyEvent> pendingEvents = (List) this.mPendingEventsMap.get(service);
            if (pendingEvents != null) {
                for (int i = 0; i < pendingEvents.size(); i += MSG_ON_KEY_EVENT_TIMEOUT) {
                    removeReferenceToPendingEventLocked((PendingKeyEvent) pendingEvents.get(i));
                }
                this.mPendingEventsMap.remove(service);
            }
        }
    }

    private PendingKeyEvent obtainPendingEventLocked(KeyEvent event, int policyFlags) {
        PendingKeyEvent pendingEvent = (PendingKeyEvent) this.mPendingEventPool.acquire();
        if (pendingEvent == null) {
            pendingEvent = new PendingKeyEvent();
        }
        pendingEvent.event = event;
        pendingEvent.policyFlags = policyFlags;
        pendingEvent.referenceCount = 0;
        pendingEvent.handled = DEBUG;
        return pendingEvent;
    }

    private static PendingKeyEvent removeEventFromListLocked(List<PendingKeyEvent> listOfEvents, int sequence) {
        for (int i = 0; i < listOfEvents.size(); i += MSG_ON_KEY_EVENT_TIMEOUT) {
            PendingKeyEvent pendingKeyEvent = (PendingKeyEvent) listOfEvents.get(i);
            if (pendingKeyEvent.event.getSequenceNumber() == sequence) {
                listOfEvents.remove(pendingKeyEvent);
                return pendingKeyEvent;
            }
        }
        return null;
    }

    private boolean removeReferenceToPendingEventLocked(PendingKeyEvent pendingEvent) {
        int i = pendingEvent.referenceCount - 1;
        pendingEvent.referenceCount = i;
        if (i > 0) {
            return DEBUG;
        }
        this.mKeyEventTimeoutHandler.removeMessages(MSG_ON_KEY_EVENT_TIMEOUT, pendingEvent);
        if (pendingEvent.handled) {
            pendingEvent.event.recycle();
        } else {
            if (this.mSentEventsVerifier != null) {
                this.mSentEventsVerifier.onKeyEvent(pendingEvent.event, 0);
            }
            this.mHandlerToSendKeyEventsToInputFilter.obtainMessage(this.mMessageTypeForSendKeyEvent, pendingEvent.policyFlags | 1073741824, 0, pendingEvent.event).sendToTarget();
        }
        this.mPendingEventPool.release(pendingEvent);
        return true;
    }
}
