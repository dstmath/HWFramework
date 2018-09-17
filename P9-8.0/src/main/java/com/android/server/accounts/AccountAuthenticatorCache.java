package com.android.server.accounts;

import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.R;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class AccountAuthenticatorCache extends RegisteredServicesCache<AuthenticatorDescription> implements IAccountAuthenticatorCache {
    private static final String TAG = "Account";
    private static final MySerializer sSerializer = new MySerializer();

    private static class MySerializer implements XmlSerializerAndParser<AuthenticatorDescription> {
        /* synthetic */ MySerializer(MySerializer -this0) {
            this();
        }

        private MySerializer() {
        }

        public void writeAsXml(AuthenticatorDescription item, XmlSerializer out) throws IOException {
            out.attribute(null, SoundModelContract.KEY_TYPE, item.type);
        }

        public AuthenticatorDescription createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return AuthenticatorDescription.newKey(parser.getAttributeValue(null, SoundModelContract.KEY_TYPE));
        }
    }

    public AccountAuthenticatorCache(Context context) {
        super(context, "android.accounts.AccountAuthenticator", "android.accounts.AccountAuthenticator", "account-authenticator", sSerializer);
    }

    public AuthenticatorDescription parseServiceAttributes(Resources res, String packageName, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AccountAuthenticator);
        try {
            String accountType = sa.getString(2);
            int labelId = sa.getResourceId(0, 0);
            int iconId = sa.getResourceId(1, 0);
            int smallIconId = sa.getResourceId(3, 0);
            int prefId = sa.getResourceId(4, 0);
            boolean customTokens = sa.getBoolean(5, false);
            if (TextUtils.isEmpty(accountType)) {
                return null;
            }
            AuthenticatorDescription authenticatorDescription = new AuthenticatorDescription(accountType, packageName, labelId, iconId, smallIconId, prefId, customTokens);
            sa.recycle();
            return authenticatorDescription;
        } finally {
            sa.recycle();
        }
    }

    public /* bridge */ /* synthetic */ ServiceInfo getServiceInfo(AuthenticatorDescription authenticatorDescription, int i) {
        return getServiceInfo(authenticatorDescription, i);
    }
}
