# wikipath

A program to find a path between two Wikipedia articles. It is my first
clojure program. Makes use of [ubigraph](http://ubietylab.net/ubigraph/) to
display the explored tree.

## TODO

 * Provide a way to cache the results so we don't make unnecessary requests.
 * Refactor messy code, hard-coded strings and the like.
 * Implement more searching algorithms. Right now there's only depth-first.
 * Possibly make use of a more suited data-type to represent trees.

## Important

Wikipedia is supported by Wikimedia Foundation, a non-profit organization.
Help support Wikipedia by making a donation [here](https://donate.wikimedia.org/w/index.php?title=Special:FundraiserLandingPage&country=ES&uselang=en&utm_medium=spontaneous&utm_source=fr-redir&utm_campaign=spontaneous).
