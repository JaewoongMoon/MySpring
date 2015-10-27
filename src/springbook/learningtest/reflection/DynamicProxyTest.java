package springbook.learningtest.reflection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

//import net.sf.cglib.transform.ClassFilter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;

public class DynamicProxyTest {
	
	@Test
	public void simpleProxy(){
		//Hello hello = new HelloTarget();
		Hello proxiedHello = (Hello)Proxy.newProxyInstance(
				getClass().getClassLoader(), // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로
				new Class[] {Hello.class}, //구현할 인터페이스
				new UppercaseHanlder(new HelloTarget())
				);
				
		assertThat(proxiedHello.sayHello("Ted"), is("HELLO TED"));
		assertThat(proxiedHello.sayHi("Ted"), is("HI TED"));
		assertThat(proxiedHello.sayThankYou("Ted"), is("THANK YOU TED"));
	}
	
	@Test
	public void HellUppercaseTest(){
		//Hello proxieHello = new HelloUppercase(new HelloTarget());
		Hello proxieHello = (Hello)Proxy.newProxyInstance(
				getClass().getClassLoader(), // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로
				new Class[] {Hello.class}, //구현할 인터페이스
				new UppercaseHanlder(new HelloTarget())
				);
				
		
		assertThat(proxieHello.sayHello("Ted"), is("HELLO TED"));
		assertThat(proxieHello.sayHi("Ted"), is("HI TED"));
		assertThat(proxieHello.sayThankYou("Ted"), is("THANK YOU TED"));
	}
	
	@Test
	public void proxyFactoryBean(){
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		pfBean.addAdvice(new UppercaseAdvice()); //부가기능을 담은 어드바이스를 추가한다. 여러개를 추가할 수도 있다. 
		
		Hello proxiedHello = (Hello)pfBean.getObject(); //FactoryBean이므로 getObject()로 생성된 프록시를 가져온다. 
		assertThat(proxiedHello.sayHello("Toby") , is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby") , is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby") , is("THANK YOU TOBY"));
		
	}
	
	static class UppercaseAdvice implements MethodInterceptor {
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String ret = (String)invocation.proceed(); //리플렉션의 Method와 달리 메소드 실행 시 타깃 오브젝트를 전달할 필요가 없다. MethodInvocation 은 메소드 정보와 함께 타깃 오브젝트를 알고 있기 때문이다. 
			return ret.toUpperCase();
		}
	}
	
	@Test
	public void pointcutAdvisor(){
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut(); //메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성
		pointcut.setMappedName("sayH*");
		
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		
		Hello proxiedHello = (Hello)pfBean.getObject(); 
		
		assertThat(proxiedHello.sayHello("Toby") , is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby") , is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby") , is("Thank You Toby"));
	}
	
	@Test
	public void classNamePointcutAdvisor(){
		//포인트 컷 준비
		NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut(){
			@Override
			public ClassFilter getClassFilter() {
				return new ClassFilter() {
					public boolean matches(Class<?> clazz){
						return clazz.getSimpleName().startsWith("HelloT");
					}
				};
			}
		};
		//classMethodPointcut.setClassFilter(classMethodPointcut.getClassFilter());
		classMethodPointcut.setMappedName("sayH*"); //sayH로 시작하는 메소드 이름을 가진 메소드만 선정한다.
		
		// 테스트
		checkAdviced(new HelloTarget(), classMethodPointcut, true); //Class 포인트컷이 적용되는 클래스
		
		
		class HelloWorld extends HelloTarget{};
		// 이 부분에서 테스트 오류..
		checkAdviced(new HelloWorld(), classMethodPointcut, false); //Class 포인트컷이 적용되지 않는 클래스
	
		class HelloToby extends HelloTarget{};
		checkAdviced(new HelloToby(), classMethodPointcut, true); 
	}
	
	private void checkAdviced(Object target, Pointcut pointcut, boolean adviced){
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(target);
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		if(adviced){
			assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY")); //메소드 선정 방식을 통해 어드바이스 적용
			assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
			assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
		}
		else{
			assertThat(proxiedHello.sayHello("Toby"), is("Hello Toby")); 
			assertThat(proxiedHello.sayHi("Toby"), is("Hi Toby"));
			assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
		}
	}
}
