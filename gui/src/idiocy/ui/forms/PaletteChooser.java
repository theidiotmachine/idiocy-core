package idiocy.ui.forms;

import idiocy.ui.GlobalUISettings;
import idiocy.ui.JavaRepaintCallback;
import idiocy.ui.palette.Palette;
import idiocy.ui.palette.PaletteStore;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;

public class PaletteChooser extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList list1;
    private JavaRepaintCallback javaRepaintCallback;
    private Palette oldPalette;

    private DefaultListModel defaultListModel;

    public PaletteChooser(JFrame f, JavaRepaintCallback paint) {
        super(f, "Choose Palette");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                String s = (String) list1.getSelectedValue();
                Palette p = PaletteStore.getPaletteByName(s);
                GlobalUISettings.setPalette(p);
                javaRepaintCallback.go();
            }
        });

        javaRepaintCallback = paint;
        oldPalette = GlobalUISettings.palette();
    }

    private void onOK() {
        // add your code here
        //String s = (String) list1.getSelectedValue();
        //Palette p = PaletteStore.getPaletteByName(s);
        //GlobalUISettings.setPalette(p);
        dispose();
        javaRepaintCallback.go();
    }

    private void onCancel() {
        // add your code here if necessary
        GlobalUISettings.setPalette(oldPalette );

        dispose();
        javaRepaintCallback.go();
    }

    private void createUIComponents() {
        list1 = new JList();
        defaultListModel = new DefaultListModel();
        list1.setModel(defaultListModel);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        String[] names = PaletteStore.getPaletteNames();
        for (int i = 0; i < names.length; ++i){
            String name = names[i];
            defaultListModel.addElement(name);
            if(name.equals(GlobalUISettings.palette().name())){
                list1.setSelectedIndex(i);
            }
        }

    }
}
