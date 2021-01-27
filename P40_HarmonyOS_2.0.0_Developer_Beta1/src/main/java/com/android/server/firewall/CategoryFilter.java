package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import java.io.IOException;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class CategoryFilter implements Filter {
    private static final String ATTR_NAME = "name";
    public static final FilterFactory FACTORY = new FilterFactory("category") {
        /* class com.android.server.firewall.CategoryFilter.AnonymousClass1 */

        @Override // com.android.server.firewall.FilterFactory
        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            String categoryName = parser.getAttributeValue(null, "name");
            if (categoryName != null) {
                return new CategoryFilter(categoryName);
            }
            throw new XmlPullParserException("Category name must be specified.", parser, null);
        }
    };
    private final String mCategoryName;

    private CategoryFilter(String categoryName) {
        this.mCategoryName = categoryName;
    }

    @Override // com.android.server.firewall.Filter
    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        Set<String> categories = intent.getCategories();
        if (categories == null) {
            return false;
        }
        return categories.contains(this.mCategoryName);
    }
}
