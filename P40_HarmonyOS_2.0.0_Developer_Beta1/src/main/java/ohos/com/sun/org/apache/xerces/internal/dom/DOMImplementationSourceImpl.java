package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DOMImplementationList;
import ohos.org.w3c.dom.DOMImplementationSource;

public class DOMImplementationSourceImpl implements DOMImplementationSource {
    public DOMImplementation getDOMImplementation(String str) {
        DOMImplementation dOMImplementation = CoreDOMImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation, str)) {
            return dOMImplementation;
        }
        DOMImplementation dOMImplementation2 = DOMImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation2, str)) {
            return dOMImplementation2;
        }
        return null;
    }

    public DOMImplementationList getDOMImplementationList(String str) {
        DOMImplementation dOMImplementation = CoreDOMImplementationImpl.getDOMImplementation();
        Vector vector = new Vector();
        if (testImpl(dOMImplementation, str)) {
            vector.addElement(dOMImplementation);
        }
        DOMImplementation dOMImplementation2 = DOMImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation2, str)) {
            vector.addElement(dOMImplementation2);
        }
        return new DOMImplementationListImpl(vector);
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0015  */
    boolean testImpl(ohos.org.w3c.dom.DOMImplementation r6, java.lang.String r7) {
        /*
            r5 = this;
            java.util.StringTokenizer r5 = new java.util.StringTokenizer
            r5.<init>(r7)
            boolean r7 = r5.hasMoreTokens()
            r0 = 0
            if (r7 == 0) goto L_0x0011
            java.lang.String r7 = r5.nextToken()
            goto L_0x0012
        L_0x0011:
            r7 = r0
        L_0x0012:
            r1 = 1
            if (r7 == 0) goto L_0x0047
            boolean r2 = r5.hasMoreTokens()
            r3 = 0
            if (r2 == 0) goto L_0x0028
            java.lang.String r2 = r5.nextToken()
            char r4 = r2.charAt(r3)
            switch(r4) {
                case 48: goto L_0x002a;
                case 49: goto L_0x002a;
                case 50: goto L_0x002a;
                case 51: goto L_0x002a;
                case 52: goto L_0x002a;
                case 53: goto L_0x002a;
                case 54: goto L_0x002a;
                case 55: goto L_0x002a;
                case 56: goto L_0x002a;
                case 57: goto L_0x002a;
                default: goto L_0x0027;
            }
        L_0x0027:
            goto L_0x0029
        L_0x0028:
            r2 = r0
        L_0x0029:
            r1 = r3
        L_0x002a:
            if (r1 == 0) goto L_0x003e
            boolean r7 = r6.hasFeature(r7, r2)
            if (r7 != 0) goto L_0x0033
            return r3
        L_0x0033:
            boolean r7 = r5.hasMoreTokens()
            if (r7 == 0) goto L_0x0011
            java.lang.String r7 = r5.nextToken()
            goto L_0x0012
        L_0x003e:
            boolean r7 = r6.hasFeature(r7, r0)
            if (r7 != 0) goto L_0x0045
            return r3
        L_0x0045:
            r7 = r2
            goto L_0x0012
        L_0x0047:
            return r1
            switch-data {48->0x002a, 49->0x002a, 50->0x002a, 51->0x002a, 52->0x002a, 53->0x002a, 54->0x002a, 55->0x002a, 56->0x002a, 57->0x002a, }
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl.testImpl(ohos.org.w3c.dom.DOMImplementation, java.lang.String):boolean");
    }
}
