package dev.pacr;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord.RequiredPersistedData;

import java.util.UUID;

/**
 * Stores the minimal data required to reconstruct a WebAuthn credential record after the user
 * registers a passkey.
 */
public class WebAuthnCredential {
	
	public String username;
	public String credentialId;
	public UUID aaguid;
	public byte[] publicKey;
	public long publicKeyAlgorithm;
	public long counter;
	
	public WebAuthnCredential() {
	}
	
	/**
	 * Build from Quarkus's RequiredPersistedData record.
	 */
	public WebAuthnCredential(RequiredPersistedData data) {
		this.username = data.username();
		this.credentialId = data.credentialId();
		this.aaguid = data.aaguid();
		this.publicKey = data.publicKey();
		this.publicKeyAlgorithm = data.publicKeyAlgorithm();
		this.counter = data.counter();
	}
	
	/**
	 * Reconstruct the full WebAuthnCredentialRecord from stored data.
	 */
	public WebAuthnCredentialRecord toCredentialRecord() {
		RequiredPersistedData data =
				new RequiredPersistedData(username, credentialId, aaguid, publicKey,
						publicKeyAlgorithm, counter);
		return WebAuthnCredentialRecord.fromRequiredPersistedData(data);
	}
}
