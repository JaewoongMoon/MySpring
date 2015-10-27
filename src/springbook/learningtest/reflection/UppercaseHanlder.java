package springbook.learningtest.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UppercaseHanlder implements InvocationHandler {
	Object target;
	
	public UppercaseHanlder(Object target){
		this.target = target;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(target, args); //타킷으로 위임. 인터페이스 메소드 호출에 모두 적용된다.
		if(ret instanceof String && method.getName().startsWith("say")){
			return ((String)ret).toUpperCase();
		}else{
			return ret;
		}
	}
}
