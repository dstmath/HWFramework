package ohos.utils.fastjson;

public enum PropertyNamingStrategy {
    CamelCase,
    PascalCase,
    SnakeCase,
    KebabCase;

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.utils.fastjson.PropertyNamingStrategy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy = new int[PropertyNamingStrategy.values().length];

        static {
            try {
                $SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.SnakeCase.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.KebabCase.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.PascalCase.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.CamelCase.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public String translate(String str) {
        char charAt;
        int i = AnonymousClass1.$SwitchMap$ohos$utils$fastjson$PropertyNamingStrategy[ordinal()];
        int i2 = 0;
        if (i == 1) {
            StringBuilder sb = new StringBuilder();
            while (i2 < str.length()) {
                char charAt2 = str.charAt(i2);
                if (charAt2 < 'A' || charAt2 > 'Z') {
                    sb.append(charAt2);
                } else {
                    char c = (char) (charAt2 + ' ');
                    if (i2 > 0) {
                        sb.append('_');
                    }
                    sb.append(c);
                }
                i2++;
            }
            return sb.toString();
        } else if (i == 2) {
            StringBuilder sb2 = new StringBuilder();
            while (i2 < str.length()) {
                char charAt3 = str.charAt(i2);
                if (charAt3 < 'A' || charAt3 > 'Z') {
                    sb2.append(charAt3);
                } else {
                    char c2 = (char) (charAt3 + ' ');
                    if (i2 > 0) {
                        sb2.append('-');
                    }
                    sb2.append(c2);
                }
                i2++;
            }
            return sb2.toString();
        } else if (i == 3) {
            char charAt4 = str.charAt(0);
            if (charAt4 < 'a' || charAt4 > 'z') {
                return str;
            }
            char[] charArray = str.toCharArray();
            charArray[0] = (char) (charArray[0] - ' ');
            return new String(charArray);
        } else if (i != 4 || (charAt = str.charAt(0)) < 'A' || charAt > 'Z') {
            return str;
        } else {
            char[] charArray2 = str.toCharArray();
            charArray2[0] = (char) (charArray2[0] + ' ');
            return new String(charArray2);
        }
    }
}
