package mrg;

import mrg.data.BudgetYear;
import mrg.data.CostElement;
import mrg.data.Item;
import mrg.data.LineItem;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;

public class CESTester
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ServerRuntime cayenneRuntime = new ServerRuntime("cayenne-CESDomain.xml");
        ObjectContext context        = cayenneRuntime.getContext();

        LineItem    li  = context.newObject(LineItem.class);
        Item        i   = context.newObject(Item.class);
        CostElement ce1 = context.newObject(CostElement.class);
        CostElement ce2 = context.newObject(CostElement.class);
        CostElement ce3 = context.newObject(CostElement.class);
        BudgetYear  by1 = context.newObject(BudgetYear.class);
        BudgetYear  by2 = context.newObject(BudgetYear.class);
        BudgetYear  by3 = context.newObject(BudgetYear.class);

        li.addToItems(i);
        i.addToCostElements(ce1);
        ce1.addToCostElements(ce2);
        ce2.addToCostElements(ce3);
        ce3.setQuantity(by1);
        ce3.setTotalCost(by2);
        ce3.setUnitCost(by3);
        by1.setLineItem(li);
        by2.setLineItem(li);
        by3.setLineItem(li);

//        ce2.setItem(i);
//        ce3.setItem(i);

        context.commitChanges();

        context.deleteObject(li);

        context.commitChanges();
    }
}
