package java.util.regex;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public class PatternSyntaxException extends IllegalArgumentException {
    private static final String nl = ((String) AccessController.doPrivileged(new GetPropertyAction("line.separator")));
    private static final long serialVersionUID = -3864639126226059218L;
    private final String desc;
    private final int index;
    private final String pattern;

    public PatternSyntaxException(String desc2, String regex, int index2) {
        this.desc = desc2;
        this.pattern = regex;
        this.index = index2;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.desc);
        if (this.index >= 0) {
            sb.append(" near index ");
            sb.append(this.index);
        }
        sb.append(nl);
        sb.append(this.pattern);
        if (this.index >= 0) {
            sb.append(nl);
            for (int i = 0; i < this.index; i++) {
                sb.append(' ');
            }
            sb.append('^');
        }
        return sb.toString();
    }
}
