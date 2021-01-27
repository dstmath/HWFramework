package ohos.global.icu.text;

final class RBNFChinesePostProcessor implements RBNFPostProcessor {
    private static final String[] rulesetNames = {"%traditional", "%simplified", "%accounting", "%time"};
    private int format;
    private boolean longForm;

    @Override // ohos.global.icu.text.RBNFPostProcessor
    public void init(RuleBasedNumberFormat ruleBasedNumberFormat, String str) {
    }

    RBNFChinesePostProcessor() {
    }

    @Override // ohos.global.icu.text.RBNFPostProcessor
    public void process(StringBuilder sb, NFRuleSet nFRuleSet) {
        String name = nFRuleSet.getName();
        int i = 0;
        while (true) {
            String[] strArr = rulesetNames;
            if (i >= strArr.length) {
                break;
            } else if (strArr[i].equals(name)) {
                this.format = i;
                this.longForm = i == 1 || i == 3;
            } else {
                i++;
            }
        }
        if (this.longForm) {
            int indexOf = sb.indexOf("*");
            while (indexOf != -1) {
                sb.delete(indexOf, indexOf + 1);
                indexOf = sb.indexOf("*", indexOf);
            }
            return;
        }
        String[][] strArr2 = {new String[]{"萬", "億", "兆", "〇"}, new String[]{"万", "亿", "兆", "〇"}, new String[]{"萬", "億", "兆", "零"}};
        String[] strArr3 = strArr2[this.format];
        for (int i2 = 0; i2 < strArr3.length - 1; i2++) {
            int indexOf2 = sb.indexOf(strArr3[i2]);
            if (indexOf2 != -1) {
                sb.insert(indexOf2 + strArr3[i2].length(), '|');
            }
        }
        int indexOf3 = sb.indexOf("點");
        if (indexOf3 == -1) {
            indexOf3 = sb.length();
        }
        String str = strArr2[this.format][3];
        int i3 = 0;
        int i4 = -1;
        while (indexOf3 >= 0) {
            int lastIndexOf = sb.lastIndexOf("|", indexOf3);
            int lastIndexOf2 = sb.lastIndexOf(str, indexOf3);
            int i5 = lastIndexOf2 > lastIndexOf ? (lastIndexOf2 <= 0 || sb.charAt(lastIndexOf2 + -1) == '*') ? 1 : 2 : 0;
            int i6 = lastIndexOf - 1;
            switch ((i3 * 3) + i5) {
                case 0:
                case 2:
                case 3:
                case 6:
                case 8:
                    i4 = -1;
                    i3 = i5;
                    indexOf3 = i6;
                case 1:
                    i4 = lastIndexOf2;
                    i3 = i5;
                    indexOf3 = i6;
                case 4:
                    sb.delete(lastIndexOf2 - 1, lastIndexOf2 + str.length());
                    i3 = 0;
                    i4 = -1;
                    indexOf3 = i6;
                case 5:
                    sb.delete(i4 - 1, i4 + str.length());
                    i4 = -1;
                    i3 = i5;
                    indexOf3 = i6;
                case 7:
                    sb.delete(lastIndexOf2 - 1, lastIndexOf2 + str.length());
                    i3 = 0;
                    i4 = -1;
                    indexOf3 = i6;
                default:
                    throw new IllegalStateException();
            }
        }
        int length = sb.length();
        while (true) {
            length--;
            if (length >= 0) {
                char charAt = sb.charAt(length);
                if (charAt == '*' || charAt == '|') {
                    sb.delete(length, length + 1);
                }
            } else {
                return;
            }
        }
    }
}
