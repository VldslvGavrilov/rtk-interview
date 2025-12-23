package com.rtk.interview;

import lombok.Data;

/**
 * Configuration class to allow {@link ThreadManager} class work with different amount of calculation threads
 */
@Data
public class ThreadConfiguration {

    private final int threadId;
    //Interval for thread to wake and do some calculations
    private final int wakeIntervalInSec;
    //Duration min and max defines the interval for which thread lock(took for calculation) resource
    private final int durationMinInSec;
    private final int durationMaxInSec;
    private final int numberOfResources;
}
