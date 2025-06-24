package com.mtvs.flykidsbackend.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 클래스(Global Exception Handler)
 *
 * 이 클래스는 애플리케이션 전역에서 발생하는 예외를 처리하며,
 * 인증/인가 문제, 요청 데이터 검증 실패, 그리고 일반적인 서버 오류를
 * 일관된 방식으로 클라이언트에게 응답할 수 있도록 한다.
 *
 * 주요 기능:
 * - 인증 실패 시 401 Unauthorized 응답 처리
 * - 권한 부족 시 403 Forbidden 응답 처리
 * - 요청 DTO 검증 실패 시 필드별 오류 메시지 상세 반환
 * - 그 외 예상치 못한 예외에 대해 500 Internal Server Error 응답 처리
 * - 서버 로그에 예외 스택트레이스 출력하여 문제 추적 지원
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 인증 실패 처리 핸들러
     *
     * BadCredentialsException 예외가 발생하면 호출됨.
     * 클라이언트에게 401 상태코드와 명확한 메시지를 반환한다.
     *
     * @param ex 인증 실패 예외 객체
     * @return 401 상태와 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)  // HTTP 401 Unauthorized
                .body("인증 실패: 아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    /**
     * 권한 부족 처리 핸들러
     *
     * AccessDeniedException 예외가 발생하면 호출됨.
     * 클라이언트에게 403 상태코드와 명확한 권한 부족 메시지를 반환한다.
     *
     * @param ex 권한 부족 예외 객체
     * @return 403 상태와 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)  // HTTP 403 Forbidden
                .body("권한이 없습니다.");
    }

    /**
     * 요청 바디(파라미터) 검증 실패 처리
     *
     * 요청 DTO에 대해 @Valid 검증 중 실패하면 호출되는 메서드다.
     * 각 필드별 어떤 검증이 실패했는지 상세히 추출하여 클라이언트에 전달한다.
     *
     * @param ex      검증 실패 예외 객체(MethodArgumentNotValidException)
     * @param headers HTTP 헤더
     * @param status  HTTP 상태 코드 (스프링 6부터 HttpStatusCode 타입)
     * @param request 웹 요청 객체
     * @return 400 Bad Request 상태와 필드별 오류 메시지 맵을 포함한 ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        // BindingResult에서 모든 오류를 조회하며 필드명과 메시지를 추출
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();  // 실패한 필드명
            String errorMsg = error.getDefaultMessage();         // 검증 실패 메시지
            errors.put(fieldName, errorMsg);
        });

        // 클라이언트에 어떤 필드가 어떤 이유로 실패했는지 자세히 전달
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * 그 외 모든 예외 처리 핸들러
     *
     * 예상하지 못한 서버 내부 예외가 발생했을 때 호출된다.
     * 서버 콘솔에 스택 트레이스를 출력하여 디버깅에 도움을 주고,
     * 클라이언트에는 일반적인 오류 메시지와 500 상태를 응답한다.
     *
     * @param ex 발생한 예외 객체
     * @return 500 Internal Server Error 상태와 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        ex.printStackTrace();  // 서버 로그에 상세 오류 기록 (운영 환경에서는 로깅 프레임워크 활용 권장)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}
