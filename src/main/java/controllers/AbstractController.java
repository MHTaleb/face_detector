/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author taleb
 * @param <T> the wanted entity
 */
public abstract class AbstractController<T> {

    private final Class<T> entityClass;
    protected EntityManager lazyEm;

    public AbstractController(Class<T> entityClass) {
        this.entityClass = entityClass; // this reflette l object courant de la classe en cours
    }

    public int count() {
        final EntityManager em = getEntityManager();
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);

        cq.select(em.getCriteriaBuilder().count(rt));

        javax.persistence.Query q = em.createQuery(cq);

        return ((Long) q.getSingleResult()).intValue();
    }

    public void create(T entity) {
        final EntityManager em = getEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(entity);
            transaction.commit();

        } catch (Exception e) {
            transaction.rollback();
        }finally{
            em.close();
        }
    }

    public void edit(T entity) {
        final EntityManager em = getEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(entity);
            transaction.commit();

        } catch (Exception e) {
            transaction.rollback();
        }finally{
            em.close();
        }

    }

    public T find(Object id) {
        final EntityManager em = getEntityManager();
        final T find = em.find(entityClass, id);
        em.close();
        return find;
    }

    public List<T> findAll() {
        final EntityManager em = getEntityManager();
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();

        cq.select(cq.from(entityClass));

        em.close();
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        final EntityManager em = getEntityManager();
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();

        cq.select(cq.from(entityClass));

        javax.persistence.Query q = em.createQuery(cq);

        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);

        em.close();
        return q.getResultList();
    }

    public void remove(Long id) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            T entity;

            entity = em.getReference(entityClass, id);

            em.remove(entity);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    protected abstract EntityManager getEntityManager();
}