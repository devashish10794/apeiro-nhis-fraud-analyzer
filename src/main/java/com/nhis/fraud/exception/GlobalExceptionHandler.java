package com.nhis.fraud.exception;

import com.nhis.fraud.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest req) {
		ErrorResponse body = new ErrorResponse(
			OffsetDateTime.now(),
			status.value(),
			error,
			message,
			req != null ? req.getRequestURI() : null
		);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage(), req);
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "validation_error", ex.getMessage(), req);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage(), req);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, "data_integrity_violation", ex.getMostSpecificCause().getMessage(), req);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
		return build(HttpStatus.PAYLOAD_TOO_LARGE, "payload_too_large", ex.getMessage(), req);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
		log.error("Unhandled error", ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "An unexpected error occurred", req);
	}
}


