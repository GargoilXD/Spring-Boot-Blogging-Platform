package com.blog.Security;

import java.time.Instant;

public record SecurityEvent(
        String  type,
        String  username,
        String  ipAddress,
        String  detail,
        Instant timestamp
) {}