package android.telephony;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.carrier.CarrierIdentifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@SystemApi
public final class CarrierRestrictionRules implements Parcelable {
    public static final int CARRIER_RESTRICTION_DEFAULT_ALLOWED = 1;
    public static final int CARRIER_RESTRICTION_DEFAULT_NOT_ALLOWED = 0;
    public static final Parcelable.Creator<CarrierRestrictionRules> CREATOR = new Parcelable.Creator<CarrierRestrictionRules>() {
        /* class android.telephony.CarrierRestrictionRules.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CarrierRestrictionRules createFromParcel(Parcel in) {
            return new CarrierRestrictionRules(in);
        }

        @Override // android.os.Parcelable.Creator
        public CarrierRestrictionRules[] newArray(int size) {
            return new CarrierRestrictionRules[size];
        }
    };
    public static final int MULTISIM_POLICY_NONE = 0;
    public static final int MULTISIM_POLICY_ONE_VALID_SIM_MUST_BE_PRESENT = 1;
    private static final char WILD_CHARACTER = '?';
    private List<CarrierIdentifier> mAllowedCarriers;
    private int mCarrierRestrictionDefault;
    private List<CarrierIdentifier> mExcludedCarriers;
    private int mMultiSimPolicy;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CarrierRestrictionDefault {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MultiSimPolicy {
    }

    private CarrierRestrictionRules() {
        this.mAllowedCarriers = new ArrayList();
        this.mExcludedCarriers = new ArrayList();
        this.mCarrierRestrictionDefault = 0;
        this.mMultiSimPolicy = 0;
    }

    private CarrierRestrictionRules(Parcel in) {
        this.mAllowedCarriers = new ArrayList();
        this.mExcludedCarriers = new ArrayList();
        in.readTypedList(this.mAllowedCarriers, CarrierIdentifier.CREATOR);
        in.readTypedList(this.mExcludedCarriers, CarrierIdentifier.CREATOR);
        this.mCarrierRestrictionDefault = in.readInt();
        this.mMultiSimPolicy = in.readInt();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean isAllCarriersAllowed() {
        return this.mAllowedCarriers.isEmpty() && this.mExcludedCarriers.isEmpty() && this.mCarrierRestrictionDefault == 1;
    }

    public List<CarrierIdentifier> getAllowedCarriers() {
        return this.mAllowedCarriers;
    }

    public List<CarrierIdentifier> getExcludedCarriers() {
        return this.mExcludedCarriers;
    }

    public int getDefaultCarrierRestriction() {
        return this.mCarrierRestrictionDefault;
    }

    public int getMultiSimPolicy() {
        return this.mMultiSimPolicy;
    }

    public List<Boolean> areCarrierIdentifiersAllowed(List<CarrierIdentifier> carrierIds) {
        ArrayList<Boolean> result = new ArrayList<>(carrierIds.size());
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= carrierIds.size()) {
                break;
            }
            boolean inAllowedList = isCarrierIdInList(carrierIds.get(i), this.mAllowedCarriers);
            boolean inExcludedList = isCarrierIdInList(carrierIds.get(i), this.mExcludedCarriers);
            if (this.mCarrierRestrictionDefault == 0) {
                if (!inAllowedList || inExcludedList) {
                    z = false;
                }
                result.add(Boolean.valueOf(z));
            } else {
                if (inExcludedList && !inAllowedList) {
                    z = false;
                }
                result.add(Boolean.valueOf(z));
            }
            i++;
        }
        if (this.mMultiSimPolicy == 1) {
            Iterator<Boolean> it = result.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().booleanValue()) {
                        result.replaceAll($$Lambda$CarrierRestrictionRules$LmZXhiwgp1w_MAHEuZsMgdCVMiU.INSTANCE);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return result;
    }

    static /* synthetic */ Boolean lambda$areCarrierIdentifiersAllowed$0(Boolean x) {
        return true;
    }

    private static boolean isCarrierIdInList(CarrierIdentifier id, List<CarrierIdentifier> list) {
        for (CarrierIdentifier listItem : list) {
            if (patternMatch(id.getMcc(), listItem.getMcc()) && patternMatch(id.getMnc(), listItem.getMnc())) {
                String listItemValue = convertNullToEmpty(listItem.getSpn());
                String idValue = convertNullToEmpty(id.getSpn());
                if (listItemValue.isEmpty() || patternMatch(idValue, listItemValue)) {
                    String listItemValue2 = convertNullToEmpty(listItem.getImsi());
                    String idValue2 = convertNullToEmpty(id.getImsi());
                    if (!patternMatch(idValue2.substring(0, Math.min(idValue2.length(), listItemValue2.length())), listItemValue2)) {
                        continue;
                    } else {
                        String listItemValue3 = convertNullToEmpty(listItem.getGid1());
                        String idValue3 = convertNullToEmpty(id.getGid1());
                        if (!patternMatch(idValue3.substring(0, Math.min(idValue3.length(), listItemValue3.length())), listItemValue3)) {
                            continue;
                        } else {
                            String listItemValue4 = convertNullToEmpty(listItem.getGid2());
                            String idValue4 = convertNullToEmpty(id.getGid2());
                            if (patternMatch(idValue4.substring(0, Math.min(idValue4.length(), listItemValue4.length())), listItemValue4)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static String convertNullToEmpty(String value) {
        return Objects.toString(value, "");
    }

    private static boolean patternMatch(String str, String pattern) {
        if (str.length() != pattern.length()) {
            return false;
        }
        String lowerCaseStr = str.toLowerCase();
        String lowerCasePattern = pattern.toLowerCase();
        for (int i = 0; i < lowerCasePattern.length(); i++) {
            if (!(lowerCasePattern.charAt(i) == lowerCaseStr.charAt(i) || lowerCasePattern.charAt(i) == '?')) {
                return false;
            }
        }
        return true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(this.mAllowedCarriers);
        out.writeTypedList(this.mExcludedCarriers);
        out.writeInt(this.mCarrierRestrictionDefault);
        out.writeInt(this.mMultiSimPolicy);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "CarrierRestrictionRules(allowed:" + this.mAllowedCarriers + ", excluded:" + this.mExcludedCarriers + ", default:" + this.mCarrierRestrictionDefault + ", multisim policy:" + this.mMultiSimPolicy + ")";
    }

    public static final class Builder {
        private final CarrierRestrictionRules mRules = new CarrierRestrictionRules();

        public CarrierRestrictionRules build() {
            return this.mRules;
        }

        public Builder setAllCarriersAllowed() {
            this.mRules.mAllowedCarriers.clear();
            this.mRules.mExcludedCarriers.clear();
            this.mRules.mCarrierRestrictionDefault = 1;
            return this;
        }

        public Builder setAllowedCarriers(List<CarrierIdentifier> allowedCarriers) {
            this.mRules.mAllowedCarriers = new ArrayList(allowedCarriers);
            return this;
        }

        public Builder setExcludedCarriers(List<CarrierIdentifier> excludedCarriers) {
            this.mRules.mExcludedCarriers = new ArrayList(excludedCarriers);
            return this;
        }

        public Builder setDefaultCarrierRestriction(int carrierRestrictionDefault) {
            this.mRules.mCarrierRestrictionDefault = carrierRestrictionDefault;
            return this;
        }

        public Builder setMultiSimPolicy(int multiSimPolicy) {
            this.mRules.mMultiSimPolicy = multiSimPolicy;
            return this;
        }
    }
}
