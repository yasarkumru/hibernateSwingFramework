package utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class UiUtils {

	public static void setPanelTitle(JPanel panel, String title) {
		panel.setBorder(new TitledBorder(null, title,
				TitledBorder.LEADING, TitledBorder.TOP, new Font(null,
						Font.BOLD, 18), null));
	}
	
	public static void setContainerEnabled(Container container, boolean enabled) {
		Component[] components = container.getComponents();
		for (Component component : components) {
			if(component instanceof JLabel)
				continue;
			component.setEnabled(enabled);
			if (component instanceof Container)
				setContainerEnabled((Container) component, enabled);
		}
	}
}
