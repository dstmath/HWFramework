package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;

public final class AttributeCache {
    private static final int CACHE_SIZE = 4;
    private static AttributeCache sInstance = null;
    @GuardedBy("this")
    private final Configuration mConfiguration = new Configuration();
    private final Context mContext;
    @GuardedBy("this")
    private final LruCache<String, Package> mPackages = new LruCache(4);

    public static final class Entry {
        public final TypedArray array;
        public final Context context;

        public Entry(Context c, TypedArray ta) {
            this.context = c;
            this.array = ta;
        }

        void recycle() {
            if (this.array != null) {
                this.array.recycle();
            }
        }
    }

    public static final class Package {
        public final Context context;
        private final SparseArray<ArrayMap<int[], Entry>> mMap = new SparseArray();

        public Package(Context c) {
            this.context = c;
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
            Package pkg = (Package) this.mPackages.remove(packageName);
            if (pkg != null) {
                for (int i = 0; i < pkg.mMap.size(); i++) {
                    ArrayMap<int[], Entry> map = (ArrayMap) pkg.mMap.valueAt(i);
                    for (int j = 0; j < map.size(); j++) {
                        ((Entry) map.valueAt(j)).recycle();
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
            Package pkg = (Package) this.mPackages.get(packageName);
            ArrayMap map = null;
            Entry ent2 = null;
            if (pkg != null) {
                map = (ArrayMap) pkg.mMap.get(resId);
                if (map != null) {
                    ent2 = (Entry) map.get(styleable);
                    if (ent2 != null) {
                        return ent2;
                    }
                }
                ent = ent2;
            } else {
                try {
                    Context context = this.mContext.createPackageContextAsUser(packageName, 0, new UserHandle(userId));
                    if (context == null) {
                        return null;
                    }
                    pkg = new Package(context);
                    this.mPackages.put(packageName, pkg);
                    ent = null;
                } catch (NameNotFoundException e) {
                    return null;
                }
            }
            if (map == null) {
                map = new ArrayMap();
                pkg.mMap.put(resId, map);
            }
            try {
                ent2 = new Entry(pkg.context, pkg.context.obtainStyledAttributes(resId, styleable));
                try {
                    map.put(styleable, ent2);
                    return ent2;
                } catch (NotFoundException e2) {
                    return null;
                }
            } catch (NotFoundException e3) {
                ent2 = ent;
                return null;
            }
        }
    }
}
