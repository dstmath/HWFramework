package ohos.aafwk.ability;

import java.util.List;

public interface IHostCallback {
    void onAcquire(int i, FormRecord formRecord);

    void onFormUninstalled(List<Integer> list);

    void onUpdate(int i, FormRecord formRecord);
}
