package com.android.server.wm;

import android.os.Bundle;
import com.huawei.android.util.SlogEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwMagicWinModulePolicy {
    private static final String TAG = "HWMW_HwMagicWinModulePolicy";

    public interface IPolicyOperation {
        void execute(List list, Bundle bundle);
    }

    public static class ModulePolicy {
        protected final Map<Integer, PolicySyntax> mPolicyOpetions = new HashMap();

        /* access modifiers changed from: protected */
        public void addPolicy(int policyId, IPolicyOperation op, Class<?>... paramTypes) {
            this.mPolicyOpetions.put(Integer.valueOf(policyId), new PolicySyntax(policyId, op, paramTypes));
        }

        public void performHwMagicWindowPolicy(int policyId, List params, Bundle result) {
            PolicySyntax policy = this.mPolicyOpetions.get(Integer.valueOf(policyId));
            if (policy != null && policy.operation != null) {
                if (!(params instanceof ArrayList) || policy.checkParameters((ArrayList) params)) {
                    policy.operation.execute(params, result);
                    return;
                }
                SlogEx.e(HwMagicWinModulePolicy.TAG, "Invalid param for policy=" + policyId);
            }
        }
    }

    public static class PolicySyntax {
        public IPolicyOperation operation;
        public int paramCnt;
        public List paramTypeSet = new ArrayList();
        public int policyId;

        public PolicySyntax(int id, IPolicyOperation op, Class<?>... paramTypes) {
            this.policyId = id;
            this.operation = op;
            this.paramCnt = paramTypes.length;
            for (int i = 0; i < this.paramCnt; i++) {
                this.paramTypeSet.add(paramTypes[i]);
            }
        }

        public boolean checkParameters(ArrayList<Object> params) {
            if (params.size() != this.paramCnt) {
                SlogEx.e(HwMagicWinModulePolicy.TAG, "Cnt error:   expected cnt=" + this.paramCnt + "   actual cnt=" + params.size());
                return false;
            }
            boolean hasError = false;
            for (int i = 0; i < this.paramCnt; i++) {
                if (params.get(i) != null && !((Class) this.paramTypeSet.get(i)).isInstance(params.get(i))) {
                    SlogEx.e(HwMagicWinModulePolicy.TAG, "Type error:   index=" + i + "   expected type=" + this.paramTypeSet.get(i) + "   actual type=" + params.get(i).getClass());
                    hasError = true;
                }
            }
            if (!hasError) {
                return true;
            }
            return false;
        }
    }
}
