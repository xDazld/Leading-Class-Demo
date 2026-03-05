package dev.pacr;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bridges the Quarkus WebAuthn security layer to our JPA-backed user store.
 * All methods offload blocking JPA work to a worker thread so that the Vert.x
 * IO (event-loop) thread is never blocked.
 */
@ApplicationScoped
public class PasskeyUserProvider implements WebAuthnUserProvider {
	
	@Inject
	UserStore userStore;
	
	/**
	 * Called during authentication — return all credentials for a username.
	 */
	@Override
	public Uni<List<WebAuthnCredentialRecord>> findByUsername(String username) {
		return Uni.createFrom().<List<WebAuthnCredentialRecord>>item(() -> {
			User user = userStore.findByUsername(username);
			if (user == null) {
				return Collections.emptyList();
			}
			return user.credentials.stream().map(WebAuthnCredential::toCredentialRecord)
					.collect(Collectors.toList());
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}
	
	/**
	 * Called during authentication — return a credential by its ID.
	 */
	@Override
	public Uni<WebAuthnCredentialRecord> findByCredentialId(String credentialId) {
		return Uni.createFrom().item(() -> {
			User user = userStore.findByCredentialId(credentialId);
			if (user == null) {
				return null;
			}
			return user.credentials.stream().filter(c -> c.credentialId.equals(credentialId))
					.findFirst().map(WebAuthnCredential::toCredentialRecord).orElse(null);
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}
	
	/**
	 * Called after successful registration — persist the new credential.
	 */
	@Override
	public Uni<Void> store(WebAuthnCredentialRecord record) {
		return Uni.createFrom().<Void>item(() -> {
			userStore.store(record.getRequiredPersistedData());
			return null;
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}
	
	/**
	 * Called after successful login — update the signature counter.
	 */
	@Override
	public Uni<Void> update(String credentialId, long counter) {
		return Uni.createFrom().<Void>item(() -> {
			userStore.updateCounter(credentialId, counter);
			return null;
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}
}
