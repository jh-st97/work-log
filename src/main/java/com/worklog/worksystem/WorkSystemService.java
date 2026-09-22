package com.worklog.worksystem;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.worksystem.dto.WorkSystemRequest;
import com.worklog.worksystem.dto.WorkSystemResponse;

@Service
public class WorkSystemService {

	private final WorkSystemRepository workSystemRepository;
	private final MemberRepository memberRepository;

	public WorkSystemService(WorkSystemRepository workSystemRepository, MemberRepository memberRepository) {
		this.workSystemRepository = workSystemRepository;
		this.memberRepository = memberRepository;
	}

	// 내 업무 시스템 전체 조회
	public List<WorkSystemResponse> getWorkSystems(Long memberId) {
		return workSystemRepository.findByMemberId(memberId)
				.stream()
				.map(WorkSystemResponse::from)
				.toList();
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

	// 업무 시스템 삭제 (보관 처리 없이 진짜로 삭제)
	@Transactional
	public void deleteWorkSystem(Long id, Long memberId) {
		WorkSystem workSystem = findMyWorkSystem(id, memberId);
		workSystemRepository.delete(workSystem);
	}

	// 업무 시스템을 찾고, 진짜 내 것인지 확인하는 공통 로직
	private WorkSystem findMyWorkSystem(Long id, Long memberId) {
		return workSystemRepository.findByIdAndMemberId(id, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.WORK_SYSTEM_NOT_FOUND));
	}

}
