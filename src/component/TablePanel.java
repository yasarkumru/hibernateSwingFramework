package component;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import pdfExcel.ExcelManager;
import pdfExcel.ReportInfo;
import utils.Entity;
import utils.Utils;
import model.GenericTableModel;
import net.miginfocom.swing.MigLayout;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.awt.FlowLayout;

public class TablePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private CTextField textField;
	private CTable table;
	private TableRowSorter<GenericTableModel> sorter;
	private static final String TABLE_PANEL = "table";
	private static final String ANIMATION_PANEL = "animation";
	private JPanel cardPanel;
	private JScrollPane scrollPane;
	private JPanel animationPanel;
	private JPanel bottomPanel;
	private CButton raporButton;
	private ReportInfo reportInfo;

	public void setEntityAndColumnName(Entity entity, String columnName) {
		table.setEntityAndColumnName(entity, columnName);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public TablePanel(String title, Class<?> clazz) {
		this(title, clazz, true, true);
	}

	public TablePanel(String title, Class<?> clazz, boolean updatable) {
		this(title, clazz, updatable, true);

	}

	public CTextField getSearchTextField() {
		return textField;
	}

	public CButton getExcelRaporButton() {
		return btnExcelRapor;
	}

	public TablePanel(String title, Class<?> clazz, boolean updateable, boolean autoResizableColumns) {

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("",
				"[30.00px,leading][119.00px,leading][grow,fill][30][30,trailing]", "[35px]"));

		JLabel lblAra = new JLabel("");
		panel.add(lblAra, "cell 0 0,grow");
		lblAra.setIcon(new ImageIcon(TablePanel.class.getResource("/icons/search.png")));

		textField = new CTextField();
		textField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		textField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(textField, "cell 1 0,alignx center,aligny center");
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				searchTextChanged();
			}
		});
		textField.setColumns(10);

		textField.setHint("Ara");
		setLayout(new BorderLayout(0, 0));

		cardPanel = new JPanel();
		add(cardPanel, BorderLayout.CENTER);
		cardPanel.setLayout(new CardLayout(0, 0));

		scrollPane = new JScrollPane();
		cardPanel.add(scrollPane, TablePanel.TABLE_PANEL);

		table = new CTable(title, clazz, updateable, autoResizableColumns);
		scrollPane.setViewportView(table);

		animationPanel = new JPanel();
		cardPanel.add(animationPanel, TablePanel.ANIMATION_PANEL);
		animationPanel.setLayout(new BorderLayout(0, 0));

		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setIcon(new ImageIcon(TablePanel.class.getResource("/icons/loading.gif")));
		animationPanel.add(label, BorderLayout.CENTER);
		add(panel, BorderLayout.NORTH);

		btnExcelRapor = new CButton();
		btnExcelRapor.setIcon(new ImageIcon(TablePanel.class.getResource("/icons/excel-icon.png")));
		btnExcelRapor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				excelRaporClicked();

			}
		});
		panel.add(btnExcelRapor, "cell 3 0,grow");

		raporButton = new CButton();
		raporButton.setHorizontalAlignment(SwingConstants.TRAILING);
		raporButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(raporButton, "cell 4 0,grow");
		raporButton.setIcon(new ImageIcon(TablePanel.class.getResource("/icons/pdf_icon.png")));
		raporButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					raporClicked();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (DocumentException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		initTable();

		setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), title,
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(bottomPanel, BorderLayout.SOUTH);

		lblGirdiSays = new JLabel("Gösterilen girdi sayısı:");
		bottomPanel.add(lblGirdiSays);

		countLabel = new JLabel("0");
		bottomPanel.add(countLabel);
	}

	protected void excelRaporClicked() {
		try {
			ExcelManager.getInstance();
			ExcelManager.insertObjects(table);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setReportInfo(ReportInfo reportInfo) {
		this.reportInfo = reportInfo;
	}

	protected void raporClicked() throws DocumentException, IOException {

		Boolean[] columnSelectionBooleanArray = ColumnSelectionDialog
				.showColumnSelectionDialog(table);

		if (columnSelectionBooleanArray == null) {
			Utils.showErrorMessage("Rapor için en az 1 kolon seçili olmalıdır.");
			return;
		}

		File tempFile = File.createTempFile("pdf", ".pdf");
		tempFile.deleteOnExit();
		int tableColumnSize = tableColumnSize(columnSelectionBooleanArray);
		Rectangle a4 = new Rectangle(PageSize.A4);

		Document document = new Document(a4);
		document.setMargins(10, 10, 10, 10);

		if (tableColumnSize > 6)
			document.setPageSize(a4.rotate());

		PdfWriter.getInstance(document, new FileOutputStream(tempFile));

		PdfPTable pdfTable = createPDFTable(columnSelectionBooleanArray);

		if (pdfTable == null) {
			return;
		}
		pdfTable.setWidths(getColumnWidths(columnSelectionBooleanArray, table));

		document.open();

		if (reportInfo != null) {
			if (reportInfo.getTitle() != null) {
				Paragraph title = new Paragraph(reportInfo.getTitle(), titleFont);
				title.setAlignment(Element.ALIGN_CENTER);
				document.add(title);
				addEmptyParagraph(document);
			}

			if (reportInfo.getHeadRows() != null) {
				List<String> headRows = reportInfo.getHeadRows();
				for (String row : headRows) {
					Paragraph paragraph = new Paragraph(new Phrase(row));

					document.add(paragraph);
				}
				addEmptyParagraph(document);
			}
		}

		document.add(pdfTable);
		addEmptyParagraph(document);

		if (reportInfo != null) {
			if (reportInfo.getTailRows() != null) {
				List<String> tailRows = reportInfo.getTailRows();
				for (String row : tailRows) {
					Paragraph paragraph = new Paragraph(new Phrase(row));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					document.add(paragraph);
				}
				addEmptyParagraph(document);
			}
		}

		document.add(new LineSeparator());
		Paragraph paragraph = new Paragraph("ALTISOFT", titleFont);
		paragraph.add(new Phrase(" /altisoft.com.tr/", tableFont));
		paragraph.setAlignment(Element.ALIGN_CENTER);
		document.add(paragraph);
		addEmptyParagraph(document);
		document.add(new LineSeparator());
		Paragraph paragraph2 = new Paragraph(new Phrase("info@altisoft.com.tr / 0(312) 231 25 84"));
		paragraph2.setAlignment(Element.ALIGN_CENTER);
		document.add(paragraph2);
		addEmptyParagraph(document);
		document.add(new LineSeparator());
		document.close();

		if (Desktop.isDesktopSupported())
			try {
				Desktop.getDesktop().open(tempFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	private static void addEmptyParagraph(Document document) throws DocumentException {
		document.add(new Paragraph(new Phrase(" ")));
	}

	private static int[] getColumnWidths(Boolean[] showColumnSelectionDialog, CTable table2) {
		int[] columnWidths = new int[tableColumnSize(showColumnSelectionDialog)];
		int counter = 0;
		for (int i = 0; i < showColumnSelectionDialog.length; i++) {
			if (!showColumnSelectionDialog[i])
				continue;
			int width2 = table2.getColumnModel().getColumn(counter).getWidth();
			columnWidths[counter] = width2;
			counter++;
		}
		return columnWidths;
	}

	private static int tableColumnSize(Boolean[] columnArray) {
		int count = 0;
		for (boolean b : columnArray) {
			if (b)
				count++;
		}
		return count;
	}

	private static final BaseColor LIGHT_GRAY = new BaseColor(new Color(235, 235, 235).getRGB());

	private static BaseFont baseFont = null;

	static {
		try {
			baseFont = BaseFont.createFont("Helvetica", "CP1254", BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Font tableFont = new Font(baseFont, 12, Font.NORMAL);
	private static Font titleFont = new Font(baseFont, 18, Font.BOLD);
	private CButton btnExcelRapor;
	private JLabel lblGirdiSays;
	private JLabel countLabel;

	private PdfPTable createPDFTable(Boolean[] showColumnSelectionDialog) {

		int columnCount = tableColumnSize(showColumnSelectionDialog);
		if (columnCount == 0)
			return null;
		PdfPTable pdfTable = new PdfPTable(columnCount);
		pdfTable.setWidthPercentage(100);

		for (int i = 0; i < table.getColumnCount(); i++) {
			if (!showColumnSelectionDialog[i])
				continue;
			PdfPCell header = new PdfPCell(new Phrase(table.getColumnName(i), tableFont));
			pdfTable.addCell(header);
		}

		for (int i = 0; i < table.getRowCount(); i++) {
			for (int j = 0; j < table.getColumnCount(); j++) {
				if (!showColumnSelectionDialog[j])
					continue;
				Object valueAt = table.getValueAt(i, j);

				PdfPCell cell;
				if (valueAt == null) {
					cell = new PdfPCell(new Phrase());
					if (i % 2 == 0)
						cell.setBackgroundColor(LIGHT_GRAY);
					pdfTable.addCell(cell);
					continue;
				}
				if (valueAt instanceof Color) {
					cell = new PdfPCell(new Phrase());
					cell.setBackgroundColor(new BaseColor(((Color) valueAt).getRGB()));
					pdfTable.addCell(cell);
					continue;
				}

				if (valueAt instanceof Double) {
					cell = new PdfPCell(new Phrase(Utils.DECIMAL_FORMAT.format(valueAt)));
					if (i % 2 == 0)
						cell.setBackgroundColor(LIGHT_GRAY);
					pdfTable.addCell(cell);
					continue;
				}

				cell = new PdfPCell(new Phrase(valueAt.toString(), tableFont));
				if (i % 2 == 0)
					cell.setBackgroundColor(LIGHT_GRAY);
				pdfTable.addCell(cell);
			}
		}

		return pdfTable;
	}

	public void setClazz(Class<?> clazz) {
		table.setClazz(clazz);
	}

	private void initTable() {
		GenericTableModel model = (GenericTableModel) table.getModel();
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		table.setTablePanel(this);

		table.getActionMap().put("controlf", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				textField.requestFocus();
				textField.selectAll();
			}
		});

		table.getInputMap().put(KeyStroke.getKeyStroke("control F"), "controlf");

		table.getActionMap().put("controlr", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					raporClicked();
				} catch (DocumentException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		table.getInputMap().put(KeyStroke.getKeyStroke("control R"), "controlr");

		table.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				tableQueried();

			}
		});

	}

	protected void tableQueried() {
		countLabel.setText(Integer.toString(table.getRowCount()));
	}

	public List<Object> getQueriedResults() {
		int rowCount = table.getRowCount();
		GenericTableModel model = (GenericTableModel) table.getModel();
		List<Object> results = new ArrayList<>(rowCount);
		for (int i = 0; i < rowCount; i++) {
			Object valueAt = model.getValueAt(table.convertRowIndexToModel(i),
					GenericTableModel.OBJECT_COLUMN);
			results.add(valueAt);
		}
		return results;
	}

	protected void searchTextChanged() {
		table.getSelectionModel().clearSelection();
		String query = textField.getText();

		String[] queries = query.split(" ");

		if (query.length() == 0) {
			sorter.setRowFilter(null);
			tableQueried();
			return;
		}
		List<RowFilter<Object, Object>> filters = new LinkedList<>();
		for (String q : queries) {
			filters.add(RowFilter.regexFilter("(?i)" + q));
		}

		try {
			sorter.setRowFilter(RowFilter.andFilter(filters));
			tableQueried();
		} catch (Exception e1) {
			e1.printStackTrace();
			textField.setText("");
			Utils.showMessage("Yazilan regex hatali");
			return;
		}
	}

	protected void showLoading(boolean show) {
		CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
		if (show) {
			cardLayout.show(cardPanel, ANIMATION_PANEL);
		} else {
			cardLayout.show(cardPanel, TABLE_PANEL);
		}
	}

	public void setObjects(List<?> objects) {
		table.setObjects(objects);
	}

	public void refresh() {
		table.refresh();
	}

	public Object getSelectedObject() {

		return table.getSelectedObject();
	}

	public List<Object> getSelectedObjects() {
		return table.getSelectedObjects();
	}

	public CTable getTable() {
		return table;
	}

	public void clearSelection() {
		textField.setText("");
		searchTextChanged();
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public CButton getRaporButton() {
		return raporButton;
	}
}
