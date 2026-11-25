package chnu.edu.booksecurity.auth;

import chnu.edu.booksecurity.jwt.JwtService;
import chnu.edu.booksecurity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // throws BadCredentialsException (-> 401) if email or password is wrong
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = (User) auth.getPrincipal();
        var token = jwtService.generateToken(Map.of("fullName", user.getFullName()), user);
        return new AuthenticationResponse(token);
    }
}
