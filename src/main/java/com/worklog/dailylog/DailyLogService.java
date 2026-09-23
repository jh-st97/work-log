package com.worklog.dailylog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.dailylog.dto.DailyLogRequest;
import com.worklog.dailylog.dto.DailyLogResponse;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.tasklog.TaskLogRepository;
import com.worklog.tasklog.dto.TaskLogResponse;

@Service
public class DailyLogService {

	private final DailyLogRepository dailyLogRepository;
	private final MemberRepository memberRepository;
	private final TaskLogRepository taskLogRepository;

	public DailyLogService(DailyLogRepository dailyLogRepository, MemberRepository memberRepository,
			TaskLogRepository taskLogRepository) {
		this.dailyLogRepository = dailyLogRepository;
		this.memberRepository = memberRepository;
		this.taskLogRepository = taskLogRepository;
	}

	// 기간별 목록 조회 (페이징) — Page<DailyLog>를 Page<DailyLogResponse>로 바꿔서 반환
	public Page<DailyLogResponse> getDailyLogs(Long memberId, LocalDate from, LocalDate to, Pageable pageable) {
		return dailyLogRepository.findByMemberIdAndLogDateBetween(memberId, from, to, pageable)
				.map(DailyLogResponse::from);
	}

	// 하루 일지 조회. 아직 기록이 없으면 404 (자동으로 만들어주지 않음 — 저장은 PUT에서만)
	// 회고와 그날의 진행 메모를 같이 보여준다 (기획서 규칙)
	public DailyLogResponse getDailyLog(Long memberId, LocalDate date) {
		DailyLog dailyLog = findMyDailyLog(memberId, date)
				.orElseThrow(() -> new BusinessException(ErrorCode.DAILY_LOG_NOT_FOUND));

		return DailyLogResponse.of(dailyLog, getTaskLogResponses(dailyLog.getId()));
	}

	// 하루 회고 저장 — 있으면 수정, 없으면 새로 만든다 (PUT다운 upsert 동작)
	@Transactional
	public DailyLogResponse saveDailyLog(Long memberId, LocalDate date, DailyLogRequest request) {
		DailyLog dailyLog = findMyDailyLog(memberId, date)
				.map(existing -> {
					existing.updateSummary(request.summary());
					return existing;
				})
				.orElseGet(() -> {
					Member member = memberRepository.findById(memberId)
							.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
					return dailyLogRepository.save(new DailyLog(member, date, request.summary()));
				});

		return DailyLogResponse.of(dailyLog, getTaskLogResponses(dailyLog.getId()));
	}

	// Optional을 그대로 반환한다 — 호출하는 쪽(위 두 메서드)마다 "없을 때" 처리가 달라서
	// (하나는 404, 하나는 새로 만들기) 여기서 미리 예외를 던지지 않는다
	private Optional<DailyLog> findMyDailyLog(Long memberId, LocalDate date) {
		return dailyLogRepository.findByMemberIdAndLogDate(memberId, date);
	}

	private List<TaskLogResponse> getTaskLogResponses(Long dailyLogId) {
		return taskLogRepository.findByDailyLogIdOrderByCreatedAtAsc(dailyLogId).stream()
				.map(TaskLogResponse::from)
				.toList();
	}

}
