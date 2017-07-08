package android.icu.impl;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

public abstract class ICUNotifier {
    private List<EventListener> listeners;
    private final Object notifyLock;
    private NotifyThread notifyThread;

    private static class NotifyThread extends Thread {
        private final ICUNotifier notifier;
        private final List<EventListener[]> queue;

        NotifyThread(ICUNotifier notifier) {
            this.queue = new ArrayList();
            this.notifier = notifier;
        }

        public void queue(EventListener[] list) {
            synchronized (this) {
                this.queue.add(list);
                notify();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            while (true) {
                try {
                    EventListener[] list;
                    synchronized (this) {
                        while (true) {
                            if (this.queue.isEmpty()) {
                                wait();
                            } else {
                                list = (EventListener[]) this.queue.remove(0);
                            }
                        }
                        break;
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

    public ICUNotifier() {
        this.notifyLock = new Object();
    }

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
                        return;
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
