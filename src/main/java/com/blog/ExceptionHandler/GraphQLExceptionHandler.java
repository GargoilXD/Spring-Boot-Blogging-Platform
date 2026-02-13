package com.blog.ExceptionHandler;

import com.blog.Exception.AuthenticationException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLType;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLExceptionHandler.class);

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable exception, @NonNull DataFetchingEnvironment env) {
        String fieldName = env.getField().getName();
        GraphQLType parentType = env.getParentType();
        logError(exception, parentType.toString(), fieldName);
        ErrorInfo errorInfo = buildErrorMessage(exception);
        return GraphqlErrorBuilder.newError()
                .message(errorInfo.message())
                .extensions(errorInfo.extensions())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
    
    private void logError(Throwable exception, String parentType, String fieldName) {
        String logMessage = String.format(
            "GraphQL error in %s.%s: %s",
            parentType,
            fieldName,
            exception.getMessage()
        );
        switch (exception) {
            case IllegalArgumentException ignored -> logger.warn(logMessage);
            case AuthenticationException ignored -> logger.warn(logMessage);
            case EntityNotFoundException ignored -> logger.warn(logMessage);
            case EntityExistsException ignored -> logger.warn(logMessage);
            default -> logger.error(logMessage, exception);
        }
    }

    private ErrorInfo buildErrorMessage(Throwable exception) {
        String profile = System.getProperty("spring.profiles.active", "dev");
        boolean isDev = "dev".equals(profile) || "local".equals(profile);

        return switch (exception) {
            case IllegalArgumentException ignored -> new ErrorInfo(
                "Invalid input provided. Please check your request parameters and try again.",
                Map.of("code", "INVALID_INPUT", "type", "VALIDATION_ERROR")
            );
            case AuthenticationException ignored -> new ErrorInfo(
                "Authentication failed. Please check your credentials and try again.",
                Map.of("code", "AUTHENTICATION_FAILED", "type", "AUTH_ERROR")
            );
            case EntityNotFoundException ignored -> new ErrorInfo(
                "The requested resource was not found. It may have been deleted or you don't have permission to access it.",
                Map.of("code", "NOT_FOUND", "type", "RESOURCE_ERROR")
            );
            case EntityExistsException ignored -> new ErrorInfo(
                "A resource with these details already exists. Please use different values or check if the resource already exists.",
                Map.of("code", "ALREADY_EXISTS", "type", "CONFLICT_ERROR")
            );
            case RuntimeException runtimeException when runtimeException.getMessage() != null -> {
                if (isDev) {
                    yield new ErrorInfo(
                        runtimeException.getMessage(),
                        Map.of(
                            "code", "RUNTIME_ERROR",
                            "type", "SERVER_ERROR",
                            "exception", runtimeException.getClass().getSimpleName()
                        )
                    );
                } else {
                    yield new ErrorInfo(
                        "An error occurred while processing your request. Please try again later.",
                        Map.of("code", "INTERNAL_ERROR", "type", "SERVER_ERROR")
                    );
                }
            }
            default -> {
                if (isDev) {
                    yield new ErrorInfo(
                        "Internal server error: " + exception.getClass().getSimpleName(),
                        Map.of(
                            "code", "INTERNAL_ERROR",
                            "type", "SERVER_ERROR",
                            "exception", exception.getClass().getSimpleName()
                        )
                    );
                } else {
                    yield new ErrorInfo(
                        "An unexpected error occurred. Please try again later or contact support if the problem persists.",
                        Map.of("code", "INTERNAL_ERROR", "type", "SERVER_ERROR")
                    );
                }
            }
        };
    }

    private record ErrorInfo(String message, Map<String, Object> extensions) {}
}