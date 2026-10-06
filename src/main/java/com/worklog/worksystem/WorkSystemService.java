package com.worklog.worksystem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.task.TaskWorkSystemRepository;
import com.worklog.worksystem.dto.WorkSystemRequest;
import com.worklog.worksystem.dto.WorkSystemResponse;

@Service
public class WorkSystemService {

	private final WorkSystemRepository workSystemRepository;
	private final MemberRepository memberRepository;
	private final TaskWorkSystemRepository taskWorkSystemRepository;

	public WorkSystemService(WorkSystemRepository workSystemRepository, MemberRepository memberRepository,
			TaskWorkSystemRepository taskWorkSystemRepository) {
		this.workSystemRepository = workSystemRepository;
		this.memberRepository = memberRepository;
		this.taskWorkSystemRepository = taskWorkSystemRepository;
	}

	// 내 업무 시스템 목록 조회 (페이징)
	public Page<WorkSystemResponse> getWorkSystems(Long memberId, Pageable pageable) {
		return workSystemRepository.findByMemberId(memberId, pageable)
				.map(WorkSystemResponse::from);
	}

	// 업무 시스템 등록
	@Transactional
	public WorkSystemResponse createWorkSystem(Long memberId, WorkSystemRequest request) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (workSystemRepository.existsByMemberIdAndName(memberId, request.name())) {
			throw new BusinessException(ErrorCode.WORK_SYSTEM_DUPLICATED);
		}

		WorkSystem workSystem = new WorkSystem(member, request.name(), request.description());
		WorkSystem saved = workSystemRepository.save(workSystem);
		return WorkSystemResponse.from(saved);
	}

	// 업무 시스템 수정
	@Transactional
	public WorkSystemResponse updateWorkSystem(Long id, Long memberId, WorkSystemRequest request) {
		WorkSystem workSystem = findMyWorkSystem(id, memberId);

		if (workSystemRepository.existsByMemberIdAndNameAndIdNot(memberId, request.name(), id)) {
			throw new BusinessException(ErrorCode.WORK_SYSTEM_DUPLICATED);
		}

		workSystem.update(request.name(), request.description());
		return WorkSystemResponse.from(workSystem);
	}

	// 업무 시스템 삭제 (보관 처리 없이 진짜로 삭제).
	// 업무에 붙어 있던 시스템이면 연결(task_work_system)부터 끊고 지운다 — 업무 자체는 그대로 남는다.
	@Transactional
	public void deleteWorkSystem(Long id, Long memberId) {
		WorkSystem workSystem = findMyWorkSystem(id, memberId);
		taskWorkSystemRepository.deleteByWorkSystemId(id);
		taskWorkSystemRepository.flush();
		workSystemRepository.delete(workSystem);
	}

	// 업무 시스템을 찾고, 진짜 내 것인지 확인하는 공통 로직
	private WorkSystem findMyWorkSystem(Long id, Long memberId) {
		return workSystemRepository.findByIdAndMemberId(id, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.WORK_SYSTEM_NOT_FOUND));
	}

}
