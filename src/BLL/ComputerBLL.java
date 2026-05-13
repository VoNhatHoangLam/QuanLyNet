/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BLL;

import DAL.ComputerDAL;
import DTO.CompStatus;
import DTO.ComputerDTO;
import java.util.List;

/**
 *
 * @author ripni
 */
public class ComputerBLL {
    private ComputerDAL compDAL = ComputerDAL.getInstance();
    private static ComputerBLL instance;
    
    private ComputerBLL(){}
    
    public static ComputerBLL getInstance(){
        if(instance == null) instance = new ComputerBLL();
        return instance;
    }

    public List<ComputerDTO> getAllComputers() throws Exception {
        return compDAL.getAllComputers();
    }

    public String createComputer(ComputerDTO comp) {
        if (comp.getComputerName().isBlank()) return "Tên máy không được để trống!";
        if (comp.getPricePerHour() <= 0) return "Giá máy phải lớn hơn 0!";
        try {
            return compDAL.insertComputer(comp) ? "Thêm thành công!" : "Thêm thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi";
        }
    }

    public String updateComputer(ComputerDTO comp) {
        if(comp.getStatus() != CompStatus.AVAILABLE) return "Không thể cập nhật máy đang bận";
        try {
            return compDAL.updateComputer(comp) ? "Cập nhật thành công!" : "Cập nhật thất bại!";
        } catch (Exception e) {
            return "Lỗi";
        }
    }

    public String deleteComputer(int id) {
        try {
            return compDAL.deleteComputer(id) ? "Xóa thành công!" : "Xóa thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi";
        }
    }
}
