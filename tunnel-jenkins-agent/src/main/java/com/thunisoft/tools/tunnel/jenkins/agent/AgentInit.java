package com.thunisoft.tools.tunnel.jenkins.agent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

/**
 * @Author taotao
 * @Date 2023/7/11 20:53
 */
@Component
@Slf4j
public class AgentInit {
    @Value("${jenkins.server.url:http://127.0.0.1:8081}")
    private String url;
    @Value(("${jenkins.agent.secret}"))
    private String secret;
    @Value(("${jenkins.agent.workDir}"))
    private String workDir;
    @Value("${jenkins.agent.jnlpUrl}")
    private String jnlpUrl;
    @PostConstruct
    private void init() {
        workDir = new File(workDir).getAbsolutePath();
        new Thread(() -> {
            while (true) {
                URLClassLoader cl = null;
                try {
                    String jar = url + "/jnlpJars/agent.jar";
                    log.info("jar file:{}", jar);
                    URL jarUrl = new URL(jar);
                    if (SystemUtils.IS_OS_LINUX) {
                        File jarFile = new File("lib/agent.jar");
                        FileUtils.copyURLToFile(jarUrl, jarFile);
                        jarUrl = jarFile.toURI().toURL();
                    }
                    cl = new URLClassLoader(new URL[]{jarUrl}, null);
                    cl.loadClass("hudson.remoting.Launcher")
                            .getDeclaredMethod("main", String[].class)
                            .invoke(null, new Object[]{new String[]{
                                    "-jnlpUrl",
                                    jnlpUrl,
                                    "-secret",
                                    secret,
                                    "-workDir",
                                    workDir
                            }});
                    break;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        if (null != cl) {

                            cl.close();
                        }
                    } catch (Exception ex) {//NOSONAR
                        log.error(e.getMessage(), e);
                    }
                }
            }

        }).start();
    }
}
