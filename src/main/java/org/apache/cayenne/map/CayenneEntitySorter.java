package org.apache.cayenne.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements dependency sorting algorithms for ObjEntities, DbEntities and DataObjects.
 *
 * @since 3.1
 */
public class CayenneEntitySorter implements EntitySorter
{
    private static final Log log = LogFactory.getLog(CayenneEntitySorter.class);

    private boolean initialized;

    protected EntityResolver entityResolver;

    protected Map<DbEntity, ComponentRecord>      components;
    protected Map<DbEntity, List<DbRelationship>> reflexiveDbEntities;

    protected Comparator<DbEntity>  dbEntityComparator;
    protected Comparator<ObjEntity> objEntityComparator;

    /**
     * @since 3.1.
     */
    public CayenneEntitySorter()
    {
        log.info("CayenneEntitySorter() called");

        dbEntityComparator  = new DbEntityComparator();
        objEntityComparator = new ObjEntityComparator();

        initialized = false;
    }

    /**
     * @deprecated since 3.1. Use {@link #CayenneEntitySorter()} constructor together with
     *             {@link #setDataMaps(Collection)} instead.
     */
    @Deprecated
    public CayenneEntitySorter(Collection<DataMap> dataMaps)
    {
        this();
        log.info("CayenneEntitySorter(Collection<DataMap> dataMaps) called");
        setDataMaps(dataMaps);
    }



    /**
     * Re-indexes internal sorter in a thread-safe manner.
     */
    protected void sortIndexes()
    {
        log.info("sortIndexes() called");

        // Correct double check locking per Joshua Bloch.
        //     http://java.sun.com/developer/technicalArticles/Interviews/bloch_effective_08_qa.html

        boolean localInitialized = initialized;

        if (localInitialized == false)
        {
            synchronized (this)
            {
                localInitialized = initialized;
                if (localInitialized)
                {
                    reallySortIndexes();
                    initialized = true;
                }
            }
        }
    }

    /**
     * Re-indexes internal sorting without synchronization.  Should only be
     * called by sortIndexes().
     *
     * Strategy:
     * 1) Get all the tables.
     * 2) Get the relationships for each table.
     * 3) Create weighted list with most important tables at the front.
     */
    protected void reallySortIndexes()
    {
        log.info("reallySortIndexes() called, entityResolver = " + entityResolver);

        // Bail out if no EntityResolver.  Don't set initialized to true (didn't do anything).
        if (entityResolver == null)
            return;

        int                                 entityCount         = entityResolver.getDbEntities().size();
        Map<DbEntity, List<DbRelationship>> reflexiveDbEntities = new HashMap<DbEntity, List<DbRelationship>>(entityCount / 8);
        Map<String, DbEntity>               tableMap            = new HashMap<String, DbEntity>(entityCount);

        // Collect all of the database entities (tables).
        for (DbEntity dbEntity : entityResolver.getDbEntities())
        {
            tableMap.put(dbEntity.getFullyQualifiedName(), dbEntity);
            //                referentialDigraph.addVertex(entity);
        }

        // Find the relationships we care about.
        for (DbEntity destination : tableMap.values())
        {
            for (DbRelationship candidate : destination.getRelationships())
            {
                if ((!candidate.isToMany() && !candidate.isToDependentPK()) || candidate.isToMasterPK())
                {
                    DbEntity origin = (DbEntity) candidate.getTargetEntity();
                    boolean newReflexive = destination.equals(origin);

                    for (DbJoin join : candidate.getJoins())
                    {
                        DbAttribute targetAttribute = join.getTarget();
                        if (targetAttribute.isPrimaryKey())
                        {

                            if (newReflexive)
                            {
                                List<DbRelationship> reflexiveRels = reflexiveDbEntities.get(destination);
                                if (reflexiveRels == null)
                                {
                                    reflexiveRels = new ArrayList<DbRelationship>(1);
                                    reflexiveDbEntities.put(destination, reflexiveRels);
                                }
                                reflexiveRels.add(candidate);
                                newReflexive = false;
                            }

//                            List<DbAttribute> fks = referentialDigraph.getArc(origin, destination);
//                            if (fks == null)
//                            {
//                                fks = new ArrayList<DbAttribute>();
//                                referentialDigraph.putArc(origin, destination, fks);
//                            }
//
//                            fks.add(targetAttribute);
                        }
                    }
                }
            }

        }

    }

    /**
     * Sets EntityResolver for this sorter. All entities present in the resolver will be
     * used to determine sort ordering.
     *
     * @since 3.1
     */
    public void setEntityResolver(EntityResolver entityResolver)
    {
        this.entityResolver = entityResolver;
        this.initialized    = false;
    }

    /**
     * Initializes a list of DataMaps used by the sorter.
     *
     * @since 1.1
     * @deprecated since 3.1 {@link #setEntityResolver(EntityResolver)} is used, and this
     *             method is never called.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public void setDataMaps(Collection<DataMap> dataMaps)
    {
        setEntityResolver(new EntityResolver(dataMaps == null ? Collections.EMPTY_LIST : dataMaps));
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

    protected Comparator<DbEntity> getDbEntityComparator(boolean dependantFirst)
    {
        Comparator<DbEntity> c = dbEntityComparator;

        if (dependantFirst)
            c = new ReverseComparator(c);

        return c;
    }

    protected Comparator<ObjEntity> getObjEntityComparator(boolean dependantFirst)
    {
        Comparator<ObjEntity> c = objEntityComparator;

        if (dependantFirst)
            c = new ReverseComparator(c);

        return c;
    }

    private final class ObjEntityComparator implements Comparator<ObjEntity>
    {
        public int compare(ObjEntity o1, ObjEntity o2)
        {
            if (o1 == o2)
                return 0;
            DbEntity t1 = o1.getDbEntity();
            DbEntity t2 = o2.getDbEntity();
            return dbEntityComparator.compare(t1, t2);
        }
    }

    private final class DbEntityComparator implements Comparator<DbEntity>
    {
        public int compare(DbEntity t1, DbEntity t2)
        {
            int result = 0;

            if (t1 == t2)
                return 0;
            if (t1 == null)
                result = -1;
            else if (t2 == null)
                result = 1;
            else
            {
                ComponentRecord rec1 = components.get(t1);
                ComponentRecord rec2 = components.get(t2);
                int index1 = rec1.index;
                int index2 = rec2.index;
                result = (index1 > index2 ? 1 : (index1 < index2 ? -1 : 0));
                if (result != 0 && rec1.component == rec2.component)
                    result = 0;
            }
            return result;
        }
    }

    private final static class ComponentRecord
    {
        int                  index; // FIXME: This is really the weight
        Collection<DbEntity> component;

        ComponentRecord(int index, Collection<DbEntity> component)
        {
            this.index = index;
            this.component = component;
        }

    }
}
