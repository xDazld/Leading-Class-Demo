package dev.pacr;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bridges the Quarkus WebAuthn security layer to our in-memory user store.
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
		User user = userStore.findByUsername(username);
		if (user == null) {
			return Uni.createFrom().item(Collections.emptyList());
		}
		List<WebAuthnCredentialRecord> records =
				user.credentials.stream().map(WebAuthnCredential::toCredentialRecord)
						.collect(Collectors.toList());
		return Uni.createFrom().item(records);
	}
	
	/**
	 * Called during authentication — return a credential by its ID.
	 */
	@Override
	public Uni<WebAuthnCredentialRecord> findByCredentialId(String credentialId) {
		User user = userStore.findByCredentialId(credentialId);
		if (user == null) {
			return Uni.createFrom().nullItem();
		}
		return user.credentials.stream().filter(c -> c.credentialId.equals(credentialId))
				.findFirst().map(c -> Uni.createFrom().item(c.toCredentialRecord()))
				.orElse(Uni.createFrom().nullItem());
	}
	
	/**
	 * Called after successful registration — persist the new credential.
	 */
	@Override
	public Uni<Void> store(WebAuthnCredentialRecord record) {
		WebAuthnCredentialRecord.RequiredPersistedData data = record.getRequiredPersistedData();
		User user = userStore.getOrCreate(data.username());
		// avoid duplicates
		user.credentials.removeIf(c -> c.credentialId.equals(data.credentialId()));
		user.credentials.add(new WebAuthnCredential(data));
		return Uni.createFrom().voidItem();
	}
	
	/**
	 * Called after successful login — update the signature counter.
	 */
	@Override
	public Uni<Void> update(String credentialId, long counter) {
		User user = userStore.findByCredentialId(credentialId);
		if (user != null) {
			for (WebAuthnCredential cred : user.credentials) {
				if (cred.credentialId.equals(credentialId)) {
					cred.counter = counter;
					break;
				}
			}
		}
		return Uni.createFrom().voidItem();
	}
}
