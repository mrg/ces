package org.apache.cayenne.map;

import java.util.Collection;
import java.util.List;

public class CayenneEntitySorter implements EntitySorter
{
    /**
     * Sets EntityResolver for this sorter. All entities present in the resolver will be
     * used to determine sort ordering.
     *
     * @since 3.1
     */
    public void setEntityResolver(EntityResolver resolver)
    {

    }

    /**
     * Initializes a list of DataMaps used by the sorter.
     *
     * @deprecated since 3.1 {@link #setEntityResolver(EntityResolver)} is used, and this
     *             method is never called.
     */
    @Deprecated
    public void setDataMaps(Collection<DataMap> dataMaps)
    {
        // TODO Auto-generated method stub

    }

    public void sortDbEntities(List<DbEntity> dbEntities, boolean deleteOrder)
    {
        // TODO Auto-generated method stub

    }

    public void sortObjEntities(List<ObjEntity> objEntities, boolean deleteOrder)
    {
        // TODO Auto-generated method stub

    }

    public void sortObjectsForEntity(ObjEntity entity, List<?> objects, boolean deleteOrder)
    {
        // TODO Auto-generated method stub

    }

}
