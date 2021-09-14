package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookItApiUtils;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DBUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class ApiStepDefs {

    String token;
    Response response;
    String emailGlobal;
    @Given("I logged Bookit api using {string} and {string}")
    public void i_logged_Bookit_api_using_and(String email, String password) {

       token = BookItApiUtils.generateToken(email,password);
        emailGlobal = email;



    }

    @When("I get the current user information from api")
    public void i_get_the_current_user_information_from_api() {
        //send get request to retrieve current user information
        String url = ConfigurationReader.get("qa2api.uri")+"/api/users/me";

       response=     given().accept(ContentType.JSON)
                                     .and()
                                     .header("Authorization",token)
                               .when()
                                       .get(url);


    }

    @Then("status code should be {int}")
    public void status_code_should_be(int statusCode) {

        assertEquals(statusCode,response.statusCode());

    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match() {
        //API -DB
        //get information from database
        String query = "select id,firstname,lastname,role\n" +
                "from users\n" +
                "where email ='"+emailGlobal+"';";

        Map<String, Object> rowMap = DBUtils.getRowMap(query);
        System.out.println("rowMap = " + rowMap);
        long expectedId = (long) rowMap.get("id");
        String expectedFirstName = (String) rowMap.get("firstname");
        String expectedLastName = (String) rowMap.get("lastname");
        String expectedRole = (String) rowMap.get("role");

        //get information from api
        JsonPath jsonPath = response.jsonPath();

        long actualId = jsonPath.getLong("id");
        String actualFirstName = jsonPath.getString("firstName");
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");

        //compare API - DB
        assertEquals(expectedId,actualId);
        assertEquals(expectedFirstName,actualFirstName);
        assertEquals(expectedLastName,actualLastName);
        assertEquals(expectedRole,actualRole);




    }

    @Then("UI,API and Database user information must be match")
    public void ui_API_and_Database_user_information_must_be_match() {
        //API and DB
        //get information from database
        String query=" select u.id, u.firstname, u.lastname, role,t.name,t.batch_number,c.location from users u inner join team t \n" +
                " on u.team_id=t.id \n" +
                " inner join campus c\n" +
                " on u.campus_id=c.id \n" +
                " where u.email like '"+emailGlobal+"'";


        Map<String, Object> rowMap = DBUtils.getRowMap(query);
        System.out.println("rowMap = " + rowMap);
        long expectedid = (long)rowMap.get("id");
        int expectedBatchNumber = (int) rowMap.get("batch_number");
        String expectedFirstName = (String) rowMap.get("firstname");
        String expectedLastName = (String) rowMap.get("lastname");
        String expectedRole = (String) rowMap.get("role");
        String expectedCampusLocation= (String) rowMap.get("location");
        String expectedTeamName= (String) rowMap.get("name");

        //get information from api
        JsonPath jsonPath = response.jsonPath();

        long actualIdAPI = jsonPath.getLong("id");
        String actualFirstNameAPI = jsonPath.getString("firstName");
        String actualLastNameAPI = jsonPath.getString("lastName");
        String actualRoleAPI = jsonPath.getString("role");



        //GET INFORMATION FROM UI
        SelfPage selfPage = new SelfPage();
        String actualfullNameUI=selfPage.name.getText();
        String actualroleUI = selfPage.role.getText();
        String actualteamUI = selfPage.team.getText();
        int actualbatchIdUI = Integer.parseInt(selfPage.batch.getText().substring(1));
        String actualcampusUI = selfPage.campus.getText();


        //UI vs DB
        String expectedFullName=expectedFirstName+" "+expectedLastName;

     assertEquals(expectedFullName,actualfullNameUI);
     assertEquals(expectedRole,actualroleUI);
     assertEquals(expectedCampusLocation,actualcampusUI);
     assertEquals(expectedBatchNumber,actualbatchIdUI);
     assertEquals(expectedTeamName,actualteamUI);

        //compare API - DB
        assertEquals(expectedid,actualIdAPI);
        assertEquals(expectedFirstName,actualFirstNameAPI);
        assertEquals(expectedLastName,actualLastNameAPI);
        assertEquals(expectedRole,actualRoleAPI);


        //UI vs API
        String actualFullNameAPI=actualFirstNameAPI+" "+actualLastNameAPI;
     assertEquals(actualFullNameAPI,actualfullNameUI);
     assertEquals(actualRoleAPI,actualroleUI);


    }


}
