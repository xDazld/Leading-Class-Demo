package dev.pacr;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

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
	public TemplateInstance home(@Context HttpHeaders headers) {
		SiteExperience siteExperience = resolveSiteExperience(headers);
		return withCommonData(index.instance(), siteExperience);
	}
	
	@GET
	@Path("signup")
	@Produces(MediaType.TEXT_HTML)
	public Object signupPage(@Context HttpHeaders headers) {
		SiteExperience siteExperience = resolveSiteExperience(headers);
		if (!siteExperience.allowSignup()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return withCommonData(signup.instance(), siteExperience);
	}
	
	@GET
	@Path("login")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance loginPage(@Context HttpHeaders headers) {
		SiteExperience siteExperience = resolveSiteExperience(headers);
		return withCommonData(login.instance(), siteExperience);
	}
	
	@GET
	@Path("logo")
	@Produces("image/jpeg")
	public Response logo(@Context HttpHeaders headers) {
		SiteExperience siteExperience = resolveSiteExperience(headers);
		String picturePath = "pictures/" + siteExperience.logoFileName();
		try (InputStream picture = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(picturePath)) {
			if (picture == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			return Response.ok(picture.readAllBytes()).type("image/jpeg").build();
		} catch (IOException e) {
			throw new WebApplicationException("Unable to load logo image", e,
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	// ── protected page ────────────────────────────────────────────────────────
	
	@GET
	@Path("dashboard")
	@Authenticated
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance dashboardPage(@Context SecurityContext sec,
										  @Context HttpHeaders headers) {
		SiteExperience siteExperience = resolveSiteExperience(headers);
		String username =
				sec.getUserPrincipal() != null ? sec.getUserPrincipal().getName() : "User";
		return withCommonData(dashboard.instance(), siteExperience).data("username", username);
	}
	
	private TemplateInstance withCommonData(TemplateInstance templateInstance,
											SiteExperience siteExperience) {
		return templateInstance.data("canSignup", siteExperience.allowSignup())
				.data("logoPath", "/logo").data("siteHost", siteExperience.hostName());
	}
	
	private SiteExperience resolveSiteExperience(HttpHeaders headers) {
		String hostHeader = headers.getHeaderString(HttpHeaders.HOST);
		String host = normalizeHost(hostHeader);
		String firstLabel = firstLabel(host);
		
		if ("parker2".equals(firstLabel)) {
			return new SiteExperience(host.isBlank() ? "parker2.doggo.dance" : host,
					"Parker 2.jpeg", false);
		}
		
		if ("parker1".equals(firstLabel)) {
			return new SiteExperience(host.isBlank() ? "parker1.pacr.dev" : host, "Parker 1.jpeg",
					true);
		}
		
		return new SiteExperience("parker1.pacr.dev", "Parker 1.jpeg", true);
	}
	
	private String normalizeHost(String hostHeader) {
		if (hostHeader == null || hostHeader.isBlank()) {
			return "";
		}
		String normalized = hostHeader.trim().toLowerCase(Locale.ROOT);
		int portSeparator = normalized.indexOf(':');
		if (portSeparator >= 0) {
			return normalized.substring(0, portSeparator);
		}
		return normalized;
	}
	
	private String firstLabel(String host) {
		if (host == null || host.isBlank()) {
			return "";
		}
		int dotIndex = host.indexOf('.');
		if (dotIndex < 0) {
			return host;
		}
		return host.substring(0, dotIndex);
	}
	
	private record SiteExperience(String hostName, String logoFileName, boolean allowSignup) {
	}
}

