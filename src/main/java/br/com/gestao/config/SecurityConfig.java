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
			auth.requestMatchers("/login").permitAll();
			auth.requestMatchers("/admin/**").hasAuthority("ADMIN_SAAS");
			auth.requestMatchers("/empresa/usuarios/**").hasAuthority("ADMIN_EMPRESA");
			auth.requestMatchers("/cadastro/**").hasAnyAuthority("ADMIN_EMPRESA", "GERENTE", "OPERADOR");
			auth.requestMatchers("/pdv/**").hasAnyAuthority("ADMIN_EMPRESA", "GERENTE", "OPERADOR");
			auth.requestMatchers("/financeiro/configuracao/**").hasAnyAuthority("ADMIN_EMPRESA", "GERENTE");
			auth.requestMatchers("/financeiro/**").hasAnyAuthority("ADMIN_EMPRESA", "GERENTE", "OPERADOR");
			auth.requestMatchers("/venda/**").hasAnyAuthority("ADMIN_EMPRESA", "GERENTE", "OPERADOR");
			auth.anyRequest().authenticated();
		}).formLogin(login -> {
			login.loginPage("/login").defaultSuccessUrl("/home").permitAll();
		}).logout(logout -> {
			logout.logoutUrl("/logout").permitAll();
			logout.logoutSuccessUrl("/login?logout").permitAll();
		}).rememberMe(me -> {
			me.key("lembrarDeMim");
			me.alwaysRemember(true);
		}).exceptionHandling(exception -> exception.accessDeniedPage("/403")).build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
