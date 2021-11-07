package com.swaraj.sportyshoes.repositories;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swaraj.sportyshoes.entities.Category;

@Transactional
@Repository
public class CategoryDAO {

	@Autowired
	private SessionFactory sessionFactory;

	public CategoryDAO() {
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveCategory(Category category) {
		Session session = this.sessionFactory.getCurrentSession();
    	session.saveOrUpdate(category);
    	session.flush();		
	}
	
	public Category findById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
        String sql= "SELECT a FROM Category a where a.id = :id";
    	Query<Category> query = session.createQuery(sql, Category.class);
		query.setParameter("id", id);
        return query.getSingleResult();
	}
	
    public List<Category> findAll() {
        Session session = this.sessionFactory.getCurrentSession();   
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> cq = cb.createQuery(Category.class);
        Root<Category> rootEntry = cq.from(Category.class);
        CriteriaQuery<Category> all = cq.select(rootEntry);

        TypedQuery<Category> allQuery = session.createQuery(all);
        return allQuery.getResultList();      
    }


}
