package com.bezpredel.opentable.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* Date: 7/30/12
* Time: 6:58 PM
*/
public class Logger extends JPanel {
    private final JTextPane text = new JTextPane();
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");


    public Logger() {
        super(new BorderLayout());
        add(new JScrollPane(text));
        text.setEditable(false);
        text.setFont(new Font("SansSerif", 0, 10));

        MutableAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(set, 0);
        text.setParagraphAttributes(set, false);
    }

    public void log(String s, String name) {
        String[] ss = s.split("\n");
        String time = timestampFormat.format(new Date());
        for(int i=0; i<ss.length; i++) {
            String line = time + ": [" + name + "] " + ss[i] + "\n";
            try {
                Document document = text.getDocument();
                document.insertString(document.getLength(), line, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }



}
