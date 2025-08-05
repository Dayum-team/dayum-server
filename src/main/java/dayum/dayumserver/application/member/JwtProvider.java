package dayum.dayumserver.application.member;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final Key secretKey =
      Keys.hmacShaKeyFor("dayum-super-secret-key-please-change-this".getBytes());
  private final long accessTokenValidity = 1000L * 60 * 30; // 30분
  private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일

  public String createToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isValid(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getSubject(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public Date getExpiration(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration();
  }
}
