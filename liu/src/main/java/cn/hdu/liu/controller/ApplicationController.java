package cn.hdu.liu.controller;

import cn.hdu.liu.obj.ApplicationRecord;
import cn.hdu.liu.obj.DigitalObjectDisplay;
import cn.hdu.liu.obj.Result;
import cn.hdu.liu.obj.User;
import cn.hdu.liu.service.ApplicationService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // 初始化同步数据（可在应用启动时调用）
    @PostConstruct
    public void init() {
        applicationService.syncAllDataObjects();
    }

    // 用户申请接口
    @PostMapping("/apply/{objectId}")
    public Result applyForObject(
            @PathVariable String objectId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("用户未登录");
        }

        applicationService.applyForObject(objectId, user.getUsername());
        return Result.success("申请已提交");
    }

    // 获取所有数字对象展示
    @GetMapping("/display")
    public Result<List<DigitalObjectDisplay>> getAllDisplayObjects() {
        return Result.success(applicationService.getAllDisplayObjects());
    }

    // 获取所有申请记录（数源方/治理方使用）
    @GetMapping("/records")
    public Result<List<ApplicationRecord>> getAllApplications() {
        return Result.success(applicationService.getAllApplications());
    }

    // 数源方审批接口
    @PostMapping("/source/approve/{recordId}")
    public Result approveBySource(@PathVariable Long recordId) {
        applicationService.approveBySource(recordId);
        return Result.success("数源方已同意");
    }

    // 治理方审批接口
    @PostMapping("/governance/approve/{recordId}")
    public Result approveByGovernance(@PathVariable Long recordId) {
        applicationService.approveByGovernance(recordId);
        return Result.success("治理方已同意");
    }

    // 数源方拒绝接口
    @PostMapping("/source/reject/{recordId}")
    public Result rejectBySource(@PathVariable Long recordId) {
        applicationService.rejectBySource(recordId);
        return Result.success("数源方已拒绝");
    }

    // 治理方拒绝接口
    @PostMapping("/governance/reject/{recordId}")
    public Result rejectByGovernance(@PathVariable Long recordId) {
        applicationService.rejectByGovernance(recordId);
        return Result.success("治理方已拒绝");
    }
}