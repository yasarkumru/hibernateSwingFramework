package component;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class CTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;
	private int limit = 255;
	
	public CTextArea() {
	}
	
	public CTextArea(int limit){
		this();
		this.limit = limit;
	}
	
	@Override
	protected Document createDefaultModel() {
		return new LimitDocument();
	}
	
	private class LimitDocument extends PlainDocument {

        private static final long serialVersionUID = 1L;

		@Override
        public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
            if (str == null) return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }       

    }

}
