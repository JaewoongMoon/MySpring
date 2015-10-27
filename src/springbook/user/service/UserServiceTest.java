package springbook.user.service;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;

import static org.hamcrest.CoreMatchers.notNullValue;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.*;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
	
	@Autowired ApplicationContext context;  // 팩토리 빈을 가져오려면 애플리케이션 컨텍스트가 필요하다. 
	@Autowired UserService userService;
	@Autowired UserService testUserService; //같은 타입의 빈이 두 개 존재하기 때문에 필드이름을 기준으로 주입된 빈이 결정됨. 
	@Autowired UserServiceImpl userServiceImpl;
	@Autowired DataSource dataSource;
	@Autowired MailSender mailSender;
	@Autowired UserDao userDao;
	@Autowired PlatformTransactionManager transactionManager;
	
	List<User> users;
	
	@Before
	public void setUp(){
		users = Arrays.asList(
					new User("gyumee", "jwmoon", "springno1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "xxx@gmail.com"),
					new User("leegw700", "rika", "springno2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "xxx@gmail.com"),
					new User("bumjin", "yasuko", "springno3", Level.SILVER, MIN_RECCOMEND_FOR_GOLD-1, 29, "xxx@gmail.com"),
					new User("erwin", "yuya", "springno3", Level.SILVER, MIN_RECCOMEND_FOR_GOLD, 30, "xxx@gmail.com"),
					new User("mgnite", "kimura", "springno3", Level.GOLD, 100, Integer.MAX_VALUE, "xxx@gmail.com")
				);
	}
	
	@Test
	public void upgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl(); //고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성
		
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao); // 목 오브젝트로 만든 UserDao를 직접 DI해준다. 
		
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "leegw700", Level.SILVER);
		checkUserAndLevel(updated.get(1), "erwin", Level.GOLD);
		
		List<String> requests = mockMailSender.getRequests();
		assertThat(requests.size(), is(2));
		assertThat(requests.get(0), is(users.get(1).getEmail()));
		assertThat(requests.get(1), is(users.get(3).getEmail()));
	}
	
	@Test
	public void mockUpgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		UserDao mockUserDao = mock(UserDao.class);
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1));
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
	}
	
	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}
	
	private void checkLevelUpgraded(User user, boolean upgraded){
		User userUpdate = userDao.get(user.getId());
		if(upgraded){
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}else{
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	@Test
	public void add(){
		userDao.deleteAll();
		
		User userWithLevel = users.get(4); // GOLD LEVEL
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userServiceImpl.add(userWithLevel);
		userServiceImpl.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
		
	}
	
	static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "erwin"; //테스트 픽스쳐의 users(3)의 값 
		
		protected void upgradeLevel(User user) {
			if(user.getId().equals(this.id)){
				throw new TestUserServiceException();
			}
			super.upgradeLevels();
		}
	}
	
	static class TestUserServiceException extends RuntimeException{
		
	}
	
	static class MockMailSender implements MailSender{
		private List<String> requests = new ArrayList<String>();
		
		public List<String> getRequests(){
			return requests;
		}
		
		public void send(SimpleMailMessage mailMessage) throws MailException{
			requests.add(mailMessage.getTo()[0]);
		}
		
		public void send(SimpleMailMessage[] mailMessage) throws MailException{
		}
		
		
	}
	
	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList();
		
		private MockUserDao(List<User> users){
			this.users = users;
		}
		
		public List<User> getUpdated(){
			return this.updated;
		}
		
		public List<User> getAll(){
			return this.users;
		}
		
		public void update(User user){
			updated.add(user);
		}
		
		public void add (User user){throw new UnsupportedOperationException(); }
		
		public void deleteAll (){throw new UnsupportedOperationException(); }
		
		public User get (String id){throw new UnsupportedOperationException(); }
		
		public int getCount (){throw new UnsupportedOperationException(); }
	}
	
	@Test
	public void upgradeAllOrNothing() {

		userDao.deleteAll();
		for(User user:users){ userDao.add(user); }
		
		try{
		this.testUserService.upgradeLevels();
			fail("TestUserServiceException Expected!");
		}catch (TestUserServiceException e){
			
		}
		checkLevelUpgraded(users.get(1), false);
	}
}
