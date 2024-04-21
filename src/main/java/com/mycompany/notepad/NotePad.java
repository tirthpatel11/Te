package com.mycompany.notepad;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.undo.UndoManager;

public final class NotePad extends JFrame implements ActionListener, WindowListener, MouseListener {
    JTextArea jta = new JTextArea();
    File fnameContainer;
    UndoManager undoManager = new UndoManager();
    JPopupMenu popupMenu;

    public NotePad() {
        Font fnt = new Font("Arial", Font.PLAIN, 15);
        Container con = getContentPane();

        JMenuBar jmb = new JMenuBar();
        JMenu jmfile = new JMenu("File");
        JMenu jmedit = new JMenu("Edit");
        JMenu jmsearch = new JMenu("Search");

        JScrollPane sbrText = new JScrollPane(jta);
        sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jta.setFont(fnt);
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.getDocument().addUndoableEditListener(undoManager);
        jta.addMouseListener(this);

        con.setLayout(new BorderLayout());
        con.add(sbrText);

        createMenuItem(jmfile, "New");
        createMenuItem(jmfile, "Open");
        createMenuItem(jmfile, "Save");
        createMenuItem(jmfile, "Save As");
        jmfile.addSeparator();
        createMenuItem(jmfile, "Exit");

        createMenuItem(jmedit, "Undo");
        createMenuItem(jmedit, "Redo");
        jmfile.addSeparator();
        createMenuItem(jmedit, "Cut");
        createMenuItem(jmedit, "Copy");
        createMenuItem(jmedit, "Paste");

        createMenuItem(jmsearch, "Find");

        jmb.add(jmfile);
        jmb.add(jmedit);
        jmb.add(jmsearch);

        setJMenuBar(jmb);
        addWindowListener(this);
        setSize(500, 500);
        setTitle("Untitled.txt - Notepad");

        setVisible(true);
    }
    
    public void createMenuItem(JMenu jm, String txt) {
        JMenuItem jmi = new JMenuItem(txt);
        jmi.addActionListener(this);
        jm.add(jmi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();

        if (e.getActionCommand().equals("New")) {
            this.setTitle("Untitled.txt - Notepad");
            jta.setText("");
            fnameContainer = null;
        } else if (e.getActionCommand().equals("Open")) {
            int ret = jfc.showDialog(null, "Open");

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File fyl = jfc.getSelectedFile();
                    OpenFile(fyl.getAbsolutePath());
                    this.setTitle(fyl.getName() + " - Notepad");
                    fnameContainer = fyl;
                } catch (IOException ers) {
                    System.out.println(ers);
                }
            }

        } else if (e.getActionCommand().equals("Save")) {
            if (fnameContainer != null) {
                try {
                    SaveFile(fnameContainer.getAbsolutePath());
                    this.setTitle(fnameContainer.getName() + " - Notepad");
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            } else {
                saveAsOperation();
            }
        } else if (e.getActionCommand().equals("Save As")) {
            saveAsOperation();
        } else if (e.getActionCommand().equals("Exit")) {
            Exiting();
        } else if (e.getActionCommand().equals("Copy")) {
            jta.copy();
        } else if (e.getActionCommand().equals("Paste")) {
            jta.paste();
        } else if (e.getActionCommand().equals("Cut")) {
            jta.cut();
        } else if (e.getActionCommand().equals("Undo")) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } else if (e.getActionCommand().equals("Redo")) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } else if (e.getActionCommand().equals("Find")) {
            String input = JOptionPane.showInputDialog(this, "Enter text to find:");
            if (input != null && !input.isEmpty()) {
                highlightText(input);
            }
        }
    }

    private void saveAsOperation() {
        JFileChooser jfc = new JFileChooser();
        jfc.setSelectedFile(new File("Untitled.txt"));
        int ret = jfc.showSaveDialog(null);

        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                File fyl = jfc.getSelectedFile();
                SaveFile(fyl.getAbsolutePath());
                this.setTitle(fyl.getName() + " - Notepad");
                fnameContainer = fyl;
            } catch (IOException ers2) {
                System.out.println(ers2);
            }
        }
    }

    public void OpenFile(String fname) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
        String l;
        jta.setText("");

        while ((l = d.readLine()) != null) {
            jta.append(l + "\n");
        }

        d.close();
    }

    public void SaveFile(String fname) throws IOException {
        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(fname))) {
            o.writeBytes(jta.getText());
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        Exiting();
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    public void Exiting() {
        System.exit(0);
    }

    private void highlightText(String text) {
        String content = jta.getText();
        int index = content.indexOf(text);
        if (index >= 0) {
            jta.requestFocusInWindow();
            jta.select(index, index + text.length());
        } else {
            JOptionPane.showMessageDialog(this, "Text not found!");
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            String selectedText = jta.getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                createPopupMenu(true);
                popupMenu.show(jta, e.getX(), e.getY());
            } else {
                createPopupMenu(false);
                popupMenu.show(jta, e.getX(), e.getY());
            }
        }
    }

    private void createPopupMenu(boolean hasSelectedText) {
        popupMenu = new JPopupMenu();
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");

        cutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jta.cut();
            }
        });

        copyItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jta.copy();
            }
        });
        
        pasteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jta.paste();
            }
        });

        if (hasSelectedText) {
            popupMenu.add(cutItem);
            popupMenu.add(copyItem);
        }
        popupMenu.add(pasteItem);
    }
    

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        NotePad notePad = new NotePad();
    }
}