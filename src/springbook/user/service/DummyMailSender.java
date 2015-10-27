package springbook.user.service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class DummyMailSender implements MailSender {
	public void send(SimpleMailMessage arg0) throws MailException {
		// TODO Auto-generated method stub
		
	}
	public void send(SimpleMailMessage[] arg0) throws MailException {
		// TODO Auto-generated method stub
		
	}
}
