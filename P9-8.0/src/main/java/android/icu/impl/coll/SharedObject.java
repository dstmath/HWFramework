package android.icu.impl.coll;

import android.icu.util.ICUCloneNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedObject implements Cloneable {
    private AtomicInteger refCount = new AtomicInteger();

    public static final class Reference<T extends SharedObject> implements Cloneable {
        private T ref;

        public Reference(T r) {
            this.ref = r;
            if (r != null) {
                r.addRef();
            }
        }

        public Reference<T> clone() {
            try {
                Reference<T> c = (Reference) super.clone();
                if (this.ref != null) {
                    this.ref.addRef();
                }
                return c;
            } catch (Throwable e) {
                throw new ICUCloneNotSupportedException(e);
            }
        }

        public T readOnly() {
            return this.ref;
        }

        public T copyOnWrite() {
            T r = this.ref;
            if (r.getRefCount() <= 1) {
                return r;
            }
            T r2 = r.clone();
            r.removeRef();
            this.ref = r2;
            r2.addRef();
            return r2;
        }

        public void clear() {
            if (this.ref != null) {
                this.ref.removeRef();
                this.ref = null;
            }
        }

        protected void finalize() throws Throwable {
            super.finalize();
            clear();
        }
    }

    public SharedObject clone() {
        try {
            SharedObject c = (SharedObject) super.clone();
            c.refCount = new AtomicInteger();
            return c;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public final void addRef() {
        this.refCount.incrementAndGet();
    }

    public final void removeRef() {
        this.refCount.decrementAndGet();
    }

    public final int getRefCount() {
        return this.refCount.get();
    }

    public final void deleteIfZeroRefCount() {
    }
}
