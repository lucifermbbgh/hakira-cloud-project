package com.hakira.ledger.workflow.controller;

import com.hakira.common.pojo.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/workflow/approval")
@Slf4j
@RequiredArgsConstructor
public class ApprovalController {
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;

    /** 启动审批流程 */
    @PostMapping("/start")
    public Result<Map<String, Object>> startApproval(@RequestBody Map<String, Object> params) {
        String voucherNo = (String) params.getOrDefault("voucherNo", "UNKNOWN");
        Map<String, Object> variables = new HashMap<>();
        variables.put("voucherNo", voucherNo);
        variables.put("initiator", params.getOrDefault("initiator", "system"));

        var processInstance = runtimeService.startProcessInstanceByKey(
            "ledger-entry-approval", variables);

        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", processInstance.getId());
        result.put("voucherNo", voucherNo);
        result.put("message", "审批流程已启动");
        return Result.returnSuccess(result);
    }

    /** 查询待审批任务 */
    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> getPendingTasks(
            @RequestParam(defaultValue = "manager") String group) {
        List<Task> tasks = taskService.createTaskQuery()
            .taskCandidateGroup(group).list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("taskName", task.getName());
            item.put("processInstanceId", task.getProcessInstanceId());
            item.put("variables", taskService.getVariables(task.getId()));
            result.add(item);
        }
        return Result.returnSuccess(result);
    }

    /** 完成任务（审批通过/退回） */
    @PostMapping("/complete/{taskId}")
    public Result<String> completeTask(@PathVariable String taskId,
                                        @RequestBody Map<String, Object> vars) {
        taskService.complete(taskId, vars);
        return Result.returnSuccess("任务已完成");
    }
}
