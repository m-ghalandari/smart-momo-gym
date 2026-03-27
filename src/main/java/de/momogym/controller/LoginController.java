package de.momogym.controller;

import de.momogym.services.LoginService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class LoginController {

	private String username;
	private String password;

	@Inject
	private LoginService loginService;

	public String login() {
		return loginService.login(username, password);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
