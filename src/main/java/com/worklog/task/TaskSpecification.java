package com.worklog.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

// 업무 목록 조회의 필터 조건들을 하나의 동적 쿼리로 조립한다.
// Specification은 "쿼리를 코드로 짓는" 방식이라, findByXxx처럼 메서드 이름만으로는
// 표현 못 하는 "조건이 있을 때만 걸기"를 할 수 있다 — 파라미터가 null이면 그냥 건너뛴다.
public class TaskSpecification {

	public static Specification<Task> search(Long memberId, TaskStatus status, TaskPriority priority,
			Long projectId, Long systemId, Long tagId, LocalDate dueDateFrom, LocalDate dueDateTo, String keyword) {

		// (root, query, cb) 세 개를 받는 람다 하나가 Specification이다.
		// root: Task 테이블, query: 지금 짓고 있는 쿼리 전체, cb: 조건(=, LIKE, AND 등)을 만드는 도구
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 내 업무 중 보관 안 된 것만 (필터 여부와 상관없이 항상 적용)
			predicates.add(cb.equal(root.get("member").get("id"), memberId));
			predicates.add(cb.isNull(root.get("archivedAt")));

			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (priority != null) {
				predicates.add(cb.equal(root.get("priority"), priority));
			}
			if (projectId != null) {
				predicates.add(cb.equal(root.get("project").get("id"), projectId));
			}
			if (dueDateFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
			}
			if (dueDateTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
			}
			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.get("title")), like),
						cb.like(cb.lower(root.get("description")), like)));
			}

			// 태그·업무 시스템은 Task가 직접 갖고 있는 필드가 아니라 중간 엔티티(TaskTag,
			// TaskWorkSystem)를 통해서만 연결된다. Task 쪽에서 이 관계를 모르게(단방향) 설계했기
			// 때문에, "이 태그/시스템이 달린 업무 id들" 안에 있는지를 서브쿼리로 확인한다.
			if (tagId != null) {
				Subquery<Long> subquery = query.subquery(Long.class);
				Root<TaskTag> taskTagRoot = subquery.from(TaskTag.class);
				subquery.select(taskTagRoot.get("task").get("id"))
						.where(cb.equal(taskTagRoot.get("tag").get("id"), tagId));
				predicates.add(root.get("id").in(subquery));
			}

			if (systemId != null) {
				Subquery<Long> subquery = query.subquery(Long.class);
				Root<TaskWorkSystem> taskWorkSystemRoot = subquery.from(TaskWorkSystem.class);
				subquery.select(taskWorkSystemRoot.get("task").get("id"))
						.where(cb.equal(taskWorkSystemRoot.get("workSystem").get("id"), systemId));
				predicates.add(root.get("id").in(subquery));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
