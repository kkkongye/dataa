package cn.hdu.liu.controller;

import cn.hdu.liu.blockchain.yuan.contract.Yuan;
import cn.hdu.liu.mapper.UserMapper;
import cn.hdu.liu.obj.*;

import cn.hdu.liu.service.DataObjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdu.Entity.DataCapsule;

import com.hdu.bswabe.BswabePub;
import com.hdu.service.DBService;
import com.hdu.service.DPService;
import com.hdu.service.Impl.DBServiceImpl;
import com.hdu.service.Impl.DPServiceImpl;
import com.thanos.web3j.abi.datatypes.Utf8String;
import com.thanos.web3j.config.SystemConfig;
import com.thanos.web3j.crypto.Credentials;
import com.thanos.web3j.model.ThanosTransactionReceipt;
import com.thanos.web3j.protocol.Web3j;
import com.thanos.web3j.protocol.manage.Web3Manager;
import com.thanos.web3j.utils.ConfigResourceUtil;
import com.thanos.common.crypto.key.asymmetric.SecureKey;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.ls.LSOutput;

import javax.crypto.spec.PSource;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@RestController
@RequestMapping("/api")
public class SourceController {

    private static final Logger log = LoggerFactory.getLogger(SourceController.class);
/**
    static List<String> httpList = List.of("121.36.228.85:8580");
    static List<String> rpcList = List.of("121.36.228.85:8180");

    SystemConfig Config = new SystemConfig(
            1,
            60,
            httpList,
            rpcList,
            false,
            null,
            null,
            "J:\\za\\新建文件夹\\新建文件夹\\liu\\src\\main\\resources\\logback.xml"
    );
    SystemConfig systemConfig = ConfigResourceUtil.loadSystemConfig();
    //ConfigResourceUtil.loadLogConfig(systemConfig.logConfigPath());
    Web3Manager web3Manager = new Web3Manager(systemConfig);
    Web3j web3j = web3Manager.getHttpWeb3jRandomly();
    //SecureKey user = SecureKey.fromPrivate(Hex.decode("010001308f761b30da0baa33457550420bb8938d040a0c6f0582d9351fd5cead86ff11"));
    SecureKey user = SecureKey.getInstance("ECDSA", 1); //随机生成密钥对
    Credentials cred = Credentials.create(user);//封装用户信息

    Yuan yuan1 = Yuan.deploy(
            web3j,
            cred,
            BigInteger.valueOf(1),
            BigInteger.valueOf(3000000),
            BigInteger.valueOf(0)
    ).get();
    String contractAddress = yuan1.getContractAddress();
    Yuan yuan = Yuan.load(
            contractAddress,
            web3j,
            cred,
            BigInteger.ONE,          // Gas Price
            BigInteger.valueOf(3000000) // Gas Limit
    );


    ThanosTransactionReceipt receipt = yuan.register(new Utf8String("123456"), (Set) null).get();
    int ret = receipt.hashCode();

 **/
    DPService dpService = new DPServiceImpl();

    DBService dbService = new DBServiceImpl();

    @Autowired
    private DataObjectService dataObjectService;


    @Autowired
    private UserMapper userMapper;


    private String encryptedData;
    private String token;

    public SourceController() throws Exception {
    }

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
        file.transferTo(new File("J:\\za\\cun\\"+newFileName));

