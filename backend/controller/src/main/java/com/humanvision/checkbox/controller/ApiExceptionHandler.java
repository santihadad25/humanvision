package com.humanvision.checkbox.controller;

import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidDocumentException.class)
    ProblemDetail handleInvalidDocument(InvalidDocumentException exception) {
        return rejectRequest(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleUploadTooLarge() {
        return rejectRequest(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file is too large.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ProblemDetail handleMissingMultipartField(MissingServletRequestPartException exception) {
        return rejectRequest(HttpStatus.BAD_REQUEST, "Missing multipart field '" + exception.getRequestPartName() + "'.");
    }

    @ExceptionHandler(MultipartException.class)
    ProblemDetail handleMalformedMultipart() {
        return rejectRequest(HttpStatus.BAD_REQUEST, "Request is not a valid multipart upload.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return rejectRequest(HttpStatus.METHOD_NOT_ALLOWED, "Method " + exception.getMethod() + " is not supported here.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ProblemDetail handleUnsupportedContentType() {
        return rejectRequest(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type must be multipart/form-data.");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    ResponseEntity<ProblemDetail> handleNotAcceptable() {
        ProblemDetail problem = rejectRequest(HttpStatus.NOT_ACCEPTABLE, "Only application/json responses are available.");
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler({ServletRequestBindingException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ProblemDetail handleMalformedRequest() {
        return rejectRequest(HttpStatus.BAD_REQUEST, "Malformed request.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleResourceNotFound() {
        return rejectRequest(HttpStatus.NOT_FOUND, "Resource not found.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.");
    }

    private static ProblemDetail rejectRequest(HttpStatus status, String reason) {
        log.atWarn()
                .addKeyValue("status", status.value())
                .addKeyValue("reason", reason)
                .log("request rejected");
        return ProblemDetail.forStatusAndDetail(status, reason);
    }
}
