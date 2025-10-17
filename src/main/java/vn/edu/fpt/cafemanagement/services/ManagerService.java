package vn.edu.fpt.cafemanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.entities.Role;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ManagerService {
    private final ManagerRepository managerRepository;

    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public List<Manager> getList() {
        return managerRepository.findAll();
    }

    public Manager findById(int artistId) {
        return managerRepository.findById(artistId).orElse(null);
    }

    @Transactional
    public Manager createStaff(Manager staff) {
        return managerRepository.save(staff);
    }
    @Transactional
    //Create, Update
    public Manager save(Manager staff) {
        return managerRepository.save(staff);
    }
    public Manager findByUsername(String username) {
        return managerRepository.findByUsername(username).orElse(null);
    }

    public Manager saveManager(Manager manager) {
        return managerRepository.save(manager);
    }

    @Transactional
    public void deleteById(int id) {
        managerRepository.deleteById(id);
    }

    public Manager getDefaultManager() {
        return managerRepository.findAll().isEmpty() ? null : managerRepository.findAll().get(0);
    }

    public boolean isUsernameTaken(String username, Integer idToExclude) {
        Optional<Manager> existing = managerRepository.findByUsername(username);
        // nếu không tồn tại -> false (chưa bị lấy)
        // nếu tồn tại và id của existing khác idToExclude -> true (bị lấy bởi người khác)
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public boolean isEmailTaken(String email, Integer idToExclude) {
        Optional<Manager> existing = managerRepository.findByEmail(email);
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public boolean isPhoneTaken(String phone, Integer idToExclude) {
        Optional<Manager> existing = managerRepository.findByPhoneNumber(phone);
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public List<Manager> searchStaff(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return managerRepository.findAll();
        return managerRepository.search(keyword.trim());
    }



}
