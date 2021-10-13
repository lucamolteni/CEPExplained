package kie.live;

import java.util.Date;

import org.kie.api.definition.type.Duration;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("ts")
@Duration("duration")
public class SystemRebootEvent {
    private Date ts;
    private Long duration;
    private Long computerId;
    private Long userId;

    public SystemRebootEvent(Date ts, Long duration, Long computerId, Long userId) {
        this.ts = ts;
        this.duration = duration;
        this.computerId = computerId;
        this.userId = userId;
    }

    public SystemRebootEvent() {
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }
}
