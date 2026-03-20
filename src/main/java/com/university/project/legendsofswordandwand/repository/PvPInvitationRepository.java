package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.PvPInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PvPInvitationRepository extends JpaRepository<PvPInvitation, Long> {

  boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

  @Query("SELECT i FROM PvPInvitation i WHERE i.receiver.username = :username AND i.status = 'PENDING'")
  List<PvPInvitation> findPendingByReceiverUsername(@Param("username") String username);

  @Query("SELECT i FROM PvPInvitation i WHERE i.sender.username = :username AND i.status = 'PENDING'")
  List<PvPInvitation> findPendingBySenderUsername(@Param("username") String username);
}
