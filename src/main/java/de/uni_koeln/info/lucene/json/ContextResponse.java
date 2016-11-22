package de.uni_koeln.info.lucene.json;

import java.util.Arrays;
import java.util.List;

public class ContextResponse implements Comparable<ContextResponse>{
	
	private String context;
	private String id;
	private List<String> keywords;
	private float score;
	private String source;
	private String created;
	

	public ContextResponse(String id, String context, float score, String keywords, String source, String created) {
		this.id = id;
		this.context = context;
		this.score = score;
		this.keywords = Arrays.asList(keywords.split(" "));
		this.source = source;
		this.created = created;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public String getCreated() {
		return created;
	}
	
	public String getSource() {
		return source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContextResponse other = (ContextResponse) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

	@Override
	public int compareTo(ContextResponse o) {
		return Float.compare(o.getScore(), score);
	}

	
}
