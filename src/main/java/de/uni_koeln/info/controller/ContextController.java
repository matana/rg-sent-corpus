package de.uni_koeln.info.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.uni_koeln.info.lucene.Searcher;
import de.uni_koeln.info.lucene.json.ContextResponse;

@RestController
public class ContextController {

	@Autowired
	private Searcher searcher;

	@RequestMapping(value = "/api/rg/sent/corpous/{key}", method = RequestMethod.GET)
	public List<ContextResponse> findKeyInContext(@PathVariable String key)
			throws IOException, ParseException, InvalidTokenOffsetsException {

		List<ContextResponse> results = searcher.searchAndHighlight(key);
		List<ContextResponse> arrayList = new ArrayList<>(results);
		Collections.sort(arrayList);

		return arrayList;
	}
	
	@RequestMapping(value = "/api/rg/corpous/size", method = RequestMethod.GET)
	public String indexSize() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("size", searcher.getIndexSize() + "");
		map.put("date", new Date());
		return new JSONObject(map).toString(1);
	}

	@RequestMapping(value = "/api/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String test(HttpServletRequest request) throws IOException {
		Enumeration<String> headerNames = request.getHeaderNames();
		Map<String, Object> map = new HashMap<>();
		while (headerNames.hasMoreElements()) {
			String nextElement = headerNames.nextElement();
			String header = request.getHeader(nextElement);
			map.put(nextElement, header);
		}
		map.put("date", new Date());
		return new JSONObject(map).toString(1);
	}

}


