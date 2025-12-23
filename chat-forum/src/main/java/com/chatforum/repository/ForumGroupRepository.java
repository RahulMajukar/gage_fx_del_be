package com.chatforum.repository;

import com.chatforum.entity.ForumGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumGroupRepository extends JpaRepository<ForumGroup, Long> {

    @Query("SELECT g FROM ForumGroup g WHERE :username MEMBER OF g.members")
    List<ForumGroup> findGroupsByMember(@Param("username") String username);
}