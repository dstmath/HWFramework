package com.huawei.i18n.tmr.address;

import com.huawei.i18n.tmr.address.en.SearchEngine;
import java.util.Locale;

public class AddressTmr {
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005b, code lost:
        if (r0.equals("en") != false) goto L_0x0069;
     */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f  */
    public static int[] getAddress(String txtMessage) {
        String locale = Locale.getDefault().getLanguage();
        char c = 0;
        int[] result = new int[0];
        int hashCode = locale.hashCode();
        if (hashCode != 3201) {
            if (hashCode != 3241) {
                if (hashCode != 3246) {
                    if (hashCode != 3276) {
                        if (hashCode != 3371) {
                            if (hashCode == 3588 && locale.equals("pt")) {
                                c = 5;
                                if (c != 0) {
                                    return SearchEngine.getInstance().search(txtMessage);
                                }
                                if (c == 1) {
                                    return com.huawei.i18n.tmr.address.de.SearchEngine.search(txtMessage);
                                }
                                if (c == 2) {
                                    return com.huawei.i18n.tmr.address.es.SearchEngine.search(txtMessage);
                                }
                                if (c == 3) {
                                    return com.huawei.i18n.tmr.address.it.SearchEngine.search(txtMessage);
                                }
                                if (c == 4) {
                                    return com.huawei.i18n.tmr.address.fr.SearchEngine.search(txtMessage);
                                }
                                if (c != 5) {
                                    return result;
                                }
                                return com.huawei.i18n.tmr.address.pt.SearchEngine.search(txtMessage);
                            }
                        } else if (locale.equals("it")) {
                            c = 3;
                            if (c != 0) {
                            }
                        }
                    } else if (locale.equals("fr")) {
                        c = 4;
                        if (c != 0) {
                        }
                    }
                } else if (locale.equals("es")) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            }
        } else if (locale.equals("de")) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
