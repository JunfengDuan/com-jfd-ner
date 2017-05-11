package com.bop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${sourceTableName:tbl_case_simple_basic}")
	private String sourceTableName;
	@Value("${sourceFieldName:name}")
	private String sourceFieldName;
	@Value("${targetTableName:N_FAZHI}")
	private String targetTableName;

	public Map<String, Object> takeFromDeque(BlockingDeque<Map> blockingDeque) {
		try {
			return blockingDeque.take();

		} catch (InterruptedException e) {
			logger.error("Take message from deque error:{}", e.getMessage());
			return  new HashMap<>();
		}
	}

	public void queryFromDB(AtomicInteger offset, BlockingDeque<Map> blockingDeque, int capacity){
		try {
			String querySql = String.format(QUERY, sourceFieldName,sourceFieldName,sourceTableName,offset.get()+capacity, offset);
			logger.info("Query sql :{}",querySql);

			List<Map<String, Object>>list = jdbcTemplate.queryForList(querySql);
			blockingDeque.addAll(list);
		} catch (DataAccessException e) {
			logger.error("Database connect exception:{}",e.getMessage());
		}
	}

	public void saveResults(String id, Map<String,Object> segments){

		segments.entrySet().forEach(e -> {
			Map<String,Object> values = (Map<String,Object>) e.getValue();
			values.entrySet().forEach(entry ->logger.info("\n{} : {}-{}",e.getKey(),entry.getValue(),entry.getKey()));

			saveToDB(id, e.getKey(), values);
		});

	}

	private void saveToDB(String id, String text, Map<String,Object> map){
		if(StringUtils.isBlank(text))
			return;

		String org = map.get(ORGANIZATION) == null ? "" : (String) map.get(ORGANIZATION);
		String per = map.get(PERSON) == null ? "" : (String) map.get(PERSON);
		JSONArray other = JSONArray.parseArray((String) map.get(O));
		String jsonString = other.size()==0 ? "" : other.toString();

		String value = "'"+id+ "','" +text+ "','" +org+ "','" +per+ "','" +jsonString+ "'";
		String sql = String.format(SAVE, targetTableName, value);

		logger.info("Save sql :{}",sql);

		jdbcTemplate.execute(sql);

	}

}
