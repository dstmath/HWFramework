package android.provider;

import android.content.ComponentName;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import com.android.internal.util.ArrayUtils;
import java.util.Locale;

public class SettingsValidators {
    public static final Validator ANY_INTEGER_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    };
    public static final Validator ANY_STRING_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            return true;
        }
    };
    public static final Validator BOOLEAN_VALIDATOR = new DiscreteValueValidator(new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1"});
    public static final Validator COMPONENT_NAME_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            return (value == null || ComponentName.unflattenFromString(value) == null) ? false : true;
        }
    };
    public static final Validator LENIENT_IP_ADDRESS_VALIDATOR = new Validator() {
        private static final int MAX_IPV6_LENGTH = 45;

        public boolean validate(String value) {
            boolean z = false;
            if (value == null) {
                return false;
            }
            if (value.length() <= 45) {
                z = true;
            }
            return z;
        }
    };
    public static final Validator LOCALE_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            if (value == null) {
                return false;
            }
            for (Locale locale : Locale.getAvailableLocales()) {
                if (value.equals(locale.toString())) {
                    return true;
                }
            }
            return false;
        }
    };
    public static final Validator NON_NEGATIVE_INTEGER_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            boolean z = false;
            try {
                if (Integer.parseInt(value) >= 0) {
                    z = true;
                }
                return z;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    };
    public static final Validator NULLABLE_COMPONENT_NAME_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            return value == null || SettingsValidators.COMPONENT_NAME_VALIDATOR.validate(value);
        }
    };
    public static final Validator PACKAGE_NAME_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            return value != null && isStringPackageName(value);
        }

        private boolean isStringPackageName(String value) {
            if (value == null) {
                return false;
            }
            boolean isValidPackageName = true;
            for (String subpart : value.split("\\.")) {
                isValidPackageName &= isSubpartValidForPackageName(subpart);
                if (!isValidPackageName) {
                    break;
                }
            }
            return isValidPackageName;
        }

        private boolean isSubpartValidForPackageName(String subpart) {
            if (subpart.length() == 0) {
                return false;
            }
            boolean isValidSubpart = Character.isLetter(subpart.charAt(0));
            for (int i = 1; i < subpart.length(); i++) {
                isValidSubpart &= Character.isLetterOrDigit(subpart.charAt(i)) || subpart.charAt(i) == '_';
                if (!isValidSubpart) {
                    break;
                }
            }
            return isValidSubpart;
        }
    };
    public static final Validator URI_VALIDATOR = new Validator() {
        public boolean validate(String value) {
            try {
                Uri.decode(value);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    };

    public static final class ComponentNameListValidator implements Validator {
        private final String mSeparator;

        public ComponentNameListValidator(String separator) {
            this.mSeparator = separator;
        }

        public boolean validate(String value) {
            if (value == null) {
                return false;
            }
            for (String element : value.split(this.mSeparator)) {
                if (!SettingsValidators.COMPONENT_NAME_VALIDATOR.validate(element)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class DiscreteValueValidator implements Validator {
        private final String[] mValues;

        public DiscreteValueValidator(String[] values) {
            this.mValues = values;
        }

        public boolean validate(String value) {
            return ArrayUtils.contains(this.mValues, value);
        }
    }

    public static final class InclusiveFloatRangeValidator implements Validator {
        private final float mMax;
        private final float mMin;

        public InclusiveFloatRangeValidator(float min, float max) {
            this.mMin = min;
            this.mMax = max;
        }

        public boolean validate(String value) {
            boolean z = false;
            try {
                float floatValue = Float.parseFloat(value);
                if (floatValue >= this.mMin && floatValue <= this.mMax) {
                    z = true;
                }
                return z;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        }
    }

    public static final class InclusiveIntegerRangeValidator implements Validator {
        private final int mMax;
        private final int mMin;

        public InclusiveIntegerRangeValidator(int min, int max) {
            this.mMin = min;
            this.mMax = max;
        }

        public boolean validate(String value) {
            boolean z = false;
            try {
                int intValue = Integer.parseInt(value);
                if (intValue >= this.mMin && intValue <= this.mMax) {
                    z = true;
                }
                return z;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static final class PackageNameListValidator implements Validator {
        private final String mSeparator;

        public PackageNameListValidator(String separator) {
            this.mSeparator = separator;
        }

        public boolean validate(String value) {
            if (value == null) {
                return false;
            }
            for (String element : value.split(this.mSeparator)) {
                if (!SettingsValidators.PACKAGE_NAME_VALIDATOR.validate(element)) {
                    return false;
                }
            }
            return true;
        }
    }

    public interface Validator {
        boolean validate(String str);
    }
}
