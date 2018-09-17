package android.icu.text;

final class RBNFChinesePostProcessor implements RBNFPostProcessor {
    private static final String[] rulesetNames = new String[]{"%traditional", "%simplified", "%accounting", "%time"};
    private int format;
    private boolean longForm;

    RBNFChinesePostProcessor() {
    }

    public void init(RuleBasedNumberFormat formatter, String rules) {
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0029  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void process(StringBuilder buf, NFRuleSet ruleSet) {
        String name = ruleSet.getName();
        int i = 0;
        while (i < rulesetNames.length) {
            if (rulesetNames[i].equals(name)) {
                this.format = i;
                boolean z = i == 1 || i == 3;
                this.longForm = z;
                if (this.longForm) {
                    int n;
                    String DIAN = "點";
                    markers = new String[3][];
                    markers[0] = new String[]{"萬", "億", "兆", "〇"};
                    markers[1] = new String[]{"万", "亿", "兆", "〇"};
                    markers[2] = new String[]{"萬", "億", "兆", "零"};
                    String[] m = markers[this.format];
                    for (i = 0; i < m.length - 1; i++) {
                        n = buf.indexOf(m[i]);
                        if (n != -1) {
                            buf.insert(m[i].length() + n, '|');
                        }
                    }
                    int x = buf.indexOf("點");
                    if (x == -1) {
                        x = buf.length();
                    }
                    int s = 0;
                    n = -1;
                    String ling = markers[this.format][3];
                    while (x >= 0) {
                        int m2 = buf.lastIndexOf("|", x);
                        int nn = buf.lastIndexOf(ling, x);
                        int ns = 0;
                        if (nn > m2) {
                            if (nn > 0) {
                                if (buf.charAt(nn - 1) != '*') {
                                    ns = 2;
                                }
                            }
                            ns = 1;
                        }
                        x = m2 - 1;
                        switch ((s * 3) + ns) {
                            case 0:
                                s = ns;
                                n = -1;
                                break;
                            case 1:
                                s = ns;
                                n = nn;
                                break;
                            case 2:
                                s = ns;
                                n = -1;
                                break;
                            case 3:
                                s = ns;
                                n = -1;
                                break;
                            case 4:
                                buf.delete(nn - 1, ling.length() + nn);
                                s = 0;
                                n = -1;
                                break;
                            case 5:
                                buf.delete(n - 1, ling.length() + n);
                                s = ns;
                                n = -1;
                                break;
                            case 6:
                                s = ns;
                                n = -1;
                                break;
                            case 7:
                                buf.delete(nn - 1, ling.length() + nn);
                                s = 0;
                                n = -1;
                                break;
                            case 8:
                                s = ns;
                                n = -1;
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                    }
                    i = buf.length();
                    while (true) {
                        i--;
                        if (i >= 0) {
                            char c = buf.charAt(i);
                            if (c == '*' || c == '|') {
                                buf.delete(i, i + 1);
                            }
                        } else {
                            return;
                        }
                    }
                }
                i = buf.indexOf("*");
                while (i != -1) {
                    buf.delete(i, i + 1);
                    i = buf.indexOf("*", i);
                }
                return;
            }
            i++;
        }
        if (this.longForm) {
        }
    }
}
