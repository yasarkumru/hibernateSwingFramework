package pdfExcel;

import java.util.List;

public class ReportInfo {

	private String title;
	private List<String> headRows;
	private List<String> tailRows;

	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getHeadRows() {
		return headRows;
	}

	public void setHeadRows(List<String> headRows) {
		this.headRows = headRows;
	}

	public List<String> getTailRows() {
		return tailRows;
	}

	public void setTailRows(List<String> tailRows) {
		this.tailRows = tailRows;
	}

}
