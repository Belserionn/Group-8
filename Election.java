package votingsystem;

import java.time.LocalDateTime;


public class Election {
    public enum Status { PENDING, OPEN, CLOSED }

    private String title;
    private Status status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    public Election(String title, Status status, LocalDateTime openedAt, LocalDateTime closedAt) {
        this.title = title;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
