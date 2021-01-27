package com.huawei.anim.dynamicanimation;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import java.util.ArrayList;
import java.util.HashMap;

/* access modifiers changed from: package-private */
/* compiled from: AnimationHandler */
public class a {
    public static final ThreadLocal<a> a = new ThreadLocal<>();
    private static final long b = 10;
    private final HashMap<b, Long> c = new HashMap<>();
    private final ArrayList<b> d = new ArrayList<>();
    private final C0000a e = new C0000a();
    private c f;
    private long g = 0;
    private boolean h = false;

    /* access modifiers changed from: package-private */
    /* compiled from: AnimationHandler */
    public interface b {
        boolean doAnimationFrame(long j);
    }

    a() {
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.anim.dynamicanimation.a$a  reason: collision with other inner class name */
    /* compiled from: AnimationHandler */
    public class C0000a {
        C0000a() {
        }

        /* access modifiers changed from: package-private */
        public void a() {
            a.this.g = SystemClock.uptimeMillis();
            a aVar = a.this;
            aVar.a(aVar.g);
            if (a.this.d.size() > 0) {
                a.this.c().a();
            }
        }
    }

    public static a a() {
        if (a.get() == null) {
            a.set(new a());
        }
        return a.get();
    }

    public static long b() {
        if (a.get() == null) {
            return 0;
        }
        return a.get().g;
    }

    public void a(c cVar) {
        this.f = cVar;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private c c() {
        if (this.f == null) {
            if (Build.VERSION.SDK_INT >= 16) {
                this.f = new e(this.e);
            } else {
                this.f = new d(this.e);
            }
        }
        return this.f;
    }

    public void a(b bVar, long j) {
        if (this.d.size() == 0) {
            c().a();
        }
        if (!this.d.contains(bVar)) {
            this.d.add(bVar);
        }
        if (j > 0) {
            this.c.put(bVar, Long.valueOf(SystemClock.uptimeMillis() + j));
        }
    }

    public void a(b bVar) {
        this.c.remove(bVar);
        int indexOf = this.d.indexOf(bVar);
        if (indexOf >= 0) {
            this.d.set(indexOf, null);
            this.h = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void a(long j) {
        long uptimeMillis = SystemClock.uptimeMillis();
        for (int i = 0; i < this.d.size(); i++) {
            b bVar = this.d.get(i);
            if (bVar != null && b(bVar, uptimeMillis)) {
                bVar.doAnimationFrame(j);
            }
        }
        d();
    }

    private boolean b(b bVar, long j) {
        if (this.c.get(bVar) == null) {
            return true;
        }
        if (this.c.get(bVar).longValue() >= j) {
            return false;
        }
        this.c.remove(bVar);
        return true;
    }

    private void d() {
        if (this.h) {
            for (int size = this.d.size() - 1; size >= 0; size--) {
                if (this.d.get(size) == null) {
                    this.d.remove(size);
                }
            }
            this.h = false;
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: AnimationHandler */
    public static class e extends c {
        private final Choreographer b = Choreographer.getInstance();
        private final Choreographer.FrameCallback c = new Choreographer.FrameCallback() {
            /* class com.huawei.anim.dynamicanimation.a.e.AnonymousClass1 */

            public void a(long j) {
                e.this.a.a();
            }
        };

        e(C0000a aVar) {
            super(aVar);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.anim.dynamicanimation.a.c
        public void a() {
            this.b.postFrameCallback(this.c);
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: AnimationHandler */
    public static class d extends c {
        private static final long b = -1;
        private final Runnable c = new Runnable() {
            /* class com.huawei.anim.dynamicanimation.a.d.AnonymousClass1 */

            public void a() {
                d.this.e = SystemClock.uptimeMillis();
                d.this.a.a();
            }
        };
        private final Handler d = new Handler(Looper.myLooper());
        private long e = b;

        d(C0000a aVar) {
            super(aVar);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.anim.dynamicanimation.a.c
        public void a() {
            this.d.postDelayed(this.c, Math.max(a.b - (SystemClock.uptimeMillis() - this.e), 0L));
        }
    }

    /* access modifiers changed from: package-private */
    /* compiled from: AnimationHandler */
    public static abstract class c {
        final C0000a a;

        /* access modifiers changed from: package-private */
        public abstract void a();

        c(C0000a aVar) {
            this.a = aVar;
        }
    }
}
