package com.example.expense_management_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
	
	private SecurityUtils() {
	}
	
	public static String getCurrentUserEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new InsufficientAuthenticationException("Authentication required");
		}
		
		return authentication.getName();
	}

}
