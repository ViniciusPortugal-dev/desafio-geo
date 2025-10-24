package dev.challenge.servicea.feign;

import dev.challenge.common.configuration.ReplicationFeignConfig;
import dev.challenge.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-b", url = "${service.b.url}", configuration = ReplicationFeignConfig.class)
public interface UserBClient {

    @PostMapping("/usuarios")
    UserDTO createUser(@RequestBody UserDTO dto);

    @PutMapping("/usuarios/{id}")
    UserDTO updateUser(@PathVariable("id") String id, @RequestBody UserDTO dto);

    @DeleteMapping("/usuarios/{id}")
    void deleteUser(@PathVariable("id") String id);

}
