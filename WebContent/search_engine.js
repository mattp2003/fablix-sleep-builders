let cache_storage = {};

function handleLookup(query, doneCallback) {
    // Check if the query length is at least 3 characters
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
                console.log("lookup ajax error");
                console.log(errorData);
            }
        });
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    doneCallback({ suggestions: cache_storage[query] });

}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    //console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
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
