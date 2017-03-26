package pdfExcel;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import component.CTable;

public class ExcelManager {
	private static ExcelManager instance;

	public static ExcelManager getInstance() {
		if (instance == null)
			instance = new ExcelManager();
		return instance;
	}

	private ExcelManager() {
	}

	public static void insertObjects(CTable table) throws IllegalArgumentException,
			IllegalAccessException, IOException {

		Workbook workbook = new XSSFWorkbook();
		int columnCount = table.getColumnCount();

		Sheet sheet = workbook.createSheet(table.getClazz().getSimpleName());

		int rowNum = 0;
		Row row = sheet.createRow(rowNum++);
		for (int i = 0; i < columnCount; i++) {
			Cell createCell = row.createCell(i);
			createCell
					.setCellValue(table.getColumnModel().getColumn(i).getHeaderValue().toString());
		}

		for (int i = 0; i < table.getRowCount(); i++) {
			Row row2 = sheet.createRow(rowNum++);

			for (int j = 0; j < table.getColumnCount(); j++) {
				Cell createCell = row2.createCell(j);
				Object valueAt = table.getValueAt(i, j);
				if (valueAt == null)
					createCell.setCellValue("");
				else {
					setCellType(createCell, valueAt);
					createCell.setCellValue(valueAt.toString());
				}

			}
		}

		File tempFile = File.createTempFile("xlsx", ".xlsx");
		tempFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tempFile);
		workbook.write(fos);
		fos.close();
		if (Desktop.isDesktopSupported())
			try {
				Desktop.getDesktop().open(tempFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void setCellType(Cell createCell, Object valueAt) {
		if (valueAt instanceof Number) {
			createCell.setCellType(Cell.CELL_TYPE_NUMERIC);
		} else {
			createCell.setCellType(Cell.CELL_TYPE_STRING);
		}
	}

}
