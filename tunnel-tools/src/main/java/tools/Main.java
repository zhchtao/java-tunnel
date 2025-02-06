package tools;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMethodCache(basePackages = {"tools.jetcache"})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}