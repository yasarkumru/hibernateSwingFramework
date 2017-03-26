package component;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

public class CDateChooser extends JDateChooser {

	private static final long serialVersionUID = 1L;
	private static String format = "dd-MM-yyyy";
	private static String formatWithTime = "dd-MM-yyyy HH:mm";
	private boolean time;

	public CDateChooser(Date date) {
		this(date, false, false);
	}

	public CDateChooser(Date date, boolean time, boolean editable) {
		super(date);
		this.time = time;
		setPreferredSize(new Dimension(120, 20));
		JTextField dateEditor2 = (JTextField) getDateEditor();
		if (!editable)
			dateEditor2.setFocusable(false);
		if (time)
			setDateFormatString(formatWithTime);
		else
			setDateFormatString(format);
	}

	@Override
	public void setDate(Date arg0) {
		if (!time && arg0 != null) {
			Calendar instance = Calendar.getInstance();
			instance.setTime(arg0);
			instance.set(Calendar.HOUR_OF_DAY, 0);
			instance.set(Calendar.MINUTE, 0);
			instance.set(Calendar.SECOND, 0);

			arg0 = instance.getTime();
		}
		super.setDate(arg0);
	}

}
