package com.example.expense_management_api.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmailIgnoreCase(email)
								  .orElseThrow(()-> new UsernameNotFoundException("User not found"));
		
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities("USER")
				.build();
	}

}
