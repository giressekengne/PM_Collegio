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
import java.time.LocalDate;
import java.time.ZoneId;


 // @author gigatore
 
public class checkOut extends javax.swing.JFrame {

    /**
     * Creates new form checkOut
     */
    public checkOut() {
        initComponents();
        date();
        Connect();
        
    }
    
    Connection con;
    PreparedStatement pst;

    // per convertire una stringa in int
    public int convInt(String num){
        int ris;
        ris = Integer.parseInt(num.substring(1));  
        return ris;   
    }
    
    //metodo per visualizzare il la l'id della camera in modo alphanumerico
    public String convAlfa(int num){
        String ris;
        ris = "R0" + String.format("%03d",num);
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
        emailTextField.setText("");
        nameTextField.setText("");
        mobileTextField.setText("");
        cidTextField.setText("");
        rnTextField.setText("");
        ppdTextField.setText("");
        totalTextField.setText("");
        nodTextField.setText("");
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
        
        ResultSet rs;
         try{
           
            String ids = idTextField.getText();
            int id = convInt(ids);
            int giorni;
            double prezzoT;

            pst=con.prepareStatement("SELECT U.nome, U.telefono, U.email, R.room_id, R.prezzo, Re.check_in, Re.check_out FROM Reservation Re INNER JOIN User U ON U.user_counter = Re.user_id INNER JOIN Room R ON R.room_id = Re.room_id WHERE Re.reservation_id=?");
            pst.setInt(1, id);
            rs=pst.executeQuery();
            int rni; //= rs.getInt("R.room_id");
            String rn ; //= convAlfa(rni);
            String ci;
            String co = codTextField.getText();
            //giorni = calculDate( );
            //prezzoT = Double.parseDouble(ppdTextField.getText()) * giorni;
            if(rs.next()){
                
                ci = rs.getString("check_in");
                rni = rs.getInt("R.room_id");
                rn = convAlfa(rni);
                giorni = calculDate(ci,co);
                emailTextField.setText(rs.getString("email"));
                nameTextField.setText(rs.getString("nome"));
                mobileTextField.setText(rs.getString("telefono"));
                cidTextField.setText(rs.getString("check_in"));
                rnTextField.setText(rn);
                ppdTextField.setText(rs.getString("prezzo"));
                prezzoT = Double.parseDouble(ppdTextField.getText()) * giorni;
                totalTextField.setText(Double.toString(prezzoT));
                nodTextField.setText(Integer.toString(giorni));
                
                // impedire di editare 
                emailTextField.setEditable(false);
                nameTextField.setEditable(false);
                mobileTextField.setEditable(false);
                cidTextField.setEditable(false);
                rnTextField.setEditable(false);
                ppdTextField.setEditable(false);
                totalTextField.setEditable(false);
                nodTextField.setEditable(false);
                                        

                        
            }else{
                JOptionPane.showMessageDialog(this, "Reservation Not Found");
                idTextField.setText("");
            }
               
//            answerTextField.setText("");
//            npwTextField.setText("");
        }catch (Exception ex) {
           // Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
           //System.out.println(mess);
        }
    
    }
    
