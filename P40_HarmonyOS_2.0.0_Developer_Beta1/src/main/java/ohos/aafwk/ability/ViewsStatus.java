package ohos.aafwk.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.Text;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ViewsStatus implements Sequenceable {
    public static final String KEY_TEXT = "text";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108672, "ViewsStatus");
    private static final int MAX_UNMARSHALLING_SIZE = 3145728;
    private HashMap<Integer, Intent> allStatus = new HashMap<>();
    private Intent fullPageIntent;

    private ViewsStatus() {
    }

    static ViewsStatus buildViewsStatus(ComponentProvider componentProvider, Set<Integer> set, Intent intent) {
        if (componentProvider == null) {
            HiLog.error(LABEL, "components is null when build views status", new Object[0]);
            return null;
        } else if (set == null) {
            HiLog.error(LABEL, "componentIds Listen is null when build views status", new Object[0]);
            return null;
        } else {
            ViewsStatus viewsStatus = new ViewsStatus();
            ComponentContainer allComponents = componentProvider.getAllComponents();
            if (allComponents == null) {
                HiLog.error(LABEL, "component container is null when build views status", new Object[0]);
                return null;
            }
            for (Integer num : set) {
                int intValue = num.intValue();
                Component findComponentById = allComponents.findComponentById(intValue);
                if (findComponentById == null) {
                    HiLog.error(LABEL, "unavailable component id when build views status. component id: %{public}d", new Object[]{Integer.valueOf(intValue)});
                } else {
                    viewsStatus.allStatus.put(Integer.valueOf(intValue), getViewStatus(findComponentById));
                }
            }
            viewsStatus.fullPageIntent = new Intent(intent);
            return viewsStatus;
        }
    }

    private static Intent getViewStatus(Component component) {
        Intent intent = new Intent();
        if (component == null) {
            HiLog.error(LABEL, "component is null when get view status, return empty intent.", new Object[0]);
            return intent;
        }
        if (component instanceof Button) {
            getStatusFromButton(intent, (Button) component);
        } else if (component instanceof Text) {
            getStatusFromText(intent, (Text) component);
        } else {
            HiLog.error(LABEL, "do not support component. component: %{public}s", new Object[]{component.getClass()});
        }
        return intent;
    }

    private static void getStatusFromText(Intent intent, Text text) {
        intent.setParam("text", text.getText());
    }

    private static void getStatusFromButton(Intent intent, Button button) {
        getStatusFromText(intent, button);
    }

    public static ViewsStatus createFromParcel(Parcel parcel) {
        ViewsStatus viewsStatus = new ViewsStatus();
        if (!parcel.readSequenceable(viewsStatus)) {
            return null;
        }
        return viewsStatus;
    }

    public Intent getStatusByComponentId(int i) {
        return this.allStatus.get(Integer.valueOf(i));
    }

    public Intent getFullPageIntent() {
        return this.fullPageIntent;
    }

    public boolean marshalling(Parcel parcel) {
        HashMap<Integer, Intent> hashMap = this.allStatus;
        if (!parcel.writeInt(hashMap != null ? hashMap.size() : 0)) {
            return false;
        }
        HashMap<Integer, Intent> hashMap2 = this.allStatus;
        if (hashMap2 != null) {
            for (Map.Entry<Integer, Intent> entry : hashMap2.entrySet()) {
                if (!parcel.writeInt(entry.getKey().intValue())) {
                    return false;
                }
                parcel.writeSequenceable(entry.getValue());
            }
        }
        parcel.writeSequenceable(this.fullPageIntent);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 3145728 || readInt < 0) {
            HiLog.error(LABEL, "unmarshalling error. wrong length", new Object[0]);
            return false;
        }
        this.allStatus.clear();
        for (int i = 0; i < readInt; i++) {
            int readInt2 = parcel.readInt();
            Intent intent = new Intent();
            if (!parcel.readSequenceable(intent)) {
                return false;
            }
            this.allStatus.put(Integer.valueOf(readInt2), intent);
        }
        this.fullPageIntent = new Intent();
        return parcel.readSequenceable(this.fullPageIntent);
    }
}
