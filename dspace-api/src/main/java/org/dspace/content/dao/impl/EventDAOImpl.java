/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.dspace.app.util.Constant;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.content.Event;
import org.dspace.content.dao.EventDAO;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.hibernate.Query;

import org.apache.logging.log4j.Logger;
/**
 * Hibernate implementation of the Database Access Object interface class for
 * the Item object. This class is responsible for all database calls for the
 * Item object and is autowired by spring This class should never be accessed
 * directly.
 *
 * @author kevinvandevelde at atmire.com
 */
@SuppressWarnings("deprecation")
public class EventDAOImpl extends AbstractHibernateDSODAO<Event> implements EventDAO {

    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(EventDAOImpl.class);

    protected EventDAOImpl() {
        super();
    }

    @Override
    public int FindcountBydate(Context context, Integer limit, Date startdate, Date enddate, int dspaceObjectid, String email, String s) throws Exception {
        String sqlQuery = "select count (*) as total FROM event as v where v.title is not null  and   action_date <= '" + enddate + "' and  action_date >= '" + startdate + "' #Search# ";
        sqlQuery = sqlQuery.replace("#Search#", s);
        if (dspaceObjectid != -1) {
            sqlQuery = sqlQuery + " AND dspaceobjecttype ='" + dspaceObjectid + "' ";
        }
        if (!email.equalsIgnoreCase("-1")) {
            sqlQuery = sqlQuery + " AND userid ='" + email + "' ";
        }
        //System.err.println("queryString::" + sqlQuery);
        return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
    }

    @Override
    public List<Object[]> FindcountBydateforChart(Context context, String oderby, Date startdate, Date enddate, int dspaceObjectid, String email, boolean fromtable, int o, String s, String order) throws Exception {
        String queryString = "SELECT st." + oderby + " as name, COUNT(st." + oderby + ") AS total FROM  Event as st  where st.title is not null  AND    st.action_date <= :enddate AND  st.action_date >= :startdate";

        //System.err.println("oderby:::" + oderby);
        if (dspaceObjectid != -1) {
            queryString = queryString + " AND dspaceobjecttype = :dspaceobjecttype ";
        }
        if (!email.equalsIgnoreCase("-1")) {
            queryString = queryString + " AND userid = :email";
        }
        queryString = queryString + " GROUP BY st." + oderby + "";
        //System.err.println("FindcountBydateforChart::" + queryString);
        //System.err.println("========" + enddate);
        Query query = (Query) createQuery(context, queryString);
        query.setParameter("enddate", enddate);
        query.setParameter("startdate", startdate);
        if (dspaceObjectid != -1) {
            query.setParameter("dspaceobjecttype", dspaceObjectid);
        }
        if (!email.equalsIgnoreCase("-1")) {
            query.setParameter("email", email);
        }
        //System.err.println("queryString::" + queryString);
        //System.err.println("======================" + new Gson().toJson(query.list()));
        return query.list();
    }

    @Override
    public List<Object> findEventBYDate(Context context, Integer limit, Date startdate, Date enddate, int dspaceObjectid, String email, boolean fromtable, int o, String s, String order) throws Exception {
        String querySorted = "select * FROM event as v where v.title is not null  and    action_date <= '" + enddate + "' and  action_date >= '" + startdate + "' #Search# ";
        querySorted = querySorted.replace("#Search#", s);
        if (dspaceObjectid != -1) {
            querySorted = querySorted + " AND dspaceobjecttype ='" + dspaceObjectid + "' ";
        }
        if (!email.equalsIgnoreCase("-1")) {
            querySorted = querySorted + " AND userid ='" + email + "' ";
        }
        querySorted = querySorted + " #ORDER# ";
        querySorted = querySorted.replace("#ORDER#", order);
        if (fromtable == true) {
            querySorted += "   LIMIT " + o + " OFFSET " + limit;
        }
        //System.err.println("queryString::" + querySorted);
        Query query = createSQLQuery(context, querySorted).addEntity("v", Event.class);

        return query.list();
    }

