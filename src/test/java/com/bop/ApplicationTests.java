package com.bop;

import org.dom4j.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ApplicationTests {

	JdbcTemplate jdbcTemplate;

	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

//	@Test
	public void contextLoads() {


	}
	public ApplicationTests(){

	}

	/*public static void main(String[] args){
		String s = "<ORGANIZATION>北京 酷宝 汽车 装饰 有限公司</ORGANIZATION>";
		String ss = "<ORGANIZATION>北京 酷宝 汽车 装饰 有限 公司</ORGANIZATION> 的 董事长 <PERSON>刘洋</PERSON> 的 出售 的 装饰 材料 不 符合 国家 标准";
		parseXml(s);
	}*/

	public static void main(String[] args) {
//		String str = "<div><h3>dsijiswer*dfhjgf</h3></div><table><h3>sdsd</h3></table>";
		String str = "<ORGANIZATION>北京 酷宝 汽车 装饰 有限 公司</ORGANIZATION> 的 董<a>事</a>长 <PERSON>刘洋</PERSON> 的 出售 的 装饰";
//		Pattern p = Pattern.compile("<h3.*?/h3>");
		/*Pattern p = Pattern.compile("<.*?>([^a-zA-Z]+)</.*?>");
		Matcher m = p.matcher(str);
		while (m.find()) {
			System.out.println(m.group());
		}*/
/*
		Map map = new IdentityHashMap<>();
		map.put("aa",2);
		map.put("aa",3);

		System.out.println(map);*/

//		String sql = "select AJNAME from GASJ01 where rownum <= 10";

//		List list = jdbcTemplate.queryForList(sql);

//		System.out.println(list);



	}

	private static void parseXml(String xml){

		try {
			Document document = DocumentHelper.parseText(xml);
			Element element = document.getRootElement();

			Object data = element.getData();
			String name = element.getName();
			String text = element.getText();
			String sss = "";
//			List<Element> elements = root.elements();

			/*for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
				Element element = it.next();
				List<Attribute> attributes = element.attributes();
				for (int i = 0; i < attributes.size(); i++) {
					Attribute attribute = attributes.get(i);
					if ("service".equals(attribute.getText())) {
						System.out.println(element.getName() + "  :  "
								+ element.getText());
					}
				}
			}*/
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}

}
