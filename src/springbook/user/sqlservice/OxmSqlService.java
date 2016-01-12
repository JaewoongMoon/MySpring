package springbook.user.sqlservice;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class OxmSqlService implements SqlService{

	private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
	
	private final BaseSqlService baseSqlService = new BaseSqlService();
	
	private Resource sqlmap;
	
	public void setSqlmap(Resource sqlmap) {
		this.oxmSqlReader.setSqlmap(sqlmap);
	}

	private SqlRegistry sqlRegistry = new HashMapSqlRegistry();

	public void setSqlRegistry(SqlRegistry sqlRegistry) {
		this.sqlRegistry = sqlRegistry;
	}
	
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.oxmSqlReader.setUnmarshaller(unmarshaller);
	}

	@PostConstruct
	public void loadSql(){
		this.baseSqlService.setSqlReader(this.oxmSqlReader);
		this.baseSqlService.setSqlRegistry(this.sqlRegistry);
		this.baseSqlService.loadSql();
	}
	
	public String getSql(String key) throws SqlRetrievalFailureException {
		return this.baseSqlService.getSql(key);
	}
	
	private class OxmSqlReader implements SqlReader {
		private Unmarshaller unmarshaller;
	
		private Resource sqlmap = new ClassPathResource("sqlmap.xml", UserDao.class); //default
		
		public void setSqlmap(Resource sqlmap) {
			this.sqlmap = sqlmap;
		}
		public void setUnmarshaller(Unmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}
		
		public void read(SqlRegistry sqlRegistry) {
			
			try{
				Source source = new StreamSource(sqlmap.getInputStream());
				
				Sqlmap sqlmap = (Sqlmap)unmarshaller.unmarshal(source);
				System.out.println("size : " + sqlmap.getSql().size());
				for(SqlType sql : sqlmap.getSql()){
					sqlRegistry.registerSql(sql.getKey(), sql.getValue());
				}
				
			}catch(IOException e){
				throw new IllegalArgumentException(this.sqlmap.getFilename() + "을 가져올 수 없습니다. ", e);
			}
		}
		
	}
}
