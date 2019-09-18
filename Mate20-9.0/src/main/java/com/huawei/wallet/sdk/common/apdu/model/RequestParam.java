package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import java.util.HashMap;
import java.util.Map;

public class RequestParam {
    private String issuerId;
    private int mode;
    private String productId;
    private int type;

    public RequestParam(String productId2, int mode2, int type2, String issuerId2) {
        this.productId = productId2;
        this.mode = mode2;
        this.type = type2;
        this.issuerId = issuerId2;
    }

    public int hashCode() {
        int i = 0;
        int result = 31 * ((31 * ((31 * 1) + (this.issuerId == null ? 0 : this.issuerId.hashCode()))) + this.mode);
        if (this.productId != null) {
            i = this.productId.hashCode();
        }
        return (31 * (result + i)) + this.type;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RequestParam)) {
            return false;
        }
        RequestParam param = (RequestParam) o;
        if (!this.productId.equals(param.productId) || !this.issuerId.equals(param.issuerId) || this.type != param.type || this.mode != param.mode) {
            return false;
        }
        return true;
    }

    public Map<String, String> convert2Map() {
        Map<String, String> params = new HashMap<>();
        params.put("productid", this.productId);
        params.put("mode", "" + this.mode);
        params.put(Constants.FIELD_HCI_CONFIG_DATA_TYPE, "" + this.type);
        params.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, this.issuerId);
        return params;
    }
}
