package android.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.security.KeyChain;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AliasActivity extends Activity {
    public final String ALIAS_META_DATA = "android.app.alias";

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = getPackageManager().getActivityInfo(getComponentName(), 128).loadXmlMetaData(getPackageManager(), "android.app.alias");
            if (parser2 != null) {
                Intent intent = parseAlias(parser2);
                if (intent != null) {
                    startActivity(intent);
                    finish();
                    parser2.close();
                    return;
                }
                throw new RuntimeException("No <intent> tag found in alias description");
            }
            throw new RuntimeException("Alias requires a meta-data field android.app.alias");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Error parsing alias", e);
        } catch (XmlPullParserException e2) {
            throw new RuntimeException("Error parsing alias", e2);
        } catch (IOException e3) {
            throw new RuntimeException("Error parsing alias", e3);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private Intent parseAlias(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Intent intent = null;
        do {
            type = parser.next();
            if (type == 1) {
                break;
            }
        } while (type != 2);
        String nodeName = parser.getName();
        if (KeyChain.EXTRA_ALIAS.equals(nodeName)) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (!(type2 == 3 || type2 == 4)) {
                    if ("intent".equals(parser.getName())) {
                        Intent gotIntent = Intent.parseIntent(getResources(), parser, attrs);
                        if (intent == null) {
                            intent = gotIntent;
                        }
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            return intent;
        }
        throw new RuntimeException("Alias meta-data must start with <alias> tag; found" + nodeName + " at " + parser.getPositionDescription());
    }
}
