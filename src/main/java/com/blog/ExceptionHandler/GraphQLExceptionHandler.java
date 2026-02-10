package com.blog.ExceptionHandler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.blog.Exception.DataAccessException;
import com.blog.Exception.AuthenticationException;
import com.blog.Exception.UserAlreadyExistsException;

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
        if (exception instanceof IllegalArgumentException) {
            logger.warn(logMessage);
        } else if (exception instanceof AuthenticationException) {
            logger.warn(logMessage);
        } else if (exception instanceof UserAlreadyExistsException) {
            logger.warn(logMessage);
        } else if (exception instanceof DataAccessException) {
            logger.error(logMessage, exception);
        } else {
            logger.error(logMessage, exception);
        }
    }
    
    private String buildErrorMessage(Throwable exception) {
        String profile = System.getProperty("spring.profiles.active", "dev");
        boolean isDev = "dev".equals(profile) || "local".equals(profile);
        
        if (exception instanceof IllegalArgumentException) {
            return "Invalid input: " + exception.getMessage();
        } else if (exception instanceof AuthenticationException) {
            return "Authentication failed";
        } else if (exception instanceof UserAlreadyExistsException) {
            return "Username already exists";
        } else if (exception instanceof DataAccessException) {
            return "Database error occurred";
        } else if (exception instanceof RuntimeException && exception.getMessage() != null) {
            return isDev ? exception.getMessage() : "Request failed";
        } else {
            return isDev ? "Internal error: " + exception.getClass().getSimpleName() : "Request failed";
        }
    }
}