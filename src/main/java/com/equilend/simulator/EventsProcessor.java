package com.equilend.simulator;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventsProcessor {
    //maybe just have one user that is either a lender or borrower
    private User lender;
    private User borrower;
    private static final Logger logger = LogManager.getLogger();

    public EventsProcessor(){
        this.lender = null;
        this.borrower = null;
    }

    public User getLender() {
        return lender;
    } 

    public void setLender(User lender) {
        this.lender = lender;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    private void processTradeEvent(Event event){
        logger.info("Processing Trade Event");
        /*
         * a lender bot considers whether to propose a contract 
         */
    }

    private void processContractEvent(Event event){
        logger.info("Processing Propose Contract Event");
        /*
         *  a borrower bot considers whether to accept or decline
         */
    }
    
    private void processContractCancelEvent(Event event){
        logger.info("Processing Contract Cancel Event");
        /*
         * a borrower bot is sad, or possibly slightly relieved
         */
    }

    private void processContractApproveEvent(Event event){
        logger.info("Processing Contract Approve Event");
        /*
         * a lender bot celebrates!
         */
    }
    
    private void processContractDeclineEvent(Event event){
        logger.info("Processing Contract Decline Event");
        /*
         * a lender bot wonders where it all went wrong
         */
    }

    private void processEvents(List<Event> events){
        for (Event event : events){
            switch (event.getEventType()){
                case "TRADE":   processTradeEvent(event);
                                break;
                case "CONTRACT":    processContractEvent(event);
                                    break;
                case "CONTRACT_CANCEL": processContractCancelEvent(event);
                                        break;
                case "CONTRACT_APPROVE":    processContractApproveEvent(event);
                                            break;
                case "CONTRACT_DECLINE":    processContractDeclineEvent(event);
                                            break;
                default:  logger.warn("Functionality not yet supported.");
                                break;
            }
        }
    }
    
    public void listen(){
        if (lender == null){
            logger.error("Unable to listen");
            return;
        }
        Token token = lender.getToken();
        OffsetDateTime since = APIConnector.getCurrentTime();
        OffsetDateTime before;
        while (true){
            waitMillisecs(5000L);
            before = APIConnector.getCurrentTime();
            List<Event> events;
            try{
                events = APIConnector.getAllEvents(token, since, before);
                processEvents(events);
                since = before;
            } catch(APIException e){
                logger.error("Error getting events", e);
                return;
            }
        }
    }
        
    public static void waitMillisecs(Long interval){
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e ){
            e.printStackTrace();
        }
    }

}
