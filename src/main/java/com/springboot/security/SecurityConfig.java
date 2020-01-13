package com.springboot.security;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.springboot.security.dao.SecurityDAO;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DataSource dataSource;
	@Autowired private SecurityDAO sd;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		setAntMatchers(http, "ROLE_");

		http
//		.csrf().disable()
		//		.authorizeRequests()
		//		.antMatchers("/adminOnly").hasAuthority("ROLE_ADMIN")
		//		.antMatchers("/userOnly/**").hasAnyAuthority("ROLE_GUEST", "ROLE_ADMIN")
		//		.antMatchers("/**").permitAll()	// 넓은 범위의 URL을 아래에 배치한다.
		//		.anyRequest().authenticated()
		//		.and()
		.formLogin().loginPage("/login").failureUrl("/login?error").permitAll().successHandler(new LoginSuccessForOAauthHandler())
		.and()
		.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		.addLogoutHandler(new TaskImplementingLogoutHandler()).permitAll().logoutSuccessUrl("/");

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.jdbcAuthentication()
		.dataSource(dataSource)
		.rolePrefix("ROLE_")
		.usersByUsernameQuery("select username, replace(password, '$2y', '$2a'), true from simple_users where username = ?")
		.authoritiesByUsernameQuery("select username, role from simple_users where username = ?");

	}

	// passwordEncoder() 추가
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	protected void setAntMatchers(HttpSecurity http, String rolePrefix) throws Exception {
		List<Map<String, Object>> list = sd.getAuthReq();
		System.out.println(list);
		for(Map<String, Object> m : list) {
			
			String[] roles = m.get("hasAuthority").toString().split(",");
			
			for(int i = 0; i < roles.length; i++) {
				roles[i] = rolePrefix + roles[i].toUpperCase();
			}

			// DB에서 url을 읽어올 때 앞에 '/'이 없다면
			// 앞에 '/'를 넣어준다.
			String url = m.get("url").toString();
			if(url.charAt(0) != '/') {
				url = "/" + url;
			}
			
			http.authorizeRequests()
			.antMatchers(url)
			.hasAnyAuthority(roles);

			System.out.println(url);
			for(String s : roles) {
				System.out.println(s);
			}
		}

		http.authorizeRequests()
		.antMatchers("/**").permitAll()	// 넓은 범위의 URL을 아래에 배치한다.
		.anyRequest().authenticated();

	}

}