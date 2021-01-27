package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.java_cup.internal.runtime.Symbol;
import ohos.com.sun.java_cup.internal.runtime.lr_parser;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* compiled from: XPathParser */
class CUP$XPathParser$actions {
    private final XPathParser parser;

    CUP$XPathParser$actions(XPathParser xPathParser) {
        this.parser = xPathParser;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:305:0x0f88, code lost:
        if (((ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Step) r2).isAbbreviatedDot() != false) goto L_0x0fe6;
     */
    public final Symbol CUP$XPathParser$do_action(int i, lr_parser lr_parser, Stack stack, int i2) throws Exception {
        Constants constants;
        Constants parentLocationPath;
        Step step;
        Step step2;
        LiteralExpr literalExpr;
        Object obj;
        Object obj2;
        Object functionCall;
        Object obj3;
        int i3 = -1;
        String str = null;
        Object obj4 = null;
        int i4 = 1;
        switch (i) {
            case 0:
                int i5 = i2 - 1;
                int i6 = ((Symbol) stack.elementAt(i5)).left;
                int i7 = ((Symbol) stack.elementAt(i5)).right;
                Symbol symbol = new Symbol(0, ((Symbol) stack.elementAt(i5)).left, ((Symbol) stack.elementAt(i2 + 0)).right, (SyntaxTreeNode) ((Symbol) stack.elementAt(i5)).value);
                lr_parser.done_parsing();
                return symbol;
            case 1:
                int i8 = i2 + 0;
                int i9 = ((Symbol) stack.elementAt(i8)).left;
                int i10 = ((Symbol) stack.elementAt(i8)).right;
                return new Symbol(1, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i8)).right, (Pattern) ((Symbol) stack.elementAt(i8)).value);
            case 2:
                int i11 = i2 + 0;
                int i12 = ((Symbol) stack.elementAt(i11)).left;
                int i13 = ((Symbol) stack.elementAt(i11)).right;
                return new Symbol(1, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i11)).right, (Expression) ((Symbol) stack.elementAt(i11)).value);
            case 3:
                int i14 = i2 + 0;
                int i15 = ((Symbol) stack.elementAt(i14)).left;
                int i16 = ((Symbol) stack.elementAt(i14)).right;
                return new Symbol(28, ((Symbol) stack.elementAt(i14)).left, ((Symbol) stack.elementAt(i14)).right, (Pattern) ((Symbol) stack.elementAt(i14)).value);
            case 4:
                int i17 = i2 - 2;
                int i18 = ((Symbol) stack.elementAt(i17)).left;
                int i19 = ((Symbol) stack.elementAt(i17)).right;
                int i20 = i2 + 0;
                int i21 = ((Symbol) stack.elementAt(i20)).left;
                int i22 = ((Symbol) stack.elementAt(i20)).right;
                return new Symbol(28, ((Symbol) stack.elementAt(i17)).left, ((Symbol) stack.elementAt(i20)).right, new AlternativePattern((Pattern) ((Symbol) stack.elementAt(i17)).value, (Pattern) ((Symbol) stack.elementAt(i20)).value));
            case 5:
                int i23 = i2 + 0;
                return new Symbol(29, ((Symbol) stack.elementAt(i23)).left, ((Symbol) stack.elementAt(i23)).right, new AbsolutePathPattern(null));
            case 6:
                int i24 = i2 + 0;
                int i25 = ((Symbol) stack.elementAt(i24)).left;
                int i26 = ((Symbol) stack.elementAt(i24)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i24)).right, new AbsolutePathPattern((RelativePathPattern) ((Symbol) stack.elementAt(i24)).value));
            case 7:
                int i27 = i2 + 0;
                int i28 = ((Symbol) stack.elementAt(i27)).left;
                int i29 = ((Symbol) stack.elementAt(i27)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i27)).left, ((Symbol) stack.elementAt(i27)).right, (IdKeyPattern) ((Symbol) stack.elementAt(i27)).value);
            case 8:
                int i30 = i2 - 2;
                int i31 = ((Symbol) stack.elementAt(i30)).left;
                int i32 = ((Symbol) stack.elementAt(i30)).right;
                int i33 = i2 + 0;
                int i34 = ((Symbol) stack.elementAt(i33)).left;
                int i35 = ((Symbol) stack.elementAt(i33)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i30)).left, ((Symbol) stack.elementAt(i33)).right, new ParentPattern((IdKeyPattern) ((Symbol) stack.elementAt(i30)).value, (RelativePathPattern) ((Symbol) stack.elementAt(i33)).value));
            case 9:
                int i36 = i2 - 2;
                int i37 = ((Symbol) stack.elementAt(i36)).left;
                int i38 = ((Symbol) stack.elementAt(i36)).right;
                int i39 = i2 + 0;
                int i40 = ((Symbol) stack.elementAt(i39)).left;
                int i41 = ((Symbol) stack.elementAt(i39)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i36)).left, ((Symbol) stack.elementAt(i39)).right, new AncestorPattern((IdKeyPattern) ((Symbol) stack.elementAt(i36)).value, (RelativePathPattern) ((Symbol) stack.elementAt(i39)).value));
            case 10:
                int i42 = i2 + 0;
                int i43 = ((Symbol) stack.elementAt(i42)).left;
                int i44 = ((Symbol) stack.elementAt(i42)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i42)).right, new AncestorPattern((RelativePathPattern) ((Symbol) stack.elementAt(i42)).value));
            case 11:
                int i45 = i2 + 0;
                int i46 = ((Symbol) stack.elementAt(i45)).left;
                int i47 = ((Symbol) stack.elementAt(i45)).right;
                return new Symbol(29, ((Symbol) stack.elementAt(i45)).left, ((Symbol) stack.elementAt(i45)).right, (RelativePathPattern) ((Symbol) stack.elementAt(i45)).value);
            case 12:
                int i48 = i2 - 1;
                int i49 = ((Symbol) stack.elementAt(i48)).left;
                int i50 = ((Symbol) stack.elementAt(i48)).right;
                IdPattern idPattern = new IdPattern((String) ((Symbol) stack.elementAt(i48)).value);
                this.parser.setHasIdCall(true);
                return new Symbol(27, ((Symbol) stack.elementAt(i2 - 3)).left, ((Symbol) stack.elementAt(i2 + 0)).right, idPattern);
            case 13:
                int i51 = i2 - 3;
                int i52 = ((Symbol) stack.elementAt(i51)).left;
                int i53 = ((Symbol) stack.elementAt(i51)).right;
                int i54 = i2 - 1;
                int i55 = ((Symbol) stack.elementAt(i54)).left;
                int i56 = ((Symbol) stack.elementAt(i54)).right;
                return new Symbol(27, ((Symbol) stack.elementAt(i2 - 5)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new KeyPattern((String) ((Symbol) stack.elementAt(i51)).value, (String) ((Symbol) stack.elementAt(i54)).value));
            case 14:
                int i57 = i2 - 1;
                int i58 = ((Symbol) stack.elementAt(i57)).left;
                int i59 = ((Symbol) stack.elementAt(i57)).right;
                return new Symbol(30, ((Symbol) stack.elementAt(i2 - 3)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new ProcessingInstructionPattern((String) ((Symbol) stack.elementAt(i57)).value));
            case 15:
                int i60 = i2 + 0;
                int i61 = ((Symbol) stack.elementAt(i60)).left;
                int i62 = ((Symbol) stack.elementAt(i60)).right;
                return new Symbol(31, ((Symbol) stack.elementAt(i60)).left, ((Symbol) stack.elementAt(i60)).right, (StepPattern) ((Symbol) stack.elementAt(i60)).value);
            case 16:
                int i63 = i2 - 2;
                int i64 = ((Symbol) stack.elementAt(i63)).left;
                int i65 = ((Symbol) stack.elementAt(i63)).right;
                int i66 = i2 + 0;
                int i67 = ((Symbol) stack.elementAt(i66)).left;
                int i68 = ((Symbol) stack.elementAt(i66)).right;
                return new Symbol(31, ((Symbol) stack.elementAt(i63)).left, ((Symbol) stack.elementAt(i66)).right, new ParentPattern((StepPattern) ((Symbol) stack.elementAt(i63)).value, (RelativePathPattern) ((Symbol) stack.elementAt(i66)).value));
            case 17:
                int i69 = i2 - 2;
                int i70 = ((Symbol) stack.elementAt(i69)).left;
                int i71 = ((Symbol) stack.elementAt(i69)).right;
                int i72 = i2 + 0;
                int i73 = ((Symbol) stack.elementAt(i72)).left;
                int i74 = ((Symbol) stack.elementAt(i72)).right;
                return new Symbol(31, ((Symbol) stack.elementAt(i69)).left, ((Symbol) stack.elementAt(i72)).right, new AncestorPattern((StepPattern) ((Symbol) stack.elementAt(i69)).value, (RelativePathPattern) ((Symbol) stack.elementAt(i72)).value));
            case 18:
                int i75 = i2 + 0;
                int i76 = ((Symbol) stack.elementAt(i75)).left;
                int i77 = ((Symbol) stack.elementAt(i75)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i75)).left, ((Symbol) stack.elementAt(i75)).right, this.parser.createStepPattern(3, ((Symbol) stack.elementAt(i75)).value, null));
            case 19:
                int i78 = i2 - 1;
                int i79 = ((Symbol) stack.elementAt(i78)).left;
                int i80 = ((Symbol) stack.elementAt(i78)).right;
                Object obj5 = ((Symbol) stack.elementAt(i78)).value;
                int i81 = i2 + 0;
                int i82 = ((Symbol) stack.elementAt(i81)).left;
                int i83 = ((Symbol) stack.elementAt(i81)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i78)).left, ((Symbol) stack.elementAt(i81)).right, this.parser.createStepPattern(3, obj5, (Vector) ((Symbol) stack.elementAt(i81)).value));
            case 20:
                int i84 = i2 + 0;
                int i85 = ((Symbol) stack.elementAt(i84)).left;
                int i86 = ((Symbol) stack.elementAt(i84)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i84)).left, ((Symbol) stack.elementAt(i84)).right, (StepPattern) ((Symbol) stack.elementAt(i84)).value);
            case 21:
                int i87 = i2 - 1;
                int i88 = ((Symbol) stack.elementAt(i87)).left;
                int i89 = ((Symbol) stack.elementAt(i87)).right;
                int i90 = i2 + 0;
                int i91 = ((Symbol) stack.elementAt(i90)).left;
                int i92 = ((Symbol) stack.elementAt(i90)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i87)).left, ((Symbol) stack.elementAt(i90)).right, (ProcessingInstructionPattern) ((StepPattern) ((Symbol) stack.elementAt(i87)).value).setPredicates((Vector) ((Symbol) stack.elementAt(i90)).value));
            case 22:
                int i93 = i2 - 1;
                int i94 = ((Symbol) stack.elementAt(i93)).left;
                int i95 = ((Symbol) stack.elementAt(i93)).right;
                int i96 = i2 + 0;
                int i97 = ((Symbol) stack.elementAt(i96)).left;
                int i98 = ((Symbol) stack.elementAt(i96)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i93)).left, ((Symbol) stack.elementAt(i96)).right, this.parser.createStepPattern(((Integer) ((Symbol) stack.elementAt(i93)).value).intValue(), ((Symbol) stack.elementAt(i96)).value, null));
            case 23:
                int i99 = i2 - 2;
                int i100 = ((Symbol) stack.elementAt(i99)).left;
                int i101 = ((Symbol) stack.elementAt(i99)).right;
                int i102 = i2 - 1;
                int i103 = ((Symbol) stack.elementAt(i102)).left;
                int i104 = ((Symbol) stack.elementAt(i102)).right;
                Object obj6 = ((Symbol) stack.elementAt(i102)).value;
                int i105 = i2 + 0;
                int i106 = ((Symbol) stack.elementAt(i105)).left;
                int i107 = ((Symbol) stack.elementAt(i105)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i99)).left, ((Symbol) stack.elementAt(i105)).right, this.parser.createStepPattern(((Integer) ((Symbol) stack.elementAt(i99)).value).intValue(), obj6, (Vector) ((Symbol) stack.elementAt(i105)).value));
            case 24:
                int i108 = i2 - 1;
                int i109 = ((Symbol) stack.elementAt(i108)).left;
                int i110 = ((Symbol) stack.elementAt(i108)).right;
                Integer num = (Integer) ((Symbol) stack.elementAt(i108)).value;
                int i111 = i2 + 0;
                int i112 = ((Symbol) stack.elementAt(i111)).left;
                int i113 = ((Symbol) stack.elementAt(i111)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i108)).left, ((Symbol) stack.elementAt(i111)).right, (StepPattern) ((Symbol) stack.elementAt(i111)).value);
            case 25:
                int i114 = i2 - 2;
                int i115 = ((Symbol) stack.elementAt(i114)).left;
                int i116 = ((Symbol) stack.elementAt(i114)).right;
                Integer num2 = (Integer) ((Symbol) stack.elementAt(i114)).value;
                int i117 = i2 - 1;
                int i118 = ((Symbol) stack.elementAt(i117)).left;
                int i119 = ((Symbol) stack.elementAt(i117)).right;
                int i120 = i2 + 0;
                int i121 = ((Symbol) stack.elementAt(i120)).left;
                int i122 = ((Symbol) stack.elementAt(i120)).right;
                return new Symbol(32, ((Symbol) stack.elementAt(i114)).left, ((Symbol) stack.elementAt(i120)).right, (ProcessingInstructionPattern) ((StepPattern) ((Symbol) stack.elementAt(i117)).value).setPredicates((Vector) ((Symbol) stack.elementAt(i120)).value));
            case 26:
                int i123 = i2 + 0;
                int i124 = ((Symbol) stack.elementAt(i123)).left;
                int i125 = ((Symbol) stack.elementAt(i123)).right;
                return new Symbol(33, ((Symbol) stack.elementAt(i123)).left, ((Symbol) stack.elementAt(i123)).right, ((Symbol) stack.elementAt(i123)).value);
            case 27:
                int i126 = i2 + 0;
                return new Symbol(33, ((Symbol) stack.elementAt(i126)).left, ((Symbol) stack.elementAt(i126)).right, new Integer(-1));
            case 28:
                int i127 = i2 + 0;
                return new Symbol(33, ((Symbol) stack.elementAt(i127)).left, ((Symbol) stack.elementAt(i127)).right, new Integer(3));
            case 29:
                int i128 = i2 + 0;
                return new Symbol(33, ((Symbol) stack.elementAt(i128)).left, ((Symbol) stack.elementAt(i128)).right, new Integer(8));
            case 30:
                int i129 = i2 + 0;
                return new Symbol(33, ((Symbol) stack.elementAt(i129)).left, ((Symbol) stack.elementAt(i129)).right, new Integer(7));
            case 31:
                int i130 = i2 + 0;
                return new Symbol(34, ((Symbol) stack.elementAt(i130)).left, ((Symbol) stack.elementAt(i130)).right, null);
            case 32:
                int i131 = i2 + 0;
                int i132 = ((Symbol) stack.elementAt(i131)).left;
                int i133 = ((Symbol) stack.elementAt(i131)).right;
                return new Symbol(34, ((Symbol) stack.elementAt(i131)).left, ((Symbol) stack.elementAt(i131)).right, (QName) ((Symbol) stack.elementAt(i131)).value);
            case 33:
                int i134 = i2 + 0;
                return new Symbol(42, ((Symbol) stack.elementAt(i134)).left, ((Symbol) stack.elementAt(i134)).right, new Integer(2));
            case 34:
                return new Symbol(42, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new Integer(3));
            case 35:
                return new Symbol(42, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new Integer(2));
            case 36:
                int i135 = i2 + 0;
                int i136 = ((Symbol) stack.elementAt(i135)).left;
                int i137 = ((Symbol) stack.elementAt(i135)).right;
                Vector vector = new Vector();
                vector.addElement((Expression) ((Symbol) stack.elementAt(i135)).value);
                return new Symbol(35, ((Symbol) stack.elementAt(i135)).left, ((Symbol) stack.elementAt(i135)).right, vector);
            case 37:
                int i138 = i2 - 1;
                int i139 = ((Symbol) stack.elementAt(i138)).left;
                int i140 = ((Symbol) stack.elementAt(i138)).right;
                int i141 = i2 + 0;
                int i142 = ((Symbol) stack.elementAt(i141)).left;
                int i143 = ((Symbol) stack.elementAt(i141)).right;
                Vector vector2 = (Vector) ((Symbol) stack.elementAt(i141)).value;
                vector2.insertElementAt((Expression) ((Symbol) stack.elementAt(i138)).value, 0);
                return new Symbol(35, ((Symbol) stack.elementAt(i138)).left, ((Symbol) stack.elementAt(i141)).right, vector2);
            case 38:
                int i144 = i2 - 1;
                int i145 = ((Symbol) stack.elementAt(i144)).left;
                int i146 = ((Symbol) stack.elementAt(i144)).right;
                return new Symbol(5, ((Symbol) stack.elementAt(i2 - 2)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new Predicate((Expression) ((Symbol) stack.elementAt(i144)).value));
            case 39:
                int i147 = i2 + 0;
                int i148 = ((Symbol) stack.elementAt(i147)).left;
                int i149 = ((Symbol) stack.elementAt(i147)).right;
                return new Symbol(2, ((Symbol) stack.elementAt(i147)).left, ((Symbol) stack.elementAt(i147)).right, (Expression) ((Symbol) stack.elementAt(i147)).value);
            case 40:
                int i150 = i2 + 0;
                int i151 = ((Symbol) stack.elementAt(i150)).left;
                int i152 = ((Symbol) stack.elementAt(i150)).right;
                return new Symbol(8, ((Symbol) stack.elementAt(i150)).left, ((Symbol) stack.elementAt(i150)).right, (Expression) ((Symbol) stack.elementAt(i150)).value);
            case 41:
                int i153 = i2 - 2;
                int i154 = ((Symbol) stack.elementAt(i153)).left;
                int i155 = ((Symbol) stack.elementAt(i153)).right;
                int i156 = i2 + 0;
                int i157 = ((Symbol) stack.elementAt(i156)).left;
                int i158 = ((Symbol) stack.elementAt(i156)).right;
                return new Symbol(8, ((Symbol) stack.elementAt(i153)).left, ((Symbol) stack.elementAt(i156)).right, new LogicalExpr(0, (Expression) ((Symbol) stack.elementAt(i153)).value, (Expression) ((Symbol) stack.elementAt(i156)).value));
            case 42:
                int i159 = i2 + 0;
                int i160 = ((Symbol) stack.elementAt(i159)).left;
                int i161 = ((Symbol) stack.elementAt(i159)).right;
                return new Symbol(9, ((Symbol) stack.elementAt(i159)).left, ((Symbol) stack.elementAt(i159)).right, (Expression) ((Symbol) stack.elementAt(i159)).value);
            case 43:
                int i162 = i2 - 2;
                int i163 = ((Symbol) stack.elementAt(i162)).left;
                int i164 = ((Symbol) stack.elementAt(i162)).right;
                int i165 = i2 + 0;
                int i166 = ((Symbol) stack.elementAt(i165)).left;
                int i167 = ((Symbol) stack.elementAt(i165)).right;
                return new Symbol(9, ((Symbol) stack.elementAt(i162)).left, ((Symbol) stack.elementAt(i165)).right, new LogicalExpr(1, (Expression) ((Symbol) stack.elementAt(i162)).value, (Expression) ((Symbol) stack.elementAt(i165)).value));
            case 44:
                int i168 = i2 + 0;
                int i169 = ((Symbol) stack.elementAt(i168)).left;
                int i170 = ((Symbol) stack.elementAt(i168)).right;
                return new Symbol(10, ((Symbol) stack.elementAt(i168)).left, ((Symbol) stack.elementAt(i168)).right, (Expression) ((Symbol) stack.elementAt(i168)).value);
            case 45:
                int i171 = i2 - 2;
                int i172 = ((Symbol) stack.elementAt(i171)).left;
                int i173 = ((Symbol) stack.elementAt(i171)).right;
                int i174 = i2 + 0;
                int i175 = ((Symbol) stack.elementAt(i174)).left;
                int i176 = ((Symbol) stack.elementAt(i174)).right;
                return new Symbol(10, ((Symbol) stack.elementAt(i171)).left, ((Symbol) stack.elementAt(i174)).right, new EqualityExpr(0, (Expression) ((Symbol) stack.elementAt(i171)).value, (Expression) ((Symbol) stack.elementAt(i174)).value));
            case 46:
                int i177 = i2 - 2;
                int i178 = ((Symbol) stack.elementAt(i177)).left;
                int i179 = ((Symbol) stack.elementAt(i177)).right;
                int i180 = i2 + 0;
                int i181 = ((Symbol) stack.elementAt(i180)).left;
                int i182 = ((Symbol) stack.elementAt(i180)).right;
                return new Symbol(10, ((Symbol) stack.elementAt(i177)).left, ((Symbol) stack.elementAt(i180)).right, new EqualityExpr(1, (Expression) ((Symbol) stack.elementAt(i177)).value, (Expression) ((Symbol) stack.elementAt(i180)).value));
            case 47:
                int i183 = i2 + 0;
                int i184 = ((Symbol) stack.elementAt(i183)).left;
                int i185 = ((Symbol) stack.elementAt(i183)).right;
                return new Symbol(11, ((Symbol) stack.elementAt(i183)).left, ((Symbol) stack.elementAt(i183)).right, (Expression) ((Symbol) stack.elementAt(i183)).value);
            case 48:
                int i186 = i2 - 2;
                int i187 = ((Symbol) stack.elementAt(i186)).left;
                int i188 = ((Symbol) stack.elementAt(i186)).right;
                int i189 = i2 + 0;
                int i190 = ((Symbol) stack.elementAt(i189)).left;
                int i191 = ((Symbol) stack.elementAt(i189)).right;
                return new Symbol(11, ((Symbol) stack.elementAt(i186)).left, ((Symbol) stack.elementAt(i189)).right, new RelationalExpr(3, (Expression) ((Symbol) stack.elementAt(i186)).value, (Expression) ((Symbol) stack.elementAt(i189)).value));
            case 49:
                int i192 = i2 - 2;
                int i193 = ((Symbol) stack.elementAt(i192)).left;
                int i194 = ((Symbol) stack.elementAt(i192)).right;
                int i195 = i2 + 0;
                int i196 = ((Symbol) stack.elementAt(i195)).left;
                int i197 = ((Symbol) stack.elementAt(i195)).right;
                return new Symbol(11, ((Symbol) stack.elementAt(i192)).left, ((Symbol) stack.elementAt(i195)).right, new RelationalExpr(2, (Expression) ((Symbol) stack.elementAt(i192)).value, (Expression) ((Symbol) stack.elementAt(i195)).value));
            case 50:
                int i198 = i2 - 2;
                int i199 = ((Symbol) stack.elementAt(i198)).left;
                int i200 = ((Symbol) stack.elementAt(i198)).right;
                int i201 = i2 + 0;
                int i202 = ((Symbol) stack.elementAt(i201)).left;
                int i203 = ((Symbol) stack.elementAt(i201)).right;
                return new Symbol(11, ((Symbol) stack.elementAt(i198)).left, ((Symbol) stack.elementAt(i201)).right, new RelationalExpr(5, (Expression) ((Symbol) stack.elementAt(i198)).value, (Expression) ((Symbol) stack.elementAt(i201)).value));
            case 51:
                int i204 = i2 - 2;
                int i205 = ((Symbol) stack.elementAt(i204)).left;
                int i206 = ((Symbol) stack.elementAt(i204)).right;
                int i207 = i2 + 0;
                int i208 = ((Symbol) stack.elementAt(i207)).left;
                int i209 = ((Symbol) stack.elementAt(i207)).right;
                return new Symbol(11, ((Symbol) stack.elementAt(i204)).left, ((Symbol) stack.elementAt(i207)).right, new RelationalExpr(4, (Expression) ((Symbol) stack.elementAt(i204)).value, (Expression) ((Symbol) stack.elementAt(i207)).value));
            case 52:
                int i210 = i2 + 0;
                int i211 = ((Symbol) stack.elementAt(i210)).left;
                int i212 = ((Symbol) stack.elementAt(i210)).right;
                return new Symbol(12, ((Symbol) stack.elementAt(i210)).left, ((Symbol) stack.elementAt(i210)).right, (Expression) ((Symbol) stack.elementAt(i210)).value);
            case 53:
                int i213 = i2 - 2;
                int i214 = ((Symbol) stack.elementAt(i213)).left;
                int i215 = ((Symbol) stack.elementAt(i213)).right;
                int i216 = i2 + 0;
                int i217 = ((Symbol) stack.elementAt(i216)).left;
                int i218 = ((Symbol) stack.elementAt(i216)).right;
                return new Symbol(12, ((Symbol) stack.elementAt(i213)).left, ((Symbol) stack.elementAt(i216)).right, new BinOpExpr(0, (Expression) ((Symbol) stack.elementAt(i213)).value, (Expression) ((Symbol) stack.elementAt(i216)).value));
            case 54:
                int i219 = i2 - 2;
                int i220 = ((Symbol) stack.elementAt(i219)).left;
                int i221 = ((Symbol) stack.elementAt(i219)).right;
                int i222 = i2 + 0;
                int i223 = ((Symbol) stack.elementAt(i222)).left;
                int i224 = ((Symbol) stack.elementAt(i222)).right;
                return new Symbol(12, ((Symbol) stack.elementAt(i219)).left, ((Symbol) stack.elementAt(i222)).right, new BinOpExpr(1, (Expression) ((Symbol) stack.elementAt(i219)).value, (Expression) ((Symbol) stack.elementAt(i222)).value));
            case 55:
                int i225 = i2 + 0;
                int i226 = ((Symbol) stack.elementAt(i225)).left;
                int i227 = ((Symbol) stack.elementAt(i225)).right;
                return new Symbol(13, ((Symbol) stack.elementAt(i225)).left, ((Symbol) stack.elementAt(i225)).right, (Expression) ((Symbol) stack.elementAt(i225)).value);
            case 56:
                int i228 = i2 - 2;
                int i229 = ((Symbol) stack.elementAt(i228)).left;
                int i230 = ((Symbol) stack.elementAt(i228)).right;
                int i231 = i2 + 0;
                int i232 = ((Symbol) stack.elementAt(i231)).left;
                int i233 = ((Symbol) stack.elementAt(i231)).right;
                return new Symbol(13, ((Symbol) stack.elementAt(i228)).left, ((Symbol) stack.elementAt(i231)).right, new BinOpExpr(2, (Expression) ((Symbol) stack.elementAt(i228)).value, (Expression) ((Symbol) stack.elementAt(i231)).value));
            case 57:
                int i234 = i2 - 2;
                int i235 = ((Symbol) stack.elementAt(i234)).left;
                int i236 = ((Symbol) stack.elementAt(i234)).right;
                int i237 = i2 + 0;
                int i238 = ((Symbol) stack.elementAt(i237)).left;
                int i239 = ((Symbol) stack.elementAt(i237)).right;
                return new Symbol(13, ((Symbol) stack.elementAt(i234)).left, ((Symbol) stack.elementAt(i237)).right, new BinOpExpr(3, (Expression) ((Symbol) stack.elementAt(i234)).value, (Expression) ((Symbol) stack.elementAt(i237)).value));
            case 58:
                int i240 = i2 - 2;
                int i241 = ((Symbol) stack.elementAt(i240)).left;
                int i242 = ((Symbol) stack.elementAt(i240)).right;
                int i243 = i2 + 0;
                int i244 = ((Symbol) stack.elementAt(i243)).left;
                int i245 = ((Symbol) stack.elementAt(i243)).right;
                return new Symbol(13, ((Symbol) stack.elementAt(i240)).left, ((Symbol) stack.elementAt(i243)).right, new BinOpExpr(4, (Expression) ((Symbol) stack.elementAt(i240)).value, (Expression) ((Symbol) stack.elementAt(i243)).value));
            case 59:
                int i246 = i2 + 0;
                int i247 = ((Symbol) stack.elementAt(i246)).left;
                int i248 = ((Symbol) stack.elementAt(i246)).right;
                return new Symbol(14, ((Symbol) stack.elementAt(i246)).left, ((Symbol) stack.elementAt(i246)).right, (Expression) ((Symbol) stack.elementAt(i246)).value);
            case 60:
                int i249 = i2 + 0;
                int i250 = ((Symbol) stack.elementAt(i249)).left;
                int i251 = ((Symbol) stack.elementAt(i249)).right;
                return new Symbol(14, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i249)).right, new UnaryOpExpr((Expression) ((Symbol) stack.elementAt(i249)).value));
            case 61:
                int i252 = i2 + 0;
                int i253 = ((Symbol) stack.elementAt(i252)).left;
                int i254 = ((Symbol) stack.elementAt(i252)).right;
                return new Symbol(18, ((Symbol) stack.elementAt(i252)).left, ((Symbol) stack.elementAt(i252)).right, (Expression) ((Symbol) stack.elementAt(i252)).value);
            case 62:
                int i255 = i2 - 2;
                int i256 = ((Symbol) stack.elementAt(i255)).left;
                int i257 = ((Symbol) stack.elementAt(i255)).right;
                int i258 = i2 + 0;
                int i259 = ((Symbol) stack.elementAt(i258)).left;
                int i260 = ((Symbol) stack.elementAt(i258)).right;
                return new Symbol(18, ((Symbol) stack.elementAt(i255)).left, ((Symbol) stack.elementAt(i258)).right, new UnionPathExpr((Expression) ((Symbol) stack.elementAt(i255)).value, (Expression) ((Symbol) stack.elementAt(i258)).value));
            case 63:
                int i261 = i2 + 0;
                int i262 = ((Symbol) stack.elementAt(i261)).left;
                int i263 = ((Symbol) stack.elementAt(i261)).right;
                return new Symbol(19, ((Symbol) stack.elementAt(i261)).left, ((Symbol) stack.elementAt(i261)).right, (Expression) ((Symbol) stack.elementAt(i261)).value);
            case 64:
                int i264 = i2 + 0;
                int i265 = ((Symbol) stack.elementAt(i264)).left;
                int i266 = ((Symbol) stack.elementAt(i264)).right;
                return new Symbol(19, ((Symbol) stack.elementAt(i264)).left, ((Symbol) stack.elementAt(i264)).right, (Expression) ((Symbol) stack.elementAt(i264)).value);
            case 65:
                int i267 = i2 - 2;
                int i268 = ((Symbol) stack.elementAt(i267)).left;
                int i269 = ((Symbol) stack.elementAt(i267)).right;
                int i270 = i2 + 0;
                int i271 = ((Symbol) stack.elementAt(i270)).left;
                int i272 = ((Symbol) stack.elementAt(i270)).right;
                return new Symbol(19, ((Symbol) stack.elementAt(i267)).left, ((Symbol) stack.elementAt(i270)).right, new FilterParentPath((Expression) ((Symbol) stack.elementAt(i267)).value, (Expression) ((Symbol) stack.elementAt(i270)).value));
            case 66:
                int i273 = i2 - 2;
                int i274 = ((Symbol) stack.elementAt(i273)).left;
                int i275 = ((Symbol) stack.elementAt(i273)).right;
                Expression expression = (Expression) ((Symbol) stack.elementAt(i273)).value;
                int i276 = i2 + 0;
                int i277 = ((Symbol) stack.elementAt(i276)).left;
                int i278 = ((Symbol) stack.elementAt(i276)).right;
                Expression expression2 = (Expression) ((Symbol) stack.elementAt(i276)).value;
                if (!(expression2 instanceof Step) || !this.parser.isElementAxis(((Step) expression2).getAxis())) {
                    i4 = -1;
                }
                FilterParentPath filterParentPath = new FilterParentPath(new FilterParentPath(expression, new Step(5, i4, null)), expression2);
                if (!(expression instanceof KeyCall)) {
                    filterParentPath.setDescendantAxis();
                }
                return new Symbol(19, ((Symbol) stack.elementAt(i273)).left, ((Symbol) stack.elementAt(i276)).right, filterParentPath);
            case 67:
                int i279 = i2 + 0;
                int i280 = ((Symbol) stack.elementAt(i279)).left;
                int i281 = ((Symbol) stack.elementAt(i279)).right;
                return new Symbol(4, ((Symbol) stack.elementAt(i279)).left, ((Symbol) stack.elementAt(i279)).right, (Expression) ((Symbol) stack.elementAt(i279)).value);
            case 68:
                int i282 = i2 + 0;
                int i283 = ((Symbol) stack.elementAt(i282)).left;
                int i284 = ((Symbol) stack.elementAt(i282)).right;
                return new Symbol(4, ((Symbol) stack.elementAt(i282)).left, ((Symbol) stack.elementAt(i282)).right, (Expression) ((Symbol) stack.elementAt(i282)).value);
            case 69:
                int i285 = i2 + 0;
                int i286 = ((Symbol) stack.elementAt(i285)).left;
                int i287 = ((Symbol) stack.elementAt(i285)).right;
                return new Symbol(21, ((Symbol) stack.elementAt(i285)).left, ((Symbol) stack.elementAt(i285)).right, (Expression) ((Symbol) stack.elementAt(i285)).value);
            case 70:
                int i288 = i2 - 2;
                int i289 = ((Symbol) stack.elementAt(i288)).left;
                int i290 = ((Symbol) stack.elementAt(i288)).right;
                Expression expression3 = (Expression) ((Symbol) stack.elementAt(i288)).value;
                int i291 = i2 + 0;
                int i292 = ((Symbol) stack.elementAt(i291)).left;
                int i293 = ((Symbol) stack.elementAt(i291)).right;
                Expression expression4 = (Expression) ((Symbol) stack.elementAt(i291)).value;
                if ((expression3 instanceof Step) && ((Step) expression3).isAbbreviatedDot()) {
                    expression3 = expression4;
                } else if (!((Step) expression4).isAbbreviatedDot()) {
                    expression3 = new ParentLocationPath((RelativeLocationPath) expression3, expression4);
                }
                return new Symbol(21, ((Symbol) stack.elementAt(i288)).left, ((Symbol) stack.elementAt(i291)).right, expression3);
            case 71:
                int i294 = i2 + 0;
                int i295 = ((Symbol) stack.elementAt(i294)).left;
                int i296 = ((Symbol) stack.elementAt(i294)).right;
                return new Symbol(21, ((Symbol) stack.elementAt(i294)).left, ((Symbol) stack.elementAt(i294)).right, (Expression) ((Symbol) stack.elementAt(i294)).value);
            case 72:
                int i297 = i2 + 0;
                return new Symbol(23, ((Symbol) stack.elementAt(i297)).left, ((Symbol) stack.elementAt(i297)).right, new AbsoluteLocationPath());
            case 73:
                int i298 = i2 + 0;
                int i299 = ((Symbol) stack.elementAt(i298)).left;
                int i300 = ((Symbol) stack.elementAt(i298)).right;
                return new Symbol(23, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i298)).right, new AbsoluteLocationPath((Expression) ((Symbol) stack.elementAt(i298)).value));
            case 74:
                int i301 = i2 + 0;
                int i302 = ((Symbol) stack.elementAt(i301)).left;
                int i303 = ((Symbol) stack.elementAt(i301)).right;
                return new Symbol(23, ((Symbol) stack.elementAt(i301)).left, ((Symbol) stack.elementAt(i301)).right, (Expression) ((Symbol) stack.elementAt(i301)).value);
            case 75:
                int i304 = i2 - 2;
                int i305 = ((Symbol) stack.elementAt(i304)).left;
                int i306 = ((Symbol) stack.elementAt(i304)).right;
                Expression expression5 = (Expression) ((Symbol) stack.elementAt(i304)).value;
                int i307 = i2 + 0;
                int i308 = ((Symbol) stack.elementAt(i307)).left;
                int i309 = ((Symbol) stack.elementAt(i307)).right;
                Step step3 = (Step) ((Expression) ((Symbol) stack.elementAt(i307)).value);
                int axis = step3.getAxis();
                int nodeType = step3.getNodeType();
                Vector predicates = step3.getPredicates();
                if (axis == 3 && nodeType != 2) {
                    if (predicates == null) {
                        step3.setAxis(4);
                        if (expression5 instanceof Step) {
                            constants = step3;
                            break;
                        }
                        parentLocationPath = new ParentLocationPath((RelativeLocationPath) expression5, step3);
                    } else if (!(expression5 instanceof Step) || !((Step) expression5).isAbbreviatedDot()) {
                        constants = new ParentLocationPath((RelativeLocationPath) expression5, new ParentLocationPath(new Step(5, 1, null), step3));
                    } else {
                        parentLocationPath = new ParentLocationPath(new Step(5, 1, null), step3);
                    }
                    constants = parentLocationPath;
                } else if (axis == 2 || nodeType == 2) {
                    constants = new ParentLocationPath((RelativeLocationPath) expression5, new ParentLocationPath(new Step(5, 1, null), step3));
                } else {
                    constants = new ParentLocationPath((RelativeLocationPath) expression5, new ParentLocationPath(new Step(5, -1, null), step3));
                }
                return new Symbol(22, ((Symbol) stack.elementAt(i304)).left, ((Symbol) stack.elementAt(i307)).right, constants);
            case 76:
                int i310 = i2 + 0;
                int i311 = ((Symbol) stack.elementAt(i310)).left;
                int i312 = ((Symbol) stack.elementAt(i310)).right;
                Expression expression6 = (Expression) ((Symbol) stack.elementAt(i310)).value;
                if ((expression6 instanceof Step) && this.parser.isElementAxis(((Step) expression6).getAxis())) {
                    i3 = 1;
                }
                return new Symbol(24, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i310)).right, new AbsoluteLocationPath(this.parser.insertStep(new Step(5, i3, null), (RelativeLocationPath) expression6)));
            case 77:
                int i313 = i2 + 0;
                int i314 = ((Symbol) stack.elementAt(i313)).left;
                int i315 = ((Symbol) stack.elementAt(i313)).right;
                Object obj7 = ((Symbol) stack.elementAt(i313)).value;
                if (obj7 instanceof Step) {
                    step = (Step) obj7;
                } else {
                    step = new Step(3, this.parser.findNodeType(3, obj7), null);
                }
                return new Symbol(7, ((Symbol) stack.elementAt(i313)).left, ((Symbol) stack.elementAt(i313)).right, step);
            case 78:
                int i316 = i2 - 1;
                int i317 = ((Symbol) stack.elementAt(i316)).left;
                int i318 = ((Symbol) stack.elementAt(i316)).right;
                Object obj8 = ((Symbol) stack.elementAt(i316)).value;
                int i319 = i2 + 0;
                int i320 = ((Symbol) stack.elementAt(i319)).left;
                int i321 = ((Symbol) stack.elementAt(i319)).right;
                Vector vector3 = (Vector) ((Symbol) stack.elementAt(i319)).value;
                if (obj8 instanceof Step) {
                    step2 = (Step) obj8;
                    step2.addPredicates(vector3);
                } else {
                    step2 = new Step(3, this.parser.findNodeType(3, obj8), vector3);
                }
                return new Symbol(7, ((Symbol) stack.elementAt(i316)).left, ((Symbol) stack.elementAt(i319)).right, step2);
            case 79:
                int i322 = i2 - 2;
                int i323 = ((Symbol) stack.elementAt(i322)).left;
                int i324 = ((Symbol) stack.elementAt(i322)).right;
                Integer num3 = (Integer) ((Symbol) stack.elementAt(i322)).value;
                int i325 = i2 - 1;
                int i326 = ((Symbol) stack.elementAt(i325)).left;
                int i327 = ((Symbol) stack.elementAt(i325)).right;
                Object obj9 = ((Symbol) stack.elementAt(i325)).value;
                int i328 = i2 + 0;
                int i329 = ((Symbol) stack.elementAt(i328)).left;
                int i330 = ((Symbol) stack.elementAt(i328)).right;
                return new Symbol(7, ((Symbol) stack.elementAt(i322)).left, ((Symbol) stack.elementAt(i328)).right, new Step(num3.intValue(), this.parser.findNodeType(num3.intValue(), obj9), (Vector) ((Symbol) stack.elementAt(i328)).value));
            case 80:
                int i331 = i2 - 1;
                int i332 = ((Symbol) stack.elementAt(i331)).left;
                int i333 = ((Symbol) stack.elementAt(i331)).right;
                Integer num4 = (Integer) ((Symbol) stack.elementAt(i331)).value;
                int i334 = i2 + 0;
                int i335 = ((Symbol) stack.elementAt(i334)).left;
                int i336 = ((Symbol) stack.elementAt(i334)).right;
                return new Symbol(7, ((Symbol) stack.elementAt(i331)).left, ((Symbol) stack.elementAt(i334)).right, new Step(num4.intValue(), this.parser.findNodeType(num4.intValue(), ((Symbol) stack.elementAt(i334)).value), null));
            case 81:
                int i337 = i2 + 0;
                int i338 = ((Symbol) stack.elementAt(i337)).left;
                int i339 = ((Symbol) stack.elementAt(i337)).right;
                return new Symbol(7, ((Symbol) stack.elementAt(i337)).left, ((Symbol) stack.elementAt(i337)).right, (Expression) ((Symbol) stack.elementAt(i337)).value);
            case 82:
                int i340 = i2 - 1;
                int i341 = ((Symbol) stack.elementAt(i340)).left;
                int i342 = ((Symbol) stack.elementAt(i340)).right;
                return new Symbol(41, ((Symbol) stack.elementAt(i340)).left, ((Symbol) stack.elementAt(i2 + 0)).right, (Integer) ((Symbol) stack.elementAt(i340)).value);
            case 83:
                int i343 = i2 + 0;
                return new Symbol(41, ((Symbol) stack.elementAt(i343)).left, ((Symbol) stack.elementAt(i343)).right, new Integer(2));
            case 84:
                int i344 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i344)).left, ((Symbol) stack.elementAt(i344)).right, new Integer(0));
            case 85:
                int i345 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i345)).left, ((Symbol) stack.elementAt(i345)).right, new Integer(1));
            case 86:
                int i346 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i346)).left, ((Symbol) stack.elementAt(i346)).right, new Integer(2));
            case 87:
                int i347 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i347)).left, ((Symbol) stack.elementAt(i347)).right, new Integer(3));
            case 88:
                int i348 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i348)).left, ((Symbol) stack.elementAt(i348)).right, new Integer(4));
            case 89:
                int i349 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i349)).left, ((Symbol) stack.elementAt(i349)).right, new Integer(5));
            case 90:
                int i350 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i350)).left, ((Symbol) stack.elementAt(i350)).right, new Integer(6));
            case 91:
                int i351 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i351)).left, ((Symbol) stack.elementAt(i351)).right, new Integer(7));
            case 92:
                int i352 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i352)).left, ((Symbol) stack.elementAt(i352)).right, new Integer(9));
            case 93:
                int i353 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i353)).left, ((Symbol) stack.elementAt(i353)).right, new Integer(10));
            case 94:
                int i354 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i354)).left, ((Symbol) stack.elementAt(i354)).right, new Integer(11));
            case 95:
                int i355 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i355)).left, ((Symbol) stack.elementAt(i355)).right, new Integer(12));
            case 96:
                int i356 = i2 + 0;
                return new Symbol(40, ((Symbol) stack.elementAt(i356)).left, ((Symbol) stack.elementAt(i356)).right, new Integer(13));
            case 97:
                int i357 = i2 + 0;
                return new Symbol(20, ((Symbol) stack.elementAt(i357)).left, ((Symbol) stack.elementAt(i357)).right, new Step(13, -1, null));
            case 98:
                int i358 = i2 + 0;
                return new Symbol(20, ((Symbol) stack.elementAt(i358)).left, ((Symbol) stack.elementAt(i358)).right, new Step(10, -1, null));
            case 99:
                int i359 = i2 + 0;
                int i360 = ((Symbol) stack.elementAt(i359)).left;
                int i361 = ((Symbol) stack.elementAt(i359)).right;
                return new Symbol(6, ((Symbol) stack.elementAt(i359)).left, ((Symbol) stack.elementAt(i359)).right, (Expression) ((Symbol) stack.elementAt(i359)).value);
            case 100:
                int i362 = i2 - 1;
                int i363 = ((Symbol) stack.elementAt(i362)).left;
                int i364 = ((Symbol) stack.elementAt(i362)).right;
                int i365 = i2 + 0;
                int i366 = ((Symbol) stack.elementAt(i365)).left;
                int i367 = ((Symbol) stack.elementAt(i365)).right;
                return new Symbol(6, ((Symbol) stack.elementAt(i362)).left, ((Symbol) stack.elementAt(i365)).right, new FilterExpr((Expression) ((Symbol) stack.elementAt(i362)).value, (Vector) ((Symbol) stack.elementAt(i365)).value));
            case 101:
                int i368 = i2 + 0;
                int i369 = ((Symbol) stack.elementAt(i368)).left;
                int i370 = ((Symbol) stack.elementAt(i368)).right;
                return new Symbol(17, ((Symbol) stack.elementAt(i368)).left, ((Symbol) stack.elementAt(i368)).right, (Expression) ((Symbol) stack.elementAt(i368)).value);
            case 102:
                int i371 = i2 - 1;
                int i372 = ((Symbol) stack.elementAt(i371)).left;
                int i373 = ((Symbol) stack.elementAt(i371)).right;
                return new Symbol(17, ((Symbol) stack.elementAt(i2 - 2)).left, ((Symbol) stack.elementAt(i2 + 0)).right, (Expression) ((Symbol) stack.elementAt(i371)).value);
            case 103:
                int i374 = i2 + 0;
                int i375 = ((Symbol) stack.elementAt(i374)).left;
                int i376 = ((Symbol) stack.elementAt(i374)).right;
                String str2 = (String) ((Symbol) stack.elementAt(i374)).value;
                int lastIndexOf = str2.lastIndexOf(58);
                if (lastIndexOf > 0) {
                    str = this.parser._symbolTable.lookupNamespace(str2.substring(0, lastIndexOf));
                }
                if (str == null) {
                    literalExpr = new LiteralExpr(str2);
                } else {
                    literalExpr = new LiteralExpr(str2, str);
                }
                return new Symbol(17, ((Symbol) stack.elementAt(i374)).left, ((Symbol) stack.elementAt(i374)).right, literalExpr);
            case 104:
                int i377 = i2 + 0;
                int i378 = ((Symbol) stack.elementAt(i377)).left;
                int i379 = ((Symbol) stack.elementAt(i377)).right;
                Long l = (Long) ((Symbol) stack.elementAt(i377)).value;
                long longValue = l.longValue();
                if (longValue < -2147483648L || longValue > 2147483647L) {
                    obj = new RealExpr((double) longValue);
                } else if (l.doubleValue() == XPath.MATCH_SCORE_QNAME) {
                    obj = new RealExpr(l.doubleValue());
                } else if (l.intValue() == 0) {
                    obj = new IntExpr(l.intValue());
                } else if (l.doubleValue() == XPath.MATCH_SCORE_QNAME) {
                    obj = new RealExpr(l.doubleValue());
                } else {
                    obj = new IntExpr(l.intValue());
                }
                return new Symbol(17, ((Symbol) stack.elementAt(i377)).left, ((Symbol) stack.elementAt(i377)).right, obj);
            case 105:
                int i380 = i2 + 0;
                int i381 = ((Symbol) stack.elementAt(i380)).left;
                int i382 = ((Symbol) stack.elementAt(i380)).right;
                return new Symbol(17, ((Symbol) stack.elementAt(i380)).left, ((Symbol) stack.elementAt(i380)).right, new RealExpr(((Double) ((Symbol) stack.elementAt(i380)).value).doubleValue()));
            case 106:
                int i383 = i2 + 0;
                int i384 = ((Symbol) stack.elementAt(i383)).left;
                int i385 = ((Symbol) stack.elementAt(i383)).right;
                return new Symbol(17, ((Symbol) stack.elementAt(i383)).left, ((Symbol) stack.elementAt(i383)).right, (Expression) ((Symbol) stack.elementAt(i383)).value);
            case 107:
                int i386 = i2 + 0;
                int i387 = ((Symbol) stack.elementAt(i386)).left;
                int i388 = ((Symbol) stack.elementAt(i386)).right;
                QName qName = (QName) ((Symbol) stack.elementAt(i386)).value;
                SyntaxTreeNode lookupName = this.parser.lookupName(qName);
                if (lookupName != null) {
                    if (lookupName instanceof Variable) {
                        obj4 = new VariableRef((Variable) lookupName);
                    } else if (lookupName instanceof Param) {
                        obj4 = new ParameterRef((Param) lookupName);
                    } else {
                        obj4 = new UnresolvedRef(qName);
                    }
                }
                if (lookupName == null) {
                    obj4 = new UnresolvedRef(qName);
                }
                return new Symbol(15, ((Symbol) stack.elementAt(i2 - 1)).left, ((Symbol) stack.elementAt(i386)).right, obj4);
            case 108:
                int i389 = i2 - 2;
                int i390 = ((Symbol) stack.elementAt(i389)).left;
                int i391 = ((Symbol) stack.elementAt(i389)).right;
                QName qName2 = (QName) ((Symbol) stack.elementAt(i389)).value;
                if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_CURRENT_STRING)) {
                    obj2 = new CurrentCall(qName2);
                } else {
                    if (qName2 == this.parser.getQNameIgnoreDefaultNs("number")) {
                        XPathParser xPathParser = this.parser;
                        functionCall = new NumberCall(qName2, XPathParser.EmptyArgs);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs("string")) {
                        XPathParser xPathParser2 = this.parser;
                        functionCall = new StringCall(qName2, XPathParser.EmptyArgs);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_CONCAT_STRING)) {
                        XPathParser xPathParser3 = this.parser;
                        functionCall = new ConcatCall(qName2, XPathParser.EmptyArgs);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs("true")) {
                        obj2 = new BooleanExpr(true);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs("false")) {
                        obj2 = new BooleanExpr(false);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs("name")) {
                        obj2 = new NameCall(qName2);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_GENERATE_ID_STRING)) {
                        XPathParser xPathParser4 = this.parser;
                        functionCall = new GenerateIdCall(qName2, XPathParser.EmptyArgs);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_STRING_LENGTH_STRING)) {
                        XPathParser xPathParser5 = this.parser;
                        functionCall = new StringLengthCall(qName2, XPathParser.EmptyArgs);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_POSITION_STRING)) {
                        obj2 = new PositionCall(qName2);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_LAST_STRING)) {
                        obj2 = new LastCall(qName2);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_LOCAL_PART_STRING)) {
                        obj2 = new LocalNameCall(qName2);
                    } else if (qName2 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_NAMESPACE_STRING)) {
                        obj2 = new NamespaceUriCall(qName2);
                    } else {
                        XPathParser xPathParser6 = this.parser;
                        functionCall = new FunctionCall(qName2, XPathParser.EmptyArgs);
                    }
                    obj2 = functionCall;
                }
                return new Symbol(16, ((Symbol) stack.elementAt(i389)).left, ((Symbol) stack.elementAt(i2 + 0)).right, obj2);
            case 109:
                int i392 = i2 - 3;
                int i393 = ((Symbol) stack.elementAt(i392)).left;
                int i394 = ((Symbol) stack.elementAt(i392)).right;
                QName qName3 = (QName) ((Symbol) stack.elementAt(i392)).value;
                int i395 = i2 - 1;
                int i396 = ((Symbol) stack.elementAt(i395)).left;
                int i397 = ((Symbol) stack.elementAt(i395)).right;
                Vector vector4 = (Vector) ((Symbol) stack.elementAt(i395)).value;
                if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_CONCAT_STRING)) {
                    obj3 = new ConcatCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("number")) {
                    obj3 = new NumberCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Constants.DOCUMENT_PNAME)) {
                    this.parser.setMultiDocument(true);
                    obj3 = new DocumentCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("string")) {
                    obj3 = new StringCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("boolean")) {
                    obj3 = new BooleanCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("name")) {
                    obj3 = new NameCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_GENERATE_ID_STRING)) {
                    obj3 = new GenerateIdCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_NOT_STRING)) {
                    obj3 = new NotCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("format-number")) {
                    obj3 = new FormatNumberCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_UNPARSED_ENTITY_URI_STRING)) {
                    obj3 = new UnparsedEntityUriCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("key")) {
                    obj3 = new KeyCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("id")) {
                    KeyCall keyCall = new KeyCall(qName3, vector4);
                    this.parser.setHasIdCall(true);
                    obj3 = keyCall;
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_CEILING_STRING)) {
                    obj3 = new CeilingCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_ROUND_STRING)) {
                    obj3 = new RoundCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_FLOOR_STRING)) {
                    obj3 = new FloorCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_CONTAINS_STRING)) {
                    obj3 = new ContainsCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_STRING_LENGTH_STRING)) {
                    obj3 = new StringLengthCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_STARTS_WITH_STRING)) {
                    obj3 = new StartsWithCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING)) {
                    obj3 = new FunctionAvailableCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING)) {
                    obj3 = new ElementAvailableCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_LOCAL_PART_STRING)) {
                    obj3 = new LocalNameCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs("lang")) {
                    obj3 = new LangCall(qName3, vector4);
                } else if (qName3 == this.parser.getQNameIgnoreDefaultNs(Keywords.FUNC_NAMESPACE_STRING)) {
                    obj3 = new NamespaceUriCall(qName3, vector4);
                } else if (qName3 == this.parser.getQName(Constants.TRANSLET_URI, "xsltc", "cast")) {
                    obj3 = new CastCall(qName3, vector4);
                } else if (qName3.getLocalPart().equals("nodeset") || qName3.getLocalPart().equals("node-set")) {
                    this.parser.setCallsNodeset(true);
                    obj3 = new FunctionCall(qName3, vector4);
                } else {
                    obj3 = new FunctionCall(qName3, vector4);
                }
                return new Symbol(16, ((Symbol) stack.elementAt(i392)).left, ((Symbol) stack.elementAt(i2 + 0)).right, obj3);
            case 110:
                int i398 = i2 + 0;
                int i399 = ((Symbol) stack.elementAt(i398)).left;
                int i400 = ((Symbol) stack.elementAt(i398)).right;
                Vector vector5 = new Vector();
                vector5.addElement((Expression) ((Symbol) stack.elementAt(i398)).value);
                return new Symbol(36, ((Symbol) stack.elementAt(i398)).left, ((Symbol) stack.elementAt(i398)).right, vector5);
            case 111:
                int i401 = i2 - 2;
                int i402 = ((Symbol) stack.elementAt(i401)).left;
                int i403 = ((Symbol) stack.elementAt(i401)).right;
                int i404 = i2 + 0;
                int i405 = ((Symbol) stack.elementAt(i404)).left;
                int i406 = ((Symbol) stack.elementAt(i404)).right;
                Vector vector6 = (Vector) ((Symbol) stack.elementAt(i404)).value;
                vector6.insertElementAt((Expression) ((Symbol) stack.elementAt(i401)).value, 0);
                return new Symbol(36, ((Symbol) stack.elementAt(i401)).left, ((Symbol) stack.elementAt(i404)).right, vector6);
            case 112:
                int i407 = i2 + 0;
                int i408 = ((Symbol) stack.elementAt(i407)).left;
                int i409 = ((Symbol) stack.elementAt(i407)).right;
                return new Symbol(38, ((Symbol) stack.elementAt(i407)).left, ((Symbol) stack.elementAt(i407)).right, (QName) ((Symbol) stack.elementAt(i407)).value);
            case 113:
                int i410 = i2 + 0;
                int i411 = ((Symbol) stack.elementAt(i410)).left;
                int i412 = ((Symbol) stack.elementAt(i410)).right;
                return new Symbol(39, ((Symbol) stack.elementAt(i410)).left, ((Symbol) stack.elementAt(i410)).right, (QName) ((Symbol) stack.elementAt(i410)).value);
            case 114:
                int i413 = i2 + 0;
                int i414 = ((Symbol) stack.elementAt(i413)).left;
                int i415 = ((Symbol) stack.elementAt(i413)).right;
                return new Symbol(3, ((Symbol) stack.elementAt(i413)).left, ((Symbol) stack.elementAt(i413)).right, (Expression) ((Symbol) stack.elementAt(i413)).value);
            case 115:
                int i416 = i2 + 0;
                int i417 = ((Symbol) stack.elementAt(i416)).left;
                int i418 = ((Symbol) stack.elementAt(i416)).right;
                return new Symbol(25, ((Symbol) stack.elementAt(i416)).left, ((Symbol) stack.elementAt(i416)).right, ((Symbol) stack.elementAt(i416)).value);
            case 116:
                int i419 = i2 + 0;
                return new Symbol(25, ((Symbol) stack.elementAt(i419)).left, ((Symbol) stack.elementAt(i419)).right, new Integer(-1));
            case 117:
                int i420 = i2 + 0;
                return new Symbol(25, ((Symbol) stack.elementAt(i420)).left, ((Symbol) stack.elementAt(i420)).right, new Integer(3));
            case 118:
                int i421 = i2 + 0;
                return new Symbol(25, ((Symbol) stack.elementAt(i421)).left, ((Symbol) stack.elementAt(i421)).right, new Integer(8));
            case 119:
                int i422 = i2 - 1;
                int i423 = ((Symbol) stack.elementAt(i422)).left;
                int i424 = ((Symbol) stack.elementAt(i422)).right;
                EqualityExpr equalityExpr = new EqualityExpr(0, new NameCall(this.parser.getQNameIgnoreDefaultNs("name")), new LiteralExpr((String) ((Symbol) stack.elementAt(i422)).value));
                Vector vector7 = new Vector();
                vector7.addElement(new Predicate(equalityExpr));
                return new Symbol(25, ((Symbol) stack.elementAt(i2 - 3)).left, ((Symbol) stack.elementAt(i2 + 0)).right, new Step(3, 7, vector7));
            case 120:
                int i425 = i2 + 0;
                return new Symbol(25, ((Symbol) stack.elementAt(i425)).left, ((Symbol) stack.elementAt(i425)).right, new Integer(7));
            case 121:
                int i426 = i2 + 0;
                return new Symbol(26, ((Symbol) stack.elementAt(i426)).left, ((Symbol) stack.elementAt(i426)).right, null);
            case 122:
                int i427 = i2 + 0;
                int i428 = ((Symbol) stack.elementAt(i427)).left;
                int i429 = ((Symbol) stack.elementAt(i427)).right;
                return new Symbol(26, ((Symbol) stack.elementAt(i427)).left, ((Symbol) stack.elementAt(i427)).right, (QName) ((Symbol) stack.elementAt(i427)).value);
            case 123:
                int i430 = i2 + 0;
                int i431 = ((Symbol) stack.elementAt(i430)).left;
                int i432 = ((Symbol) stack.elementAt(i430)).right;
                return new Symbol(37, ((Symbol) stack.elementAt(i430)).left, ((Symbol) stack.elementAt(i430)).right, this.parser.getQNameIgnoreDefaultNs((String) ((Symbol) stack.elementAt(i430)).value));
            case 124:
                int i433 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i433)).left, ((Symbol) stack.elementAt(i433)).right, this.parser.getQNameIgnoreDefaultNs("div"));
            case 125:
                int i434 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i434)).left, ((Symbol) stack.elementAt(i434)).right, this.parser.getQNameIgnoreDefaultNs("mod"));
            case 126:
                int i435 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i435)).left, ((Symbol) stack.elementAt(i435)).right, this.parser.getQNameIgnoreDefaultNs("key"));
            case 127:
                int i436 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i436)).left, ((Symbol) stack.elementAt(i436)).right, this.parser.getQNameIgnoreDefaultNs("child"));
            case 128:
                int i437 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i437)).left, ((Symbol) stack.elementAt(i437)).right, this.parser.getQNameIgnoreDefaultNs("ancestor-or-self"));
            case 129:
                int i438 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i438)).left, ((Symbol) stack.elementAt(i438)).right, this.parser.getQNameIgnoreDefaultNs("attribute"));
            case 130:
                int i439 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i439)).left, ((Symbol) stack.elementAt(i439)).right, this.parser.getQNameIgnoreDefaultNs("child"));
            case 131:
                int i440 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i440)).left, ((Symbol) stack.elementAt(i440)).right, this.parser.getQNameIgnoreDefaultNs("decendant"));
            case 132:
                int i441 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i441)).left, ((Symbol) stack.elementAt(i441)).right, this.parser.getQNameIgnoreDefaultNs("decendant-or-self"));
            case 133:
                int i442 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i442)).left, ((Symbol) stack.elementAt(i442)).right, this.parser.getQNameIgnoreDefaultNs("following"));
            case 134:
                int i443 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i443)).left, ((Symbol) stack.elementAt(i443)).right, this.parser.getQNameIgnoreDefaultNs("following-sibling"));
            case 135:
                int i444 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i444)).left, ((Symbol) stack.elementAt(i444)).right, this.parser.getQNameIgnoreDefaultNs(Constants.ATTRNAME_NAMESPACE));
            case 136:
                int i445 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i445)).left, ((Symbol) stack.elementAt(i445)).right, this.parser.getQNameIgnoreDefaultNs("parent"));
            case 137:
                int i446 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i446)).left, ((Symbol) stack.elementAt(i446)).right, this.parser.getQNameIgnoreDefaultNs("preceding"));
            case 138:
                int i447 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i447)).left, ((Symbol) stack.elementAt(i447)).right, this.parser.getQNameIgnoreDefaultNs("preceding-sibling"));
            case 139:
                int i448 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i448)).left, ((Symbol) stack.elementAt(i448)).right, this.parser.getQNameIgnoreDefaultNs("self"));
            case 140:
                int i449 = i2 + 0;
                return new Symbol(37, ((Symbol) stack.elementAt(i449)).left, ((Symbol) stack.elementAt(i449)).right, this.parser.getQNameIgnoreDefaultNs("id"));
            default:
                throw new Exception("Invalid action number found in internal parse table");
        }
    }
}
