package com.example.agenteval.domain.service;

public interface ExecService {

    String agentInvoke(String workDir, String command, String prompt);

}
