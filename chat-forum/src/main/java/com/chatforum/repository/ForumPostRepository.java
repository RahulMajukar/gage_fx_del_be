package com.chatforum.repository;

import com.chatforum.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    @Query("SELECT p FROM ForumPost p LEFT JOIN FETCH p.attachments WHERE p.group.groupId=:groupId ORDER BY p.createdAt ASC")
    List<ForumPost> findByGroupIdWithAttachments(@Param("groupId") Long groupId);
}
