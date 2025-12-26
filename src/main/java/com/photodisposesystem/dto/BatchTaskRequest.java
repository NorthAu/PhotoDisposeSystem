package com.photodisposesystem.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchTaskRequest {

    @NotNull
    private Long userId;

    @NotNull
    private List<TaskRequest> tasks;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<TaskRequest> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskRequest> tasks) {
        this.tasks = tasks;
    }
}
