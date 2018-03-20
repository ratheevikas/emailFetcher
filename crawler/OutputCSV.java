package crawler;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class OutputCSV {

	
	private String searchTerm;
	
	
	private String url;
	
	
	private String email;
	
	
	private String timeFetched;

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "OutputCSV [searchTerm=" + searchTerm + ", url=" + url
				+ ", email=" + email + ", timeFetched=" + timeFetched + "]";
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTimeFetched() {
		return timeFetched;
	}

	public void setTimeFetched(String timeFetched) {
		this.timeFetched = timeFetched;
	}

}
