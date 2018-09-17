package java.lang;

import java.io.Serializable;
import java.util.Objects;

public final class StackTraceElement implements Serializable {
    private static final long serialVersionUID = 6992337162326171013L;
    private String declaringClass;
    private String fileName;
    private int lineNumber;
    private String methodName;

    public StackTraceElement(String declaringClass, String methodName, String fileName, int lineNumber) {
        this.declaringClass = (String) Objects.requireNonNull((Object) declaringClass, "Declaring class is null");
        this.methodName = (String) Objects.requireNonNull((Object) methodName, "Method name is null");
        this.fileName = fileName;
        this.lineNumber = lineNumber;
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
        result.append(getClassName()).append(".").append(this.methodName);
        if (isNativeMethod()) {
            result.append("(Native Method)");
        } else if (this.fileName != null) {
            if (this.lineNumber >= 0) {
                result.append("(").append(this.fileName).append(":").append(this.lineNumber).append(")");
            } else {
                result.append("(").append(this.fileName).append(")");
            }
        } else if (this.lineNumber >= 0) {
            result.append("(Unknown Source:").append(this.lineNumber).append(")");
        } else {
            result.append("(Unknown Source)");
        }
        return result.toString();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackTraceElement)) {
            return false;
        }
        StackTraceElement e = (StackTraceElement) obj;
        if (e.declaringClass.equals(this.declaringClass) && e.lineNumber == this.lineNumber && Objects.equals(this.methodName, e.methodName)) {
            z = Objects.equals(this.fileName, e.fileName);
        }
        return z;
    }

    public int hashCode() {
        return (((((this.declaringClass.hashCode() * 31) + this.methodName.hashCode()) * 31) + Objects.hashCode(this.fileName)) * 31) + this.lineNumber;
    }
}
