package com.university.project.legendsofswordandwand.service;

public interface IPvPService {

  void createInvitation(String senderUsername, String receiverUsername);

  void acceptInvitation(Long inviteId);
}
