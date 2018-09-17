package org.apache.xalan;

import org.apache.xalan.templates.Constants;

public class Version {
    public static String getVersion() {
        return getProduct() + " " + getImplementationLanguage() + " " + getMajorVersionNum() + Constants.ATTRVAL_THIS + getReleaseVersionNum() + Constants.ATTRVAL_THIS + (getDevelopmentVersionNum() > 0 ? "D" + getDevelopmentVersionNum() : "" + getMaintenanceVersionNum());
    }

    public static void main(String[] argv) {
        System.out.println(getVersion());
    }

    public static String getProduct() {
        return "Xalan";
    }

    public static String getImplementationLanguage() {
        return "Java";
    }

    public static int getMajorVersionNum() {
        return 2;
    }

    public static int getReleaseVersionNum() {
        return 7;
    }

    public static int getMaintenanceVersionNum() {
        return 1;
    }

    public static int getDevelopmentVersionNum() {
        try {
            if (new String("").length() == 0) {
                return 0;
            }
            return Integer.parseInt("");
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
