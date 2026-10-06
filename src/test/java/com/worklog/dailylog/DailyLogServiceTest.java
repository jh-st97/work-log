package com.worklog.dailylog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.dailylog.dto.DailyLogRequest;
import com.worklog.dailylog.dto.DailyLogResponse;
import com.worklog.support.ServiceTestSupport;
import com.worklog.task.dto.TaskResponse;
import com.worklog.tasklog.TaskLogService;
import com.worklog.tasklog.dto.TaskLogRequest;

class DailyLogServiceTest extends ServiceTestSupport {

	@Autowired
	private DailyLogService dailyLogService;
	@Autowired
	private DailyLogRepository dailyLogRepository;
	@Autowired
	private TaskLogService taskLogService;

	private static final LocalDate DAY = LocalDate.of(2026, 9, 23);

	@Test
	@DisplayName("같은 날짜에 회고를 두 번 저장하면 새로 만들지 않고 기존 것을 수정한다 (PUT upsert)")
	void saveDailyLog_twiceOnSameDate_updatesInsteadOfCreating() {
		DailyLogResponse first = dailyLogService.saveDailyLog(owner.getId(), DAY, new DailyLogRequest("처음 회고"));
		DailyLogResponse second = dailyLogService.saveDailyLog(owner.getId(), DAY, new DailyLogRequest("고친 회고"));

		assertThat(second.id()).isEqualTo(first.id());
		assertThat(second.summary()).isEqualTo("고친 회고");
		assertThat(dailyLogRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("기록이 없는 날짜를 조회하면 DAILY_LOG_NOT_FOUND가 발생한다")
	void getDailyLog_whenNotExists_throwsNotFound() {
		assertThatThrownBy(() -> dailyLogService.getDailyLog(owner.getId(), DAY))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.DAILY_LOG_NOT_FOUND);
	}

	@Test
	@DisplayName("다른 회원이 같은 날짜에 쓴 일지는 내 조회에 나오지 않는다")
	void getDailyLog_isScopedToMember() {
		dailyLogService.saveDailyLog(other.getId(), DAY, new DailyLogRequest("남의 회고"));

		assertThatThrownBy(() -> dailyLogService.getDailyLog(owner.getId(), DAY))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.DAILY_LOG_NOT_FOUND);
	}

	@Test
	@DisplayName("하루 일지 조회는 회고와 그날의 진행 메모(업무 제목 포함)를 함께 돌려준다")
	void getDailyLog_includesTaskLogs() {
		TaskResponse task = createTask("배포 자동화");
		dailyLogService.saveDailyLog(owner.getId(), DAY, new DailyLogRequest("오늘 회고"));
		taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "스크립트를 만들었다", 60));

		DailyLogResponse found = dailyLogService.getDailyLog(owner.getId(), DAY);

		assertThat(found.summary()).isEqualTo("오늘 회고");
		assertThat(found.taskLogs()).hasSize(1);
		assertThat(found.taskLogs().get(0).taskTitle()).isEqualTo("배포 자동화");
		assertThat(found.taskLogs().get(0).logDate()).isEqualTo(DAY);
	}

	@Test
	@DisplayName("기간 목록은 범위 안의 일지만, 요청한 정렬·페이지 크기대로 돌려준다")
	void getDailyLogs_filtersByRangeAndPaginates() {
		for (int day = 20; day <= 25; day++) {
			dailyLogService.saveDailyLog(owner.getId(), LocalDate.of(2026, 9, day), new DailyLogRequest("회고 " + day));
		}
		// 남의 일지는 범위 안에 있어도 나오면 안 된다
		dailyLogService.saveDailyLog(other.getId(), LocalDate.of(2026, 9, 22), new DailyLogRequest("남의 회고"));

		Page<DailyLogResponse> page = dailyLogService.getDailyLogs(owner.getId(), LocalDate.of(2026, 9, 21),
				LocalDate.of(2026, 9, 24), PageRequest.of(0, 2, Sort.by("logDate").descending()));

		// 21~24일 = 4개, 한 페이지에 2개씩 → 최신순으로 24, 23일
		assertThat(page.getTotalElements()).isEqualTo(4);
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.getContent()).extracting(DailyLogResponse::summary).containsExactly("회고 24", "회고 23");
		// 목록 조회는 진행 메모를 안 채운다(빈 목록)
		assertThat(page.getContent().get(0).taskLogs()).isEmpty();
	}

}
