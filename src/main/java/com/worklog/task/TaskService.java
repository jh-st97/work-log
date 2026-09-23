package com.worklog.task;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.project.Project;
import com.worklog.project.ProjectRepository;
import com.worklog.tag.Tag;
import com.worklog.tag.TagRepository;
import com.worklog.tag.dto.TagResponse;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.task.dto.TaskStatusRequest;
import com.worklog.worksystem.WorkSystem;
import com.worklog.worksystem.WorkSystemRepository;
import com.worklog.worksystem.dto.WorkSystemResponse;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final TaskTagRepository taskTagRepository;
	private final TaskWorkSystemRepository taskWorkSystemRepository;
	private final MemberRepository memberRepository;
	private final ProjectRepository projectRepository;
	private final TagRepository tagRepository;
	private final WorkSystemRepository workSystemRepository;

	public TaskService(TaskRepository taskRepository, TaskTagRepository taskTagRepository,
			TaskWorkSystemRepository taskWorkSystemRepository, MemberRepository memberRepository,
			ProjectRepository projectRepository, TagRepository tagRepository,
			WorkSystemRepository workSystemRepository) {
		this.taskRepository = taskRepository;
		this.taskTagRepository = taskTagRepository;
		this.taskWorkSystemRepository = taskWorkSystemRepository;
		this.memberRepository = memberRepository;
		this.projectRepository = projectRepository;
		this.tagRepository = tagRepository;
		this.workSystemRepository = workSystemRepository;
	}

	// 내 업무 목록 (보관 제외). 태그·시스템까지 업무마다 따로 조회한다 —
	// 업무가 많아지면 요청이 여러 번 나가는 비효율(N+1)이 있는데, 이건 나중에(4단계) 개선한다.
	public List<TaskResponse> getTasks(Long memberId) {
		return taskRepository.findByMemberIdAndArchivedAtIsNull(memberId)
				.stream()
				.map(task -> TaskResponse.of(task, getTagResponses(task.getId()), getWorkSystemResponses(task.getId())))
				.toList();
	}

	public TaskResponse getTask(Long id, Long memberId) {
		Task task = findMyTask(id, memberId);
		return TaskResponse.of(task, getTagResponses(id), getWorkSystemResponses(id));
	}

	@Transactional
	public TaskResponse createTask(Long memberId, TaskRequest request) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// 프로젝트가 진짜 내 것인지 확인 (기획서 규칙)
		Project project = findMyProject(request.projectId(), memberId);

		Task task = new Task(member, project, request.title(), request.description(),
				request.priority(), request.dueDate());
		Task saved = taskRepository.save(task);

		// 태그·업무 시스템도 전부 내 것인지 확인한 뒤 연결한다
		List<Tag> tags = resolveTags(request.tagIds(), memberId);
		List<WorkSystem> systems = resolveWorkSystems(request.systemIds(), memberId);
		linkTags(saved, tags);
		linkWorkSystems(saved, systems);

		return TaskResponse.of(saved, tags.stream().map(TagResponse::from).toList(),
				systems.stream().map(WorkSystemResponse::from).toList());
	}

	@Transactional
	public TaskResponse updateTask(Long id, Long memberId, TaskRequest request) {
		Task task = findMyTask(id, memberId);
		Project project = findMyProject(request.projectId(), memberId);

		// 더티 체킹으로 기본 정보만 갱신 (상태는 여기서 안 바꿈)
		task.update(project, request.title(), request.description(), request.priority(), request.dueDate());

		// 태그·업무 시스템 연결은 기존 걸 전부 지우고 요청받은 걸로 새로 만든다
		taskTagRepository.deleteByTaskId(id);
		taskWorkSystemRepository.deleteByTaskId(id);

		List<Tag> tags = resolveTags(request.tagIds(), memberId);
		List<WorkSystem> systems = resolveWorkSystems(request.systemIds(), memberId);
		linkTags(task, tags);
		linkWorkSystems(task, systems);

		return TaskResponse.of(task, tags.stream().map(TagResponse::from).toList(),
				systems.stream().map(WorkSystemResponse::from).toList());
	}

	@Transactional
	public TaskResponse changeStatus(Long id, Long memberId, TaskStatusRequest request) {
		Task task = findMyTask(id, memberId);
		task.changeStatus(request.status());
		return TaskResponse.of(task, getTagResponses(id), getWorkSystemResponses(id));
	}

	@Transactional
	public void archiveTask(Long id, Long memberId) {
		Task task = findMyTask(id, memberId);
		task.archive();
	}

	// 업무를 찾고, 진짜 내 것인지 확인 (Project와 같은 패턴: 없으면 404, 남의 것이면 403)
	private Task findMyTask(Long id, Long memberId) {
		Task task = taskRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.TASK_ACCESS_DENIED);
		}

		return task;
	}

	private Project findMyProject(Long projectId, Long memberId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

		if (!project.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
		}

		return project;
	}

	// 요청받은 태그 번호들이 전부 내 태그인지 확인하면서 엔티티로 바꾼다
	private List<Tag> resolveTags(List<Long> tagIds, Long memberId) {
		if (tagIds == null) {
			return List.of();
		}

		return tagIds.stream()
				.map(tagId -> tagRepository.findByIdAndMemberId(tagId, memberId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND)))
				.toList();
	}

	private List<WorkSystem> resolveWorkSystems(List<Long> systemIds, Long memberId) {
		if (systemIds == null) {
			return List.of();
		}

		return systemIds.stream()
				.map(systemId -> workSystemRepository.findByIdAndMemberId(systemId, memberId)
						.orElseThrow(() -> new BusinessException(ErrorCode.WORK_SYSTEM_NOT_FOUND)))
				.toList();
	}

	private void linkTags(Task task, List<Tag> tags) {
		tags.forEach(tag -> taskTagRepository.save(new TaskTag(task, tag)));
	}

	private void linkWorkSystems(Task task, List<WorkSystem> systems) {
		systems.forEach(system -> taskWorkSystemRepository.save(new TaskWorkSystem(task, system)));
	}

	private List<TagResponse> getTagResponses(Long taskId) {
		return taskTagRepository.findByTaskId(taskId).stream()
				.map(taskTag -> TagResponse.from(taskTag.getTag()))
				.toList();
	}

	private List<WorkSystemResponse> getWorkSystemResponses(Long taskId) {
		return taskWorkSystemRepository.findByTaskId(taskId).stream()
				.map(taskWorkSystem -> WorkSystemResponse.from(taskWorkSystem.getWorkSystem()))
				.toList();
	}

}
