package android.icu.impl;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

public abstract class ICUNotifier {
    private List<EventListener> listeners;
    private final Object notifyLock = new Object();
    private NotifyThread notifyThread;

    private static class NotifyThread extends Thread {
        private final ICUNotifier notifier;
        private final List<EventListener[]> queue = new ArrayList();

        NotifyThread(ICUNotifier notifier) {
            this.notifier = notifier;
        }

        public void queue(EventListener[] list) {
            synchronized (this) {
                this.queue.add(list);
                notify();
            }
        }

        public void run() {
            while (true) {
                try {
                    EventListener[] list;
                    synchronized (this) {
                        while (this.queue.isEmpty()) {
                            wait();
                        }
                        list = (EventListener[]) this.queue.remove(0);
                    }
                    for (EventListener notifyListener : list) {
                        this.notifier.notifyListener(notifyListener);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    protected abstract boolean acceptsListener(EventListener eventListener);

    protected abstract void notifyListener(EventListener eventListener);

    public void addListener(EventListener l) {
        if (l == null) {
            throw new NullPointerException();
        } else if (acceptsListener(l)) {
            synchronized (this.notifyLock) {
                if (this.listeners == null) {
                    this.listeners = new ArrayList();
                } else {
                    for (EventListener ll : this.listeners) {
                        if (ll == l) {
                            return;
                        }
                    }
                }
                this.listeners.add(l);
            }
        } else {
            throw new IllegalStateException("Listener invalid for this notifier.");
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0030, code:
            return;
     */
    /* JADX WARNING: Missing block: B:19:0x0032, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeListener(EventListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        synchronized (this.notifyLock) {
            if (this.listeners != null) {
                Iterator<EventListener> iter = this.listeners.iterator();
                while (iter.hasNext()) {
                    if (iter.next() == l) {
                        iter.remove();
                        if (this.listeners.size() == 0) {
                            this.listeners = null;
                        }
                    }
                }
            }
        }
    }

    public void notifyChanged() {
        if (this.listeners != null) {
            synchronized (this.notifyLock) {
                if (this.listeners != null) {
                    if (this.notifyThread == null) {
                        this.notifyThread = new NotifyThread(this);
                        this.notifyThread.setDaemon(true);
                        this.notifyThread.start();
                    }
                    this.notifyThread.queue((EventListener[]) this.listeners.toArray(new EventListener[this.listeners.size()]));
                }
            }
        }
    }
}
