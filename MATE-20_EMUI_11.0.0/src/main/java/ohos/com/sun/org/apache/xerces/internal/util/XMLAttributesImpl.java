package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.xml.internal.stream.XMLBufferListener;

public class XMLAttributesImpl implements XMLAttributes, XMLBufferListener {
    protected static final int MAX_HASH_COLLISIONS = 40;
    protected static final int MULTIPLIERS_MASK = 31;
    protected static final int MULTIPLIERS_SIZE = 32;
    protected static final int SIZE_LIMIT = 20;
    protected static final int TABLE_SIZE = 101;
    protected Attribute[] fAttributeTableView;
    protected int[] fAttributeTableViewChainState;
    protected Attribute[] fAttributes;
    protected int[] fHashMultipliers;
    protected boolean fIsTableViewConsistent;
    protected int fLargeCount;
    protected int fLength;
    protected boolean fNamespaces;
    protected int fTableViewBuckets;

    @Override // ohos.com.sun.xml.internal.stream.XMLBufferListener
    public void refresh(int i) {
    }

    public XMLAttributesImpl() {
        this(101);
    }

    public XMLAttributesImpl(int i) {
        this.fNamespaces = true;
        this.fLargeCount = 1;
        this.fAttributes = new Attribute[4];
        this.fTableViewBuckets = i;
        int i2 = 0;
        while (true) {
            Attribute[] attributeArr = this.fAttributes;
            if (i2 < attributeArr.length) {
                attributeArr[i2] = new Attribute();
                i2++;
            } else {
                return;
            }
        }
    }

