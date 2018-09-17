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
    private static final MySerializer sSerializer = null;

    private static class MySerializer implements XmlSerializerAndParser<AuthenticatorDescription> {
        private MySerializer() {
        }

        public void writeAsXml(AuthenticatorDescription item, XmlSerializer out) throws IOException {
            out.attribute(null, SoundModelContract.KEY_TYPE, item.type);
        }

        public AuthenticatorDescription createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return AuthenticatorDescription.newKey(parser.getAttributeValue(null, SoundModelContract.KEY_TYPE));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accounts.AccountAuthenticatorCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accounts.AccountAuthenticatorCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountAuthenticatorCache.<clinit>():void");
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

    public /* bridge */ /* synthetic */ ServiceInfo getServiceInfo(AuthenticatorDescription type, int userId) {
        return getServiceInfo(type, userId);
    }
}
