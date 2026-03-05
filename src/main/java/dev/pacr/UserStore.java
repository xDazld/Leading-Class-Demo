package dev.pacr;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user store — fine for a classroom demo. Restarting the server clears all registered
 * users.
 */
@ApplicationScoped
public class UserStore {
	
	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	
	/**
	 * Return existing user or null.
	 */
	public User findByUsername(String username) {
		return users.get(username);
	}
	
	/**
	 * Return existing user or create a new one.
	 */
	public User getOrCreate(String username) {
		return users.computeIfAbsent(username, User::new);
	}
	
	/**
	 * Find a user who owns the given credential ID.
	 */
	public User findByCredentialId(String credentialId) {
		for (User user : users.values()) {
			for (WebAuthnCredential cred : user.credentials) {
				if (cred.credentialId.equals(credentialId)) {
					return user;
				}
			}
		}
		return null;
	}
}

