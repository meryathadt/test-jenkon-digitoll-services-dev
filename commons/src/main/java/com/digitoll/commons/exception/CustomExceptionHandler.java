package com.digitoll.commons.exception;

import com.digitoll.commons.dto.ErrorDTO;
import com.digitoll.commons.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> clientErrorException(HttpStatusCodeException ex) {

        ErrorResponseDTO responseDTO = (ErrorResponseDTO) parseJson(ex.getResponseBodyAsString(), ErrorResponseDTO.class).orElse(
                new ErrorResponseDTO(ex.getStatusCode(), ex.getLocalizedMessage(), Collections.emptyList())
        );

        responseDTO.setStatus(ex.getStatusCode());
        responseDTO.setMessage(ex.getLocalizedMessage());

        //log.error(ex.getLocalizedMessage(), ex);
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    private Optional<Object> parseJson(String json, Class cls) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, cls));
        } catch (Exception e) {
            return Optional.ofNullable(null);
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        List<ErrorDTO> errors = Stream.concat(
                ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(e -> new ErrorDTO(StringUtils.capitalize(e.getField().toLowerCase()) + ": " + e.getDefaultMessage())),
                ex.getBindingResult().getGlobalErrors()
                        .stream()
                        .map(e -> new ErrorDTO(StringUtils.capitalize(e.getObjectName().toLowerCase()) + ": " + e.getDefaultMessage()))
        ).collect(Collectors.toList());

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                "The request could not be understood by the server due to malformed syntax.",
                errors
        );

        log.error(ex.getLocalizedMessage(), ex);

        return handleExceptionInternal(
                ex, responseDTO, headers, responseDTO.getStatus(), request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                new ErrorDTO(ex.getParameterName() + " parameter is missing")
        );

        log.error(ex.getLocalizedMessage(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        List<ErrorDTO> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorDTO(v.getPropertyPath() + ": " + v.getMessage()))
                .collect(Collectors.toList());

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                errors
        );

        log.error(ex.getLocalizedMessage(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                new ErrorDTO(ex.getName() + " should be of type " + ex.getRequiredType().getName())
        );

        log.error(ex.getLocalizedMessage(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.METHOD_NOT_ALLOWED,
                ex.getLocalizedMessage(),
                new ErrorDTO(ex.getMethod() + " method is not supported for this request.")
        );

        log.error(ex.getLocalizedMessage(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getLocalizedMessage(),
                new ErrorDTO(ex.getContentType() + " media type is not supported for this request.")
        );

        log.error(ex.getLocalizedMessage(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> usernameNotFoundException(UsernameNotFoundException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<Object> userExistsException(UserExistsException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.CONFLICT,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> noSuchElementException(NoSuchElementException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    } 
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> resourceNotFoundException(ResourceNotFoundException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }     
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException(BadCredentialsException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );
        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }      

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(AccessDeniedException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(NoPosIdAssignedToUserException.class)
    public ResponseEntity<Object> handleMissPosId(NoPosIdAssignedToUserException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }


    @ExceptionHandler(SaleIncompleteDataException.class)
    public ResponseEntity<Object> handleSaleIncomplete(SaleIncompleteDataException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(SaleRowIncompleteDataException.class)
    public ResponseEntity<Object> handleSaleRowIncomplete(SaleRowIncompleteDataException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(CTPosDTOIncompleteDataException.class)
    public ResponseEntity<Object> handleCTPosDTOIncompleteData(CTPosDTOIncompleteDataException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(SaleRowNotFoundException.class)
    public ResponseEntity<Object> handleSaleRowNotFound(SaleRowNotFoundException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<Object> handleSaleNotFoundException(SaleNotFoundException ex) {

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(),
                Collections.emptyList()
        );

        log.error(responseDTO.toString(), ex);

        return new ResponseEntity<Object>(
                responseDTO, new HttpHeaders(), responseDTO.getStatus()
        );
    }
}
