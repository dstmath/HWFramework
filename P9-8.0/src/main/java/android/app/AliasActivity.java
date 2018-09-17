package android.app;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AliasActivity extends Activity {
    public final String ALIAS_META_DATA = "android.app.alias";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XmlResourceParser parser = null;
        try {
            parser = getPackageManager().getActivityInfo(getComponentName(), 128).loadXmlMetaData(getPackageManager(), "android.app.alias");
            if (parser == null) {
                throw new RuntimeException("Alias requires a meta-data field android.app.alias");
            }
            Intent intent = parseAlias(parser);
            if (intent == null) {
                throw new RuntimeException("No <intent> tag found in alias description");
            }
            startActivity(intent);
            finish();
            if (parser != null) {
                parser.close();
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Error parsing alias", e);
        } catch (XmlPullParserException e2) {
            throw new RuntimeException("Error parsing alias", e2);
        } catch (IOException e3) {
            throw new RuntimeException("Error parsing alias", e3);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
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
        if ("alias".equals(nodeName)) {
            int outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    return intent;
                }
                if (!(type == 3 || type == 4)) {
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
