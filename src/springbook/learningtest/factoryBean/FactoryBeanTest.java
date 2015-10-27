package springbook.learningtest.factoryBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration //설정파일 이름을 지정하지 않으면 클래스이름 + -context.xml 이 디폴트로 사용된다. 
public class FactoryBeanTest {
	@Autowired
	ApplicationContext context;
	
	@Test
	public void getMessageFromFactoryBean(){
		Object message = context.getBean("message"); // 타입이 확실하지 않으므로 Autowired가 아닌 getBean을 사용하였음.
		assertThat(message, is(Message.class)); //타입 확인
		assertThat(((Message)message).getText(), is("Factory Bean")); // 설정과 기능확인 
		
	}
	
	@Test
	public void getFactoryBean() throws Exception{
		Object factory = context.getBean("&message"); //&가 붙고 안붙고에 따라 getBean() 메소드가 돌려주는 오브젝트가 달라짐.
		assertThat(factory, is(MessageFactoryBean.class));
	}
}
