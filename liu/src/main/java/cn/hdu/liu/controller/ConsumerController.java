package cn.hdu.liu.controller;


import cn.hdu.liu.obj.Result;
import cn.hdu.liu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    @Autowired

    private static final String STORAGE_DIR = "./data/";

    @GetMapping("/{id}")
    public String verifyAttributes(@PathVariable Integer id) {
        String result = "ok";
        return result;
    }

    @PostMapping("/receive")
    public Result receiveData(@RequestBody Map<String, String> request) {
        try {
            // 验证必要参数
            if (!request.containsKey("data") || !request.containsKey("token")) {
                return Result.error("Missing required fields: data or token");
            }

            String currentEncryptedData = request.get("data");
            String currentToken = request.get("token");

            // 存储到本地
            String filename = "data_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".dat";
            Path path = Paths.get(STORAGE_DIR + filename);
            Files.createDirectories(path.getParent());

            String content = "Data: " + currentEncryptedData + "\nToken: " + currentToken;
            Files.writeString(path, content, StandardOpenOption.CREATE);

            return Result.success("Data received and stored successfully");
        } catch (IOException e) {
            return Result.error("Storage failed: " + e.getMessage());
        }
    }

    @PostMapping("/decrypt")
    public Result decryptData() {
        try {
            String currentEncryptedData = "及你太美";
            if (!StringUtils.hasText(currentEncryptedData)) {
                return Result.error("No data available for decryption");
            }

            // 加入具体CP-ABE算法 这里示例
            if (!currentEncryptedData.startsWith("ENCRYPTED_") || !currentEncryptedData.endsWith("_DATA")) {
                return Result.error("Invalid data format");
            }

            String decryptedData = currentEncryptedData
                    .replaceFirst("ENCRYPTED_", "")
                    .replaceAll("_DATA$", "");

            Map<String, String> response = new HashMap<>();
            response.put("decrypted_data", decryptedData);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Decryption failed: " + e.getMessage());
        }
    }
}