package cn.hdu.liu.controller;

import com.hdu.service.DUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.hdu.liu.obj.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(SourceController.class);



    @PostMapping("/a")
    public Result upload(MultipartFile file) throws IOException {
        log.info("文件上传:()",file);
        String filename = file.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        String extname = filename.substring(index + 1);
        String newFileName = UUID.randomUUID().toString() + extname;
        log.info("新的文件名:()",newFileName);
        file.transferTo(new File("J:\\za\\cun\\"+newFileName));

        return Result.success();
    }
}


