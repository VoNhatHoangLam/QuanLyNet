/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import BLL.ComputerBLL;
import BLL.UsageSessionBLL;
import DTO.CompStatus;
import DTO.ComputerDTO;
import DTO.UsageSessionDTO;

import java.awt.Color;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ripni
 */
public class frmMain extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(frmMain.class.getName());
    private ComputerBLL compBLL = ComputerBLL.getInstance();
    private UsageSessionBLL sessionBLL = UsageSessionBLL.getInstance();
    private int selectedCompId = -1;
    private ComputerDTO selectedComputer = null;
    private UsageSessionDTO currentSession = null;

    /**
     * Creates new form frmMain
     */
    public frmMain() {
        initComponents();
        setGUI();
    }

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new frmMain().setVisible(true));
    }
    
    private void setGUI(){
        loadCompTable();
        loadComputerMap();
        pnUsageInfo.setVisible(false);
    }

    public void loadComputerMap() {
        loadComputerMap(""); 
    }

    public void loadComputerMap(String keyword) {
        pnCompMap.removeAll();
        this.selectedComputer = null;
        try {
            List<ComputerDTO> list;
            if (keyword == null || keyword.isBlank()) {
                list = compBLL.getAllComputers();
            } else {
                list = compBLL.searchComputers(keyword.trim()); 
            }
            int compCount = 1;
            for (ComputerDTO cp : list) {
                JButton btn = new JButton();
                btn.setText("Máy " + compCount++ + ": " + cp.getComputerName());
                btn.setPreferredSize(new Dimension(150, 150));
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);

                switch (cp.getStatus().toString().toUpperCase()) {
                    case "AVAILABLE":
                        btn.setBackground(Color.GREEN);
                        break;
                    case "OCCUPIED":
                        btn.setBackground(Color.RED);
                        break;
                    case "MAINTENANCE":
                        btn.setBackground(Color.GRAY);
                        btn.setEnabled(false);
                        break;
                }
                
                btn.addActionListener(e -> {
                    this.selectedComputer = cp;
                    updateSidePanel(cp);
                });
                pnCompMap.add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ máy: " + e.getMessage());
        }
        
        pnCompMap.revalidate();
        pnCompMap.repaint();
    }

    private void updateSidePanel(ComputerDTO cp) {
        pnUsageInfo.setVisible(true);
        txtPayCompName.setText(cp.getComputerName());
        txtPriceAtStart.setText(String.format("%,.0f VNĐ/h", cp.getPricePerHour()));

        if (cp.getStatus() == CompStatus.AVAILABLE) {
            txtStartTime.setText("------");
            
            btnStart.setVisible(true);      // Hiện nút Bắt đầu
            btnPrintBill.setVisible(false);    // Ẩn nút Thanh toán
            btnCancel.setVisible(false);    // Ẩn nút hủy phiên
        } 
        else {
            try{
                UsageSessionDTO session = sessionBLL.getCurSessionByCompId(cp.getComputerId());
                if (session != null) {
                    this.currentSession = session;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    txtStartTime.setText(session.getStartTime().format(formatter));
                }               
                btnStart.setVisible(false);     // Ẩn nút Bắt đầu
                btnPrintBill.setVisible(true);     // Hiện nút Thanh toán
                btnCancel.setVisible(true);     // Hiện nút hủy phiên 
            }
            catch(Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải thông tin phiên sử dụng");
                return;
            }
        }
    }

    private void loadCompTable() {
        try {
            List<ComputerDTO> list = compBLL.getAllComputers();
            DefaultTableModel model = (DefaultTableModel) tblComputer.getModel();
            model.setRowCount(0);
            for (ComputerDTO c : list) {
                model.addRow(new Object[]{c.getComputerId(), c.getComputerName(), c.getPricePerHour(), c.getStatus()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải bảng Máy tính");
        }
    }   

    private void clearComputerForm() {
        txtCompName.setText("");
        txtCompPrice.setText("");
        this.selectedCompId = -1;
    }
    
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {                                           
        String name = txtCompName.getText();
        String priceRaw = txtCompPrice.getText();
        if (name.isBlank() || priceRaw.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên máy và Giá máy!");
            return; 
        }

        try {
            double price = Double.parseDouble(priceRaw);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Giá tiền không được là số âm!");
                return;
            }
            ComputerDTO comp = new ComputerDTO();
            comp.setComputerName(name);
            comp.setPricePerHour(price);
            comp.setStatus(CompStatus.AVAILABLE);

            String msg = compBLL.createComputer(comp);
            JOptionPane.showMessageDialog(this, msg);
            if (msg.contains("thành công")) {
                loadCompTable();
                clearComputerForm();
                loadComputerMap();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá máy không hợp lệ! Vui lòng chỉ nhập số (ví dụ: 5000 hoặc 7500.5).");
            txtCompPrice.requestFocus();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jCalendar1 = new com.toedter.calendar.JCalendar();
        jPanel12 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        pnUsageInfo = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtPayCompName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtStartTime = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPriceAtStart = new javax.swing.JTextField();
        btnPrintBill = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        txtSearchHome = new javax.swing.JTextField();
        btnSearchHome = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        pnCompMap = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblComputer = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCompName = new javax.swing.JTextField();
        txtCompPrice = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        dcBegin = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        dcEnd = new com.toedter.calendar.JDateChooser();
        btnShowReport = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lbProfit = new javax.swing.JLabel();
        lbTotalSession = new javax.swing.JLabel();
        lbMostUse = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbReport = new javax.swing.JTable();
        lbLeastUse = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        pnUsageInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin phiên sử dụng"));

        jLabel6.setText("Tên máy:");

        txtPayCompName.setEditable(false);

        jLabel7.setText("Giờ vào:");

        txtStartTime.setEditable(false);

        jLabel8.setText("Đơn giá:");

        txtPriceAtStart.setEditable(false);

        btnPrintBill.setBackground(new java.awt.Color(255, 255, 0));
        btnPrintBill.setText("In hóa đơn");
        btnPrintBill.addActionListener(this::btnPrintBillActionPerformed);

        btnStart.setBackground(new java.awt.Color(0, 255, 51));
        btnStart.setText("Mở máy");
        btnStart.addActionListener(this::btnStartActionPerformed);

        btnCancel.setBackground(new java.awt.Color(255, 51, 0));
        btnCancel.setText("Hủy phiên");
        btnCancel.addActionListener(this::btnCancelActionPerformed);

        javax.swing.GroupLayout pnUsageInfoLayout = new javax.swing.GroupLayout(pnUsageInfo);
        pnUsageInfo.setLayout(pnUsageInfoLayout);
        pnUsageInfoLayout.setHorizontalGroup(
            pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnUsageInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
                .addGap(12, 12, 12)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPayCompName, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(txtStartTime, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtPriceAtStart)))
            .addGroup(pnUsageInfoLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnPrintBill, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnUsageInfoLayout.setVerticalGroup(
            pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnUsageInfoLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtPayCompName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(txtPriceAtStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(pnUsageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart)
                    .addComponent(btnPrintBill))
                .addGap(18, 18, 18)
                .addComponent(btnCancel)
                .addContainerGap(380, Short.MAX_VALUE))
        );

        jPanel1.add(pnUsageInfo, java.awt.BorderLayout.LINE_END);

        btnSearchHome.setBackground(new java.awt.Color(204, 204, 204));
        btnSearchHome.setText("Tìm kiếm");
        btnSearchHome.addActionListener(this::btnSearchHomeActionPerformed);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(txtSearchHome, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearchHome)
                .addContainerGap(444, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchHome))
                .addGap(35, 35, 35))
        );

        jPanel1.add(jPanel16, java.awt.BorderLayout.PAGE_START);

        pnCompMap.setLayout(new java.awt.GridBagLayout());
        jScrollPane4.setViewportView(pnCompMap);

        jPanel1.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Trang chủ", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 681, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel5.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 681, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel5.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        jPanel8.setLayout(new java.awt.BorderLayout());

        tblComputer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Mã máy", "Tên máy", "Giá máy", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblComputer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblComputerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblComputer);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 669, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel8.add(jPanel9, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Tên máy");

        jLabel2.setText("Giá máy");

        btnAdd.setText("Thêm");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnUpdate.setText("Sửa");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnDelete.setText("Xóa");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnSearch.setBackground(new java.awt.Color(204, 204, 204));
        btnSearch.setText("Tìm kiếm");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnAdd))
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCompName, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                            .addComponent(txtCompPrice))
                        .addGap(194, 194, 194)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSearch))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(btnUpdate)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtCompName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSearch)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCompPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(23, 23, 23)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel10, java.awt.BorderLayout.PAGE_START);

        jPanel5.add(jPanel8, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Quản lý máy", jPanel2);

        jLabel3.setText("Từ ngày:");

        jLabel4.setText("Đến ngày:");

        btnShowReport.setBackground(new java.awt.Color(102, 255, 0));
        btnShowReport.setText("Xem báo cáo");
        btnShowReport.addActionListener(this::btnShowReportActionPerformed);

        lbProfit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbProfit.setText("Tổng doanh thu:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbProfit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(377, 377, 377))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbProfit)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        lbTotalSession.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbTotalSession.setText("Tổng số phiên:");

        lbMostUse.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbMostUse.setText("Máy dùng nhiều nhất:");

        jPanel11.setLayout(new java.awt.BorderLayout());

        tbReport.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã GD", "Tên máy", "Giờ vào", "Giờ ra", "Thời gian", "Tổng tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tbReport);

        lbLeastUse.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbLeastUse.setText("Máy dùng ít nhất:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dcBegin, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dcEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(btnShowReport)
                .addGap(47, 47, 47))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbLeastUse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbMostUse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbTotalSession, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(189, 189, 189)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel4)
                        .addComponent(dcBegin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3))
                    .addComponent(dcEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowReport))
                .addGap(19, 19, 19)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lbTotalSession)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbMostUse)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbLeastUse)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Thống kê", jPanel4);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (selectedCompId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa máy này không?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            CompStatus status = CompStatus.valueOf(tblComputer.getValueAt(tblComputer.getSelectedRow(), 3).toString());
            if(status == CompStatus.OCCUPIED){
                JOptionPane.showMessageDialog(this, "Không thể xóa máy đang có người dùng");
                return;
            }
            String result = compBLL.deleteComputer(selectedCompId);
            JOptionPane.showMessageDialog(this, result);
            loadCompTable();
            clearComputerForm();
            loadComputerMap();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (selectedCompId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy trong bảng để sửa!");
            return;
        }

        try {
            String name = txtCompName.getText();
            String priceRaw = txtCompPrice.getText();
            if (name.isBlank() || priceRaw.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên máy và Giá máy!");
                return;
            }
            double price = Double.parseDouble(priceRaw);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Giá tiền không được là số âm!");
                return;
            }

            ComputerDTO comp = new ComputerDTO();
            comp.setComputerId(selectedCompId);
            comp.setComputerName(name);
            comp.setPricePerHour(price);
            comp.setStatus(CompStatus.AVAILABLE);
            String result = compBLL.updateComputer(comp);
            JOptionPane.showMessageDialog(this, result);
            if(result.contains("thành công")){
                loadCompTable();
                clearComputerForm();
                loadComputerMap();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá máy không hợp lệ! Vui lòng chỉ nhập số (ví dụ: 5000 hoặc 7500.5).");
            txtCompPrice.requestFocus();
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void tblComputerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblComputerMouseClicked
        int rowIndex = tblComputer.getSelectedRow();
        if (rowIndex >= 0) {
            String id = tblComputer.getValueAt(rowIndex, 0).toString();
            String name = tblComputer.getValueAt(rowIndex, 1).toString();
            String price = tblComputer.getValueAt(rowIndex, 2).toString();
            this.selectedCompId = Integer.parseInt(id);
            txtCompName.setText(name);
            txtCompPrice.setText(price);
        }
    }//GEN-LAST:event_tblComputerMouseClicked

    private void btnShowReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowReportActionPerformed
        java.util.Date fromDate = dcBegin.getDate();
        java.util.Date toDate = dcEnd.getDate();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");
            return;
        }

        if (fromDate.after(toDate)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(toDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        java.util.Date fixedToDate = cal.getTime();

        try {
            List<UsageSessionDTO> list = sessionBLL.getPaidSessionsByDateRange(fromDate, fixedToDate);
            DefaultTableModel model = (DefaultTableModel) tbReport.getModel();
            model.setRowCount(0);            
            double totalProfit = 0;
            Map<String, Integer> computerUsageCount = new HashMap<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (UsageSessionDTO s : list) {
                long duration = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                model.addRow(new Object[]{
                    s.getSessionId(),
                    s.getComputerName(),
                    s.getStartTime().format(dtf),
                    s.getEndTime().format(dtf),
                    duration + " phút",
                    String.format("%,.0f VND", s.getTotalPrice())
                });

                totalProfit += s.getTotalPrice();
                String compName = s.getComputerName();
                computerUsageCount.put(compName, computerUsageCount.getOrDefault(compName, 0) + 1);
            }
            String mostUsed = "---";
            String leastUsed = "---";
            
            if (!computerUsageCount.isEmpty()) {
                mostUsed = Collections.max(computerUsageCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                leastUsed = Collections.min(computerUsageCount.entrySet(), Map.Entry.comparingByValue()).getKey();
            }
            lbProfit.setText(String.format("Tổng doanh thu:  %,.0f VNĐ", totalProfit));
            lbTotalSession.setText("Tổng số phiên:  " + list.size());
            lbMostUse.setText("Máy dùng nhiều nhất:  " + mostUsed);
            lbLeastUse.setText("Máy dùng ít nhất:  " + leastUsed);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu thống kê");
            return;
        }
    }//GEN-LAST:event_btnShowReportActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadCompTable();
            return;
        }

        try {
            List<ComputerDTO> results = compBLL.searchComputers(keyword);
            
            DefaultTableModel model = (DefaultTableModel) tblComputer.getModel();
            model.setRowCount(0); 

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy máy nào khớp với từ khóa: " + keyword);
            } else {
                for (ComputerDTO cp : results) {
                    model.addRow(new Object[]{
                        cp.getComputerId(),
                        cp.getComputerName(),
                        String.format("%,.0f", cp.getPricePerHour()),
                        cp.getStatus()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm");
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn HỦY phiên sử dụng này?\n",
            "Hủy phiên", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String result = sessionBLL.cancelSession(currentSession);
            if (result.contains("thành công")) {
                JOptionPane.showMessageDialog(this, "Đã hủy phiên thành công!");
                loadComputerMap();
                loadCompTable();
                pnUsageInfo.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, result, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        String result = sessionBLL.startNewSession(selectedComputer);
        if (result.contains("thành công")) {
            JOptionPane.showMessageDialog(this, "Máy " + selectedComputer.getComputerName() + " đã bắt đầu tính giờ!");
            loadComputerMap();
            loadCompTable();
            pnUsageInfo.setVisible(false);
            this.selectedComputer = null;
        }
        else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPrintBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintBillActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận thanh toán cho " + selectedComputer.getComputerName() + "?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String result = sessionBLL.checkout(currentSession, selectedComputer.getComputerName());
            if (result.contains("thành công")) {
                JOptionPane.showMessageDialog(this, "Hóa đơn đã được in ra file.");
                loadComputerMap();
                loadCompTable();
                pnUsageInfo.setVisible(false);
                this.currentSession = null;
                this.selectedComputer = null;
            } else {
                JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }//GEN-LAST:event_btnPrintBillActionPerformed

    private void btnSearchHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchHomeActionPerformed
        loadComputerMap(txtSearchHome.getText());
    }//GEN-LAST:event_btnSearchHomeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnPrintBill;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSearchHome;
    private javax.swing.JButton btnShowReport;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnUpdate;
    private com.toedter.calendar.JDateChooser dcBegin;
    private com.toedter.calendar.JDateChooser dcEnd;
    private com.toedter.calendar.JCalendar jCalendar1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lbLeastUse;
    private javax.swing.JLabel lbMostUse;
    private javax.swing.JLabel lbProfit;
    private javax.swing.JLabel lbTotalSession;
    private javax.swing.JPanel pnCompMap;
    private javax.swing.JPanel pnUsageInfo;
    private javax.swing.JTable tbReport;
    private javax.swing.JTable tblComputer;
    private javax.swing.JTextField txtCompName;
    private javax.swing.JTextField txtCompPrice;
    private javax.swing.JTextField txtPayCompName;
    private javax.swing.JTextField txtPriceAtStart;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearchHome;
    private javax.swing.JTextField txtStartTime;
    // End of variables declaration//GEN-END:variables
}
