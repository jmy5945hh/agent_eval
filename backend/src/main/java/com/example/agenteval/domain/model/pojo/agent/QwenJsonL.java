package com.example.agenteval.domain.model.pojo.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenJsonL {

    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("parentUuid")
    private String parentUuid;
    @JsonProperty("sessionId")
    private String sessionId;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("type")
    private String type;
    @JsonProperty("provenance")
    private String provenance;
    @JsonProperty("cwd")
    private String cwd;
    @JsonProperty("version")
    private String version;
    /*@JsonProperty("message")
    private MessageDTO message;*/
    @JsonProperty("subtype")
    private String subtype;
    /*@JsonProperty("systemPayload")
    private SystemPayloadDTO systemPayload;*/
    @JsonProperty("model")
    private String model;
    @JsonProperty("usageMetadata")
    private UsageMetadataDTO usageMetadata;
    @JsonProperty("contextWindowSize")
    private Integer contextWindowSize;
    /*@JsonProperty("toolCallResult")
    private ToolCallResultDTO toolCallResult;*/

    @NoArgsConstructor
    @Data
    public static class MessageDTO {
        @JsonProperty("role")
        private String role;
        @JsonProperty("parts")
        private List<PartsDTO> parts;

        @NoArgsConstructor
        @Data
        public static class PartsDTO {
            @JsonProperty("text")
            private String text;
        }
    }

    @NoArgsConstructor
    @Data
    public static class SystemPayloadDTO {
        @JsonProperty("snapshot")
        private SnapshotDTO snapshot;

        @NoArgsConstructor
        @Data
        public static class SnapshotDTO {
            @JsonProperty("type")
            private String type;
            @JsonProperty("version")
            private Integer version;
            @JsonProperty("surface")
            private String surface;
            @JsonProperty("fileStates")
            private FileStatesDTO fileStates;
            @JsonProperty("promptCount")
            private Integer promptCount;
            @JsonProperty("promptCountAtLastCommit")
            private Integer promptCountAtLastCommit;

            @NoArgsConstructor
            @Data
            public static class FileStatesDTO {
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class UsageMetadataDTO {
        @JsonProperty("promptTokenCount")
        private Integer promptTokenCount;
        @JsonProperty("candidatesTokenCount")
        private Integer candidatesTokenCount;
        @JsonProperty("thoughtsTokenCount")
        private Integer thoughtsTokenCount;
        @JsonProperty("totalTokenCount")
        private Integer totalTokenCount;
        @JsonProperty("cachedContentTokenCount")
        private Integer cachedContentTokenCount;
    }

    @NoArgsConstructor
    @Data
    public static class ToolCallResultDTO {
        @JsonProperty("callId")
        private String callId;
        @JsonProperty("status")
        private String status;
        @JsonProperty("resultDisplay")
        private ResultDisplayDTO resultDisplay;
        @JsonProperty("executionStatus")
        private String executionStatus;

        @NoArgsConstructor
        @Data
        public static class ResultDisplayDTO {
            @JsonProperty("type")
            private String type;
            @JsonProperty("subagentName")
            private String subagentName;
            @JsonProperty("taskDescription")
            private String taskDescription;
            @JsonProperty("taskPrompt")
            private String taskPrompt;
            @JsonProperty("status")
            private String status;
            @JsonProperty("toolCalls")
            private List<?> toolCalls;
        }
    }
}
