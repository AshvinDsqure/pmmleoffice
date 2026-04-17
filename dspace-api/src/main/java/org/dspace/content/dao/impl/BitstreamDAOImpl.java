/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.*;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.dspace.content.Bitstream;
import org.dspace.content.Bitstream_;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Constants;
import org.dspace.core.Context;

/**
 * Hibernate implementation of the Database Access Object interface class for the Bitstream object.
 * This class is responsible for all database calls for the Bitstream object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class BitstreamDAOImpl extends AbstractHibernateDSODAO<Bitstream> implements BitstreamDAO {

    protected BitstreamDAOImpl() {
        super();
    }

    @Override
    public List<Bitstream> findDeletedBitstreams(Context context) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Bitstream.class);
        Root<Bitstream> bitstreamRoot = criteriaQuery.from(Bitstream.class);
        criteriaQuery.select(bitstreamRoot);
        criteriaQuery.where(criteriaBuilder.equal(bitstreamRoot.get(Bitstream_.deleted), true));
        return list(context, criteriaQuery, false, Bitstream.class, -1, -1);

    }

    @Override
    public List<Bitstream> findDuplicateInternalIdentifier(Context context, Bitstream bitstream) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Bitstream.class);
        Root<Bitstream> bitstreamRoot = criteriaQuery.from(Bitstream.class);
        criteriaQuery.select(bitstreamRoot);
        criteriaQuery.where(criteriaBuilder.and(
            criteriaBuilder.equal(bitstreamRoot.get(Bitstream_.internalId), bitstream.getInternalId()),
            criteriaBuilder.notEqual(bitstreamRoot.get(Bitstream_.id), bitstream.getID())
                            )
        );
        return list(context, criteriaQuery, false, Bitstream.class, -1, -1);
    }

    @Override
    public List<Bitstream> findBitstreamsWithNoRecentChecksum(Context context) throws SQLException {
        Query query = createQuery(context,
                                  "select b from Bitstream b where b not in (select c.bitstream from " +
                                      "MostRecentChecksum c)");
        return query.getResultList();
    }

    @Override
    public Iterator<Bitstream> findByCommunity(Context context, Community community) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
            "join b.bundles bitBundles " +
            "join bitBundles.items item " +
            "join item.collections itemColl " +
            "join itemColl.communities community " +
            "WHERE :community IN community");

        query.setParameter("community", community);

        return iterate(query);
    }

    @Override
    public Iterator<Bitstream> findByCollection(Context context, Collection collection) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
            "join b.bundles bitBundles " +
            "join bitBundles.items item " +
            "join item.collections c " +
            "WHERE :collection IN c");

        query.setParameter("collection", collection);

        return iterate(query);
    }

    @Override
    public Iterator<Bitstream> findByItem(Context context, Item item) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b " +
            "join b.bundles bitBundles " +
            "join bitBundles.items item " +
            "WHERE :item IN item");

        query.setParameter("item", item);

        return iterate(query);
    }

    @Override
    public List<Bitstream> findByItemIdAndInward(Context context, UUID itemid, UUID inwardid,Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "SELECT bt FROM WorkflowProcessReferenceDoc as d join d.bitstream as bt join d.workflowProcess as wp join wp.workflowType as wft join wp.item as i where i.id=:itemid and wft.id=:workflowtypeid");
        query.setParameter("workflowtypeid",inwardid);
        query.setParameter("itemid",itemid);
        return query.getResultList();
    }

    @Override
    public Iterator<Bitstream> findByStoreNumber(Context context, Integer storeNumber) throws SQLException {
        Query query = createQuery(context, "select b from Bitstream b where b.storeNumber = :storeNumber");
        query.setParameter("storeNumber", storeNumber);
        return iterate(query);
    }

    @Override
    public Long countByStoreNumber(Context context, Integer storeNumber) throws SQLException {


        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<Bitstream> bitstreamRoot = criteriaQuery.from(Bitstream.class);
        criteriaQuery.where(criteriaBuilder.equal(bitstreamRoot.get(Bitstream_.storeNumber), storeNumber));
        return countLong(context, criteriaQuery, criteriaBuilder, bitstreamRoot);
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) from Bitstream"));
    }

    @Override
    public int countDeleted(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Bitstream b WHERE b.deleted=true"));
    }

    @Override
    public int countWithNoPolicy(Context context) throws SQLException {
        Query query = createQuery(context,
                                  "SELECT count(bit.id) from Bitstream bit where bit.deleted<>true and bit.id not in" +
                                      " (select res.dSpaceObject from ResourcePolicy res where res.resourceTypeId = " +
                                      ":typeId )");
        query.setParameter("typeId", Constants.BITSTREAM);
        return count(query);
    }

    @Override
    public List<Bitstream> getNotReferencedBitstreams(Context context) throws SQLException {
        return list(createQuery(context, "select bit from Bitstream bit where bit.deleted != true" +
            " and bit.id not in (select bit2.id from Bundle bun join bun.bitstreams bit2)" +
            " and bit.id not in (select com.logo.id from Community com)" +
            " and bit.id not in (select col.logo.id from Collection col)" +
            " and bit.id not in (select bun.primaryBitstream.id from Bundle bun)"));
    }

    @Override
    public List<Bitstream> getNotBitstreamsbySatatus(Context context ,UUID drfttype, UUID itemid,UUID workflowstatuscloseid,UUID dspaceclosecloseid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context,
                "SELECT DISTINCT b " +
                        "FROM WorkflowProcessReferenceDoc d " +
                        "JOIN d.workflowprocessnote n " +
                        "JOIN d.bitstream b " +   // ✅ also changed to INNER JOIN (recommended)
                        "JOIN d.workflowProcess wp " +
                        "JOIN wp.item i " +
                        "JOIN wp.workflowStatus status " +
                        "WHERE i.id = :itemid " +
                        "AND (status.id = :stid OR status.id = :dspaceclose)  and d.drafttype.id=:drafttype " +
                        " ORDER BY b.noteindex ASC "
        );

        query.setParameter("drafttype", drfttype);
        query.setParameter("itemid", itemid);
        query.setParameter("stid", workflowstatuscloseid);
        query.setParameter("dspaceclose", dspaceclosecloseid);

        if (offset != null && offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    @Override
    public Iterator<Bitstream> findAll(Context context, int limit, int offset) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        return findByX(context, Bitstream.class, map, true, limit, offset).iterator();

    }
}
