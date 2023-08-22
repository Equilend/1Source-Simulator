package com.equilend.simulator;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventsProcessor {
    private User user;
    private Mode mode;
    private Long waitInterval;
    private static final Logger logger = LogManager.getLogger();

    public EventsProcessor(User user, Configurator configurator){
        this.user = user;
        this.mode = configurator.getMode();
        this.waitInterval = configurator.getWaitInterval();
    }

    private void processTradeEvent(Event event){
        logger.info("Processing Trade Event");
        /*
         * a lender bot considers whether to propose a contract 
         */
        if (mode == Mode.LENDER){
            System.out.println("Lender bot proposes contract from trade");
            String uri = event.getResourceUri();
            String[] arr = uri.split("/");
            String agreementId = arr[arr.length-1];
            user.considerProposingContract(agreementId);
        }
    }

    private void processContractEvent(Event event){
        logger.info("Processing Propose Contract Event");
        /*
         *  a borrower bot considers whether to accept or decline
         */
        if (mode == Mode.BORROWER){
            System.out.println("Borrower bot considers whether to approve or decline contract proposal");
            String uri = event.getResourceUri();
            String[] arr = uri.split("/");
            String contractId = arr[arr.length-1];
            user.considerContractProposal(contractId);
        }
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
        if (user == null){
            logger.error("Unable to listen");
            return;
        }
        Token token = user.getToken();
        OffsetDateTime since = APIConnector.getCurrentTime();
        OffsetDateTime before;
        while (true){
            waitMillisecs(waitInterval);
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
            if (events.size() == 0){
                logger.info("No new events");
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
