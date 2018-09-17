package android.app;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.rms.iaware.DataContract.BaseAttr;
import android.security.KeyChain;
import android.security.keymaster.KeymasterDefs;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AliasActivity extends Activity {
    public final String ALIAS_META_DATA;

    public AliasActivity() {
        this.ALIAS_META_DATA = "android.app.alias";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = getPackageManager().getActivityInfo(getComponentName(), KeymasterDefs.KM_ALGORITHM_HMAC).loadXmlMetaData(getPackageManager(), "android.app.alias");
            if (xmlResourceParser == null) {
                throw new RuntimeException("Alias requires a meta-data field android.app.alias");
            }
            Intent intent = parseAlias(xmlResourceParser);
            if (intent == null) {
                throw new RuntimeException("No <intent> tag found in alias description");
            }
            startActivity(intent);
            finish();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Error parsing alias", e);
        } catch (XmlPullParserException e2) {
            throw new RuntimeException("Error parsing alias", e2);
        } catch (IOException e3) {
            throw new RuntimeException("Error parsing alias", e3);
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    return intent;
                }
                if (!(type == 3 || type == 4)) {
                    if (BaseAttr.INTENT.equals(parser.getName())) {
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
