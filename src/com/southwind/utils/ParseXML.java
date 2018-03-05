package com.southwind.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ParseXML {
	
	//读取C3P0数据源配置信息
	public static Map<String,String> getC3P0Properties(){
		Map<String,String> map = new HashMap<String,String>();
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read("src/config.xml");
			//获取根节点
			Element root = document.getRootElement();
			Iterator iter = root.elementIterator();
			while(iter.hasNext()){
				Element e = (Element) iter.next();
				//解析environments节点
				if("environments".equals(e.getName())){
					Iterator iter2 = e.elementIterator();
					while(iter2.hasNext()){
						//解析environment节点
						Element e2 = (Element) iter2.next();
						Iterator iter3 = e2.elementIterator();
						while(iter3.hasNext()){
							Element e3 = (Element) iter3.next();
							//解析dataSource节点
							if("dataSource".equals(e3.getName())){
								if("POOLED".equals(e3.attributeValue("type"))){
									Iterator iter4 = e3.elementIterator();
									//获取数据库连接信息
									while(iter4.hasNext()){
										Element e4 = (Element) iter4.next();
										map.put(e4.attributeValue("name"),e4.attributeValue("value"));
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;	
	}
	
	//根据接口查找对应的mapper.xml
	public static String getMapperXML(String className){
		//保存xml路径
    	String xml = "";
    	SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read("src/config.xml");
			Element root = document.getRootElement();
			Iterator iter = root.elementIterator();
			while(iter.hasNext()){
				Element mappersElement = (Element) iter.next();
				if("mappers".equals(mappersElement.getName())){
					Iterator iter2 = mappersElement.elementIterator();
					while(iter2.hasNext()){
						Element mapperElement = (Element) iter2.next();
						//com.southwin.dao.UserDAO . 替换 #
						className = className.replace(".", "#");
						//获取接口结尾名
						String classNameEnd = className.split("#")[className.split("#").length-1];
						String resourceName = mapperElement.attributeValue("resource");
						//获取resource结尾名
						String resourceName2 = resourceName.split("/")[resourceName.split("/").length-1];
						//UserDAO.xml . 替换 #
						resourceName2 = resourceName2.replace(".", "#");
						String resourceNameEnd = resourceName2.split("#")[0];
						if(classNameEnd.equals(resourceNameEnd)){
							xml="src/"+resourceName;
						}
					}
				}
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xml;
	}
}
