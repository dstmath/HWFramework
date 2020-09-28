package android.app.admin;

import java.util.ArrayList;

public interface IHwDeviceAdminInfo {
    ArrayList<PolicyInfo> getHwUsedPoliciesList();

    public static class PolicyInfo {
        public final int description;
        public final int descriptionForSecondaryUsers;
        public final int ident;
        public final int label;
        public final int labelForSecondaryUsers;
        public final String tag;

        public PolicyInfo(int ident2, String tag2, int label2, int description2) {
            this(ident2, tag2, label2, description2, label2, description2);
        }

        public PolicyInfo(int ident2, String tag2, int label2, int description2, int labelForSecondaryUsers2, int descriptionForSecondaryUsers2) {
            this.ident = ident2;
            this.tag = tag2;
            this.label = label2;
            this.description = description2;
            this.labelForSecondaryUsers = labelForSecondaryUsers2;
            this.descriptionForSecondaryUsers = descriptionForSecondaryUsers2;
        }
    }
}