    @Override
    public List<Object[]> findEventTop10item(Context context, int limit, int Objecttype, int action, String collactionID, String communityID, Date startDate, Date endDate) throws Exception {
        String queryString = "SELECT st.dspaceobjectid as name, COUNT(st.dspaceobjectid) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype CollacationTag communityTag DataFormate"
                + " GROUP BY st.dspaceobjectid ORDER BY total DESC";
        if (collactionID != "" && collactionID.length() != 0) {
            queryString = queryString.replace("CollacationTag", " and st.parenCollection=:collactionID");
        } else {
            queryString = queryString.replace("CollacationTag", "");
        }

        if (communityID != "" && communityID.length() != 0) {
            queryString = queryString.replace("communityTag", " and st.parenCommunity=:communityID");
        } else {
            queryString = queryString.replace("communityTag", "");
        }
        if (startDate != null && endDate != null) {
            queryString = queryString.replace("DataFormate", "AND    st.action_date >= :enddate AND  st.action_date <= :startdate");
        } else {
            queryString = queryString.replace("DataFormate", "");
        }
        Query query = (Query) createQuery(context, queryString);
        query.setMaxResults(10);
        //System.err.println("queryString::" + queryString);
        query.setParameter("dspaceobjecttype", Objecttype);
        query.setParameter("action", action);
        if (collactionID != "" && collactionID.length() != 0) {
            query.setParameter("collactionID", UUID.fromString(collactionID));
        }
        if (communityID != "" && communityID.length() != 0) {
            query.setParameter("communityID", UUID.fromString(communityID));
        }
        if (startDate != null && endDate != null) {
            query.setParameter("enddate", endDate);
            query.setParameter("startdate", startDate);
        }
        return query.list();
    }

    @Override
    public List<Object[]> findEventTop10Community(Context context, int limit, int Objecttype, int action, String collactionID, String communityID, Date startDate, Date endDate) throws Exception {
        String queryString = "SELECT st.title as name, COUNT(st.title) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype CollacationTag communityTag DataFormate"
                + " GROUP BY st.title  ORDER BY total DESC";
        if (collactionID != "" && collactionID.length() != 0) {
            queryString = queryString.replace("CollacationTag", " and st.parenCollection=:collactionID");
        } else {
            queryString = queryString.replace("CollacationTag", "");
        }
        if (communityID != "" && communityID.length() != 0) {
            queryString = queryString.replace("communityTag", " and st.parenCommunity=:communityID");
        } else {
            queryString = queryString.replace("communityTag", "");
        }
        if (startDate != null && endDate != null) {
            queryString = queryString.replace("DataFormate", "AND    st.action_date >= :enddate AND  st.action_date <= :startdate");
        } else {
            queryString = queryString.replace("DataFormate", "");
        }
        Query query = (Query) createQuery(context, queryString);
        query.setMaxResults(10);
        query.setParameter("dspaceobjecttype", Objecttype);
        query.setParameter("action", action);
        //System.err.println("findEventTop10Communitys::" + queryString);
        //System.err.println("communityID" + communityID);
        //System.err.println("communityID" + collactionID);
        if (collactionID != "" && collactionID.length() != 0) {
            query.setParameter("collactionID", UUID.fromString(collactionID));
        }
        if (communityID != "" && communityID.length() != 0) {
            query.setParameter("communityID", UUID.fromString(communityID));
        }
        if (startDate != null && endDate != null) {
          //  System.out.println("endDate date in api:::::" + endDate);
          //  System.out.println("start date in api:::::" + startDate);
            query.setParameter("enddate", endDate);
            query.setParameter("startdate", startDate);
        }
        return query.list();
    }

    @Override
    public List<Object[]> findEventTop10Collaction(Context context, int limit, int Objecttype, int action, String collactionID, String communityID, Date startDate, Date endDate) throws Exception {
        String queryString = "SELECT st.title as name, COUNT(st.title) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype CollacationTag communityTag DataFormate"
                + " GROUP BY st.title ORDER BY total DESC ";
        if (collactionID != "" && collactionID.length() != 0) {
            queryString = queryString.replace("CollacationTag", " and st.parenCollection=:collactionID");
        } else {
            queryString = queryString.replace("CollacationTag", "");
        }
        if (communityID != "" && communityID.length() != 0) {
            queryString = queryString.replace("communityTag", " and st.parenCommunity=:communityID");
        } else {
            queryString = queryString.replace("communityTag", "");
        }
        if (startDate != null && endDate != null) {
            queryString = queryString.replace("DataFormate", "AND    st.action_date >= :enddate AND  st.action_date <= :startdate");
        } else {
            queryString = queryString.replace("DataFormate", "");
        }
        Query query = (Query) createQuery(context, queryString);
        query.setMaxResults(10);
        query.setParameter("dspaceobjecttype", Objecttype);
        query.setParameter("action", action);
        //System.err.println("queryString::" + queryString);
        if (collactionID != "" && collactionID.length() != 0) {
            query.setParameter("collactionID", UUID.fromString(collactionID));
        }
        if (communityID != "" && communityID.length() != 0) {
            query.setParameter("communityID", UUID.fromString(communityID));
        }
        if (startDate != null && endDate != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            query.setParameter("enddate", endDate);
            query.setParameter("startdate", startDate);
        }
        return query.list();
    }

