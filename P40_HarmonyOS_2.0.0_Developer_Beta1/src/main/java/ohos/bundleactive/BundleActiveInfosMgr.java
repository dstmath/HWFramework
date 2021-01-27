package ohos.bundleactive;

import com.huawei.ohos.bundleactiveadapter.BundleActiveInfosAdapter;
import com.huawei.ohos.bundleactiveadapter.BundleActiveInfosMgrAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.app.Context;

public final class BundleActiveInfosMgr {
    public static final int BY_ANNUALLY = 4;
    public static final int BY_DAILY = 1;
    public static final int BY_MONTHLY = 3;
    public static final int BY_OPTIMIZED = 0;
    public static final int BY_WEEKLY = 2;
    public static final int USAGE_PRIORITY_GROUP_ALIVE = 1;
    public static final int USAGE_PRIORITY_GROUP_NEVER = 5;
    public static final int USAGE_PRIORITY_GROUP_OFTEN = 3;
    public static final int USAGE_PRIORITY_GROUP_PREFERENCE = 2;
    public static final int USAGE_PRIORITY_GROUP_PRIVILEGE = 0;
    public static final int USAGE_PRIORITY_GROUP_SELDOM = 4;
    private final BundleActiveInfosMgrAdapter mManagerAdapter;

    private BundleActiveInfosMgr(Object obj) {
        if (obj instanceof BundleActiveInfosMgrAdapter) {
            this.mManagerAdapter = (BundleActiveInfosMgrAdapter) obj;
        } else {
            this.mManagerAdapter = null;
        }
    }

    public static BundleActiveInfosMgr newInstance(Context context) {
        BundleActiveInfosMgrAdapter newInstance;
        if (context == null || (newInstance = BundleActiveInfosMgrAdapter.newInstance(context)) == null) {
            return null;
        }
        return new BundleActiveInfosMgr(newInstance);
    }

    public List<BundleActiveInfos> queryBundleActiveInfosByInterval(int i, long j, long j2) {
        ArrayList arrayList = new ArrayList();
        this.mManagerAdapter.queryBundleActiveInfosByInterval(i, j, j2).forEach(new Consumer(arrayList) {
            /* class ohos.bundleactive.$$Lambda$BundleActiveInfosMgr$HTrq5QCfh8VofeXJ0V2h7WFU4BI */
            private final /* synthetic */ ArrayList f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BundleActiveInfosMgr.lambda$queryBundleActiveInfosByInterval$0(this.f$0, (BundleActiveInfosAdapter) obj);
            }
        });
        return arrayList;
    }

    public Map<String, BundleActiveInfos> queryBundleActiveInfos(long j, long j2) {
        HashMap hashMap = new HashMap();
        this.mManagerAdapter.queryBundleActiveInfos(j, j2).forEach(new BiConsumer(hashMap) {
            /* class ohos.bundleactive.$$Lambda$BundleActiveInfosMgr$Pv8d7J8s1iUu3VsgHBpobFcFJ68 */
            private final /* synthetic */ HashMap f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                BundleActiveInfosMgr.lambda$queryBundleActiveInfos$1(this.f$0, (String) obj, (BundleActiveInfosAdapter) obj2);
            }
        });
        return hashMap;
    }

    public BundleActiveStates queryBundleActiveStates(long j, long j2) {
        return new BundleActiveStates(this.mManagerAdapter.queryBundleActiveStates(j, j2));
    }

    public BundleActiveStates queryCurrentBundleActiveStates(long j, long j2) {
        return new BundleActiveStates(this.mManagerAdapter.queryCurrentBundleActiveStates(j, j2));
    }

    public boolean isIdleState(String str) {
        return this.mManagerAdapter.isIdleState(str);
    }

    public int queryAppUsagePriorityGroup() {
        return this.mManagerAdapter.queryAppUsagePriorityGroup();
    }
}
