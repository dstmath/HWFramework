package com.huawei.nb.container;

import com.huawei.nb.environment.Disposable;
import com.huawei.nb.exception.ExceptionHelper;
import com.huawei.nb.utils.validation.ObjectValidation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class DisposableContainer implements Disposable, Container<Disposable> {
    volatile boolean disposed;
    private final Object mLock;
    HashSet<Disposable> resources;

    public DisposableContainer() {
        this.mLock = new Object();
        this.resources = new HashSet<>();
    }

    public DisposableContainer(Disposable... resources2) {
        this.mLock = new Object();
        ObjectValidation.verifyNotNull(resources2, "resources is null");
        this.resources = new HashSet<>(resources2.length + 1);
        for (Disposable d : resources2) {
            ObjectValidation.verifyNotNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    public DisposableContainer(Iterable<? extends Disposable> resources2) {
        this.mLock = new Object();
        ObjectValidation.verifyNotNull(resources2, "resources is null");
        this.resources = new HashSet<>();
        for (Disposable d : resources2) {
            ObjectValidation.verifyNotNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    public void dispose() {
        if (!this.disposed) {
            synchronized (this.mLock) {
                if (!this.disposed) {
                    this.disposed = true;
                    HashSet<Disposable> set = this.resources;
                    this.resources = null;
                    dispose(set);
                }
            }
        }
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public boolean add(Disposable d) {
        ObjectValidation.verifyNotNull(d, "d is null");
        synchronized (this.mLock) {
            if (!this.disposed) {
                HashSet<Disposable> set = this.resources;
                if (set == null) {
                    set = new HashSet<>();
                    this.resources = set;
                }
                set.add(d);
                return true;
            }
            d.dispose();
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: boolean} */
    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v3, types: [int] */
    /* JADX WARNING: type inference failed for: r2v4 */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean addAll(Disposable... ds) {
        ? r2 = 0;
        ObjectValidation.verifyNotNull(ds, "ds is null");
        synchronized (this.mLock) {
            if (!this.disposed) {
                HashSet<Disposable> set = this.resources;
                if (set == null) {
                    set = new HashSet<>(ds.length + 1);
                    this.resources = set;
                }
                int length = ds.length;
                while (r2 < length) {
                    Disposable d = ds[r2];
                    ObjectValidation.verifyNotNull(d, "d is null");
                    set.add(d);
                    r2++;
                }
                r2 = 1;
            } else {
                for (Disposable d2 : ds) {
                    d2.dispose();
                }
            }
        }
        return r2;
    }

    public boolean remove(Disposable d) {
        if (!delete(d)) {
            return false;
        }
        d.dispose();
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        return false;
     */
    public boolean delete(Disposable d) {
        ObjectValidation.verifyNotNull(d, "Disposable item is null");
        if (this.disposed) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.disposed) {
                return false;
            }
            HashSet<Disposable> set = this.resources;
            if (set != null && set.remove(d)) {
                return true;
            }
        }
    }

    public void clear() {
        if (!this.disposed) {
            synchronized (this.mLock) {
                if (!this.disposed) {
                    HashSet<Disposable> set = this.resources;
                    this.resources = null;
                    dispose(set);
                }
            }
        }
    }

    public int size() {
        int i = 0;
        if (!this.disposed) {
            synchronized (this.mLock) {
                if (!this.disposed) {
                    HashSet<Disposable> set = this.resources;
                    if (set != null) {
                        i = set.size();
                    }
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void dispose(HashSet<Disposable> set) {
        if (set != null) {
            List<Throwable> errors = null;
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Disposable) {
                    try {
                        ((Disposable) o).dispose();
                    } catch (Throwable ex) {
                        ExceptionHelper.throwIfFatal(ex);
                        if (errors == null) {
                            errors = new ArrayList<>();
                        }
                        errors.add(ex);
                    }
                }
            }
        }
    }

    public Iterator<Disposable> iterator() {
        Iterator<Disposable> it;
        synchronized (this.mLock) {
            it = this.resources.iterator();
        }
        return it;
    }
}
