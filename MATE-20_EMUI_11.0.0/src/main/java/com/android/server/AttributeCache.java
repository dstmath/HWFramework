package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;

public final class AttributeCache {
    private static final int CACHE_SIZE = 4;
    private static AttributeCache sInstance = null;
    @GuardedBy({"this"})
    private final Configuration mConfiguration = new Configuration();
    private final Context mContext;
    @GuardedBy({"this"})
    private final LruCache<String, Package> mPackages = new LruCache<>(4);

    public static final class Package {
        public final Context context;
        private final SparseArray<ArrayMap<int[], Entry>> mMap = new SparseArray<>();

        public Package(Context c) {
            this.context = c;
        }
    }

    public static final class Entry {
        public final TypedArray array;
        public final Context context;

        public Entry(Context c, TypedArray ta) {
            this.context = c;
            this.array = ta;
        }

        /* access modifiers changed from: package-private */
        public void recycle() {
            TypedArray typedArray = this.array;
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new AttributeCache(context);
        }
    }

    public static AttributeCache instance() {
        return sInstance;
    }

    public AttributeCache(Context context) {
        this.mContext = context;
    }

    public void removePackage(String packageName) {
        synchronized (this) {
            Package pkg = this.mPackages.remove(packageName);
            if (pkg != null) {
                for (int i = 0; i < pkg.mMap.size(); i++) {
                    ArrayMap<int[], Entry> map = (ArrayMap) pkg.mMap.valueAt(i);
                    for (int j = 0; j < map.size(); j++) {
                        map.valueAt(j).recycle();
                    }
                }
                pkg.context.getResources().flushLayoutCache();
            }
        }
    }

    public void updateConfiguration(Configuration config) {
        synchronized (this) {
            if ((-1073741985 & this.mConfiguration.updateFrom(config)) != 0) {
                this.mPackages.evictAll();
            }
        }
    }

    public Entry get(String packageName, int resId, int[] styleable, int userId) {
        Entry ent;
        synchronized (this) {
            Package pkg = this.mPackages.get(packageName);
            ArrayMap<int[], Entry> map = null;
            if (pkg != null) {
                map = (ArrayMap) pkg.mMap.get(resId);
                if (!(map == null || (ent = map.get(styleable)) == null)) {
                    return ent;
                }
            } else {
                try {
                    Context context = this.mContext.createPackageContextAsUser(packageName, 0, new UserHandle(userId));
                    if (context == null) {
                        return null;
                    }
                    pkg = new Package(context);
                    this.mPackages.put(packageName, pkg);
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
            }
            if (map == null) {
                map = new ArrayMap<>();
                pkg.mMap.put(resId, map);
            }
            try {
                Entry ent2 = new Entry(pkg.context, pkg.context.obtainStyledAttributes(resId, styleable));
                map.put(styleable, ent2);
                return ent2;
            } catch (Resources.NotFoundException e2) {
                return null;
            }
        }
    }
}
