package com.equilend.simulator;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.lang.Thread;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers; 
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Settlement.Instruction.Instruction;
import com.equilend.simulator.Settlement.Instruction.LocalMarketFields;
import com.equilend.simulator.Trade.Currency;
import com.equilend.simulator.Trade.SettlementType;
import com.equilend.simulator.Trade.Trade;
import com.equilend.simulator.Trade.Collateral.Collateral;
import com.equilend.simulator.Trade.Collateral.CollateralType;
import com.equilend.simulator.Trade.Collateral.RoundingMode;
import com.equilend.simulator.Trade.ExecutionVenue.ExecutionVenue;
import com.equilend.simulator.Trade.ExecutionVenue.Platform;
import com.equilend.simulator.Trade.ExecutionVenue.VenueType;
import com.equilend.simulator.Trade.ExecutionVenue.VenueParty.VenueParty;
import com.equilend.simulator.Trade.Instrument.Instrument;
import com.equilend.simulator.Trade.Rate.Rate;
import com.equilend.simulator.Trade.TransactingParty.Party;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Simulator 
{
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    private static LocalDate today = LocalDate.now();
    private static String todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
    private static LocalDate tPlus2 = today.plusDays(2);
    private static String tPlus2Str = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(tPlus2);

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException
    {
        String lenderFilename = "src/main/java/com/equilend/simulator/lender_config.txt";
        String borrowerFilename = "src/main/java/com/equilend/simulator/borrower_config.txt";
        User lender = new User(lenderFilename, PartyRole.LENDER);
        List<ContractProposalResponse> responses = lender.proposeContractsFromAgreements(null,"TBORR-US");
        List<String> contractIds = responses.stream()
                                            .map(response -> response.getContractId())
                                            .collect(Collectors.toList());
        contractIds.forEach(System.out::println);
        User borrower = new User(borrowerFilename, PartyRole.BORROWER);
    }

    public static void simulateLendRequests (int numProposals, Long intervalInMillisecs) throws URISyntaxException, IOException, InterruptedException
    {
        System.out.println("Start simulation");
  
        Token token = getBearerToken();
        for (int i = 0; i < numProposals; i++){
            ContractProposal contract = createContractProposal();
            postContractProposal(token, contract);
            wait(intervalInMillisecs);
        }
        System.out.println("Done");

    }
    
    public static void wait(Long interval) throws InterruptedException{
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e ){
            e.printStackTrace();
        }
    }

    public static Map<String, String> readFormData (String filename) throws FileNotFoundException{
        Map<String, String> formData = new HashMap<>();
        try{
                Scanner scanner = new Scanner(new File(filename));
                while (scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    String[] keyValuePair = line.split("=");
                    formData.put(keyValuePair[0], keyValuePair[1]);
                }
                scanner.close();
        } catch (FileNotFoundException e){
            throw new FileNotFoundException("config.txt not found");
        }
        return formData;
    }

    public static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    public static Token getBearerToken() throws URISyntaxException, IOException, InterruptedException
    {
        Map<String, String> formData = readFormData("src/main/java/com/equilend/simulator/lender_config.txt");

        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageauth.equilend.com/auth/realms/1Source/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(getFormDataAsString(formData)))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());

        return gson.fromJson(postResponse.body(), Token.class);
    }
    
    public static void proposeContractsFromAgreementsBetween(String lenderPartyId, String borrowerPartyId) throws URISyntaxException, IOException, InterruptedException{
        List<Agreement> agreements = getAllAgreements();
        for (Agreement agreement : agreements){
            Boolean betweenTlenAndTborr = true;
            Trade t = agreement.getTrade();
            Set<String> set = new HashSet<>();
            set.add(lenderPartyId);
            set.add(borrowerPartyId);
            for (TransactingParty p : t.getTransactingParties()){
                if (!set.contains(p.getParty().getPartyId())){
                    betweenTlenAndTborr = false;
                }
            }
            if (betweenTlenAndTborr){
                System.out.println("proposing contract... " + t.getInstrument().getTicker());
                postContractProposal(getBearerToken(), createContractProposal(t));
            }
        }
    }

    public static ContractProposalResponse postContractProposal(Token token, ContractProposal contract) throws URISyntaxException, IOException, InterruptedException
    {
        String contractJson = gson.toJson(contract);

        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .POST(BodyPublishers.ofString(contractJson))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        return gson.fromJson(postResponse.body(), ContractProposalResponse.class);
    }

    public static Trade createGenericTrade(){
        Platform platform = new Platform("X", "Phone Brokered", "EXTERNAL", "0");
        List<VenueParty> venueParties = new ArrayList<>();
        VenueParty lenderVenueParty = new VenueParty(PartyRole.LENDER);
        venueParties.add(lenderVenueParty);
        VenueParty borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        venueParties.add(borrowerVenueParty);
        ExecutionVenue executionVenue = new ExecutionVenue(VenueType.OFFPLATFORM, platform, venueParties);
        Instrument instrument = new Instrument("MSFT", "594918104", "US5949181045", "2588173", "BBG001S5TD05", "MICROSOFT CORP COM" );
        Rate rate = new Rate(BigDecimal.valueOf(.125));
        Long quantity = Long.valueOf(25025);
        Currency billingCurrency = Currency.USD;
        BigDecimal dividendRatePct = BigDecimal.valueOf(100);
        String tradeDate = todayStr;
        String settlementDate = tPlus2Str;
        SettlementType settlementType = SettlementType.DVP;
        Collateral collateral = new Collateral(BigDecimal.valueOf(8758750), BigDecimal.valueOf(8933925), Currency.USD,
        CollateralType.CASH, 10, RoundingMode.ALWAYSUP, BigDecimal.valueOf(102));
        List<TransactingParty> transactingParties = new ArrayList<>();
        TransactingParty lenderTransactingParty = new TransactingParty();
        lenderTransactingParty.setPartyRole(PartyRole.LENDER);
        Party lenderParty = new Party("TLEN-US", "Test Lender US", "KTB500SKZSDI75VSFU40");
        lenderTransactingParty.setParty(lenderParty);
        transactingParties.add(lenderTransactingParty);
        TransactingParty borrowingTransactingParty = new TransactingParty();
        borrowingTransactingParty.setPartyRole(PartyRole.BORROWER);
        Party borrowerParty = new Party("TBORR-US", "Test Borrower US", "KTB500SKZSDI75VSFU40");
        borrowingTransactingParty.setParty(borrowerParty);
        transactingParties.add(borrowingTransactingParty);

        return new Trade(executionVenue, instrument, rate, quantity, billingCurrency, dividendRatePct, tradeDate, settlementDate, settlementType, collateral, transactingParties);
    }

    public static Settlement createGenericSettlement(PartyRole role){
        List<LocalMarketFields> localMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields localMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        localMarketFieldsList.add(localMarketFields);
        Instruction instruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", localMarketFieldsList);
        return new Settlement(role, instruction);          
    }

    /*
     * ADD: Get current date for trade date & settlement date
     * ADD: Randomize selection of defined financial instruments w info hard coded.. (MFST, AMZN, F, AAPL, etc)
     * ADD: define differences for borrower and lender... (can borrower propose contract?..)
     * ADD: Potentially accept a contract id to model the simulated contract proposals after...
     */
    public static ContractProposal createContractProposal(){
        Trade trade = createGenericTrade();

        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createGenericSettlement(PartyRole.LENDER));

        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;
    }

    public static ContractProposal createContractProposal(Trade trade){
        trade.getCollateral().setRoundingRule(10);
        trade.getCollateral().setRoundingMode(RoundingMode.ALWAYSUP);
        trade.getCollateral().setMargin(BigDecimal.valueOf(102));
        
        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createGenericSettlement(PartyRole.LENDER));
        
        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;        
    }

    public static List<Party> getAllParties (Token token) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/parties"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());

        Type partyListType = new TypeToken<ArrayList<Party>>(){}.getType();
        return gson.fromJson(getResponse.body(), partyListType);
    }

    public static Party getPartyById (Token token, String id) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/parties" + "/" + id))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        
        Type partyListType = new TypeToken<ArrayList<Party>>(){}.getType();
        ArrayList<Party> partyList = gson.fromJson(getResponse.body(), partyListType);
        return partyList.get(0);
    }

    public static List<Event> getAllEvents (Token token) throws URISyntaxException, IOException, InterruptedException
    {
        String time = URLEncoder.encode("2023-07-01T00:00:00-04:00", java.nio.charset.StandardCharsets.UTF_8.toString());
        
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/events" + "?" + "since=" + time))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        
        Type eventListType = new TypeToken<ArrayList<Event>>(){}.getType();
        return gson.fromJson(getResponse.body(), eventListType);
    }

    public static List<Agreement> getAllAgreements() throws URISyntaxException, IOException, InterruptedException
    {
        Token token = getBearerToken();
        String time = URLEncoder.encode(todayStr+"T00:00:00-04:00", java.nio.charset.StandardCharsets.UTF_8.toString());
        
        HttpRequest getRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/agreements" + "?" + "since=" + time))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
        
        Type agreementListType = new TypeToken<ArrayList<Agreement>>(){}.getType();
        return gson.fromJson(getResponse.body(), agreementListType);
    }
}
