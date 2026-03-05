package dev.pacr;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/")
public class AuthResource {
	
	@Inject
	Template index;
	
	@Inject
	Template signup;
	
	@Inject
	Template login;
	
	@Inject
	Template dashboard;
	
	// ── public pages ──────────────────────────────────────────────────────────
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance home() {
		return index.instance();
	}
	
	@GET
	@Path("signup")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance signupPage() {
		return signup.instance();
	}
	
	@GET
	@Path("login")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance loginPage() {
		return login.instance();
	}
	
	// ── protected page ────────────────────────────────────────────────────────
	
	@GET
	@Path("dashboard")
	@Authenticated
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance dashboardPage(@Context SecurityContext sec) {
		String username =
				sec.getUserPrincipal() != null ? sec.getUserPrincipal().getName() : "User";
		return dashboard.data("username", username);
	}
}

