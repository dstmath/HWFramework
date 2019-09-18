package java.lang;

import java.io.Serializable;
import java.util.Objects;

public final class StackTraceElement implements Serializable {
    private static final long serialVersionUID = 6992337162326171013L;
    private String declaringClass;
    private String fileName;
    private int lineNumber;
    private String methodName;

    public StackTraceElement(String declaringClass2, String methodName2, String fileName2, int lineNumber2) {
        this.declaringClass = (String) Objects.requireNonNull(declaringClass2, "Declaring class is null");
        this.methodName = (String) Objects.requireNonNull(methodName2, "Method name is null");
        this.fileName = fileName2;
        this.lineNumber = lineNumber2;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getClassName() {
        return this.declaringClass;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public boolean isNativeMethod() {
        return this.lineNumber == -2;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getClassName());
        result.append(".");
        result.append(this.methodName);
        if (isNativeMethod()) {
            result.append("(Native Method)");
        } else if (this.fileName != null) {
            if (this.lineNumber >= 0) {
                result.append("(");
                result.append(this.fileName);
                result.append(":");
                result.append(this.lineNumber);
                result.append(")");
            } else {
                result.append("(");
                result.append(this.fileName);
                result.append(")");
            }
        } else if (this.lineNumber >= 0) {
            result.append("(Unknown Source:");
            result.append(this.lineNumber);
            result.append(")");
        } else {
            result.append("(Unknown Source)");
        }
        return result.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackTraceElement)) {
            return false;
        }
        StackTraceElement e = (StackTraceElement) obj;
        if (!e.declaringClass.equals(this.declaringClass) || e.lineNumber != this.lineNumber || !Objects.equals(this.methodName, e.methodName) || !Objects.equals(this.fileName, e.fileName)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((this.declaringClass.hashCode() * 31) + this.methodName.hashCode())) + Objects.hashCode(this.fileName))) + this.lineNumber;
    }
}
