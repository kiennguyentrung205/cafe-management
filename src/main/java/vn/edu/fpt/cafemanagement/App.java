package vn.edu.fpt.cafemanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import vn.edu.fpt.cafemanagement.entities.Shift;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.services.ShiftService;
import vn.edu.fpt.cafemanagement.services.TableService;
import vn.edu.fpt.cafemanagement.services.VoucherService;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);

        Dotenv dotenv = Dotenv.load();
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
    }
}
