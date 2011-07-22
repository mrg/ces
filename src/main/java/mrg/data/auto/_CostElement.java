package mrg.data.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;

import mrg.data.BudgetYear;
import mrg.data.CostElement;
import mrg.data.Item;

/**
 * Class _CostElement was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _CostElement extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String COST_ELEMENTS_PROPERTY = "costElements";
    public static final String ITEM_PROPERTY = "item";
    public static final String PARENT_COST_ELEMENT_PROPERTY = "parentCostElement";
    public static final String QUANTITY_PROPERTY = "quantity";
    public static final String TOTAL_COST_PROPERTY = "totalCost";
    public static final String UNIT_COST_PROPERTY = "unitCost";

    public static final String ID_PK_COLUMN = "id";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
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


    public void setItem(Item item) {
        setToOneTarget("item", item, true);
    }

    public Item getItem() {
        return (Item)readProperty("item");
    }


    public void setParentCostElement(CostElement parentCostElement) {
        setToOneTarget("parentCostElement", parentCostElement, true);
    }

    public CostElement getParentCostElement() {
        return (CostElement)readProperty("parentCostElement");
    }


    public void setQuantity(BudgetYear quantity) {
        setToOneTarget("quantity", quantity, true);
    }

    public BudgetYear getQuantity() {
        return (BudgetYear)readProperty("quantity");
    }


    public void setTotalCost(BudgetYear totalCost) {
        setToOneTarget("totalCost", totalCost, true);
    }

    public BudgetYear getTotalCost() {
        return (BudgetYear)readProperty("totalCost");
    }


    public void setUnitCost(BudgetYear unitCost) {
        setToOneTarget("unitCost", unitCost, true);
    }

    public BudgetYear getUnitCost() {
        return (BudgetYear)readProperty("unitCost");
    }


}
