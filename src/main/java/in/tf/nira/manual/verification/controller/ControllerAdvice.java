package in.tf.nira.manual.verification.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.tf.nira.manual.verification.exception.RequestException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestControllerAdvice
public class ControllerAdvice {
	
	@ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Object>> handleGeneralException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleRequestException(RequestException ex) {
    	List<ServiceError> errors = new ArrayList<>();		
		ResponseWrapper<Object> responseError = new ResponseWrapper<>();
		for (ServiceError serviceError : ex.getErrors()) {
			ServiceError errorResponse = new ServiceError();
			errorResponse.setErrorCode(serviceError.getErrorCode());
			errorResponse.setMessage(serviceError.getMessage());
			errors.add(errorResponse);
		}
		responseError.setErrors(errors);		
		return new ResponseEntity<>(responseError, HttpStatus.OK);
    }

    private ResponseEntity<ResponseWrapper<Object>> buildErrorResponse(HttpStatus status, String message, Exception ex) {
        ServiceError error = new ServiceError();
        error.setErrorCode(String.valueOf(status.value()));
        error.setMessage(message);

        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setErrors(List.of(error));
        responseWrapper.setResponse(null);

        return new ResponseEntity<>(responseWrapper, status);
    }
}
