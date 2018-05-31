/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Hashtable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import services.EMF_Provider;


/**
 *
 * @author taleb
 * @param <T>
 */
public class GenericEntityController<T> extends AbstractController<T>{

    public GenericEntityController(Class<T> entityClass) {
        super(entityClass);
    }

    public List<T> findByQuery(String queryName,Hashtable<String,Object> param){
        final Query createNamedQuery = getEntityManager().createNamedQuery(queryName);
        param.entrySet().stream().forEach(entry -> {
        createNamedQuery.setParameter(entry.getKey(), entry.getValue());
        });
        return createNamedQuery.getResultList();
    }
    
    public void RemoveUpdateByQuery(String queryName,Hashtable<String,Object> param){
        final EntityManager em = getEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        
        transaction.begin();
        try {
            final Query createNamedQuery = em.createNamedQuery(queryName);
            param.entrySet().stream().forEach(entry -> {
            createNamedQuery.setParameter(entry.getKey(), entry.getValue());
            });
            createNamedQuery.executeUpdate();
            
        } catch (Exception e) {
            transaction.rollback();
        }finally{
            
            transaction.commit();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return EMF_Provider.getCurrentSessionEMF().createEntityManager();
                
    }
    
}