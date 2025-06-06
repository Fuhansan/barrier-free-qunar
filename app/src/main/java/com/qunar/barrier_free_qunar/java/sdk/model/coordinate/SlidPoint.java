package com.qunar.barrier_free_qunar.java.sdk.model.coordinate;

import java.util.Optional;

/**
 * 手势动作轨迹类
 */
public class SlidPoint {
    /**
     * 起始坐标
     */
    private Point start;
    /**
     * 结束坐标
     */
    private Point end;

    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 持续时间
     */

    private Long  duration;

    /**
     * 默认返回手势
     */
    private  static final Point defaultPoint = new Point(0,0);

    public SlidPoint() {
    }

    public SlidPoint(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.startTime = 0L;
        this.duration = 500L;

    }

    public SlidPoint(Point start, Point end, Long startTime, Long duration) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Point getStart() {
        return Optional.ofNullable(start).orElse(defaultPoint);
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return Optional.ofNullable(end).orElse(defaultPoint);
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
