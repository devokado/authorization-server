//package com.devokado.authServer.exceptions;
//
//import com.devokado.authServer.model.response.ApiError;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@RestControllerAdvice
//@EnableWebMvc
//public class RestExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @ExceptionHandler(RestException.class)
//    @ResponseStatus()
//    protected ResponseEntity<Object> handleRestException(RestException ex) {
//        ApiError apiError = new ApiError();
//        if (ex.getStatus() == 0)
//            apiError.setStatus(HttpStatus.BAD_REQUEST.value());
//        else
//            apiError.setStatus(ex.getStatus());
//        apiError.setMessage(ex.getMessage());
//        return buildResponseEntity(apiError);
//    }
//
//    @ExceptionHandler(CustomException.class)
//    protected ResponseEntity<Object> handleCustomException(CustomException ex) {
//        return buildResponseEntityCustom(ex.getObject(), ex.getStatus());
//    }
//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            HttpHeaders headers,
//            HttpStatus status,
//            WebRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value());
//        apiError.setMessage("Validation error");
//        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
//        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
//        return buildResponseEntity(apiError);
//    }
//
//    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
//        return ResponseEntity.status(apiError.getStatus()).body(apiError);
//    }
//
//    private ResponseEntity<Object> buildResponseEntityCustom(Object object, int status) {
//        return ResponseEntity.status(status).body(object);
//    }
//}