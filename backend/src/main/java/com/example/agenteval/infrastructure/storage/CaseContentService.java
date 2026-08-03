package com.example.agenteval.infrastructure.storage;

import com.example.agenteval.domain.model.CaseAnswerListPO;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.pojo.CaseFile;
import com.example.agenteval.domain.repository.CaseAnswerListPORespository;
import com.example.agenteval.domain.service.impl.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 文本以 UUID 文件名存储在 MinIO 中。标准答案的每个文件按路径分别上传到 MinIO，
 * 文件的索引信息（caseId + filePath + standardAnswerKey）存储在 case_answer_list 表中。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseContentService {

    private final MinioService minioService;
    private final CaseAnswerListPORespository caseAnswerListRepository;

    /**
     * 从 MinIO 加载案例的 prompt 文本。
     *
     * @param caseItem the evaluation case entity
     * @return prompt text content, or empty string if not found
     */
    public String loadPrompt(EvaluationCasePO caseItem) {
        String key = caseItem.getPromptKey();
        if (key == null || key.isEmpty()) {
            return "";
        }
        try {
            String content = minioService.getAndReadFile(key);
            return content != null ? content : "";
        } catch (Exception e) {
            log.warn("Failed to load prompt for case {} from MinIO key={}", caseItem.getId(), key, e);
            return "";
        }
    }

    /**
     * 从 case_answer_list 表和 MinIO 加载案例的标准答案文件列表。
     *
     * 流程：
     * 1. 查询 case_answer_list 表，获取该案例的所有标准答案记录
     * 2. 按记录中的 standardAnswerKey 从 MinIO 读取文件内容
     * 3. 组装为 List&lt;CaseFile&gt; 返回
     *
     * @param caseItem the evaluation case entity
     * @return list of standard answer files, or empty list if not found
     */
    public List<CaseFile> loadStandardAnswer(EvaluationCasePO caseItem) {
        List<CaseAnswerListPO> records = caseAnswerListRepository.findByCaseId(caseItem.getId());
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        List<CaseFile> files = new ArrayList<>();
        for (CaseAnswerListPO record : records) {
            try {
                String fileKey = record.getStandardAnswerKey();
                if (fileKey == null || fileKey.isEmpty()) {
                    continue;
                }
                InputStream is = minioService.getFile(fileKey);
                byte[] bytes = is.readAllBytes();
                files.add(new CaseFile(record.getFilePath(), new SimpleMultipartFile(record.getFilePath(), bytes)));
            } catch (Exception e) {
                log.warn("Failed to load answer file for case {} path={} key={}",
                        caseItem.getId(), record.getFilePath(), record.getStandardAnswerKey(), e);
            }
        }
        return files;
    }

    /**
     * 将 prompt 文本保存到 MinIO，并将生成的文件名写入 entity.promptKey。
     *
     * @param caseItem   the evaluation case entity
     * @param promptText prompt content to save
     */
    public void savePrompt(EvaluationCasePO caseItem, String promptText) {
        if (promptText == null || promptText.isEmpty()) {
            return;
        }
        try {
            String fileName = minioService.createAndUploadFile(promptText);
            caseItem.setPromptKey(fileName);
            log.info("Saved prompt for case {} → {}", caseItem.getId(), fileName);
        } catch (Exception e) {
            log.warn("Failed to save prompt to MinIO for case {}", caseItem.getId(), e);
            throw new RuntimeException("Failed to save prompt to MinIO", e);
        }
    }

    /**
     * 将标准答案文件逐个上传到 MinIO，并将每个文件的索引信息存入 case_answer_list 表。
     * 采用全量替换策略：先删除 MinIO 中的旧文件 + DB 中的旧记录，再上传新文件 + 插入新记录。
     *
     * @param caseItem  the evaluation case entity
     * @param caseFiles standard answer file list (path + InputStream)
     */
    public void saveStandardAnswer(EvaluationCasePO caseItem, List<CaseFile> caseFiles) {
        if (caseFiles == null || caseFiles.isEmpty()) {
            return;
        }
        try {
            // 1. 查询旧记录，删除 MinIO 中的旧文件
            List<CaseAnswerListPO> oldRecords = caseAnswerListRepository.findByCaseId(caseItem.getId());
            for (CaseAnswerListPO oldRecord : oldRecords) {
                try {
                    minioService.deleteFile(oldRecord.getStandardAnswerKey());
                    log.debug("Deleted old answer file from MinIO: {}", oldRecord.getStandardAnswerKey());
                } catch (Exception e) {
                    log.warn("Failed to delete old answer file from MinIO: key={}",
                            oldRecord.getStandardAnswerKey(), e);
                }
            }

            // 2. 删除 DB 中的旧记录
            if (!oldRecords.isEmpty()) {
                caseAnswerListRepository.deleteByCaseId(caseItem.getId());
            }

            // 3. 逐个上传新文件到 MinIO，并构建新记录
            List<CaseAnswerListPO> records = new ArrayList<>();
            for (CaseFile caseFile : caseFiles) {
                String fileKey = buildAnswerFileKey(caseItem.getId(), caseFile.getPath());
                minioService.uploadFile(fileKey, caseFile.getFile().getInputStream());
                log.debug("Uploaded answer file: {}", fileKey);

                CaseAnswerListPO record = CaseAnswerListPO.builder()
                        .caseId(caseItem.getId())
                        .filePath(caseFile.getPath())
                        .standardAnswerKey(fileKey)
                        .build();
                records.add(record);
            }

            // 4. 批量保存新记录到 case_answer_list 表
            caseAnswerListRepository.saveAll(records);
            log.info("Replaced standard answers for case {}: deleted {} old files, saved {} new files",
                    caseItem.getId(), oldRecords.size(), records.size());
        } catch (Exception e) {
            log.warn("Failed to save standard answers for case {}", caseItem.getId(), e);
            throw new RuntimeException("Failed to save standard answers", e);
        }
    }

    /**
     * 构建标准答案文件在 MinIO 中的 key。
     */
    private String buildAnswerFileKey(Integer caseId, String filePath) {
        return "cases/" + caseId + "/answers/" + filePath;
    }
}
