package org.apache.cayenne.map;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
    protected void sortEntities()
    {
        log.info("sortEntities() called");

        // Correct double check locking per Joshua Bloch.
        //     http://java.sun.com/developer/technicalArticles/Interviews/bloch_effective_08_qa.html

        boolean localInitialized = initialized;

        if (localInitialized == false)
        {
            synchronized (this)
            {
                localInitialized = initialized;

                if (localInitialized == false)
                {
                    reallySortEntities();
                    initialized = true;
                }
            }
        }
    }

    /**
     * Re-indexes internal sorting without synchronization.  All of the the tables
     * defined by the EntityResolver must be sorted and weighted into a proper
     * dependency order for correct insertion and deletion of objects/records with
     * the database.
     * <p/>
     * Should only be called by sortIndexes().
     * <p/>
     * Strategy:<br/>
     * 1) Get all the tables.<br/>
     * 2) Get the relationships for each table.<br/>
     * 3) Create weighted list with most important tables at the front.<br/>
     */
    protected void reallySortEntities()
    {
        log.info("reallySortEntities() called, entityResolver = " + entityResolver);

        // Bail out if no EntityResolver.  Don't set initialized to true (didn't do anything).
        if (entityResolver == null)
            return;

        int                                 entityCount         = entityResolver.getDbEntities().size();
        Map<DbEntity, List<DbRelationship>> reflexiveDbEntities = new HashMap<DbEntity, List<DbRelationship>>(entityCount / 8);
        Map<String, DbEntity>               tableMap            = new HashMap<String, DbEntity>(entityCount);

        // Collect all of the database entities (tables) defined by the EntityResolver.
        for (DbEntity dbEntity : entityResolver.getDbEntities())
        {
            tableMap.put(dbEntity.getFullyQualifiedName(), dbEntity);
            //                referentialDigraph.addVertex(entity);
        }

        /**
         * The weighted list of DbEntity objects (representing tables). This
         * list will be modified so that the most important "heavier" DbEntity
         * objects (those that contain primary keys which are needed as foreign
         * keys by later objects) are at the beginning of the list and the least
         * important "lighter" DbEntity objects (those needing the primary keys
         * of previous objects) are at the end. This is for insert ordering.
         * Delete ordering is the weighted list reversed. The list is
         * initialized with all the DbEntity objects in the current
         * EntityResolver and then sorted in-place.
         */
        LinkedList<DbEntity> weightedDbEntities = new LinkedList<DbEntity>(entityResolver.getDbEntities());

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S Z");

System.out.println(dateFormat.format(new Date()));


        boolean squeakyClean;

        START_OVER:
        do
        {

            ListIterator<DbEntity> weightedDbEntityIterator = weightedDbEntities.listIterator();

            while (weightedDbEntityIterator.hasNext())
            {
                int      currentDbIndex  = weightedDbEntityIterator.nextIndex();
                DbEntity currentDbEntity = weightedDbEntityIterator.next();

                log.info("current db entity = " + currentDbEntity + " " + currentDbIndex);

                for (int i = currentDbIndex + 1; i < weightedDbEntities.size(); i++)
                {
                    DbEntity targetDbEntity = weightedDbEntities.get(i);

                    log.info("target db entity = " + targetDbEntity + " " + i);

                    for (DbRelationship currentRelationship : currentDbEntity.getRelationships())
                    {
                        log.info("candidate relationship = " + currentRelationship);

                        // If the current relationship target equals the target DbEntity, process it.
                        if (currentRelationship.getTargetEntity().equals(targetDbEntity))
                        {
                            if (currentRelationship.isToMany())
                                continue; // Don't care about to-many (PK to an FK).

                            if (currentRelationship.isToDependentPK())
                                continue; // Don't care if the target depends on our PK.

                            if (currentRelationship.isToPK()) // Now we start to care!
                            {
                                // The current DbEntity depends on the target DbEntity for a
                                // primary key and needs to be moved AFTER the target DbEntity.

                                weightedDbEntityIterator.remove(); // Remove the currentDbEntity
                                weightedDbEntities.add(i, currentDbEntity); // Put it AFTER the one we just found that we depend upon.
                                squeakyClean = false;
                                continue START_OVER;
                            }
                        }
                    }
                }
            }

            squeakyClean = true; // Made it!
        } while (squeakyClean == false);
System.out.println(dateFormat.format(new Date()));

        // Loop over all of the tables to find the relationships we care about.  These
        // relationships will determine the proper ordering of the tables.
        for (DbEntity destinationTable : tableMap.values())
        {
            // Loop over all the relationships in the table.
            for (DbRelationship candidateRelationship : destinationTable.getRelationships())
            {
                // Check to see if we care about this relationship.  We care about FKs -> PKs.
                if ((!candidateRelationship.isToMany() && !candidateRelationship.isToDependentPK()) || candidateRelationship.isToMasterPK())
                {
                    DbEntity origin       = (DbEntity) candidateRelationship.getTargetEntity();
                    boolean  newReflexive = destinationTable.equals(origin);

                    for (DbJoin join : candidateRelationship.getJoins())
                    {
                        DbAttribute targetAttribute = join.getTarget();

                        if (targetAttribute.isPrimaryKey())
                        {
                            if (newReflexive)
                            {
                                List<DbRelationship> reflexiveRelationships = reflexiveDbEntities.get(destinationTable);

                                if (reflexiveRelationships == null)
                                {
                                    reflexiveRelationships = new ArrayList<DbRelationship>(1);
                                    reflexiveDbEntities.put(destinationTable, reflexiveRelationships);
                                }

                                reflexiveRelationships.add(candidateRelationship);
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
    @Override
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
    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public void setDataMaps(Collection<DataMap> dataMaps)
    {
        setEntityResolver(new EntityResolver(dataMaps == null ? Collections.EMPTY_LIST : dataMaps));
    }

    @Override
    public void sortDbEntities(List<DbEntity> dbEntities, boolean deleteOrder)
    {
        sortEntities();
    }

    @Override
    public void sortObjEntities(List<ObjEntity> objEntities, boolean deleteOrder)
    {
        sortEntities();
    }

    @Override
    public void sortObjectsForEntity(ObjEntity entity, List<?> objects, boolean deleteOrder)
    {
        sortEntities();
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
        @Override
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
        @Override
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