        DataObject tmpObject= dataObjectService.importFromExcel("J:\\za\\cun\\"+newFileName,origin,uuid);
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
    public Result search(@PathVariable String id) throws ExecutionException, InterruptedException {
        DataObject dataObject =  dataObjectService.findById(id);
        /**
        String objectCode = dataObject.getId();
        ThanosTransactionReceipt receipt = yuan.query(new Utf8String(objectCode),(Set) null).get();
        log.info("查询成功!"+receipt);
         **/
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
            if (request.getMetadata() != null && tmpObject.getDataEntity() != null) {
                tmpObject.getDataEntity().setMetadata(request.getMetadata());
            }


            tmpObject.setConstraintSet(request.getConstraintSet());
            tmpObject.setPropagationControl(request.getPropagationControl());
            tmpObject.setLocationInfo(request.getLocationInfo());


            String  objectCode = tmpObject.getId();
            dataObjectService.saveDataObject(tmpObject);
            session.removeAttribute("tmpDataObject");
            /**
            log.info("注册成功！"+ret);

            ThanosTransactionReceipt receipt = yuan.upload(new Utf8String(objectCode),(Set) null).get();
            int ret1 = receipt.hashCode();
            log.info("上传成功!"+ret1);
             **/

            return Result.success("数字对象创建成功");
        } catch (Exception e) {
            log.error("保存失败: ", e);
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @PutMapping("/objects/{id}")
    public Result update(@PathVariable String id, @RequestBody DataObject dataObject) throws ExecutionException, InterruptedException {
        log.info("根据ID修改数字对象: {}, 数据: {}", id, dataObject);
        dataObjectService.update(id, dataObject);
        String objectCode = dataObject.getId();
        /**
        ThanosTransactionReceipt receipt = yuan.modify(new Utf8String(objectCode),(Set) null).get();
        log.info("修改成功!"+receipt);
        **/
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



    @GetMapping("/selectIds")
    public Result<DataCapsule> selectIds(
            @RequestParam String ids
    ) throws ExecutionException, InterruptedException {
        log.info("根据ID列表查询数据对象: {}", ids);

        if (ids == null || ids.trim().isEmpty()) {
           log.error("ID列表不能为空");
        }

        String[] idArray = ids.split("\\s*,\\s*");
        List<DataObject> resultList = new ArrayList<>();
        for (String id : idArray) {
            DataObject dataObject = dataObjectService.findById(id);
            /**
            String objectCode = dataObject.getId();
            ThanosTransactionReceipt receipt = yuan.query(new Utf8String(objectCode),(Set) null).get();
            log.info("查询成功!"+receipt);
             **/
            if (dataObject != null) {
                resultList.add(dataObject);
            } else {
                log.warn("ID {} 不存在，已跳过", id);
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] plainText = objectMapper.writeValueAsBytes(resultList);

            BswabePub bswabePub = new BswabePub();

            dbService.setup(bswabePub);


            // 4. 设置过期时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(2025, Calendar.AUGUST, 15, 10, 30, 0);
            Date timeExpire = calendar.getTime();

            // 5. 访问次数限制
            int visitTime = 100;

            // 6. 构造访问策略
            String duId = "DU_123"; // 数据需求方ID
            String dsId = "DS_456"; // 数源方ID
            String sduId = "SDU_789"; // 数据需求方ID
            String policy = String.format("%s %s %s 3of3", duId, dsId, sduId);

            DataCapsule dataCapsule = dpService.encapsulate(
                    bswabePub,
                    plainText,
                    timeExpire,
                    visitTime,
                    policy,
                    new String[]{duId, dsId, sduId}
            );

            return Result.success(dataCapsule);
        } catch (Exception e) {
            log.error("加密数据失败: ", e);
            DataCapsule dataCapsule1 = new DataCapsule();
            return Result.success(dataCapsule1);
        }
    }

    @PostMapping("/objects/{id}/categories")
    public Result setCategories(
            @PathVariable String id,
            @RequestBody CategoryRequest request
    ) {
        DataObject dataObject = dataObjectService.findById(id);
        if (dataObject == null) {
            return Result.error("ID为 " + id + " 的数字对象不存在");
        }
        try {
            dataObject.setIndustryCategory(request.getIndustryCategory());
            dataObject.setProcessingTimeCategory(request.getProcessingTimeCategory());
            dataObject.setDataSourceCategory(request.getDataSourceCategory());
            dataObjectService.saveDataObject(dataObject);
            return Result.success("分类值设置成功");
        } catch (Exception e) {
            log.error("保存失败: ", e);
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setRoll(request.getRoll());
            userMapper.insert(user);
            return Result.success("用户注册成功");
        } catch (Exception e) {
            log.error("注册失败: ", e);
            return Result.error("用户名已存在或数据格式错误");
        }
    }





    @PostMapping("/objects/{id}/total_values")
    public Result setTotalValues(
            @PathVariable String id,
            @RequestBody TotalValuesRequest request
    ) {
        DataObject dataObject = dataObjectService.findById(id);
        if (dataObject == null) {
            return Result.error("ID为 " + id + " 的数字对象不存在");
        }
        try {
            dataObject.setTotalCategoryValue(request.getTotalCategoryValue());
            dataObject.setTotalGradeValue(request.getTotalGradeValue());
            dataObjectService.saveDataObject(dataObject);
            return Result.success("总分类/分级值设置成功");
        } catch (Exception e) {
            log.error("保存失败: ", e);
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }


    @PostMapping("/setWeights")
    public Result setGradeWeights(@RequestBody WeightRequest request) {
        dataObjectService.setWeights(
                request.getGeneral(),
                request.getImportant(),
                request.getCore()
        );
        return Result.success("分级权重更新成功");
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


