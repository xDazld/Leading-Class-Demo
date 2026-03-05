package dev.pacr;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord.RequiredPersistedData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * JPA-backed user store with persistent database storage.
 * All methods are called from worker threads (never from the IO event loop).
 */
@ApplicationScoped
public class UserStore {
	
	@Inject
	EntityManager em;
	
	/**
	 * Return existing user or null.
	 */
	@Transactional
	public User findByUsername(String username) {
		try {
			return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
					.setParameter("username", username).getSingleResult();
		} catch (jakarta.persistence.NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Return existing user or create a new one (must be called within a transaction).
	 */
	@Transactional
	public User getOrCreate(String username) {
		User user = findByUsername(username);
		if (user == null) {
			user = new User(username);
			em.persist(user);
		}
		return user;
	}
	
	/**
	 * Find a user who owns the given credential ID.
	 */
	@Transactional
	public User findByCredentialId(String credentialId) {
		try {
			return em.createQuery(
					"SELECT u FROM User u JOIN u.credentials c WHERE c.credentialId = " +
							":credentialId",
					User.class).setParameter("credentialId", credentialId).getSingleResult();
		} catch (jakarta.persistence.NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Persist a new credential for the given user (called after successful registration).
	 */
	@Transactional
	public void store(RequiredPersistedData data) {
		User user = getOrCreate(data.username());
		// Avoid duplicates
		user.credentials.removeIf(c -> c.credentialId.equals(data.credentialId()));
		WebAuthnCredential newCred = new WebAuthnCredential(data);
		newCred.user = user;
		user.credentials.add(newCred);
		em.persist(newCred);
	}
	
	/**
	 * Update the signature counter for a credential (called after successful login).
	 */
	@Transactional
	public void updateCounter(String credentialId, long counter) {
		try {
			WebAuthnCredential cred = em.createQuery(
							"SELECT c FROM WebAuthnCredential c WHERE c.credentialId = " +
									":credentialId",
							WebAuthnCredential.class).setParameter("credentialId", credentialId)
					.getSingleResult();
			cred.counter = counter;
		} catch (jakarta.persistence.NoResultException e) {
			// credential not found, nothing to update
		}
	}
}

