package com.swaraj.sportyshoes.repositories;

import javax.validation.Valid;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swaraj.sportyshoes.entities.Role;
import com.swaraj.sportyshoes.entities.User;

@Transactional
@Repository
public class RoleRepository {

	@Autowired
	private SessionFactory sessionFactory;

	public RoleRepository() {
	}

	public void save(@Valid Role role) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(role);
	}

	public Role getRoleByName(String name) {
		Session session = this.sessionFactory.getCurrentSession();
		String sql = "SELECT a FROM Role a where a.name = :name";
		Query<Role> query = session.createQuery(sql, Role.class);
		query.setParameter("name", name);
		return query.getSingleResult();
	}

}
