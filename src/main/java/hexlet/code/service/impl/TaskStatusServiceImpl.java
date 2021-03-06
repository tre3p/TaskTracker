package hexlet.code.service.impl;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.exceptions.NotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private TaskStatusRepository taskStatusRepository;

    @Override
    public TaskStatus createTaskStatus(TaskStatusDto taskStatusDto) {
        return taskStatusRepository.save(new TaskStatus(
                taskStatusDto.name()
        ));
    }

    @Override
    public TaskStatus updateTaskStatus(TaskStatusDto taskStatusDto, Long id) {
        return taskStatusRepository.save(taskStatusRepository.findById(id)
                .map(t -> {
                    t.setName(taskStatusDto.name());
                    return t;
                })
                .orElseThrow(() -> new NotFoundException("Task status with such ID not found")));
    }
}
