package com.campus.matching.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Model representing a project join invitation sent to a student.
 * Maps to SQLite table 'invitations'.
 */
public class Invitation {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String roleName;
    private String status = "PENDING";
    private String createdAt;

    public Invitation() {
    }

    public Invitation(Long projectId, Long senderId, Long receiverId, String roleName) {
        this.projectId = projectId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.roleName = roleName;
    }

    public Invitation(Long id, Long projectId, Long senderId, Long receiverId, String roleName, String status) {
        this.id = id;
        this.projectId = projectId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.roleName = roleName;
        this.status = status != null ? status : "PENDING";
    }

    public Invitation(Long id, Long projectId, Long senderId, Long receiverId, String roleName, String status, String createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.roleName = roleName;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    @JsonAlias("project_id")
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getSenderId() {
        return senderId;
    }

    @JsonAlias("sender_id")
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    @JsonAlias("receiver_id")
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getRoleName() {
        return roleName;
    }

    @JsonAlias("role_name")
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return Objects.equals(id, that.id) ||
               (Objects.equals(projectId, that.projectId) &&
                Objects.equals(senderId, that.senderId) &&
                Objects.equals(receiverId, that.receiverId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, senderId, receiverId);
    }

    @Override
    public String toString() {
        return "Invitation{id=" + id + ", projectId=" + projectId + ", senderId=" + senderId + ", receiverId=" + receiverId + ", status='" + status + "'}";
    }
}
