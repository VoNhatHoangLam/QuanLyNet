package DAL;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import DTO.ComputerDTO;
import DTO.PaymentStatus;
import DTO.UsageSessionDTO;

public class UsageSessionDAL {
    private static UsageSessionDAL instance;

    private UsageSessionDAL(){}

    public static UsageSessionDAL getInstance(){
        if(instance == null) instance = new UsageSessionDAL();
        return instance;
    }

    public boolean startSession(ComputerDTO cp) throws SQLException {
        String insertSessionSql = "INSERT INTO UsageSession (computerId, priceAtStart, paymentStatus) VALUES (?, ?, 'UNPAID')";
        String updateComputerSql = "UPDATE Computer SET status = 'OCCUPIED' WHERE computerId = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSessionSql)) {
                pstmt1.setInt(1, cp.getComputerId());
                pstmt1.setDouble(2, cp.getPricePerHour());
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(updateComputerSql)) {
                pstmt2.setInt(1, cp.getComputerId());
                pstmt2.executeUpdate();
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); 
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public boolean endSession(UsageSessionDTO session) throws SQLException {
        String sqlUpdateSession = "UPDATE UsageSession SET endTime = ?, totalPrice = ?, paymentStatus = 'PAID' WHERE sessionId = ?";
        String sqlUpdateComputer = "UPDATE Computer SET status = 'AVAILABLE' WHERE computerId = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 
            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlUpdateSession)) {
                pstmt1.setTimestamp(1, Timestamp.valueOf(session.getEndTime()));
                pstmt1.setDouble(2, session.getTotalPrice());
                pstmt1.setInt(3, session.getSessionId());
                pstmt1.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlUpdateComputer)) {
                pstmt2.setInt(1, session.getComputerId());
                pstmt2.executeUpdate();
            }
            conn.commit(); 
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); 
            }
            throw e; 
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public UsageSessionDTO getCurSessionByCompId(int computerId) throws SQLException {
        String sql = "SELECT * FROM UsageSession WHERE computerId = ? AND paymentStatus = 'UNPAID' " +
                    "ORDER BY startTime DESC LIMIT 1";        
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, computerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UsageSessionDTO session = new UsageSessionDTO();
                    session.setSessionId(rs.getInt("sessionId"));
                    session.setComputerId(rs.getInt("computerId"));
                    session.setPriceAtStart(rs.getDouble("priceAtStart"));
                    session.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
                    session.setPaymentStatus(PaymentStatus.valueOf(rs.getString("paymentStatus")));
                    return session;
                }
            }
        }
        return null;
    }

    public boolean cancelSession(UsageSessionDTO session) throws SQLException {
        String sqlDeleteSession = "DELETE FROM UsageSession WHERE sessionId = ?";
        String sqlUpdateComputer = "UPDATE Computer SET status = 'AVAILABLE' WHERE computerId = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteSession)) {
                pstmt1.setInt(1, session.getSessionId());
                int affectedRows = pstmt1.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlUpdateComputer)) {
                pstmt2.setInt(1, session.getComputerId());
                pstmt2.executeUpdate();
            }

            conn.commit(); 
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
