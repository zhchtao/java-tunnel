package tools.jetcache.service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import org.springframework.stereotype.Service;
import tools.jetcache.dto.User;

import java.util.concurrent.TimeUnit;

/**
 * @Author taotao
 * @Date 2024/11/21 17:41
 */
@Service
public class UserService {
    @Cached(name = "userRightSysIds:", key = "#name", timeUnit = TimeUnit.DAYS, expire = 1, area = "spel:#args[0]")
    public User get (String name) {
        return User.generic(name);
    }
    @CacheUpdate(name = "userRightSysIds:", key = "#name", value = "#user", condition = "#user != null", area = "spel:#args[0]")
    public void update(String name, User user) {
    }
}
