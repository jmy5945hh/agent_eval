package com.example.agenteval.domain.service.impl.exec;

import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.exec.condition.UnixCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service("unix")
@Slf4j
@Conditional(UnixCondition.class)
@RequiredArgsConstructor
public class UnixExecServiceImpl extends ExecBaseAbstractService implements ExecService {

    @Override
    public String agentInvoke(String workDir, String command, String prompt) {
        return "";
    }
}
