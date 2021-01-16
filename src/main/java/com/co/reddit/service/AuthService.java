package com.co.reddit.service;

import com.co.reddit.dto.AuthenticationResponse;
import com.co.reddit.dto.LoginRequest;
import com.co.reddit.dto.RegisterRequest;
import com.co.reddit.exception.SpringRedditException;
import com.co.reddit.model.NotificationEmail;
import com.co.reddit.model.User;
import com.co.reddit.model.VerificationToken;
import com.co.reddit.repository.UserRepository;
import com.co.reddit.repository.VerificationTokenRepository;
import com.co.reddit.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    //Contructor injection
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);

       String token =  generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8080/api/auth/accountVerificacion/" + token));
    }

    private String generateVerificationToken(User user){
        String token  = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return  token;
    }

    public void verifyAccount(String token) {
       Optional<VerificationToken> verificationToken =  verificationTokenRepository.findByToken(token);
       verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token"));
       fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
         Authentication authenticate =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername()
                      ,loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);  /* SecurityContext is used to store the details of the currently authenticated user,So, if you have to get the
                                                                            username or any other user details, you need to get this SecurityContext first.*/

        String token = jwtProvider.generateToken(authenticate);
        return new AuthenticationResponse(token,loginRequest.getUsername());

    }
}
