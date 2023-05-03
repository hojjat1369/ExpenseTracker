package ir.expense.tracker.makharej.common.util;


import io.jsonwebtoken.*;
import ir.expense.tracker.makharej.common.exception.InvalidToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
@Slf4j
public class JwtUtil {

	@Value("${expense.checker.jwt.secret}")
	private String jwtSecret;

	@Value("${expense.checker.jwt.expiration}")
	private int jwtExpirationMs;

	public String generateJwtToken(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		return Jwts.builder()
				   .setSubject((userPrincipal.getUsername()))
				   .setIssuedAt(new Date())
				   .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				   .signWith(SignatureAlgorithm.HS512, jwtSecret)
				   .compact();
	}

	public String getUserNameFromJwtToken(String token) {

		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) throws InvalidToken {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
			throw new InvalidToken();
		}
	}

}
