package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.PvPInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PvPInvitationRepository extends JpaRepository<PvPInvitation, Long> {

  boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
