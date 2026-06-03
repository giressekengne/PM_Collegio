package PM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// @author gigatore

public class gestioneFatture extends javax.swing.JFrame {

    Connection con;
    PreparedStatement pst;

    public gestioneFatture() {
        initComponents();
        Connect();
        loadTable();
        fattureTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = fattureTable.getSelectedRow();
                if (row >= 0) {
                    String stato = (String) ((DefaultTableModel) fattureTable.getModel()).getValueAt(row, 6);
                    boolean pagabile = "in attesa".equals(stato) || "non pagato".equals(stato);
                    pagaButton.setEnabled(pagabile);
                    annullaButton.setEnabled(pagabile);
                }
            }
        });
        pagaButton.setEnabled(false);
        annullaButton.setEnabled(false);
    }

    public void Connect() {
        try {
            con = DBConfig.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(gestioneFatture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTable() {
        try {
            DefaultTableModel model = (DefaultTableModel) fattureTable.getModel();
            model.setRowCount(0);
            String sql = "SELECT F.fattura_id, F.reservation_id, U.nome, R.numero_stanza, " +
                "F.importo, F.data_emissione, F.stato " +
                "FROM Fattura F " +
                "JOIN Reservation Res ON Res.reservation_id = F.reservation_id " +
                "JOIN User U ON U.user_counter = Res.user_id " +
                "JOIN Room R ON R.room_id = Res.room_id";
            if ("U".equalsIgnoreCase(Session.roleType)) {
                pst = con.prepareStatement(sql + " WHERE Res.user_id = ? ORDER BY F.fattura_id DESC");
                pst.setString(1, Session.userCounter);
            } else {
                pst = con.prepareStatement(sql + " ORDER BY F.fattura_id DESC");
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    "P0" + String.format("%03d", rs.getInt(2)),
                    rs.getString(3),
                    rs.getInt(4),
                    String.format("€ %.2f", rs.getDouble(5)),
                    rs.getString(6),
                    rs.getString(7)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento fatture.");
        }
    }

    private int getSelectedFatturaId() {
        int row = fattureTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona prima una fattura dalla tabella.");
            return -1;
        }
        return (int) ((DefaultTableModel) fattureTable.getModel()).getValueAt(row, 0);
    }

    public void pagaFattura() {
        int fatturaId = getSelectedFatturaId();
        if (fatturaId < 0) return;
        try {
            pst = con.prepareStatement("SELECT metodo_id, nome FROM MetodoPagamento");
            ResultSet rs = pst.executeQuery();
            Map<String, Integer> metodiMap = new LinkedHashMap<>();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                String nome = rs.getString("nome");
                metodiMap.put(nome, rs.getInt("metodo_id"));
                model.addElement(nome);
            }
            if (model.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "Nessun metodo di pagamento disponibile nel DB.");
                return;
            }

            JComboBox<String> combo = new JComboBox<>(model);
            int result = JOptionPane.showConfirmDialog(this, combo,
                "Seleziona metodo di pagamento", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String metodoNome = (String) combo.getSelectedItem();
            int metodoId = metodiMap.get(metodoNome);

            pst = con.prepareStatement("SELECT importo FROM Fattura WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            rs = pst.executeQuery();
            double importo = rs.next() ? rs.getDouble(1) : 0;

            pst = con.prepareStatement("SELECT MAX(pagamento_id) FROM Pagamento");
            rs = pst.executeQuery();
            int pagamentoId = (rs.next() && rs.getObject(1) != null) ? rs.getInt(1) + 1 : 1;

            pst = con.prepareStatement(
                "INSERT INTO Pagamento(pagamento_id, fattura_id, metodo_id, importo, data_pagamento) VALUES(?,?,?,?,?)");
            pst.setInt(1, pagamentoId);
            pst.setInt(2, fatturaId);
            pst.setInt(3, metodoId);
            pst.setDouble(4, importo);
            pst.setString(5, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            pst.executeUpdate();

            pst = con.prepareStatement("UPDATE Fattura SET stato='pagato' WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Pagamento registrato con successo!");
            loadTable();
            pagaButton.setEnabled(false);
            annullaButton.setEnabled(false);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore durante il pagamento.");
            ex.printStackTrace();
        }
    }

    public void annullaFattura() {
        int fatturaId = getSelectedFatturaId();
        if (fatturaId < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Annullare la fattura #" + fatturaId + "? Lo stato diventerà 'non pagato'.",
            "Conferma annullamento", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            pst = con.prepareStatement("UPDATE Fattura SET stato='non pagato' WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Fattura annullata.");
            loadTable();
            pagaButton.setEnabled(false);
            annullaButton.setEnabled(false);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore annullamento fattura.");
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        // Close button
        closeButton = new javax.swing.JButton();
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png")));
        closeButton.addActionListener(e -> dispose());
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 0, -1, -1));

        // Logo
        logoLabel = new javax.swing.JLabel("Gestione Fatture");
        logoLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 18));
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/manage.png")));
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 300, 50));

        // Table
        fattureTable = new javax.swing.JTable();
        fattureTable.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Prenotazione", "Cliente", "Camera", "Importo", "Data", "Stato"}
        ) {
            public boolean isCellEditable(int row, int col) { return false; }
        });
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(fattureTable);
        getContentPane().add(scrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 1040, 420));

        // Paga button
        pagaButton = new javax.swing.JButton("Paga");
        pagaButton.setBackground(new java.awt.Color(0, 204, 153));
        pagaButton.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 15));
        pagaButton.setForeground(new java.awt.Color(0, 51, 0));
        pagaButton.addActionListener(e -> pagaFattura());
        getContentPane().add(pagaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, 160, 45));

        // Annulla button
        annullaButton = new javax.swing.JButton("Annulla");
        annullaButton.setBackground(new java.awt.Color(204, 0, 0));
        annullaButton.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 15));
        annullaButton.setForeground(java.awt.Color.WHITE);
        annullaButton.addActionListener(e -> annullaFattura());
        getContentPane().add(annullaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 530, 160, 45));

        // Background (per ULTIMO → dietro a tutto)
        bgLabel = new javax.swing.JLabel();
        bgLabel.setBackground(new java.awt.Color(250, 250, 252));
        bgLabel.setOpaque(true);
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 600));

        setSize(1100, 600);
        setLocationRelativeTo(null);
    }

    private javax.swing.JButton closeButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JTable fattureTable;
    private javax.swing.JButton pagaButton;
    private javax.swing.JButton annullaButton;
    private javax.swing.JLabel bgLabel;
}
