package com.example.agenteval.infrastructure.storage;

import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.pojo.CaseFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves case content (prompt text, standard answer files) from object storage URIs
 * stored on the EvaluationCase entity.
 *
 * Usage:
 *   String prompt = caseContentService.loadPrompt(caseItem);
 *   List<CaseFile> answers = caseContentService.loadStandardAnswer(caseItem);
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseContentService {

    private final ObjectStorageService storageService;

    /**
     * Load the prompt text for a case from object storage.
     *
     * Falls back gracefully:
     * - Returns empty string if promptKey is null
     * - Returns empty string and logs warning if object not found
     *
     * @param caseItem the evaluation case entity
     * @return prompt text content
     */
    public String loadPrompt(EvaluationCasePO caseItem) {
        String key = caseItem.getPromptKey();
        if (key == null || key.isEmpty()) {
            return "";
        }
        String content = storageService.downloadText(key);
        return content != null ? content : "";
    }

    public List<CaseFile> loadStandardAnswer(EvaluationCasePO caseItem) {
        String key = caseItem.getStandardAnswerKey();
        if (key == null || key.isEmpty()) {
            return new ArrayList<>();
        }
        List<CaseFile> files = storageService.downloadJsonList(key, CaseFile.class);
        return files != null ? files : new ArrayList<>();
    }

    public void savePrompt(EvaluationCasePO caseItem, String promptText) {
        String key = buildPromptKey(caseItem.getId());
        String uri = storageService.uploadText(key, promptText);
        caseItem.setPromptKey(uri);
        log.info("Saved prompt for case {} → {}", caseItem.getId(), uri);
    }

    public void saveStandardAnswer(EvaluationCasePO caseItem, List<CaseFile> files) {
        String key = buildStandardAnswerKey(caseItem.getId());
        String uri = storageService.uploadJsonList(key, files != null ? files : new ArrayList<>());
        caseItem.setStandardAnswerKey(uri);
        log.info("Saved standard answer for case {} → {} ({} files)",
                caseItem.getId(), uri, files != null ? files.size() : 0);
    }

    private String buildPromptKey(Integer caseId) {
        return "cases/" + caseId + "/prompt.txt";
    }

    private String buildStandardAnswerKey(Integer caseId) {
        return "cases/" + caseId + "/standard_answer.json";
    }
}
