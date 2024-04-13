/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(10, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";

        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_name"] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "</tr>";

        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "</tr>";

        let stars = "";
        for (let j = 0; j < Math.min(3, resultData[i]["stars"].length); j++){
            if (j >= 1){
                stars += ", ";
            }
            stars += resultData[i]["stars"][j];
        }

        rowHTML += "<th>" + stars + "</th>";
        rowHTML += "</tr>";

        let genres = "";
        for (let j = 0; j < Math.min(3, resultData[i]["genres"].length); j++){
            if (j >= 1){
                genres += ", ";
            }
            genres += resultData[i]["genres"][j];
        }

        rowHTML += "<th>" + genres + "</th>";
        rowHTML += "</tr>";

        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});