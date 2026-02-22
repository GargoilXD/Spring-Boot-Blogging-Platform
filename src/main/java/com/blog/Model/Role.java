package com.blog.Model;

/**
 * Defines the available user roles for Role-Based Access Control (RBAC).
 * - ADMIN: Full access including user management and admin endpoints
 * - AUTHOR: Can create, edit, and delete their own posts
 * - READER: Read-only access to published posts and comments
 */
public enum Role {
    ADMIN,
    AUTHOR,
    READER
}