    @Override
    public List<Object[]> findEventTop10Search(Context context, int limit, int Objecttype, int action, Date startDate, Date endDate) throws Exception {
        String queryString = "SELECT st.title as name, COUNT(st.title) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype DataFormate"
                + " GROUP BY st.title  ORDER BY total DESC ";
        if (startDate != null && endDate != null) {
            queryString = queryString.replace("DataFormate", "AND    st.action_date >= :enddate AND  st.action_date <= :startdate");
        } else {
            queryString = queryString.replace("DataFormate", "");
        }
        Query query = (Query) createQuery(context, queryString);
        query.setMaxResults(10);
        query.setParameter("dspaceobjecttype", Objecttype);
        if (startDate != null && endDate != null) {
            query.setParameter("enddate", endDate);
            query.setParameter("startdate", startDate);
        }
        query.setParameter("action", action);
        //System.err.println("queryString::" + queryString);
        return query.list();
    }

    @Override
    public List<Object[]> findEventTop10CommunityByCountry(Context context, int limit, int Objecttype, int action, String collactionID, String communityID, Date startDate, Date endDate, String item) throws Exception {        
        return null;
    }

    @Override
    public List<Object[]> findEventTop10ItemByCountry(Context context, int limit, int Objecttype, int action, String iteamID) throws Exception {
        String queryString = "SELECT  st.city as city, st.country as country,st.lat as lat,st.lon as lon, COUNT(st.city) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype and st.dspaceobjectid =:Objectid "
                + " GROUP BY st.city,st.country,st.lat,st.lon ORDER BY total DESC";
        Query query = (Query) createQuery(context, queryString);
        query.setParameter("action", action);
        query.setParameter("Objectid", UUID.fromString(iteamID));
        query.setParameter("dspaceobjecttype", Objecttype);
        return query.list();
    }

    @Override
    public List<Object[]> getIteamByMonthCount(Context context, String Query) throws Exception {
       String queryString = "SELECT CAST(st.action_date AS DATE) AS txn_month, COUNT( case when st.dspaceobjecttype = 2 THEN  st.action_date END) AS iteam ,COUNT( case when st.dspaceobjecttype = 0 THEN  st.action_date END) AS bit "
                + "FROM  Event as st where st.action = 4 and st.dspaceobjecttype in  (2,0)  "
                 + "    "+Query+" \n"
                + "GROUP BY  CAST(st.action_date AS DATE)  order by CAST(st.action_date AS DATE)    DESC";
        Query query = (org.hibernate.Query) createQuery(context, queryString);
        query.setMaxResults(10);        
        return query.list();
    }

    @Override
    public List<Object[]> getBitstreamByMonthCount(Context context, int limit, int Objecttype, int action, String iteamid) throws Exception {
        String queryString = "SELECT cast(st.action_date as date), COUNT(st.action_date) AS total FROM  Event as st  where st.action = :action and st.dspaceobjecttype= :dspaceobjecttype  and st.parenCollection =:Objectid"
                + " GROUP BY st.title, cast(st.action_date as date)  ORDER BY total DESC ";
        Query query = (Query) createQuery(context, queryString);
        query.setMaxResults(10);
        query.setParameter("dspaceobjecttype", Objecttype);
        query.setParameter("action", action);
        query.setParameter("Objectid", UUID.fromString(iteamid));
        return query.list();
    }

    @Override
    public int viewCount(Context context, UUID dspaceObjectid) throws Exception {

        String sqlQuery = " select count(*) from event where action=4 AND dspaceobjecttype = 2  ";
        if (dspaceObjectid != null) {
            sqlQuery = sqlQuery + " AND dspaceobjectid ='" + dspaceObjectid + "' ";
        }
        return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
    }
    @Override
    public int viewCountbyType(Context context, UUID dspaceObjectid,int type) throws Exception {

        String sqlQuery = " select count(*) from event where action=4 AND dspaceobjecttype = "+type;
        
        if (dspaceObjectid != null) {
            sqlQuery = sqlQuery + " AND dspaceobjectid ='" + dspaceObjectid + "' ";
        }
        return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
    }
    

