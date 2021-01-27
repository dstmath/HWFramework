package com.google.android.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import com.google.android.util.AbstractMessageParser;
import java.util.ArrayList;

public class SmileyParser extends AbstractMessageParser {
    private SmileyResources mRes;

    public SmileyParser(String text, SmileyResources res) {
        super(text, true, false, false, false, false, false);
        this.mRes = res;
    }

    /* access modifiers changed from: protected */
    @Override // com.google.android.util.AbstractMessageParser
    public AbstractMessageParser.Resources getResources() {
        return this.mRes;
    }

    public CharSequence getSpannableString(Context context) {
        int resid;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (getPartCount() == 0) {
            return "";
        }
        ArrayList<AbstractMessageParser.Token> tokens = getPart(0).getTokens();
        int len = tokens.size();
        for (int i = 0; i < len; i++) {
            AbstractMessageParser.Token token = tokens.get(i);
            int start = builder.length();
            builder.append((CharSequence) token.getRawText());
            if (token.getType() == AbstractMessageParser.Token.Type.SMILEY && (resid = this.mRes.getSmileyRes(token.getRawText())) != -1) {
                builder.setSpan(new ImageSpan(context, resid), start, builder.length(), 33);
            }
        }
        return builder;
    }
}
