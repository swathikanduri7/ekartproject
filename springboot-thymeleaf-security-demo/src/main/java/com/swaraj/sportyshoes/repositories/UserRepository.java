package com.swaraj.sportyshoes.repositories;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swaraj.sportyshoes.entities.User;



/*public interface UserRepository extends JpaRepository<User, Integer>
{

	Optional<User> findByEmail(String email);

}*/

@Transactional
@Repository
public class UserRepository 
{

	 @Autowired
	    private SessionFactory sessionFactory;

	    public User findByEmail(String email) {
	        Session session = this.sessionFactory.getCurrentSession();
	        String sql= "SELECT a FROM User a where a.email = :email";
	    	Query<User> query = session.createQuery(sql, User.class);
			query.setParameter("email", email);
	        return query.getSingleResult();
	    }
	    
	    public List<User> findAll() {
	        Session session = this.sessionFactory.getCurrentSession();
	   
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        CriteriaQuery<User> cq = cb.createQuery(User.class);
	        Root<User> rootEntry = cq.from(User.class);
	        CriteriaQuery<User> all = cq.select(rootEntry);

	        TypedQuery<User> allQuery = session.createQuery(all);
	        return allQuery.getResultList();      
	    }

		public void save(@Valid User user) {
			Session session = this.sessionFactory.getCurrentSession();
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    	session.saveOrUpdate(user);		
		}

		public User findById(int id) {
			Session session = this.sessionFactory.getCurrentSession();
	        String sql= "SELECT a FROM User a where a.id = :id";
	    	Query<User> query = session.createQuery(sql, User.class);
			query.setParameter("id", id);
	        return query.getSingleResult();
		}


}
