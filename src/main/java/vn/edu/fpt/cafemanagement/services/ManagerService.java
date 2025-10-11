package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {
    private final ManagerRepository managerRepository;

    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public List<Manager> getAllManagers() {
        return managerRepository.findAll();
    }

    public Optional<Manager> getManagerById(int id) {
        return managerRepository.findById(id);
    }

    public Manager findByUsername(String username) {
        return managerRepository.findByUsername(username).orElse(null);
    }

    public Manager saveManager(Manager manager) {
        return managerRepository.save(manager);
    }

    public void deleteManager(int id) {
        managerRepository.deleteById(id);
    }

    public Manager getDefaultManager() {
        return managerRepository.findAll().isEmpty() ? null : managerRepository.findAll().get(0);
    }

}
