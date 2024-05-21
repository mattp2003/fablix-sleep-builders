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

function updateSuggestedHtml(movies, title){
    html = ""
    suggestionContainer.empty();
    if (title.length == 0){
        return
    }

    html += "<div class=\"suggestion\" id=\"suggestion" + 0 + "\"onmouseover=\"hover(" + 0 + ")\"  onmouseleave=\"hoverOut(" + 0 + ")\" >";
    html += title;
    html += "</div>"
    for (let i = 1; i < movies.length + 1; i++){
        html += "<div class=\"suggestion\" id=\"suggestion" + i + "\"onmouseover=\"hover(" + i + ")\"  onmouseleave=\"hoverOut(" + i + ")\" >";
        html += movies[i-1];
        html += "</div>"
    }
    suggestionContainer.append(html);
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
        hover(Math.max(-1, idx-1));
    }
    else if (e.keyCode == 40){
        hoverOut(idx);
        hover(Math.min(idx+1, suggestionContainer.children().length-1));
    }

}