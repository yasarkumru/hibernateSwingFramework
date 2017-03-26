package utils;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class Utils {
	public static final NumberFormat DECIMAL_FORMAT = NumberFormat
			.getInstance();
	
	static{
		JComponent.setDefaultLocale(Locale.getDefault());
	}
	static {
		DECIMAL_FORMAT.setMaximumFractionDigits(3);
		DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
	}

	public static void showMessage(String message) {
		JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), message);
	}

	public static void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), message,
				"Hata!", JOptionPane.ERROR_MESSAGE);
	}

	public static int showConfirmMessage(String message) {
		int showConfirmDialog = JOptionPane.showConfirmDialog(
				JOptionPane.getRootFrame(), message, "?",
				JOptionPane.YES_NO_OPTION);
		return showConfirmDialog;
	}
	
	public static String showInputDialog(String message){
		String showInputDialog = JOptionPane.showInputDialog(message);
		return showInputDialog;
	}

}
