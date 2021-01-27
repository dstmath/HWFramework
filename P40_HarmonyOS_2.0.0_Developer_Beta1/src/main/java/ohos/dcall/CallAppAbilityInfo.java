package ohos.dcall;

import android.content.ComponentName;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class CallAppAbilityInfo {
    private ComponentName mComponentName;
    private int mType;

    public CallAppAbilityInfo(ComponentName componentName, int i) {
        this.mComponentName = componentName;
        this.mType = i;
    }

    /* access modifiers changed from: package-private */
    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    /* access modifiers changed from: package-private */
    public void setComponentName(ComponentName componentName) {
        this.mComponentName = componentName;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mType;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof CallAppAbilityInfo)) {
            return false;
        }
        CallAppAbilityInfo callAppAbilityInfo = (CallAppAbilityInfo) obj;
        if (this.mType != callAppAbilityInfo.getType()) {
            return false;
        }
        if (this.mComponentName == null && callAppAbilityInfo.mComponentName != null) {
            return false;
        }
        if (this.mComponentName == null || callAppAbilityInfo.mComponentName != null) {
            return this.mComponentName.equals(callAppAbilityInfo.mComponentName);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.mComponentName);
    }

    public String toString() {
        return "[" + this.mComponentName + "," + this.mType + "]";
    }
}
