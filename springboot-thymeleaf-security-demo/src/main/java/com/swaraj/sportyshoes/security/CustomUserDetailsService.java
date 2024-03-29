package com.swaraj.sportyshoes.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swaraj.sportyshoes.entities.User;
import com.swaraj.sportyshoes.repositories.UserRepository;

/**
 * @author Ramesh Fadatare
 *
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(userName);
		if(null!=user){
			return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
					getAuthorities(user));
		}else{
			throw new UsernameNotFoundException("Email " + userName + " not found");
		}				//.orElseThrow(() -> new UsernameNotFoundException("Email " + userName + " not found"));
		
		
	}

	private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
		String[] userRoles = user.getRoles().stream().map((role) -> role.getName()).toArray(String[]::new);
		Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(userRoles);
		return authorities;
	}
}
