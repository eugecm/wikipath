# wikipath

Authors: Eugenio Cano-Manuel Mendoza, Federico José Sörenson Sánchez.

A program to find a path between two Wikipedia articles. It is my first
clojure program. Makes use of [ubigraph](http://ubietylab.net/ubigraph/) to
display the explored tree.

Ubigraph server and client can be found
[here](http://ubietylab.net/ubigraph/content/Downloads/index.php). The client
can also be found within this repository as it is licensed under the Apache
License, Version 2.0. I cannot distribute the server, you'll have to download
it from the Ubigraph website.

## TODO

 * Provide a way to cache the results so we don't make unnecessary requests.
 * Refactor messy code, hard-coded strings and the like.
 * Make it independent of Ubigraph (and any other visualization method).
 * Implement more searching algorithms. Right now there's only depth-first.
 * Possibly make use of a more suited data-type to represent trees.

## Important

Wikipedia is supported by Wikimedia Foundation, a non-profit organization.
Help support Wikipedia by making a donation [here](https://donate.wikimedia.org/w/index.php?title=Special:FundraiserLandingPage&country=ES&uselang=en&utm_medium=spontaneous&utm_source=fr-redir&utm_campaign=spontaneous).
