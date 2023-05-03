package ir.expense.tracker.makharej.aspect;


import ir.expense.tracker.makharej.common.util.JwtUtil;
import ir.expense.tracker.makharej.dto.user.Userable;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


@Component
@Aspect
@Order(1)
@RequiredArgsConstructor
public class AppendUseImplementation {

	@Autowired
	private final JwtUtil jwtUtils;
	@Autowired
	private final UserService userService;

	@Pointcut("@annotation(ir.expense.tracker.makharej.aspect.AppendUser))")
	protected void appendUserInfo() {

		// For defining the Point Cut with annotation
	}

	@Around("appendUserInfo()")
	public Object appendUserInfo(ProceedingJoinPoint joinPoint) throws Throwable {

		appendUserInfoToInput(joinPoint);

		return joinPoint.proceed();
	}

	private void appendUserInfoToInput(ProceedingJoinPoint joinPoint) {

		String jwtToken = parseJwt();
		if (jwtToken != null){
			String username = jwtUtils.getUserNameFromJwtToken(jwtToken);
			if(StringUtils.hasText(username)){
				User user = userService.findUserByUsername(username);
				Userable userableInput = getUserableInput(joinPoint);
				if (user != null && userableInput != null){
					userableInput.setUserId(user.getId());
				}
			}
		}
	}

	private String getAuthorizationFromHeader(ProceedingJoinPoint joinPoint) {

		MethodSignature methodSignature = (MethodSignature) joinPoint.getStaticPart().getSignature();
		Method method = methodSignature.getMethod();
		return getAuthorizationHeader(method, joinPoint.getArgs());
	}

	public static String getAuthorizationHeader(Method method, Object[] parameters) {

		Annotation[][] methodAnnotations = method.getParameterAnnotations();

		if (methodAnnotations != null){
			for (int i = 0; i < methodAnnotations.length; i++){
				if (methodAnnotations[i].length > 0 && (methodAnnotations[i][0].annotationType().equals(RequestHeader.class))){
					return (String) parameters[i];
				}
			}
		}
		return null;
	}

	private Userable getUserableInput(ProceedingJoinPoint joinPoint) {

		Userable userable = null;
		if(joinPoint != null && joinPoint.getArgs() != null)
		for (Object arg : joinPoint.getArgs()){
			if (arg instanceof Userable){
				userable = (Userable) arg;
				break;
			}
		}
		return userable;
	}

	private boolean hasAuthorizationBearer(String header) {

		return (header != null && !header.isEmpty() && header.startsWith("Bearer"));
	}

	private String getAccessToken(String header) {

		return header.split(" ")[1].trim();
	}

	private String parseJwt() {

		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}

}
