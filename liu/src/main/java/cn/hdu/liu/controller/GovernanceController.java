package cn.hdu.liu.controller;

import cn.hdu.liu.obj.Result;
import cn.hdu.liu.obj.DataObject;
import cn.hdu.liu.service.GovernanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GovernanceController {


    private static final Logger log = LoggerFactory.getLogger(GovernanceController.class);

    //改
    private static final String STORAGE_DIR = "./data/";
    private String currentEncryptedData;
    private String currentToken;
    @Autowired
    private GovernanceService governanceService;
    private String cleanData;
    private String encryptedData;




    @PostMapping("/receive")
    public Result receiveData(@RequestBody Map<String, String> request) {
        try {
            // 验证必要参数
            if (!request.containsKey("data") || !request.containsKey("token")) {
                return Result.error("Missing required fields: data or token");
            }

            currentEncryptedData = request.get("data");
            currentToken = request.get("token");

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

    @GetMapping("/inspect/{id}")
    public Result inspectTuple(@PathVariable String id) {


        DataObject targetDataObject = governanceService.search(id);
        if (targetDataObject == null) {
            return Result.error("ID对应的数据不存在");
        }

        // 使用tuple中的字段进行校验
        String encryptedData = cleanData;

        Map<String, Object> checks = new HashMap<>();
        checks.put("data_exists", StringUtils.hasText(encryptedData));

        boolean validFormat = encryptedData != null
                && encryptedData.startsWith("ENCRYPTED_")
                && encryptedData.endsWith("_DATA");
        checks.put("valid_format", validFormat);

        boolean allValid = (boolean) checks.get("data_exists")
                && (boolean) checks.get("valid_format");

        return Result.success(Map.of("checks", checks, "valid", allValid));
    }
    @PostMapping("/encrypt11")
    public Result encryptData() {
        String rawData = "Sensitive data to be encrypted";
//jiami
        this.encryptedData = "ENCRYPTED_" + rawData + "_DATA";
//
        Map<String, String> response = new HashMap<>();
        response.put("status", "Encryption successful");
        response.put("data_length", String.valueOf(encryptedData.length()));
        return Result.success(response);
    }

    @PostMapping("/send-data")
    public ResponseEntity<String> sendData() {
        if (encryptedData == null ) {
            return ResponseEntity.badRequest().body("No encrypted data available. Call /encrypt first.");
        }


        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("data", encryptedData);

        // 发送到数源方（假设数源方服务地址为http://localhost:8081）
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/receive-data",
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        this.encryptedData = null;

        return ResponseEntity.ok("Data sent to governance system. Response: " + response.getBody());
    }

}