package cz.rohlik;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

import java.sql.SQLException;

@SpringBootApplication
@EnableRetry
public class RohlikApplication {

    public static void main(String[] args) {
        SpringApplication.run(RohlikApplication.class, args);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile({"!it"})
    public Server h2Server() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
