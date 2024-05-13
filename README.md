Demo URL: https://drive.google.com/file/d/1U4qNZfuiP4qTIoB8xfqNh45BZ2EZ6rUN/view

Contributions:

William:
XML parser

Matthew:
Employee dashboard


Optimizations:
1. Order of file parsing. First parsed actors file, to add as many actors as possible, as this file has the least conflicts. Followed by movies, which also has minimal relational inconsistencies. By saving the casts for last, we are able to skip requerying which leads us into the next optimization, we don't have to repeat and requery every actor/movie for every entry of the cast.
2. Saving missing actors and movies to hashmap. Since many missing entries are repeated, these can be skipped without requerying into the database to check if they are missing. Since all actors and movies were already added previously, no actors will be intentionally skipped, allowing us to skip around 6000 queries of missing data.
