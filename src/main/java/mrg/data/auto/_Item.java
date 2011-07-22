package mrg.data.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;

import mrg.data.CostElement;
import mrg.data.LineItem;

/**
 * Class _Item was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Item extends CayenneDataObject {

    public static final String LOT_PROPERTY = "lot";
    public static final String COST_ELEMENTS_PROPERTY = "costElements";
    public static final String LINE_ITEM_PROPERTY = "lineItem";

    public static final String ID_PK_COLUMN = "id";

    public void setLot(String lot) {
        writeProperty("lot", lot);
    }
    public String getLot() {
        return (String)readProperty("lot");
    }

    public void addToCostElements(CostElement obj) {
        addToManyTarget("costElements", obj, true);
    }
    public void removeFromCostElements(CostElement obj) {
        removeToManyTarget("costElements", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<CostElement> getCostElements() {
        return (List<CostElement>)readProperty("costElements");
    }


    public void setLineItem(LineItem lineItem) {
        setToOneTarget("lineItem", lineItem, true);
    }

    public LineItem getLineItem() {
        return (LineItem)readProperty("lineItem");
    }


}
