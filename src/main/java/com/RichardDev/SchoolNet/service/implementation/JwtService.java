package com.RichardDev.SchoolNet.service.implementation;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }


    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {


        Long userId = null;
        if (userDetails instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) userDetails).getId();
            System.out.println("DEBUG - Generando token para usuario ID: " + userId + ", username: " + userDetails.getUsername());
        } else {
            System.out.println("WARNING - UserDetails no es CustomUserDetails, no se puede obtener ID");
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .claim("userId", userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object userIdObj = claims.get("userId");

            System.out.println("DEBUG - extractUserId - userIdObj: " + userIdObj + " (tipo: " + (userIdObj != null ? userIdObj.getClass().getSimpleName() : "null") + ")");

            if (userIdObj == null) {
                System.out.println("WARNING - userId claim es null en el token");
                return null;
            }

            if (userIdObj instanceof Integer) {
                Long result = ((Integer) userIdObj).longValue();
                System.out.println("DEBUG - userId extraído como Integer->Long: " + result);
                return result;
            }

            if (userIdObj instanceof Long) {
                System.out.println("DEBUG - userId extraído como Long: " + userIdObj);
                return (Long) userIdObj;
            }

            if (userIdObj instanceof String) {
                try {
                    Long result = Long.parseLong((String) userIdObj);
                    System.out.println("DEBUG - userId extraído como String->Long: " + result);
                    return result;
                } catch (NumberFormatException e) {
                    System.out.println("ERROR - No se pudo parsear userId como Long: " + userIdObj);
                    return null;
                }
            }

            System.out.println("ERROR - Tipo de userId no soportado: " + userIdObj.getClass().getSimpleName());
            return null;

        } catch (Exception e) {
            System.out.println("ERROR - Excepción al extraer userId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @PostConstruct
    public void debugJwtSecret() {
        System.out.println("DEBUG - JWT_SECRET cargado: " + secretKey);
    }

}
