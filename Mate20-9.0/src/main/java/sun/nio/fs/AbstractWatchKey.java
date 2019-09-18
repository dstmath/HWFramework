package sun.nio.fs;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractWatchKey implements WatchKey {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int MAX_EVENT_LIST_SIZE = 512;
    static final Event<Object> OVERFLOW_EVENT = new Event<>(StandardWatchEventKinds.OVERFLOW, null);
    private final Path dir;
    private List<WatchEvent<?>> events = new ArrayList();
    private Map<Object, WatchEvent<?>> lastModifyEvents = new HashMap();
    private State state = State.READY;
    private final AbstractWatchService watcher;

    private static class Event<T> implements WatchEvent<T> {
        private final T context;
        private int count = 1;
        private final WatchEvent.Kind<T> kind;

        Event(WatchEvent.Kind<T> type, T context2) {
            this.kind = type;
            this.context = context2;
        }

        public WatchEvent.Kind<T> kind() {
            return this.kind;
        }

        public T context() {
            return this.context;
        }

        public int count() {
            return this.count;
        }

        /* access modifiers changed from: package-private */
        public void increment() {
            this.count++;
        }
    }

    private enum State {
        READY,
        SIGNALLED
    }

    protected AbstractWatchKey(Path dir2, AbstractWatchService watcher2) {
        this.watcher = watcher2;
        this.dir = dir2;
    }

    /* access modifiers changed from: package-private */
    public final AbstractWatchService watcher() {
        return this.watcher;
    }

    public Path watchable() {
        return this.dir;
    }

    /* access modifiers changed from: package-private */
    public final void signal() {
        synchronized (this) {
            if (this.state == State.READY) {
                this.state = State.SIGNALLED;
                this.watcher.enqueueKey(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void signalEvent(WatchEvent.Kind<?> kind, Object context) {
        boolean isModify = kind == StandardWatchEventKinds.ENTRY_MODIFY ? true : $assertionsDisabled;
        synchronized (this) {
            int size = this.events.size();
            if (size > 0) {
                WatchEvent<?> prev = this.events.get(size - 1);
                if (prev.kind() != StandardWatchEventKinds.OVERFLOW) {
                    if (kind != prev.kind() || !Objects.equals(context, prev.context())) {
                        if (!this.lastModifyEvents.isEmpty()) {
                            if (isModify) {
                                WatchEvent<?> ev = this.lastModifyEvents.get(context);
                                if (ev != null) {
                                    ((Event) ev).increment();
                                    return;
                                }
                            } else {
                                this.lastModifyEvents.remove(context);
                            }
                        }
                        if (size >= 512) {
                            kind = StandardWatchEventKinds.OVERFLOW;
                            isModify = $assertionsDisabled;
                            context = null;
                        }
                    }
                }
                ((Event) prev).increment();
                return;
            }
            Event<Object> ev2 = new Event<>(kind, context);
            if (isModify) {
                this.lastModifyEvents.put(context, ev2);
            } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                this.events.clear();
                this.lastModifyEvents.clear();
            }
            this.events.add(ev2);
            signal();
        }
    }

    public final List<WatchEvent<?>> pollEvents() {
        List<WatchEvent<?>> result;
        synchronized (this) {
            result = this.events;
            this.events = new ArrayList();
            this.lastModifyEvents.clear();
        }
        return result;
    }

    public final boolean reset() {
        boolean isValid;
        synchronized (this) {
            if (this.state == State.SIGNALLED && isValid()) {
                if (this.events.isEmpty()) {
                    this.state = State.READY;
                } else {
                    this.watcher.enqueueKey(this);
                }
            }
            isValid = isValid();
        }
        return isValid;
    }
}
