package com.android.server.accessibility;

import android.os.Binder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.util.ArrayMap;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.Slog;
import android.view.InputEventConsistencyVerifier;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KeyEventDispatcher implements Callback {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "KeyEventDispatcher";
    private static final int MAX_POOL_SIZE = 10;
    public static final int MSG_ON_KEY_EVENT_TIMEOUT = 1;
    private static final long ON_KEY_EVENT_TIMEOUT_MILLIS = 500;
    private final Handler mHandlerToSendKeyEventsToInputFilter;
    private Handler mKeyEventTimeoutHandler;
    private final Object mLock;
    private final int mMessageTypeForSendKeyEvent;
    private final Pool<PendingKeyEvent> mPendingEventPool;
    private final Map<KeyEventFilter, ArrayList<PendingKeyEvent>> mPendingEventsMap;
    private final PowerManager mPowerManager;
    private final InputEventConsistencyVerifier mSentEventsVerifier;

    public interface KeyEventFilter {
        boolean onKeyEvent(KeyEvent keyEvent, int i);
    }

    private static final class PendingKeyEvent {
        KeyEvent event;
        boolean handled;
        int policyFlags;
        int referenceCount;

        /* synthetic */ PendingKeyEvent(PendingKeyEvent -this0) {
            this();
        }

        private PendingKeyEvent() {
        }
    }

    public KeyEventDispatcher(Handler handlerToSendKeyEventsToInputFilter, int messageTypeForSendKeyEvent, Object lock, PowerManager powerManager) {
        this.mPendingEventPool = new SimplePool(10);
        this.mPendingEventsMap = new ArrayMap();
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            this.mSentEventsVerifier = new InputEventConsistencyVerifier(this, 0, KeyEventDispatcher.class.getSimpleName());
        } else {
            this.mSentEventsVerifier = null;
        }
        this.mHandlerToSendKeyEventsToInputFilter = handlerToSendKeyEventsToInputFilter;
        this.mMessageTypeForSendKeyEvent = messageTypeForSendKeyEvent;
        this.mKeyEventTimeoutHandler = new Handler(handlerToSendKeyEventsToInputFilter.getLooper(), this);
        this.mLock = lock;
        this.mPowerManager = powerManager;
    }

    public KeyEventDispatcher(Handler handlerToSendKeyEventsToInputFilter, int messageTypeForSendKeyEvent, Object lock, PowerManager powerManager, Handler timeoutHandler) {
        this(handlerToSendKeyEventsToInputFilter, messageTypeForSendKeyEvent, lock, powerManager);
        this.mKeyEventTimeoutHandler = timeoutHandler;
    }

    public boolean notifyKeyEventLocked(KeyEvent event, int policyFlags, List<? extends KeyEventFilter> keyEventFilters) {
        Object pendingKeyEvent = null;
        KeyEvent localClone = KeyEvent.obtain(event);
        for (int i = 0; i < keyEventFilters.size(); i++) {
            KeyEventFilter keyEventFilter = (KeyEventFilter) keyEventFilters.get(i);
            if (keyEventFilter.onKeyEvent(localClone, localClone.getSequenceNumber())) {
                if (pendingKeyEvent == null) {
                    pendingKeyEvent = obtainPendingEventLocked(localClone, policyFlags);
                }
                ArrayList<PendingKeyEvent> pendingEventList = (ArrayList) this.mPendingEventsMap.get(keyEventFilter);
                if (pendingEventList == null) {
                    pendingEventList = new ArrayList();
                    this.mPendingEventsMap.put(keyEventFilter, pendingEventList);
                }
                pendingEventList.add(pendingKeyEvent);
                pendingKeyEvent.referenceCount++;
            }
        }
        if (pendingKeyEvent == null) {
            localClone.recycle();
            return false;
        }
        this.mKeyEventTimeoutHandler.sendMessageDelayed(this.mKeyEventTimeoutHandler.obtainMessage(1, pendingKeyEvent), 500);
        return true;
    }

    public void setOnKeyEventResult(KeyEventFilter keyEventFilter, boolean handled, int sequence) {
        synchronized (this.mLock) {
            PendingKeyEvent pendingEvent = removeEventFromListLocked((List) this.mPendingEventsMap.get(keyEventFilter), sequence);
            if (pendingEvent != null) {
                if (handled && (pendingEvent.handled ^ 1) != 0) {
                    pendingEvent.handled = handled;
                    long identity = Binder.clearCallingIdentity();
                    try {
                        this.mPowerManager.userActivity(pendingEvent.event.getEventTime(), 3, 0);
                        Binder.restoreCallingIdentity(identity);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
                removeReferenceToPendingEventLocked(pendingEvent);
            }
        }
    }

    public void flush(KeyEventFilter keyEventFilter) {
        synchronized (this.mLock) {
            List<PendingKeyEvent> pendingEvents = (List) this.mPendingEventsMap.get(keyEventFilter);
            if (pendingEvents != null) {
                for (int i = 0; i < pendingEvents.size(); i++) {
                    removeReferenceToPendingEventLocked((PendingKeyEvent) pendingEvents.get(i));
                }
                this.mPendingEventsMap.remove(keyEventFilter);
            }
        }
    }

    public boolean handleMessage(Message message) {
        if (message.what != 1) {
            Slog.w(LOG_TAG, "Unknown message: " + message.what);
            return false;
        }
        PendingKeyEvent pendingKeyEvent = message.obj;
        synchronized (this.mLock) {
            Iterator listForService$iterator = this.mPendingEventsMap.values().iterator();
            while (listForService$iterator.hasNext() && (!((ArrayList) listForService$iterator.next()).remove(pendingKeyEvent) || !removeReferenceToPendingEventLocked(pendingKeyEvent))) {
            }
        }
        return true;
    }

    private PendingKeyEvent obtainPendingEventLocked(KeyEvent event, int policyFlags) {
        PendingKeyEvent pendingEvent = (PendingKeyEvent) this.mPendingEventPool.acquire();
        if (pendingEvent == null) {
            pendingEvent = new PendingKeyEvent();
        }
        pendingEvent.event = event;
        pendingEvent.policyFlags = policyFlags;
        pendingEvent.referenceCount = 0;
        pendingEvent.handled = false;
        return pendingEvent;
    }

    private static PendingKeyEvent removeEventFromListLocked(List<PendingKeyEvent> listOfEvents, int sequence) {
        for (int i = 0; i < listOfEvents.size(); i++) {
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
            return false;
        }
        this.mKeyEventTimeoutHandler.removeMessages(1, pendingEvent);
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
