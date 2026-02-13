package com.blog.ExceptionHandler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.blog.Exception.AuthenticationException;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable exception, @NonNull DataFetchingEnvironment env) {
        String fieldName = env.getField().getName();
        String parentType = ((Logger) env.getParentType()).getName();
        logError(exception, parentType, fieldName);
        String errorMessage = buildErrorMessage(exception);
        return GraphqlErrorBuilder.newError()
                .message(errorMessage)
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
            case IllegalArgumentException illegalArgumentException -> logger.warn(logMessage);
            case AuthenticationException authenticationException -> logger.warn(logMessage);
            default -> logger.error(logMessage, exception);
        }
    }
    
    private String buildErrorMessage(Throwable exception) {
        String profile = System.getProperty("spring.profiles.active", "dev");
        boolean isDev = "dev".equals(profile) || "local".equals(profile);

        return switch (exception) {
            case IllegalArgumentException illegalArgumentException -> "Invalid input: " + exception.getMessage();
            case AuthenticationException authenticationException -> "Authentication failed";
            case RuntimeException runtimeException when exception.getMessage() != null -> isDev ? exception.getMessage() : "Request failed";
            default -> isDev ? "Internal error: " + exception.getClass().getSimpleName() : "Request failed";
        };
    }
}