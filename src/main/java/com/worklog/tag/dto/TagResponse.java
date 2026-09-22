package com.worklog.tag.dto;

import com.worklog.tag.Tag;

public record TagResponse(Long id, String name) {

	public static TagResponse from(Tag tag) {
		return new TagResponse(tag.getId(), tag.getName());
	}

}
