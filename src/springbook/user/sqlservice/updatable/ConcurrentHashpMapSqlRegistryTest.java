package springbook.user.sqlservice.updatable;


import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import springbook.user.sqlservice.SqlNotFoundException;
import springbook.user.sqlservice.SqlUpdateFailureException;
import springbook.user.sqlservice.UpdatableSqlRegistry;

public class ConcurrentHashpMapSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {

	
	@Override
	protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
		return new ConcurrentHashMapSqlRegistry();
	}
	
	
}
