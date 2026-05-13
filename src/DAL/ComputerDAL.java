/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAL;

import DTO.ComputerDTO;
import DTO.CompStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author ripni
 */
public class ComputerDAL {
    private static ComputerDAL instance;
           
    private ComputerDAL(){}

    public static ComputerDAL getInstance(){
        if(instance == null) instance = new ComputerDAL();
        return instance;
    }
    
    public List<ComputerDTO> getAllComputers() throws SQLException {
        List<ComputerDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM Computer";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ComputerDTO comp = new ComputerDTO(
                    rs.getInt("computerId"),
                    rs.getString("computerName"),
                    rs.getDouble("pricePerHour"),
                    CompStatus.valueOf(rs.getString("status"))
                );
                list.add(comp);
            }
        }
        return list;
    }

    public boolean insertComputer(ComputerDTO comp) throws SQLException {
        String sql = "INSERT INTO Computer (computerName, pricePerHour, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, comp.getComputerName());
            pstmt.setDouble(2, comp.getPricePerHour());
            pstmt.setString(3, comp.getStatus().toString());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateComputer(ComputerDTO comp) throws SQLException {
        String sql = "UPDATE Computer SET computerName = ?, pricePerHour = ?, status = ? WHERE computerId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, comp.getComputerName());
            pstmt.setDouble(2, comp.getPricePerHour());
            pstmt.setString(3, comp.getStatus().toString());
            pstmt.setInt(4, comp.getComputerId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteComputer(int id) throws SQLException {
        String sql = "DELETE FROM Computer WHERE computerId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}
