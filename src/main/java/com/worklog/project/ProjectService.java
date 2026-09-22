package com.worklog.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.project.dto.ProjectRequest;
import com.worklog.project.dto.ProjectResponse;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final MemberRepository memberRepository;

	public ProjectService(ProjectRepository projectRepository, MemberRepository memberRepository) {
		this.projectRepository = projectRepository;
		this.memberRepository = memberRepository;
	}

	// 내 프로젝트 목록 조회 (보관된 것 제외)
	public List<ProjectResponse> getProjects(Long memberId) {
		return projectRepository.findByMemberIdAndArchivedAtIsNull(memberId)
				.stream()
				.map(ProjectResponse::from) // Project 엔티티 목록을 ProjectResponse 목록으로 하나씩 변환
				.toList();
	}

	// 프로젝트 상세 조회 (내 것인지 확인 포함)
	public ProjectResponse getProject(Long id, Long memberId) {
		Project project = findMyProject(id, memberId);
		return ProjectResponse.from(project);
	}

	// 프로젝트 등록
	@Transactional
	public ProjectResponse createProject(Long memberId, ProjectRequest request) {
		// 토큰에 들어있는 회원 번호로 실제 회원을 찾는다 (없으면 404)
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		Project project = new Project(member, request.name(), request.description(),
				request.startDate(), request.endDate());

		Project saved = projectRepository.save(project);
		return ProjectResponse.from(saved);
	}

	// 프로젝트 수정
	@Transactional
	public ProjectResponse updateProject(Long id, Long memberId, ProjectRequest request) {
		Project project = findMyProject(id, memberId);
		// save()를 따로 부르지 않아도, 트랜잭션이 끝날 때 바뀐 값이 자동으로 UPDATE된다 (더티 체킹)
		project.update(request.name(), request.description(), request.startDate(), request.endDate());
		return ProjectResponse.from(project);
	}

	// 프로젝트 보관 처리 (진짜로 삭제하지 않음)
	@Transactional
	public void archiveProject(Long id, Long memberId) {
		Project project = findMyProject(id, memberId);
		project.archive();
	}

	// 프로젝트를 찾고, 그게 진짜 내 것인지까지 확인하는 공통 로직
	// (상세 조회, 수정, 보관 처리에서 다 같이 쓴다)
	private Project findMyProject(Long id, Long memberId) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND)); // 없으면 404

		if (!project.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED); // 남의 것이면 403
		}

		return project;
	}

}
