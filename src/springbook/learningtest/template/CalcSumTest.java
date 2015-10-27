package springbook.learningtest.template;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class CalcSumTest {
	Calculator calculator;
	String numFilePath;
	
	@Before
	public void setUp(){
		this.calculator = new Calculator();
		this.numFilePath = "D:\\numbers.txt";
	}
	
	@Test
	public void sumOfNumbers() throws IOException {
		int sum = calculator.calcSum(numFilePath);
		
		assertThat(sum, is(10));
	}
	
	@Test
	public void multiplyOfNumbers() throws IOException {
		assertThat(calculator.calcMultiply(this.numFilePath), is(24));
	}
	
	@Test
	public void concatenateStrings() throws IOException {
		assertThat(calculator.concatenate(this.numFilePath), is("1234"));
	}

}
