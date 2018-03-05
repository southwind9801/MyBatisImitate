package com.southwind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.southwind.entity.User;

public class MyInvocationHandler implements InvocationHandler{
	
	private String className;
	
	public Object getInstance(Class cls){
		//保存接口类型
		className = cls.getName();
        Object newProxyInstance = Proxy.newProxyInstance(  
        		cls.getClassLoader(),  
                new Class[] { cls }, 
                this); 
        return (Object)newProxyInstance;
    }
	
    public Object invoke(Object proxy, Method method, Object[] args)  throws Throwable {        
    	SAXReader reader = new SAXReader();
    	//返回结果
    	Object obj = null;
    	try {
    		//获取对应的mapper.xml
    		String xml = ParseXML.getMapperXML(className);
			Document document = reader.read(xml);
			Element root = document.getRootElement();
			Iterator iter = root.elementIterator();
			while(iter.hasNext()){
				Element element = (Element) iter.next();
				String id = element.attributeValue("id");
				if(method.getName().equals(id)){
					//获取C3P0信息，创建数据源对象
					Map<String,String> map = ParseXML.getC3P0Properties();
					ComboPooledDataSource datasource = new ComboPooledDataSource();
					datasource.setDriverClass(map.get("driver"));
					datasource.setJdbcUrl(map.get("url"));
					datasource.setUser(map.get("username"));
					datasource.setPassword(map.get("password"));
					datasource.setInitialPoolSize(20);
					datasource.setMaxPoolSize(40);
					datasource.setMinPoolSize(2);
					datasource.setAcquireIncrement(5);
					Connection conn = datasource.getConnection();
					//获取sql语句
					String sql = element.getText();
					//获取参数类型
					String parameterType = element.attributeValue("parameterType");
					//创建pstmt
					PreparedStatement pstmt = createPstmt(sql,parameterType,conn,args);
					ResultSet rs = pstmt.executeQuery();
					if(rs.next()){
						//读取返回数据类型
						String resultType = element.attributeValue("resultType");	
						//反射创建对象
						Class clazz = Class.forName(resultType);
						obj = clazz.newInstance();
						//获取ResultSet数据
						ResultSetMetaData rsmd = rs.getMetaData();
						//遍历实体类属性集合，依次将结果集中的值赋给属性
						Field[] fields = clazz.getDeclaredFields();
						for(int i = 0; i < fields.length; i++){
							Object value = setFieldValueByResultSet(fields[i],rsmd,rs);
							//通过属性名找到对应的setter方法
							String name = fields[i].getName();
							name = name.substring(0, 1).toUpperCase() + name.substring(1);
							String MethodName = "set"+name;
							Method methodObj = clazz.getMethod(MethodName,fields[i].getType());
							//调用setter方法完成赋值
							methodObj.invoke(obj, value);
						}
					}
					conn.close();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
       return obj;
    }
    
    /**
     * 根据条件创建pstmt
     * @param sql
     * @param parameterType
     * @param conn
     * @param args
     * @return
     * @throws Exception
     */
    public PreparedStatement createPstmt(String sql,String parameterType,Connection conn,Object[] args) throws Exception{
    	PreparedStatement pstmt = null;
    	try {
	    	switch(parameterType){
	    		case "int":
	    			int start = sql.indexOf("#{");
	        		int end = sql.indexOf("}");
	        		//获取参数占位符 #{name}
	        		String target = sql.substring(start, end+1);
	        		//将参数占位符替换为?
	        		sql = sql.replace(target, "?");
	        		pstmt = conn.prepareStatement(sql);
	    			int num = Integer.parseInt(args[0].toString());
					pstmt.setInt(1, num);
	    			break;
	    		case "java.lang.String":
	    			int start2 = sql.indexOf("#{");
	        		int end2 = sql.indexOf("}");
	        		String target2 = sql.substring(start2, end2+1);
	        		sql = sql.replace(target2, "?");
	        		pstmt = conn.prepareStatement(sql);
	    			String str = args[0].toString();
					pstmt.setString(1, str);
	    			break;
	    		default:
	    			Class clazz = Class.forName(parameterType);
	    			Object obj = args[0];
	    			boolean flag = true;
	    			//存储参数
	    			List<Object> values = new ArrayList<Object>();
	    			//保存带#的sql
	    			String sql2 = "";
	    			while(flag){
	    				int start3 = sql.indexOf("#{");
	    				//判断#{}是否替换完成
	    				if(start3<0){
	    					flag = false;
	    					break;
	    				}
	    				int end3 = sql.indexOf("}");
	    				String target3 = sql.substring(start3, end3+1);
	    				//获取#{}的值 如#{name}拿到name
	    				String name = sql.substring(start3+2, end3);
	    				//通过反射获取对应的getter方法
						name = name.substring(0, 1).toUpperCase() + name.substring(1);
						String MethodName = "get"+name;
						Method methodObj = clazz.getMethod(MethodName);
						//调用getter方法完成赋值
						Object value = methodObj.invoke(obj);
						values.add(value);
						sql = sql.replace(target3, "?");
						sql2 = sql.replace("?", "#");
	    			}
	    			//截取sql2，替换参数
	    			String[] sqls = sql2.split("#");
	    			pstmt = conn.prepareStatement(sql);
	    			for(int i = 0; i < sqls.length-1; i++){
	    				Object value = values.get(i);
	    				if("java.lang.String".equals(value.getClass().getName())){
	    					pstmt.setString(i+1, (String)value);
	    				}
	    				if("java.lang.Integer".equals(value.getClass().getName())){
	    					pstmt.setInt(i+1, (Integer)value);
	    				}
	    			}
	    			break;
	    		}
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return pstmt;
    }
    
    /**
     * 根据将结果集中的值赋给对应的属性
     * @param field
     * @param rsmd
     * @param rs
     * @return
     */
    public Object setFieldValueByResultSet(Field field,ResultSetMetaData rsmd,ResultSet rs){
    	Object result = null;
    	try {
			int count = rsmd.getColumnCount();
			for(int i=1;i<=count;i++){
				if(field.getName().equals(rsmd.getColumnName(i))){
					String type = field.getType().getName();
					switch (type) {
						case "int":
							result = rs.getInt(field.getName());
							break;
						case "java.lang.String":
							result = rs.getString(field.getName());
							break;
					default:
						break;
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
    
  
}
