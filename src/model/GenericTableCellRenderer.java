package model;

import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import utils.Utils;

public class GenericTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Color LIGHT_GRAY = new Color(235, 235, 235);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
			"dd MMMM yyyy EEEE HH:mm");
	private static BooleanRenderer booleanRenderer = new BooleanRenderer();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (value instanceof Boolean) {
			return booleanRenderer.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
		if (value instanceof Double) {
			value = Utils.DECIMAL_FORMAT.format(value);
		}
		Component cell = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);

		if (row % 2 == 1 && !isSelected) {
			cell.setBackground(LIGHT_GRAY);
		} else if (!isSelected) {
			cell.setBackground(Color.WHITE);
		}

		if (value instanceof Color) {
			JLabel label = (JLabel) cell;
			cell.setBackground((Color) value);
			label.setText("");
			return label;
		}

		if (value instanceof JLabel) {
			JLabel label = (JLabel) cell;
			JLabel val = (JLabel) value;
			label.setIcon(val.getIcon());
			label.setText(val.getText());
			return label;
		}

		if (value instanceof Date) {
			JLabel label = (JLabel) cell;
			String format = SIMPLE_DATE_FORMAT.format((Date) value);
			label.setText(format);
			return label;
		}
		return cell;
	}

	static class BooleanRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

		public BooleanRenderer() {
			super();
			setHorizontalAlignment(JLabel.CENTER);
			setBorderPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			if (row % 2 == 1 && !isSelected) {
				setBackground(LIGHT_GRAY);
			} else if (!isSelected) {
				setBackground(Color.WHITE);
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));

			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			} else {
				setBorder(noFocusBorder);
			}

			return this;
		}
	}

}
