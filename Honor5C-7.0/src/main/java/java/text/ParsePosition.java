package java.text;

public class ParsePosition {
    int errorIndex;
    int index;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ParsePosition(int index) {
        this.index = 0;
        this.errorIndex = -1;
        this.index = index;
    }

    public void setErrorIndex(int ei) {
        this.errorIndex = ei;
    }

    public int getErrorIndex() {
        return this.errorIndex;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof ParsePosition)) {
            return false;
        }
        ParsePosition other = (ParsePosition) obj;
        if (this.index == other.index && this.errorIndex == other.errorIndex) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.errorIndex << 16) | this.index;
    }

    public String toString() {
        return getClass().getName() + "[index=" + this.index + ",errorIndex=" + this.errorIndex + ']';
    }
}
