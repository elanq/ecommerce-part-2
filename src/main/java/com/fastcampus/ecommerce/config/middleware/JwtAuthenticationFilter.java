package com.fastcampus.ecommerce.config.middleware;

import com.fastcampus.ecommerce.service.JwtService;
import com.fastcampus.ecommerce.service.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final UserDetailsImpl userDetailService;
  private final JwtService jwtService;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      try {
        filterChain.doFilter(request, response);
      } catch (InsufficientAuthenticationException exception) {
        handlerExceptionResolver.resolveException(request, response, null, exception);
      }
      return;
    }

    try {
      //Authorization => Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkVsYW4gUWlzdGhpIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlIjoiQURNSU5fUk9MRSIsInVzZXJfaWQiOjV9.H7KCD8-pC0KiMyIUdNzumqOLm11BoJeThj77c9cAF7k
      final String jwt = authHeader.substring(7);
      if (jwtService.validateToken(jwt)) {
        final String userIdentifier = jwtService.getUsernameFromToken(jwt);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userIdentifier != null && authentication != null) {
          UserDetails userDetails = userDetailService.loadUserByUsername(userIdentifier);
          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());

          authenticationToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
      }


    } catch (Exception exception) {
      handlerExceptionResolver.resolveException(request, response, null, exception);
    }
  }
}
