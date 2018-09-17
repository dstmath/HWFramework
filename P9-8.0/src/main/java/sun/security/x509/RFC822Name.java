package sun.security.x509;

import java.io.IOException;
import java.util.Locale;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class RFC822Name implements GeneralNameInterface {
    private String name;

    public RFC822Name(DerValue derValue) throws IOException {
        this.name = derValue.getIA5String();
        parseName(this.name);
    }

    public RFC822Name(String name) throws IOException {
        parseName(name);
        this.name = name;
    }

    public void parseName(String name) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IOException("RFC822Name may not be null or empty");
        }
        String domain = name.substring(name.indexOf(64) + 1);
        if (domain.length() == 0) {
            throw new IOException("RFC822Name may not end with @");
        } else if (domain.startsWith(".") && domain.length() == 1) {
            throw new IOException("RFC822Name domain may not be just .");
        }
    }

    public int getType() {
        return 1;
    }

    public String getName() {
        return this.name;
    }

    public void encode(DerOutputStream out) throws IOException {
        out.putIA5String(this.name);
    }

    public String toString() {
        return "RFC822Name: " + this.name;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RFC822Name)) {
            return false;
        }
        return this.name.equalsIgnoreCase(((RFC822Name) obj).name);
    }

    public int hashCode() {
        return this.name.toUpperCase(Locale.ENGLISH).hashCode();
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 1) {
            return -1;
        }
        String inName = ((RFC822Name) inputName).getName().toLowerCase(Locale.ENGLISH);
        String thisName = this.name.toLowerCase(Locale.ENGLISH);
        if (inName.equals(thisName)) {
            return 0;
        }
        if (thisName.endsWith(inName)) {
            if (inName.indexOf(64) != -1) {
                return 3;
            }
            if (inName.startsWith(".")) {
                return 2;
            }
            if (thisName.charAt(thisName.lastIndexOf(inName) - 1) == '@') {
                return 2;
            }
            return 3;
        } else if (!inName.endsWith(thisName)) {
            return 3;
        } else {
            if (thisName.indexOf(64) != -1) {
                return 3;
            }
            if (thisName.startsWith(".")) {
                return 1;
            }
            if (inName.charAt(inName.lastIndexOf(thisName) - 1) == '@') {
                return 1;
            }
            return 3;
        }
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        String subtree = this.name;
        int i = 1;
        int atNdx = subtree.lastIndexOf(64);
        if (atNdx >= 0) {
            i = 2;
            subtree = subtree.substring(atNdx + 1);
        }
        while (subtree.lastIndexOf(46) >= 0) {
            subtree = subtree.substring(0, subtree.lastIndexOf(46));
            i++;
        }
        return i;
    }
}
