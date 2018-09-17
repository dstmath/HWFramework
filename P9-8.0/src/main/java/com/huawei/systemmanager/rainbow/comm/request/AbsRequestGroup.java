package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.util.Log;
import com.huawei.systemmanager.rainbow.comm.request.GroupRequestPolicy.FailRequestPolicy;
import java.util.ArrayList;
import java.util.List;

public class AbsRequestGroup extends AbsRequest {
    private static final String TAG = "AbsRequestGroup";
    private FailRequestPolicy mPolicy = FailRequestPolicy.RETURN_WHEN_FAILED;
    private List<AbsRequest> mRequests = new ArrayList();

    public AbsRequestGroup(FailRequestPolicy policy) {
        this.mPolicy = policy;
    }

    public AbsRequestGroup(List<AbsRequest> requestList, FailRequestPolicy policy) {
        this.mRequests.addAll(requestList);
        this.mPolicy = policy;
    }

    public void addRequest(AbsRequest request) {
        if (request != null) {
            this.mRequests.add(request);
        }
    }

    public boolean isEmpty() {
        return this.mRequests.isEmpty();
    }

    protected void doRequest(Context ctx) {
        for (AbsRequest request : this.mRequests) {
            if (!request.processRequest(ctx)) {
                setRequestFailed();
                if (!this.mPolicy.shouldContinue()) {
                    Log.d(TAG, "doRequest stop because of the policy");
                    return;
                }
            }
        }
    }
}
