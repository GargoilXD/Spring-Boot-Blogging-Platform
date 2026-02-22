package com.blog.API.Rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/web")
@Tag(name = "CSRF Demo", description = "Demonstrates the CSRF token mechanism for form-based endpoints (Epic 3, User Story 3.1)")
public class CsrfDemoController {
    @GetMapping("/csrf-demo")
    @Operation(
        summary = "Get CSRF token",
        description = "Returns the current CSRF token. Send this value as the 'X-XSRF-TOKEN' " +
                      "header on any POST/PUT/DELETE request to /web/** endpoints."
    )
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken == null) {
            return ResponseEntity.ok(Map.of(
                "message",    "CSRF is disabled for /api/** (stateless JWT API)",
                "csrfToken",  "N/A",
                "headerName", "N/A"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "csrfToken",    csrfToken.getToken(),
            "headerName",   csrfToken.getHeaderName(),    // "X-XSRF-TOKEN"
            "parameterName", csrfToken.getParameterName(), // "_csrf"
            "note", "Include this token as the '" + csrfToken.getHeaderName() +
                    "' header on POST/PUT/DELETE requests to /web/** endpoints"
        ));
    }
    @PostMapping("/csrf-demo/submit")
    @Operation(
        summary = "CSRF-protected form submission",
        description = "Requires a valid X-XSRF-TOKEN header. Returns 403 if the CSRF token is " +
                      "missing or invalid. This demonstrates how CSRF protection works for " +
                      "form-based (stateful) endpoints."
    )
    public ResponseEntity<Map<String, String>> submitForm(@RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "CSRF token validated successfully. Form submission accepted.",
            "data",    body != null ? body.toString() : "no body"
        ));
    }
}
