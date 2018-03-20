package crawler;

import com.opencsv.bean.CsvBindByName;

public class InputCSV {
	
	
	
	
	private String searchTerm;
	
	
	private String scanned;
	
	
	private String status;
	
	
	private String startTime;
	
	
	private String endTime;

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}



	public String getScanned() {
		return scanned;
	}

	public void setScanned(String scanned) {
		this.scanned = scanned;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "InputCSV [searchTerm=" + searchTerm + ", scanned=" + scanned
				+ ", status=" + status + ", startTime=" + startTime
				+ ", endTime=" + endTime + "]";
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}
