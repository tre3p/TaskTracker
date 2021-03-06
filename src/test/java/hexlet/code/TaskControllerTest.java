package hexlet.code;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.UsersController.ID;
import static hexlet.code.utils.TestUtils.BASE_API_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static hexlet.code.controller.TaskController.BY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DBRider
@DataSet("test-data.yml")
@AutoConfigureMockMvc
public class TaskControllerTest {

    private static String taskToPatchJson;

    @Autowired
    private TestUtils testUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    @BeforeAll
    void init() throws IOException {
        taskToPatchJson = testUtils.readFileContent(TestUtils.FIXTURES_PATH + "taskToPatch.json");
    }

    @Test
    void testCreateTask() throws Exception {
        assertThat(taskRepository.findAll().size()).isEqualTo(2);
        testUtils.regDefaultTask();
        assertThat(taskRepository.findAll().size()).isEqualTo(3);
    }

    @Test
    void testGetTaskById() throws Exception {
        User user = userRepository.findAll().get(0);

        MockHttpServletResponse getResp = testUtils.perform(
                get(BASE_API_URL + TASK_CONTROLLER_PATH + ID, 1),
                user.getEmail()
        ).andReturn().getResponse();

        String body = getResp.getContentAsString();

        assertThat(getResp.getStatus()).isEqualTo(200);
        assertThat(body).contains("First Test Task");
    }

    @Test
    void testChangeTaskData() throws Exception {
        User user = userRepository.findAll().get(0);
        Task task = taskRepository.findAll().get(0);

        MockHttpServletResponse resp = testUtils.perform(
                put(BASE_API_URL + TASK_CONTROLLER_PATH + ID, task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskToPatchJson),
                user.getEmail()
        ).andReturn().getResponse();

        String body = resp.getContentAsString();

        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(body).contains("Patched Task");
    }

    @Test
    void testGetAllTasks() throws Exception {
        MockHttpServletResponse resp = testUtils.perform(
                get(BASE_API_URL + TASK_CONTROLLER_PATH)
        ).andReturn().getResponse();

        assertThat(resp.getStatus()).isEqualTo(200);
    }

    @Test
    void testFilterTasks1() throws Exception {
        User user = userRepository.findAll().get(0);
        MockHttpServletResponse resp = testUtils.perform(
                get(BASE_API_URL + TASK_CONTROLLER_PATH + BY + "?taskStatus=2&executorId=50"),
                user.getEmail()
        ).andReturn().getResponse();

        String body = resp.getContentAsString();

        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(body).contains("Second Test Task");
        assertThat(body).doesNotContain("First Test Task");
    }

    @Test
    void testDeleteTaskById() throws Exception {
        User user = userRepository.findById(20L).get();
        Task task = taskRepository.findById(1L).get();

        MockHttpServletResponse resp = testUtils.perform(
                delete(BASE_API_URL + TASK_CONTROLLER_PATH + ID, task.getId()),
                user.getEmail()
        ).andReturn().getResponse();

        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(taskRepository.findById(task.getId())).isNotPresent();
    }

    @Test
    void testUserCantDeleteNotHisOwnTask() throws Exception {
        User user = userRepository.findById(20L).get();
        Task task = taskRepository.findById(2L).get();

        MockHttpServletResponse resp = testUtils.perform(
                delete(BASE_API_URL + TASK_CONTROLLER_PATH + ID, task.getId()),
                user.getEmail()
        ).andReturn().getResponse();

        assertThat(resp.getStatus()).isEqualTo(401);
        assertThat(taskRepository.findById(task.getId())).isPresent();
    }
}
