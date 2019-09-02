
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bacom
 */
class LimieJTextField extends PlainDocument {
    private int limit;
    
    public LimieJTextField(int i) {
        super();
        this.limit = i;
    }
    
    public void insertString(int c, String str, AttributeSet attr) throws BadLocationException
    {
        if (str == null) return;
        if((getLength()+str.length()) <= limit)
        {
            super.insertString(c, str, attr);
        }
    }
}