    @Override
    public int bitViewCount(Context context, UUID dspaceObjectid) throws Exception {

        String sqlQuery = " select count(*) from event where action=4 AND dspaceobjecttype = 0";
        if (dspaceObjectid != null) {
            sqlQuery = sqlQuery + " AND dspaceobjectid ='" + dspaceObjectid + "' ";
        }

        //System.out.println("-- EventDAOImpl.java ----************ " + sqlQuery);
        return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
    }

    @Override
    public List<Event> getDataByFilter(Context context, String query, int count) throws Exception {
        String SQL = "SELECT  e \nFROM Event e where " + query;
        //System.out.println("workinProgres::" + SQL);
        Query q = (Query) createQuery(context, SQL);
        if (count != -1) {
            q.setFirstResult(count);
            q.setMaxResults(10);
        }
        return q.list();
    }

    @Override
    public Iterator getDataCountByFilter(Context context, String query, String GroubBy) throws Exception {
        String SQL = "SELECT " + GroubBy + ", COUNT(" + GroubBy + ")  \nFROM Event e where " + query + "  GROUP BY " + GroubBy;
        //String SQL = "SELECT "+GroubBy+", 100 * "+GroubBy+" / (SUM("+GroubBy+") OVER ()) \"% percentage\"   \nFROM Event e where " + query +"  GROUP BY " + GroubBy;
        //System.out.println("getDataCountByFilter::" + SQL);
        return ((Query) createQuery(context, SQL)).iterate();
    }

    @Override
    public Long getDataByFilterCount(Context context, String query) throws Exception {
        String SQL = "SELECT  count(e) \nFROM Event e where " + query;
        //System.out.println("workinProgres::" + SQL);
        Query q = (Query) createQuery(context, SQL);
        return (Long) q.uniqueResult();
    }

    @Override
    public List<Object[]> itemBarChart(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.country, " + GroupBy + ", count(" + GroupBy + "),e.dspaceobjecttype from event e LEFT JOIN geolocationevent ge "
                + "ON e.geolocationeventid = ge.uuid where e.dspaceobjecttype in (2,0) and e.action=4 " + Query + "  GROUP BY " + GroupBy + " ,ge.country,e.dspaceobjecttype";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(12);
        return query.list();
    }

    @Override
    public List<Object[]> itemLineChart(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString1 = "Select CAST(e.action_date AS DATE) , e.documenttype, count(CAST(e.action_date AS DATE))\n"
                + "from event e where dspaceobjectid in \n"
                + "(Select e.dspaceobjectid from event e where  e.action = 4 and e.dspaceobjecttype =2  \n"
                + "    "+Query+" \n"
                + " GROUP BY e.dspaceobjectid ORDER BY count(e.dspaceobjectid) DESC limit 10 )  "+Query+"  GROUP BY  e.documenttype ,CAST(e.action_date AS DATE)  ,e.dspaceobjecttype ORDER BY CAST(e.action_date AS DATE)    DESC";
        String queryString = "Select " + GroupBy + ", count(" + GroupBy + "),e.dspaceobjecttype from event e LEFT JOIN geolocationevent ge "
                + "ON e.geolocationeventid = ge.uuid where e.dspaceobjecttype in (2,0) and e.action=4 " + Query + "  GROUP BY " + GroupBy + " ,ge.country,e.dspaceobjecttype";       
        Query query = createSQLQuery(cntxt, queryString1);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        return query.list();
    }

    @Override
    public List<Object[]> itemLineChartAll(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString1 = "Select " + GroupBy + " , e.documenttype, count(" + GroupBy + ") from event e "
                + "where e.action = 4 and e.dspaceobjecttype =2   " + Query + "  GROUP BY  e.documenttype ," + GroupBy + " ,e.dspaceobjecttype ORDER BY " + GroupBy + "   DESC";
        String queryString = "Select CAST(e.action_date AS DATE) , e.documenttype, count(CAST(e.action_date AS DATE))\n"
                + "from event e where e.action = 4 and e.dspaceobjecttype =2   " + Query + "\n"
                +" GROUP BY  e.documenttype ,CAST(e.action_date AS DATE)  ,e.dspaceobjecttype ORDER BY CAST(e.action_date AS DATE)    DESC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        return query.list();
    }

