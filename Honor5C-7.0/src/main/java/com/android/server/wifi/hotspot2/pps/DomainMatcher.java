package com.android.server.wifi.hotspot2.pps;

import com.android.server.wifi.hotspot2.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DomainMatcher {
    private static final String[] TestDomains = null;
    private final Label mRoot;

    private static class Label {
        private final Match mMatch;
        private final Map<String, Label> mSubDomains;

        private Label(Match match) {
            this.mMatch = match;
            this.mSubDomains = match == Match.None ? new HashMap() : null;
        }

        private void addDomain(Iterator<String> labels, Match match) {
            String labelName = (String) labels.next();
            if (labels.hasNext()) {
                Label subLabel = new Label(Match.None);
                this.mSubDomains.put(labelName, subLabel);
                subLabel.addDomain(labels, match);
                return;
            }
            this.mSubDomains.put(labelName, new Label(match));
        }

        private Label getSubLabel(String labelString) {
            return (Label) this.mSubDomains.get(labelString);
        }

        public Match getMatch() {
            return this.mMatch;
        }

        private void toString(StringBuilder sb) {
            if (this.mSubDomains != null) {
                sb.append(".{");
                for (Entry<String, Label> entry : this.mSubDomains.entrySet()) {
                    sb.append((String) entry.getKey());
                    ((Label) entry.getValue()).toString(sb);
                }
                sb.append('}');
                return;
            }
            sb.append('=').append(this.mMatch);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
    }

    public enum Match {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void");
    }

    public DomainMatcher(List<String> primary, List<List<String>> secondary) {
        this.mRoot = new Label(null);
        for (List<String> secondaryLabel : secondary) {
            this.mRoot.addDomain(secondaryLabel.iterator(), Match.Secondary);
        }
        this.mRoot.addDomain(primary.iterator(), Match.Primary);
    }

    public Match isSubDomain(List<String> domain) {
        Label label = this.mRoot;
        for (String labelString : domain) {
            label = label.getSubLabel(labelString);
            if (label == null) {
                return Match.None;
            }
            if (label.getMatch() != Match.None) {
                return label.getMatch();
            }
        }
        return Match.None;
    }

    public static boolean arg2SubdomainOfArg1(List<String> arg1, List<String> arg2) {
        if (arg2.size() < arg1.size()) {
            return false;
        }
        Iterator<String> l2 = arg2.iterator();
        for (String equals : arg1) {
            if (!equals.equals(l2.next())) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "Domain matcher " + this.mRoot;
    }

    public static void main(String[] args) {
        int i = 0;
        DomainMatcher dm1 = new DomainMatcher(Utils.splitDomain("android.google.com"), Collections.emptyList());
        for (String domain : TestDomains) {
            String domain2;
            System.out.println(domain2 + ": " + dm1.isSubDomain(Utils.splitDomain(domain2)));
        }
        List<List<String>> secondaries = new ArrayList();
        secondaries.add(Utils.splitDomain("apple.com"));
        secondaries.add(Utils.splitDomain("net"));
        DomainMatcher dm2 = new DomainMatcher(Utils.splitDomain("android.google.com"), secondaries);
        String[] strArr = TestDomains;
        int length = strArr.length;
        while (i < length) {
            domain2 = strArr[i];
            System.out.println(domain2 + ": " + dm2.isSubDomain(Utils.splitDomain(domain2)));
            i++;
        }
    }
}
