package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.PatternMatcher;
import com.android.server.wm.WindowManagerService.H;
import java.io.IOException;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

abstract class StringFilter implements Filter {
    public static final FilterFactory ACTION = null;
    private static final String ATTR_CONTAINS = "contains";
    private static final String ATTR_EQUALS = "equals";
    private static final String ATTR_IS_NULL = "isNull";
    private static final String ATTR_PATTERN = "pattern";
    private static final String ATTR_REGEX = "regex";
    private static final String ATTR_STARTS_WITH = "startsWith";
    public static final ValueProvider COMPONENT = null;
    public static final ValueProvider COMPONENT_NAME = null;
    public static final ValueProvider COMPONENT_PACKAGE = null;
    public static final ValueProvider DATA = null;
    public static final ValueProvider HOST = null;
    public static final ValueProvider MIME_TYPE = null;
    public static final ValueProvider PATH = null;
    public static final ValueProvider SCHEME = null;
    public static final ValueProvider SSP = null;
    private final ValueProvider mValueProvider;

    private static abstract class ValueProvider extends FilterFactory {
        public abstract String getValue(ComponentName componentName, Intent intent, String str);

        protected ValueProvider(String tag) {
            super(tag);
        }

        public Filter newFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
            return StringFilter.readFromXml(this, parser);
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.10 */
    static class AnonymousClass10 extends ValueProvider {
        AnonymousClass10(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            Uri data = intent.getData();
            if (data != null) {
                return data.getPath();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.1 */
    static class AnonymousClass1 extends ValueProvider {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            if (resolvedComponent != null) {
                return resolvedComponent.flattenToString();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.2 */
    static class AnonymousClass2 extends ValueProvider {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            if (resolvedComponent != null) {
                return resolvedComponent.getClassName();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.3 */
    static class AnonymousClass3 extends ValueProvider {
        AnonymousClass3(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            if (resolvedComponent != null) {
                return resolvedComponent.getPackageName();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.4 */
    static class AnonymousClass4 extends ValueProvider {
        AnonymousClass4(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            return intent.getAction();
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.5 */
    static class AnonymousClass5 extends ValueProvider {
        AnonymousClass5(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            Uri data = intent.getData();
            if (data != null) {
                return data.toString();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.6 */
    static class AnonymousClass6 extends ValueProvider {
        AnonymousClass6(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            return resolvedType;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.7 */
    static class AnonymousClass7 extends ValueProvider {
        AnonymousClass7(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            Uri data = intent.getData();
            if (data != null) {
                return data.getScheme();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.8 */
    static class AnonymousClass8 extends ValueProvider {
        AnonymousClass8(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            Uri data = intent.getData();
            if (data != null) {
                return data.getSchemeSpecificPart();
            }
            return null;
        }
    }

    /* renamed from: com.android.server.firewall.StringFilter.9 */
    static class AnonymousClass9 extends ValueProvider {
        AnonymousClass9(String $anonymous0) {
            super($anonymous0);
        }

        public String getValue(ComponentName resolvedComponent, Intent intent, String resolvedType) {
            Uri data = intent.getData();
            if (data != null) {
                return data.getHost();
            }
            return null;
        }
    }

    private static class ContainsFilter extends StringFilter {
        private final String mFilterValue;

        public ContainsFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mFilterValue = attrValue;
        }

        public boolean matchesValue(String value) {
            return value != null ? value.contains(this.mFilterValue) : false;
        }
    }

    private static class EqualsFilter extends StringFilter {
        private final String mFilterValue;

        public EqualsFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mFilterValue = attrValue;
        }

        public boolean matchesValue(String value) {
            return value != null ? value.equals(this.mFilterValue) : false;
        }
    }

    private static class IsNullFilter extends StringFilter {
        private final boolean mIsNull;

        public IsNullFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mIsNull = Boolean.parseBoolean(attrValue);
        }

        public IsNullFilter(ValueProvider valueProvider, boolean isNull) {
            super(null);
            this.mIsNull = isNull;
        }

        public boolean matchesValue(String value) {
            return (value == null) == this.mIsNull;
        }
    }

    private static class PatternStringFilter extends StringFilter {
        private final PatternMatcher mPattern;

        public PatternStringFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mPattern = new PatternMatcher(attrValue, 2);
        }

        public boolean matchesValue(String value) {
            return value != null ? this.mPattern.match(value) : false;
        }
    }

    private static class RegexFilter extends StringFilter {
        private final Pattern mPattern;

        public RegexFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mPattern = Pattern.compile(attrValue);
        }

        public boolean matchesValue(String value) {
            return value != null ? this.mPattern.matcher(value).matches() : false;
        }
    }

    private static class StartsWithFilter extends StringFilter {
        private final String mFilterValue;

        public StartsWithFilter(ValueProvider valueProvider, String attrValue) {
            super(null);
            this.mFilterValue = attrValue;
        }

        public boolean matchesValue(String value) {
            return value != null ? value.startsWith(this.mFilterValue) : false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.firewall.StringFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.firewall.StringFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.firewall.StringFilter.<clinit>():void");
    }

    protected abstract boolean matchesValue(String str);

    private StringFilter(ValueProvider valueProvider) {
        this.mValueProvider = valueProvider;
    }

    public static StringFilter readFromXml(ValueProvider valueProvider, XmlPullParser parser) throws IOException, XmlPullParserException {
        StringFilter filter = null;
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            StringFilter newFilter = getFilter(valueProvider, parser, i);
            if (newFilter != null) {
                if (filter != null) {
                    throw new XmlPullParserException("Multiple string filter attributes found");
                }
                filter = newFilter;
            }
        }
        if (filter == null) {
            return new IsNullFilter(valueProvider, false);
        }
        return filter;
    }

    private static StringFilter getFilter(ValueProvider valueProvider, XmlPullParser parser, int attributeIndex) {
        String attributeName = parser.getAttributeName(attributeIndex);
        switch (attributeName.charAt(0)) {
            case HdmiCecKeycode.CEC_KEYCODE_PAUSE_RECORD_FUNCTION /*99*/:
                if (attributeName.equals(ATTR_CONTAINS)) {
                    return new ContainsFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            case H.KEYGUARD_DISMISS_DONE /*101*/:
                if (attributeName.equals(ATTR_EQUALS)) {
                    return new EqualsFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            case HdmiCecKeycode.CEC_KEYCODE_SELECT_AV_INPUT_FUNCTION /*105*/:
                if (attributeName.equals(ATTR_IS_NULL)) {
                    return new IsNullFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            case HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE /*112*/:
                if (attributeName.equals(ATTR_PATTERN)) {
                    return new PatternStringFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            case HdmiCecKeycode.CEC_KEYCODE_F2_RED /*114*/:
                if (attributeName.equals(ATTR_REGEX)) {
                    return new RegexFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            case HdmiCecKeycode.CEC_KEYCODE_F3_GREEN /*115*/:
                if (attributeName.equals(ATTR_STARTS_WITH)) {
                    return new StartsWithFilter(valueProvider, parser.getAttributeValue(attributeIndex));
                }
                return null;
            default:
                return null;
        }
    }

    public boolean matches(IntentFirewall ifw, ComponentName resolvedComponent, Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        return matchesValue(this.mValueProvider.getValue(resolvedComponent, intent, resolvedType));
    }
}
