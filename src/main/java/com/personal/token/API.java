package com.personal.token;

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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



/**
 * Hello world!
 *
 */
public class API 
{
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static Gson gson = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException
    {
        //Parse command line arguments 
        //Add more argument checking...
        if (args.length != 3){
            LocalDate today = LocalDate.now();
            LocalDate tPlus2 = today.plusDays(2);

            String todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
            String tPlus2Str = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(tPlus2);
            System.out.println(todayStr);
            System.out.println(tPlus2Str);


            throw new IllegalArgumentException("Correct format: ./simulate [borrow | lend] [# of proposals] [milliseconds between proposa]");
        }

        Boolean simulateLend = args[0].toLowerCase().equals("lend");
        int numProposals = Integer.parseInt(args[1]);
        Long intervalInMillisecs = Long.parseLong(args[2]);

        if (simulateLend){
            simulateLendRequests(numProposals, intervalInMillisecs);
        }
        else{
            System.out.println("We only simulate contract proposals from lenders for now...");
        }
    }

    private static void simulateLendRequests (int numProposals, Long intervalInMillisecs) throws URISyntaxException, IOException, InterruptedException
    {
        System.out.println("Start simulation");
        Map<String, String> formData = readFormData("src/main/java/com/personal/token/config.txt");
        Token token = getBearerToken(formData);
        for (int i = 0; i < numProposals; i++){
            ContractProposal contract = createContractProposal();
            postContractProposal(token, contract);
            wait(intervalInMillisecs);
        }
        System.out.println("Done");

    }
    
    private static void wait(Long interval) throws InterruptedException{
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e ){
            e.printStackTrace();
        }
    }
    
    private static Map<String, String> readFormData (String filename) throws FileNotFoundException{
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

    private static Token getBearerToken(Map<String, String> formData) throws URISyntaxException, IOException, InterruptedException
    {
        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageauth.equilend.com/auth/realms/1Source/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(getFormDataAsString(formData)))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());

        return gson.fromJson(postResponse.body(), Token.class);
    }
    
    private static String getFormDataAsString(Map<String, String> formData) {
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

    private static void postContractProposal(Token token, ContractProposal contract) throws URISyntaxException, IOException, InterruptedException
    {
        String contractJson = gson.toJson(contract);

        HttpRequest postRequest = HttpRequest
            .newBuilder()
            .uri(new URI("https://stageapi.equilend.com/v1/ledger/contracts"))
            .header("Authorization", "Bearer " + token.getAccess_token())
            .POST(BodyPublishers.ofString(contractJson))
            .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse);
    }

    private static Map<String, Object> createContractProposalParameters(){
        Map<String, Object> params = new HashMap<>();
        params.put("ticker", "MSFT");
        params.put("cusip", "594918104");
        return params;
    }

    /*
     * ADD: Get current date for trade date & settlement date
     * ADD: Randomize selection of defined financial instruments w info hard coded.. (MFST, AMZN, F, AAPL, etc)
     * ADD: define differences for borrower and lender... (can borrower propose contract?..)
     * ADD: Potentially accept a contract id to model the simulated contract proposals after...
     */
    private static ContractProposal createContractProposal(){
        Map<String, Object> params = createContractProposalParameters();
        
        Platform platform = new Platform("X", "Phone Brokered", "EXTERNAL", "0");
        List<VenueParty> venueParties = new ArrayList<>();
        VenueParty lenderVenueParty = new VenueParty(PartyRole.LENDER);
        venueParties.add(lenderVenueParty);
        VenueParty borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        venueParties.add(borrowerVenueParty);
        ExecutionVenue executionVenue = new ExecutionVenue(VenueType.OFFPLATFORM, platform, venueParties);

        Instrument instrument = new Instrument((String)params.get("ticker"), "594918104", "US5949181045", "2588173", "BBG001S5TD05", "MICROSOFT CORP COM" );
        Rate rate = new Rate(BigDecimal.valueOf(.125));
        Long quantity = Long.valueOf(25025);
        Currency billingCurrency = Currency.USD;
        BigDecimal dividendRatePct = BigDecimal.valueOf(100);
        String tradeDate = "2023-07-25";
        String settlementDate = "2023-07-27";
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
        
        Trade trade = new Trade(executionVenue, instrument, rate, quantity, billingCurrency, dividendRatePct, tradeDate, settlementDate, settlementType, collateral, transactingParties);


        List<Settlement> settlements = new ArrayList<Settlement>();
        
        List<LocalMarketFields> lenderLocalMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields lenderLocalMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        lenderLocalMarketFieldsList.add(lenderLocalMarketFields);
        Instruction lenderInstruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", lenderLocalMarketFieldsList);
        Settlement lenderSettlement = new Settlement(PartyRole.LENDER, lenderInstruction);
        settlements.add(lenderSettlement);
        
        List<LocalMarketFields> borrowerLocalMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields borrowerLocalMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        borrowerLocalMarketFieldsList.add(borrowerLocalMarketFields);
        Instruction borrowerInstruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", borrowerLocalMarketFieldsList);
        Settlement borrowerSettlement = new Settlement(PartyRole.BORROWER, borrowerInstruction);
        settlements.add(borrowerSettlement);       

        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;
    }

    private static List<Party> getAllParties (Token token) throws URISyntaxException, IOException, InterruptedException
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

    private static Party getPartyById (Token token, String id) throws URISyntaxException, IOException, InterruptedException
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

    private static List<Event> getAllEvents (Token token) throws URISyntaxException, IOException, InterruptedException
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
}
