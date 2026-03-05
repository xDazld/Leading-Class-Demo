package dev.pacr;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory user model for the passkey learning demo.
 */
public class User {
	
	public final String username;
	public final List<WebAuthnCredential> credentials = new ArrayList<>();
	
	public User(String username) {
		this.username = username;
	}
}

