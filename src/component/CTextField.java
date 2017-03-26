package component;

import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;

import org.jdesktop.xswingx.PromptSupport;

import utils.Utils;

public class CTextField extends JFormattedTextField {

	private static final long serialVersionUID = 1L;
	int limit = 255;

	public CTextField() {
		
	}
	
	public CTextField(Format format){
		super(format);
		setFocusLostBehavior(PERSIST);
	}
	
	@Override
	protected void processFocusEvent(FocusEvent e) {
		if(e.isTemporary())
			return;
		if (e.getID() == FocusEvent.FOCUS_LOST) { 
			try {
				commitEdit();
			} catch (ParseException e1) {
				setValue(null);
			}
        }  
        super.processFocusEvent(e); 
		
	}

	public CTextField(int limit) {
		this();
		this.limit = limit;
	}

	public CTextField(MaskFormatter f) {
		super(f);
		f.setPlaceholderCharacter('_');
	}

	public void setHint(String hint) {
		PromptSupport.setPrompt(hint, this);
	}

	private static final char DECIMAL_SEPERATOR = DecimalFormatSymbols.getInstance()
			.getDecimalSeparator();
	
	public void setAsNumberTextField() {
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
				
				if ((keyChar >= '0' && keyChar <= '9') || keyChar == '-'
						|| keyChar == DECIMAL_SEPERATOR) {
					return;
				}
				e.consume();
			}
		});
	}
	
	public void setAsOnlyNumberTextField() {
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
				
				if ((keyChar >= '0' && keyChar <= '9')) {
					return;
				}
				e.consume();
			}
		});
	}
	

	public double getDouble() throws ParseException {
		Number format2 = Utils.DECIMAL_FORMAT.parse(getText());
		String format3 = Utils.DECIMAL_FORMAT.format(format2);
		Number parse = Utils.DECIMAL_FORMAT.parse(format3);
		return parse.doubleValue();
	}

	@Override
	protected Document createDefaultModel() {
		return new LimitDocument();
	}

	private class LimitDocument extends PlainDocument {

		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {
			if (str == null)
				return;

			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}

	}
	

}
