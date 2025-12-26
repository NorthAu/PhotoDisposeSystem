package com.photodisposesystem.dto;

import com.photodisposesystem.model.TaskType;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {

    @NotNull
    private Long imageId;

    @NotNull
    private TaskType taskType;

    private String option;

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
