package BLL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Desktop;

import DAL.UsageSessionDAL;
import DTO.CompStatus;
import DTO.ComputerDTO;
import DTO.UsageSessionDTO;

public class UsageSessionBLL {
    private UsageSessionDAL dal = UsageSessionDAL.getInstance();
    private static UsageSessionBLL instance;

    private UsageSessionBLL(){}

    public static UsageSessionBLL getInstance(){
        if(instance == null) instance = new UsageSessionBLL();
        return instance;
    }

    public String checkout(UsageSessionDTO session, String computerName) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            session.setEndTime(endTime);

            long minutes = Duration.between(session.getStartTime(), endTime).toMinutes();

            double totalPrice = (minutes / 60.0) * session.getPriceAtStart();
            //double finalPrice = Math.ceil(rawPrice / 1000) * 1000; // Làm tròn lên hàng nghìn
            session.setTotalPrice(totalPrice);

            if (dal.endSession(session)) {
                exportBillToFile(session, computerName, minutes);
                return "In bill thành công.";
            }
            return "Lỗi cập nhật Database!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi";
        }
    }

    public void exportBillToFile(UsageSessionDTO session, String computerName, long minutes) {
        File directory = new File("HoaDon");
        if (!directory.exists()) {
            directory.mkdirs(); 
        }

        // Đặt tên file theo định dạng: Bill_SessionID_NgayThang.txt
        String timeStr = session.getEndTime().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm"));
        String fileName = "Bill_" + session.getSessionId() + "_" + timeStr + ".txt";
        File finalFile = new File(directory, fileName);
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(finalFile), StandardCharsets.UTF_8))) {
            writer.println("=========================================");
            writer.println("           HÓA ĐƠN QUÁN NET SV           ");
            writer.println("=========================================");
            writer.println("Mã giao dịch : " + session.getSessionId());
            writer.println("Tên máy      : " + computerName);
            writer.println("-----------------------------------------");
            writer.println("Thời gian BĐ : " + session.getStartTime().format(dtf));
            writer.println("Thời gian KT : " + session.getEndTime().format(dtf));
            writer.println("Tổng thời gian: " + minutes + " phút");
            writer.println("Đơn giá      : " + String.format("%,.0f", session.getPriceAtStart()) + " VNĐ/giờ");
            writer.println("-----------------------------------------");
            writer.println("TỔNG THANH TOÁN: " + String.format("%,.0f", session.getTotalPrice()) + " VNĐ");
            writer.println("=========================================");
            writer.println("         CẢM ƠN VÀ HẸN GẶP LẠI!          ");

            if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(finalFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi in hóa đơn");
        }
    }

    public UsageSessionDTO getCurSessionByCompId(int computerId) throws Exception {
        return dal.getCurSessionByCompId(computerId);
    }

    public String startNewSession(ComputerDTO cp) {
        try {
            if (cp.getStatus() != CompStatus.AVAILABLE) {
                return "Máy không sẵn sàng!";
            }
            
            if (dal.startSession(cp)) {
                return "Mở máy thành công!";
            }
            return "Lỗi khi mở máy.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi mở máy";
        }
    }

    public String cancelSession(UsageSessionDTO session) {
        try {
            if (dal.cancelSession(session)) {
                return "Hủy phiên thành công";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi hủy phiên";
        }
        return "Hủy phiên thất bại!";
    }

    public List<UsageSessionDTO> getPaidSessionsByDateRange(java.util.Date fromDate, java.util.Date toDate) throws SQLException {
        return dal.getPaidSessionsByDateRange(fromDate, toDate);
    }
}
