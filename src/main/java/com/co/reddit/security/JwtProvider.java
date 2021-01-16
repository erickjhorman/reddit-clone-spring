package com.co.reddit.security;

import com.co.reddit.exception.SpringRedditException;
import org.springframework.security.core.userdetails.User;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream,"secret".toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SpringRedditException("Exception occured while retrieving public key from keystore");
        }
    }
  public String generateToken(Authentication authentication){
      User principal = (User) authentication.getPrincipal();
      return Jwts.builder()
              .setSubject(principal.getUsername())
              .signWith(getPrivateKey())
              .compact();
    }

    private Key getPrivateKey() {
      try {
         return  (PrivateKey) keyStore.getKey("springblog","secret".toCharArray());
      }catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e){
          throw new SpringRedditException("Exception occured while retrieving public key from keystore");
      }
    }

}
