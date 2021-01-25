package ohos.aafwk.content;

import java.util.regex.Pattern;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PatternsMatcher implements Sequenceable {
    public static final Sequenceable.Producer<PatternsMatcher> PRODUCER = $$Lambda$PatternsMatcher$2JO6Ks_75UUJqaiLLcgzRLauqg.INSTANCE;
    private MatchType matchType;
    private String pattern;
    private Pattern patternMatcher;
    private String start = "\\*";

    static /* synthetic */ PatternsMatcher lambda$static$0(Parcel parcel) {
        PatternsMatcher patternsMatcher = new PatternsMatcher();
        patternsMatcher.unmarshalling(parcel);
        return patternsMatcher;
    }

    public enum MatchType {
        DEFAULT(0),
        PREFIX(1),
        PATTERN(2),
        GLOBAL(3);
        
        private int value;

        private MatchType(int i) {
            this.value = i;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getValue() {
            return this.value;
        }
    }

    private PatternsMatcher() {
    }

    public PatternsMatcher(String str, MatchType matchType2) {
        this.pattern = str;
        if (matchType2 != null) {
            this.matchType = matchType2;
            if (matchType2 == MatchType.PATTERN && str != null) {
                this.patternMatcher = Pattern.compile(str);
            }
        } else if (str == null || !this.pattern.contains("*")) {
            this.matchType = MatchType.DEFAULT;
        } else {
            this.matchType = MatchType.GLOBAL;
        }
    }

    public PatternsMatcher(String str) {
        this.pattern = str;
        if (str == null || !this.pattern.contains("*")) {
            this.matchType = MatchType.DEFAULT;
        } else {
            this.matchType = MatchType.GLOBAL;
        }
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean match(String str) {
        if (str == null) {
            return this.pattern == null;
        }
        if (this.pattern == null) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType[this.matchType.ordinal()];
        if (i == 1) {
            return this.pattern.equals(str);
        }
        if (i == 2) {
            return str.startsWith(this.pattern);
        }
        if (i == 3) {
            return this.patternMatcher.matcher(str).matches();
        }
        if (i != 4) {
            return false;
        }
        return globalMatch(str);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.aafwk.content.PatternsMatcher$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType = new int[MatchType.values().length];

        static {
            try {
                $SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType[MatchType.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType[MatchType.PREFIX.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType[MatchType.PATTERN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$aafwk$content$PatternsMatcher$MatchType[MatchType.GLOBAL.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PatternsMatcher)) {
            return false;
        }
        PatternsMatcher patternsMatcher = (PatternsMatcher) obj;
        String str = this.pattern;
        if (str == null) {
            if (patternsMatcher.pattern == null && this.matchType == patternsMatcher.matchType) {
                return true;
            }
            return false;
        } else if (!str.equals(patternsMatcher.pattern) || this.matchType != patternsMatcher.matchType) {
            return false;
        } else {
            return true;
        }
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.pattern);
        parcel.writeInt(this.matchType.value);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.pattern = parcel.readString();
        int readInt = parcel.readInt();
        if (readInt < MatchType.DEFAULT.getValue() || readInt > MatchType.GLOBAL.getValue()) {
            return false;
        }
        this.matchType = MatchType.values()[readInt];
        if (this.matchType != MatchType.PATTERN) {
            return true;
        }
        this.patternMatcher = Pattern.compile(this.pattern);
        return true;
    }

    private boolean globalMatch(String str) {
        String str2 = this.pattern;
        if (str2 == null) {
            return str == null;
        }
        if (str == null) {
            return str2 == null;
        }
        String[] split = str2.split(this.start);
        int i = 0;
        for (String str3 : split) {
            if (!str3.isEmpty()) {
                int indexOf = str.indexOf(str3);
                if (indexOf == -1) {
                    return false;
                }
                i = indexOf + str3.length();
            }
        }
        return str.length() <= i || this.pattern.endsWith("*");
    }
}
