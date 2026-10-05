package com.humanvision.checkbox.service.logging;

import org.slf4j.Logger;

public final class RejectedInputLog {
    private RejectedInputLog() {}

    public static void warn(Logger log, String problem, Exception cause) {
        log.atWarn().addKeyValue("cause", cause.toString()).log(problem);
        log.atDebug().setCause(cause).log("{}: details", problem);
    }
}