    public void conferm() {
        
        ResultSet rs;
        if(emailTextField.getText().equals("")){
            JOptionPane.showMessageDialog(this, "email field  Requied");
            //surnameTextField.requestFocus();
        }
        else if(idTextField.getText().equals("")){
            JOptionPane.showMessageDialog(this, "surname field  Requied");
            //nameTextField.requestFocus();
        }
        else if(nameTextField.getText().equals("")){
            JOptionPane.showMessageDialog(this, "name field  Requied");
            //mobileTextField.requestFocus();
        }
        else if(ppdTextField.equals("")){ 
            JOptionPane.showMessageDialog(this, "gender field  Requied");
            //addressComboBox.requestFocus();
        }
        else if(codTextField.equals("")){
            JOptionPane.showMessageDialog(this, "address field  Requied");
            //mobileTextField.requestFocus();
        }
        else if(mobileTextField.getText().length()!=10){
            JOptionPane.showMessageDialog(this, "Mobile Number Should be 10 Digit");
            mobileTextField.requestFocus();
        }
        else if(cidTextField.getText().equals("")){
            JOptionPane.showMessageDialog(this, " date field requied");
            //rnComboBox.requestFocus();
        }
        else if(totalTextField.equals("")){
            JOptionPane.showMessageDialog(this, "Sorry, This Room Not avaible Select another room");
        }
        else if(rnTextField.equals("")){
            JOptionPane.showMessageDialog(this, "field requied");
        }
        else if(nodTextField.equals("")){
            JOptionPane.showMessageDialog(this, "field requied");
        }
        else{
            int fatturaId = -1;
            try{
                String ids = idTextField.getText();
                int id = convInt(ids);
                int giorni = Integer.parseInt(nodTextField.getText());
                int rni = convInt(rnTextField.getText());
                double importo = Double.parseDouble(totalTextField.getText());

                con.setAutoCommit(false);

                pst = con.prepareStatement("UPDATE Reservation SET check_out=?, status=?, note=?, giorni=? WHERE reservation_id=?");
                pst.setString(1, codTextField.getText());
                pst.setString(2, "completata");
                pst.setString(3, "Camera libera");
                pst.setInt(4, giorni);
                pst.setInt(5, id);
                pst.executeUpdate();

                pst = con.prepareStatement("UPDATE Room SET stato=? WHERE room_id=?");
                pst.setString(1, "disponibile");
                pst.setInt(2, rni);
                pst.executeUpdate();

                String oggi = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                pst = con.prepareStatement("SELECT fattura_id FROM Fattura WHERE reservation_id=?");
                pst.setInt(1, id);
                ResultSet frs = pst.executeQuery();
                if (frs.next()) {
                    fatturaId = frs.getInt(1);
                    pst = con.prepareStatement("UPDATE Fattura SET importo=?, data_emissione=?, stato='in attesa' WHERE fattura_id=?");
                    pst.setDouble(1, importo);
                    pst.setString(2, oggi);
                    pst.setInt(3, fatturaId);
                    pst.executeUpdate();
                } else {
                    pst = con.prepareStatement(
                        "INSERT INTO Fattura(reservation_id, importo, data_emissione, stato) VALUES(?,?,?,?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                    pst.setInt(1, id);
                    pst.setDouble(2, importo);
                    pst.setString(3, oggi);
                    pst.setString(4, "in attesa");
                    pst.executeUpdate();
                    ResultSet gk = pst.getGeneratedKeys();
                    if (gk.next()) fatturaId = gk.getInt(1);
                }

                con.commit();
                cancel();

            }catch(SQLException ex){
                try {
                    if (con != null) con.rollback();
                }catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore checkout:\n" + ex.getMessage(), "Errore DB", JOptionPane.ERROR_MESSAGE);
            }finally {
                try {
                    if (pst != null) pst.close();
                    if (con != null) {
                        con.setAutoCommit(true);
                        con.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (fatturaId > 0) {
                new fattura(fatturaId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Checkout completato, ma nessuna fattura trovata per questa prenotazione.");
            }
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
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        rnLabel = new javax.swing.JLabel();
        rnTextField = new javax.swing.JTextField();
        cidLabel = new javax.swing.JLabel();
        cidTextField = new javax.swing.JTextField();
        mobileLabel = new javax.swing.JLabel();
        ppdLabel = new javax.swing.JLabel();
        mobileTextField = new javax.swing.JTextField();
        ppdTextField = new javax.swing.JTextField();
        emailLabel = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        nodLabel = new javax.swing.JLabel();
        nodTextField = new javax.swing.JTextField();
        codLabel = new javax.swing.JLabel();
        codTextField = new javax.swing.JTextField();
        totalLabel = new javax.swing.JLabel();
        totalTextField = new javax.swing.JTextField();
        coButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
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
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 0, -1, -1));

        logoLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/checked.png"))); // NOI18N
        logoLabel.setText("CheckOut");
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 151, -1));

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

        nameLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        nameLabel.setText("Name");
        getContentPane().add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 151, 26));

        nameTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 180, 34));

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

        mobileLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        mobileLabel.setText("Mobile");
        getContentPane().add(mobileLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 132, 184, 26));

        ppdLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        ppdLabel.setText("Price Per Day");
        getContentPane().add(ppdLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 132, 172, 26));

        mobileTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(mobileTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 186, 184, 34));

        ppdTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        ppdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppdTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(ppdTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(776, 186, 172, 34));

        emailLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        emailLabel.setText("Email");
        getContentPane().add(emailLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 247, 184, 26));

        emailTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(emailTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(428, 312, 184, 36));

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

        coButton.setBackground(new java.awt.Color(0, 204, 153));
        coButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        coButton.setForeground(new java.awt.Color(153, 0, 0));
        coButton.setText("CheckOut");
        coButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coButtonActionPerformed(evt);
            }
        });
        getContentPane().add(coButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(641, 502, 115, 39));

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

        bgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/background.png"))); // NOI18N
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(-20, 0, 1160, 1010));

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

    private void coButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coButtonActionPerformed
        // TODO add your handling code here:
        conferm();
        
    }//GEN-LAST:event_coButtonActionPerformed

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
                new checkOut().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bgLabel;
    private javax.swing.JLabel cidLabel;
    private javax.swing.JTextField cidTextField;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton coButton;
    private javax.swing.JLabel codLabel;
    private javax.swing.JTextField codTextField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel mobileLabel;
    private javax.swing.JTextField mobileTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel nodLabel;
    private javax.swing.JTextField nodTextField;
    private javax.swing.JLabel ppdLabel;
    private javax.swing.JTextField ppdTextField;
    private javax.swing.JLabel rnLabel;
    private javax.swing.JTextField rnTextField;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField totalTextField;
    // End of variables declaration//GEN-END:variables
}
