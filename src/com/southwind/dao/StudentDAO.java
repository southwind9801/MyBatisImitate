package com.southwind.dao;

import com.southwind.entity.Student;

public interface StudentDAO {
	public Student getById(int id);
	public Student getByStudent(Student student);
	public Student getByName(String name);
	public Student getByStudent2(Student student);
}
