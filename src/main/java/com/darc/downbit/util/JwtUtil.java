package com.darc.downbit.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/24-22:18:46
 * @description
 */
@Slf4j
public class JwtUtil {

    private static final String ISSUER = "darc";

    private static final String SECRET = "darc@downbit";

    public static String createToken(String subject, String username, long expiration) {
        return Jwts.builder()
                //设置头部信息
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                //设置载荷信息
                .setSubject(subject)
                .claim("username", username)
                .setIssuer(ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                //设置签名
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            // 添加日志打印
            log.warn("Token解析失败");
            return null;
        }

    }


}
