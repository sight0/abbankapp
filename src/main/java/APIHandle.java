import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.time.temporal.ChronoUnit.SECONDS;

public class APIHandle {

    // Singleton Class

    private static APIHandle apiHandle;

    private String access_token;
    private String refresh_token;

    // Cached Data from the API
    private String firstName;
    private String lastName;
    private String email;
    private int accountsNum;
    private int notificationsNum;
    private List<Map<String,String>> accounts = new ArrayList<>();
    private List<Map<String,String>> notifications = new ArrayList<>();
    private List<Map<String,String>> allnotifications = new ArrayList<>();

    private final String apiURI = "http://139.162.185.53:8080/";
//    private final String apiURI = "http://127.0.0.1:8080/";

    public static APIHandle getInstance(){
        if(apiHandle == null)
            apiHandle = new APIHandle();
        return apiHandle;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getNotificationsNum() {
        return notificationsNum;
    }

    public int getAccountsNum() {
        return accountsNum;
    }

    public List<Map<String, String>> getAccounts() {
        return accounts;
    }

    public List<Map<String, String>> getNotifications() {
        return notifications;
    }

    public String getApiURI() {
        return apiURI;
    }

    public HttpRequest.Builder request(String path, Boolean auth) throws URISyntaxException, IOException, InterruptedException {
        String uri = apiURI + path;
        HttpRequest.Builder request = HttpRequest.newBuilder(new URI(uri)).timeout(Duration.of(10, SECONDS));
        if(auth) request = request.header("Authorization", "Bearer ".concat(this.getAccess_token()));
        return request;
//        if(method.equals("GET")){
//            request = request.GET();
//            return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
//        }
//        if(method.equals("POST")) {
//            request = request.POST(HttpRequest.BodyPublishers.ofString)
//        }

    }

    public void refreshInformation(){
        try {
            String path = "customer/getInformation";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(JSON, Map.class);
            this.firstName = (String) map.get("firstName");
            this.lastName = (String) map.get("lastName");
            this.accountsNum = Integer.parseInt((String) map.get("numAccounts"));
            this.email = (String) map.get("email");
            this.accounts = (List<Map<String, String>>) map.get("accounts");
            this.notificationsNum = notifications.size();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<Map<String,String>>  refreshNotifications(){
        try {
            String path = "customer/getNotifications?all=false";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            this.notifications = mapper.readValue(JSON, List.class);
            this.notificationsNum = this.notifications.size();
            return this.notifications;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String,String>> getAllNotifications(){
        try {
            String path = "customer/getNotifications?all=true";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            this.allnotifications = mapper.readValue(JSON, List.class);
            return this.allnotifications;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void clearNotifications(){
        try {
            String path = "customer/seeNotifications";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            this.notificationsNum = 0;
            this.notifications = new ArrayList<>();
            refreshNotifications();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean editProfile(String pin, String mail, String password){
        try {
            String query = "";
            if(mail != null && password != null && pin != null) query = String.format("?pin=%s&email=%s&password=%s",pin,mail,password);
            if(mail != null && password != null && pin.equals("")) query = String.format("?email=%s&password=%s",mail,password);
            if(mail != null && password.equals("") && pin != null) query = String.format("?pin=%s&email=%s",pin,mail);
            if(mail != null && password.equals("")&& pin.equals("")) query = String.format("?email=%s",mail);
            if(mail.equals("") && password != null && pin != null) query = String.format("?pin=%s&password=%s",pin,password);
            if(mail.equals("") && password != null && pin.equals("")) query = String.format("?password=%s",password);
            if(mail.equals("") && password.equals("") && pin != null) query = String.format("?pin=%s",pin);
            if(mail .equals("") && password .equals("") && pin.equals("")) return false;
            String path = "customer/editProfile".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.statusCode() == 200;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean signup(String username, String firstname, String lastname, String email, String password){
        try {
            String query = String.format("?username=%s&email=%s&password=%s&firstname=%s&lastname=%s",username,email, password,firstname,lastname);
            String path = "signup".concat(query);
            HttpRequest.Builder req = request(path, false);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.statusCode() == 200;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Map<String,List<Map<String, String>>> getStatement(){
        try {
            String path = "customer/requestStatement";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String,List<Map<String, String>>> map = mapper.readValue(JSON, Map.class);
            return map;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, List<Map<String, String>>> getStatement(String accountNumber){
        try {
            String query = String.format("?accountNumber=%s",accountNumber.strip());
            String path = "customer/requestStatement".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String,List<Map<String, String>>> map = mapper.readValue(JSON, Map.class);
            return map;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public String transfer(String senderAccount, String receiverAccount, String amount){
        try {
            String query = String.format("?senderAccount=%s&receiverAccount=%s&amount=%s",senderAccount, receiverAccount, amount);
            String path = "customer/transfer".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.body();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getDebts(){
        try {
            String path = "customer/getDebt";
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.GET().header("Accept", "application/json").build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(JSON, Map.class);
            return (String) map.get("Total Debt");
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String payDebt(String accountNumber, String amount){
        try {
            String query = String.format("?accountNumber=%s&amount=%s",accountNumber.strip(), amount);
            String path = "customer/payDebt".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.body();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getDebts(String accountNumber){
        try {
            final String[] debt = new String[1];
            this.getAccounts().forEach((account) -> {
                if(account.get("accountNumber").equals(accountNumber))
                    debt[0] = account.get("accountDebt");
            });
            return debt[0];
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String requestAccount(String accountType){
        try {
            String query = String.format("?accountType=%s",accountType);
            String path = "customer/requestAccount".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.body();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String requestLoan(String accountNumber, String amount){
        try {
            String query = String.format("?accountNumber=%s&amount=%s",accountNumber, amount);
            String path = "customer/requestLoan".concat(query);
            HttpRequest.Builder req = request(path, true);
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            return res.body();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean signin(String username, String password){
        try {
            String query = String.format("?username=%s&password=%s", username, password);
            String path = "signin".concat(query);
            HttpRequest.Builder req = request(path, false);
//            ObjectMapper objectMapper = new ObjectMapper();
//            String requestBody = objectMapper
//                    .writerWithDefaultPrettyPrinter()
//                    .writeValueAsString(map);
//            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
            HttpRequest request = req.POST(HttpRequest.BodyPublishers.ofString("")).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            CompletableFuture<HttpResponse<String>> cf = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> res = cf.join();
            if(res.statusCode() == 403) return false;
            String JSON = res.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(JSON, Map.class);
            this.access_token = map.get("access_token");
            this.refresh_token = map.get("refresh_token");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}