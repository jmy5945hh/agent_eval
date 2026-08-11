package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.TaskCaseRunPO;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskCaseRunPORespository extends BaseRepository<TaskCaseRunPO, Integer> {

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

    @Query("SELECT t FROM TaskCaseRunPO t WHERE  t.taskId = :taskId and t.status=1 order by t.createTime desc limit 1")
    TaskCaseRunPO finByNextRunCase(Integer taskId);
}
