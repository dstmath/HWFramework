package android.icu.text;

final class RBNFChinesePostProcessor implements RBNFPostProcessor {
    private static final String[] rulesetNames = {"%traditional", "%simplified", "%accounting", "%time"};
    private int format;
    private boolean longForm;

    RBNFChinesePostProcessor() {
    }

    public void init(RuleBasedNumberFormat formatter, String rules) {
    }

    public void process(StringBuilder buf, NFRuleSet ruleSet) {
        int s;
        int s2;
        StringBuilder sb = buf;
        String name = ruleSet.getName();
        int i = 0;
        while (true) {
            if (i >= rulesetNames.length) {
                break;
            } else if (rulesetNames[i].equals(name)) {
                this.format = i;
                this.longForm = i == 1 || i == 3;
            } else {
                i++;
            }
        }
        if (this.longForm != 0) {
            for (int i2 = sb.indexOf("*"); i2 != -1; i2 = sb.indexOf("*", i2)) {
                sb.delete(i2, i2 + 1);
            }
            return;
        }
        String[][] markers = {new String[]{"萬", "億", "兆", "〇"}, new String[]{"万", "亿", "兆", "〇"}, new String[]{"萬", "億", "兆", "零"}};
        String[] m = markers[this.format];
        for (int i3 = 0; i3 < m.length - 1; i3++) {
            int n = sb.indexOf(m[i3]);
            if (n != -1) {
                sb.insert(m[i3].length() + n, '|');
            }
        }
        int x = sb.indexOf("點");
        if (x == -1) {
            x = buf.length();
        }
        int s3 = 0;
        int n2 = -1;
        String ling = markers[this.format][3];
        while (x >= 0) {
            int m2 = sb.lastIndexOf("|", x);
            int nn = sb.lastIndexOf(ling, x);
            int ns = 0;
            if (nn > m2) {
                ns = (nn <= 0 || sb.charAt(nn + -1) == '*') ? 1 : 2;
            }
            x = m2 - 1;
            switch ((s3 * 3) + ns) {
                case 0:
                    s2 = ns;
                    s = -1;
                    break;
                case 1:
                    s2 = ns;
                    s = nn;
                    break;
                case 2:
                    s2 = ns;
                    s = -1;
                    break;
                case 3:
                    s2 = ns;
                    s = -1;
                    break;
                case 4:
                    sb.delete(nn - 1, ling.length() + nn);
                    s2 = 0;
                    s = -1;
                    break;
                case 5:
                    sb.delete(n2 - 1, ling.length() + n2);
                    s2 = ns;
                    s = -1;
                    break;
                case 6:
                    s2 = ns;
                    s = -1;
                    break;
                case 7:
                    sb.delete(nn - 1, ling.length() + nn);
                    s2 = 0;
                    s = -1;
                    break;
                case 8:
                    s2 = ns;
                    s = -1;
                    break;
                default:
                    throw new IllegalStateException();
            }
            n2 = s;
            s3 = s2;
        }
        int i4 = buf.length();
        while (true) {
            i4--;
            if (i4 >= 0) {
                char c = sb.charAt(i4);
                if (c == '*' || c == '|') {
                    sb.delete(i4, i4 + 1);
                }
            } else {
                return;
            }
        }
    }
}
