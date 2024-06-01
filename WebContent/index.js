let cache_storage = {};

function handleLookup(query, doneCallback) {
    // Check if the query length is at least 3 characters
    console.log(query.length)
    if (query.length < 3) {
        console.log("Query too short for autocomplete (minimum 3 characters required)");
        return;
    }

    if (query in cache_storage) {
        console.log("Using cached results for query: " + query);
        doneCallback({ suggestions: cache_storage[query] });
    } else {
        console.log("sending AJAX request to backend Java Servlet");

        // AJAX request to the backend
        jQuery.ajax({
            "method": "GET",
            "url": "api/search?query=" + encodeURIComponent(query), // Use encodeURIComponent for proper encoding of the query
            "success": function(data) {
                console.log("lookup AJAX success");
                // Store it in the cache
                cache_storage[query] = JSON.parse(data);
                handleLookupAjaxSuccess(data, query, doneCallback);
            },
            "error": function(errorData) {
                console.log("lookup AJAX error");
                console.log(errorData);
            }
        });
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    doneCallback({ suggestions: cache_storage[query] });

}

function handleSelectSuggestion(suggestion) {
    const id = suggestion["data"]["movieId"]

    window.location.href = "./movie.html?id=" + id;
}

$('#autocomplete').autocomplete({
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300,
});

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);

    window.location.href = "./movies.html?title=" + query;
}

$('#autocomplete').keypress(function(event) {
    if (event.keyCode == 13) {
        handleNormalSearch($('#autocomplete').val())
    }
})

function handleStarts() {
    const starts = "0123456789abcdefghijklmnopqrstuvwxyz*";

    for (let i = 0; i < starts.length; i++) {
        let listItem = $("<li>");
        let link = $("<a>").attr("href", "./movies.html?startsWith=" + starts[i]).text(starts[i]);
        listItem.append(link);
        $("#starts-body").append(listItem);
    }
}

handleStarts();

function handleGenres(data){
    let genres = data["genres"]
    let list = jQuery("#genres-body");

    for (let i = 0; i < genres.length; i++) {
        let listItem = $("<li>");
        let link = $("<a>").attr("href", "./movies.html?genre=" + genres[i]).text(genres[i]);
        listItem.append(link);
        list.append(listItem);
    }
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleGenres(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});