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
function handleMovieResult(resultData) {
    let moviesTableBodyElement = jQuery("#movies_table_body");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="movie.html?id=' + resultData[i]['id'] + '">'
            + resultData[i]["title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genres"] + "</th>";
        let stars = resultData[i]["stars"].split(", ");
        let stars_id = resultData[i]["stars_id"].split(", ");
        let stars_info = build_stars(stars, stars_id);

        rowHTML += "<th>"
        for (let i = 0; i < stars.length; i++){
            if (i !== 0){
                rowHTML += "; "
            }
            rowHTML += '<a href="single-star.html?id=' + stars_info[stars[i]] + '">'
                + stars[i] + '</a>'
        }
        rowHTML += "</th>"

        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        moviesTableBodyElement.append(rowHTML);
    }
}

function build_stars(stars, stars_id) {
    let stars_dict = {};
    for (let i = 0; i < stars.length; i++) stars_dict[stars[i]] = stars_id[i];
    return stars_dict;
}


function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

let url = "api/movies"

const query_params = new Map()
const genre = getParameterByName("genre")
console.log(genre)
if (genre){
    query_params.set("genre",genre)
}
const startsWith = getParameterByName("startsWith")
if (startsWith){
    query_params.set("startsWith", startsWith)
}
let c = 0
console.log(query_params)
query_params.forEach((value, key) => {
    console.log(key)
    if (c === 0){
        url += "?"
    }
    else{
        url += "&"
    }
    url += key + "=" + value
})
console.log(url)

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: url, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});