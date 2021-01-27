package ohos.data.distributed.common;

import java.util.Map;

public interface SyncCallback {
    void syncCompleted(Map<String, Integer> map);
}
