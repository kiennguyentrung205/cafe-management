    package vn.edu.fpt.cafemanagement.services;
    
    import jakarta.transaction.Transactional;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import vn.edu.fpt.cafemanagement.entities.Manager;
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
    
        public Page<Manager> findByIsActiveTrue(Pageable pageable) {
            return managerRepository.findAll(pageable);
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
    
        public Page<Manager> getActiveStaffs(Pageable pageable) {
            return managerRepository.findByIsActiveTrue(pageable);
        }
    
        public Page<Manager> getDeletedStaffs(Pageable pageable) {
            return managerRepository.findByIsActiveFalse(pageable);
        }
    
        public Page<Manager> searchStaff(String keyword, Pageable pageable) {
            return managerRepository.search(keyword.trim(), pageable);
        }
    
    
    
        public List<Manager> getActiveStaffs() {
            return managerRepository.findByIsActiveTrue();
        }
    
        public List<Manager> getDeletedStaffs() {
            return managerRepository.findByIsActiveFalse();
        }
    
        @Transactional
        public void softDelete(int id) {
            Manager staff = managerRepository.findById(id).orElse(null);
            if (staff != null) {
                staff.setActive(false);
                managerRepository.save(staff);
            }
        }
    
        @Transactional
        public void hardDelete(int id) {
            managerRepository.deleteById(id);
        }
    
        @Transactional
        public void restore(int id) {
            Manager staff = managerRepository.findById(id).orElse(null);
            if (staff != null) {
                staff.setActive(true);
                managerRepository.save(staff);
            }
        }
    }
