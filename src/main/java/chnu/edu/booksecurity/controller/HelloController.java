package chnu.edu.booksecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hello")
public class HelloController {

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public String helloAdmin() {
        return "Hello, admin!";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/user")
    public String helloUser() {
        return "Hello, user!";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    @GetMapping("/man")
    public String helloMan() {
        return "Hello, man!";
    }

    // no annotation: any authenticated user
    @GetMapping("/unknown")
    public String helloUnknown() {
        return "Hello, unknown!";
    }
}