    @Override
    public List<Object[]> ItemByDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select  Cast(e.dspaceobjectid as varchar)  ,count(e.dspaceobjectid)  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY Cast(e.dspaceobjectid as varchar) ORDER BY count(e.dspaceobjectid) DESC";

        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of ItemByDate ::" + queryString);
        query.setMaxResults(10);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.ITEM);
        return query.list();
    }

    @Override
    public int totalItemView(Context cntxt, String Query) throws Exception {
        String queryString = "Select count(*) from event e"
                + " where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " ";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.ITEM);
        return ((BigInteger) query.uniqueResult()).intValue();
    }

    @Override
    public int totalItemDownlode(Context cntxt, String Query) throws Exception {
        String queryString = "Select count(*) from event e"
                + " where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " ";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.BITSTREAM);
        return ((BigInteger) query.uniqueResult()).intValue();
    }

    @Override
    public List<Object[]> ItemViewBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) asc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        //query.setMaxResults(7);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.ITEM);
        return query.list();
    }
    @Override
    public List<Object[]> ItemViewDownloadBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE)),e.dspaceobjecttype  from event e where  e.action = :action and e.dspaceobjecttype in (2,0) " + Query + " GROUP BY CAST(e.action_date AS DATE),e.dspaceobjecttype ORDER BY CAST(e.action_date AS DATE) asc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        query.setParameter("action", Constant.VIEW);        
        return query.list();
    }

    @Override
    public List<Object[]> CommunitieViewBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) ASC";
        Query query = createSQLQuery(cntxt, queryString);
       //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(10);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.COMMUNITY);
        return query.list();
    }
    @Override
    public List<Object[]> CollectionViewBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) ASC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(10);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.COLLECTION);
        return query.list();
    }

    @Override
    public List<Object[]> SearchBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype and e.title notnull " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) ASC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of SearchBYDate ::" + queryString);        
        query.setParameter("action", Constant.Search);
        query.setParameter("dspaceobjecttype", Constants.SEARCH);
        return query.list();
    }

    @Override
    public List<Object[]> TypeViewBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE) , count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) ASC";
        Query query = createSQLQuery(cntxt, queryString);
        System.out.println("Query of itemBarChart ::" + queryString);
        
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.ITEM);
        return query.list();
    }

    @Override
    public List<Object[]> ItemByType(Context cntxt, String Query) throws Exception {
        String queryString = "Select  Cast(e.dspaceobjectid as varchar)  ,count(e.dspaceobjectid)  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY Cast(e.dspaceobjectid as varchar) ORDER BY count(e.dspaceobjectid) DESC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(5);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.ITEM);
        return query.list();
    }

    @Override
    public List<Object[]> ItemDownlodeBYDate(Context cntxt, String Query) throws Exception {
        String queryString = "Select CAST(e.action_date AS DATE), count(CAST(e.action_date AS DATE))  from event e where  e.action = :action and e.dspaceobjecttype =:dspaceobjecttype " + Query + " GROUP BY CAST(e.action_date AS DATE) ORDER BY CAST(e.action_date AS DATE) asc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
       // query.setMaxResults(7);
        query.setParameter("action", Constant.VIEW);
        query.setParameter("dspaceobjecttype", Constants.BITSTREAM);
        return query.list();
    }

    @Override
    public List<Object[]> getGeolocationData(Context cntxt, String Query) throws Exception {
        String queryString1 = "Select ge.city,ge.countrycode from event e LEFT JOIN geolocationevent ge "
                + "ON e.geolocationeventid = ge.uuid where   e.action=4   " + Query + "";
        String queryString="Select ge.countrycode ,e.dspaceobjecttype,e.action ,count(ge.countrycode) from event e LEFT JOIN geolocationevent ge ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (2,0,8) and ge.city notNULL and e.action  in (4,5) " + Query + " group by ge.countrycode , e.dspaceobjecttype ,ge.countrycode,e.action";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getGeolocationData ::" + queryString);

        return query.list();
    }

    @Override
    public List<Object[]> getTrendingCommunitie(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select " + GroupBy + ", count(" + GroupBy + ") from event e  "
                + "where e.dspaceobjecttype in (4) and e.action=4 " + Query + "  GROUP BY " + GroupBy + " ORDER BY count(" + GroupBy + ")  DESC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(10);
        return query.list();
    }
    @Override
    public List<Object[]> getTrendingCollection(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select " + GroupBy + ", count(" + GroupBy + ") from event e  "
                + "where e.dspaceobjecttype in (3) and e.action=4 " + Query + "  GROUP BY " + GroupBy + " ORDER BY count(" + GroupBy + ")  DESC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(10);
        return query.list();
    }

    @Override
    public List<Object[]> getTrendingSearch(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select " + GroupBy + ", count(" + GroupBy + ") from event e  "
                + "where e.dspaceobjecttype in (8) and e.action=5 and  e.title notnull " + Query + "  GROUP BY " + GroupBy + " ORDER BY count(" + GroupBy + ")  DESC";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);
        query.setMaxResults(10);
        return query.list();
    }

    @Override
    public List<Object[]> itemTypeChart(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select  e.documenttype, count(e.documenttype) from event e  "
                + " where  e.dspaceobjecttype in (2) and e.action = 4 " + Query + "  GROUP BY  e.documenttype ,e.dspaceobjecttype ";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object> getcountry(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select DISTINCT ge.country from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (2,0,7) and e.action  in (4,5) " + Query + " ";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getCountByObject(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select  ge.country,count(ge.country), e.dspaceobjecttype,e.action,countrycode from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (2,0,8) and ge.city notNULL and e.action  in (4,5) " + Query + " GROUP by ge.country,e.dspaceobjecttype,e.action,countrycode order by count(ge.country) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getCountByObject ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getcityByCountry(Context cntxt, String Query, String country, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (2,0,8) and ge.city notNULL and country='"+country+"' and e.action  in (4,5) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action order by count(ge.city) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        return query.list();
    }
    
    @Override
    public List<Object[]> getcityByObject(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action,lat,lon from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (2,0,8) and ge.city notNULL  and e.action  in (4,5) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action,lat,lon";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of itemBarChart ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getcityBycommunity(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action,lat,lon from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (4) and ge.city notNULL  and e.action  in (4) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action,lat,lon";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getcountryBycommunity(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.country,count(ge.country), e.dspaceobjecttype,e.action  from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (4) and ge.city notNULL  and e.action  in (4) " + Query + " GROUP by ge.country,e.dspaceobjecttype,e.action  order by count(ge.country) desc";
        Query query = createSQLQuery(cntxt, queryString);
        System.out.println("Query of getcountryBycommunity ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getcitycountryBycommunity(Context cntxt, String Query, String Country, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action  from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (4) and ge.city notNULL and country='"+Country+"'   and e.action  in (4) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action  order by count(ge.city) desc";
        Query query = createSQLQuery(cntxt, queryString);
        System.out.println("Query of getcitycountryBycommunity ::" + queryString);        
        return query.list();
    }
    
    
    @Override
    public List<Object[]> getcityByCollection(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action,lat,lon from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (3) and ge.city notNULL  and e.action  in (4) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action,lat,lon";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> getcountryByCollection(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.country,count(ge.country), e.dspaceobjecttype,e.action from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (3) and ge.city notNULL  and e.action  in (4) " + Query + " GROUP by ge.country,e.dspaceobjecttype,e.action  order by count(ge.country) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    
    @Override 
    public List<Object[]> getcitycountryByCollection(Context cntxt, String Query, String Country, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (3) and ge.city notNULL and country='"+Country+"'  and e.action  in (4) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action  order by count(ge.city) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    
    @Override
    public List<Object[]> getcityBySearch(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action,lat,lon from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (8) and ge.city notNULL  and e.action  in (5) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action,lat,lon";
        Query query = createSQLQuery(cntxt, queryString);
        System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    @Override 
    public List<Object[]> getcountryBySearch(Context cntxt, String Query, String GroupBy) throws Exception {
        String queryString = "Select ge.country,count(ge.country), e.dspaceobjecttype,e.action from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (8) and ge.city notNULL  and e.action  in (5) " + Query + " GROUP by ge.country,e.dspaceobjecttype,e.action  order by count(ge.country) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    
    @Override 
    public List<Object[]> getcitycountryBySearch(Context cntxt, String Query, String Country, String GroupBy) throws Exception {
        String queryString = "Select ge.city,count(ge.city), e.dspaceobjecttype,e.action from event e RIGHT JOIN geolocationevent ge"
                + " ON e.geolocationeventid = ge.uuid where  e.dspaceobjecttype in (8) and ge.city notNULL  and country='"+Country+"'  and e.action  in (5) " + Query + " GROUP by ge.city,e.dspaceobjecttype,e.action  order by count(ge.city) desc";
        Query query = createSQLQuery(cntxt, queryString);
        //System.out.println("Query of getcityBycommunity ::" + queryString);        
        return query.list();
    }
    @Override
    public List<Object[]> ViewCountReport(Context cntxt, String Query) throws Exception {
        Query query = createSQLQuery(cntxt, Query);
        return query.list();
    }

}
