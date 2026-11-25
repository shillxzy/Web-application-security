package chnu.edu.booksecurity.user;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void init() {
        repository.deleteAll();
        repository.saveAll(List.of(
                newUser("Ivan", "Petrenko", "user@books.com", "user", Role.USER),
                newUser("Olena", "Kovalenko", "admin@books.com", "admin", Role.ADMIN),
                newUser("Mykola", "Shevchuk", "superadmin@books.com", "superadmin", Role.SUPERADMIN)
        ));
    }

    private User newUser(String firstName, String lastName, String email, String password, Role role) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .accountLocked(false)
                .roles(List.of(role))
                .build();
    }

    // users log in by email, not by username
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
