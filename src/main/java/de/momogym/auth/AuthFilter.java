package de.momogym.auth;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {
	@Inject
	private UserSession userSession;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
		FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		HttpServletResponse res = (HttpServletResponse) servletResponse;

		String path = req.getRequestURI();

		boolean isLoginRequest = path.endsWith("login.xhtml");
		boolean isRegisterRequest = path.endsWith("register.xhtml");
		boolean isResourceRequest = path.contains("jakarta.faces.resource");

		if (isLoginRequest || isRegisterRequest || isResourceRequest || userSession.isLoggedIn()) {
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			res.sendRedirect(req.getContextPath() + "/login.xhtml");
		}
	}

	@Override
	public void destroy() {
	}
}
