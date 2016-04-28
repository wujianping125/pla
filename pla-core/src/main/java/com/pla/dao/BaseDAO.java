package com.pla.dao;

import com.pla.query.Pager;
import com.pla.query.Record;
import com.pla.utils.PojoUtil;
import com.pla.utils.SimplePropertyUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BaseDAO<T> implements IBaseDAO<T> {
    protected Class<T> clazz;

    public BaseDAO() {
        this.clazz = PojoUtil.getSuperClassGenricType(getClass());
    }

    protected abstract Session getSession();

    public T load(Serializable id) {
        Criteria criteria = Criteria.create(clazz).idEq(id);
        return (T) criteria.getDetachedCriteria().getExecutableCriteria(getSession()).uniqueResult();
    }

    public List<T> list(Criteria criteria) {
        criteria.generateOrderBy();
        return criteria.getDetachedCriteria().getExecutableCriteria(getSession()).list();
    }

    public List<T> list(Criteria criteria, int offset, int size) {
        criteria.generateOrderBy();
        return criteria.getDetachedCriteria().getExecutableCriteria(getSession()).setFirstResult(offset)
                .setMaxResults(size).list();
    }

    public T first(Criteria criteria) {
        List<T> list = this.list(criteria, 0, 1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public int count(Criteria criteria) {
        Long count = (Long) criteria.getDetachedCriteria().getExecutableCriteria(getSession())
                .setProjection(Projections.rowCount()).uniqueResult();
        return count.intValue();
    }

    public T uniqueResult(Criteria criteria) {
        return (T) criteria.getDetachedCriteria().getExecutableCriteria(getSession()).uniqueResult();
    }

    public Pager<T> pager(Criteria criteria, int pageNo, int pageSize) {
        Pager<T> pager = new Pager<T>(pageNo, pageSize);
        int count = this.count(criteria);
        pager.setTotalCount(count);

        criteria.generateOrderBy();
        List<T> list = this.list(criteria, pager.getOffset(), pager.getPageSize());
        pager.setList(list);
        return pager;
    }

    //-------------------------- Query for parts of properties--------------------------
    public T uniqueResult(Criteria criteria, String... propertyNames) {
        try {
            if (propertyNames == null || propertyNames.length == 0)
                return null;

            ProjectionList projectionList = Projections.projectionList();

            for (String propertyName : propertyNames) {
                projectionList.add(Projections.property(propertyName));
            }

            Object obj = criteria.getDetachedCriteria().getExecutableCriteria(getSession())
                    .setProjection(projectionList).uniqueResult();
            T t = clazz.newInstance();
            if (obj instanceof Object[]) {
                Object[] objects = (Object[]) obj;
                for (int i = 0; i < objects.length; i++) {
                    SimplePropertyUtil.setProperty(t, propertyNames[i], objects[i]);
                }
            } else {
                SimplePropertyUtil.setProperty(t, propertyNames[0], obj);
            }
            return t;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    public T first(Criteria criteria, String... propertyNames) {
        List<T> list = list(criteria, 0, 1, propertyNames);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public List<T> list(Criteria criteria, String... propertyNames) {
        criteria.generateOrderBy();
        return list(criteria, null, null, propertyNames);
    }

    public List<T> list(Criteria criteria, Integer offset, Integer size, String... propertyNames) {
        try {
            criteria.generateOrderBy();
            if (propertyNames == null || propertyNames.length == 0)
                return null;
            ProjectionList projectionList =Projections.projectionList();
            for (String propertyName : propertyNames) {
                projectionList.add(Projections.property(propertyName));
            }

            org.hibernate.Criteria cri = criteria.getDetachedCriteria().getExecutableCriteria(getSession())
                    .setProjection(projectionList);

            if (offset != null)
                cri.setFirstResult(offset);
            if (size != null)
                cri.setMaxResults(size);
            List list = cri.list();
            List<T> records = new ArrayList<T>();
            for (Object obj : list) {
                T t = clazz.newInstance();
                if (obj instanceof Object[]) {
                    Object[] objects = (Object[]) obj;
                    for (int i = 0; i < objects.length; i++) {
                        SimplePropertyUtil.setProperty(t, propertyNames[i], objects[i]);
                    }
                } else {
                    SimplePropertyUtil.setProperty(t, propertyNames[0], obj);
                }
                records.add(t);
            }
            return records;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    public Pager<T> pager(Criteria criteria, int pageNo, int pageSize, String... propertyNames) {

        if (propertyNames == null || propertyNames.length == 0)
            return null;

        Pager<T> pager = new Pager<T>(pageNo, pageSize);
        int count = this.count(criteria);
        pager.setTotalCount(count);

        criteria.generateOrderBy();

        List<T> list = this.list(criteria, pager.getOffset(), pager.getPageSize(), propertyNames);
        pager.setList(list);
        return pager;
    }

    //-------------------------- Query for record --------------------------
    public Record record(Criteria criteria) {
        try {
            if (criteria.getAliasList().isEmpty()) {
                throw new HibernateException("It is not Object[] result.");
            }
            Object obj = criteria.getDetachedCriteria().getExecutableCriteria(getSession()).uniqueResult();
            Record record = new Record();
            if (obj instanceof Object[]) {
                Object[] objects = (Object[]) obj;
                for (int i = 0; i < objects.length; i++) {
                    record.put(criteria.getAliasList().get(i), objects[i]);
                }
            } else {
                record.put(criteria.getAliasList().get(0), obj);
            }
            return record;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    public List<Record> recordList(Criteria criteria) {
        return recordList(criteria, null, null);
    }

    public List<Record> recordList(Criteria criteria, Integer offset, Integer size) {
        try {
            if (criteria.getAliasList().isEmpty()) {
                throw new HibernateException("It is not Object[] results.");
            }

            criteria.generateOrderBy();

            org.hibernate.Criteria cri = criteria.getDetachedCriteria().getExecutableCriteria(getSession());

            if (offset != null)
                cri.setFirstResult(offset);
            if (size != null)
                cri.setMaxResults(size);
            List list = cri.list();
            List<Record> records = new ArrayList<Record>();
            for (Object obj : list) {
                Record record = new Record();
                if (obj instanceof Object[]) {
                    Object[] objects = (Object[]) obj;
                    for (int i = 0; i < objects.length; i++) {
                        record.put(criteria.getAliasList().get(i), objects[i]);
                    }
                } else {
                    record.put(criteria.getAliasList().get(0), obj);
                }
                records.add(record);
            }
            return records;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    public Record recordFirst(Criteria criteria) {
        List<Record> list = recordList(criteria, 0, 1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