    public void setNamespaces(boolean z) {
        this.fNamespaces = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public int addAttribute(QName qName, String str, String str2) {
        return addAttribute(qName, str, str2, null);
    }

    public int addAttribute(QName qName, String str, String str2, XMLString xMLString) {
        int i;
        int i2;
        int i3;
        if (this.fLength < 20) {
            if (qName.uri == null || qName.uri.equals("")) {
                i = getIndexFast(qName.rawname);
            } else {
                i = getIndexFast(qName.uri, qName.localpart);
            }
            if (i == -1) {
                i = this.fLength;
                this.fLength = i + 1;
                Attribute[] attributeArr = this.fAttributes;
                if (i == attributeArr.length) {
                    Attribute[] attributeArr2 = new Attribute[(attributeArr.length + 4)];
                    System.arraycopy(attributeArr, 0, attributeArr2, 0, attributeArr.length);
                    for (int length = this.fAttributes.length; length < attributeArr2.length; length++) {
                        attributeArr2[length] = new Attribute();
                    }
                    this.fAttributes = attributeArr2;
                }
            }
        } else if (qName.uri == null || qName.uri.length() == 0 || (i = getIndexFast(qName.uri, qName.localpart)) == -1) {
            if (!this.fIsTableViewConsistent || (i3 = this.fLength) == 20 || (i3 > 20 && i3 > this.fTableViewBuckets)) {
                prepareAndPopulateTableView();
                this.fIsTableViewConsistent = true;
            }
            int tableViewBucket = getTableViewBucket(qName.rawname);
            if (this.fAttributeTableViewChainState[tableViewBucket] != this.fLargeCount) {
                i2 = this.fLength;
                this.fLength = i2 + 1;
                Attribute[] attributeArr3 = this.fAttributes;
                if (i2 == attributeArr3.length) {
                    Attribute[] attributeArr4 = new Attribute[(attributeArr3.length << 1)];
                    System.arraycopy(attributeArr3, 0, attributeArr4, 0, attributeArr3.length);
                    for (int length2 = this.fAttributes.length; length2 < attributeArr4.length; length2++) {
                        attributeArr4[length2] = new Attribute();
                    }
                    this.fAttributes = attributeArr4;
                }
                this.fAttributeTableViewChainState[tableViewBucket] = this.fLargeCount;
                Attribute[] attributeArr5 = this.fAttributes;
                attributeArr5[i2].next = null;
                this.fAttributeTableView[tableViewBucket] = attributeArr5[i2];
            } else {
                Attribute attribute = this.fAttributeTableView[tableViewBucket];
                int i4 = 0;
                while (attribute != null && attribute.name.rawname != qName.rawname) {
                    attribute = attribute.next;
                    i4++;
                }
                if (attribute == null) {
                    i2 = this.fLength;
                    this.fLength = i2 + 1;
                    Attribute[] attributeArr6 = this.fAttributes;
                    if (i2 == attributeArr6.length) {
                        Attribute[] attributeArr7 = new Attribute[(attributeArr6.length << 1)];
                        System.arraycopy(attributeArr6, 0, attributeArr7, 0, attributeArr6.length);
                        for (int length3 = this.fAttributes.length; length3 < attributeArr7.length; length3++) {
                            attributeArr7[length3] = new Attribute();
                        }
                        this.fAttributes = attributeArr7;
                    }
                    if (i4 >= 40) {
                        this.fAttributes[i2].name.setValues(qName);
                        rebalanceTableView(this.fLength);
                    } else {
                        Attribute[] attributeArr8 = this.fAttributes;
                        Attribute attribute2 = attributeArr8[i2];
                        Attribute[] attributeArr9 = this.fAttributeTableView;
                        attribute2.next = attributeArr9[tableViewBucket];
                        attributeArr9[tableViewBucket] = attributeArr8[i2];
                    }
                } else {
                    i = getIndexFast(qName.rawname);
                }
            }
            i = i2;
        }
        Attribute attribute3 = this.fAttributes[i];
        attribute3.name.setValues(qName);
        attribute3.type = str;
        attribute3.value = str2;
        attribute3.xmlValue = xMLString;
        attribute3.nonNormalizedValue = str2;
        attribute3.specified = false;
        if (attribute3.augs != null) {
            attribute3.augs.removeAllItems();
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void removeAllAttributes() {
        this.fLength = 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void removeAttributeAt(int i) {
        this.fIsTableViewConsistent = false;
        int i2 = this.fLength;
        if (i < i2 - 1) {
            Attribute[] attributeArr = this.fAttributes;
            Attribute attribute = attributeArr[i];
            System.arraycopy(attributeArr, i + 1, attributeArr, i, (i2 - i) - 1);
            this.fAttributes[this.fLength - 1] = attribute;
        }
        this.fLength--;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setName(int i, QName qName) {
        this.fAttributes[i].name.setValues(qName);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void getName(int i, QName qName) {
        qName.setValues(this.fAttributes[i].name);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setType(int i, String str) {
        this.fAttributes[i].type = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setValue(int i, String str) {
        setValue(i, str, null);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setValue(int i, String str, XMLString xMLString) {
        Attribute attribute = this.fAttributes[i];
        attribute.value = str;
        attribute.nonNormalizedValue = str;
        attribute.xmlValue = xMLString;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setNonNormalizedValue(int i, String str) {
        if (str == null) {
            str = this.fAttributes[i].value;
        }
        this.fAttributes[i].nonNormalizedValue = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getNonNormalizedValue(int i) {
        return this.fAttributes[i].nonNormalizedValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setSpecified(int i, boolean z) {
        this.fAttributes[i].specified = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public boolean isSpecified(int i) {
        return this.fAttributes[i].specified;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public int getLength() {
        return this.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getType(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return getReportableType(this.fAttributes[i].type);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getType(String str) {
        int index = getIndex(str);
        if (index != -1) {
            return getReportableType(this.fAttributes[index].type);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getValue(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        if (this.fAttributes[i].value == null && this.fAttributes[i].xmlValue != null) {
            Attribute[] attributeArr = this.fAttributes;
            attributeArr[i].value = attributeArr[i].xmlValue.toString();
        }
        return this.fAttributes[i].value;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getValue(String str) {
        int index = getIndex(str);
        if (index == -1) {
            return null;
        }
        if (this.fAttributes[index].value == null) {
            Attribute[] attributeArr = this.fAttributes;
            attributeArr[index].value = attributeArr[index].xmlValue.toString();
        }
        return this.fAttributes[index].value;
    }

    public String getName(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fAttributes[i].name.rawname;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public int getIndex(String str) {
        for (int i = 0; i < this.fLength; i++) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.rawname != null && attribute.name.rawname.equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public int getIndex(String str, String str2) {
        for (int i = 0; i < this.fLength; i++) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart != null && attribute.name.localpart.equals(str2) && (str == attribute.name.uri || !(str == null || attribute.name.uri == null || !attribute.name.uri.equals(str)))) {
                return i;
            }
        }
        return -1;
    }

    public int getIndexByLocalName(String str) {
        for (int i = 0; i < this.fLength; i++) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart != null && attribute.name.localpart.equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getLocalName(int i) {
        if (!this.fNamespaces) {
            return "";
        }
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fAttributes[i].name.localpart;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getQName(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        String str = this.fAttributes[i].name.rawname;
        return str != null ? str : "";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public QName getQualifiedName(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fAttributes[i].name;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getType(String str, String str2) {
        int index;
        if (this.fNamespaces && (index = getIndex(str, str2)) != -1) {
            return getType(index);
        }
        return null;
    }

    public int getIndexFast(String str) {
        for (int i = 0; i < this.fLength; i++) {
            if (this.fAttributes[i].name.rawname == str) {
                return i;
            }
        }
        return -1;
    }

    public void addAttributeNS(QName qName, String str, String str2) {
        Attribute[] attributeArr;
        int i = this.fLength;
        this.fLength = i + 1;
        Attribute[] attributeArr2 = this.fAttributes;
        if (i == attributeArr2.length) {
            if (this.fLength < 20) {
                attributeArr = new Attribute[(attributeArr2.length + 4)];
            } else {
                attributeArr = new Attribute[(attributeArr2.length << 1)];
            }
            Attribute[] attributeArr3 = this.fAttributes;
            System.arraycopy(attributeArr3, 0, attributeArr, 0, attributeArr3.length);
            for (int length = this.fAttributes.length; length < attributeArr.length; length++) {
                attributeArr[length] = new Attribute();
            }
            this.fAttributes = attributeArr;
        }
        Attribute attribute = this.fAttributes[i];
        attribute.name.setValues(qName);
        attribute.type = str;
        attribute.value = str2;
        attribute.nonNormalizedValue = str2;
        attribute.specified = false;
        attribute.augs.removeAllItems();
    }

    public QName checkDuplicatesNS() {
        int i = this.fLength;
        if (i > 20) {
            return checkManyDuplicatesNS();
        }
        Attribute[] attributeArr = this.fAttributes;
        int i2 = 0;
        while (i2 < i - 1) {
            Attribute attribute = attributeArr[i2];
            i2++;
            int i3 = i2;
            while (true) {
                if (i3 < i) {
                    Attribute attribute2 = attributeArr[i3];
                    if (attribute.name.localpart == attribute2.name.localpart && attribute.name.uri == attribute2.name.uri) {
                        return attribute2.name;
                    }
                    i3++;
                }
            }
        }
        return null;
    }

    private QName checkManyDuplicatesNS() {
        this.fIsTableViewConsistent = false;
        prepareTableView();
        int i = this.fLength;
        Attribute[] attributeArr = this.fAttributes;
        Attribute[] attributeArr2 = this.fAttributeTableView;
        int[] iArr = this.fAttributeTableViewChainState;
        int i2 = this.fLargeCount;
        for (int i3 = 0; i3 < i; i3++) {
            Attribute attribute = attributeArr[i3];
            int tableViewBucket = getTableViewBucket(attribute.name.localpart, attribute.name.uri);
            if (iArr[tableViewBucket] != i2) {
                iArr[tableViewBucket] = i2;
                attribute.next = null;
                attributeArr2[tableViewBucket] = attribute;
            } else {
                Attribute attribute2 = attributeArr2[tableViewBucket];
                int i4 = 0;
                while (attribute2 != null) {
                    if (attribute2.name.localpart == attribute.name.localpart && attribute2.name.uri == attribute.name.uri) {
                        return attribute.name;
                    }
                    attribute2 = attribute2.next;
                    i4++;
                }
                if (i4 >= 40) {
                    rebalanceTableViewNS(i3 + 1);
                    i2 = this.fLargeCount;
                } else {
                    attribute.next = attributeArr2[tableViewBucket];
                    attributeArr2[tableViewBucket] = attribute;
                }
            }
        }
        return null;
    }

    public int getIndexFast(String str, String str2) {
        for (int i = 0; i < this.fLength; i++) {
            Attribute attribute = this.fAttributes[i];
            if (attribute.name.localpart == str2 && attribute.name.uri == str) {
                return i;
            }
        }
        return -1;
    }

    private String getReportableType(String str) {
        return str.charAt(0) == '(' ? SchemaSymbols.ATTVAL_NMTOKEN : str;
    }

    /* access modifiers changed from: protected */
    public int getTableViewBucket(String str) {
        return (hash(str) & Integer.MAX_VALUE) % this.fTableViewBuckets;
    }

    /* access modifiers changed from: protected */
    public int getTableViewBucket(String str, String str2) {
        if (str2 == null) {
            return (hash(str) & Integer.MAX_VALUE) % this.fTableViewBuckets;
        }
        return (hash(str, str2) & Integer.MAX_VALUE) % this.fTableViewBuckets;
    }

    private int hash(String str) {
        if (this.fHashMultipliers == null) {
            return str.hashCode();
        }
        return hash0(str);
    }

    private int hash(String str, String str2) {
        if (this.fHashMultipliers == null) {
            return str.hashCode() + (str2.hashCode() * 31);
        }
        return hash0(str) + (hash0(str2) * this.fHashMultipliers[32]);
    }

    private int hash0(String str) {
        int length = str.length();
        int[] iArr = this.fHashMultipliers;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            i = (i * iArr[i2 & 31]) + str.charAt(i2);
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public void cleanTableView() {
        int i = this.fLargeCount + 1;
        this.fLargeCount = i;
        if (i < 0) {
            if (this.fAttributeTableViewChainState != null) {
                for (int i2 = this.fTableViewBuckets - 1; i2 >= 0; i2--) {
                    this.fAttributeTableViewChainState[i2] = 0;
                }
            }
            this.fLargeCount = 1;
        }
    }

    private void growTableView() {
        int i = this.fLength;
        int i2 = this.fTableViewBuckets;
        while (true) {
            i2 = (i2 << 1) + 1;
            if (i2 >= 0) {
                if (i <= i2) {
                    break;
                }
            } else {
                i2 = Integer.MAX_VALUE;
                break;
            }
        }
        this.fTableViewBuckets = i2;
        this.fAttributeTableView = null;
        this.fLargeCount = 1;
    }

    /* access modifiers changed from: protected */
    public void prepareTableView() {
        if (this.fLength > this.fTableViewBuckets) {
            growTableView();
        }
        if (this.fAttributeTableView == null) {
            int i = this.fTableViewBuckets;
            this.fAttributeTableView = new Attribute[i];
            this.fAttributeTableViewChainState = new int[i];
            return;
        }
        cleanTableView();
    }

    /* access modifiers changed from: protected */
    public void prepareAndPopulateTableView() {
        prepareAndPopulateTableView(this.fLength);
    }

    private void prepareAndPopulateTableView(int i) {
        prepareTableView();
        for (int i2 = 0; i2 < i; i2++) {
            Attribute attribute = this.fAttributes[i2];
            int tableViewBucket = getTableViewBucket(attribute.name.rawname);
            int[] iArr = this.fAttributeTableViewChainState;
            int i3 = iArr[tableViewBucket];
            int i4 = this.fLargeCount;
            if (i3 != i4) {
                iArr[tableViewBucket] = i4;
                attribute.next = null;
                this.fAttributeTableView[tableViewBucket] = attribute;
            } else {
                Attribute[] attributeArr = this.fAttributeTableView;
                attribute.next = attributeArr[tableViewBucket];
                attributeArr[tableViewBucket] = attribute;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getPrefix(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        String str = this.fAttributes[i].name.prefix;
        return str != null ? str : "";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getURI(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fAttributes[i].name.uri;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public String getValue(String str, String str2) {
        int index = getIndex(str, str2);
        if (index != -1) {
            return getValue(index);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public Augmentations getAugmentations(String str, String str2) {
        int index = getIndex(str, str2);
        if (index != -1) {
            return this.fAttributes[index].augs;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public Augmentations getAugmentations(String str) {
        int index = getIndex(str);
        if (index != -1) {
            return this.fAttributes[index].augs;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public Augmentations getAugmentations(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fAttributes[i].augs;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
    public void setAugmentations(int i, Augmentations augmentations) {
        this.fAttributes[i].augs = augmentations;
    }

    public void setURI(int i, String str) {
        this.fAttributes[i].name.uri = str;
    }

    public void setSchemaId(int i, boolean z) {
        this.fAttributes[i].schemaId = z;
    }

    public boolean getSchemaId(int i) {
        if (i < 0 || i >= this.fLength) {
            return false;
        }
        return this.fAttributes[i].schemaId;
    }

    public boolean getSchemaId(String str) {
        int index = getIndex(str);
        if (index != -1) {
            return this.fAttributes[index].schemaId;
        }
        return false;
    }

    public boolean getSchemaId(String str, String str2) {
        int index;
        if (this.fNamespaces && (index = getIndex(str, str2)) != -1) {
            return this.fAttributes[index].schemaId;
        }
        return false;
    }

    @Override // ohos.com.sun.xml.internal.stream.XMLBufferListener
    public void refresh() {
        if (this.fLength > 0) {
            for (int i = 0; i < this.fLength; i++) {
                getValue(i);
            }
        }
    }

    private void prepareAndPopulateTableViewNS(int i) {
        prepareTableView();
        for (int i2 = 0; i2 < i; i2++) {
            Attribute attribute = this.fAttributes[i2];
            int tableViewBucket = getTableViewBucket(attribute.name.localpart, attribute.name.uri);
            int[] iArr = this.fAttributeTableViewChainState;
            int i3 = iArr[tableViewBucket];
            int i4 = this.fLargeCount;
            if (i3 != i4) {
                iArr[tableViewBucket] = i4;
                attribute.next = null;
                this.fAttributeTableView[tableViewBucket] = attribute;
            } else {
                Attribute[] attributeArr = this.fAttributeTableView;
                attribute.next = attributeArr[tableViewBucket];
                attributeArr[tableViewBucket] = attribute;
            }
        }
    }

    private void rebalanceTableView(int i) {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[33];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        prepareAndPopulateTableView(i);
    }

    private void rebalanceTableViewNS(int i) {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[33];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        prepareAndPopulateTableViewNS(i);
    }

    /* access modifiers changed from: package-private */
    public static class Attribute {
        public Augmentations augs = new AugmentationsImpl();
        public QName name = new QName();
        public Attribute next;
        public String nonNormalizedValue;
        public boolean schemaId;
        public boolean specified;
        public String type;
        public String value;
        public XMLString xmlValue;

        Attribute() {
        }
    }
}
