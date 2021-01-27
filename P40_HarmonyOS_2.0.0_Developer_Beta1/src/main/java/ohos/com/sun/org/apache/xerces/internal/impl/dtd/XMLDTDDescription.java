package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class XMLDTDDescription extends XMLResourceIdentifierImpl implements ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription {
    protected ArrayList fPossibleRoots = null;
    protected String fRootName = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription
    public String getGrammarType() {
        return XMLGrammarDescription.XML_DTD;
    }

    public XMLDTDDescription(XMLResourceIdentifier xMLResourceIdentifier, String str) {
        setValues(xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId(), xMLResourceIdentifier.getExpandedSystemId());
        this.fRootName = str;
        this.fPossibleRoots = null;
    }

    public XMLDTDDescription(String str, String str2, String str3, String str4, String str5) {
        setValues(str, str2, str3, str4);
        this.fRootName = str5;
        this.fPossibleRoots = null;
    }

    public XMLDTDDescription(XMLInputSource xMLInputSource) {
        setValues(xMLInputSource.getPublicId(), null, xMLInputSource.getBaseSystemId(), xMLInputSource.getSystemId());
        this.fRootName = null;
        this.fPossibleRoots = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription
    public String getRootName() {
        return this.fRootName;
    }

    public void setRootName(String str) {
        this.fRootName = str;
        this.fPossibleRoots = null;
    }

    public void setPossibleRoots(ArrayList arrayList) {
        this.fPossibleRoots = arrayList;
    }

    public void setPossibleRoots(Vector vector) {
        this.fPossibleRoots = vector != null ? new ArrayList(vector) : null;
    }

    public boolean equals(Object obj) {
        if (!((obj instanceof XMLGrammarDescription) && getGrammarType().equals(((XMLGrammarDescription) obj).getGrammarType()))) {
            return false;
        }
        XMLDTDDescription xMLDTDDescription = (XMLDTDDescription) obj;
        String str = this.fRootName;
        if (str != null) {
            String str2 = xMLDTDDescription.fRootName;
            if (!(str2 == null || str2.equals(str))) {
                return false;
            }
            ArrayList arrayList = xMLDTDDescription.fPossibleRoots;
            if (arrayList != null && !arrayList.contains(this.fRootName)) {
                return false;
            }
        } else {
            ArrayList arrayList2 = this.fPossibleRoots;
            if (arrayList2 != null) {
                String str3 = xMLDTDDescription.fRootName;
                if (str3 != null) {
                    if (!arrayList2.contains(str3)) {
                        return false;
                    }
                } else if (xMLDTDDescription.fPossibleRoots == null) {
                    return false;
                } else {
                    int size = arrayList2.size();
                    boolean z = false;
                    for (int i = 0; i < size; i++) {
                        z = xMLDTDDescription.fPossibleRoots.contains((String) this.fPossibleRoots.get(i));
                        if (z) {
                            break;
                        }
                    }
                    if (!z) {
                        return false;
                    }
                }
            }
        }
        if (this.fExpandedSystemId != null) {
            if (!this.fExpandedSystemId.equals(xMLDTDDescription.fExpandedSystemId)) {
                return false;
            }
        } else if (xMLDTDDescription.fExpandedSystemId != null) {
            return false;
        }
        if (this.fPublicId != null) {
            if (!this.fPublicId.equals(xMLDTDDescription.fPublicId)) {
                return false;
            }
            return true;
        } else if (xMLDTDDescription.fPublicId != null) {
            return false;
        } else {
            return true;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl
    public int hashCode() {
        if (this.fExpandedSystemId != null) {
            return this.fExpandedSystemId.hashCode();
        }
        if (this.fPublicId != null) {
            return this.fPublicId.hashCode();
        }
        return 0;
    }
}
