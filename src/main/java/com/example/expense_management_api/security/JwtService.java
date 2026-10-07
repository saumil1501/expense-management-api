package com.example.expense_management_api.security;

import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.PublicKey;
import java.util.*;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.expense_management_api.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	private final String secret;
	private final long expiration;
	
	public JwtService(@Value("${jwt.secret}") String secret,
					  @Value("${jwt.expiration}") long expiration) {
		this.secret = secret;
		this.expiration = expiration;
	}
	
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	public String generateToken(User user) {
		
		Date now = new Date();
		
		Date expirationDate = new Date(expiration + now.getTime());
		
		return Jwts.builder()
					.subject(user.getEmail())
					.claim("userId", user.getId())
					.issuedAt(now)
					.expiration(expirationDate)
					.signWith(getSigningKey())
					.compact();
	}
	
	private Claims extractAllClaims(String token) {
		
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public String extractEmail(String token) {
		return extractAllClaims(token).getSubject();
	}
	
	private Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	public boolean isTokenValid(String token, String email) {

	    String tokenEmail = extractEmail(token);

	    return tokenEmail.equalsIgnoreCase(email)
	            && !isTokenExpired(token);
	}

}
