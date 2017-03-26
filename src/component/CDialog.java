package component;

import javax.swing.JDialog;

public class CDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public CDialog() {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
	}
}