package dev.challenge.serviceb.feign;

import dev.challenge.common.configuration.ReplicationFeignConfig;
import dev.challenge.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-a", url = "${service.a.url}", configuration = ReplicationFeignConfig.class)
public interface UserAClient {

    @PostMapping("/usuarios")
    UserDTO createUser(@RequestBody UserDTO dto);

    @PutMapping("/usuarios/{id}")
    UserDTO updateUser(@PathVariable("id") String id, @RequestBody UserDTO dto);

    @DeleteMapping("/usuarios/{id}")
    void deleteUser(@PathVariable("id") String id);
}
