package com.manlyminotaurs.messaging;

import java.time.LocalDateTime;

class JanitorialRequest extends Request{

    JanitorialRequest(String requestID, String requestType, int priority, Boolean isComplete, Boolean adminConfirm, LocalDateTime startTime, LocalDateTime endTime, String password, String nodeID, String messageID) {
        super(requestID, requestType, priority, isComplete, adminConfirm, startTime,endTime, password, nodeID, messageID);
    }
    @Override
    public boolean service() {
        this.setComplete(true);
        return true;
    }

    //TODO: Delete the request from the DB
    @Override
    public boolean clear() {
        return false;
    }
}
