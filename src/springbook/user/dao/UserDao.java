package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.mysql.jdbc.MysqlErrorNumbers;

import springbook.user.domain.User;

public interface UserDao {

	public void add(User user);

	public void deleteAll();

	public User get(String id);

	public int getCount();
	
	public List<User> getAll();
	
	public void update(User user);
}
