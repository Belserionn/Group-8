package votingsystem;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
 

public class Forms {
 
    
    public static class Result {
        public final int button;
        public final String[] values;
 
        Result(int button, String[] values) {
            this.button = button;
            this.values = values;
        }
    }
 
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    private static final int FIELD_WIDTH = 420;
 
    
 
    private static Border thick(int px) {
        return BorderFactory.createLineBorder(Color.BLACK, px);
    }
 
    private static void paintHint(JComponent c, Graphics g, String hint, boolean empty) {
        if (!empty) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.setFont(c.getFont());
        Insets in = c.getInsets();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, in.left, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
 
    private static class HintField extends JTextField {
        private final String hint;
 
        HintField(String hint) {
            this.hint = hint;
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintHint(this, g, hint, getText().isEmpty());
        }
    }
 
    private static class HintPassword extends JPasswordField {
        private final String hint;
 
        HintPassword(String hint) {
            this.hint = hint;
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintHint(this, g, hint, getPassword().length == 0);
        }
    }
 
    private static JTextComponent field(String hint, boolean secret) {
        JTextComponent f = secret ? new HintPassword(hint) : new HintField(hint);
        f.setFont(FONT);
        f.setBorder(BorderFactory.createCompoundBorder(thick(3),
            BorderFactory.createEmptyBorder(0, 18, 0, 18)));
        f.setPreferredSize(new Dimension(FIELD_WIDTH, 52));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        return f;
    }
 
    
    private static JComponent withCaption(JTextComponent f, String caption) {
        if (caption == null) {
            return f;
        }
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(caption);
        l.setFont(FONT.deriveFont(13f));
        p.add(l, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }
 
    private static JButton button(String text, int width, int height) {
        JButton b = new JButton(text);
        b.setFont(FONT.deriveFont(16f));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setBorder(thick(3));
        b.setPreferredSize(new Dimension(width, height));
        return b;
    }
 
    
    private static JComponent headerBox(String text) {
        JTextArea a = new JTextArea(text);
        a.setEditable(false);
        a.setFocusable(false);
        a.setFont(MONO);
        a.setBackground(Color.WHITE);
        a.setBorder(BorderFactory.createCompoundBorder(thick(3),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        Dimension pref = a.getPreferredSize();
        a.setPreferredSize(new Dimension(Math.max(300, pref.width), pref.height));
        a.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        return a;
    }
 
    private static JPanel newInner(int vertical, int horizontal) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Color.WHITE);
        inner.setBorder(BorderFactory.createEmptyBorder(vertical, horizontal,
            vertical, horizontal));
        return inner;
    }
 
    private static Component gap(int px) {
        return Box.createRigidArea(new Dimension(0, px));
    }
 
    private static JDialog dialog(String title, JPanel inner) {
        JDialog d = new JDialog((Frame) null, title, true);
        JPanel frame = new JPanel(new BorderLayout());
        frame.setBackground(Color.WHITE);
        frame.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(14, 14, 14, 14), thick(4)));
        frame.add(inner, BorderLayout.CENTER);
        d.setContentPane(frame);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setResizable(false);
        return d;
    }
 
