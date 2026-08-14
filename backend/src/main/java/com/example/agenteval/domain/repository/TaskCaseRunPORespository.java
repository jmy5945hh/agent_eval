package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.TaskCaseRunPO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskCaseRunPORespository extends BaseRepository<TaskCaseRunPO, Integer>, JpaSpecificationExecutor<TaskCaseRunPO> {

    /**
     * 根据案例 ID 查询所有关联的测评任务运行记录。
     */
    List<TaskCaseRunPO> findByCaseId(int caseId);

    /**
     * 根据任务 ID 查询所有执行记录。
     */
    List<TaskCaseRunPO> findByTaskId(int taskId);

    /**
     * 根据task_id批量查询
     *
     * @param taskIds
     * @return
     */
    List<TaskCaseRunPO> findByTaskIdIn(List<Integer> taskIds);

    TaskCaseRunPO findBySessionId(String sessionId);

    TaskCaseRunPO findFirstByTaskIdAndStatusOrderByCreateTimeDesc(Integer taskId, Integer status);

    TaskCaseRunPO findFirstByTaskIdAndStatusAndEvalStatusOrderByCreateTimeDesc(Integer taskId, Integer status, Integer evalStatus);

    List<TaskCaseRunPO> findByTaskIdAndStatusNot(Integer id, Integer status);
}
