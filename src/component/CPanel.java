package component;

import javax.swing.JPanel;

public class CPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private boolean authorized = true;
	
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled && authorized);
	}
}
