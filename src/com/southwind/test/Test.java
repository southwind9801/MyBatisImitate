package com.southwind.test;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.southwind.dao.StudentDAO;
import com.southwind.dao.UserDAO;
import com.southwind.entity.Student;
import com.southwind.entity.User;
import com.southwind.utils.MyInvocationHandler;
import com.southwind.utils.ParseXML;

public class Test {
	public static void main(String[] args) {
		
//		StudentDAO studentDAO = (StudentDAO) new MyInvocationHandler().getInstance(StudentDAO.class);
////		Student stu = studentDAO.getById(1);
////		Student stu = studentDAO.getByName("张三");
//		
////		Student student = new Student();
////		student.setId(1);
////		student.setName("张三");
////		Student stu = studentDAO.getByStudent(student);
//		
//		Student student = new Student();
//		student.setName("张三");
//		student.setTel("13567853467");
//		Student stu = studentDAO.getByStudent2(student);
//		
//		System.out.println(stu);
		
		UserDAO userDAO = (UserDAO) new MyInvocationHandler().getInstance(UserDAO.class);
		User user = userDAO.get(33);
		System.out.println(user);
				
	}
}