    private static void show(JDialog d) {
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);  
    }
 
   
    private static JPanel buttonRow(final JDialog d, final int[] clicked, String[] buttons) {
        JPanel row = new JPanel(buttons.length == 1
            ? new FlowLayout(FlowLayout.RIGHT, 0, 0)
            : new GridLayout(1, buttons.length, 22, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            JButton b = button(buttons[i], buttons.length == 1 ? 150 : 120, 44);
            b.addActionListener(e -> {
                clicked[0] = index;
                d.dispose();
            });
            row.add(b);
        }
        d.getRootPane().setDefaultButton((JButton) row.getComponent(0));
        return row;
    }
 
   
    public static Result window(String title, String[] hints, boolean[] secret,
                                String... buttons) {
        return window(title, null, hints, secret, null, buttons);
    }
 
 
    public static Result window(String title, String header, String[] hints,
                                boolean[] secret, String[] initial, String... buttons) {
        final int[] clicked = {-1};
        final JTextComponent[] fields = new JTextComponent[hints.length];
        JPanel inner = newInner(22, 26);
        final JDialog d = dialog(title, inner);
 
        if (header != null && !header.isEmpty()) {
            inner.add(headerBox(header));
            inner.add(gap(18));
        }
        for (int i = 0; i < hints.length; i++) {
            fields[i] = field(hints[i], secret[i]);
            boolean hasValue = initial != null && initial[i] != null;
            if (hasValue) {
                fields[i].setText(initial[i]);
            }
            inner.add(withCaption(fields[i], hasValue ? hints[i] : null));
            inner.add(gap(18));
        }
        inner.add(buttonRow(d, clicked, buttons));
        show(d);
 
        if (clicked[0] < 0) {
            return null;
        }
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = fields[i] instanceof JPasswordField
                ? new String(((JPasswordField) fields[i]).getPassword())
                : fields[i].getText();
        }
        return new Result(clicked[0], values);
    }
 
    public static int menu(String title, String header, String... labels) {
        final int[] clicked = {-1};
        JPanel inner = newInner(28, 44);
        final JDialog d = dialog(title, inner);
 
        if (header != null && !header.isEmpty()) {
            inner.add(headerBox(header));
            inner.add(gap(24));
        }
        int cols = labels.length > 4 ? 2 : 1;
        JPanel grid = new JPanel(new GridLayout(0, cols, 22, 22));
        grid.setBackground(Color.WHITE);
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JButton b = button(labels[i], 300, 56);
            b.addActionListener(e -> {
                clicked[0] = index;
                d.dispose();
            });
            grid.add(b);
        }
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);
        grid.setMaximumSize(grid.getPreferredSize());
        inner.add(grid);
        show(d);
        return clicked[0];
    }
 
    
    public static int choose(String title, String header, String[] options) {
        final int[] clicked = {-1};
        JPanel inner = newInner(22, 26);
        final JDialog d = dialog(title, inner);
 
        if (header != null && !header.isEmpty()) {
            inner.add(headerBox(header));
            inner.add(gap(18));
        }
        JComboBox<String> box = new JComboBox<>(options);
        box.setFont(FONT.deriveFont(15f));
        JPanel boxed = new JPanel(new BorderLayout());
        boxed.setBackground(Color.WHITE);
        boxed.setBorder(thick(3));
        boxed.add(box, BorderLayout.CENTER);
        boxed.setPreferredSize(new Dimension(
            Math.max(FIELD_WIDTH, box.getPreferredSize().width + 6), 52));
        boxed.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        boxed.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(boxed);
        inner.add(gap(18));
        inner.add(buttonRow(d, clicked, new String[] {"ok", "cancel"}));
        show(d);
        return clicked[0] == 0 ? box.getSelectedIndex() : -1;
    }
 
    
    public static void message(String title, String text) {
        final int[] clicked = {-1};
        JPanel inner = newInner(22, 26);
        final JDialog d = dialog(title, inner);
        inner.add(headerBox(text));
        inner.add(gap(18));
        inner.add(buttonRow(d, clicked, new String[] {"ok"}));
        show(d);
    }
 
    
    public static boolean confirm(String title, String text) {
        final int[] clicked = {-1};
        JPanel inner = newInner(22, 26);
        final JDialog d = dialog(title, inner);
        inner.add(headerBox(text));
        inner.add(gap(18));
        inner.add(buttonRow(d, clicked, new String[] {"yes", "no"}));
        show(d);
        return clicked[0] == 0;
    }
 
   
    public static void showText(String title, String text) {
        final int[] clicked = {-1};
        JPanel inner = newInner(22, 26);
        final JDialog d = dialog(title, inner);
        JTextArea area = new JTextArea(text, 18, 58);
        area.setEditable(false);
        area.setFont(MONO);
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(thick(3));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(scroll);
        inner.add(gap(18));
        inner.add(buttonRow(d, clicked, new String[] {"close"}));
        show(d);
    }
}
 