package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.Model.Role;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import com.blog.Security.SecurityEventLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints for user management and security monitoring. Requires ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class RestAdminController {
    private final UserRepository userRepository;
    private final SecurityEventLogger securityEventLogger;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "List all users",
        description = "Returns all registered users with their roles. Accessible only by ADMIN users."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<SuccessResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id",        u.getId(),
                        "username",  u.getUsername(),
                        "email",     u.getEmail(),
                        "fullName",  u.getFullName(),
                        "role",      u.getRole().name(),
                        "createdAt", u.getCreatedAt().toString()
                ))
                .toList();
        return ResponseEntity.ok(
            new SuccessResponse<>(HttpStatus.OK, "Users retrieved successfully", users)
        );
    }
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user role",
        description = "Changes a user's role. Valid roles: ADMIN, AUTHOR, READER. Callable only by ADMIN users."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role value"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<SuccessResponse<Map<String, Object>>> updateUserRole(@PathVariable Integer id, @RequestParam String role) {
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(HttpStatus.BAD_REQUEST, "Invalid role. Valid values: ADMIN, AUTHOR, READER"));
        }
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        Role previousRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Role updated from " + previousRole + " to " + newRole,
            Map.of(
                    "userId", id,
                    "username", user.getUsername(),
                    "previousRole", previousRole.name(),
                    "newRole", newRole.name()
            )
        ));
    }
    @GetMapping("/security/failures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Login failure report",
        description = "Returns per-user login failure counts for brute-force detection. Threshold for flagging: 5 consecutive failures. ADMIN only."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Failure report generated"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getFailureReport() {
        Map<String, Integer> failures = securityEventLogger.getFailureSummary();
        long flaggedUsers = failures.values().stream().filter(count -> count >= 5).count();
        Map<String, Object> report = Map.of(
            "totalUsersWithFailures", failures.size(),
            "flaggedForBruteForce",   flaggedUsers,
            "failureCounts",          failures,
            "threshold",              5
        );
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Security failure report generated", report));
    }
}
