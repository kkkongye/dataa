package cn.hdu.liu.controller;

import cn.hdu.liu.obj.DataObjectRequest;
import cn.hdu.liu.service.DataObjectService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hdu.liu.obj.Result;
import cn.hdu.liu.obj.DataObject;
import cn.hdu.liu.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api")
public class SourceController {

    private static final Logger log = LoggerFactory.getLogger(SourceController.class);


    @Autowired
    private DataObjectService dataObjectService;
    @Autowired
    private SourceService SourceService;
    private String encryptedData;
    private String token;

    @PostMapping("/objects/excel")
    public Result upload(MultipartFile file, HttpSession session) throws IOException {
        log.info("文件上传:()",file);
        String filename = file.getOriginalFilename();

        String origin;
        int extIndex = filename.lastIndexOf(".");
        if (extIndex > 0) {  // 确保文件名有后缀
            origin = filename.substring(0, extIndex);  // 去掉后缀
        } else {
            origin = filename;  // 无后缀则直接使用全名
        }

        int index = filename.lastIndexOf(".");
        String extname = filename.substring(index + 1);

        String uuid = UUID.randomUUID().toString();

        String newFileName = uuid + '.' + extname;
        log.info("新的文件名:()",newFileName);
        file.transferTo(new File("D:\\datasystem\\excel\\"+newFileName));

        DataObject tmpObject= dataObjectService.importFromExcel("D:\\datasystem\\excel\\"+newFileName,origin,uuid);
        session.setAttribute("tmpDataObject", tmpObject);

        return Result.success();
    }


    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        log.info("根据id删除数字对象:{}",id);
        dataObjectService.delete(id);
        return Result.success();
    }

    @GetMapping("/objects/{id}")
    public Result search(@PathVariable String id) {
        DataObject dataObject =  dataObjectService.findById(id);
        return Result.success(dataObject);
    }

    @PostMapping("/objects")
    public Result add(
            @RequestBody DataObjectRequest request,  // 接收三个 JSON 数据
            HttpSession session                       // 从 Session 获取临时对象
    ) {

        DataObject tmpObject = (DataObject) session.getAttribute("tmpDataObject");
        if (tmpObject == null) {
            return Result.error("请先上传 Excel 文件");
        }

        try {

            tmpObject.setConstraintSet(request.getConstraintSet());
            tmpObject.setPropagationControl(request.getPropagationControl());
            tmpObject.setLocationInfo(request.getLocationInfo());


            dataObjectService.saveDataObject(tmpObject);


            session.removeAttribute("tmpDataObject");
            return Result.success("数字对象创建成功");
        } catch (Exception e) {
            log.error("保存失败: ", e);
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @PutMapping("/objects/{id}")
    public Result update(@PathVariable String id, @RequestBody DataObject dataObject) {
        log.info("根据ID修改数字对象: {}, 数据: {}", id, dataObject);
        dataObjectService.update(id, dataObject);
        return Result.success();
    }
    @GetMapping(value = "/objects/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<DataObject>> list() {
        log.info("查询全部数据对象信息");
        List<DataObject> dataObjectList = dataObjectService.findAll();
        return Result.success(dataObjectList);
    }

    @GetMapping("/baogao1")
    public Result baogao1(){
        runPythonScript("empty_check.py");
        log.info("成功生成审查报告1");
        return Result.success();
    }

    @GetMapping("/baogao2")
    public Result baogao2(){
        runPythonScript("cuo.py");
        log.info("成功生成审查报告2");
        return Result.success();
    }

    public static void runPythonScript(String scriptName) {
        try {

            String pythonCommand = "python";


            String workspacePath = "J:/pycharm/projects/project1";
            String scriptPath = workspacePath + File.separator + scriptName;


            ProcessBuilder pb = new ProcessBuilder(
                    pythonCommand,
                    scriptPath
            );


            pb.directory(new File(workspacePath));


            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process process = pb.start();


            int exitCode = process.waitFor();
            log.info("Python脚本执行完毕，退出码: {}", exitCode);
        } catch (IOException | InterruptedException e) {
            log.error("执行Python脚本失败: {}", e.getMessage(), e);
        }
    }



    @GetMapping(value="/selectIds")
    public Result<List<DataObject>> selectIds(@RequestParam String ids) {
        log.info("根据ID列表查询数据对象: {}", ids);

        if (ids == null || ids.trim().isEmpty()) {
            log.info("ID列表不能为空");
        }


        String[] idArray = ids.split("\\s*,\\s*");
        List<DataObject> resultList = new ArrayList<>();

        for (String id : idArray) {
            DataObject dataObject = dataObjectService.findById(id);
            if (dataObject != null) {
                resultList.add(dataObject);
            } else {
                log.warn("ID {} 不存在，已跳过", id);
            }
        }

        return Result.success(resultList);
    }

    @PostMapping("/encrypt")
    public Result encryptData() {
        String rawData = "Sensitive data to be encrypted";
//jiami
        this.encryptedData = "ENCRYPTED_" + rawData + "_DATA";
        this.token = "GENERATED_TOKEN_" + System.currentTimeMillis();
//
        Map<String, String> response = new HashMap<>();
        response.put("status", "Encryption successful");
        response.put("data_length", String.valueOf(encryptedData.length()));
        return Result.success(response);
    }


    @PostMapping("/objects/submit")
    public ResponseEntity<String> sendData() {
        if (encryptedData == null || token == null) {
            return ResponseEntity.badRequest().body("No encrypted data available. Call /encrypt first.");
        }


        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("data", encryptedData);
        requestBody.put("token", token);

        // 发送到数据治理方（假设治理方服务地址为http://localhost:8081）
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8080/governance/receive",
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        this.encryptedData = null;
        this.token = null;

        return ResponseEntity.ok("Data sent to governance system. Response: " + response.getBody());
    }

    @GetMapping("/getToken")
    public Result<String> getToken() {
        String token = "TOKEN_" + UUID.randomUUID().toString();
        log.info("Generated token: {}", token);
        return Result.success(token);
    }
}