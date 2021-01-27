package ohos.aafwk.utils.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class LogLabelBuilder {
    private static final LogLabel LABEL_INNER = new LogLabelBuilder().setTag("LogLabelBuilder").build();
    private static final int MAX_SUBMODULE_CNT = 6;
    static final String PKG_PREFIX = "ohos.aafwk.";
    private static final int STACK_OFFSET = 3;
    static final String TAG_DEFAULT = "Aafwk";
    static final String TAG_KEY = "AafwkKey";
    static final String TAG_KEY_BOUND = "AafwkKeyBound";
    private static final int TYPE_DEFAULT = 3;
    private static Map<String, Integer> pkgMap = new HashMap(6);
    private int domain = 218108672;
    private String tag = TAG_DEFAULT;
    private int type = 3;

    static {
        pkgMap.put("ohos.aafwk.ability", Integer.valueOf((int) LogDomain.ABILITY));
        pkgMap.put("ohos.aafwk.content", Integer.valueOf((int) LogDomain.CONTENT));
        pkgMap.put("ohos.aafwk.kits", Integer.valueOf((int) LogDomain.TEST));
        pkgMap.put("ohos.aafwk.abilityjet", Integer.valueOf((int) LogDomain.JET));
    }

    private static Optional<StackTraceElement> getCallerElm(StackTraceElement[] stackTraceElementArr) {
        if (stackTraceElementArr == null) {
            Log.error(LABEL_INNER, "trace is null", new Object[0]);
            return Optional.empty();
        }
        for (int i = 3; i < stackTraceElementArr.length; i++) {
            StackTraceElement stackTraceElement = stackTraceElementArr[i];
            String className = stackTraceElement.getClassName();
            if (!(className.equals(Log.class.getName()) || className.equals(LogLabelBuilder.class.getName()) || className.equals(LogLabel.class.getName()) || className.equals(ZLogger.class.getName()))) {
                return Optional.of(stackTraceElement);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getFullClassName() {
        return getCallerElm(Thread.currentThread().getStackTrace()).map($$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM.INSTANCE);
    }

    private static int getDomain(String str) {
        if (!str.startsWith(PKG_PREFIX)) {
            return 218108672;
        }
        int indexOf = str.indexOf(46, 11);
        if (indexOf < 0) {
            indexOf = str.length();
        }
        return pkgMap.getOrDefault(str.substring(0, indexOf), 218108672).intValue();
    }

    public LogLabelBuilder setType(int i) {
        if (i == 1 || i == 3) {
            this.type = i;
            return this;
        }
        throw new IllegalArgumentException("Illegal type for ability log label. type: " + i);
    }

    public LogLabelBuilder setDomain(int i) {
        if (i == 0 || (i >= 218108672 && i < 218108928)) {
            this.domain = i;
            return this;
        }
        throw new IllegalArgumentException("Illegal domain id for ability log label. domain: " + i);
    }

    public LogLabelBuilder setTag(String str) {
        U u;
        String str2 = "";
        if (str == null || str.isEmpty()) {
            Optional<String> fullClassName = getFullClassName();
            try {
                int intValue = fullClassName.map($$Lambda$LogLabelBuilder$LgUIzrii5LgUpx06ABMDZRI5EbI.INSTANCE).orElse((U) -1).intValue();
                U orElse = fullClassName.map(new Function(intValue) {
                    /* class ohos.aafwk.utils.log.$$Lambda$LogLabelBuilder$O0txMaw43826Biz5xA4nPG7PPmU */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return ((String) obj).substring(0, this.f$0);
                    }
                }).orElse(str2);
                u = fullClassName.map(new Function(intValue) {
                    /* class ohos.aafwk.utils.log.$$Lambda$LogLabelBuilder$ivOmrSKKcExyprkpxYn67h6Uotg */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return ((String) obj).substring(this.f$0 + 1);
                    }
                }).orElse(TAG_DEFAULT);
                str2 = orElse;
            } catch (StringIndexOutOfBoundsException unused) {
                u = TAG_DEFAULT;
            }
            setDomain(getDomain(str2));
            this.tag = u;
        } else {
            this.tag = str;
        }
        return this;
    }

    public LogLabel build() {
        if (!this.tag.equals(TAG_DEFAULT) || LogLabel.LABEL_DEF == null) {
            return new LogLabel(this.type, this.domain, this.tag);
        }
        Log.info(LABEL_INNER, "default label can not be rebuild", new Object[0]);
        return LogLabel.LABEL_DEF;
    }
}
