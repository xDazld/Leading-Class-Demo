package dev.pacr;

import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord.RequiredPersistedData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA entity that stores the minimal data required to reconstruct a WebAuthn credential record
 * after the user registers a passkey.
 */
@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredential {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	public User user;
	
	@Column(unique = true, nullable = false)
	public String credentialId;
	
	@Column
	public UUID aaguid;
	
	@Column(columnDefinition = "BYTEA")
	public byte[] publicKey;
	
	@Column
	public long publicKeyAlgorithm;
	
	@Column
	public long counter;
	
	public String username;
	
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
