package app;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main {
    static final String URL="jdbc:mysql://localhost:3306/assignment_db";
    static final String USER="root", PASS="";
    static DefaultTableModel model;

    public static void main(String[] a){
        try{FlatDarkLaf.setup();}catch(Exception e){}
        SwingUtilities.invokeLater(Main::ui);
    }
    static Connection c() throws SQLException { return DriverManager.getConnection(URL,USER,PASS); }

    static void ui(){
        JFrame f=new JFrame("Assignment Reminder");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900,560); f.setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel h=new JLabel("Assignment Reminder");
        h.setFont(h.getFont().deriveFont(Font.BOLD,26f));
        root.add(h,BorderLayout.NORTH);
        model=new DefaultTableModel(new String[]{"ID","Title","Course","Due","Priority","Done"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable t=new JTable(model); t.setRowHeight(30);
        root.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel form=new JPanel(new GridLayout(2,5,8,8));
        JTextField ti=new JTextField(), co=new JTextField(), du=new JTextField("2026-12-01 23:59");
        JComboBox<String> pr=new JComboBox<>(new String[]{"Low","Medium","High"});
        JButton add=new JButton("+ Add");
        form.add(new JLabel("Title")); form.add(new JLabel("Course"));
        form.add(new JLabel("Due")); form.add(new JLabel("Priority")); form.add(new JLabel(""));
        form.add(ti); form.add(co); form.add(du); form.add(pr); form.add(add);
        JPanel act=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton done=new JButton("Toggle Done"), del=new JButton("Delete");
        act.add(done); act.add(del);
        JPanel south=new JPanel(new BorderLayout(5,5));
        south.add(form,BorderLayout.CENTER); south.add(act,BorderLayout.SOUTH);
        root.add(south,BorderLayout.SOUTH);
        f.setContentPane(root); f.setVisible(true);

        add.addActionListener(e->{
            if(ti.getText().isBlank()){JOptionPane.showMessageDialog(f,"Title required");return;}
            try(Connection c=c(); PreparedStatement ps=c.prepareStatement(
                "INSERT INTO assignments(title,course,due_date,priority) VALUES(?,?,?,?)")){
                ps.setString(1,ti.getText().trim()); ps.setString(2,co.getText().trim());
                ps.setString(3,du.getText().trim()); ps.setString(4,(String)pr.getSelectedItem());
                ps.executeUpdate(); ti.setText(""); co.setText(""); refresh();
            }catch(Exception ex){JOptionPane.showMessageDialog(f,"DB: "+ex.getMessage());}
        });
        done.addActionListener(e->{
            int r=t.getSelectedRow(); if(r<0)return;
            try(Connection c=c(); PreparedStatement ps=c.prepareStatement("UPDATE assignments SET completed=NOT completed WHERE id=?")){
                ps.setInt(1,(int)model.getValueAt(r,0)); ps.executeUpdate(); refresh();
            }catch(Exception ex){JOptionPane.showMessageDialog(f,ex.getMessage());}
        });
        del.addActionListener(e->{
            int r=t.getSelectedRow(); if(r<0)return;
            try(Connection c=c(); PreparedStatement ps=c.prepareStatement("DELETE FROM assignments WHERE id=?")){
                ps.setInt(1,(int)model.getValueAt(r,0)); ps.executeUpdate(); refresh();
            }catch(Exception ex){JOptionPane.showMessageDialog(f,ex.getMessage());}
        });
        refresh();
    }
    static void refresh(){
        model.setRowCount(0);
        try(Connection c=c(); Statement s=c.createStatement();
            ResultSet rs=s.executeQuery("SELECT id,title,course,due_date,priority,completed FROM assignments ORDER BY due_date")){
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getTimestamp(4),rs.getString(5),rs.getBoolean(6)?"YES":""});
        }catch(Exception e){System.err.println(e.getMessage());}
    }
}
