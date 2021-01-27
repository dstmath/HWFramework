package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class XSGrammarBucket {
    Map<String, SchemaGrammar> fGrammarRegistry = new HashMap();
    SchemaGrammar fNoNSGrammar = null;

    public SchemaGrammar getGrammar(String str) {
        if (str == null) {
            return this.fNoNSGrammar;
        }
        return this.fGrammarRegistry.get(str);
    }

    public void putGrammar(SchemaGrammar schemaGrammar) {
        if (schemaGrammar.getTargetNamespace() == null) {
            this.fNoNSGrammar = schemaGrammar;
        } else {
            this.fGrammarRegistry.put(schemaGrammar.getTargetNamespace(), schemaGrammar);
        }
    }

    public boolean putGrammar(SchemaGrammar schemaGrammar, boolean z) {
        SchemaGrammar grammar = getGrammar(schemaGrammar.fTargetNamespace);
        if (grammar != null) {
            return grammar == schemaGrammar;
        }
        if (!z) {
            putGrammar(schemaGrammar);
            return true;
        }
        Vector importedGrammars = schemaGrammar.getImportedGrammars();
        if (importedGrammars == null) {
            putGrammar(schemaGrammar);
            return true;
        }
        Vector vector = (Vector) importedGrammars.clone();
        for (int i = 0; i < vector.size(); i++) {
            SchemaGrammar schemaGrammar2 = (SchemaGrammar) vector.elementAt(i);
            SchemaGrammar grammar2 = getGrammar(schemaGrammar2.fTargetNamespace);
            if (grammar2 == null) {
                Vector importedGrammars2 = schemaGrammar2.getImportedGrammars();
                if (importedGrammars2 != null) {
                    for (int size = importedGrammars2.size() - 1; size >= 0; size--) {
                        SchemaGrammar schemaGrammar3 = (SchemaGrammar) importedGrammars2.elementAt(size);
                        if (!vector.contains(schemaGrammar3)) {
                            vector.addElement(schemaGrammar3);
                        }
                    }
                }
            } else if (grammar2 != schemaGrammar2) {
                return false;
            }
        }
        putGrammar(schemaGrammar);
        for (int size2 = vector.size() - 1; size2 >= 0; size2--) {
            putGrammar((SchemaGrammar) vector.elementAt(size2));
        }
        return true;
    }

    public boolean putGrammar(SchemaGrammar schemaGrammar, boolean z, boolean z2) {
        Vector importedGrammars;
        if (!z2) {
            return putGrammar(schemaGrammar, z);
        }
        if (getGrammar(schemaGrammar.fTargetNamespace) == null) {
            putGrammar(schemaGrammar);
        }
        if (!z || (importedGrammars = schemaGrammar.getImportedGrammars()) == null) {
            return true;
        }
        Vector vector = (Vector) importedGrammars.clone();
        for (int i = 0; i < vector.size(); i++) {
            SchemaGrammar schemaGrammar2 = (SchemaGrammar) vector.elementAt(i);
            if (getGrammar(schemaGrammar2.fTargetNamespace) == null) {
                Vector importedGrammars2 = schemaGrammar2.getImportedGrammars();
                if (importedGrammars2 != null) {
                    for (int size = importedGrammars2.size() - 1; size >= 0; size--) {
                        SchemaGrammar schemaGrammar3 = (SchemaGrammar) importedGrammars2.elementAt(size);
                        if (!vector.contains(schemaGrammar3)) {
                            vector.addElement(schemaGrammar3);
                        }
                    }
                }
            } else {
                vector.remove(schemaGrammar2);
            }
        }
        for (int size2 = vector.size() - 1; size2 >= 0; size2--) {
            putGrammar((SchemaGrammar) vector.elementAt(size2));
        }
        return true;
    }

    public SchemaGrammar[] getGrammars() {
        int i = 0;
        int size = this.fGrammarRegistry.size() + (this.fNoNSGrammar == null ? 0 : 1);
        SchemaGrammar[] schemaGrammarArr = new SchemaGrammar[size];
        for (Map.Entry<String, SchemaGrammar> entry : this.fGrammarRegistry.entrySet()) {
            schemaGrammarArr[i] = entry.getValue();
            i++;
        }
        SchemaGrammar schemaGrammar = this.fNoNSGrammar;
        if (schemaGrammar != null) {
            schemaGrammarArr[size - 1] = schemaGrammar;
        }
        return schemaGrammarArr;
    }

    public void reset() {
        this.fNoNSGrammar = null;
        this.fGrammarRegistry.clear();
    }
}
