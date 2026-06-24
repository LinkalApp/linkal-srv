package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.domain.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Rest
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminResource {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserDetail>> findAll(
            @RequestParam(required = false) RoleType role,
            @RequestParam(required = false) Boolean verified) {
        return ResponseEntity.ok(userService.findAll(role, verified));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDetail> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
