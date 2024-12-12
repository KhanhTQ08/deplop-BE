package com.datn.demo.AppConFig;

import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.RoleEntity;
import com.datn.demo.Repositories.AccountRepository;
import com.datn.demo.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class Config {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authorize -> authorize
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/user/**").hasRole("USER")
						.requestMatchers("/forgot-password").permitAll()
						.anyRequest().permitAll()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/perform_login")
						.defaultSuccessUrl("/index", true)
						.failureUrl("/login?error=true")
						.permitAll()
				)
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/login")
						.defaultSuccessUrl("/oauth2/success", true)
						.failureUrl("/login?error=true")
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout=true")
						.permitAll()
				)
				.csrf(csrf -> csrf.disable());

		return http.build();
	}

	private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		return new DefaultOAuth2UserService();
	}

	public void saveUser(String username, String password, String fullName, String phoneNumber, String email, RoleEntity role) {
		String encodedPassword = passwordEncoder().encode(password);
		AccountEntity account = new AccountEntity();
		account.setUsername(username);
		account.setPassword(encodedPassword);
		account.setFullName(fullName);
		account.setPhoneNumber(phoneNumber);
		account.setEmail(email);
		account.setRole(role);
		accountRepository.save(account);
	}
}