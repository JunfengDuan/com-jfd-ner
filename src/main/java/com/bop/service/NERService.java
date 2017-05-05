package com.bop.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static com.bop.util.NerType.*;

@Service
public class NERService {

	private static final Logger logger = LoggerFactory.getLogger(NERService.class);

	JdbcTemplate jdbcTemplate;

	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List queryList(){

		List<Map<String, Object>> list = null;
		try {
			list = jdbcTemplate.queryForList(QUERY);
		} catch (DataAccessException e) {
			logger.error("Database connect exception:{}",e.getMessage());
		}
		return list;
	}

	public void saveResults(Map<String,Object> segments){

		segments.entrySet().forEach(e -> {
			Map<String,Object> values = (Map<String,Object>) e.getValue();
			values.entrySet().forEach(entry ->logger.info("\n{} : {}-{}",e.getKey(),entry.getValue(),entry.getKey()));

			saveToDB(e.getKey(), values);
		});

	}

	private void saveToDB(String text, Map<String,Object> map){

		String org = (String) map.get(ORGANIZATION);
		String per = (String) map.get(PERSON);
		JSONArray other = JSONArray.parseArray((String) map.get(O));
		String jsonString = other.size()==0 ? null : other.toString();

//		String state = isSuccess(org, per);

		String value = "'"+text + "','" + org + "','" + per + "','" + jsonString + "'";
		String sql = String.format(SAVE, value);

		logger.info("Save sql :{}",sql);

		jdbcTemplate.execute(sql);

	}

	private String isSuccess(String org, String per){

		if(StringUtils.isBlank(org) || StringUtils.isBlank(per)){
			return "fail";
		}else {
			return "success";
		}
	}

}