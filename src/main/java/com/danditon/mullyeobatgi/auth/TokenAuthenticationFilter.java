package com.danditon.mullyeobatgi.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * SecurityConfig 에서만 만든다. 빈으로 등록하면 서블릿 필터로 한 번 더 걸린다.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIX = "Bearer ";

	private final TokenService tokenService;

	public TokenAuthenticationFilter(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public static String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith(PREFIX)) {
			return null;
		}
		return header.substring(PREFIX.length()).trim();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String token = extractToken(request);
		if (token != null && !token.isEmpty()) {
			tokenService.authenticate(token).ifPresent(user -> {
				AuthUser principal = new AuthUser(user.getId(), user.getName(), user.getRole());
				List<SimpleGrantedAuthority> authorities = user.getRole() == null
						? List.of()
						: List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
				var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			});
		}
		chain.doFilter(request, response);
	}

}
