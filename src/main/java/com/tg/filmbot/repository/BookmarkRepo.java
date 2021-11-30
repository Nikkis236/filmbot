package com.tg.filmbot.repository;

import com.tg.filmbot.entity.Bookmark;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepo extends CrudRepository<Bookmark, Long> {
}
