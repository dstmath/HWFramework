package ohos.global.icu.text;

import ohos.miscservices.httpaccess.HttpConstant;

class FunctionReplacer implements UnicodeReplacer {
    private UnicodeReplacer replacer;
    private Transliterator translit;

    public FunctionReplacer(Transliterator transliterator, UnicodeReplacer unicodeReplacer) {
        this.translit = transliterator;
        this.replacer = unicodeReplacer;
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public int replace(Replaceable replaceable, int i, int i2, int[] iArr) {
        return this.translit.transliterate(replaceable, i, this.replacer.replace(replaceable, i, i2, iArr) + i) - i;
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public String toReplacerPattern(boolean z) {
        return HttpConstant.URL_PARAM_DELIMITER + this.translit.getID() + "( " + this.replacer.toReplacerPattern(z) + " )";
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public void addReplacementSetTo(UnicodeSet unicodeSet) {
        unicodeSet.addAll(this.translit.getTargetSet());
    }
}
