package com.blog.Security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
public class SecurityEvent {
    String  type;
    String  username;
    String  ipAddress;
    String  detail;
    Instant timestamp;
}