package PM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.ZoneId;


 // @author gigatore
 
public class manageRiservation extends javax.swing.JFrame {

    /**
     * Creates new form checkOut
     */
    private javax.swing.JButton fattureButton;

    public manageRiservation() {
        initComponents();
        codTextField.setText("");
        codTextField.setEditable(true);
        Connect();
        loadTable();
        configureForRole();

        fattureButton = new javax.swing.JButton("Fatture");
        fattureButton.setBackground(new java.awt.Color(0, 204, 153));
        fattureButton.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 14));
        fattureButton.setForeground(new java.awt.Color(153, 0, 0));
        fattureButton.addActionListener(e -> new gestioneFatture().setVisible(true));
        getContentPane().add(fattureButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 502, 114, 39));
        getContentPane().setComponentZOrder(fattureButton, 0);
    }
    
    Connection con;
    PreparedStatement pst;
    //DefaultTableModel d;
    
    // per convertire una stringa in int
    public int convInt(String num){
        int ris;
        ris = Integer.parseInt(num.substring(1));  
        return ris;   
    }
    
    public String convAlfa(int num){
        String ris;
        ris = "R0" + String.format("%03d",num);
        return ris;
    }

    public String convAlfaR(int num){
        String ris;
        ris = "P0" + String.format("%03d",num);
        return ris;
    }
    
    public void date() {
        
        SimpleDateFormat  dat = new SimpleDateFormat("yyyy-MM-dd ");
        Date d = new Date();
        codTextField.setText(dat.format(d));       
        codTextField.setEditable(false);
    
    }
    
    public void Connect()
    {
        try{
            con = DBConfig.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(manageRoom.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void cancel() {
        idTextField.setText("");
        userTextField.setText("");
        rnTextField.setText("");
        cidTextField.setText("");
        committenteTextField.setText("");
        ppdTextField.setText("");
        noteTextField.setText("");
        nodTextField.setText("");
        codTextField.setText("");
        totalTextField.setText("");
    }
    
    private void configureForRole() {
        if ("U".equalsIgnoreCase(Session.roleType)) {
            updateButton.setVisible(false);
            cidTextField.setEditable(false);
            codTextField.setEditable(false);
            noteTextField.setEditable(false);
        }
    }

    public int calculDate(String f1, String f2) {
        int days = 0;
        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd"); 
        
        try{ 
            
            Date d1=simple.parse(f1); // meno recente
            Date d2=simple.parse(f2); // piu  recente
                
            long diff = d2.getTime() - d1.getTime();
            days=(int)(diff/(1000*24*60*60));
            
            if(days == 0)
                days++;
            
           
        }catch(Exception ex){
           ex.getStackTrace();
        }
        return days; 
    }
    
    public void dataFields() {
        if (idTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Inserire l'ID prenotazione");
            return;
        }
        try {
            int id = convInt(idTextField.getText());
            String sql = "SELECT U.nome, Re.committente_id, R.room_id, R.prezzo, Re.check_in, Re.check_out, Re.status, Re.note, Re.giorni " +
                "FROM Reservation Re " +
                "INNER JOIN User U ON U.user_counter = Re.user_id " +
                "INNER JOIN Room R ON R.room_id = Re.room_id " +
                "WHERE Re.reservation_id = ?";
            if ("U".equalsIgnoreCase(Session.roleType)) {
                pst = con.prepareStatement(sql + " AND Re.user_id = ?");
                pst.setInt(1, id);
                pst.setString(2, Session.userCounter);
            } else {
                pst = con.prepareStatement(sql);
                pst.setInt(1, id);
            }
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String ci = rs.getString("check_in");
                String co = rs.getString("check_out");
                int rni = rs.getInt("R.room_id");
                double prezzo = rs.getDouble("prezzo");
                int giorni = rs.getInt("giorni");
                if (giorni == 0) giorni = calculDate(ci, co != null ? co : new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                double totale = prezzo * giorni;

                userTextField.setText(rs.getString("U.nome"));
                committenteTextField.setText(String.valueOf(rs.getInt("committente_id")));
                rnTextField.setText(convAlfa(rni));
                cidTextField.setText(ci);
                codTextField.setText(co != null ? co : "");
                ppdTextField.setText(rs.getString("status"));
                noteTextField.setText(rs.getString("note") != null ? rs.getString("note") : "");
                nodTextField.setText(String.valueOf(giorni));
                totalTextField.setText(String.valueOf(totale));
            } else {
                JOptionPane.showMessageDialog(this, "Prenotazione non trovata");
                idTextField.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore ricerca prenotazione");
            ex.printStackTrace();
        }
    }

    public void conferm() {
        if ("U".equalsIgnoreCase(Session.roleType)) return;
        if (idTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Cercare prima una prenotazione per ID");
            return;
        }
        try {
            int id = convInt(idTextField.getText());
            int giorni = calculDate(cidTextField.getText(), codTextField.getText());

            pst = con.prepareStatement("SELECT user_id, check_in, check_out FROM Reservation WHERE reservation_id = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            String oldCheckIn = "", oldCheckOut = "", userId = "";
            if (rs.next()) {
                oldCheckIn = rs.getString("check_in");
                oldCheckOut = rs.getString("check_out") != null ? rs.getString("check_out") : "";
                userId = rs.getString("user_id");
            }

            con.setAutoCommit(false);

            pst = con.prepareStatement("UPDATE Reservation SET check_in=?, check_out=?, note=?, giorni=? WHERE reservation_id=?");
            pst.setString(1, cidTextField.getText());
            pst.setString(2, codTextField.getText());
            pst.setString(3, noteTextField.getText());
            pst.setInt(4, giorni);
            pst.setInt(5, id);
            pst.executeUpdate();

            pst = con.prepareStatement(
                "INSERT INTO StoricoPrenotazioni(reservation_id, user_id, check_in_precedente, check_out_precedente, nuovo_check_in, nuovo_check_out, data_modifica) " +
                "VALUES(?,?,?,?,?,?,?)");
            pst.setInt(1, id);
            pst.setString(2, userId);
            pst.setString(3, oldCheckIn);
            pst.setString(4, oldCheckOut);
            pst.setString(5, cidTextField.getText());
            pst.setString(6, codTextField.getText());
            pst.setString(7, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            pst.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Prenotazione aggiornata con successo");
            loadTable();
            cancel();
        } catch (SQLException ex) {
            try { con.rollback(); } catch (SQLException e) { }
            JOptionPane.showMessageDialog(this, "Errore aggiornamento prenotazione");
            ex.printStackTrace();
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { }
        }
    }

    public void loadTable() {
        try {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            String sql = "SELECT Re.reservation_id, U.nome, Re.committente_id, Re.check_in, Re.check_out, Re.status, R.room_id, Re.note, Re.giorni, (R.prezzo * COALESCE(Re.giorni, 1)) " +
                "FROM Reservation Re " +
                "INNER JOIN User U ON U.user_counter = Re.user_id " +
                "INNER JOIN Room R ON R.room_id = Re.room_id";
            if ("U".equalsIgnoreCase(Session.roleType)) {
                pst = con.prepareStatement(sql + " WHERE Re.user_id = ?");
                pst.setString(1, Session.userCounter);
            } else {
                pst = con.prepareStatement(sql);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    convAlfaR(rs.getInt(1)),
                    rs.getString(2),
                    rs.getInt(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    convAlfa(rs.getInt(7)),
                    rs.getString(8),
                    rs.getInt(9),
                    rs.getDouble(10)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento prenotazioni");
        }
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        logoLabel = new javax.swing.JLabel();
        idLabel = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        rnLabel = new javax.swing.JLabel();
        rnTextField = new javax.swing.JTextField();
        cidLabel = new javax.swing.JLabel();
        cidTextField = new javax.swing.JTextField();
        committenteLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        committenteTextField = new javax.swing.JTextField();
        ppdTextField = new javax.swing.JTextField();
        noteLabel = new javax.swing.JLabel();
        noteTextField = new javax.swing.JTextField();
        nodLabel = new javax.swing.JLabel();
        nodTextField = new javax.swing.JTextField();
        codLabel = new javax.swing.JLabel();
        codTextField = new javax.swing.JTextField();
        totalLabel = new javax.swing.JLabel();
        totalTextField = new javax.swing.JTextField();
        updateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        bgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png"))); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 0, -1, -1));

        logoLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/manage.png"))); // NOI18N
        logoLabel.setText("Manage Riservation");
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 210, -1));

        idLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        idLabel.setText("ID Allote");
        getContentPane().add(idLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(294, 66, 100, 24));

        idTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(idTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(392, 62, 220, 34));

        searchButton.setBackground(new java.awt.Color(0, 204, 153));
        searchButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        searchButton.setForeground(new java.awt.Color(153, 0, 0));
        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        getContentPane().add(searchButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 62, 126, 34));

        userLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        userLabel.setText("User");
        getContentPane().add(userLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 151, 26));

        userTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(userTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 180, 34));

        rnLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        rnLabel.setText("Room Number");
        getContentPane().add(rnLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 151, 26));

        rnTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(rnTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 310, 180, 36));

        cidLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        cidLabel.setText("CheckIn Date");
        getContentPane().add(cidLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 380, 142, 26));

        cidTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(cidTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 430, 170, 34));

        committenteLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        committenteLabel.setText("Committente");
        getContentPane().add(committenteLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 132, 184, 26));

        statusLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        statusLabel.setText("Status");
        getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 132, 172, 26));

        committenteTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(committenteTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 186, 184, 34));

        ppdTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        ppdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppdTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(ppdTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 186, 172, 34));

        noteLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        noteLabel.setText("Note");
        getContentPane().add(noteLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 247, 184, 26));

        noteTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(noteTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 312, 184, 36));

        nodLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        nodLabel.setText("Number Of Days");
        getContentPane().add(nodLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 247, 172, 26));

        nodTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(nodTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 312, 172, 34));

        codLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        codLabel.setText("CheckOut Date");
        getContentPane().add(codLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 378, 184, 26));

        codTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(codTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 431, 184, 34));

        totalLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        totalLabel.setText("Total Amount");
        getContentPane().add(totalLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 378, 172, 26));

        totalTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(totalTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 431, 172, 34));

        updateButton.setBackground(new java.awt.Color(0, 204, 153));
        updateButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        updateButton.setForeground(new java.awt.Color(153, 0, 0));
        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        getContentPane().add(updateButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(641, 502, 115, 39));

        clearButton.setBackground(new java.awt.Color(0, 204, 153));
        clearButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(153, 0, 0));
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        getContentPane().add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(834, 502, 114, 39));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Allote", "User", "Committente", "CheckIn", "CheckOut", "Status", "Room Number", "Note", "Ndays", "Total"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 570, 1130, -1));

        bgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/background.png"))); // NOI18N
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1160, 1010));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ppdTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppdTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ppdTextFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        dataFields();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
         cancel();    
    }//GEN-LAST:event_clearButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // TODO add your handling code here:
        conferm();
        
    }//GEN-LAST:event_updateButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(checkOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(checkOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(checkOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(checkOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new manageRiservation().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bgLabel;
    private javax.swing.JLabel cidLabel;
    private javax.swing.JTextField cidTextField;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel codLabel;
    private javax.swing.JTextField codTextField;
    private javax.swing.JLabel committenteLabel;
    private javax.swing.JTextField committenteTextField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel nodLabel;
    private javax.swing.JTextField nodTextField;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JTextField noteTextField;
    private javax.swing.JTextField ppdTextField;
    private javax.swing.JLabel rnLabel;
    private javax.swing.JTextField rnTextField;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField totalTextField;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
}
