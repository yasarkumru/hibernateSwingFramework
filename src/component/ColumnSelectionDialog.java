package component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import model.TableProperties;

public class ColumnSelectionDialog extends CDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private CTable columnTable;
	private CTable valuesTable;

	/**
	 * Create the dialog.
	 */
	private ColumnSelectionDialog(CTable valuesTable) {
		this.valuesTable = valuesTable;
		setTitle("RAPOR DÜZENLEME");
		ImageIcon icon = new ImageIcon(
				TablePanel.class.getResource("/icons/pdf_icon.png"));
		setIconImage(icon.getImage());
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				columnTable = new CTable((String) null, ColumnProperty.class,
						false, false);
				scrollPane.setViewportView(columnTable);

			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("TAMAM");
				okButton.setIcon(new ImageIcon(TablePanel.class
						.getResource("/icons/onay.png")));
				okButton.setFont(new Font("Tahoma", Font.BOLD, 12));
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okClicked();
					}
				});
				{
					selectAllButton = new CButton();
					selectAllButton.setIcon(new ImageIcon(TablePanel.class
							.getResource("/icons/temizle.png")));
					selectAllButton.setFont(new Font("Tahoma", Font.BOLD, 12));
					selectAllButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							selectAllClicked();
						}
					});
					selectAllButton.setText("TÜMÜNÜ BIRAK");
					buttonPane.add(selectAllButton);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("İPTAL");
				cancelButton.setIcon(new ImageIcon(TablePanel.class
						.getResource("/icons/geriButonu.png")));
				cancelButton.setFont(new Font("Tahoma", Font.BOLD, 12));
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelClicked();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}

		}
		initTable();
	}

	protected void selectAllClicked() {
		isAllSelected = !isAllSelected;
		for (ColumnProperty columnProperty : columnProperties) {
			columnProperty.setShowValue(isAllSelected);
		}

		columnTable.setObjects(columnProperties);
		if (isAllSelected) {
			selectAllButton.setText("TÜMÜNÜ BIRAK");
		} else {
			selectAllButton.setText("TÜMÜNÜ SEÇ");
		}

	}

	private List<ColumnProperty> columnProperties = new ArrayList<>();
	private boolean isAllSelected = true;

	private void initTable() {
		selection = 1;
		columnProperties = new ArrayList<>();
		for (int i = 0; i < valuesTable.getColumnCount(); i++) {
			ColumnProperty columnProperty = new ColumnProperty();
			columnProperty.setColumnName(valuesTable.getColumnName(i));
			columnProperty.setShowValue(Boolean.TRUE);
			columnProperties.add(columnProperty);
		}
		columnTable.setObjects(columnProperties);
		columnTable.getActionMap().put("enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				okClicked();
			}
		});
		columnTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");

	}

	public class ColumnProperty {

		private String columnName;
		private Boolean showValue;

		@TableProperties(name = "Kolon adı", columnOrder = 0)
		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		@TableProperties(name = "Gösterilsinmi?", editable = true, columnOrder = 1)
		public Boolean getShowValue() {
			return showValue;
		}

		public void setShowValue(Boolean showValue) {
			this.showValue = showValue;
		}

	}

	private static int selection;
	private CButton selectAllButton;

	public static Boolean[] showColumnSelectionDialog(CTable valuesTable) {
		ColumnSelectionDialog csd = new ColumnSelectionDialog(valuesTable);
		csd.setVisible(true);
		if (selection != 0)
			return null;
		List<?> objects = csd.getColumnTable().getObjects();
		Boolean[] columnArray = new Boolean[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			ColumnProperty c = (ColumnProperty) objects.get(i);
			columnArray[i] = c.getShowValue();
		}
		
		for (Boolean boolean1 : columnArray) {
			if(boolean1)
				return columnArray;
		}
		return null;
	}

	protected void okClicked() {
		selection = 0;
		dispose();
	}

	protected void cancelClicked() {
		selection = 1;
		dispose();
	}

	public CTable getValuesTable() {
		return valuesTable;
	}

	public CTable getColumnTable() {
		return columnTable;
	}

}
