titleInput = $("#title")
suggestionContainer = $("#title-autocomplete")

titleInput.on("input", suggest)
titleInput.on("keydown", upDown)

movies = ["test", "test2"]
suggestionCache = {}

idx = 0
function hover(i){
    idx = i;
    const suggestion = $("#suggestion" + i);
    suggestion.css("background-color", "gray")
}

function hoverOut(i){
    const suggestion = $("#suggestion" + i);
    suggestion.css("background-color", "white")
}

function handleSuggestionResult(resultData, title){
    const m = []
    const ids = []
    const res = resultData.movies;
    for (let i = 0; i < res.length; i++){
        m.push(res[i].title)
        ids.push(res[i].id)
    }
    movies = m;

    html = ""
    let c = 1;
    for (let i = 0; i < movies.length; i++){
        const t = movies[i];
        if (t !== title){
            html += "<div class=\"suggestion\" id=\"suggestion" + c + "\"onmouseover=\"hover(" + c + ")\"  onmouseleave=\"hoverOut(" + c + ")\" >";
            html += t;
            html += "</div>";
            c+=1;
        }
    }
    suggestionContainer.append(html);
    hover(0);
}

function updateSuggestedHtml(movies, title){
    suggestionContainer.empty();
    if (title.length == 0){
        return
    }
    const titles = new Set();
    titles.add(title);
    suggestionContainer.append("<div class=\"suggestion\" id=\"suggestion" + 0 + "\"onmouseover=\"hover(" + 0 + ")\"  onmouseleave=\"hoverOut(" + 0 + ")\" >" + title + "</div>");

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // url: "/api/movies" + ,
        url: "api/suggestion?title=" + title,
        success: (resultData) => handleSuggestionResult(resultData, title) // Setting callback function to handle data returned successfully by the StarsServlet
    });

    hover(0);
}

function suggest(e){
    const title = e.target.value
    const cache = {}

    if (title.length <= 3){
        //console.log("hiding")
        updateSuggestedHtml([], title)
    }
    else{
        //console.log(title)
        updateSuggestedHtml(movies, title)
    }
}

function upDown(e){
    //console.log(suggestionContainer.children().length)
    if (e.keyCode == 38){
        hoverOut(idx);
        hover(Math.max(0, idx-1));
    }
    else if (e.keyCode == 40){
        hoverOut(idx);
        hover(Math.min(idx+1, suggestionContainer.children().length-1));
    }

}