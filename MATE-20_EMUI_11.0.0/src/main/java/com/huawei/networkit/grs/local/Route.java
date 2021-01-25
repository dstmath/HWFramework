package com.huawei.networkit.grs.local;

import android.text.TextUtils;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.common.Logger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Route {
    public static final String ISSUE_COUNTRY_POLICY = "issue_country";
    public static final String NO_ROUTEBY_COUNTRY = "no_route_country";
    public static final String NO_ROUTE_COUNTRYGROUPID = "no-country";
    public static final String NO_ROUTE_POLICY = "no_route";
    public static final String REG_COUNTRY_POLICY = "reg_country";
    public static final Set<String> ROUTE_BY_TYPE_SET = Collections.unmodifiableSet(new HashSet<String>(16) {
        /* class com.huawei.networkit.grs.local.Route.AnonymousClass1 */
        private static final long serialVersionUID = -5601289263249416904L;

        {
            add(Route.SER_COUNTRY_POLICY);
            add(Route.REG_COUNTRY_POLICY);
            add(Route.ISSUE_COUNTRY_POLICY);
        }
    });
    public static final String ROUTE_TYPE_SPLIT = ">";
    public static final String SER_COUNTRY_POLICY = "ser_country";
    private static final String TAG = Route.class.getSimpleName();
    public static final String UNCONDITIONAL_POLICY = "unconditional";

    public static String getRouteCountry(String routeBy, GrsBaseInfo grsBaseInfo) {
        if (TextUtils.isEmpty(routeBy)) {
            Logger.w(TAG, "routeBy must be not empty string or null.");
            return null;
        } else if (!NO_ROUTE_POLICY.equals(routeBy) && !UNCONDITIONAL_POLICY.equals(routeBy)) {
            return getCountryByRouteType(routeBy, grsBaseInfo);
        } else {
            Logger.v(TAG, "routeBy equals NO_ROUTE_POLICY");
            return NO_ROUTEBY_COUNTRY;
        }
    }

    private static String getCountryByRouteType(String routeBy, GrsBaseInfo grsBaseInfo) {
        String serCountry = grsBaseInfo.getSerCountry();
        String regCountry = grsBaseInfo.getRegCountry();
        String issueCountry = grsBaseInfo.getIssueCountry();
        String[] groupRoutes = routeBy.split(ROUTE_TYPE_SPLIT);
        for (String groupRoute : groupRoutes) {
            if (ROUTE_BY_TYPE_SET.contains(groupRoute.trim())) {
                if (SER_COUNTRY_POLICY.equals(groupRoute.trim()) && !TextUtils.isEmpty(serCountry)) {
                    return serCountry;
                }
                if (REG_COUNTRY_POLICY.equals(groupRoute.trim()) && !TextUtils.isEmpty(regCountry)) {
                    return regCountry;
                }
                if (ISSUE_COUNTRY_POLICY.equals(groupRoute.trim()) && !TextUtils.isEmpty(issueCountry)) {
                    return issueCountry;
                }
            }
        }
        return "";
    }
}
