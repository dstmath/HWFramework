package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Relation;

public class RelationInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$RelationInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getRelations() == null || contact.getRelations().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (Relation relation : contact.getRelations()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Relation.CONTENT_ITEM_TYPE, operationType, relation.getId());
                if (!isEmpty(relation.getRelationName())) {
                    fillStringContent(predicatesBuilder, "data1", relation.getRelationName());
                }
                if (relation.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", relation.getLabelId());
                }
                if (relation.getLabelId() == 0 && !isEmpty(relation.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", relation.getLabelName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
