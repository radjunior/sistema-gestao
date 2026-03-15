package br.com.gestao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final String[] patterns = { "/css/**", "/js/**", "/assets/**" };

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(auth -> {
			auth.requestMatchers(patterns).permitAll();
			auth.anyRequest().authenticated();
		}).formLogin(login -> {
			login.loginPage("/login").defaultSuccessUrl("/home").permitAll();
		}).logout(logout -> {
			logout.logoutUrl("/logout").permitAll();
			logout.logoutSuccessUrl("/login?logout").permitAll();
		}).rememberMe(me -> {
			me.key("lembrarDeMim");
			me.alwaysRemember(true);
		}).build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
